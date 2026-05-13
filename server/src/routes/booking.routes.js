const router = require('express').Router();
const db = require('../config/db');
const { authenticate, requireRole } = require('../middleware/auth');

// GET /api/bookings  (ADMIN/STAFF: all, CUSTOMER: own)
router.get('/', authenticate, async (req, res) => {
  try {
    let rows;
    if (req.user.role === 'CUSTOMER') {
      [rows] = await db.query(
        `SELECT b.*, u.full_name, u.email
         FROM bookings b JOIN users u ON b.user_id = u.user_id
         WHERE b.user_id = ?`,
        [req.user.user_id]
      );
    } else {
      [rows] = await db.query(
        `SELECT b.*, u.full_name, u.email
         FROM bookings b JOIN users u ON b.user_id = u.user_id`
      );
    }

    // Attach tickets to each booking
    if (rows.length > 0) {
      const bookingIds = rows.map(r => r.booking_id);
      const [tickets] = await db.query(
        `SELECT t.*, p.full_name AS passenger_name, f.flight_code,
                s.seat_number, s.class_type, t.ticket_price
         FROM tickets t
         JOIN passengers p ON t.passenger_id = p.passenger_id
         JOIN flights f ON t.flight_id = f.flight_id
         JOIN seats s ON t.seat_id = s.seat_id
         WHERE t.booking_id IN (?)`,
        [bookingIds]
      );
      const ticketMap = {};
      tickets.forEach(t => {
        if (!ticketMap[t.booking_id]) ticketMap[t.booking_id] = [];
        ticketMap[t.booking_id].push(t);
      });
      rows = rows.map(b => ({ ...b, tickets: ticketMap[b.booking_id] || [] }));
    }

    res.json(rows);
  } catch (err) {
    res.status(500).json({ message: err.message });
  }
});

// GET /api/bookings/:id
router.get('/:id', authenticate, async (req, res) => {
  try {
    const [rows] = await db.query(
      `SELECT b.*, u.full_name, u.email
       FROM bookings b JOIN users u ON b.user_id = u.user_id
       WHERE b.booking_id = ?`,
      [req.params.id]
    );
    if (rows.length === 0) return res.status(404).json({ message: 'Không tìm thấy booking' });

    const booking = rows[0];
    if (req.user.role === 'CUSTOMER' && booking.user_id !== req.user.user_id)
      return res.status(403).json({ message: 'Không có quyền' });

    // Also fetch tickets for this booking
    const [tickets] = await db.query(
      `SELECT t.*, p.full_name AS passenger_name, f.flight_code, s.seat_number, s.class_type
       FROM tickets t
       JOIN passengers p ON t.passenger_id = p.passenger_id
       JOIN flights f ON t.flight_id = f.flight_id
       JOIN seats s ON t.seat_id = s.seat_id
       WHERE t.booking_id = ?`,
      [req.params.id]
    );
    res.json({ ...booking, tickets });
  } catch (err) {
    res.status(500).json({ message: err.message });
  }
});

// POST /api/bookings  — create booking + tickets in one transaction
router.post('/', authenticate, async (req, res) => {
  const { tickets } = req.body;
  // tickets: [{ passenger, flight_id, seat_id, ticket_price }]
  if (!tickets || tickets.length === 0)
    return res.status(400).json({ message: 'Cần ít nhất 1 vé' });

  const conn = await db.getConnection();
  try {
    await conn.beginTransaction();

    const total = tickets.reduce((sum, t) => sum + Number(t.ticket_price), 0);
    const [bookingResult] = await conn.query(
      'INSERT INTO bookings (user_id, total_amount) VALUES (?, ?)',
      [req.user.user_id, total]
    );
    const booking_id = bookingResult.insertId;

    for (const t of tickets) {
      // Insert passenger
      const [pResult] = await conn.query(
        `INSERT INTO passengers (full_name, gender, date_of_birth, passport_number, nationality)
         VALUES (?, ?, ?, ?, ?)`,
        [t.passenger.full_name, t.passenger.gender, t.passenger.date_of_birth,
         t.passenger.passport_number, t.passenger.nationality]
      );

      // Check trạng thái ghế trong flight_seats
      const [fsRows] = await conn.query(
        `SELECT flight_seat_id, seat_status FROM flight_seats
         WHERE flight_id = ? AND seat_id = ?`,
        [t.flight_id, t.seat_id]
      );
      if (fsRows.length > 0 && fsRows[0].seat_status !== 'AVAILABLE')
        throw new Error(`Ghế ${t.seat_id} không khả dụng (${fsRows[0].seat_status})`);

      await conn.query(
        `INSERT INTO tickets (booking_id, passenger_id, flight_id, seat_id, ticket_price)
         VALUES (?, ?, ?, ?, ?)`,
        [booking_id, pResult.insertId, t.flight_id, t.seat_id, t.ticket_price]
      );

      // Cập nhật flight_seats thành BOOKED
      if (fsRows.length > 0) {
        await conn.query(
          "UPDATE flight_seats SET seat_status = 'BOOKED' WHERE flight_seat_id = ?",
          [fsRows[0].flight_seat_id]
        );
      } else {
        await conn.query(
          "INSERT INTO flight_seats (flight_id, seat_id, seat_status) VALUES (?, ?, 'BOOKED')",
          [t.flight_id, t.seat_id]
        );
      }
    }

    await conn.commit();
    res.status(201).json({ message: 'Đặt vé thành công', booking_id });
  } catch (err) {
    await conn.rollback();
    res.status(400).json({ message: err.message });
  } finally {
    conn.release();
  }
});

// PATCH /api/bookings/:id/cancel
router.patch('/:id/cancel', authenticate, async (req, res) => {
  try {
    const [rows] = await db.query('SELECT * FROM bookings WHERE booking_id = ?', [req.params.id]);
    if (rows.length === 0) return res.status(404).json({ message: 'Không tìm thấy booking' });

    const booking = rows[0];
    if (req.user.role === 'CUSTOMER' && booking.user_id !== req.user.user_id)
      return res.status(403).json({ message: 'Không có quyền' });

    await db.query(
      "UPDATE bookings SET booking_status = 'CANCELLED' WHERE booking_id = ?",
      [req.params.id]
    );
    await db.query(
      "UPDATE tickets SET ticket_status = 'CANCELLED' WHERE booking_id = ?",
      [req.params.id]
    );

    // Giải phóng ghế trong flight_seats
    await db.query(
      `UPDATE flight_seats fs
       JOIN tickets t ON t.flight_id = fs.flight_id AND t.seat_id = fs.seat_id
       SET fs.seat_status = 'AVAILABLE'
       WHERE t.booking_id = ?`,
      [req.params.id]
    );
    res.json({ message: 'Hủy booking thành công' });
  } catch (err) {
    res.status(500).json({ message: err.message });
  }
});

// PATCH /api/bookings/:id/confirm  (ADMIN, STAFF)
router.patch('/:id/confirm', authenticate, requireRole('ADMIN', 'STAFF'), async (req, res) => {
  try {
    await db.query(
      "UPDATE bookings SET booking_status = 'CONFIRMED' WHERE booking_id = ?",
      [req.params.id]
    );
    res.json({ message: 'Xác nhận booking thành công' });
  } catch (err) {
    res.status(500).json({ message: err.message });
  }
});

module.exports = router;

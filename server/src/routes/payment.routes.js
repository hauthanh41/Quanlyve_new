const router = require('express').Router();
const db = require('../config/db');
const { authenticate, requireRole } = require('../middleware/auth');

// GET /api/payments  (ADMIN/STAFF)
router.get('/', authenticate, requireRole('ADMIN', 'STAFF'), async (req, res) => {
  try {
    const [rows] = await db.query(
      `SELECT p.*, b.user_id, u.full_name
       FROM payments p
       JOIN bookings b ON p.booking_id = b.booking_id
       JOIN users u ON b.user_id = u.user_id`
    );
    res.json(rows);
  } catch (err) {
    res.status(500).json({ message: err.message });
  }
});

// POST /api/payments  — create payment for a booking
router.post('/', authenticate, async (req, res) => {
  const { booking_id, payment_method, amount } = req.body;
  if (!booking_id || !payment_method || !amount)
    return res.status(400).json({ message: 'Thiếu thông tin thanh toán' });

  const conn = await db.getConnection();
  try {
    await conn.beginTransaction();

    const [bookings] = await conn.query('SELECT * FROM bookings WHERE booking_id = ?', [booking_id]);
    if (bookings.length === 0) throw new Error('Không tìm thấy booking');

    const booking = bookings[0];
    if (req.user.role === 'CUSTOMER' && booking.user_id !== req.user.user_id)
      throw new Error('Không có quyền thanh toán booking này');

    const [result] = await conn.query(
      `INSERT INTO payments (booking_id, payment_method, amount, payment_status)
       VALUES (?, ?, ?, 'PAID')`,
      [booking_id, payment_method, amount]
    );

    await conn.query(
      "UPDATE bookings SET booking_status = 'CONFIRMED' WHERE booking_id = ?",
      [booking_id]
    );

    await conn.commit();
    res.status(201).json({ message: 'Thanh toán thành công', payment_id: result.insertId });
  } catch (err) {
    await conn.rollback();
    res.status(400).json({ message: err.message });
  } finally {
    conn.release();
  }
});

// PATCH /api/payments/:id/refund  (ADMIN only)
router.patch('/:id/refund', authenticate, requireRole('ADMIN'), async (req, res) => {
  try {
    await db.query(
      "UPDATE payments SET payment_status = 'REFUNDED' WHERE payment_id = ?",
      [req.params.id]
    );
    res.json({ message: 'Hoàn tiền thành công' });
  } catch (err) {
    res.status(500).json({ message: err.message });
  }
});

module.exports = router;

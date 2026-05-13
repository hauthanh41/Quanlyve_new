const router = require('express').Router();
const db = require('../config/db');
const { authenticate, requireRole } = require('../middleware/auth');

// GET /api/flights  — public, supports ?from=&to=&date= search
router.get('/', async (req, res) => {
  const { from, to, date } = req.query;
  try {
    let rows;
    if (from || to || date) {
      const conditions = [];
      const params = [];
      if (from) {
        conditions.push('(dep.airport_code = ? OR dep.city LIKE ?)');
        params.push(from, `%${from}%`);
      }
      if (to) {
        conditions.push('(arr.airport_code = ? OR arr.city LIKE ?)');
        params.push(to, `%${to}%`);
      }
      if (date) {
        // Lọc theo ngày (DATE(departure_time) = 'yyyy-MM-dd')
        conditions.push('DATE(f.departure_time) = ?');
        params.push(date);
      }
      const where = conditions.length ? 'WHERE ' + conditions.join(' AND ') : '';
      [rows] = await db.query(`
        SELECT f.flight_id, f.flight_code, f.airplane_id,
               f.departure_airport_id, f.arrival_airport_id,
               f.departure_time, f.arrival_time, f.price, f.status,
               dep.airport_code AS departure_airport, dep.city AS departure_city,
               arr.airport_code AS arrival_airport, arr.city AS arrival_city,
               a.airplane_name
        FROM flights f
        JOIN airports dep ON f.departure_airport_id = dep.airport_id
        JOIN airports arr ON f.arrival_airport_id = arr.airport_id
        JOIN airplanes a ON f.airplane_id = a.airplane_id
        ${where}
      `, params);
    } else {
      [rows] = await db.query(`
        SELECT f.flight_id, f.flight_code, f.airplane_id,
               f.departure_airport_id, f.arrival_airport_id,
               f.departure_time, f.arrival_time, f.price, f.status,
               dep.airport_code AS departure_airport, dep.city AS departure_city,
               arr.airport_code AS arrival_airport, arr.city AS arrival_city,
               a.airplane_name
        FROM flights f
        JOIN airports dep ON f.departure_airport_id = dep.airport_id
        JOIN airports arr ON f.arrival_airport_id = arr.airport_id
        JOIN airplanes a ON f.airplane_id = a.airplane_id
      `);
    }
    res.json(rows.map(r => ({ ...r, price: parseFloat(r.price) || 0 })));
  } catch (err) {
    res.status(500).json({ message: err.message });
  }
});

// GET /api/flights/:id
router.get('/:id', async (req, res) => {
  try {
    const [rows] = await db.query(`
      SELECT f.flight_id, f.flight_code, f.airplane_id,
             f.departure_airport_id, f.arrival_airport_id,
             f.departure_time, f.arrival_time, f.price, f.status,
             dep.airport_code AS departure_airport, dep.city AS departure_city,
             arr.airport_code AS arrival_airport, arr.city AS arrival_city,
             a.airplane_name
      FROM flights f
      JOIN airports dep ON f.departure_airport_id = dep.airport_id
      JOIN airports arr ON f.arrival_airport_id = arr.airport_id
      JOIN airplanes a ON f.airplane_id = a.airplane_id
      WHERE f.flight_id = ?
    `, [req.params.id]);
    if (rows.length === 0) return res.status(404).json({ message: 'Không tìm thấy chuyến bay' });
    const row = rows[0];
    res.json({ ...row, price: parseFloat(row.price) || 0 });
  } catch (err) {
    res.status(500).json({ message: err.message });
  }
});

// Helper: tạo danh sách ghế theo cấu trúc chuẩn
// Business: hàng 1-2, cột A-F (12 ghế)
// Economy: hàng 10 trở đi, cột A-F
function buildSeats(airplane_id, totalSeats = 180) {
  const seats = [];
  // Business: rows 1-2, A-F
  for (let row = 1; row <= 2; row++) {
    for (const col of ['A', 'B', 'C', 'D', 'E', 'F']) {
      seats.push([airplane_id, `${row}${col}`, 'BUSINESS']);
    }
  }
  // Economy: rows 10 onward, A-F
  const ecoSeats = totalSeats - 12;
  const ecoRows = Math.ceil(ecoSeats / 6);
  for (let row = 10; row <= 9 + ecoRows; row++) {
    for (const col of ['A', 'B', 'C', 'D', 'E', 'F']) {
      seats.push([airplane_id, `${row}${col}`, 'ECONOMY']);
    }
  }
  return seats;
}

async function seedSeatsIfNeeded(db, airplane_id) {
  const [existing] = await db.query(
    'SELECT COUNT(*) as cnt FROM seats WHERE airplane_id = ?', [airplane_id]
  );
  if (existing[0].cnt === 0) {
    const [planes] = await db.query(
      'SELECT total_seats FROM airplanes WHERE airplane_id = ?', [airplane_id]
    );
    const totalSeats = planes[0]?.total_seats || 180;
    const seats = buildSeats(airplane_id, totalSeats);
    for (const seat of seats) {
      await db.query(
        'INSERT INTO seats (airplane_id, seat_number, class_type) VALUES (?, ?, ?)', seat
      );
    }
  }
}

async function seedFlightSeats(db, flight_id, airplane_id) {
  // Lấy tất cả seat_id của airplane
  const [seats] = await db.query(
    'SELECT seat_id FROM seats WHERE airplane_id = ?', [airplane_id]
  );
  if (seats.length === 0) return;

  // Batch insert vào flight_seats, bỏ qua nếu đã tồn tại
  for (const s of seats) {
    await db.query(
      `INSERT IGNORE INTO flight_seats (flight_id, seat_id, seat_status)
       VALUES (?, ?, 'AVAILABLE')`,
      [flight_id, s.seat_id]
    );
  }
}

// POST /api/flights  (ADMIN, STAFF)
router.post('/', authenticate, requireRole('ADMIN', 'STAFF'), async (req, res) => {
  const {
    flight_code, departure_airport_id, arrival_airport_id,
    airplane_id, departure_time, arrival_time, price, status
  } = req.body;

  if (!flight_code || !departure_airport_id || !arrival_airport_id || !airplane_id)
    return res.status(400).json({ message: 'Thiếu thông tin bắt buộc' });

  try {
    // Đếm số chuyến bay cùng route để tạo số thứ tự không trùng
    const base = flight_code; // VN-HANSGN từ client
    const [rows] = await db.query(
      "SELECT COUNT(*) as cnt FROM flights WHERE flight_code LIKE ?",
      [`${base}%`]
    );
    const seq = String(rows[0].cnt + 1).padStart(3, '0');
    const finalCode = `${base}-${seq}`;

    const [result] = await db.query(
      `INSERT INTO flights
        (flight_code, departure_airport_id, arrival_airport_id, airplane_id, departure_time, arrival_time, price, status)
       VALUES (?, ?, ?, ?, ?, ?, ?, ?)`,
      [finalCode, departure_airport_id, arrival_airport_id, airplane_id,
       departure_time, arrival_time, price, status || 'AVAILABLE']
    );

    await seedSeatsIfNeeded(db, airplane_id);
    await seedFlightSeats(db, result.insertId, airplane_id);

    res.status(201).json({ message: 'Thêm chuyến bay thành công', flight_id: result.insertId });
  } catch (err) {
    res.status(500).json({ message: err.message });
  }
});

// PUT /api/flights/:id  (ADMIN, STAFF)
router.put('/:id', authenticate, requireRole('ADMIN', 'STAFF'), async (req, res) => {
  const {
    flight_code, departure_airport_id, arrival_airport_id,
    airplane_id, departure_time, arrival_time, price, status
  } = req.body;

  try {
    await db.query(
      `UPDATE flights SET
        flight_code = ?, departure_airport_id = ?, arrival_airport_id = ?,
        airplane_id = ?, departure_time = ?, arrival_time = ?, price = ?, status = ?
       WHERE flight_id = ?`,
      [flight_code, departure_airport_id, arrival_airport_id, airplane_id,
       departure_time, arrival_time, price, status, req.params.id]
    );
    res.json({ message: 'Cập nhật chuyến bay thành công' });
  } catch (err) {
    res.status(500).json({ message: err.message });
  }
});

// DELETE /api/flights/:id  (ADMIN only)
router.delete('/:id', authenticate, requireRole('ADMIN'), async (req, res) => {
  try {
    await db.query('DELETE FROM flights WHERE flight_id = ?', [req.params.id]);
    res.json({ message: 'Xóa chuyến bay thành công' });
  } catch (err) {
    res.status(500).json({ message: err.message });
  }
});

module.exports = router;

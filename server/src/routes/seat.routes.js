const router = require('express').Router();
const db = require('../config/db');
const { authenticate, requireRole } = require('../middleware/auth');

// 1. Lấy ghế theo flight_id (Dành cho App Android)
// Route này: GET /api/seats/flight/:flight_id
router.get('/flight/:flight_id', async (req, res) => {
  const { flight_id } = req.params;

  // Nếu ID truyền vào là 0 hoặc không hợp lệ, trả về mảng trống luôn để tránh lỗi 404
  if (!flight_id || flight_id === '0') {
    return res.json([]);
  }

  try {
    // Tìm airplane_id từ chuyến bay
    const [flights] = await db.query('SELECT airplane_id FROM flights WHERE flight_id = ?', [flight_id]);

    if (flights.length === 0 || !flights[0].airplane_id) {
      return res.json([]); // Không có máy bay thì không có ghế, trả về mảng trống
    }

    const airplane_id = flights[0].airplane_id;

    // Lấy tất cả ghế của máy bay đó
    const [seats] = await db.query('SELECT * FROM seats WHERE airplane_id = ?', [airplane_id]);

    // Lấy các ghế đã bị đặt cho chuyến bay này
    const [booked] = await db.query(
      "SELECT seat_id FROM tickets WHERE flight_id = ? AND ticket_status != 'CANCELLED'",
      [flight_id]
    );
    const bookedIds = new Set(booked.map(r => r.seat_id));

    // Trả về danh sách ghế kèm trạng thái is_available
    res.json(seats.map(s => ({
      ...s,
      is_available: !bookedIds.has(s.seat_id)
    })));

  } catch (err) {
    console.error("Lỗi lấy ghế:", err);
    res.status(500).json({ message: "Lỗi server" });
  }
});

// 2. Lấy ghế theo airplane_id (Dùng cho trang Admin hoặc mục đích khác)
// Route này: GET /api/seats?airplane_id=...
router.get('/', async (req, res) => {
  const { airplane_id, flight_id } = req.query;
  if (!airplane_id) return res.status(400).json({ message: 'Cần airplane_id' });

  try {
    const [seats] = await db.query('SELECT * FROM seats WHERE airplane_id = ?', [airplane_id]);

    if (flight_id) {
      const [booked] = await db.query(
        "SELECT seat_id FROM tickets WHERE flight_id = ? AND ticket_status != 'CANCELLED'",
        [flight_id]
      );
      const bookedIds = new Set(booked.map(r => r.seat_id));
      return res.json(seats.map(s => ({ ...s, is_available: !bookedIds.has(s.seat_id) })));
    }
    res.json(seats);
  } catch (err) {
    res.status(500).json({ message: err.message });
  }
});

// 3. Thêm ghế (Chỉ ADMIN)
router.post('/', authenticate, requireRole('ADMIN'), async (req, res) => {
  const { airplane_id, seat_number, class_type } = req.body;
  try {
    const [result] = await db.query(
      'INSERT INTO seats (airplane_id, seat_number, class_type) VALUES (?, ?, ?)',
      [airplane_id, seat_number, class_type]
    );
    res.status(201).json({ message: 'Thêm ghế thành công', seat_id: result.insertId });
  } catch (err) {
    res.status(500).json({ message: err.message });
  }
});

module.exports = router;
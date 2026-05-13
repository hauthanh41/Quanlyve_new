const router = require('express').Router();
const db = require('../config/db');
const { authenticate, requireRole } = require('../middleware/auth');

// GET /api/airports  — public
router.get('/', async (req, res) => {
  try {
    const [rows] = await db.query('SELECT * FROM airports');
    res.json(rows);
  } catch (err) {
    res.status(500).json({ message: err.message });
  }
});

// POST /api/airports  (ADMIN)
router.post('/', authenticate, requireRole('ADMIN'), async (req, res) => {
  const { airport_code, airport_name, city, country } = req.body;
  try {
    const [result] = await db.query(
      'INSERT INTO airports (airport_code, airport_name, city, country) VALUES (?, ?, ?, ?)',
      [airport_code, airport_name, city, country]
    );
    res.status(201).json({ message: 'Thêm sân bay thành công', airport_id: result.insertId });
  } catch (err) {
    res.status(500).json({ message: err.message });
  }
});

// PUT /api/airports/:id  (ADMIN)
router.put('/:id', authenticate, requireRole('ADMIN'), async (req, res) => {
  const { airport_code, airport_name, city, country } = req.body;
  try {
    await db.query(
      'UPDATE airports SET airport_code=?, airport_name=?, city=?, country=? WHERE airport_id=?',
      [airport_code, airport_name, city, country, req.params.id]
    );
    res.json({ message: 'Cập nhật sân bay thành công' });
  } catch (err) {
    res.status(500).json({ message: err.message });
  }
});

// DELETE /api/airports/:id  (ADMIN)
router.delete('/:id', authenticate, requireRole('ADMIN'), async (req, res) => {
  try {
    await db.query('DELETE FROM airports WHERE airport_id = ?', [req.params.id]);
    res.json({ message: 'Xóa sân bay thành công' });
  } catch (err) {
    res.status(500).json({ message: err.message });
  }
});

module.exports = router;

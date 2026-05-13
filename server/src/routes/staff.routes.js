const router = require('express').Router();
const db = require('../config/db');
const { authenticate, requireRole } = require('../middleware/auth');

// GET /api/staff  (ADMIN)
router.get('/', authenticate, requireRole('ADMIN'), async (req, res) => {
  try {
    const [rows] = await db.query(
      `SELECT s.*, u.full_name, u.email, u.phone
       FROM staff s JOIN users u ON s.user_id = u.user_id`
    );
    res.json(rows);
  } catch (err) {
    res.status(500).json({ message: err.message });
  }
});

// POST /api/staff  (ADMIN)
router.post('/', authenticate, requireRole('ADMIN'), async (req, res) => {
  const { user_id, position, salary } = req.body;
  try {
    const [result] = await db.query(
      'INSERT INTO staff (user_id, position, salary) VALUES (?, ?, ?)',
      [user_id, position, salary]
    );
    // Update user role to STAFF
    await db.query("UPDATE users SET role = 'STAFF' WHERE user_id = ?", [user_id]);
    res.status(201).json({ message: 'Thêm nhân viên thành công', staff_id: result.insertId });
  } catch (err) {
    res.status(500).json({ message: err.message });
  }
});

// PUT /api/staff/:id  (ADMIN)
router.put('/:id', authenticate, requireRole('ADMIN'), async (req, res) => {
  const { position, salary } = req.body;
  try {
    await db.query(
      'UPDATE staff SET position = ?, salary = ? WHERE staff_id = ?',
      [position, salary, req.params.id]
    );
    res.json({ message: 'Cập nhật nhân viên thành công' });
  } catch (err) {
    res.status(500).json({ message: err.message });
  }
});

// DELETE /api/staff/:id  (ADMIN)
router.delete('/:id', authenticate, requireRole('ADMIN'), async (req, res) => {
  try {
    await db.query('DELETE FROM staff WHERE staff_id = ?', [req.params.id]);
    res.json({ message: 'Xóa nhân viên thành công' });
  } catch (err) {
    res.status(500).json({ message: err.message });
  }
});

module.exports = router;

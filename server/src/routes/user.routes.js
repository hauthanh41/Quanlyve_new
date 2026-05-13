const router = require('express').Router();
const db = require('../config/db');
const { authenticate, requireRole } = require('../middleware/auth');

// GET /api/users  (ADMIN only)
router.get('/', authenticate, requireRole('ADMIN'), async (req, res) => {
  try {
    const [rows] = await db.query(
      'SELECT user_id, full_name, email, phone, role, created_at FROM users'
    );
    res.json(rows);
  } catch (err) {
    res.status(500).json({ message: err.message });
  }
});

// GET /api/users/:id
router.get('/:id', authenticate, async (req, res) => {
  const { id } = req.params;
  if (req.user.role !== 'ADMIN' && req.user.user_id !== Number(id))
    return res.status(403).json({ message: 'Không có quyền' });

  try {
    const [rows] = await db.query(
      'SELECT user_id, full_name, email, phone, role, created_at FROM users WHERE user_id = ?',
      [id]
    );
    if (rows.length === 0) return res.status(404).json({ message: 'Không tìm thấy người dùng' });
    res.json(rows[0]);
  } catch (err) {
    res.status(500).json({ message: err.message });
  }
});

// PUT /api/users/:id
router.put('/:id', authenticate, async (req, res) => {
  const { id } = req.params;
  if (req.user.role !== 'ADMIN' && req.user.user_id !== Number(id))
    return res.status(403).json({ message: 'Không có quyền' });

  const { full_name, phone } = req.body;
  try {
    await db.query('UPDATE users SET full_name = ?, phone = ? WHERE user_id = ?', [full_name, phone, id]);
    res.json({ message: 'Cập nhật thành công' });
  } catch (err) {
    res.status(500).json({ message: err.message });
  }
});

// PUT /api/users/:id/password
router.put('/:id/password', authenticate, async (req, res) => {
  const { id } = req.params;
  if (req.user.role !== 'ADMIN' && req.user.user_id !== Number(id))
    return res.status(403).json({ message: 'Không có quyền' });

  const { current_password, new_password } = req.body;
  if (!current_password || !new_password)
    return res.status(400).json({ message: 'Thiếu mật khẩu' });
  if (new_password.length < 6)
    return res.status(400).json({ message: 'Mật khẩu mới phải ít nhất 6 ký tự' });

  try {
    const bcrypt = require('bcryptjs');
    const [rows] = await db.query('SELECT password FROM users WHERE user_id = ?', [id]);
    if (rows.length === 0) return res.status(404).json({ message: 'Không tìm thấy người dùng' });

    const match = await bcrypt.compare(current_password, rows[0].password);
    if (!match) return res.status(400).json({ message: 'Mật khẩu hiện tại không đúng' });

    const hashed = await bcrypt.hash(new_password, 10);
    await db.query('UPDATE users SET password = ? WHERE user_id = ?', [hashed, id]);
    res.json({ message: 'Đổi mật khẩu thành công' });
  } catch (err) {
    res.status(500).json({ message: err.message });
  }
});

// DELETE /api/users/:id  (ADMIN only)
router.delete('/:id', authenticate, requireRole('ADMIN'), async (req, res) => {
  try {
    await db.query('DELETE FROM users WHERE user_id = ?', [req.params.id]);
    res.json({ message: 'Xóa thành công' });
  } catch (err) {
    res.status(500).json({ message: err.message });
  }
});

module.exports = router;

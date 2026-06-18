const router = require('express').Router();
const db = require('../config/db');
const { authenticate } = require('../middleware/auth');

// Tạo bảng notifications nếu chưa có
async function ensureTable() {
  await db.query(`
    CREATE TABLE IF NOT EXISTS notifications (
      notification_id INT AUTO_INCREMENT PRIMARY KEY,
      user_id         INT NOT NULL,
      title           VARCHAR(200) NOT NULL,
      body            TEXT NOT NULL,
      type            VARCHAR(50) DEFAULT 'BOOKING',
      is_read         TINYINT(1) DEFAULT 0,
      created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
      FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE
    )
  `);
}
ensureTable().catch(console.error);

// Helper: tạo notification (dùng nội bộ từ các route khác)
async function createNotification(userId, title, body, type = 'BOOKING') {
  await db.query(
    'INSERT INTO notifications (user_id, title, body, type) VALUES (?, ?, ?, ?)',
    [userId, title, body, type]
  );
}

// GET /api/notifications — Lấy thông báo của user hiện tại
router.get('/', authenticate, async (req, res) => {
  try {
    const [rows] = await db.query(`
      SELECT notification_id AS id, title, body, type,
             CAST(is_read AS UNSIGNED) = 1 AS is_read, created_at
      FROM notifications
      WHERE user_id = ?
      ORDER BY created_at DESC
      LIMIT 50
    `, [req.user.user_id]);
    res.json(rows);
  } catch (err) {
    res.status(500).json({ message: err.message });
  }
});

// GET /api/notifications/unread-count
router.get('/unread-count', authenticate, async (req, res) => {
  try {
    const [rows] = await db.query(
      'SELECT COUNT(*) AS count FROM notifications WHERE user_id = ? AND is_read = 0',
      [req.user.user_id]
    );
    res.json({ count: rows[0].count });
  } catch (err) {
    res.status(500).json({ message: err.message });
  }
});

// PATCH /api/notifications/read-all — Đánh dấu tất cả đã đọc
router.patch('/read-all', authenticate, async (req, res) => {
  try {
    await db.query(
      'UPDATE notifications SET is_read = 1 WHERE user_id = ?',
      [req.user.user_id]
    );
    res.json({ message: 'OK' });
  } catch (err) {
    res.status(500).json({ message: err.message });
  }
});

// PATCH /api/notifications/:id/read
router.patch('/:id/read', authenticate, async (req, res) => {
  try {
    await db.query(
      'UPDATE notifications SET is_read = 1 WHERE notification_id = ? AND user_id = ?',
      [req.params.id, req.user.user_id]
    );
    res.json({ message: 'OK' });
  } catch (err) {
    res.status(500).json({ message: err.message });
  }
});

// POST /api/notifications/broadcast-flight — Admin gửi thông báo sự cố chuyến bay
// Body: { flight_id, title, body, type }
// Gửi đến tất cả hành khách có vé trên chuyến bay đó
router.post('/broadcast-flight', authenticate, async (req, res) => {
  const { authenticate: _, requireRole } = require('../middleware/auth');
  if (!['ADMIN', 'STAFF'].includes(req.user.role)) {
    return res.status(403).json({ message: 'Không có quyền' });
  }

  const { flight_id, title, body, type = 'FLIGHT_ALERT' } = req.body;
  if (!flight_id || !title || !body) {
    return res.status(400).json({ message: 'Thiếu flight_id, title hoặc body' });
  }

  try {
    // Lấy tất cả user có booking active trên chuyến bay này
    const [users] = await db.query(`
      SELECT DISTINCT b.user_id
      FROM bookings b
      JOIN tickets t ON t.booking_id = b.booking_id
      WHERE t.flight_id = ?
        AND b.booking_status != 'CANCELLED'
        AND t.ticket_status != 'CANCELLED'
    `, [flight_id]);

    if (users.length === 0) {
      return res.json({ message: 'Không có hành khách nào trên chuyến bay này', sent: 0 });
    }

    for (const u of users) {
      await createNotification(u.user_id, title, body, type);
    }

    res.json({ message: `Đã gửi thông báo đến ${users.length} hành khách`, sent: users.length });
  } catch (err) {
    res.status(500).json({ message: err.message });
  }
});

// POST /api/notifications/broadcast-all — Admin gửi thông báo đến tất cả customer
router.post('/broadcast-all', authenticate, async (req, res) => {
  if (!['ADMIN', 'STAFF'].includes(req.user.role)) {
    return res.status(403).json({ message: 'Không có quyền' });
  }

  const { title, body, type = 'SYSTEM' } = req.body;
  if (!title || !body) {
    return res.status(400).json({ message: 'Thiếu title hoặc body' });
  }

  try {
    const [users] = await db.query("SELECT user_id FROM users WHERE role = 'CUSTOMER'");
    for (const u of users) {
      await createNotification(u.user_id, title, body, type);
    }
    res.json({ message: `Đã gửi đến ${users.length} khách hàng`, sent: users.length });
  } catch (err) {
    res.status(500).json({ message: err.message });
  }
});

module.exports = router;
module.exports.createNotification = createNotification;

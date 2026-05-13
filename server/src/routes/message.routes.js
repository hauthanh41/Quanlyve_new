const router = require('express').Router();
const db = require('../config/db');
const { authenticate, requireRole } = require('../middleware/auth');

// Tạo bảng messages nếu chưa có
async function ensureTable() {
  await db.query(`
    CREATE TABLE IF NOT EXISTS messages (
      id           INT AUTO_INCREMENT PRIMARY KEY,
      sender_id    INT NOT NULL,
      receiver_id  INT NOT NULL,
      content      TEXT NOT NULL,
      is_read      TINYINT(1) DEFAULT 0,
      created_at   TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
      FOREIGN KEY (sender_id)   REFERENCES users(user_id) ON DELETE CASCADE,
      FOREIGN KEY (receiver_id) REFERENCES users(user_id) ON DELETE CASCADE
    )
  `);
}
ensureTable().catch(console.error);

// GET /api/messages/conversations  — Admin: danh sách user đã nhắn tin
router.get('/conversations', authenticate, requireRole('ADMIN'), async (req, res) => {
  try {
    const [rows] = await db.query(`
      SELECT
        u.user_id, u.full_name, u.email,
        (SELECT content FROM messages m2
         WHERE (m2.sender_id = u.user_id OR m2.receiver_id = u.user_id)
         ORDER BY m2.created_at DESC LIMIT 1) AS last_message,
        (SELECT created_at FROM messages m2
         WHERE (m2.sender_id = u.user_id OR m2.receiver_id = u.user_id)
         ORDER BY m2.created_at DESC LIMIT 1) AS last_time,
        SUM(CASE WHEN m.is_read = 0 AND m.receiver_id = ? THEN 1 ELSE 0 END) AS unread_count
      FROM users u
      JOIN messages m ON (m.sender_id = u.user_id OR m.receiver_id = u.user_id)
      WHERE u.role != 'ADMIN'
      GROUP BY u.user_id
      ORDER BY last_time DESC
    `, [req.user.user_id]);
    res.json(rows);
  } catch (err) {
    res.status(500).json({ message: err.message });
  }
});

// GET /api/messages/:userId  — Lấy lịch sử chat
// - Admin gọi: userId = customer_id → lấy chat giữa admin này và customer đó
// - Customer gọi: userId = chính mình → lấy tất cả chat của mình với bất kỳ admin nào
router.get('/:userId', authenticate, async (req, res) => {
  const { userId } = req.params;
  const myId = req.user.user_id;

  if (req.user.role !== 'ADMIN' && myId !== Number(userId)) {
    return res.status(403).json({ message: 'Không có quyền' });
  }

  try {
    let rows;
    if (req.user.role === 'ADMIN') {
      // Admin xem chat với customer cụ thể
      const customerId = Number(userId);
      [rows] = await db.query(`
        SELECT id AS message_id, sender_id, receiver_id, content,
               CAST(is_read AS UNSIGNED) = 1 AS is_read, created_at
        FROM messages
        WHERE (sender_id = ? AND receiver_id = ?)
           OR (sender_id = ? AND receiver_id = ?)
        ORDER BY created_at ASC
      `, [myId, customerId, customerId, myId]);

      // Đánh dấu đã đọc
      await db.query(
        'UPDATE messages SET is_read = 1 WHERE receiver_id = ? AND sender_id = ? AND is_read = 0',
        [myId, customerId]
      );
    } else {
      // Customer xem tất cả chat của mình với admin bất kỳ
      [rows] = await db.query(`
        SELECT id AS message_id, sender_id, receiver_id, content,
               CAST(is_read AS UNSIGNED) = 1 AS is_read, created_at
        FROM messages
        WHERE sender_id = ? OR receiver_id = ?
        ORDER BY created_at ASC
      `, [myId, myId]);

      // Đánh dấu đã đọc
      await db.query(
        'UPDATE messages SET is_read = 1 WHERE receiver_id = ? AND is_read = 0',
        [myId]
      );
    }

    res.json(rows);
  } catch (err) {
    res.status(500).json({ message: err.message });
  }
});

// POST /api/messages  — Gửi tin nhắn
router.post('/', authenticate, async (req, res) => {
  const { receiver_id, content } = req.body;
  if (!content?.trim()) return res.status(400).json({ message: 'Nội dung không được trống' });

  let actualReceiverId = receiver_id;

  if (!actualReceiverId && req.user.role !== 'ADMIN') {
    // Ưu tiên admin đã từng chat với customer này
    const [prevAdmin] = await db.query(`
      SELECT sender_id AS admin_id FROM messages
      WHERE receiver_id = ? AND sender_id IN (SELECT user_id FROM users WHERE role = 'ADMIN')
      ORDER BY created_at DESC LIMIT 1
    `, [req.user.user_id]);

    if (prevAdmin.length > 0) {
      actualReceiverId = prevAdmin[0].admin_id;
    } else {
      // Chưa có lịch sử → gửi đến admin đầu tiên
      const [admins] = await db.query("SELECT user_id FROM users WHERE role = 'ADMIN' ORDER BY user_id LIMIT 1");
      if (admins.length === 0) return res.status(404).json({ message: 'Không tìm thấy admin' });
      actualReceiverId = admins[0].user_id;
    }
  }

  if (!actualReceiverId) return res.status(400).json({ message: 'Thiếu receiver_id' });

  try {
    const [result] = await db.query(
      'INSERT INTO messages (sender_id, receiver_id, content) VALUES (?, ?, ?)',
      [req.user.user_id, actualReceiverId, content.trim()]
    );
    const [rows] = await db.query(
      `SELECT id AS message_id, sender_id, receiver_id, content,
              CAST(is_read AS UNSIGNED) = 1 AS is_read, created_at
       FROM messages WHERE id = ?`,
      [result.insertId]
    );
    res.status(201).json(rows[0]);
  } catch (err) {
    res.status(500).json({ message: err.message });
  }
});

module.exports = router;

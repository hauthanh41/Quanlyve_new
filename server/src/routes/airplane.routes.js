const router = require('express').Router();
const db = require('../config/db');
const { authenticate, requireRole } = require('../middleware/auth');

// GET /api/airplanes
router.get('/', async (req, res) => {
  try {
    const [rows] = await db.query('SELECT * FROM airplanes ORDER BY airplane_name');
    res.json(rows);
  } catch (err) {
    res.status(500).json({ message: err.message });
  }
});

module.exports = router;

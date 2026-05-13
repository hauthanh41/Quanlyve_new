/**
 * Seed seats for airplane_id 3, 4, 5, 6, 7.
 * Usage: node scripts/seed-seats-3to7.js
 */
require('dotenv').config({ path: require('path').join(__dirname, '../.env') });
const mysql = require('mysql2/promise');

const TARGET_AIRPLANE_IDS = [3, 4, 5, 6, 7];

async function run() {
  const db = await mysql.createConnection({
    host: process.env.DB_HOST,
    port: process.env.DB_PORT,
    user: process.env.DB_USER,
    password: process.env.DB_PASSWORD,
    database: process.env.DB_NAME,
  });

  for (const airplaneId of TARGET_AIRPLANE_IDS) {
    // Lấy thông tin máy bay
    const [planes] = await db.query(
      'SELECT * FROM airplanes WHERE airplane_id = ?',
      [airplaneId]
    );

    if (planes.length === 0) {
      console.log(`[SKIP] airplane_id=${airplaneId} — không tồn tại trong DB`);
      continue;
    }

    const plane = planes[0];

    // Kiểm tra đã có ghế chưa
    const [existing] = await db.query(
      'SELECT COUNT(*) as cnt FROM seats WHERE airplane_id = ?',
      [airplaneId]
    );
    if (existing[0].cnt > 0) {
      console.log(`[SKIP] ${plane.airplane_name} (id=${airplaneId}) — đã có ${existing[0].cnt} ghế`);
      continue;
    }

    const totalSeats = plane.total_seats || 180;
    const seats = [];

    // Business: hàng 1-2, cột A-F (12 ghế)
    for (let row = 1; row <= 2; row++) {
      for (const col of ['A', 'B', 'C', 'D', 'E', 'F']) {
        seats.push([airplaneId, `${row}${col}`, 'BUSINESS']);
      }
    }

    // Economy: hàng 10 trở đi, cột A-F
    const economySeats = totalSeats - 12;
    const economyRows = Math.ceil(economySeats / 6);
    for (let row = 10; row <= 9 + economyRows; row++) {
      for (const col of ['A', 'B', 'C', 'D', 'E', 'F']) {
        seats.push([airplaneId, `${row}${col}`, 'ECONOMY']);
      }
    }

    for (const seat of seats) {
      await db.query(
        'INSERT INTO seats (airplane_id, seat_number, class_type) VALUES (?, ?, ?)',
        seat
      );
    }
    console.log(`[OK] ${plane.airplane_name} (id=${airplaneId}) — thêm ${seats.length} ghế`);
  }

  await db.end();
  console.log('\nDone.');
}

run().catch(console.error);

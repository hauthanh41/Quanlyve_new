/**
 * Seed seats for all airplanes in the database.
 * Usage: node scripts/seed-seats.js
 */
require('dotenv').config({ path: require('path').join(__dirname, '../.env') });
const mysql = require('mysql2/promise');

async function run() {
  const db = await mysql.createConnection({
    host: process.env.DB_HOST,
    port: process.env.DB_PORT,
    user: process.env.DB_USER,
    password: process.env.DB_PASSWORD,
    database: process.env.DB_NAME,
  });

  const [airplanes] = await db.query('SELECT * FROM airplanes');

  for (const plane of airplanes) {
    const [existing] = await db.query(
      'SELECT COUNT(*) as cnt FROM seats WHERE airplane_id = ?',
      [plane.airplane_id]
    );
    if (existing[0].cnt > 0) {
      console.log(`[SKIP] ${plane.airplane_name} — seats already exist (${existing[0].cnt} seats)`);
      continue;
    }

    const seats = [];
    const totalSeats = plane.total_seats || 180;

    // Business: rows 1-2, A-F (12 seats)
    for (let row = 1; row <= 2; row++) {
      for (const col of ['A', 'B', 'C', 'D', 'E', 'F']) {
        seats.push([plane.airplane_id, `${row}${col}`, 'BUSINESS']);
      }
    }

    // Economy: rows 10 onward, A-F (6 per row)
    const economySeats = totalSeats - 12;
    const economyRows = Math.ceil(economySeats / 6);
    for (let row = 10; row <= 9 + economyRows; row++) {
      for (const col of ['A', 'B', 'C', 'D', 'E', 'F']) {
        seats.push([plane.airplane_id, `${row}${col}`, 'ECONOMY']);
      }
    }

    // Batch insert
    for (const seat of seats) {
      await db.query(
        'INSERT INTO seats (airplane_id, seat_number, class_type) VALUES (?, ?, ?)',
        seat
      );
    }
    console.log(`[OK] ${plane.airplane_name} (id=${plane.airplane_id}) — ${seats.length} seats added`);
  }

  await db.end();
  console.log('\nDone. Seats are ready.');
}

run().catch(console.error);

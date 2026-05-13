/**
 * Migration: Tạo bảng seats, flight_seats và seed dữ liệu.
 * Usage: node scripts/migrate-seats.js
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
    multipleStatements: true,
  });

  console.log('=== Migration: seats & flight_seats ===\n');

  // 1. Tạo bảng seats
  await db.query(`
    CREATE TABLE IF NOT EXISTS seats (
      seat_id      INT AUTO_INCREMENT PRIMARY KEY,
      airplane_id  INT          NOT NULL,
      seat_number  VARCHAR(10)  NOT NULL,
      class_type   ENUM('ECONOMY', 'BUSINESS', 'FIRST') DEFAULT 'ECONOMY',
      created_at   TIMESTAMP    DEFAULT CURRENT_TIMESTAMP,
      CONSTRAINT fk_seats_airplane
        FOREIGN KEY (airplane_id) REFERENCES airplanes(airplane_id)
        ON DELETE CASCADE,
      CONSTRAINT uq_seat_per_airplane
        UNIQUE (airplane_id, seat_number)
    )
  `);
  console.log('[OK] Bảng seats đã sẵn sàng.');

  // 2. Tạo bảng flight_seats
  await db.query(`
    CREATE TABLE IF NOT EXISTS flight_seats (
      flight_seat_id  INT AUTO_INCREMENT PRIMARY KEY,
      flight_id       INT NOT NULL,
      seat_id         INT NOT NULL,
      seat_status     ENUM('AVAILABLE', 'BOOKED', 'HOLD', 'BLOCKED') DEFAULT 'AVAILABLE',
      hold_expired_at DATETIME NULL,
      created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
      CONSTRAINT fk_flight_seats_flight
        FOREIGN KEY (flight_id) REFERENCES flights(flight_id)
        ON DELETE CASCADE,
      CONSTRAINT fk_flight_seats_seat
        FOREIGN KEY (seat_id) REFERENCES seats(seat_id)
        ON DELETE CASCADE,
      CONSTRAINT uq_flight_seat
        UNIQUE (flight_id, seat_id)
    )
  `);
  console.log('[OK] Bảng flight_seats đã sẵn sàng.');

  // 3. Seed ghế cho từng máy bay
  const [airplanes] = await db.query('SELECT * FROM airplanes');
  if (airplanes.length === 0) {
    console.log('\n[WARN] Không có máy bay nào. Bỏ qua seed ghế.');
    await db.end();
    return;
  }

  console.log(`\nTìm thấy ${airplanes.length} máy bay. Bắt đầu seed ghế...\n`);

  for (const plane of airplanes) {
    const [existing] = await db.query(
      'SELECT COUNT(*) AS cnt FROM seats WHERE airplane_id = ?',
      [plane.airplane_id]
    );
    if (existing[0].cnt > 0) {
      console.log(`[SKIP] ${plane.airplane_name} (id=${plane.airplane_id}) — đã có ${existing[0].cnt} ghế`);
      continue;
    }

    const seats = buildSeats(plane.airplane_id, plane.total_seats || 180);
    const values = seats.map(s => [s.airplaneId, s.seatNumber, s.classType]);
    await db.query(
      'INSERT INTO seats (airplane_id, seat_number, class_type) VALUES ?',
      [values]
    );
    console.log(`[OK] ${plane.airplane_name} (id=${plane.airplane_id}) — ${seats.length} ghế đã thêm`);
  }

  // 4. Seed flight_seats cho các chuyến bay hiện có
  const [flights] = await db.query('SELECT flight_id, airplane_id FROM flights');
  if (flights.length > 0) {
    console.log(`\nTìm thấy ${flights.length} chuyến bay. Seed flight_seats...\n`);
    for (const flight of flights) {
      const [existingFS] = await db.query(
        'SELECT COUNT(*) AS cnt FROM flight_seats WHERE flight_id = ?',
        [flight.flight_id]
      );
      if (existingFS[0].cnt > 0) {
        console.log(`[SKIP] Flight ${flight.flight_id} — đã có ${existingFS[0].cnt} flight_seats`);
        continue;
      }

      const [seats] = await db.query('SELECT seat_id FROM seats WHERE airplane_id = ?', [flight.airplane_id]);
      if (seats.length === 0) continue;

      const fsValues = seats.map(s => [flight.flight_id, s.seat_id, 'AVAILABLE']);
      await db.query(
        'INSERT INTO flight_seats (flight_id, seat_id, seat_status) VALUES ?',
        [fsValues]
      );
      console.log(`[OK] Flight ${flight.flight_id} — ${seats.length} flight_seats đã thêm`);
    }
  }

  await db.end();
  console.log('\n=== Migration hoàn tất. ===');
}

/**
 * Tạo danh sách ghế cho một máy bay.
 * - FIRST:    hàng 1-2, cột A-D  (8 ghế)
 * - BUSINESS: hàng 3-5, cột A-D  (12 ghế)
 * - ECONOMY:  hàng 6 trở đi, cột A-F
 */
function buildSeats(airplaneId, totalSeats) {
  const seats = [];

  // First class: rows 1-2, A-D
  for (let row = 1; row <= 2; row++) {
    for (const col of ['A', 'B', 'C', 'D']) {
      seats.push({ airplaneId, seatNumber: `${row}${col}`, classType: 'FIRST' });
    }
  }

  // Business: rows 3-5, A-D
  for (let row = 3; row <= 5; row++) {
    for (const col of ['A', 'B', 'C', 'D']) {
      seats.push({ airplaneId, seatNumber: `${row}${col}`, classType: 'BUSINESS' });
    }
  }

  // Economy: rows 6 onward, A-F
  const premiumCount = 8 + 12; // 20
  const economyCount = totalSeats - premiumCount;
  const economyRows = Math.ceil(economyCount / 6);
  for (let row = 6; row <= 5 + economyRows; row++) {
    for (const col of ['A', 'B', 'C', 'D', 'E', 'F']) {
      seats.push({ airplaneId, seatNumber: `${row}${col}`, classType: 'ECONOMY' });
    }
  }

  return seats;
}

run().catch(err => {
  console.error('[ERROR]', err.message);
  process.exit(1);
});

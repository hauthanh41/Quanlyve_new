/**
 * Run once to hash plain-text passwords already in the database.
 * Usage: node scripts/hash-passwords.js
 */
require('dotenv').config({ path: require('path').join(__dirname, '../.env') });
const bcrypt = require('bcryptjs');
const mysql = require('mysql2/promise');

async function run() {
  const db = await mysql.createConnection({
    host: process.env.DB_HOST,
    port: process.env.DB_PORT,
    user: process.env.DB_USER,
    password: process.env.DB_PASSWORD,
    database: process.env.DB_NAME,
  });

  const [users] = await db.query('SELECT user_id, email, password FROM users');

  for (const user of users) {
    // Skip already-hashed passwords (bcrypt hashes start with $2b$ or $2a$)
    if (user.password.startsWith('$2')) {
      console.log(`[SKIP] ${user.email} — already hashed`);
      continue;
    }

    const hashed = await bcrypt.hash(user.password, 10);
    await db.query('UPDATE users SET password = ? WHERE user_id = ?', [hashed, user.user_id]);
    console.log(`[OK]   ${user.email} — password hashed`);
  }

  await db.end();
  console.log('\nDone.');
}

run().catch(console.error);

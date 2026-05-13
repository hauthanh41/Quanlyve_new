require('dotenv').config({ path: require('path').join(__dirname, '../.env') });
const mysql = require('mysql2/promise');
mysql.createConnection({
  host: process.env.DB_HOST, port: process.env.DB_PORT,
  user: process.env.DB_USER, password: process.env.DB_PASSWORD,
  database: process.env.DB_NAME
}).then(async db => {
  const [msgs] = await db.query('SELECT id, sender_id, receiver_id, content, created_at FROM messages ORDER BY id DESC LIMIT 20');
  console.log('=== MESSAGES ===');
  console.log(JSON.stringify(msgs, null, 2));
  const [users] = await db.query("SELECT user_id, full_name, role FROM users");
  console.log('=== USERS ===');
  console.log(JSON.stringify(users, null, 2));
  await db.end();
}).catch(e => console.error(e.message));

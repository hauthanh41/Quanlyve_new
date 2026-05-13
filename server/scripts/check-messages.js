require('dotenv').config({ path: require('path').join(__dirname, '../.env') });
const mysql = require('mysql2/promise');
mysql.createConnection({
  host: process.env.DB_HOST, port: process.env.DB_PORT,
  user: process.env.DB_USER, password: process.env.DB_PASSWORD,
  database: process.env.DB_NAME
}).then(async db => {
  const [rows] = await db.query('DESCRIBE messages');
  console.log(JSON.stringify(rows, null, 2));
  await db.end();
}).catch(e => console.error(e.message));

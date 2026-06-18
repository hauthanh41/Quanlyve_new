require('dotenv').config({ path: require('path').join(__dirname, '../.env') });
const mysql = require('mysql2/promise');
mysql.createConnection({
  host: process.env.DB_HOST, port: process.env.DB_PORT,
  user: process.env.DB_USER, password: process.env.DB_PASSWORD,
  database: process.env.DB_NAME
}).then(async db => {
  const tables = ['bookings','flights','tickets','payments','passengers'];
  for (const t of tables) {
    const [rows] = await db.query(`DESCRIBE ${t}`);
    console.log(`\n=== ${t} ===`);
    rows.forEach(r => console.log(`  ${r.Field} (${r.Type}) ${r.Key==='PRI'?'PK':''}`));
  }
  await db.end();
}).catch(e => console.error(e.message));

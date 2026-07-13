require('dotenv').config();
const mysql = require('mysql2/promise');

console.log('🔌 Connecting to TiDB...');
console.log(`   Host: ${process.env.DB_HOST || 'localhost'}`);
console.log(`   Port: ${process.env.DB_PORT || 4000}`);
console.log(`   Database: ${process.env.DB_DATABASE || process.env.DB_NAME || 'pokemon_db'}`);
console.log(`   User: ${process.env.DB_USERNAME || process.env.DB_USER || 'root'}`);
console.log(`   SSL: ${process.env.DB_SSL || 'true'}`);

// Buat pool koneksi ke database MySQL local atau Cloud (TiDB)
const pool = mysql.createPool({
    host: process.env.DB_HOST || 'gateway01.ap-southeast-1.prod.aws.tidbcloud.com',
    user: process.env.DB_USERNAME || process.env.DB_USER || 'root',
    password: process.env.DB_PASSWORD || '',
    database: process.env.DB_DATABASE || process.env.DB_NAME || 'pokemon_db',
    port: parseInt(process.env.DB_PORT) || 4000,
    ssl: process.env.DB_SSL === 'true' ? { 
        minVersion: 'TLSv1.2', 
        rejectUnauthorized: true 
    } : undefined,
    waitForConnections: true,
    connectionLimit: 10,
    queueLimit: 0,
    enableKeepAlive: true,
    keepAliveInitialDelay: 0,
    connectTimeout: 30000,
    acquireTimeout: 30000
});

// Test koneksi (optional tapi bagus untuk debugging)
(async () => {
    try {
        const conn = await pool.getConnection();
        console.log('✅ Database connected successfully!');
        const [rows] = await conn.query('SELECT VERSION() as version');
        console.log(`📦 Database Version: ${rows[0].version}`);
        conn.release();
    } catch (err) {
        console.error('❌ Database connection failed!');
        console.error(`   Error: ${err.message}`);
        console.error('\n💡 Check your environment variables:');
        console.error(`   DB_HOST: ${process.env.DB_HOST || 'localhost'}`);
        console.error(`   DB_USERNAME: ${process.env.DB_USERNAME || process.env.DB_USER || 'root'}`);
        console.error(`   DB_DATABASE: ${process.env.DB_DATABASE || process.env.DB_NAME || 'pokemon_db'}`);
        console.error(`   DB_PORT: ${process.env.DB_PORT || 4000}`);
        console.error(`   DB_SSL: ${process.env.DB_SSL || 'true'}`);
    }
})();

module.exports = pool;
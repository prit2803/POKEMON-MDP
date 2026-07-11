require('dotenv').config();
const mysql = require('mysql2/promise');
const fs = require('fs');
const path = require('path');

async function seed() {
    console.log("Starting seeder...");

    try {
        // Create database first before requiring db.js
        const connection = await mysql.createConnection({
            host: process.env.DB_HOST || 'localhost',
            user: process.env.DB_USER || 'root',
            password: process.env.DB_PASSWORD || '',
            port: process.env.DB_PORT || 3306,
            ssl: process.env.DB_SSL === 'true' ? { minVersion: 'TLSv1.2', rejectUnauthorized: true } : undefined
        });
        await connection.query('CREATE DATABASE IF NOT EXISTS pokemon_db');
        await connection.end();
        console.log("Database ensured.");

        // Now safe to require db.js
        const db = require('./db');

        // Read schema and execute it to ensure tables exist
        const schema = fs.readFileSync(path.join(__dirname, 'schema.sql'), 'utf8');
        const statements = schema.split(';').filter(stmt => stmt.trim() !== '');

        for (let stmt of statements) {
            await db.query(stmt);
        }
        console.log("Schema applied successfully.");

        // Clear existing data
        await db.query('SET FOREIGN_KEY_CHECKS = 0');
        await db.query('TRUNCATE TABLE users');
        await db.query('TRUNCATE TABLE posts');
        await db.query('SET FOREIGN_KEY_CHECKS = 1');

        // 1. Seed Users
        console.log("Seeding users...");
        await db.query(`
            INSERT INTO users (username, email, password, role, isBanned, coins, streakCount, hasSelectedStarter, pokemonCaught, battleWon, trainerLevel) 
            VALUES 
            ('test', 'test@test.com', 'test', 'user', 0, 1000, 1, 0, 0, 0, 1),
            ('user1', 'user1@test.com', 'password', 'user', 0, 500, 0, 0, 0, 0, 1)
        `);

        // 2. Seed Posts
        console.log("Seeding posts...");
        await db.query(`
            INSERT INTO posts (title, description, price, category, isActive, stock, createdAt) 
            VALUES 
            ('Pokeball', 'A device for catching wild Pokemon.', 200, 'items', 1, 99, ?),
            ('Potion', 'Restores 20 HP.', 300, 'medicine', 1, 50, ?)
        `, [Date.now(), Date.now()]);

        console.log("Seeding completed successfully!");
        process.exit(0);
    } catch (err) {
        console.error("Seeding failed:", err);
        process.exit(1);
    }
}

seed();

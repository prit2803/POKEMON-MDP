require('dotenv').config();
const express = require('express');
const cors = require('cors');
const db = require('./db');

const app = express();
const PORT = process.env.PORT || 3000;

// ==========================================
// MIDDLEWARE
// ==========================================
app.use(cors());
app.use(express.json());

// Log all incoming requests
app.use((req, res, next) => {
    console.log(`[${new Date().toISOString()}] ${req.method} ${req.url}`);
    next();
});

// ==========================================
// ROOT & HEALTH ENDPOINTS
// ==========================================

// Root endpoint
app.get('/', (req, res) => {
    res.json({
        message: 'Pokemon Backend API is running! 🚀',
        version: '1.0.0',
        status: 'online',
        endpoints: {
            users: '/api/users',
            posts: '/api/posts',
            pokemon: '/api/pokemon',
            inventory: '/api/inventory',
            payments: '/api/payments',
            purchases: '/api/purchases',
            species: '/api/species',
            health: '/health'
        },
        timestamp: new Date().toISOString()
    });
});

// Health check endpoint
app.get('/health', async (req, res) => {
    try {
        // Test database connection
        await db.query('SELECT 1');
        res.status(200).json({
            status: 'ok',
            database: 'connected',
            uptime: process.uptime(),
            timestamp: new Date().toISOString()
        });
    } catch (err) {
        res.status(500).json({
            status: 'error',
            database: 'disconnected',
            error: err.message,
            timestamp: new Date().toISOString()
        });
    }
});

// ==========================================
// 1. USER ENDPOINTS
// ==========================================

// Insert User
app.post('/api/users', async (req, res) => {
    try {
        const u = req.body;
        const [result] = await db.query(
            `INSERT INTO users (username, email, password, role, isBanned, coins, lastClaimDate, streakCount, hasSelectedStarter, pokemonCaught, battleWon, trainerLevel, distance, nickname, team) 
             VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)`,
            [
                u.username, u.email, u.password, u.role || 'user', u.isBanned || 0,
                u.coins || 0, u.lastClaimDate || null, u.streakCount || 0, u.hasSelectedStarter || 0,
                u.pokemonCaught || 0, u.battleWon || 0, u.trainerLevel || 1, u.distance || 0.0,
                u.nickname || null, u.team || null
            ]
        );
        res.json({ id: result.insertId });
    } catch (err) {
        console.error(err);
        res.status(500).json({ error: err.message });
    }
});

// Update User
app.put('/api/users', async (req, res) => {
    try {
        const u = req.body;
        await db.query(
            `UPDATE users SET username=?, email=?, password=?, role=?, isBanned=?, coins=?, lastClaimDate=?, streakCount=?, hasSelectedStarter=?, pokemonCaught=?, battleWon=?, trainerLevel=?, distance=?, nickname=?, team=?
             WHERE id=?`,
            [
                u.username, u.email, u.password, u.role, u.isBanned, u.coins, u.lastClaimDate, u.streakCount, u.hasSelectedStarter,
                u.pokemonCaught, u.battleWon, u.trainerLevel, u.distance, u.nickname, u.team, u.id
            ]
        );
        res.json({ success: true });
    } catch (err) {
        console.error(err);
        res.status(500).json({ error: err.message });
    }
});

// Delete User
app.delete('/api/users/:id', async (req, res) => {
    try {
        const { id } = req.params;
        await db.query('DELETE FROM users WHERE id=?', [id]);
        res.json({ success: true });
    } catch (err) {
        console.error(err);
        res.status(500).json({ error: err.message });
    }
});

// User Login
app.post('/api/users/login', async (req, res) => {
    try {
        const { username, password } = req.body;
        const [rows] = await db.query(
            'SELECT * FROM users WHERE username = ? AND password = ? AND isBanned = 0 LIMIT 1',
            [username, password]
        );
        if (rows.length > 0) {
            res.json(rows[0]);
        } else {
            res.status(404).json(null);
        }
    } catch (err) {
        console.error(err);
        res.status(500).json({ error: err.message });
    }
});

// Check if username exists
app.get('/api/users/exists/:username', async (req, res) => {
    try {
        const { username } = req.params;
        const [rows] = await db.query('SELECT COUNT(*) as count FROM users WHERE username = ?', [username]);
        res.json(rows[0].count);
    } catch (err) {
        console.error(err);
        res.status(500).json({ error: err.message });
    }
});

// Get all users
app.get('/api/users', async (req, res) => {
    try {
        const [rows] = await db.query('SELECT * FROM users');
        res.json(rows);
    } catch (err) {
        console.error(err);
        res.status(500).json({ error: err.message });
    }
});

// Get total users count
app.get('/api/users/count', async (req, res) => {
    try {
        const [rows] = await db.query('SELECT COUNT(*) as count FROM users');
        res.json(rows[0].count);
    } catch (err) {
        console.error(err);
        res.status(500).json({ error: err.message });
    }
});

// Get banned users count
app.get('/api/users/banned/count', async (req, res) => {
    try {
        const [rows] = await db.query('SELECT COUNT(*) as count FROM users WHERE isBanned = 1');
        res.json(rows[0].count);
    } catch (err) {
        console.error(err);
        res.status(500).json({ error: err.message });
    }
});

// Get user by username
app.get('/api/users/by-username/:username', async (req, res) => {
    try {
        const { username } = req.params;
        const [rows] = await db.query('SELECT * FROM users WHERE username = ? LIMIT 1', [username]);
        if (rows.length > 0) {
            res.json(rows[0]);
        } else {
            res.status(404).json(null);
        }
    } catch (err) {
        console.error(err);
        res.status(500).json({ error: err.message });
    }
});

// Get user by ID
app.get('/api/users/:id', async (req, res) => {
    try {
        const { id } = req.params;
        const [rows] = await db.query('SELECT * FROM users WHERE id = ? LIMIT 1', [id]);
        if (rows.length > 0) {
            res.json(rows[0]);
        } else {
            res.status(404).json(null);
        }
    } catch (err) {
        console.error(err);
        res.status(500).json({ error: err.message });
    }
});

// Update banned status
app.put('/api/users/:id/banned', async (req, res) => {
    try {
        const { id } = req.params;
        const { status } = req.body;
        await db.query('UPDATE users SET isBanned = ? WHERE id = ?', [status, id]);
        res.json({ success: true });
    } catch (err) {
        console.error(err);
        res.status(500).json({ error: err.message });
    }
});

// Update trainer stats
app.put('/api/users/:id/stats', async (req, res) => {
    try {
        const { id } = req.params;
        const { pokemonCaught, trainerLevel, battleWon, distance } = req.body;
        await db.query(
            'UPDATE users SET pokemonCaught = ?, trainerLevel = ?, battleWon = ?, distance = ? WHERE id = ?',
            [pokemonCaught, trainerLevel, battleWon, distance, id]
        );
        res.json({ success: true });
    } catch (err) {
        console.error(err);
        res.status(500).json({ error: err.message });
    }
});

// Get specific user stats fields
app.get('/api/users/:id/pokemon-caught', async (req, res) => {
    try {
        const { id } = req.params;
        const [rows] = await db.query('SELECT pokemonCaught FROM users WHERE id = ?', [id]);
        res.json(rows.length > 0 ? rows[0].pokemonCaught : 0);
    } catch (err) {
        console.error(err);
        res.status(500).json({ error: err.message });
    }
});

app.get('/api/users/:id/trainer-level', async (req, res) => {
    try {
        const { id } = req.params;
        const [rows] = await db.query('SELECT trainerLevel FROM users WHERE id = ?', [id]);
        res.json(rows.length > 0 ? rows[0].trainerLevel : 1);
    } catch (err) {
        console.error(err);
        res.status(500).json({ error: err.message });
    }
});

app.get('/api/users/:id/battle-won', async (req, res) => {
    try {
        const { id } = req.params;
        const [rows] = await db.query('SELECT battleWon FROM users WHERE id = ?', [id]);
        res.json(rows.length > 0 ? rows[0].battleWon : 0);
    } catch (err) {
        console.error(err);
        res.status(500).json({ error: err.message });
    }
});

app.get('/api/users/:id/distance', async (req, res) => {
    try {
        const { id } = req.params;
        const [rows] = await db.query('SELECT distance FROM users WHERE id = ?', [id]);
        res.json(rows.length > 0 ? rows[0].distance : 0.0);
    } catch (err) {
        console.error(err);
        res.status(500).json({ error: err.message });
    }
});

// Update user coin
app.put('/api/users/:id/coins', async (req, res) => {
    try {
        const { id } = req.params;
        const { coins } = req.body;
        await db.query('UPDATE users SET coins = ? WHERE id = ?', [coins, id]);
        res.json({ success: true });
    } catch (err) {
        console.error(err);
        res.status(500).json({ error: err.message });
    }
});

// ==========================================
// 2. POST ENDPOINTS
// ==========================================

// Insert Post
app.post('/api/posts', async (req, res) => {
    try {
        const p = req.body;
        const [result] = await db.query(
            `INSERT INTO posts (title, description, price, category, imagePath, isActive, stock, createdAt)
             VALUES (?, ?, ?, ?, ?, ?, ?, ?)`,
            [p.title, p.description, p.price, p.category, p.imagePath || null, p.isActive !== undefined ? p.isActive : 1, p.stock || 0, p.createdAt || Date.now()]
        );
        res.json(result.insertId);
    } catch (err) {
        console.error(err);
        res.status(500).json({ error: err.message });
    }
});

// Update Post
app.put('/api/posts', async (req, res) => {
    try {
        const p = req.body;
        await db.query(
            `UPDATE posts SET title=?, description=?, price=?, category=?, imagePath=?, isActive=?, stock=?, createdAt=? WHERE id=?`,
            [p.title, p.description, p.price, p.category, p.imagePath, p.isActive, p.stock, p.createdAt, p.id]
        );
        res.json({ success: true });
    } catch (err) {
        console.error(err);
        res.status(500).json({ error: err.message });
    }
});

// Delete Post
app.delete('/api/posts', async (req, res) => {
    try {
        const p = req.body;
        await db.query('DELETE FROM posts WHERE id=?', [p.id]);
        res.json({ success: true });
    } catch (err) {
        console.error(err);
        res.status(500).json({ error: err.message });
    }
});

// Get all posts
app.get('/api/posts', async (req, res) => {
    try {
        const [rows] = await db.query('SELECT * FROM posts ORDER BY createdAt DESC');
        res.json(rows);
    } catch (err) {
        console.error(err);
        res.status(500).json({ error: err.message });
    }
});

// Get active posts
app.get('/api/posts/active', async (req, res) => {
    try {
        const [rows] = await db.query('SELECT * FROM posts WHERE isActive = 1 ORDER BY createdAt DESC');
        res.json(rows);
    } catch (err) {
        console.error(err);
        res.status(500).json({ error: err.message });
    }
});

// Get total posts count
app.get('/api/posts/count', async (req, res) => {
    try {
        const [rows] = await db.query('SELECT COUNT(*) as count FROM posts');
        res.json(rows[0].count);
    } catch (err) {
        console.error(err);
        res.status(500).json({ error: err.message });
    }
});

// Get post by ID
app.get('/api/posts/:id', async (req, res) => {
    try {
        const { id } = req.params;
        const [rows] = await db.query('SELECT * FROM posts WHERE id = ? LIMIT 1', [id]);
        if (rows.length > 0) {
            res.json(rows[0]);
        } else {
            res.status(404).json(null);
        }
    } catch (err) {
        console.error(err);
        res.status(500).json({ error: err.message });
    }
});

// Decrease post stock (atomically)
app.put('/api/posts/:id/decrease-stock', async (req, res) => {
    try {
        const { id } = req.params;
        const [result] = await db.query(
            'UPDATE posts SET stock = stock - 1 WHERE id = ? AND stock > 0',
            [id]
        );
        res.json(result.affectedRows);
    } catch (err) {
        console.error(err);
        res.status(500).json({ error: err.message });
    }
});

// ==========================================
// 3. USER INVENTORY ENDPOINTS
// ==========================================

// Get user inventory
app.get('/api/inventory/:userId', async (req, res) => {
    try {
        const { userId } = req.params;
        const [rows] = await db.query('SELECT * FROM user_inventory WHERE userId = ?', [userId]);
        res.json(rows);
    } catch (err) {
        console.error(err);
        res.status(500).json({ error: err.message });
    }
});

// Get single item in inventory
app.get('/api/inventory/:userId/:postId', async (req, res) => {
    try {
        const { userId, postId } = req.params;
        const [rows] = await db.query('SELECT * FROM user_inventory WHERE userId = ? AND postId = ? LIMIT 1', [userId, postId]);
        if (rows.length > 0) {
            res.json(rows[0]);
        } else {
            res.status(404).json(null);
        }
    } catch (err) {
        console.error(err);
        res.status(500).json({ error: err.message });
    }
});

// Insert into inventory
app.post('/api/inventory', async (req, res) => {
    try {
        const item = req.body;
        await db.query(
            'INSERT INTO user_inventory (userId, postId, quantity) VALUES (?, ?, ?)',
            [item.userId, item.postId, item.quantity]
        );
        res.json({ success: true });
    } catch (err) {
        console.error(err);
        res.status(500).json({ error: err.message });
    }
});

// Update inventory
app.put('/api/inventory', async (req, res) => {
    try {
        const item = req.body;
        await db.query(
            'UPDATE user_inventory SET userId=?, postId=?, quantity=? WHERE id=?',
            [item.userId, item.postId, item.quantity, item.id]
        );
        res.json({ success: true });
    } catch (err) {
        console.error(err);
        res.status(500).json({ error: err.message });
    }
});

// ==========================================
// 4. PAYMENT HISTORY ENDPOINTS
// ==========================================

// Insert payment
app.post('/api/payments', async (req, res) => {
    try {
        const p = req.body;
        await db.query(
            'INSERT INTO payment_history (userId, paymentMethod, coinAmount, totalPrice, status, transactionDate) VALUES (?, ?, ?, ?, ?, ?)',
            [p.userId, p.paymentMethod, p.coinAmount, p.totalPrice, p.status, p.transactionDate]
        );
        res.json({ success: true });
    } catch (err) {
        console.error(err);
        res.status(500).json({ error: err.message });
    }
});

// Get payment history
app.get('/api/payments/:userId', async (req, res) => {
    try {
        const { userId } = req.params;
        const [rows] = await db.query('SELECT * FROM payment_history WHERE userId = ? ORDER BY transactionDate DESC', [userId]);
        res.json(rows);
    } catch (err) {
        console.error(err);
        res.status(500).json({ error: err.message });
    }
});

// ==========================================
// 5. PURCHASE HISTORY ENDPOINTS
// ==========================================

// Insert purchase
app.post('/api/purchases', async (req, res) => {
    try {
        const p = req.body;
        await db.query(
            'INSERT INTO purchase_history (userId, postId, itemName, price, quantity, purchaseDate) VALUES (?, ?, ?, ?, ?, ?)',
            [p.userId, p.postId, p.itemName, p.price, p.quantity, p.purchaseDate || Date.now()]
        );
        res.json({ success: true });
    } catch (err) {
        console.error(err);
        res.status(500).json({ error: err.message });
    }
});

// Get purchase history
app.get('/api/purchases/:userId', async (req, res) => {
    try {
        const { userId } = req.params;
        const [rows] = await db.query('SELECT * FROM purchase_history WHERE userId = ? ORDER BY purchaseDate DESC', [userId]);
        res.json(rows);
    } catch (err) {
        console.error(err);
        res.status(500).json({ error: err.message });
    }
});

// ==========================================
// 6. POKEMON ENDPOINTS
// ==========================================

// Insert/Upsert Pokemon
app.post('/api/pokemon', async (req, res) => {
    try {
        const p = req.body;
        if (p.id && p.id > 0) {
            const [rows] = await db.query('SELECT id FROM pokemon_table WHERE id = ?', [p.id]);
            if (rows.length > 0) {
                await db.query(
                    `UPDATE pokemon_table SET userId=?, speciesId=?, name=?, hp=?, imageUrl=?, level=?, exp=?, isStarter=?, isLocked=?, caughtAt=? WHERE id=?`,
                    [p.userId, p.speciesId, p.name, p.hp, p.imageUrl, p.level, p.exp, p.isStarter, p.isLocked, p.caughtAt, p.id]
                );
                return res.json(p.id);
            }
        }
        
        const [result] = await db.query(
            `INSERT INTO pokemon_table (userId, speciesId, name, hp, imageUrl, level, exp, isStarter, isLocked, caughtAt)
             VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)`,
            [p.userId || 0, p.speciesId || 0, p.name, p.hp, p.imageUrl, p.level || 1, p.exp || 0, p.isStarter || 0, p.isLocked || 0, p.caughtAt || Date.now()]
        );
        res.json(result.insertId);
    } catch (err) {
        console.error(err);
        res.status(500).json({ error: err.message });
    }
});

// Update Pokemon
app.put('/api/pokemon', async (req, res) => {
    try {
        const p = req.body;
        await db.query(
            `UPDATE pokemon_table SET userId=?, speciesId=?, name=?, hp=?, imageUrl=?, level=?, exp=?, isStarter=?, isLocked=?, caughtAt=? WHERE id=?`,
            [p.userId, p.speciesId, p.name, p.hp, p.imageUrl, p.level, p.exp, p.isStarter, p.isLocked, p.caughtAt, p.id]
        );
        res.json({ success: true });
    } catch (err) {
        console.error(err);
        res.status(500).json({ error: err.message });
    }
});

// Get all pokemons
app.get('/api/pokemon', async (req, res) => {
    try {
        const [rows] = await db.query('SELECT * FROM pokemon_table');
        res.json(rows);
    } catch (err) {
        console.error(err);
        res.status(500).json({ error: err.message });
    }
});

// Get pokemon by user ID
app.get('/api/pokemon/user/:userId', async (req, res) => {
    try {
        const { userId } = req.params;
        const [rows] = await db.query('SELECT * FROM pokemon_table WHERE userId = ?', [userId]);
        res.json(rows);
    } catch (err) {
        console.error(err);
        res.status(500).json({ error: err.message });
    }
});

// Get starter pokemon by user ID
app.get('/api/pokemon/user/:userId/starter', async (req, res) => {
    try {
        const { userId } = req.params;
        const [rows] = await db.query('SELECT * FROM pokemon_table WHERE userId = ? AND isStarter = 1 LIMIT 1', [userId]);
        if (rows.length > 0) {
            res.json(rows[0]);
        } else {
            res.status(404).json(null);
        }
    } catch (err) {
        console.error(err);
        res.status(500).json({ error: err.message });
    }
});

// Delete Pokemon
app.delete('/api/pokemon', async (req, res) => {
    try {
        const p = req.body;
        await db.query('DELETE FROM pokemon_table WHERE id = ?', [p.id]);
        res.json({ success: true });
    } catch (err) {
        console.error(err);
        res.status(500).json({ error: err.message });
    }
});

// Delete all unlocked by user
app.delete('/api/pokemon/user/:userId/unlocked', async (req, res) => {
    try {
        const { userId } = req.params;
        await db.query('DELETE FROM pokemon_table WHERE userId = ? AND isLocked = 0', [userId]);
        res.json({ success: true });
    } catch (err) {
        console.error(err);
        res.status(500).json({ error: err.message });
    }
});

// Get owned species summary
app.get('/api/pokemon/user/:userId/owned-summary', async (req, res) => {
    try {
        const { userId } = req.params;
        const [rows] = await db.query(
            `SELECT speciesId, MAX(level) AS highestLevel, MIN(caughtAt) AS firstCaughtAt
             FROM pokemon_table
             WHERE userId = ? AND speciesId != 0
             GROUP BY speciesId`,
            [userId]
        );
        res.json(rows);
    } catch (err) {
        console.error(err);
        res.status(500).json({ error: err.message });
    }
});

// ==========================================
// 7. POKEDEX SPECIES ENDPOINTS
// ==========================================

// Insert list of species
app.post('/api/species/batch', async (req, res) => {
    try {
        const list = req.body;
        if (!list || list.length === 0) {
            return res.json({ success: true });
        }
        
        const values = list.map(s => [s.speciesId, s.name, s.imageUrl, s.type1, s.type2 || null]);
        await db.query(
            'INSERT INTO pokedex_species_cache (speciesId, name, imageUrl, type1, type2) VALUES ? ON DUPLICATE KEY UPDATE name=VALUES(name), imageUrl=VALUES(imageUrl), type1=VALUES(type1), type2=VALUES(type2)',
            [values]
        );
        res.json({ success: true });
    } catch (err) {
        console.error(err);
        res.status(500).json({ error: err.message });
    }
});

// Get range of species
app.get('/api/species/range', async (req, res) => {
    try {
        const { start, end } = req.query;
        const [rows] = await db.query(
            'SELECT * FROM pokedex_species_cache WHERE speciesId BETWEEN ? AND ? ORDER BY speciesId',
            [parseInt(start), parseInt(end)]
        );
        res.json(rows);
    } catch (err) {
        console.error(err);
        res.status(500).json({ error: err.message });
    }
});

// ==========================================
// 404 HANDLER (Route Not Found)
// ==========================================
app.use((req, res) => {
    res.status(404).json({
        error: 'Route not found',
        path: req.originalUrl,
        method: req.method,
        timestamp: new Date().toISOString()
    });
});

// ==========================================
// START SERVER
// ==========================================
if (require.main === module) {
    app.listen(PORT, '0.0.0.0', () => {
        console.log(`✅ Server is running on port ${PORT}`);
        console.log(`🌐 Health check: http://localhost:${PORT}/health`);
        console.log(`📖 API Docs: http://localhost:${PORT}/`);
    });
}

// ==========================================
// GRACEFUL SHUTDOWN
// ==========================================
process.on('SIGINT', async () => {
    console.log('🛑 Shutting down gracefully...');
    try {
        await db.end();
        console.log('✅ Database connection closed');
        process.exit(0);
    } catch (err) {
        console.error('❌ Error closing database:', err);
        process.exit(1);
    }
});

process.on('SIGTERM', async () => {
    console.log('🛑 Shutting down gracefully...');
    try {
        await db.end();
        console.log('✅ Database connection closed');
        process.exit(0);
    } catch (err) {
        console.error('❌ Error closing database:', err);
        process.exit(1);
    }
});

module.exports = app;
CREATE DATABASE IF NOT EXISTS pokemon_db;
USE pokemon_db;

-- 1. Users Table
CREATE TABLE IF NOT EXISTS users (
    id INT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(255) UNIQUE NOT NULL,
    email VARCHAR(255) NOT NULL,
    password VARCHAR(255) NOT NULL,
    role VARCHAR(50) DEFAULT 'user',
    isBanned INT DEFAULT 0,
    coins INT DEFAULT 0,
    lastClaimDate VARCHAR(255) NULL,
    streakCount INT DEFAULT 0,
    hasSelectedStarter INT DEFAULT 0,
    pokemonCaught INT DEFAULT 0,
    battleWon INT DEFAULT 0,
    trainerLevel INT DEFAULT 1,
    distance DOUBLE DEFAULT 0.0,
    nickname VARCHAR(255) NULL,
    team VARCHAR(255) NULL
);

-- 2. Posts Table
CREATE TABLE IF NOT EXISTS posts (
    id INT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    description TEXT NOT NULL,
    price DOUBLE NOT NULL,
    category VARCHAR(100) NOT NULL,
    imagePath VARCHAR(255) NULL,
    isActive INT DEFAULT 1,
    stock INT DEFAULT 0,
    createdAt BIGINT NOT NULL
);

-- 3. User Inventory Table
CREATE TABLE IF NOT EXISTS user_inventory (
    id INT AUTO_INCREMENT PRIMARY KEY,
    userId INT NOT NULL,
    postId INT NOT NULL,
    quantity INT DEFAULT 0,
    UNIQUE KEY unique_user_post (userId, postId)
);

-- 4. Payment History Table
CREATE TABLE IF NOT EXISTS payment_history (
    id INT AUTO_INCREMENT PRIMARY KEY,
    userId INT NOT NULL,
    paymentMethod VARCHAR(100) NOT NULL,
    coinAmount INT NOT NULL,
    totalPrice INT NOT NULL,
    status VARCHAR(50) NOT NULL,
    transactionDate BIGINT NOT NULL
);

-- 5. Purchase History Table
CREATE TABLE IF NOT EXISTS purchase_history (
    id INT AUTO_INCREMENT PRIMARY KEY,
    userId INT NOT NULL,
    postId INT NOT NULL,
    itemName VARCHAR(255) NOT NULL,
    price INT NOT NULL,
    quantity INT NOT NULL,
    purchaseDate BIGINT NOT NULL
);

-- 6. Pokemon Entity Table
CREATE TABLE IF NOT EXISTS pokemon_table (
    id INT AUTO_INCREMENT PRIMARY KEY,
    userId INT DEFAULT 0,
    speciesId INT DEFAULT 0,
    name VARCHAR(255) NOT NULL,
    hp INT NOT NULL,
    imageUrl VARCHAR(255) NOT NULL,
    level INT DEFAULT 1,
    exp INT DEFAULT 0,
    isStarter INT DEFAULT 0,
    isLocked INT DEFAULT 0,
    caughtAt BIGINT NOT NULL
);

-- 7. Pokedex Species Cache Table
CREATE TABLE IF NOT EXISTS pokedex_species_cache (
    speciesId INT PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    imageUrl VARCHAR(255) NOT NULL,
    type1 VARCHAR(100) NOT NULL,
    type2 VARCHAR(100) NULL
);

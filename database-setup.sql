-- Database setup script for two-factor authentication application
-- with Bell-Lapadula security model

-- Drop tables if they exist to ensure clean setup
DROP TABLE IF EXISTS news;
DROP TABLE IF EXISTS users;

-- Create users table
CREATE TABLE users (
    userid VARCHAR(64) NOT NULL PRIMARY KEY,
    firstname VARCHAR(64) NOT NULL,
    lastname VARCHAR(64) NOT NULL,
    salt VARCHAR(64) NOT NULL,
    password VARCHAR(64) NOT NULL,
    islocked TINYINT NOT NULL DEFAULT 0,
    faillogin TINYINT NOT NULL DEFAULT 0,
    otpsecret VARCHAR(64) NOT NULL,
    label TINYINT NOT NULL DEFAULT 1
);

-- Create news table
CREATE TABLE news (
    id INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    userid VARCHAR(64) NOT NULL,
    content TEXT NOT NULL,
    date DATETIME NOT NULL,
    label TINYINT NOT NULL DEFAULT 1,
    FOREIGN KEY (userid) REFERENCES users(userid)
);

-- Insert admin user with default password (change in production)
-- Default password is 'Admin123' with salt '0123456789abcdef0123456789abcdef'
-- Security level is Top Secret (4)
INSERT INTO users (userid, firstname, lastname, salt, password, islocked, faillogin, otpsecret, label)
VALUES (
    'admin',
    'System',
    'Administrator',
    '0123456789abcdef0123456789abcdef',
    -- This is a placeholder hash, should be generated correctly in production
    '5f4dcc3b5aa765d61d8327deb882cf99',
    0,
    0,
    -- Generate a proper OTP secret in production
    '0123456789abcdef0123456789abcdef',
    4
);

-- Create some sample security levels for testing
-- Insert sample users with different security levels (for testing only)
INSERT INTO users (userid, firstname, lastname, salt, password, islocked, faillogin, otpsecret, label)
VALUES 
    ('user1', 'John', 'Doe', '0123456789abcdef0123456789abcdef', '5f4dcc3b5aa765d61d8327deb882cf99', 0, 0, '0123456789abcdef0123456789abcdef', 1),
    ('user2', 'Jane', 'Smith', '0123456789abcdef0123456789abcdef', '5f4dcc3b5aa765d61d8327deb882cf99', 0, 0, '0123456789abcdef0123456789abcdef', 2),
    ('user3', 'Robert', 'Johnson', '0123456789abcdef0123456789abcdef', '5f4dcc3b5aa765d61d8327deb882cf99', 0, 0, '0123456789abcdef0123456789abcdef', 3);

-- Insert some sample news items with different security levels
INSERT INTO news (userid, content, date, label)
VALUES
    ('admin', 'Welcome to the secure system! This is visible to all users.', NOW(), 1),
    ('user2', 'Confidential information: Project X has been approved.', NOW(), 2),
    ('user3', 'Secret information: New security protocols will be implemented next month.', NOW(), 3),
    ('admin', 'Top Secret: Security breach detected in sector 7. Investigation ongoing.', NOW(), 4);

-- Create indexes for better performance
CREATE INDEX idx_users_userid ON users(userid);
CREATE INDEX idx_news_userid ON news(userid);
CREATE INDEX idx_news_date ON news(date);
CREATE INDEX idx_news_label ON news(label);
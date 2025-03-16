-- Database setup script for two-factor authentication application
-- with Bell-Lapadula security model

-- CREATE DATABASE secureapp; 
use secureapp;
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
-- Default password is 'Admin123'
-- OTP to add in Google Authenticator is '6UXVFVX45VZPURKDVODP33DP65Q4IYKV'
-- Security level is Top Secret (4)
INSERT INTO users (userid, firstname, lastname, salt, password, islocked, faillogin, otpsecret, label)
VALUES (
	'admin', 
    'System', 
    'Administrator', 
    '058a513fb26353d1189c1a364209fc9ff3758b1b327553944a534ca005b8ffc5', 
    '8176277866d97b7473e426575a934137cbdb6f35e6041e124a86a913a0ce4131', 
    '0', 
    '0', 
    'f52f52d6fced72fa4543ab86fdec6ff761c46155', 
    '4'
);

-- Create some sample security levels for testing
-- Insert sample users with different security levels (for testing only)
-- Insert Unclassified user with default password (change in production)
-- Default password is 'User123'
-- OTP to add in Google Authenticator is 'G7OJUTRSFFQEZDFXM22Y2CKY2RYO3QJT'
-- Security level is Unclassified (1)
INSERT INTO users (userid, firstname, lastname, salt, password, islocked, faillogin, otpsecret, label)
VALUES (
	'user1', 
    'Jone', 
    'Doe', 
    '8bb50a7d2c7d23503c0cb946b8a7e8a5f44a32c6a95df873c2f0702905803786', 
    '04616b2fbf41d42485e0fecfa6703bf84d9edcb8301367b57253b084517b3106', 
    '0', 
    '0', 
    '37dc9a4e3229604c8cb766b58d0958d470edc133', 
    '1'
);

-- Create some sample security levels for testing
-- Insert sample users with different security levels (for testing only)
-- Insert Confidential user with default password (change in production)
-- Default password is 'User123'
-- OTP to add in Google Authenticator is '34VUDQJNU2WCKQWDN36OC5RCH5ZV5YYB'
-- Security level is Confidential (2)
INSERT INTO users (userid, firstname, lastname, salt, password, islocked, faillogin, otpsecret, label)
VALUES (
	'user2', 
    'Mary', 
    'Jane', 
    '66925a16be8114d427f53956324d83fbc48a330295ba1f6b7a7f52609cd83b4c', 
    '67224bda546a3a49af1f236bcf4835046d4d9c9e09b4e38bb57fb6f3ccfd5b8f', 
    '0', 
    '0', 
    'df2b41c12da6ac2542c36efce176223f735ee301', 
    '2'
);

-- Create some sample security levels for testing
-- Insert sample users with different security levels (for testing only)
-- Insert Secret user with default password (change in production)
-- Default password is 'User123'
-- OTP to add in Google Authenticator is 'AVYD5VMEYT4WHTPXX67BMNSRSKKSUHT3'
-- Security level is Secret (3)
INSERT INTO users (userid, firstname, lastname, salt, password, islocked, faillogin, otpsecret, label)
VALUES (
	'user3', 
    'Alice', 
    'Keiichi', 
    '6187f9415bd9fa35bc045ac28c903cd607277b3524b6703152f44aa566bc2d34', 
    '6d3a4c9d28633bc7a87445391a8f6857633504e386352c5fb18cafbb9c1539db', 
    '0', 
    '0', 
    '05703ed584c4f963cdf7bfbe16365192952a1e7b', 
    '3'

);

-- Create some sample security levels for testing
-- Insert sample users with different security levels (for testing only)
-- Insert Top Secret user with default password (change in production)
-- Default password is 'User123'
-- OTP to add in Google Authenticator is '2UOIBGQFB2UESPLRVJ5UFSTLNS4B637I'
-- Security level is Top Secret (4)
INSERT INTO users (userid, firstname, lastname, salt, password, islocked, faillogin, otpsecret, label)
VALUES (
	'user4', 
    'Vuong', 
    'Do Quoc', 
    'e3cf18c06fe913fbdb427cbe9a48dc795d2017548639b5b0173e769db20562e5', 
    '7a6b8bd6ae830993dc84814487cd609211ef7afe1db00143cc16e0fc0069172c', 
    '0', 
    '0', 
    'd51c809a050ea8493d71aa7b42ca6b6cb81f6fe8', 
    '4'
);

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
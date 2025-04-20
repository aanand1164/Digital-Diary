CREATE DATABASE IF NOT EXISTS digital_diary;
USE digital_diary;

CREATE TABLE IF NOT EXISTS entries (
    id INT AUTO_INCREMENT PRIMARY KEY,
    date VARCHAR(20),
    title VARCHAR(100) UNIQUE,
    content TEXT
);

SELECT * FROM entries;
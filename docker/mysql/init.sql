CREATE DATABASE IF NOT EXISTS bookstore_auth_db;
CREATE DATABASE IF NOT EXISTS bookstore_user_db;
CREATE DATABASE IF NOT EXISTS bookstore_books_db;
CREATE DATABASE IF NOT EXISTS bookstore_order_db;
CREATE DATABASE IF NOT EXISTS bookstore_notification_db;
CREATE DATABASE IF NOT EXISTS bookstore_payment_db;

CREATE USER IF NOT EXISTS 'bookstore'@'%' IDENTIFIED BY 'bookstore';
GRANT ALL PRIVILEGES ON bookstore_auth_db.* TO 'bookstore'@'%';
GRANT ALL PRIVILEGES ON bookstore_user_db.* TO 'bookstore'@'%';
GRANT ALL PRIVILEGES ON bookstore_books_db.* TO 'bookstore'@'%';
GRANT ALL PRIVILEGES ON bookstore_order_db.* TO 'bookstore'@'%';
GRANT ALL PRIVILEGES ON bookstore_notification_db.* TO 'bookstore'@'%';
GRANT ALL PRIVILEGES ON bookstore_payment_db.* TO 'bookstore'@'%';
FLUSH PRIVILEGES;

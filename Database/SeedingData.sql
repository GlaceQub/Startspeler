-- Drop en hercreëer database
DROP DATABASE IF EXISTS startspelerdb;

CREATE DATABASE startspelerdb;

USE startspelerdb;

-- Tabellen aanmaken

CREATE TABLE `Status` (
    id INT AUTO_INCREMENT PRIMARY KEY,
   `name`VARCHAR(50) NOT NULL
);

CREATE TABLE `Role` (
    id INT AUTO_INCREMENT PRIMARY KEY,
   `name`VARCHAR(50) NOT NULL
);

CREATE TABLE `Group` (
    id INT AUTO_INCREMENT PRIMARY KEY,
   `name`VARCHAR(50) NOT NULL,
    discount FLOAT
);

CREATE TABLE Category (
    id INT AUTO_INCREMENT PRIMARY KEY,
   `name`VARCHAR(50) NOT NULL
);

CREATE TABLE `User` (
    id INT AUTO_INCREMENT PRIMARY KEY,
   `name`VARCHAR(100) NOT NULL UNIQUE,
    email VARCHAR(100),
    groupId INT NOT NULL,
    roleId INT NOT NULL,
    StatusId INT NOT NULL,
    createdAt DATETIME NOT NULL,
    FOREIGN KEY (groupId) REFERENCES `Group` (id),
    FOREIGN KEY (roleId) REFERENCES `Role` (id),
    FOREIGN KEY (StatusId) REFERENCES `Status` (id)
);

CREATE TABLE `Password` (
    id INT AUTO_INCREMENT PRIMARY KEY,
    UserId INT UNIQUE NOT NULL,
    passwordHash VARCHAR(255) NOT NULL,
    salt VARCHAR(255) NOT NULL,
    lastChanged DATETIME,
    FOREIGN KEY (UserId) REFERENCES `User` (id)
);

CREATE TABLE `Table` (
    id INT AUTO_INCREMENT PRIMARY KEY,
    number INT NOT NULL,
    StatusId INT NOT NULL,
    FOREIGN KEY (StatusId) REFERENCES `Status` (id)
);

CREATE TABLE Product (
    id INT AUTO_INCREMENT PRIMARY KEY,
   `name`VARCHAR(100) NOT NULL,
    categoryId INT NOT NULL,
    price FLOAT NOT NULL,
    popularity INT,
    FOREIGN KEY (categoryId) REFERENCES Category (id)
);

CREATE TABLE Inventory (
    id INT AUTO_INCREMENT PRIMARY KEY,
    productId INT NOT NULL,
    quantity INT NOT NULL,
    minimumQuantity INT,
    lastUpdated DATETIME NOT NULL,
    FOREIGN KEY (productId) REFERENCES Product (id)
);

CREATE TABLE `Order` (
    id INT AUTO_INCREMENT PRIMARY KEY,
    UserId INT NOT NULL,
    tableId INT NOT NULL,
    StatusId INT NOT NULL,
    totalPrice FLOAT NOT NULL,
    priceAfterDiscount FLOAT,
    createdAt DATETIME NOT NULL,
    isPlacedByStaff BOOLEAN NOT NULL,
    remarks VARCHAR(255),
    FOREIGN KEY (UserId) REFERENCES `User` (id),
    FOREIGN KEY (tableId) REFERENCES `Table` (id),
    FOREIGN KEY (StatusId) REFERENCES `Status` (id)
);

CREATE TABLE Orderitem (
    id INT AUTO_INCREMENT PRIMARY KEY,
    orderId INT NOT NULL,
    productId INT NOT NULL,
    quantity INT NOT NULL,
    price FLOAT NOT NULL,
    FOREIGN KEY (orderId) REFERENCES `Order` (id),
    FOREIGN KEY (productId) REFERENCES Product (id)
);

-- Testdata invoegen
INSERT INTO
    `Status` (`name`)
VALUES ('actief'),
    ('inactief'),
    ('in behandeling'),
    ('betaald');

INSERT INTO
    `Role` (`name`)
VALUES ('beheerder'),
    ('medewerker'),
    ('klant');

INSERT INTO
    `Group` (`name`, discount)
VALUES ('standaard', 0),
    ('VIP', 10),
    ('Community Managers', 7.5);

INSERT INTO Category (`name`) VALUES ('Bier'), ('Wijn'), ('Frisdrank');

INSERT INTO `Table` (`number`, StatusId) VALUES (1, 1), (2, 2), (3, 1);

INSERT INTO
    `Product` (
        `name`,
        categoryId,
        price,
        popularity
    )
VALUES ('Stella', 1, 3.5, 10),
    ('Coca-Cola', 3, 2.5, 20),
    ('Fanta', 3, 2.5, 20),
    ('Sprite', 3, 2.5, 20),
    ('Merlot', 2, 4.0, 5);

INSERT INTO
    Inventory (
        productId,
        quantity,
        minimumQuantity,
        lastUpdated
    )
VALUES (1, 100, 10, NOW()),
    (2, 200, 60, NOW()),
    (3, 200, 20, NOW()),
    (4, 200, 20, NOW()),
    (5, 50, 5, NOW());

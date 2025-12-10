-- Drop en hercreëer database
DROP DATABASE IF EXISTS startspelerdb;

CREATE DATABASE startspelerdb;

USE startspelerdb;

-- Tabellen aanmaken
CREATE TABLE STATUS (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(50) NOT NULL
);

CREATE TABLE ROLE (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(50) NOT NULL
);

CREATE TABLE `GROUP` (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(50) NOT NULL,
    discount FLOAT
);

CREATE TABLE CATEGORY (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(50) NOT NULL
);

CREATE TABLE USER (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE,
    email VARCHAR(100),
    groupId INT NOT NULL,
    roleId INT NOT NULL,
    FOREIGN KEY (groupId) REFERENCES `GROUP` (id),
    FOREIGN KEY (roleId) REFERENCES ROLE (id)
);

CREATE TABLE PASSWORD (
    id INT AUTO_INCREMENT PRIMARY KEY,
    userId INT UNIQUE NOT NULL,
    passwordHash VARCHAR(255) NOT NULL,
    salt VARCHAR(255) NOT NULL,
    lastChanged DATETIME,
    FOREIGN KEY (userId) REFERENCES USER (id)
);

CREATE TABLE TABLES (
    id INT AUTO_INCREMENT PRIMARY KEY,
    number INT NOT NULL,
    statusId INT NOT NULL,
    FOREIGN KEY (statusId) REFERENCES STATUS (id)
);

CREATE TABLE PRODUCT (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    categoryId INT NOT NULL,
    price FLOAT NOT NULL,
    popularity INT,
    FOREIGN KEY (categoryId) REFERENCES CATEGORY (id)
);

CREATE TABLE INVENTORY (
    id INT AUTO_INCREMENT PRIMARY KEY,
    productId INT NOT NULL,
    quantity INT NOT NULL,
    minimumQuantity INT,
    lastUpdated DATETIME NOT NULL,
    FOREIGN KEY (productId) REFERENCES PRODUCT (id)
);

CREATE TABLE ORDER_TABLE (
    id INT AUTO_INCREMENT PRIMARY KEY,
    userId INT NOT NULL,
    tableId INT NOT NULL,
    statusId INT NOT NULL,
    totalPrice FLOAT NOT NULL,
    priceAfterDiscount FLOAT,
    createdAt DATETIME NOT NULL,
    isPlacedByStaff BOOLEAN NOT NULL,
    remarks VARCHAR(255),
    FOREIGN KEY (userId) REFERENCES USER (id),
    FOREIGN KEY (tableId) REFERENCES TABLES (id),
    FOREIGN KEY (statusId) REFERENCES STATUS (id)
);

CREATE TABLE ORDERITEM (
    id INT AUTO_INCREMENT PRIMARY KEY,
    orderId INT NOT NULL,
    productId INT NOT NULL,
    quantity INT NOT NULL,
    price FLOAT NOT NULL,
    FOREIGN KEY (orderId) REFERENCES ORDER_TABLE (id),
    FOREIGN KEY (productId) REFERENCES PRODUCT (id)
);

-- Testdata invoegen
INSERT INTO
    STATUS (name)
VALUES ('actief'),
    ('inactief'),
    ('in behandeling'),
    ('betaald');

INSERT INTO
    ROLE (name)
VALUES ('beheerder'),
    ('medewerker'),
    ('klant');

INSERT INTO
    `GROUP` (name, discount)
VALUES ('standaard', 0),
    ('VIP', 10),
    ('Community Managers', 7.5);

INSERT INTO CATEGORY (name) VALUES ('Bier'), ('Wijn'), ('Frisdrank');

INSERT INTO
    USER (name, email, groupId, roleId)
VALUES (
        'Anouk',
        'anouk@voorbeeld.nl',
        1,
        1
    ),
    (
        'Bram',
        'bram@voorbeeld.nl',
        2,
        2
    ),
    ('Charlotte', NULL, 3, 3);

INSERT INTO
    PASSWORD (
        userId,
        passwordHash,
        salt,
        lastChanged
    )
VALUES (
        1,
        'wachtwoord1',
        'salt1',
        NOW()
    ),
    (
        2,
        'wachtwoord2',
        'salt2',
        NOW()
    );

INSERT INTO TABLES (number, statusId) VALUES (1, 1), (2, 2), (3, 1);

INSERT INTO
    PRODUCT (
        name,
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
    INVENTORY (
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

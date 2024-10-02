show databases;
create database billnbox;
use billnbox;


-- 1. Bill Table
CREATE TABLE Bill (
    BillID INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    EmpID INT,
    OwnerID INT,
    Time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CustomerName VARCHAR(255),
    Amount DECIMAL(10, 2),
    InvoiceNo VARCHAR(10),
    FOREIGN KEY (EmpID) REFERENCES Employee(EmpID),
    FOREIGN KEY (OwnerID) REFERENCES Owner(OwnerID)
);

-- 2. Employee Table
CREATE TABLE Employee (
    EmpID INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    Username VARCHAR(50),
    Password VARCHAR(50),
    Name VARCHAR(50),
    EmailID VARCHAR(50),
    PhoneNo VARCHAR(10),
    OwnerID INT,
    FOREIGN KEY (OwnerID) REFERENCES Owner(OwnerID)
);

-- 3. Orders Table
CREATE TABLE Orders (
    OrderID INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    BillID INT,
    SrNo INT,
    ProductName VARCHAR(100),
    Quantity INT NOT NULL,
    TotalPrice DECIMAL(10, 2) NOT NULL,
    OwnerID INT,
    FOREIGN KEY (BillID) REFERENCES Bill(BillID),
    FOREIGN KEY (OwnerID) REFERENCES Owner(OwnerID)
);

-- 4. Owner Table
CREATE TABLE Owner (
    OwnerID INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    Username VARCHAR(50),
    Password VARCHAR(50),
    Name VARCHAR(50),
    EmailID VARCHAR(50),
    PhoneNo VARCHAR(10),
    ShopName VARCHAR(50),
    ShopAddress VARCHAR(100)
);

-- 5. Product Table
CREATE TABLE Product (
    SrNo INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    ProductName VARCHAR(100) NOT NULL,
    Category VARCHAR(50) NOT NULL,
    Quantity INT NOT NULL DEFAULT 0,
    Price DECIMAL(10, 2) NOT NULL,
    MfgDate DATE,
    ExpDate DATE,
    LowQuantityAlert INT DEFAULT 1,
    OwnerID INT,
    FOREIGN KEY (OwnerID) REFERENCES Owner(OwnerID)
);

-- 6. Logs Table
CREATE TABLE logs (
    id INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    date DATE NOT NULL,
    time TIME NOT NULL,
    User VARCHAR(25),
    activity TEXT NOT NULL,
    OwnerID INT,
    FOREIGN KEY (OwnerID) REFERENCES Owner(OwnerID)
);

show tables;


delimiter ..
CREATE TRIGGER after_employee_insert
AFTER INSERT ON Employee
FOR EACH ROW
BEGIN
    DECLARE ownerUsername VARCHAR(255);
    
    -- Fetch the Username of the Owner based on the OwnerID of the new Employee
    SELECT Username INTO ownerUsername FROM Owner WHERE OwnerID = NEW.OwnerID;

    -- Ensure that NEW.Username is not NULL
    IF NEW.Username IS NOT NULL THEN
        -- Insert into logs with the fetched ownerUsername
        INSERT INTO logs (date, time, user, activity, OwnerID)
        VALUES (CURRENT_DATE, CURRENT_TIME, ownerUsername, 
                CONCAT('Employee created: ', NEW.Username), 
                NEW.OwnerID);
    ELSE
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'Username cannot be NULL in logs for employee creation';
    END IF;
END..
delimiter ;


-- Trigger for logging after an employee update
DELIMITER //
CREATE TRIGGER after_employee_update
AFTER UPDATE ON Employee
FOR EACH ROW
BEGIN
    INSERT INTO logs (date, time, User, activity, OwnerID)
    VALUES (CURRENT_DATE, CURRENT_TIME, 
            (SELECT Username FROM Owner WHERE OwnerID = NEW.OwnerID), 
            CONCAT('Employee with Username ', NEW.Username, ' updated'), 
            NEW.OwnerID);
END; //
DELIMITER ;

-- Trigger for logging after an employee delete
DELIMITER //
CREATE TRIGGER after_employee_delete
AFTER DELETE ON Employee
FOR EACH ROW
BEGIN
    INSERT INTO logs (date, time, User, activity, OwnerID)
    VALUES (CURRENT_DATE, CURRENT_TIME, 
            (SELECT Username FROM Owner WHERE OwnerID = OLD.OwnerID), 
            CONCAT('Employee with Username ', OLD.Username, ' deleted'), 
            OLD.OwnerID);
END; //
DELIMITER ;

-- Trigger for logging after an owner insert
DELIMITER //
CREATE TRIGGER after_owner_insert
AFTER INSERT ON Owner
FOR EACH ROW
BEGIN
    INSERT INTO logs (date, time, User, activity, OwnerID)
    VALUES (CURRENT_DATE, CURRENT_TIME, 
            NEW.Username, 
            CONCAT('Owner registered: ', NEW.Username), 
            NEW.OwnerID);
END; //
DELIMITER ;

-- Trigger for logging after an owner update
DELIMITER //
CREATE TRIGGER after_owner_update
AFTER UPDATE ON Owner
FOR EACH ROW
BEGIN
    INSERT INTO logs (date, time, User, activity, OwnerID)
    VALUES (CURRENT_DATE, CURRENT_TIME, 
            NEW.Username, 
            CONCAT('Owner with ID ', NEW.Username, ' updated'), 
            NEW.OwnerID);
END; //
DELIMITER ;

-- Trigger for logging after an owner delete
DELIMITER //
CREATE TRIGGER after_owner_delete
AFTER DELETE ON Owner
FOR EACH ROW
BEGIN
    INSERT INTO logs (date, time, User, activity, OwnerID)
    VALUES (CURRENT_DATE, CURRENT_TIME, 
            OLD.Username, 
            CONCAT('Owner with ID ', OLD.Username, ' deleted'), 
            OLD.OwnerID);
END; //
DELIMITER ;

show triggers;


select * from Owner;
INSERT INTO Owner (Username, Password, Name, EmailID, PhoneNo, ShopName, ShopAddress) VALUES
('Owner','a', 'Glenn Maxwell', 'chetankc2005@gmail.com', '9876543210', 'Maxwell\'s Shop', '14B, Street 2, Melbourne - 400078'),
('a', 'Alice', 'Alice Thompson', 'chetankc2005@gmail.com', '9998453218', 'The Paper Trail', '123 Stationery Lane, Suite 4B, Kurla - 400079'),
('q', 'q', 'q', 'q', 'q', 'q', 'q'),
('a', 'aaaaaaaa', 'a', 'a', '9876543210', 'a', 'a'),
('q', 'qqqqqqqq@1Q', 'a', 'a', '9876543210', 'q', 'q q');

INSERT INTO Employee (Username, Password, Name, EmailID, PhoneNo) VALUES
('Employee','1234', 'Glenn Maxwell', 'glenn@gmail.com', '9874561230'),
('Employee1', 'VKfpPcbw', 'Ravi Ashwin', 'rashwin@gmail.com', '7896541597'),
('ishan123', 'ishan@123', 'Ishan Kishan', 'ishan@gmail.com', '9457864231'),
('dekock123', 'dekock@123', 'Quniton de kock', 'quniton@gmail.com', '9087654321'),
('abd', 'abd', 'A B De Villers', 'abd@gmail.com', '7895243610');

INSERT INTO Product (SrNo, ProductName, Category, Quantity, Price, MfgDate, ExpDate, LowQuantityAlert, OwnerID) VALUES
(1, 'Notebook', 'Paper Products', 10, 45.00, '2023-07-01', '2025-07-01', 10, '1'),
(2, 'Pen', 'Writing Instruments', 25, 15.00, '2023-07-15', '2025-07-15', 25, '1'),
(3, 'Pencil', 'Writing Instruments', 25, 10.00, '2023-07-10', '2025-07-10', 25, '1'),
(4, 'Eraser', 'Accessories', 10, 5.00, '2023-07-05', '2025-07-05', 10, '1'),
(5, 'Sharpener', 'Accessories', 10, 8.00, '2023-07-05', '2025-07-05', 10, '1'),
(6, 'Ruler', 'Measuring Tools', 5, 20.00, '2023-07-01', '2025-07-01', 5, '1'),
(7, 'Glue Stick', 'Adhesives', 40, 35.00, '2023-06-30', '2025-06-30', 5, '1'),
(8, 'Scissors', 'Cutting Tools', 25, 75.00, '2023-07-01', '2025-07-01', 5, '1'),
(9, 'Stapler', 'Office Supplies', 15, 90.00, '2023-07-01', '2025-07-01', 10, '1'),
(10, 'Staples', 'Office Supplies', 50, 25.00, '2023-07-01', '2025-07-01', 15, '1'),
(11, 'Highlighter', 'Writing Instruments', 30, 45.00, '2023-07-01', '2025-07-01', 5, '1'),
(12, 'Calculator', 'Electronics', 10, 750.00, '2023-06-15', '2025-06-15', 5, '1'),
(13, 'Sticky Notes', 'Paper Products', 100, 60.00, '2023-06-30', '2025-06-30', 10, '1'),
(14, 'Whiteboard Marker', 'Writing Instruments', 50, 40.00, '2023-07-01', '2025-07-01', 10, '1'),
(15, 'Permanent Marker', 'Writing Instruments', 50, 42.00, '2023-07-01', '2025-07-01', 10, '1'),
(16, 'Binder Clips', 'Office Supplies', 40, 22.00, '2023-07-01', '2025-07-01', 10, '1'),
(17, 'Paper Clips', 'Office Supplies', 100, 5.00, '2023-07-01', '2025-07-01', 10, '1'),
(18, 'Folders', 'Paper Products', 50, 45.00, '2023-07-01', '2025-07-01', 15, '1'),
(19, 'Tape', 'Adhesives', 30, 30.00, '2023-07-01', '2024-10-08', 5, '1');


show databases;
create database billnbox;
use billnbox;
show tables;


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



INSERT INTO Product (ProductName, Category, Quantity, Price, MfgDate, ExpDate, LowQuantityAlert, OwnerID)
VALUES
('Toor Dal 1kg', 'Pulses', 150, 100.00, '2023-01-01', '2025-01-01', 25, 1),  -- 2 years
('Chana Dal 1kg', 'Pulses', 120, 90.00, '2023-02-01', '2025-02-01', 30, 1),  -- 2 years
('Moong Dal 1kg', 'Pulses', 100, 110.00, '2023-03-01', '2024-03-01', 25, 1),  -- 1 year
('Masoor Dal 1kg', 'Pulses', 110, 95.00, '2023-04-01', '2025-04-01', 25, 1),  -- 2 years
('Brown Rice 1kg', 'Grains', 80, 80.00, '2023-05-01', '2024-05-01', 15, 1),  -- 1 year
('Basmati Rice 1kg', 'Grains', 200, 90.00, '2023-06-10', '2025-06-10', 30, 1),  -- 2 years
('Quinoa 500g', 'Grains', 60, 120.00, '2023-07-05', '2024-07-05', 25, 1),  -- 1 year
('Whole Wheat Flour 1kg', 'Grains', 150, 50.00, '2023-08-20', '2024-08-20', 30, 1),  -- 1 year
('Red Kidney Beans 500g', 'Pulses', 100, 70.00, '2023-09-01', '2025-09-01', 20, 1),  -- 2 years
('Chickpeas 500g', 'Pulses', 90, 80.00, '2023-10-01', '2024-10-01', 15, 1),  -- 1 year
('Lentils 500g', 'Pulses', 95, 75.00, '2023-11-01', '2025-11-01', 20, 1),  -- 2 years
('Pigeon Peas 500g', 'Pulses', 85, 65.00, '2023-12-01', '2024-12-01', 15, 1),  -- 1 year
('Parle-G Biscuits 500g', 'Biscuits', 120, 50.00, '2023-05-01', '2024-05-01', 20, 1),  -- 1 year
('Oreo Chocolate Cream Biscuits 300g', 'Biscuits', 85, 60.00, '2023-06-15', '2024-12-15', 10, 1),  -- 1.5 years
('Lay’s Classic Salted Chips 200g', 'Snacks', 150, 35.00, '2023-01-10', '2025-01-10', 30, 1),  -- 2 years
('Kurkure Masala Munch 150g', 'Snacks', 200, 25.00, '2023-03-01', '2024-09-01', 25, 1),  -- 6 months
('Bingo Mad Angles 100g', 'Snacks', 100, 20.00, '2023-12-01', '2024-06-01', 15, 1),  -- 6 months
('Maggi Noodles 70g', 'Instant Noodles', 300, 12.00, '2024-02-20', '2025-08-20', 50, 1),  -- 1.5 years
('Top Ramen Curry Noodles 280g', 'Instant Noodles', 250, 45.00, '2024-03-10', '2025-11-10', 40, 1),  -- 20 months
('Sunfeast Marie Light Biscuits 250g', 'Biscuits', 180, 30.00, '2023-11-01', '2024-11-01', 25, 1),  -- 1 year
('Good Day Cashew Cookies 500g', 'Biscuits', 90, 55.00, '2023-10-05', '2024-10-05', 15, 1),  -- 1 year
('Britannia Bourbon 400g', 'Biscuits', 140, 60.00, '2024-04-01', '2025-04-01', 20, 1),  -- 1 year
('Haldiram’s Classic Salted Peanuts 250g', 'Snacks', 110, 40.00, '2023-09-10', '2024-09-10', 15, 1),  -- 1 year
('Pringles Original 110g', 'Snacks', 130, 80.00, '2024-01-05', '2025-01-05', 20, 1),  -- 1 year
('Doritos Nacho Cheese Chips 150g', 'Snacks', 75, 85.00, '2024-02-01', '2025-06-01', 10, 1),  -- 16 months
('Cheetos Cheese Puffs 150g', 'Snacks', 190, 70.00, '2024-03-15', '2025-07-15', 20, 1),  -- 16 months
('Yippee Noodles Magic Masala 65g', 'Instant Noodles', 250, 10.00, '2024-01-20', '2025-04-20', 40, 1),  -- 15 months
('Aloo Bhujia 200g', 'Snacks', 150, 30.00, '2024-03-05', '2025-03-05', 15, 1),  -- 1 year
('Bisleri Water 1L', 'Beverages', 200, 15.00, '2024-01-10', '2025-01-10', 50, 1),  -- 1 year
('Tata Tea 500g', 'Beverages', 120, 100.00, '2023-12-15', '2024-12-15', 25, 1),  -- 1 year
('Bournvita 500g', 'Health Drink', 80, 250.00, '2023-11-05', '2024-11-05', 15, 1),  -- 1 year
('Nescafe Coffee 100g', 'Beverages', 150, 300.00, '2024-02-10', '2025-05-10', 20, 1),  -- 15 months
('Patanjali Atta 1kg', 'Staples', 100, 40.00, '2024-03-01', '2025-03-01', 25, 1),  -- 1 year
('Dalda Ghee 1L', 'Cooking Oil', 80, 400.00, '2023-10-15', '2024-10-15', 10, 1),  -- 1 year
('Uncle Chipps 150g', 'Snacks', 90, 30.00, '2024-02-15', '2025-05-15', 15, 1),  -- 15 months
('Pillsbury Atta 5kg', 'Staples', 50, 250.00, '2024-02-20', '2025-02-20', 30, 1),  -- 1 year
('Almonds 200g', 'Dry Fruits', 90, 400.00, '2024-03-01', '2025-06-01', 15, 1),  -- 15 months
('Cashews 200g', 'Dry Fruits', 80, 450.00, '2023-12-10', '2025-04-10', 20, 1),  -- 1.5 years
('Pistachios 100g', 'Dry Fruits', 50, 600.00, '2023-10-20', '2025-10-20', 25, 1),  -- 2 years
('Raisins 200g', 'Dry Fruits', 75, 250.00, '2024-01-05', '2025-01-05', 15, 1),  -- 1 year
('Dates 250g', 'Dry Fruits', 120, 300.00, '2023-09-15', '2024-09-15', 20, 1),  -- 1 year
('Walnuts 100g', 'Dry Fruits', 60, 500.00, '2023-11-01', '2024-11-01', 20, 1);  -- 1 year


select * from Employee;

select * from Owner;
truncate table Owner;
alter table Owner add column FilePath varchar(225);
update Owner set FilePath='C:\Users\aarya\Desktop\Mini Project Sem3\Bill-n-Box\billnbox\Generated PDFs' where OwnerID=1;
update Owner set EmailID='aarya.khatate29@gmail.com' where OwnerID=1;
select FilePath from Owner where OwnerID=1;
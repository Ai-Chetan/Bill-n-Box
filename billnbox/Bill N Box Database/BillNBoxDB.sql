create database BillNBoxDB;
use BillNBoxDB;

CREATE TABLE Owner (OwnerID INT PRIMARY KEY, Username VARCHAR(20) NOT NULL, Password VARCHAR(20) NOT NULL, Name VARCHAR(20), EmailID VARCHAR(20), PhoneNo VARCHAR(10),ShopName VARCHAR(20), ShopAddress VARCHAR(100));
insert into Owner values(1001, "Owner", "1234", "Glenn Maxwell", "glenn@gmail.com", "9874561230", "Maxwell's Shop", "14B, Street 3, Melbourne");
delete from Owner where OwnerID = 1010;
select * from Owner;

CREATE TABLE Employee (EmpID INT PRIMARY KEY, Username VARCHAR(20) NOT NULL, Password VARCHAR(20) NOT NULL, Name VARCHAR(20), EmailID VARCHAR(20), PhoneNo VARCHAR(10));
insert into Employee values(1000, "Employee", "1234", "N Pooran", "nicolas@gmail.com", "8794563245");
delete from Employee where EmpID = 1001;
select * from Employee;

CREATE TABLE Product (SrNo INT AUTO_INCREMENT PRIMARY KEY, ProductName VARCHAR(100) NOT NULL, Category VARCHAR(50) NOT NULL, Quantity INT NOT NULL DEFAULT 0, Price DECIMAL(10, 2) NOT NULL, MfgDate DATE, ExpDate DATE, LowQuantityAlert INT DEFAULT 1);
desc Product;
INSERT INTO Product (ProductName, Category, Quantity, Price, MfgDate, ExpDate, LowQuantityAlert) VALUES
('Notebook', 'Paper Products', 100, 45.00, '2023-07-01', '2025-07-01', 10),('Pen', 'Writing Instruments', 200, 15.00, '2023-07-15', '2025-07-15', 25),('Pencil', 'Writing Instruments', 150, 10.00, '2023-07-10', '2025-07-10', 25),
('Eraser', 'Accessories', 100, 5.00, '2023-07-05', '2025-07-05', 10),('Sharpener', 'Accessories', 80, 8.00, '2023-07-05', '2025-07-05', 10),('Ruler', 'Measuring Tools', 60, 20.00, '2023-07-01', '2025-07-01', 5),
('Glue Stick', 'Adhesives', 40, 35.00, '2023-06-30', '2025-06-30', 5),('Scissors', 'Cutting Tools', 25, 75.00, '2023-07-01', '2025-07-01', 5),('Stapler', 'Office Supplies', 15, 90.00, '2023-07-01', '2025-07-01', 10),
('Staples', 'Office Supplies', 50, 25.00, '2023-07-01', '2025-07-01', 15),('Highlighter', 'Writing Instruments', 30, 45.00, '2023-07-01', '2025-07-01', 5),('Calculator', 'Electronics', 10, 750.00, '2023-06-15', '2025-06-15', 5),
('Sticky Notes', 'Paper Products', 100, 60.00, '2023-06-30', '2025-06-30', 10),('Whiteboard Marker', 'Writing Instruments', 50, 40.00, '2023-07-01', '2025-07-01', 10),('Permanent Marker', 'Writing Instruments', 50, 42.00, '2023-07-01', '2025-07-01', 10),
('Binder Clips', 'Office Supplies', 40, 22.00, '2023-07-01', '2025-07-01', 10),('Paper Clips', 'Office Supplies', 100, 5.00, '2023-07-01', '2025-07-01', 10),('Folders', 'Paper Products', 50, 45.00, '2023-07-01', '2025-07-01', 15),
('Tape', 'Adhesives', 30, 30.00, '2023-07-01', '2025-07-01', 5),('File', 'Paper Products', 20, 60.00, '2023-07-01', '2025-07-01', 15);
select * from Product;

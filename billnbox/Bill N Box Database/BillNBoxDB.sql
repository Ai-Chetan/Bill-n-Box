create database BillNBoxDB;
use BillNBoxDB;
show tables;
CREATE TABLE Owner (
    OwnerID INT PRIMARY KEY, 
    Username VARCHAR(20) NOT NULL,
    Password VARCHAR(20) NOT NULL,
    Name VARCHAR(20),
    EmailID VARCHAR(20),
    PhoneNo VARCHAR(10),
    ShopName VARCHAR(20),
    ShopAddress VARCHAR(100)
);
desc Owner;
insert into Owner values(1001, "Owner", "1234", "Glenn Maxwell", "glenn@gmail.com", "9874561230", "Maxwell's Shop", "14B, Street 3, Melbourne");
select * from Owner;
ALTER TABLE Owner MODIFY COLUMN OwnerID INT AUTO_INCREMENT;
delete from Owner where OwnerID = 1010;

CREATE TABLE Employee (
    EmpID INT PRIMARY KEY, 
    Username VARCHAR(20) NOT NULL,
    Password VARCHAR(20) NOT NULL,
    Name VARCHAR(20),
    EmailID VARCHAR(20),
    PhoneNo VARCHAR(10)
);
ALTER TABLE Employee MODIFY COLUMN EmpID INT AUTO_INCREMENT;
insert into Employee values(1000, "Employee", "1234", "N Pooran", "nicolas@gmail.com", "8794563245");
select * from Employee;

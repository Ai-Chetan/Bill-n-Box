# Bill-n-Box – Billing and Inventory Management System

Bill-n-Box is a **JavaFX + MySQL** based desktop application for small to medium-sized businesses to manage **billing, inventory, employees, and notifications** in a single platform.  
It features **role-based access control**, **real-time low stock and expiration alerts**, **activity logs**, and **PDF bill generation** to streamline shop operations.

---

## ✨ Features

- **Role-Based Access**
  - **Owner**: Manage inventory, generate bills, manage employees, view logs, and receive alerts.
  - **Employee**: Generate bills and view relevant data only.

- **Inventory Management**
  - Add, edit, delete products
  - Low stock and expiry date alerts
  - Real-time updates

- **Billing**
  - Create and print PDF bills
  - Auto-fetch prices from inventory
  - Customer name and date fields
  - Suggestive product search

- **Employee Management**
  - Add, edit, and delete employees
  - Set login credentials with validation

- **Notifications**
  - Low quantity alerts
  - Expiry and near-expiry product warnings

- **Activity Logs**
  - Track all major actions (product changes, billing, logins, etc.)

- **Security**
  - Username availability check
  - Strong password rules
  - Role-based restrictions

---

## 🛠 Technology Stack

- **Language**: Java (JavaFX)
- **Database**: MySQL (hosted on Aiven Cloud)
- **Architecture**: MVC (Model–View–Controller)
- **UI Design**: JavaFX Scene Builder
- **Build Tool**: Maven

---

## ⚙️ Installation & Setup

### Prerequisites
- Java 17+
- MySQL Server
- Maven

### Steps
```bash
# Clone the repository
git clone https://github.com/Ai-Chetan/Bill-n-Box.git
cd Bill-n-Box

# Build the project
./mvnw clean install

# Run the application
./mvnw javafx:run
```

## 📂 Database Setup

1. Create a MySQL database.  
2. Import the provided SQL schema.  
3. Update DB credentials in the configuration file (if required).  

---

## 📊 ER Diagram

The system models:  
- **Owner**  
- **Product**  
- **Employee**  
- **Order**  
- **Bill**  
- **Logs**  

With appropriate relationships and triggers for automatic logging of inserts, updates, and deletes.

---

## 🚀 Future Enhancements

- More granular user role permissions  
- Advanced analytics & reporting  
- Mobile app integration  
- Payment gateway support  
- GST and Tax Inclusion  

---

## 👥 Contributors

- [**Aarya Khatate**](https://github.com/aaryaKhatate)
- [**Chetan Chaudhari**](https://github.com/Ai-Chetan)
- [**Vinay Gone**](https://github.com/VinayGone2006)
- [**Yashraj Patil**](https://github.com/Yashrajpatil22)

---

## 📜 License

This project is licensed under the MIT License – see the [LICENSE](LICENSE) file for details.

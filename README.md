# # 💼 Employee Salary Management System

A desktop-based **Employee Salary Management System** built with **Java**, designed to manage employee records, salary designations, allowances, deductions, and generate printable pay slips — all without any external libraries or database setup.

---

## 🖥️ Tech Stack

| Technology | Details |
|------------|---------|
| Language   | Java (JDK 8+) |
| UI Framework | Java |
| Storage | CSV Files (No database needed) |
| IDE Support | VS Code / Eclipse / IntelliJ |

---

## ✨ Features

- 🔐 **Secure Login System** — Username & password authentication
- 👤 **Employee Management** — Add, Edit, and Delete employee records
- 💰 **Salary Configuration** — Configure designations with Basic Pay
- 📈 **Allowances** — DA, HRA, WA (Fixed or % of Basic Pay)
- 📉 **Deductions** — GPF, Income Tax, GIS, PF, LIC (Fixed or % of Basic Pay)
- 🧾 **Pay Slip Generation** — Generate monthly pay slips instantly
- 🖨️ **Print Support** — Preview and print pay slips directly
- 💾 **Auto Data Saving** — All data saved automatically in CSV files
- 🪟 **MDI Interface** — Multiple windows open simultaneously

---

## 📁 Project Structure

```
Employee-Salary-Management-System/
│
├── EmployeeSalarySystem.java     # Main source file (all classes in one file)
├── employees.csv                 # Auto-generated — stores employee records
├── settings.csv                  # Auto-generated — stores salary designations
├── users.csv                     # Auto-generated — stores login credentials
├── images/                       # Icons and images for the UI
└── README.md                     # Project documentation
```

---

## 🚀 How to Run

### Using VS Code / Terminal

**Step 1 — Clone the repository**
```bash
git clone https://github.com/YOUR_USERNAME/Employee-Salary-Management-System.git
```

**Step 2 — Navigate to the project folder**
```bash
cd Employee-Salary-Management-System
```

**Step 3 — Compile the code**
```bash
javac EmployeeSalarySystem.java
```

**Step 4 — Run the application**
```bash
java EmployeeSalarySystem
```

---

## 🔑 Default Login Credentials

> ⚠️ These are example credentials for first-time login only.

| Username | Password |
|----------|----------|
| admin *(example)* | admin *(example)* |

---

## 📸 Screenshots

### 🔐 Login Screen
> Secure login with username and password authentication.

![Login Screen](screenshots/login.png)

---

### 🖥️ Main Dashboard
> MDI-based dashboard with toolbar, menu bar, and quick access buttons.

![Main Dashboard](screenshots/dashboard.png)

---

### 👤 Add Employee
> Form to add a new employee with code, designation, name, address, and contact.

![Add Employee](screenshots/add_employee.png)

---

### 🧾 Pay Slip
> Monthly pay slip with basic pay, allowances, deductions, and net salary.

![Pay Slip](screenshots/payslip.png)

---

## 📊 Salary Calculation Logic

```
Gross Salary     = Basic Pay + DA + HRA + WA
Total Deductions = GPF + Income Tax + GIS + PF + LIC
Net Salary       = Gross Salary - Total Deductions
```

Each allowance and deduction can be configured as either:
- ✅ **Percentage (%)** of Basic Pay
- 💵 **Fixed Amount (₹)**

---

## 💾 Data Storage

All data is stored locally in CSV files — no database installation required:

| File | Description |
|------|-------------|
| `employees.csv` | Stores all employee records |
| `settings.csv` | Stores salary designation configurations |
| `users.csv` | Stores login credentials |

---

## 👨‍💻 Developer

Student — Centurion University of Technology and Management, Odisha
---

## ⭐ Support

If you found this project helpful, please consider giving it a **⭐ star** on GitHub — it means a lot!

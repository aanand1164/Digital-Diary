## 📝 Digital-Diary
A Java Swing-based Digital Diary application that allows users to create, edit, delete, and search diary entries. All data is securely stored using a MySQL database. Designed to be simple, elegant, and efficient.


## 🚀 Features
- Add new diary entries with titles and dates
- Edit and delete existing entries
- Search entries by title or date
- User-friendly GUI built with Java Swing
- MySQL integration for persistent storage


## 🛠️ Tech Stack
- **Java Swing** – for building the desktop GUI
- **MySQL** – for backend data storage
- **JDBC** – for database connectivity
- **Git** – for version control


## 💻 How to Run

1. Clone this repository:
   ```bash
   git clone https://github.com/aanand1164/digital-diary.git
   
2. Import the project into your IDE (like VS Code or IntelliJ).

3. Make sure MySQL is installed and running.

4. Create the database and table using the provided SQL script.

5. Update your database credentials in the **config.properties** file

6. Run **DigitalDiary.java** to start the application.


## 📁 Folder Structure
digital-diary/
├── lib/
│   ├── jcalendar-1.4.jar
│   ├── junit-platform-console-standalone-1.13.0-M2.jar
│   └── mysql-connector-j-9.2.0
├── DigitalDiary.java
├── DigitalDiaryUnitTests.java
├── DigitalDiaryIntegrationTests.java
├── README.md
├── .gitignore
└── config.properties

## 📄 License
This project is licensed under the MIT License.

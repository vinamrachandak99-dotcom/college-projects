package com.jobrec.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

// SINGLETON PATTERN
// Only one instance of DBConnection ever exists in the whole app.
// Every part of the system that needs the database asks this class
// for the connection instead of creating a new one each time.
public class DBConnection {

    private static DBConnection instance;   // the one and only instance
    private Connection connection;

    // private constructor -> nobody outside this class can do "new DBConnection()"
    private DBConnection() {
        try {
            Class.forName("org.sqlite.JDBC");
            connection = DriverManager.getConnection("jdbc:sqlite:jobrecruitment.db");
            System.out.println("[DBConnection] Connected to jobrecruitment.db");
            setupTables();
        } catch (Exception e) {
            throw new RuntimeException("Failed to connect to database", e);
        }
    }

    // the only way to get the instance
    public static DBConnection getInstance() {
        if (instance == null) {
            instance = new DBConnection();
        }
        return instance;
    }

    public Connection getConnection() {
        return connection;
    }

    private void setupTables() throws SQLException {
        try (Statement st = connection.createStatement()) {
            st.execute("CREATE TABLE IF NOT EXISTS candidates (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "name TEXT, age INTEGER, education TEXT, skills TEXT," +
                    "experience TEXT, location TEXT, expected_salary REAL)");

            st.execute("CREATE TABLE IF NOT EXISTS jobs (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "title TEXT, required_skills TEXT, location TEXT," +
                    "offered_salary REAL, category TEXT)");

            st.execute("CREATE TABLE IF NOT EXISTS applications (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "candidate_id INTEGER, job_id INTEGER," +
                    "stage TEXT, status TEXT)");
        }
    }
}

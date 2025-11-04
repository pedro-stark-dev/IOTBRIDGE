package com.stark.duckmodbus.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Logger;

public final class Database {
    private static final String URL = "jdbc:sqlite:historic.db";
    private static final Logger logger = Logger.getLogger(Database.class.getName());

    // Mantém uma única conexão reutilizável
    private static Connection connection;

    // 🔹 Abre a conexão (somente uma vez)
    public static synchronized Connection getConnection() {
        try {
            if (connection == null || connection.isClosed()) {
                connection = DriverManager.getConnection(URL);
                try (Statement s = connection.createStatement()) {
                    s.execute("PRAGMA foreign_keys=ON");
                    s.execute("PRAGMA journal_mode=WAL");
                }
                logger.info("Database connection established.");
            }
            return connection;
        } catch (SQLException e) {
            logger.severe("Failed to connect to database: " + e.getMessage());
            throw new RuntimeException("Database connection failed", e);
        }
    }

    // 🔹 Fecha a conexão de forma segura
    public static synchronized void closeConnection() {
        if (connection != null) {
            try {
                connection.close();
                connection = null;
                logger.info("Database connection closed.");
            } catch (SQLException e) {
                logger.warning("Error closing database connection: " + e.getMessage());
            }
        }
    }

    // 🔹 Cria a tabela caso não exista
    public static void createTable() {
        String sql = """
            CREATE TABLE IF NOT EXISTS historic (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                request TEXT,
                response TEXT,
                date TEXT,
                data INTEGER
            );
        """;

        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
            logger.info("Table 'historic' verified/created successfully.");
        } catch (SQLException e) {
            logger.severe("Failed to create table 'historic': " + e.getMessage());
            throw new RuntimeException("Create table failed", e);
        }
    }
}

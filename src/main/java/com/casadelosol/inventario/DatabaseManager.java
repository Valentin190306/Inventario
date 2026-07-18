package com.casadelosol.inventario;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.util.stream.Collectors;

public class DatabaseManager {

    private static final String DB_NAME = "inventario.db";
    private static final String SCHEMA_RESOURCE = "/db/schema.sql";

    private static DatabaseManager instance;

    private String dbUrl;
    private boolean schemaApplied;

    private DatabaseManager() {
    }

    public static synchronized DatabaseManager getInstance() {
        if (instance == null) {
            instance = new DatabaseManager();
        }
        return instance;
    }

    public void initialize() {
        try {
            Path dbPath = getDatabasePath();
            Path parentDir = dbPath.getParent();
            if (parentDir != null && !Files.exists(parentDir)) {
                Files.createDirectories(parentDir);
            }

            dbUrl = "jdbc:sqlite:" + dbPath.toAbsolutePath();

            if (!schemaApplied) {
                try (Connection conn = openConnection();
                     Statement stmt = conn.createStatement()) {
                    stmt.execute("PRAGMA foreign_keys = ON");
                }
                executeSchema();
                schemaApplied = true;
            }
        } catch (Exception e) {
            throw new RuntimeException("Error al inicializar la base de datos", e);
        }
    }

    public Connection getConnection() {
        if (dbUrl == null) {
            throw new IllegalStateException("Database not initialized. Call initialize() first.");
        }
        return openConnection();
    }

    private Connection openConnection() {
        try {
            Connection conn = DriverManager.getConnection(dbUrl);
            try (Statement stmt = conn.createStatement()) {
                stmt.execute("PRAGMA foreign_keys = ON");
            }
            return conn;
        } catch (Exception e) {
            throw new RuntimeException("Error al abrir conexión a la base de datos", e);
        }
    }

    private void executeSchema() {
        try (var inputStream = getClass().getResourceAsStream(SCHEMA_RESOURCE)) {
            if (inputStream == null) {
                throw new RuntimeException("Schema resource not found: " + SCHEMA_RESOURCE);
            }
            String sql;
            try (var reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
                sql = reader.lines().collect(Collectors.joining("\n"));
            }
            String[] statements = sql.split(";");
            try (Connection conn = openConnection();
                 Statement stmt = conn.createStatement()) {
                for (String statement : statements) {
                    String trimmed = statement.trim();
                    if (!trimmed.isEmpty()) {
                        stmt.execute(trimmed);
                    }
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Error al ejecutar el schema", e);
        }
    }

    private Path getDatabasePath() {
        String os = System.getProperty("os.name").toLowerCase();
        if (os.contains("win")) {
            String appData = System.getenv("APPDATA");
            if (appData != null) {
                return Paths.get(appData, "CasaDelSol", "Inventario", DB_NAME);
            }
        }
        return Paths.get(System.getProperty("user.dir"), DB_NAME);
    }
}

package DBConnection;

import Utils.ConfigManager;
import java.sql.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class DBContext {

    private static final String DB_URL = ConfigManager.get("DB_URL");
    private static final String DB_USER = ConfigManager.get("DB_USER");
    private static final String DB_PASSWORD = ConfigManager.get("DB_PASSWORD");

    private Connection connection;

    public Connection getConnection() {
        return connection;
    }

    public DBContext() {
        try {
            Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
            connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
        } catch (ClassNotFoundException | SQLException ex) {
            Logger.getLogger(DBContext.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}

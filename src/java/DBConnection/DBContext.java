package DBConnection;

import Utils.ConfigManager;
import java.sql.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class DBContext {

    private Connection connection;
    
    public Connection getConnection() {
        return connection;
    }

    public DBContext() {
        try {
            String url = ConfigManager.get("DB_URL",
                    "jdbc:sqlserver://localhost:1433;databaseName=DLEM_DB_2;trustServerCertificate=true;sendStringParametersAsUnicode=true");
            String user = ConfigManager.get("DB_USER", "sa");
<<<<<<< Updated upstream
            String pass = ConfigManager.get("DB_PASSWORD", "123");
=======
            String pass = ConfigManager.get("DB_PASSWORD", "1"); // Đảm bảo pass SQL Server của bạn là 123
>>>>>>> Stashed changes
            Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
            connection = DriverManager.getConnection(url, user, pass);
        } catch (ClassNotFoundException | SQLException ex) {
            Logger.getLogger(DBContext.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}

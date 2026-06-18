package DBConnection;

import java.sql.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class DBContext {

    private static final String DB_URL = "jdbc:sqlserver://localhost:1433;databaseName=DLEM_DB_2;trustServerCertificate=true;sendStringParametersAsUnicode=true";
    private static final String DB_USER = "sa";
    private static final String DB_PASSWORD = "123";

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

    // === THÊM HÀM MAIN NÀY ĐỂ CHECK KẾT NỐI ===
    public static void main(String[] args) {
        DBContext db = new DBContext();
        if (db.getConnection() != null) {
            System.out.println("🎉 KẾT NỐI DATABASE THÀNH CÔNG RỒI BẠN ƠI! 🎉");
        } else {
            System.err.println("❌ KẾT NỐI THẤT BẠI! Vui lòng kiểm tra lại SQL Server.");
        }
    }
}
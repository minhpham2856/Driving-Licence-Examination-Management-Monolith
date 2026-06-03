import java.sql.*;
public class ReproTest {
    public static void main(String[] args) {
        try {
            Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
            String url = "jdbc:sqlserver://localhost\\SQLEXPRESS:1433;databaseName=DLEM_DB";
            try (Connection conn = DriverManager.getConnection(url, "sa", "123")) {
                String sql = "insert into Person (govIdNo, fullName, dateOfBirth, gender, phoneNo, email, address, photoUrl, isWalkIn, approvalStatus, rejectionReason) values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
                try (PreparedStatement ps = conn.prepareStatement(sql, new String[]{"id"})) {
                    ps.setString(1, "012345678920");
                    ps.setString(2, "PHAM NHAT MINH");
                    ps.setDate(3, Date.valueOf("2000-03-12"));
                    ps.setBoolean(4, false);
                    ps.setString(5, "0123456789");
                    ps.setString(6, "pnmtp7189@gmail.com");
                    ps.setString(7, "x\u00e3 Ho\u00e0 L\u1ea1c");
                    ps.setNull(8, Types.NVARCHAR);
                    ps.setBoolean(9, false);
                    ps.setString(10, "Pending");
                    ps.setNull(11, Types.NVARCHAR);
                    int rows = ps.executeUpdate();
                    System.out.println("OK affectedRows=" + rows);
                    try (ResultSet keys = ps.getGeneratedKeys()) {
                        if (keys.next()) System.out.println("id=" + keys.getInt(1));
                    }
                }
            }
        } catch (Exception e) {
            System.out.println("FAIL: " + e.getClass().getSimpleName() + ": " + e.getMessage());
            e.printStackTrace();
        }
    }
}

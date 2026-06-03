import java.sql.*;
public class LookupTest {
    public static void main(String[] args) throws Exception {
        Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
        String url = "jdbc:sqlserver://localhost\\SQLEXPRESS:1433;databaseName=DLEM_DB";
        try (Connection conn = DriverManager.getConnection(url, "sa", "123")) {
            String sql = "select * from Person where govIdNo = ?";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, "001203012345");
                try (ResultSet rs = ps.executeQuery()) {
                    System.out.println("found=" + rs.next());
                }
            }
            try {
                PreparedStatement ps = conn.prepareStatement("insert into Person (govIdNo, fullName, dateOfBirth, gender, phoneNo, email, address, photoUrl, isWalkIn, approvalStatus, rejectionReason) values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)", new String[]{"id"});
                ps.setString(1, "001203012345");
                ps.setString(2, "Dup");
                ps.setDate(3, Date.valueOf("2000-01-01"));
                ps.setBoolean(4, false);
                ps.setString(5, "0111111111");
                ps.setString(6, "dup2@test.com");
                ps.setString(7, "addr");
                ps.setNull(8, Types.NVARCHAR);
                ps.setBoolean(9, false);
                ps.setString(10, "Pending");
                ps.setNull(11, Types.NVARCHAR);
                int rows = ps.executeUpdate();
                System.out.println("dup insert rows=" + rows);
            } catch (SQLException e) {
                System.out.println("dup insert error=" + e.getMessage());
            }
        }
    }
}

import java.sql.*;
public class PersonInsertTest {
    public static void main(String[] args) throws Exception {
        Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
        String url = "jdbc:sqlserver://localhost\\SQLEXPRESS:1433;databaseName=DLEM_DB";
        try (Connection conn = DriverManager.getConnection(url, "sa", "123")) {
            String sql = "insert into Person (govIdNo, fullName, dateOfBirth, gender, phoneNo, email, address, photoUrl, isWalkIn, approvalStatus, rejectionReason) values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
            try (PreparedStatement ps = conn.prepareStatement(sql, new String[]{"id"})) {
                ps.setString(1, "888888888888");
                ps.setString(2, "Nguyen Van Binh");
                ps.setDate(3, Date.valueOf("2000-03-15"));
                ps.setBoolean(4, false);
                ps.setString(5, "0888777666");
                ps.setString(6, "newtest@email.com");
                ps.setString(7, "123 Test");
                ps.setNull(8, Types.NVARCHAR);
                ps.setBoolean(9, false);
                ps.setString(10, "Pending");
                ps.setNull(11, Types.NVARCHAR);
                int rows = ps.executeUpdate();
                System.out.println("affectedRows=" + rows);
                try (ResultSet keys = ps.getGeneratedKeys()) {
                    if (keys.next()) System.out.println("generated id=" + keys.getInt(1));
                    else System.out.println("NO generated key");
                }
            }
        }
    }
}

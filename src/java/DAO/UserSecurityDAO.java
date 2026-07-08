package DAO;

public interface UserSecurityDAO {
    boolean mustChangePassword(int userId);
    boolean setMustChange(int userId, boolean value);
    String getPasswordHash(int userId);
    boolean updatePassword(int userId, String newPasswordPlain);
}

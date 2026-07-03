package dao;

import model.user.User;

public interface UserDAO {

    User getById(int id);

    User getByUsername(String username);

    User getByIdentifier(String identifier);

    User getByEmail(String email);

    boolean insert(User user);

    boolean updatePassword(int userId, String passwordHash);

    /** Vô hiệu hoá tài khoản (Status = 0). */
    boolean deactivate(int userId);
}

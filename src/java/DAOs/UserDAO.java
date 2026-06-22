package DAOs;

import Models.User;

public interface UserDAO {

    User getById(int id);

    User getByUsername(String username);

    User getByIdentifier(String identifier);

    User getByEmail(String email);

    boolean insert(User user);

    boolean updatePassword(int userId, String passwordHash);
}

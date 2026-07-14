package DAO;

import Models.User;

public interface UserDAO {

    User getById(int id);

    User getByUsername(String username);

    User getByIdentifier(String identifier);

    boolean insert(User user);

    boolean updatePassword(int userId, String passwordHash);
}

package examiner.dao;

import java.util.*;
import shared.model.User;

// DAO contract for User persistence; examiner module SQL boundary.
public interface UserDAO {

    // Loads one user row by primary key.
    User get(int id);

    // Loads one user row by username.
    User getByUsername(String username);

    // Loads one user by username, email, or linked profile government id.
    User getByIdentifier(String identifier);

    // Loads one user row by email address.
    User getByEmail(String email);

    // Inserts a new user row.
    boolean add(User user);

    // Updates password hash for one user.
    boolean updatePassword(int userId, String passwordHash);

    // Batch-loads user rows for a list of user ids.
    List<User> getAllByIds(List<Integer> ids);

    // Lists active users with the examiner role.
    List<User> getAllActiveExaminers();

    // Returns total count of user rows.
    int countAll();

    // Searches users for admin screens with optional role and status filters.
    List<User> getFilteredForAdmin(String keyword, String roleFilter, String statusFilter);
}

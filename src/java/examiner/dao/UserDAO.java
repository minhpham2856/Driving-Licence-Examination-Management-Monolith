package examiner.dao;

import java.util.*;
import shared.model.User;

public interface UserDAO {
    
    User getById(int id);
    
    User getByUsername(String username);
    
    User getByIdentifier(String identifier);
    
    User getByEmail(String email);
    
    boolean insert(User user);
    
    boolean updatePassword(int userId, String passwordHash);

    List<User> getAllByIds(List<Integer> ids);

    List<User> findActiveExaminers();

    int countAll();

    List<User> searchForAdmin(String keyword, String roleFilter, String statusFilter);
}


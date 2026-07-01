package DAO;

import Models.AccountView;
import java.util.List;

/** Admin account management. Distinct name so it won't clash with the team's UserDAO. */
public interface AccountManageDAO {
    List<AccountView> search(String keyword, String dbRole, Boolean active);
    AccountView findById(int userId);
    int create(AccountView acc, String passwordPlain, Integer actorId);
    boolean update(AccountView acc, String newPasswordOrNull, Integer actorId);
    boolean setStatus(int userId, boolean active, Integer actorId);
    boolean delete(int userId);
    boolean usernameExists(String username, int excludeUserId);
    boolean emailExists(String email, int excludeUserId);
    boolean govIdExists(String govId, int excludeUserId);
    int countAll();
    int countByRole(String dbRole);
}

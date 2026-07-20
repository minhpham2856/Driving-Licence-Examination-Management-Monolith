package admin.dao;

import admin.model.AccountView;
import admin.model.RoleOption;
import java.util.List;

public interface AccountManageDAO {
    List<AccountView> search(String keyword, Integer roleId, Boolean active);
    AccountView findById(int userId);
    List<RoleOption> listRoles();
    int create(AccountView acc, int roleId, boolean sexMale, String passwordPlain);
    boolean resetPassword(int userId, String newPasswordPlain);
    boolean setStatus(int userId, boolean active);
    boolean delete(int userId);
    boolean usernameExists(String username);
    boolean emailExists(String email);
    boolean phoneExists(String phone);
    boolean govIdExists(String govId);
    int countAll();
}

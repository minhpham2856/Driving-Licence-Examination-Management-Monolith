package examstaff.service;
import examstaff.model.Role;
public interface RoleService {
    Role getRoleById(int id);
    String getRoleNameById(int id);
    int getRoleIdByName(String roleName);
}

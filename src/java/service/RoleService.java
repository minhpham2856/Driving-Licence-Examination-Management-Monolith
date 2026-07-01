package service;
import model.Role;
public interface RoleService {
    Role getRoleById(int id);
    String getRoleNameById(int id);
    int getRoleIdByName(String roleName);
}

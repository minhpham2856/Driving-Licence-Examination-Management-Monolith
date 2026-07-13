package auth.dao;
import auth.model.Role;
public interface RoleDAO {
    Role getById(int id);
    Role getByName(String roleName);
}


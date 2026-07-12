package examiner.dao;
import shared.model.Role;
public interface RoleDAO {
    Role getById(int id);
    Role getByName(String roleName);
}


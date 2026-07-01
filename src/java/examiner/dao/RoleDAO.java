package examiner.dao;
import examiner.model.Role;
public interface RoleDAO {
    Role getById(int id);
    Role getByName(String roleName);
}

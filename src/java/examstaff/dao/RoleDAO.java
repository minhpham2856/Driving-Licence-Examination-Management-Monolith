package examstaff.dao;

import examstaff.model.Role;

public interface RoleDAO {
    Role getByName(String roleName);
}

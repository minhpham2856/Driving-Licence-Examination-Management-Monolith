package examstaff.dao;

import shared.model.Role;

public interface RoleDAO {
    Role getByName(String roleName);
}

package dao.impl;

import dao.RoleDAO;
import enums.UserRole;
import model.user.Role;

public class RoleDAOImpl implements RoleDAO {

    @Override
    public Role getById(int id) {
        for (UserRole role : UserRole.values()) {
            if (role.getId() == id) {
                return new Role(id, role.getRoleName());
            }
        }
        return null;
    }

    @Override
    public Role getByName(String roleName) {
        int id = UserRole.roleIdFromName(roleName);
        if (id == 0) {
            return null;
        }
        return new Role(id, roleName);
    }
}

package DAO.Impl;

import Constants.Db2Mappings;
import DAO.RoleDAO;
import Models.Role;

public class RoleDAOImpl implements RoleDAO {

    @Override
    public Role getById(int id) {
        for (var entry : Db2Mappings.ROLE_NAME_TO_ID.entrySet()) {
            if (entry.getValue() == id) {
                return new Role(id, entry.getKey());
            }
        }
        return null;
    }

    @Override
    public Role getByName(String roleName) {
        int id = Db2Mappings.roleIdFromName(roleName);
        if (id == 0) {
            return null;
        }
        return new Role(id, roleName);
    }
}

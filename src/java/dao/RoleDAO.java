package DAO;

import Models.Role;

public interface RoleDAO {

    Role getById(int id);

    Role getByName(String roleName);
}

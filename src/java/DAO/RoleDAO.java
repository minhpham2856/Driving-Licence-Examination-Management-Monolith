package dao;

import model.user.Role;

public interface RoleDAO {

    Role getById(int id);

    Role getByName(String roleName);
}

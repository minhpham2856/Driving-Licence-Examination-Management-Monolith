package examstaff.service;

import shared.model.Role;

public interface RoleService {

    Role getById(int id);

    String getRoleNameById(int id);

    int getRoleIdByName(String roleName);
}


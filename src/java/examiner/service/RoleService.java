package examiner.service;

import examiner.model.Role;

public interface RoleService {

    Role getById(int id);

    String getRoleNameById(int id);

    int getRoleIdByName(String roleName);
}

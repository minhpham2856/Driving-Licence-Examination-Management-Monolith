package dao.impl;




import dao.RoleDAO;

import model.user.Role;

/**
 * In-memory implementation of RoleDAO backed by ExamConstants.ROLE_NAME_TO_ID.
 * No database calls are made; roles are resolved from the constant map.
 */
public class RoleDAOImpl implements RoleDAO {

    /**
     * Looks up a role by its integer ID from the constant map.
     *
     * @param id the role ID
     * @return the Role model, or null if not found
     */
    @Override
    public Role getById(int id) {
        for (var entry : java.util.Arrays.stream(enums.UserRole.values()).collect(java.util.stream.Collectors.toMap(enums.UserRole::getRoleName, enums.UserRole::getId)).entrySet()) {
            if (entry.getValue() == id) {
                return new Role(id, entry.getKey());
            }
        }
        return null;
    }

    /**
     * Looks up a role by its display name via ExamConstants.
     *
     * @param roleName the role name (e.g. "Admin", "Examiner")
     * @return the Role model, or null if the name is unknown
     */
    @Override
    public Role getByName(String roleName) {
        int id = enums.UserRole.roleIdFromName(roleName);
        if (id == 0) {
            return null;
        }
        return new Role(id, roleName);
    }
}



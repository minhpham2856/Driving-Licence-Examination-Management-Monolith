package examiner.service.impl;

import examiner.dao.RoleDAO;
import examiner.dao.impl.RoleDAOImpl;
import shared.model.Role;
import examiner.service.RoleService;

// Resolves roles through the examiner RoleDAO copy.
public class RoleServiceImpl implements RoleService {

    private final RoleDAO roleDAO = new RoleDAOImpl();

    @Override
    public Role get(int id) {
        return roleDAO.get(id);
    }

    @Override
    public Role getByName(String name) {
        if (name == null || name.isBlank()) {
            return null;
        }
        return roleDAO.getByName(name);
    }
}

package examiner.service.impl;
import examiner.dto.*;
import shared.model.*;
import examiner.dao.RoleDAO;
import examiner.dao.impl.RoleDAOImpl;
import shared.model.Role;
import examiner.service.RoleService;
public class RoleServiceImpl implements RoleService {
    private final RoleDAO roleDAO;
    public RoleServiceImpl() {
        this.roleDAO = new RoleDAOImpl();
    }
    @Override
    public Role getById(int id) {
        return roleDAO.getById(id);
    }
    @Override
    public String getRoleNameById(int id) {
        Role role = roleDAO.getById(id);
        return role != null ? role.getRoleName() : null;
    }
    @Override
    public int getRoleIdByName(String roleName) {
        if (roleName == null || roleName.isBlank()) {
            return 0;
        }
        Role role = roleDAO.getByName(roleName);
        return role != null ? role.getRoleId() : 0;
    }
}


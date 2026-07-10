package service.impl;
import dto.*;
import model.*;
import dao.RoleDAO;
import dao.impl.RoleDAOImpl;
import model.Role;
import service.RoleService;
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

package examstaff.dao;

import shared.model.Role;

/**
 * DAO truy vấn vai trò người dùng ({@link Role}).
 */
public interface RoleDAO {

    /**
     * Lấy vai trò theo tên.
     *
     * @param roleName tên vai trò
     * @return entity hoặc {@code null} nếu không tìm thấy
     */
    Role getByName(String roleName);
}

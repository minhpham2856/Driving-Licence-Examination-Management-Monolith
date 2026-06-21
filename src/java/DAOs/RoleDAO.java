package DAOs;

import Models.Role;

/**
 * DAO cho thao tác với vai trò (Role) trong hệ thống.
 * Cung cấp các phương thức tra cứu thông tin vai trò theo mã hoặc tên.
 */
public interface RoleDAO {

    /**
     * Lấy thông tin vai trò theo mã.
     *
     * @param id mã vai trò
     * @return Role model, hoặc null nếu không tìm thấy
     */
    Role getById(int id);

    /**
     * Lấy thông tin vai trò theo tên.
     *
     * @param roleName tên vai trò (vd: "Admin", "Examiner", "Staff")
     * @return Role model, hoặc null nếu không tìm thấy
     */
    Role getByName(String roleName);
}

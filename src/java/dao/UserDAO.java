package dao;


import model.user.User;

/**
 * DAO cho thao tác với người dùng (User) trong hệ thống.
 * Cung cấp các phương thức truy vấn thông tin người dùng theo mã, tên đăng nhập,
 * định danh, email, cũng như thêm mới và cập nhật mật khẩu.
 */
public interface UserDAO {

    /**
     * Lấy thông tin người dùng theo mã.
     *
     * @param id mã người dùng
     * @return User model, hoặc null nếu không tìm thấy
     */
    User getById(int id);

    /**
     * Lấy thông tin người dùng theo tên đăng nhập.
     *
     * @param username tên đăng nhập
     * @return User model, hoặc null nếu không tìm thấy
     */
    User getByUsername(String username);

    /**
     * Lấy thông tin người dùng theo định danh duy nhất.
     *
     * @param identifier định danh người dùng
     * @return User model, hoặc null nếu không tìm thấy
     */
    User getByIdentifier(String identifier);

    /**
     * Lấy thông tin người dùng theo địa chỉ email.
     *
     * @param email địa chỉ email
     * @return User model, hoặc null nếu không tìm thấy
     */
    User getByEmail(String email);

    /**
     * Thêm mới một người dùng.
     *
     * @param user đối tượng User chứa thông tin người dùng
     * @return true nếu thêm thành công
     */
    boolean insert(User user);

    /**
     * Cập nhật mật khẩu cho người dùng.
     *
     * @param userId       mã người dùng
     * @param passwordHash hash mật khẩu mới
     * @return true nếu cập nhật thành công
     */
    boolean updatePassword(int userId, String passwordHash);
}

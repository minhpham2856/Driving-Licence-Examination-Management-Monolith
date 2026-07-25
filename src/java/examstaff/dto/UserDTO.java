package examstaff.dto;

import shared.model.Profile;

/**
 * DTO người dùng rút gọn cho ExamStaff (chủ yếu sát hạch viên / picker phân công).
 *
 * Vai trò:
 * Mang userId, username và Profile hiển thị trên examiner-allocation.jsp;
 * không chứa nghiệp vụ phân quyền.
 *
 * Ai tạo / tiêu thụ:
 * ExaminerAssignmentDAOImpl → ExaminerAllocationViewDTO / slot picker.
 */
public class UserDTO {
    private int userId;
    private String username;
    private Profile profile;

    /** User rỗng — map từ DAO. */
    public UserDTO() {
    }

    /**
     * Khởi tạo đủ định danh và hồ sơ hiển thị.
     * @param userId   mã user hệ thống
     * @param username tên đăng nhập
     * @param profile  hồ sơ cá nhân (họ tên…)
     */
    public UserDTO(int userId, String username, Profile profile) {
        this.userId = userId;
        this.username = username;
        this.profile = profile;
    }

    /** Mã người dùng hệ thống. */
    public int getUserId() {
        return userId;
    }

    /** Gán mã người dùng. */
    public void setUserId(int userId) {
        this.userId = userId;
    }

    /** Alias userId cho JSP / legacy (id). */
    public int getId() {
        return userId;
    }

    /** Gán id (alias setter cho userId). */
    public void setId(int id) {
        this.userId = id;
    }

    /** Tên đăng nhập. */
    public String getUsername() {
        return username;
    }

    /** Gán tên đăng nhập. */
    public void setUsername(String username) {
        this.username = username;
    }

    /** Hồ sơ cá nhân (họ tên, liên hệ…) gắn user. */
    public Profile getProfile() {
        return profile;
    }

    /** Gán hồ sơ cá nhân. */
    public void setProfile(Profile profile) {
        this.profile = profile;
    }
}

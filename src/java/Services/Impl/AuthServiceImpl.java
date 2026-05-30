package Services.Impl;

import DAO.PersonDAO;
import DAO.RoleDAO;
import DAO.UserDAO;
import DAO.Impl.PersonDAOImpl;
import DAO.Impl.RoleDAOImpl;
import DAO.Impl.UserDAOImpl;
import Models.Person;
import Models.Role;
import Models.User;
import Services.AuthService;
import org.mindrot.jbcrypt.BCrypt;

public class AuthServiceImpl implements AuthService {

    private final PersonDAO personDAO = new PersonDAOImpl();
    private final UserDAO userDAO = new UserDAOImpl();
    private final RoleDAO roleDAO = new RoleDAOImpl();

    @Override
    public String register(String username, String email, String password) {
        if (userDAO.getByUsername(username) != null) {
            return "Tên đăng nhập đã tồn tại.";
        }

        if (personDAO.getByEmail(email) != null) {
            return "Địa chỉ email đã được sử dụng.";
        }

        Role role = roleDAO.getByName("Registrant");
        int roleId = 6; // roleId = 6 -> Registrant
        if (role != null) {
            roleId = role.getId();
        }

        // Create new default personal info
        Person person = new Person();
        person.setFullName(username);
        person.setDateOfBirth(java.sql.Date.valueOf("2000-01-01")); // Avoid null exception
        person.setPhoneNo("");
        person.setEmail(email);
        person.setIsWalkIn(false);
        person.setApprovalStatus("Pending");

        if (!personDAO.insert(person)) {
            return "Lỗi! Vui lòng thử lại.";
        }

        // Hash pw
        String passwordHash = BCrypt.hashpw(password, BCrypt.gensalt());
        User user = new User();
        user.setPersonId(person.getId());
        user.setUsername(username);
        user.setPasswordHash(passwordHash);
        user.setRoleId(roleId);
        user.setIsActive(true);

        if (!userDAO.insert(user)) {
            return "Không thể đăng ký tài khoản. Vui lòng thử lại.";
        }

        return null;
    }

    @Override
    public User login(String identifier, String password) {
        User user = userDAO.getByIdentifier(identifier); // identifier = username, email, phone number
        if (user == null || !user.isIsActive()) {
            return null;
        }

        return BCrypt.checkpw(password, user.getPasswordHash()) ? user : null; // if correct pw then returns user
    }

    @Override
    public String forgotPassword(String email) {
        Person person = personDAO.getByEmail(email);
        if (person == null) {
            return "Không tìm thấy email";
        }

        User user = userDAO.getByIdentifier(email);
        if (user == null) {
            return "Không tìm thấy tài khoản";
        }

        String tempPassword = String.valueOf((int) ((Math.random() * 900000) + 100000));
        String hashed = BCrypt.hashpw(tempPassword, BCrypt.gensalt());

        if (!userDAO.updatePassword(user.getId(), hashed)) {
            return "Không thể cập nhật mật khẩu khôi phục. Vui lòng thử lại.";
        }

        System.out.println("==========================================================================");
        System.out.println("[LOG_TEST]");
        System.out.println("Đến: " + email);
        System.out.println("Tiêu đề: [Lái Vui] Khôi phục mật khẩu tài khoản");
        System.out.println("Nội dung:");
        System.out.println("  Xin chào " + user.getUsername() + ",");
        System.out.println("  Mật khẩu của bạn đã được khôi phục thành công");
        System.out.println("  Mật khẩu tạm thời mới là: " + tempPassword);
        System.out.println("  Vui lòng đăng nhập lại và đổi mật khẩu trong phần cài đặt tài khoản.");
        System.out.println("==========================================================================");

        return null;
    }
}

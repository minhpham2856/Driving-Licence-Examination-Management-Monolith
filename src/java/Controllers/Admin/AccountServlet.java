package Controllers.Admin;

import Constants.RoleUi;
import DAO.AccountManageDAO;
import DAO.Impl.AccountManageDAOImpl;
import Models.AccountView;
import Models.User;
import Utils.AuditLogHelper;
import Utils.PasswordGenerator;
import Utils.Sanitize;
import Utils.SessionUtil;
import Utils.Validator;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebServlet(name = "AccountServlet", urlPatterns = {"/admin/accounts"})
public class AccountServlet extends HttpServlet {

    private final AccountManageDAO dao = new AccountManageDAOImpl();
    private static final String LIST_VIEW = "/views/admin/accounts.jsp";

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        if (!SessionUtil.requireAdmin(req, resp)) return;

        // Đảm bảo nhận dữ liệu từ khóa tìm kiếm tiếng Việt không bị lỗi font
        req.setCharacterEncoding("UTF-8");

        String keyword = Sanitize.text(req.getParameter("searchKeyword"));
        String roleUi = Sanitize.text(req.getParameter("filterRole"));
        String statusFilter = Sanitize.text(req.getParameter("filterStatus"));

        String dbRole = roleUi.isEmpty() ? null : RoleUi.toDbRole(roleUi);
        Boolean active = null;
        if ("active".equals(statusFilter)) active = true;
        else if ("inactive".equals(statusFilter) || "locked".equals(statusFilter)) active = false;

        req.setAttribute("accounts", dao.search(keyword, dbRole, active));
        req.setAttribute("totalAccounts", dao.countAll());
        req.setAttribute("adminCount", dao.countByRole("Admin"));
        req.setAttribute("coiThiCount", dao.countByRole("ExamStaff"));
        req.setAttribute("chamThiCount", dao.countByRole("Examiner"));
        req.getRequestDispatcher(LIST_VIEW).forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        if (!SessionUtil.requireAdmin(req, resp)) return;

        // Đảm bảo dữ liệu form tiếng Việt gửi lên không bị lỗi font
        req.setCharacterEncoding("UTF-8");

        String action = Sanitize.text(req.getParameter("action"));
        User admin = SessionUtil.getCurrentUser(req);
        Integer actorId = (admin != null) ? admin.getId() : null;
        String ctx = req.getContextPath();

        // ---- Khóa / Mở khóa ----
        if ("lock".equals(action)) {
            int id = Sanitize.toInt(req.getParameter("id"), 0);
            boolean lock = "true".equals(req.getParameter("lock"));
            boolean ok = id > 0 && dao.setStatus(id, !lock, actorId);
            if (ok) AuditLogHelper.persist(req.getSession(), "UPDATE",
                    (lock ? "Khóa" : "Mở khóa") + " tài khoản #" + id, id);
            SessionUtil.flash(req, ok ? "success" : "danger",
                    ok ? (lock ? "Đã khóa tài khoản." : "Đã mở khóa tài khoản.") : "Thao tác thất bại.");
            resp.sendRedirect(ctx + "/admin/accounts");
            return;
        }

        // ---- Cấp lại mật khẩu ----
        if ("reset".equals(action)) {
            int id = Sanitize.toInt(req.getParameter("id"), 0);
            AccountView acc = (id > 0) ? dao.findById(id) : null;
            if (acc == null) {
                SessionUtil.flash(req, "danger", "Không tìm thấy tài khoản.");
                resp.sendRedirect(ctx + "/admin/accounts");
                return;
            }
            String tempPw = PasswordGenerator.generate();
            boolean ok = dao.resetPassword(id, tempPw, actorId);
            if (ok) {
                AuditLogHelper.persist(req.getSession(), "UPDATE", "Cấp lại mật khẩu tài khoản: " + acc.getUsername(), id);
                showTempPassword(req, acc.getUsername(), tempPw); // hiện 1 lần
                SessionUtil.flash(req, "success", "Đã cấp lại mật khẩu cho \"" + acc.getUsername() + "\".");
            } else {
                SessionUtil.flash(req, "danger", "Cấp lại mật khẩu thất bại.");
            }
            resp.sendRedirect(ctx + "/admin/accounts");
            return;
        }

        // ---- Xóa ----
        if ("delete".equals(action)) {
            int id = Sanitize.toInt(req.getParameter("id"), 0);
            boolean ok = id > 0 && dao.delete(id);
            if (ok) {
                AuditLogHelper.persist(req.getSession(), "DELETE", "Xóa tài khoản #" + id, id);
                SessionUtil.flash(req, "success", "Đã xóa tài khoản.");
            } else {
                SessionUtil.flash(req, "danger",
                        "Không thể xóa (tài khoản đang được tham chiếu bởi dữ liệu khác). Hãy dùng \"Khóa\" thay thế.");
            }
            resp.sendRedirect(ctx + "/admin/accounts");
            return;
        }

        // ---- Tạo mới (chỉ tạo, KHÔNG sửa) ----
        String username = Sanitize.text(req.getParameter("username"));
        String email = Sanitize.text(req.getParameter("email"));
        String rawRole = Sanitize.text(req.getParameter("role"));
        String roleDb = (rawRole.equals("Admin") || rawRole.equals("Examiner") || rawRole.equals("ExamStaff")
                || rawRole.equals("ManagingStaff") || rawRole.equals("Registrant"))
                ? rawRole : RoleUi.toDbRole(rawRole);
        String fullName = Sanitize.text(req.getParameter("fullName"));
        String phone = Sanitize.text(req.getParameter("phone"));
        String sex = Sanitize.text(req.getParameter("sex"));
        String govId = Sanitize.text(req.getParameter("govId"));
        String address = Sanitize.text(req.getParameter("address"));
        String dobStr = Sanitize.text(req.getParameter("dateOfBirth"));
        boolean active = !"inactive".equals(Sanitize.text(req.getParameter("status")));

        java.sql.Date dob = null;
        try { if (!dobStr.isEmpty()) dob = java.sql.Date.valueOf(dobStr); } catch (Exception ignore) {}

        // Validate bằng Validator (server-side)
        String error = Validator.username(username);
        if (error == null) error = Validator.email(email);
        if (error == null) error = Validator.fullName(fullName);
        if (error == null) error = Validator.phone(phone);
        if (error == null) error = Validator.sex(sex);
        if (error == null) error = Validator.govId(govId);
        if (error == null) error = Validator.dateOfBirth(dob);
        if (error == null && rawRole.isEmpty()) error = "Vui lòng chọn vai trò.";
        if (error == null && dao.usernameExists(username, 0)) error = "Tên đăng nhập đã tồn tại.";
        if (error == null && dao.emailExists(email, 0))       error = "Email đã được sử dụng.";
        if (error == null && dao.phoneExists(phone, 0))       error = "Số điện thoại đã được sử dụng.";
        if (error == null && dao.govIdExists(govId, 0))       error = "Số CCCD/CMND đã tồn tại.";

        if (error != null) {
            SessionUtil.flash(req, "danger", error);
            resp.sendRedirect(ctx + "/admin/accounts");
            return;
        }

        AccountView a = new AccountView();
        a.setUsername(username);
        a.setEmail(email);
        a.setRole(roleDb);
        a.setActive(active);
        a.setFullName(fullName);
        a.setPhone(phone);
        a.setSex(sex);
        a.setGovId(govId);
        a.setAddress(address.isEmpty() ? null : address);
        a.setDateOfBirth(dob);

        String tempPw = PasswordGenerator.generate();   // mật khẩu tạm ngẫu nhiên
        int newId = dao.create(a, tempPw, actorId);
        boolean ok = newId > 0;
        if (ok) {
            AuditLogHelper.persist(req.getSession(), "INSERT",
                    "Tạo tài khoản: " + username + " (" + roleDb + ")", newId);
            showTempPassword(req, username, tempPw); // hiện 1 lần cho admin
            SessionUtil.flash(req, "success",
                    "Đã tạo tài khoản \"" + username + "\". Vui lòng gửi mật khẩu tạm bên dưới cho người dùng.");
        } else {
            SessionUtil.flash(req, "danger", "Tạo tài khoản thất bại (kiểm tra dữ liệu trùng).");
        }
        resp.sendRedirect(ctx + "/admin/accounts");
    }

    /** Lưu tạm vào session để accounts.jsp hiển thị mật khẩu tạm ĐÚNG 1 LẦN. */
    private void showTempPassword(HttpServletRequest req, String username, String tempPw) {
        req.getSession().setAttribute("newAccUsername", username);
        req.getSession().setAttribute("newAccPassword", tempPw);
    }
}
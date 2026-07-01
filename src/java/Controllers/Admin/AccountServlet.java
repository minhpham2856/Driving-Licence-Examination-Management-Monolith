package Controllers.Admin;

import Constants.RoleUi;
import DAO.AccountManageDAO;
import DAO.Impl.AccountManageDAOImpl;
import Models.AccountView;
import Models.User;
import Utils.AuditLogHelper;
import Utils.Sanitize;
import Utils.SessionUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Admin account management: create / edit / assign role / lock / delete.
 * GET  /admin/accounts                 -> list (filters: searchKeyword, filterRole, filterStatus)
 * POST /admin/accounts?action=save     -> create or update (+ role)
 * POST /admin/accounts?action=lock     -> lock / unlock (Status bit)
 * POST /admin/accounts?action=delete   -> delete
 */
@WebServlet(name = "AccountServlet", urlPatterns = {"/admin/accounts"})
public class AccountServlet extends HttpServlet {

    private final AccountManageDAO dao = new AccountManageDAOImpl();
    private static final String LIST_VIEW = "/views/admin/accounts.jsp";

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        if (!SessionUtil.requireAdmin(req, resp)) return;

        // Đảm bảo nhận từ khóa tìm kiếm tiếng Việt không lỗi font
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
        
        // Đảm bảo nhận thông tin form tiếng Việt chuẩn xác
        req.setCharacterEncoding("UTF-8");
        
        String action = Sanitize.text(req.getParameter("action"));
        User admin = SessionUtil.getCurrentUser(req);
        Integer actorId = (admin != null) ? admin.getId() : null;
        String ctx = req.getContextPath();

        if ("lock".equals(action)) {
            int id = Sanitize.toInt(req.getParameter("id"), 0);
            boolean lock = "true".equals(req.getParameter("lock")); // lock=true -> set inactive
            boolean ok = id > 0 && dao.setStatus(id, !lock, actorId);
            if (ok) AuditLogHelper.persist(req.getSession(), "UPDATE",
                    (lock ? "Khóa" : "Mở khóa") + " tài khoản #" + id, id);
            SessionUtil.flash(req, ok ? "success" : "danger",
                    ok ? (lock ? "Đã khóa tài khoản." : "Đã mở khóa tài khoản.") : "Thao tác thất bại.");
            resp.sendRedirect(ctx + "/admin/accounts");
            return;
        }

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

        // ---- save (create / update) ----
        int id = Sanitize.toInt(req.getParameter("userId"), 0);
        boolean isEdit = id > 0;

        String username = Sanitize.text(req.getParameter("username"));
        String email = Sanitize.text(req.getParameter("email"));
        String password = req.getParameter("password"); // may be blank on edit
        // The role select submits the DB role string directly (Admin/Examiner/ExamStaff/ManagingStaff/Registrant).
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

        String error = null;
        if (username.isEmpty()) error = "Vui lòng nhập tên đăng nhập.";
        else if (email.isEmpty()) error = "Vui lòng nhập email.";
        else if (!isEdit && (password == null || password.length() < 6)) error = "Mật khẩu phải có ít nhất 6 ký tự.";
        else if (fullName.isEmpty()) error = "Vui lòng nhập họ tên.";
        else if (dob == null) error = "Vui lòng nhập ngày sinh hợp lệ.";
        else if (phone.isEmpty()) error = "Vui lòng nhập số điện thoại.";
        else if (sex.isEmpty()) error = "Vui lòng chọn giới tính.";
        else if (govId.isEmpty()) error = "Vui lòng nhập số CCCD/CMND.";
        else if (rawRole.isEmpty()) error = "Vui lòng chọn vai trò.";
        else if (dao.usernameExists(username, id)) error = "Tên đăng nhập đã tồn tại.";
        else if (dao.emailExists(email, id)) error = "Email đã được sử dụng.";
        else if (dao.govIdExists(govId, id)) error = "Số CCCD/CMND đã tồn tại.";

        if (error != null) {
            SessionUtil.flash(req, "danger", error);
            resp.sendRedirect(ctx + "/admin/accounts");
            return;
        }

        AccountView a = new AccountView();
        a.setUserId(id);
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

        if (isEdit) {
            boolean ok = dao.update(a, (password == null || password.isBlank()) ? null : password, actorId);
            AuditLogHelper.persist(req.getSession(), "UPDATE", "Cập nhật tài khoản: " + username, id);
            SessionUtil.flash(req, ok ? "success" : "danger",
                    ok ? "Đã cập nhật tài khoản \"" + username + "\"." : "Cập nhật thất bại.");
        } else {
            int newId = dao.create(a, password, actorId);
            boolean ok = newId > 0;
            AuditLogHelper.persist(req.getSession(), "INSERT",
                    "Tạo tài khoản: " + username + " (" + roleDb + ")", newId);
            SessionUtil.flash(req, ok ? "success" : "danger",
                    ok ? "Đã tạo tài khoản \"" + username + "\"." : "Tạo tài khoản thất bại (kiểm tra dữ liệu trùng).");
        }
        resp.sendRedirect(ctx + "/admin/accounts");
    }
}
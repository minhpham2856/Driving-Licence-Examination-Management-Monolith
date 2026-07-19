package admin.controller;

import admin.dao.AccountManageDAO;
import admin.dao.impl.AccountManageDAOImpl;
import admin.model.AccountView;
import admin.util.AdminAuditLog;
import admin.util.PasswordGenerator;
import admin.util.RoleUi;
import admin.util.Sanitize;
import admin.util.SessionUtil;
import admin.util.Validator;
import shared.model.User;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import auth.dto.UserDTO;

@WebServlet(name = "AccountServlet", urlPatterns = {"/admin/accounts"})
public class AccountServlet extends HttpServlet {

    private final AccountManageDAO dao = new AccountManageDAOImpl();
    private static final String LIST_VIEW = "/views/admin/accounts.jsp";

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        if (!SessionUtil.requireAdmin(req, resp)) return;
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
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        if (!SessionUtil.requireAdmin(req, resp)) return;
        String action = Sanitize.text(req.getParameter("action"));
        auth.dto.UserDTO admin = SessionUtil.getCurrentUser(req);
        Integer actorId = (admin != null) ? admin.getUserId() : null;
        String ctx = req.getContextPath();

        if ("lock".equals(action)) {
            int id = Sanitize.toInt(req.getParameter("id"), 0);
            boolean lock = "true".equals(req.getParameter("lock"));
            boolean ok = id > 0 && dao.setStatus(id, !lock, actorId);
            if (ok) AdminAuditLog.persist(req.getSession(), "UPDATE", (lock ? "Khóa" : "Mở khóa") + " tài khoản #" + id, id);
            SessionUtil.flash(req, ok ? "success" : "danger",
                    ok ? (lock ? "Đã khóa tài khoản." : "Đã mở khóa tài khoản.") : "Thao tác thất bại.");
            resp.sendRedirect(ctx + "/admin/accounts");
            return;
        }

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
                AdminAuditLog.persist(req.getSession(), "UPDATE", "Cấp lại mật khẩu tài khoản: " + acc.getUsername(), id);
                showTempPassword(req, acc.getUsername(), tempPw);
                SessionUtil.flash(req, "success", "Đã cấp lại mật khẩu cho \"" + acc.getUsername() + "\".");
            } else {
                SessionUtil.flash(req, "danger", "Cấp lại mật khẩu thất bại.");
            }
            resp.sendRedirect(ctx + "/admin/accounts");
            return;
        }

        if ("delete".equals(action)) {
            int id = Sanitize.toInt(req.getParameter("id"), 0);
            boolean ok = id > 0 && dao.delete(id);
            if (ok) {
                AdminAuditLog.persist(req.getSession(), "DELETE", "Xóa tài khoản #" + id, id);
                SessionUtil.flash(req, "success", "Đã xóa tài khoản.");
            } else {
                SessionUtil.flash(req, "danger", "Không thể xóa (tài khoản đang được tham chiếu bởi dữ liệu khác). Hãy dùng \"Khóa\" thay thế.");
            }
            resp.sendRedirect(ctx + "/admin/accounts");
            return;
        }

        // ---- Tạo mới ----
        String username = Sanitize.text(req.getParameter("username"));
        String email = Sanitize.text(req.getParameter("email"));
        String rawRole = Sanitize.text(req.getParameter("role"));
        String roleDb = (rawRole.equals("Admin") || rawRole.equals("Examiner") || rawRole.equals("ExamStaff")
                || rawRole.equals("ManagingStaff") || rawRole.equals("Registrant")) ? rawRole : RoleUi.toDbRole(rawRole);
        String fullName = Sanitize.text(req.getParameter("fullName"));
        String phone = Sanitize.text(req.getParameter("phone"));
        String sex = Sanitize.text(req.getParameter("sex"));
        String govId = Sanitize.text(req.getParameter("govId"));
        String address = Sanitize.text(req.getParameter("address"));
        String dobStr = Sanitize.text(req.getParameter("dateOfBirth"));
        boolean active = !"inactive".equals(Sanitize.text(req.getParameter("status")));

        java.sql.Date dob = null;
        try { if (!dobStr.isEmpty()) dob = java.sql.Date.valueOf(dobStr); } catch (Exception ignore) {}

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
        a.setUsername(username); a.setEmail(email); a.setRole(roleDb); a.setActive(active);
        a.setFullName(fullName); a.setPhone(phone); a.setSex(sex); a.setGovId(govId);
        a.setAddress(address.isEmpty() ? null : address); a.setDateOfBirth(dob);

        String tempPw = PasswordGenerator.generate();
        int newId = dao.create(a, tempPw, actorId);
        boolean ok = newId > 0;
        if (ok) {
            AdminAuditLog.persist(req.getSession(), "INSERT", "Tạo tài khoản: " + username + " (" + roleDb + ")", newId);
            showTempPassword(req, username, tempPw);
            SessionUtil.flash(req, "success", "Đã tạo tài khoản \"" + username + "\". Vui lòng gửi mật khẩu tạm bên dưới cho người dùng.");
        } else {
            SessionUtil.flash(req, "danger", "Tạo tài khoản thất bại (kiểm tra dữ liệu trùng).");
        }
        resp.sendRedirect(ctx + "/admin/accounts");
    }

    private void showTempPassword(HttpServletRequest req, String username, String tempPw) {
        req.getSession().setAttribute("newAccUsername", username);
        req.getSession().setAttribute("newAccPassword", tempPw);
    }
}

package admin.controller;

import admin.dao.AccountManageDAO;
import admin.dao.impl.AccountManageDAOImpl;
import admin.model.AccountView;
import admin.util.AdminAuditLog;
import admin.util.PasswordGenerator;
import admin.util.Sanitize;
import admin.util.SessionUtil;
import admin.util.Validator;
import auth.dto.UserDTO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

@WebServlet(name = "AccountServlet", urlPatterns = {"/admin/accounts"})
public class AccountServlet extends HttpServlet {

    private final AccountManageDAO dao = new AccountManageDAOImpl();
    private static final String LIST_VIEW = "/views/admin/accounts.jsp";

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        if (!SessionUtil.requireAdmin(req, resp)) return;
        String keyword = Sanitize.text(req.getParameter("searchKeyword"));
        Integer roleId = Sanitize.toIntegerOrNull(req.getParameter("filterRole"));
        String statusFilter = Sanitize.text(req.getParameter("filterStatus"));
        Boolean active = null;
        if ("active".equals(statusFilter)) active = true;
        else if ("inactive".equals(statusFilter) || "locked".equals(statusFilter)) active = false;

        req.setAttribute("accounts", dao.search(keyword, roleId, active));
        req.setAttribute("roles", dao.listRoles());

        // Thống kê theo roleCode (không phụ thuộc tên role tiếng gì trong DB)
        List<AccountView> all = dao.search(null, null, null);
        int admin = 0, coi = 0, cham = 0;
        for (AccountView a : all) {
            String rc = a.getRoleCode();
            if ("admin".equals(rc)) admin++;
            else if ("coi_thi".equals(rc)) coi++;
            else if ("cham_thi".equals(rc)) cham++;
        }
        req.setAttribute("totalAccounts", all.size());
        req.setAttribute("adminCount", admin);
        req.setAttribute("coiThiCount", coi);
        req.setAttribute("chamThiCount", cham);
        req.getRequestDispatcher(LIST_VIEW).forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        if (!SessionUtil.requireAdmin(req, resp)) return;
        String action = Sanitize.text(req.getParameter("action"));
        UserDTO admin = SessionUtil.getCurrentUser(req);
        String ctx = req.getContextPath();

        if ("lock".equals(action)) {
            int id = Sanitize.toInt(req.getParameter("id"), 0);
            boolean lock = "true".equals(req.getParameter("lock"));
            boolean ok = id > 0 && dao.setStatus(id, !lock);
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
                resp.sendRedirect(ctx + "/admin/accounts"); return;
            }
            String tempPw = PasswordGenerator.generate();
            boolean ok = dao.resetPassword(id, tempPw);
            if (ok) {
                AdminAuditLog.persist(req.getSession(), "UPDATE", "Cấp lại mật khẩu tài khoản: " + acc.getUsername(), id);
                req.getSession().setAttribute("newAccUsername", acc.getUsername());
                req.getSession().setAttribute("newAccPassword", tempPw);
                SessionUtil.flash(req, "success", "Đã cấp lại mật khẩu cho \"" + acc.getUsername() + "\".");
            } else SessionUtil.flash(req, "danger", "Cấp lại mật khẩu thất bại.");
            resp.sendRedirect(ctx + "/admin/accounts");
            return;
        }

        if ("delete".equals(action)) {
            int id = Sanitize.toInt(req.getParameter("id"), 0);
            boolean ok = id > 0 && dao.delete(id);
            if (ok) { AdminAuditLog.persist(req.getSession(), "DELETE", "Xóa tài khoản #" + id, id);
                SessionUtil.flash(req, "success", "Đã xóa tài khoản."); }
            else SessionUtil.flash(req, "danger", "Không thể xóa (tài khoản đang được tham chiếu). Hãy dùng \"Khóa\".");
            resp.sendRedirect(ctx + "/admin/accounts");
            return;
        }

        // ---- Tạo mới ----
        String username = Sanitize.text(req.getParameter("username"));
        String email = Sanitize.text(req.getParameter("email"));
        int roleId = Sanitize.toInt(req.getParameter("role"), 0);
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
        if (error == null && !sex.equals("Nam") && !sex.equals("Nữ")) error = "Vui lòng chọn giới tính (Nam hoặc Nữ).";
        if (error == null) error = Validator.govId(govId);
        if (error == null) error = Validator.dateOfBirth(dob);
        if (error == null && roleId <= 0) error = "Vui lòng chọn vai trò.";
        if (error == null && dao.usernameExists(username)) error = "Tên đăng nhập đã tồn tại.";
        if (error == null && dao.emailExists(email))       error = "Email đã được sử dụng.";
        if (error == null && dao.phoneExists(phone))       error = "Số điện thoại đã được sử dụng.";
        if (error == null && dao.govIdExists(govId))       error = "Số CCCD/CMND đã tồn tại.";

        if (error != null) {
            SessionUtil.flash(req, "danger", error);
            resp.sendRedirect(ctx + "/admin/accounts");
            return;
        }

        AccountView a = new AccountView();
        a.setUsername(username); a.setEmail(email); a.setActive(active);
        a.setFullName(fullName); a.setPhone(phone); a.setGovId(govId);
        a.setAddress(address.isEmpty() ? null : address); a.setDateOfBirth(dob);
        boolean sexMale = sex.equals("Nam");

        String tempPw = PasswordGenerator.generate();
        int newId = dao.create(a, roleId, sexMale, tempPw);
        if (newId > 0) {
            AdminAuditLog.persist(req.getSession(), "INSERT", "Tạo tài khoản: " + username, newId);
            req.getSession().setAttribute("newAccUsername", username);
            req.getSession().setAttribute("newAccPassword", tempPw);
            SessionUtil.flash(req, "success", "Đã tạo tài khoản \"" + username + "\". Gửi mật khẩu tạm bên dưới cho người dùng.");
        } else {
            SessionUtil.flash(req, "danger", "Tạo tài khoản thất bại (kiểm tra dữ liệu trùng).");
        }
        resp.sendRedirect(ctx + "/admin/accounts");
    }
}
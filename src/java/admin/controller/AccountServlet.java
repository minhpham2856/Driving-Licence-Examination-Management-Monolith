package admin.controller;

import admin.dao.AccountManageDAO;
import admin.dao.impl.AccountManageDAOImpl;
import admin.model.AccountView;
import admin.model.RoleOption;
import admin.service.AccountExcelService;
import admin.service.impl.AccountExcelServiceImpl;
import admin.util.AdminAuditLog;
import admin.util.BulkResult;
import admin.util.ExcelDownload;
import admin.util.PasswordGenerator;
import admin.util.RoleUi;
import admin.util.Sanitize;
import admin.util.SessionUtil;
import admin.util.Validator;
import auth.dto.UserDTO;
import auth.service.EmailService;
import auth.service.impl.EmailServiceImpl;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.Part;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

@WebServlet(name = "AccountServlet", urlPatterns = {"/admin/accounts"})
@MultipartConfig(fileSizeThreshold = 1024 * 1024, maxFileSize = 10 * 1024 * 1024, maxRequestSize = 12 * 1024 * 1024)
public class AccountServlet extends HttpServlet {

    private final AccountManageDAO dao = new AccountManageDAOImpl();
    private final EmailService emailService = new EmailServiceImpl();
    private final AccountExcelService excelService = new AccountExcelServiceImpl();
    private static final String LIST_VIEW = "/views/admin/accounts.jsp";
    private static final int TEMP_PW_LENGTH = 6;

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        if (!SessionUtil.requireAdmin(req, resp)) return;

        String action = Sanitize.text(req.getParameter("action"));

        // Tải biểu mẫu Excel trống để điền rồi import
        if ("template".equals(action)) {
            ExcelDownload.send(resp,"Bieu-mau-import-tai-khoan.xlsx",
                    out -> excelService.writeTemplate(dao.listRoles(), out));
            return;
        }

        String keyword = Sanitize.text(req.getParameter("searchKeyword"));
        Integer roleId = Sanitize.toIntegerOrNull(req.getParameter("filterRole"));
        String statusFilter = Sanitize.text(req.getParameter("filterStatus"));
        Boolean active = null;
        if ("active".equals(statusFilter)) active = true;
        else if ("inactive".equals(statusFilter) || "locked".equals(statusFilter)) active = false;

        List<AccountView> accounts = dao.search(keyword, roleId, active);

        // Xuất Excel đúng danh sách đang hiển thị (giữ nguyên bộ lọc hiện tại)
        if ("export".equals(action)) {
            ExcelDownload.send(resp,"Danh-sach-tai-khoan.xlsx", out -> excelService.writeAccounts(accounts, out));
            return;
        }

        req.setAttribute("accounts", accounts);
        req.setAttribute("roles", dao.listRoles());

        // Thống kê theo roleCode
        List<AccountView> all = dao.search(null, null, null);
        int admin = 0, coi = 0, cham = 0;
        for (AccountView a : all) {
            String rc = a.getRoleCode();
            if (RoleUi.ADMIN.equals(rc)) admin++;
            else if (RoleUi.COI_THI.equals(rc)) coi++;
            else if (RoleUi.CHAM_THI.equals(rc)) cham++;
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
        String ctx = req.getContextPath();

        if ("import".equals(action)) {
            handleImport(req, resp);
            return;
        }

        if ("bulkDelete".equals(action)) {
            handleBulkDelete(req, resp);
            return;
        }

        if ("lock".equals(action)) {
            int id = Sanitize.toInt(req.getParameter("id"), 0);
            boolean lock = "true".equals(req.getParameter("lock"));
            AccountView target = (id > 0) ? dao.findById(id) : null;

            String denied = denyIfAdminTarget(req, target, lock ? "khóa" : "mở khóa");
            if (denied != null) {
                SessionUtil.flash(req, "danger", denied);
                resp.sendRedirect(ctx + "/admin/accounts");
                return;
            }

            boolean ok = dao.setStatus(id, !lock);
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
            String tempPw = PasswordGenerator.generateNumeric(TEMP_PW_LENGTH);
            boolean ok = dao.resetPassword(id, tempPw);
            if (ok) {
                AdminAuditLog.persist(req.getSession(), "UPDATE", "Cấp lại mật khẩu tài khoản: " + acc.getUsername(), id);
                String mailError = sendCredentialsEmailAndGetError(acc.getEmail(), acc.getUsername(), tempPw, true);
                if (mailError == null) {
                    SessionUtil.flash(req, "success", "Đã cấp lại mật khẩu và gửi email tới " + acc.getEmail() + ".");
                } else {
                    SessionUtil.flash(req, "warning", "Đã cấp lại mật khẩu cho \"" + acc.getUsername()
                            + "\" nhưng gửi email thất bại: " + mailError + " Hãy sửa cấu hình rồi thử \"Cấp lại mật khẩu\" lại.");
                }
            } else SessionUtil.flash(req, "danger", "Cấp lại mật khẩu thất bại.");
            resp.sendRedirect(ctx + "/admin/accounts");
            return;
        }

        if ("delete".equals(action)) {
            int id = Sanitize.toInt(req.getParameter("id"), 0);
            AccountView target = (id > 0) ? dao.findById(id) : null;

            String denied = denyIfAdminTarget(req, target, "xóa");
            if (denied != null) {
                SessionUtil.flash(req, "danger", denied);
                resp.sendRedirect(ctx + "/admin/accounts");
                return;
            }

            boolean ok = dao.delete(id);
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

        String error = validateAccountInput(username, email, fullName, phone, sex, govId, dob, roleId);

        if (error != null) {
            reopenAccountModal(req, error, username, email, roleId, fullName, phone, dobStr, sex, govId, address, active);
            resp.sendRedirect(ctx + "/admin/accounts");
            return;
        }

        AccountView a = new AccountView();
        a.setUsername(username); a.setEmail(email); a.setActive(active);
        a.setFullName(fullName); a.setPhone(phone); a.setGovId(govId);
        a.setAddress(address.isEmpty() ? null : address); a.setDateOfBirth(dob);
        boolean sexMale = sex.equals("Nam");

        String tempPw = PasswordGenerator.generateNumeric(TEMP_PW_LENGTH);
        int newId = dao.create(a, roleId, sexMale, tempPw);
        if (newId > 0) {
            AdminAuditLog.persist(req.getSession(), "INSERT", "Tạo tài khoản: " + username, newId);
            String mailError = sendCredentialsEmailAndGetError(email, username, tempPw, false);
            if (mailError == null) {
                SessionUtil.flash(req, "success", "Đã tạo tài khoản \"" + username + "\" và gửi mật khẩu tạm tới email " + email + ".");
            } else {
                SessionUtil.flash(req, "warning", "Đã tạo tài khoản \"" + username
                        + "\" nhưng gửi email thất bại: " + mailError + " Hãy sửa cấu hình rồi dùng \"Cấp lại mật khẩu\" để gửi lại.");
            }
        } else {
            reopenAccountModal(req, "Tạo tài khoản thất bại (dữ liệu trùng với tài khoản khác). Vui lòng kiểm tra lại.",
                    username, email, roleId, fullName, phone, dobStr, sex, govId, address, active);
        }
        resp.sendRedirect(ctx + "/admin/accounts");
    }

    // ------------------------------------------------------------- import

    private void handleImport(HttpServletRequest req, HttpServletResponse resp) throws IOException, ServletException {
        String ctx = req.getContextPath();
        Part part = null;
        try {
            part = req.getPart("file");
        } catch (Exception e) {
            // request không phải multipart hoặc vượt dung lượng
        }
        if (part == null || part.getSize() == 0) {
            SessionUtil.flash(req, "danger", "Vui lòng chọn file Excel (.xlsx) cần import.");
            resp.sendRedirect(ctx + "/admin/accounts");
            return;
        }

        String fileName = part.getSubmittedFileName() == null ? "" : part.getSubmittedFileName().toLowerCase();
        if (!fileName.endsWith(".xlsx")) {
            SessionUtil.flash(req, "danger", "Chỉ hỗ trợ file Excel định dạng .xlsx. Hãy tải biểu mẫu bằng nút \"Tải biểu mẫu\".");
            resp.sendRedirect(ctx + "/admin/accounts");
            return;
        }

        List<AccountExcelService.ImportRow> rows;
        try (InputStream in = part.getInputStream()) {
            rows = excelService.readImport(in);
        } catch (Exception e) {
            SessionUtil.flash(req, "danger", "Không đọc được file Excel. Hãy dùng đúng biểu mẫu tải từ nút \"Tải biểu mẫu\".");
            resp.sendRedirect(ctx + "/admin/accounts");
            return;
        }

        if (rows.isEmpty()) {
            SessionUtil.flash(req, "danger", "File không có dòng dữ liệu nào.");
            resp.sendRedirect(ctx + "/admin/accounts");
            return;
        }

        List<RoleOption> roles = dao.listRoles();
        int created = 0, mailed = 0;
        List<String> errors = new ArrayList<>();
        String lastMailError = null;

        for (AccountExcelService.ImportRow r : rows) {
            String username = Validator.normalize(r.username);
            String email = Validator.normalize(r.email);
            String fullName = Validator.normalize(r.fullName);
            String phone = Validator.normalize(r.phone);
            String govId = Validator.normalize(r.govId);
            String sex = Validator.normalize(r.sex);
            String address = Validator.normalize(r.address);

            int roleId = resolveRoleId(roles, r.roleName);
            java.sql.Date dob = null;
            try {
                String norm = AccountExcelServiceImpl.normalizeDate(r.dateOfBirth);
                if (!norm.isEmpty()) dob = java.sql.Date.valueOf(norm);
            } catch (Exception ignore) {}

            String err = validateAccountInput(username, email, fullName, phone, sex, govId, dob, roleId);
            if (err != null) {
                errors.add("Dòng " + r.rowNumber + ": " + err);
                continue;
            }

            AccountView a = new AccountView();
            a.setUsername(username); a.setEmail(email);
            a.setActive(!isLockedStatus(r.status));
            a.setFullName(fullName); a.setPhone(phone); a.setGovId(govId);
            a.setAddress(address.isEmpty() ? null : address);
            a.setDateOfBirth(dob);

            String tempPw = PasswordGenerator.generateNumeric(TEMP_PW_LENGTH);
            int newId = dao.create(a, roleId, "Nam".equals(sex), tempPw);
            if (newId > 0) {
                created++;
                AdminAuditLog.persist(req.getSession(), "INSERT", "Import tài khoản: " + username, newId);
                String mailError = sendCredentialsEmailAndGetError(email, username, tempPw, false);
                if (mailError == null) mailed++;
                else if (lastMailError == null) lastMailError = mailError;
            } else {
                errors.add("Dòng " + r.rowNumber + ": tạo tài khoản thất bại (dữ liệu trùng).");
            }
        }

        StringBuilder msg = new StringBuilder();
        msg.append("Import xong: đã tạo ").append(created).append('/').append(rows.size()).append(" tài khoản. ");
        if (created > 0) {
            if (mailed == created) {
                msg.append("Đã gửi mật khẩu tạm qua email cho tất cả ").append(mailed).append(" tài khoản.");
            } else if (mailed == 0) {
                msg.append("CHƯA gửi được email mật khẩu cho tài khoản nào — người dùng chưa thể đăng nhập. Lý do: ")
                   .append(lastMailError).append(" Hãy sửa cấu hình rồi dùng nút \"Cấp lại MK\" cho từng tài khoản.");
            } else {
                msg.append("Gửi email mật khẩu thành công ").append(mailed).append('/').append(created)
                   .append(" tài khoản. Lý do các tài khoản còn lại chưa nhận được: ").append(lastMailError)
                   .append(" Hãy dùng nút \"Cấp lại MK\" để gửi lại.");
            }
        }
        if (!errors.isEmpty()) {
            msg.append(" Bỏ qua ").append(errors.size()).append(" dòng lỗi: ");
            int show = Math.min(errors.size(), 5);
            for (int i = 0; i < show; i++) {
                if (i > 0) msg.append(" | ");
                msg.append(errors.get(i));
            }
            if (errors.size() > show) msg.append(" | ...");
        }

        // Chỉ báo "thành công" khi vừa tạo được vừa gửi email đủ và không có dòng lỗi
        String type;
        if (created == 0) type = "danger";
        else if (!errors.isEmpty() || mailed < created) type = "warning";
        else type = "success";
        SessionUtil.flash(req, type, msg.toString());
        resp.sendRedirect(ctx + "/admin/accounts");
    }

    private boolean isLockedStatus(String status) {
        String s = Validator.normalize(status).toLowerCase();
        return s.startsWith("khóa") || s.startsWith("khoa") || s.equals("inactive") || s.startsWith("vô hiệu");
    }

    /** Khớp tên vai trò trong file với Role trong CSDL (không phân biệt hoa thường/khoảng trắng). */
    private int resolveRoleId(List<RoleOption> roles, String roleName) {
        String want = Validator.normalize(roleName);
        if (want.isEmpty() || roles == null) return 0;
        for (RoleOption r : roles) {
            if (r.getRoleName() != null && r.getRoleName().trim().equalsIgnoreCase(want)) return r.getRoleId();
        }
        return 0;
    }

    // ------------------------------------------------------- bulk delete

    /**
     * Xóa hàng loạt tài khoản: theo các dòng được tick chọn (ids),
     * hoặc theo toàn bộ kết quả của bộ lọc hiện tại (scope=filtered).
     * Tài khoản Quản trị viên luôn bị bỏ qua và báo lại lý do.
     */
    private void handleBulkDelete(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String ctx = req.getContextPath();
        List<AccountView> targets = new ArrayList<>();

        if ("filtered".equals(Sanitize.text(req.getParameter("scope")))) {
            String keyword = Sanitize.text(req.getParameter("searchKeyword"));
            Integer roleId = Sanitize.toIntegerOrNull(req.getParameter("filterRole"));
            String statusFilter = Sanitize.text(req.getParameter("filterStatus"));
            Boolean active = null;
            if ("active".equals(statusFilter)) active = true;
            else if ("inactive".equals(statusFilter) || "locked".equals(statusFilter)) active = false;
            targets = dao.search(keyword, roleId, active);
        } else {
            String[] ids = req.getParameterValues("ids");
            if (ids != null) {
                for (String raw : ids) {
                    int id = Sanitize.toInt(raw, 0);
                    AccountView a = (id > 0) ? dao.findById(id) : null;
                    if (a != null) targets.add(a);
                }
            }
        }

        if (targets.isEmpty()) {
            SessionUtil.flash(req, "danger", "Chưa chọn tài khoản nào để xóa.");
            resp.sendRedirect(ctx + "/admin/accounts");
            return;
        }

        BulkResult result = new BulkResult();
        for (AccountView a : targets) {
            String label = a.getUsername() == null ? ("#" + a.getUserId()) : a.getUsername();
            String denied = denyIfAdminTarget(req, a, "xóa");
            if (denied != null) {
                result.skip(label + " (" + denied + ")");
                continue;
            }
            if (dao.delete(a.getUserId())) {
                result.success();
                AdminAuditLog.persist(req.getSession(), "DELETE",
                        "Xóa hàng loạt tài khoản: " + label, a.getUserId());
            } else {
                result.skip(label + " (đang được tham chiếu, hãy dùng \"Khóa\")");
            }
        }

        SessionUtil.flash(req, result.flashType(), result.message("đã xóa", "tài khoản", targets.size()));
        resp.sendRedirect(ctx + "/admin/accounts");
    }

    // ------------------------------------------------------------- validation

    /** Validate dùng chung cho tạo thủ công và import. Trả về null nếu hợp lệ. */
    private String validateAccountInput(String username, String email, String fullName, String phone,
                                        String sex, String govId, java.sql.Date dob, int roleId) {
        String error = Validator.username(username);
        if (error == null) error = Validator.email(email);
        if (error == null) error = Validator.fullName(fullName);
        if (error == null) error = Validator.phone(phone);
        if (error == null && !"Nam".equals(sex) && !"Nữ".equals(sex)) error = "Vui lòng chọn giới tính (Nam hoặc Nữ).";
        if (error == null) error = Validator.govId(govId);
        if (error == null) error = Validator.dateOfBirth(dob);
        if (error == null && roleId <= 0) error = "Vai trò không hợp lệ hoặc không tồn tại.";
        if (error == null && dao.usernameExists(username)) error = "Tên đăng nhập \"" + username + "\" đã tồn tại.";
        if (error == null && dao.emailExists(email))       error = "Email \"" + email + "\" đã được sử dụng.";
        if (error == null && dao.phoneExists(phone))       error = "Số điện thoại \"" + phone + "\" đã được sử dụng.";
        if (error == null && dao.govIdExists(govId))       error = "Số CCCD \"" + govId + "\" đã tồn tại.";
        return error;
    }

    /**
     * Lưu lại dữ liệu vừa nhập + thông báo lỗi vào session để mở lại đúng modal
     * "Tạo tài khoản mới" kèm lỗi hiển thị tại chỗ, thay vì chỉ hiện banner đầu trang và mất dữ liệu đã nhập.
     */
    private void reopenAccountModal(HttpServletRequest req, String errorMessage, String username, String email,
                                     int roleId, String fullName, String phone, String dobStr, String sex,
                                     String govId, String address, boolean active) {
        SessionUtil.flash(req, "danger", errorMessage);
        var s = req.getSession();
        s.setAttribute("reopenModal", "account");
        s.setAttribute("f_username", username);
        s.setAttribute("f_email", email);
        s.setAttribute("f_role", roleId > 0 ? String.valueOf(roleId) : "");
        s.setAttribute("f_fullName", fullName);
        s.setAttribute("f_phone", phone);
        s.setAttribute("f_dateOfBirth", dobStr);
        s.setAttribute("f_sex", sex);
        s.setAttribute("f_govId", govId);
        s.setAttribute("f_address", address);
        s.setAttribute("f_status", active ? "active" : "inactive");
    }

    /**
     * Chặn thao tác khóa/xóa nhắm vào tài khoản Quản trị viên (kể cả chính mình).
     * @return null nếu được phép, ngược lại là câu thông báo từ chối.
     */
    private String denyIfAdminTarget(HttpServletRequest req, AccountView target, String actionLabel) {
        if (target == null) return "Không tìm thấy tài khoản.";

        UserDTO current = SessionUtil.getCurrentUser(req);
        if (current != null && current.getUserId() == target.getUserId()) {
            return "Bạn không thể " + actionLabel + " tài khoản của chính mình.";
        }
        if (RoleUi.ADMIN.equals(target.getRoleCode())) {
            return "Không thể " + actionLabel + " tài khoản Quản trị viên khác.";
        }
        return null;
    }

    // ------------------------------------------------------------- helpers

    /**
     * Gửi email mật khẩu tạm cho người dùng.
     * Dùng chung cho luồng tạo tài khoản mới, import và cấp lại mật khẩu.
     * @return null nếu gửi thành công; mô tả lý do cụ thể nếu thất bại (hiện thẳng cho Admin xem).
     */
    private String sendCredentialsEmailAndGetError(String to, String username, String tempPw, boolean isReset) {
        if (to == null || to.isBlank()) return "Địa chỉ email của tài khoản đang trống.";
        String subject = isReset
                ? "[Lái Vui] Mật khẩu của bạn đã được cấp lại"
                : "[Lái Vui] Tài khoản của bạn đã được tạo";
        String opening = isReset
                ? "Mật khẩu tài khoản của bạn trên hệ thống Lái Vui vừa được cấp lại."
                : "Tài khoản của bạn trên hệ thống Lái Vui đã được khởi tạo.";
        String content = "Xin chào,\n\n"
                + opening + "\n\n"
                + "Tên đăng nhập: " + username + "\n"
                + "Mật khẩu tạm: " + tempPw + "\n\n"
                + "Vì lý do bảo mật, bạn BẮT BUỘC phải đổi mật khẩu ngay trong lần đăng nhập đầu tiên.\n\n"
                + "Trân trọng,\nBan quản trị Lái Vui";
        try {
            return emailService.sendTextEmailAndGetError(to, subject, content);
        } catch (Exception e) {
            return "Lỗi không xác định: " + e.getMessage();
        }
    }
}

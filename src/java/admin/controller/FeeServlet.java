package admin.controller;

import admin.dao.FeeManageDAO;
import admin.dao.LicenceDAO;
import admin.dao.impl.FeeManageDAOImpl;
import admin.dao.impl.LicenceDAOImpl;
import admin.dto.FeeView;
import admin.model.User;
import admin.util.AuditLogHelper;
import admin.util.Sanitize;
import admin.util.SessionUtil;
import admin.util.Validator;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.math.BigDecimal;

/**
 * Admin exam-fee management (Lệ phí thi). Fees classified by Hạng GPLX + Phân
 * loại. GET /admin/exam-fee -> list (filters: searchKeyword, filterClass,
 * filterCategory, filterStatus) POST /admin/exam-fee?action=save -> create or
 * update POST /admin/exam-fee?action=delete -> delete
 */
@WebServlet(name = "FeeServlet", urlPatterns = {"/admin/exam-fee"})
public class FeeServlet extends HttpServlet {

    private final FeeManageDAO dao = new FeeManageDAOImpl();
    private final LicenceDAO licenceDAO = new LicenceDAOImpl();
    private static final String LIST_VIEW = "/views/admin/exam-fee.jsp";

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        if (!SessionUtil.requireAdmin(req, resp)) {
            return;
        }

        // Đảm bảo nhận từ khóa tìm kiếm tiếng Việt không bị lỗi font
        req.setCharacterEncoding("UTF-8");

        String keyword = Sanitize.text(req.getParameter("searchKeyword"));
        Integer licenceId = Sanitize.toIntegerOrNull(req.getParameter("filterClass"));
        String category = Sanitize.text(req.getParameter("filterCategory"));
        String statusFilter = Sanitize.text(req.getParameter("filterStatus"));
        Boolean active = null;
        if ("active".equals(statusFilter)) {
            active = true;
        } else if ("inactive".equals(statusFilter)) {
            active = false;
        }

        req.setAttribute("examFees", dao.search(keyword, licenceId, category, active));
        req.setAttribute("licenceClassesList", licenceDAO.findAll());
        req.setAttribute("totalFees", dao.countAll());
        req.setAttribute("theoryFees", dao.countByType("theory"));
        req.setAttribute("practicalFees", dao.countByType("practical") + dao.countByType("rent"));
        req.setAttribute("certFees", dao.countByType("license"));
        req.getRequestDispatcher(LIST_VIEW).forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        if (!SessionUtil.requireAdmin(req, resp)) {
            return;
        }

        // Đảm bảo nhận dữ liệu biểu phí bằng tiếng Việt không bị lỗi font
        req.setCharacterEncoding("UTF-8");

        String action = Sanitize.text(req.getParameter("action"));
        User admin = SessionUtil.getCurrentUser(req);
        Integer actorId = (admin != null) ? admin.getId() : null;
        String ctx = req.getContextPath();

        if ("delete".equals(action)) {
            int id = Sanitize.toInt(req.getParameter("id"), 0);
            FeeView fee = dao.findById(id);
            boolean ok = id > 0 && dao.delete(id);
            if (ok) {
                AuditLogHelper.persist(req.getSession(), "DELETE",
                        "Xóa biểu phí: " + (fee != null ? fee.getFeeName() : id), id);
                SessionUtil.flash(req, "success", "Đã xóa biểu phí.");
            } else {
                SessionUtil.flash(req, "danger", "Xóa biểu phí thất bại.");
            }
            resp.sendRedirect(ctx + "/admin/exam-fee");
            return;
        }

        int id = Sanitize.toInt(req.getParameter("feeId"), 0);
        boolean isEdit = id > 0;
        String name = Sanitize.text(req.getParameter("feeName"));
        String category = Sanitize.text(req.getParameter("feeType"));
        String amountStr = Sanitize.text(req.getParameter("amount"));
        Integer licenceId = Sanitize.toIntegerOrNull(req.getParameter("licenceId"));
        boolean active = !"inactive".equals(Sanitize.text(req.getParameter("status")));

        BigDecimal amount = null;
        try {
            if (!amountStr.isEmpty()) {
                amount = new BigDecimal(amountStr.replace(",", "").trim());
            }
        } catch (Exception ignore) {
        }

        String error = Validator.name("Tên biểu phí", name, 3, 100);
        if (error == null) {
            error = Validator.amount(amount);
        }
        if (name.isEmpty()) {
            error = "Vui lòng nhập tên biểu phí.";
        } else if (category.isEmpty()) {
            error = "Vui lòng chọn phân loại phí.";
        } else if (amount == null || amount.signum() < 0) {
            error = "Mức thu phải là số tiền hợp lệ (≥ 0).";
        }

        if (error != null) {
            SessionUtil.flash(req, "danger", error);
            resp.sendRedirect(ctx + "/admin/exam-fee");
            return;
        }

        FeeView fee = new FeeView();
        fee.setFeeId(id);
        fee.setFeeName(name);
        fee.setFeeType(category);
        fee.setAmount(amount);
        fee.setActive(active);
        fee.setLicenceId(licenceId);

        if (isEdit) {
            boolean ok = dao.update(fee, actorId);
            AuditLogHelper.persist(req.getSession(), "UPDATE", "Cập nhật biểu phí: " + name, id);
            SessionUtil.flash(req, ok ? "success" : "danger",
                    ok ? "Đã cập nhật biểu phí \"" + name + "\"." : "Cập nhật biểu phí thất bại.");
        } else {
            int newId = dao.insert(fee, actorId);
            boolean ok = newId > 0;
            AuditLogHelper.persist(req.getSession(), "INSERT", "Tạo biểu phí: " + name, newId);
            SessionUtil.flash(req, ok ? "success" : "danger",
                    ok ? "Đã thêm biểu phí \"" + name + "\"." : "Thêm biểu phí thất bại.");
        }
        resp.sendRedirect(ctx + "/admin/exam-fee");
    }
}

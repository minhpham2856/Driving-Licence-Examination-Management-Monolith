package admin.controller;

import admin.dao.FeeManageDAO;
import admin.dao.LicenceManageDAO;
import admin.dao.impl.FeeManageDAOImpl;
import admin.dao.impl.LicenceManageDAOImpl;
import admin.model.FeeView;
import admin.model.LicenceFeeView;
import admin.util.AdminAuditLog;
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

/** Lệ phí thi: Biểu phí theo hạng (Licence_Fee) + Danh mục phí (Fee). */
@WebServlet(name = "FeeServlet", urlPatterns = {"/admin/exam-fee"})
public class FeeServlet extends HttpServlet {

    private final FeeManageDAO dao = new FeeManageDAOImpl();
    private final LicenceManageDAO licenceDAO = new LicenceManageDAOImpl();
    private static final String LIST_VIEW = "/views/admin/exam-fee.jsp";

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        if (!SessionUtil.requireAdmin(req, resp)) return;
        Integer fLic = Sanitize.toIntegerOrNull(req.getParameter("filterLicence"));
        Integer fFee = Sanitize.toIntegerOrNull(req.getParameter("filterFee"));
        req.setAttribute("licenceFees", dao.listLicenceFees(fLic, fFee));
        req.setAttribute("fees", dao.listFees(null, null));
        req.setAttribute("activeFees", dao.listActiveFees());
        req.setAttribute("licences", licenceDAO.listAll());
        req.setAttribute("totalLicenceFees", dao.countLicenceFees());
        req.setAttribute("totalFees", dao.countFees());
        req.getRequestDispatcher(LIST_VIEW).forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        if (!SessionUtil.requireAdmin(req, resp)) return;
        String action = Sanitize.text(req.getParameter("action"));
        String ctx = req.getContextPath();
        String back = ctx + "/admin/exam-fee";

        switch (action) {
            case "saveFee":     handleSaveFee(req, resp, back); return;
            case "toggleFee":   handleToggleFee(req, resp, back); return;
            case "deleteFee":   handleDeleteFee(req, resp, back); return;
            case "deleteLF":    handleDeleteLF(req, resp, back); return;
            default:            handleSaveLF(req, resp, back); return;
        }
    }

    // ---- Fee catalog ----
    private void handleSaveFee(HttpServletRequest req, HttpServletResponse resp, String back) throws IOException {
        int id = Sanitize.toInt(req.getParameter("feeId"), 0);
        String name = Sanitize.text(req.getParameter("feeName"));
        String type = Sanitize.text(req.getParameter("feeType"));
        boolean active = !"inactive".equals(Sanitize.text(req.getParameter("status")));
        boolean isEdit = id > 0;

        String error = Validator.name("Tên phí", name, 2, 100);
        if (error == null) error = Validator.name("Loại phí", type, 2, 50);
        if (error == null && dao.feeNameExists(name, id)) error = "Tên phí đã tồn tại.";
        if (error != null) {
            SessionUtil.flash(req, "danger", error);
            var s = req.getSession();
            s.setAttribute("reopenModal", "fee");
            s.setAttribute("f_mode", isEdit ? "edit" : "create");
            s.setAttribute("f_feeId", id); s.setAttribute("f_feeName", name);
            s.setAttribute("f_feeType", type); s.setAttribute("f_active", active);
            resp.sendRedirect(back); return;
        }
        FeeView f = new FeeView(); f.setFeeId(id); f.setFeeName(name); f.setFeeType(type); f.setActive(active);
        if (isEdit) {
            boolean ok = dao.updateFee(f);
            AdminAuditLog.persist(req.getSession(), "UPDATE", "Cập nhật loại phí: " + name, id);
            SessionUtil.flash(req, ok ? "success" : "danger", ok ? "Đã cập nhật loại phí." : "Cập nhật thất bại.");
        } else {
            int newId = dao.insertFee(f);
            AdminAuditLog.persist(req.getSession(), "INSERT", "Tạo loại phí: " + name, newId);
            SessionUtil.flash(req, newId > 0 ? "success" : "danger", newId > 0 ? "Đã thêm loại phí." : "Thêm thất bại.");
        }
        resp.sendRedirect(back);
    }

    private void handleToggleFee(HttpServletRequest req, HttpServletResponse resp, String back) throws IOException {
        int id = Sanitize.toInt(req.getParameter("id"), 0);
        boolean toActive = "true".equals(req.getParameter("active"));
        boolean ok = id > 0 && dao.setFeeActive(id, toActive);
        if (ok) AdminAuditLog.persist(req.getSession(), "UPDATE", (toActive ? "Bật" : "Tắt") + " loại phí #" + id, id);
        SessionUtil.flash(req, ok ? "success" : "danger", ok ? "Đã đổi trạng thái loại phí." : "Thao tác thất bại.");
        resp.sendRedirect(back);
    }

    private void handleDeleteFee(HttpServletRequest req, HttpServletResponse resp, String back) throws IOException {
        int id = Sanitize.toInt(req.getParameter("id"), 0);
        boolean ok = id > 0 && dao.deleteFee(id);
        if (ok) { AdminAuditLog.persist(req.getSession(), "DELETE", "Xóa loại phí #" + id, id);
            SessionUtil.flash(req, "success", "Đã xóa loại phí."); }
        else SessionUtil.flash(req, "danger", "Không thể xóa (loại phí đang dùng trong biểu phí/thanh toán). Hãy tắt thay vì xóa.");
        resp.sendRedirect(back);
    }

    // ---- Licence_Fee ----
    private void handleSaveLF(HttpServletRequest req, HttpServletResponse resp, String back) throws IOException {
        int id = Sanitize.toInt(req.getParameter("licenceFeeId"), 0);
        String licStr = Sanitize.text(req.getParameter("licenceId"));
        Integer licenceId = (licStr.isEmpty() || "0".equals(licStr)) ? null : Sanitize.toIntegerOrNull(licStr);
        int feeId = Sanitize.toInt(req.getParameter("feeId"), 0);
        String amountStr = Sanitize.text(req.getParameter("amount"));
        boolean isEdit = id > 0;

        BigDecimal amount = null;
        try { if (!amountStr.isEmpty()) amount = new BigDecimal(amountStr.replace(",", "").replace(".", "")); } catch (Exception ignore) {}

        String error = null;
        if (feeId <= 0) error = "Vui lòng chọn loại phí.";
        if (error == null) error = Validator.amount(amount);
        if (error == null && dao.pairExists(licenceId, feeId, id))
            error = "Cặp (hạng + loại phí) này đã có mức phí. Hãy sửa dòng hiện có.";

        if (error != null) {
            SessionUtil.flash(req, "danger", error);
            var s = req.getSession();
            s.setAttribute("reopenModal", "lf");
            s.setAttribute("f_mode", isEdit ? "edit" : "create");
            s.setAttribute("f_lfId", id);
            s.setAttribute("f_licenceId", licenceId == null ? "" : String.valueOf(licenceId));
            s.setAttribute("f_feeId", feeId);
            s.setAttribute("f_amount", amountStr);
            resp.sendRedirect(back); return;
        }
        if (isEdit) {
            boolean ok = dao.updateLicenceFee(id, licenceId, feeId, amount);
            AdminAuditLog.persist(req.getSession(), "UPDATE", "Cập nhật mức phí #" + id, id);
            SessionUtil.flash(req, ok ? "success" : "danger", ok ? "Đã cập nhật mức phí." : "Cập nhật thất bại.");
        } else {
            int newId = dao.insertLicenceFee(licenceId, feeId, amount);
            AdminAuditLog.persist(req.getSession(), "INSERT", "Thêm mức phí (fee #" + feeId + ")", newId);
            SessionUtil.flash(req, newId > 0 ? "success" : "danger", newId > 0 ? "Đã thêm mức phí." : "Thêm thất bại.");
        }
        resp.sendRedirect(back);
    }

    private void handleDeleteLF(HttpServletRequest req, HttpServletResponse resp, String back) throws IOException {
        int id = Sanitize.toInt(req.getParameter("id"), 0);
        boolean ok = id > 0 && dao.deleteLicenceFee(id);
        if (ok) { AdminAuditLog.persist(req.getSession(), "DELETE", "Xóa mức phí #" + id, id);
            SessionUtil.flash(req, "success", "Đã xóa mức phí."); }
        else SessionUtil.flash(req, "danger", "Xóa thất bại.");
        resp.sendRedirect(back);
    }
}

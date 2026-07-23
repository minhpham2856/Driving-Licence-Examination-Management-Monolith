package policestaff.controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import policestaff.dto.PoliceCandidateDTO;
import policestaff.dto.PoliceSubmissionDTO;
import policestaff.service.PoliceDashboardService;
import policestaff.service.impl.PoliceDashboardServiceImpl;

/** Màn thẩm định hồ sơ; việc lập danh sách chính thức nằm ở servlet riêng. */
@WebServlet("/police/submissions")
public class PoliceSubmissionServlet extends HttpServlet {
    private final PoliceDashboardService service = new PoliceDashboardServiceImpl();
    private static final int PAGE_SIZE = 8;

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        int dateId = integer(req.getParameter("dateId"));
        PoliceSubmissionDTO submission = service.findSubmission(dateId);
        if (submission == null) { resp.sendError(404); return; }
        int total = service.countCandidates(dateId);
        int totalPages = Math.max(1, (total + PAGE_SIZE - 1) / PAGE_SIZE);
        int page = Math.min(Math.max(1, integer(req.getParameter("page"))), totalPages);
        req.setAttribute("submission", submission);
        req.setAttribute("candidates", service.loadCandidates(dateId, page, PAGE_SIZE));
        req.setAttribute("page", page);
        req.setAttribute("totalPages", totalPages);
        req.setAttribute("totalCandidates", total);
        flash(req, "policeSuccess");
        flash(req, "policeError");
        req.getRequestDispatcher("/views/staff/policestaff/submission-detail.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        int dateId = integer(req.getParameter("dateId"));
        int rowId = integer(req.getParameter("registrationDateId"));
        int page = Math.max(1, integer(req.getParameter("page")));
        try {
            String decision = value(req.getParameter("decision"));
            if (!List.of("APPROVED", "REJECTED").contains(decision))
                throw new IllegalArgumentException("Quyết định thẩm định không hợp lệ.");
            if (!service.review(rowId, decision, req.getParameter("reason")))
                throw new IllegalArgumentException("Hồ sơ đã được xử lý hoặc danh sách đã hoàn tất.");
            req.getSession().setAttribute("policeSuccess",
                    "Đã lưu kết quả thẩm định và gửi email thông báo cho thí sinh.");
        } catch (Exception ex) {
            req.getSession().setAttribute("policeError", ex.getMessage());
        }
        resp.sendRedirect(req.getContextPath() + "/police/submissions?dateId="
                + Math.max(dateId, 0) + "&page=" + page
                + (rowId > 0 ? "&candidate=" + rowId : ""));
    }

    private static int integer(String value) {
        try { return Integer.parseInt(value); } catch (Exception ex) { return 0; }
    }
    private static String value(String value) {
        return value == null ? "" : value.trim().toUpperCase();
    }
    private static void flash(HttpServletRequest req, String key) {
        Object value = req.getSession().getAttribute(key);
        if (value != null) {
            req.setAttribute(key, value);
            req.getSession().removeAttribute(key);
        }
    }
}

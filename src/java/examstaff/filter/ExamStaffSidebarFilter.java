package examstaff.filter;

import auth.dto.UserDTO;
import examstaff.controller.ExamStaffHttpSupport;
import examstaff.controller.ExamStaffPageBinder;
import examstaff.controller.ExamStaffPageSupport;
import examstaff.service.ExamStaffViewService;
import examstaff.service.impl.ExamStaffViewServiceImpl;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import shared.Attributes;
import shared.enums.RoleType;
import shared.model.Role;

import java.io.IOException;

/**
 * Servlet filter bảo vệ và chuẩn bị request-scope cho mọi URL {@code /examstaff/*}.
 * Chạy trước mọi servlet/JSP exam staff — auth, role, đồng bộ ca thi, bind sidebar.
 *
 * Vai trò trong luồng examstaff:
 * Đảm bảo chỉ {@code UserDTO} đã đăng nhập với {@link RoleType#EXAM_STAFF} mới vào module.
 * Khi URL mang {@code examId} khác ca đã nạp session → xóa cache thí sinh và apply ca mới.
 * Cuối cùng gọi {@link ExamStaffPageSupport#bindSidebarIfNeeded} để menu/sidebar có dữ liệu kỳ thi.
 *
 * Luồng xử lý {@link #doFilter}:
 * - No-cache headers — tránh sidebar/menu cũ sau đổi ca.
 * - Auth — thiếu session user → redirect {@code /staff/login}.
 * - Role — không phải ExamStaff → HTTP 403.
 * - Exam context — parse {@code examId}, clear cache, {@code applyExamIdFromRequest}.
 * - Sidebar bind — {@code bindSidebarIfNeeded} qua {@link ExamStaffViewService}.
 * - {@link FilterChain#doFilter} — chuyển tiếp servlet/JSP.
 *
 * Phạm vi và ai gọi:
 * Annotation {@code @WebFilter(urlPatterns = "/examstaff/*")} — mọi request exam staff
 * (dashboard, candidate call, allocation, audit, report, …) đi qua filter này trước controller.
 */
@WebFilter(urlPatterns = {"/examstaff/*"})
public class ExamStaffSidebarFilter extends HttpFilter {

    /** Service đọc dữ liệu ca/thí sinh để bind sidebar và apply examId. */
    private final ExamStaffViewService viewService = new ExamStaffViewServiceImpl();

    /**
     * Lọc request exam staff: no-cache → đăng nhập → vai trò → đồng bộ ca → bind sidebar → chain.
     * @param request  HTTP request đi vào {@code /examstaff/*}
     * @param response HTTP response (có thể redirect/403)
     * @param chain    chuỗi filter/servlet tiếp theo
     * @throws IOException      lỗi I/O khi redirect hoặc ghi lỗi
     * @throws ServletException lỗi servlet khi chuyển tiếp chain
     */
    @Override
    protected void doFilter(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        // Bước 1: chống cache trang staff (sidebar/attribute cũ)
        ExamStaffHttpSupport.applyNoCacheHeaders(response);

        // Bước 2 — AUTH: lấy user từ session hiện có (không tạo session mới nếu chưa login)
        HttpSession session = request.getSession(false);
        UserDTO user = session != null ? (UserDTO) session.getAttribute(Attributes.Session.USER) : null;

        // Bước 2b: chưa đăng nhập → tạo session chỉ để gắn thông báo lỗi, rồi redirect login
        if (user == null) {
            HttpSession loginSession = request.getSession(true);
            loginSession.setAttribute(Attributes.Session.ERROR_MESSAGE, "Bạn cần đăng nhập để truy cập.");
            response.sendRedirect(request.getContextPath() + "/staff/login");
            return;
        }

        // Bước 3 — ROLE: chỉ ExamStaff được vào; role null/khác → 403 (không bind sidebar)
        Role role = user.getRole();
        if (role == null || RoleType.fromValue(role.getRoleName()) != RoleType.EXAM_STAFF) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        }

        // Bước 4 — EXAM CONTEXT: đồng bộ examId URL ↔ session (trước khi bind sidebar)
        int urlExamId = ExamStaffHttpSupport.parseExamIdParam(request);
        if (session != null && urlExamId > 0) {
            Integer loadedExam = ExamStaffPageBinder.readLoadedExamId(session);
            // Đổi ca thi → xóa cache danh sách thí sinh của ca cũ
            if (loadedExam == null || loadedExam != urlExamId) {
                ExamStaffPageSupport.clearCandidateCache(session);
            }
            // Nạp/apply examId từ request vào session + attribute trang
            ExamStaffPageSupport.applyExamIdFromRequest(request, session,
                    viewService.listAllExams(), viewService);
        }

        // Bước 5 — SIDEBAR BIND: gắn dữ liệu menu/sidebar request-scope (sau auth + exam sync)
        ExamStaffPageSupport.bindSidebarIfNeeded(request, session, viewService);

        // Bước 6: chuyển tiếp sang servlet/JSP examstaff
        chain.doFilter(request, response);
    }
}

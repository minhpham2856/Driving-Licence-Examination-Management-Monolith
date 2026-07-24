package examstaff.controller;

import examstaff.dto.CandidateDossierViewDTO;
import examstaff.service.ExamStaffViewService;
import examstaff.service.impl.ExamStaffViewServiceImpl;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

/**
 * In / xem hồ sơ thí sinh (dossier): load theo SBD → bind phí/ảnh → forward JSP.
 *
 * Vai trò:
 * Trang in/xem hồ sơ đăng ký thí sinh (profile, phí, cờ ảnh) theo SBD và kỳ đang chọn.
 * Hỗ trợ print=true để JSP tự kích hoạt in.
 *
 * Luồng GET:
 * - Bắt buộc param sbd; thiếu → redirect candidatecall
 * - ensureExamId → loadDossier; không có profile → redirect candidatecall
 * - Bind profile / examSummary / feeLines / autoPrint → forward candidate-dossier.jsp
 *
 * Ai gọi:
 * Link từ ProcedureServlet và CandidateCallServlet sau thu phí/in hồ sơ;
 * mở tab mới từ bàn thủ tục với ?sbd=&print=true.
 */
@WebServlet("/examstaff/candidate-dossier")
public class CandidateDossierServlet extends HttpServlet {

    private final ExamStaffViewService viewService = new ExamStaffViewServiceImpl();

    /**
     * GET: bắt buộc sbd → ensureExamId → loadDossier → bind → candidate-dossier.jsp.
     * Thiếu SBD/hồ sơ → redirect candidatecall.
     * @throws ServletException lỗi forward
     * @throws IOException      lỗi redirect
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String sbd = request.getParameter("sbd");
        if (sbd == null || sbd.trim().isEmpty()) {
            response.sendRedirect("candidatecall");
            return;
        }

        int examId = ExamStaffPageSupport.ensureExamId(request, request.getSession(),
                viewService.listAllExams(), viewService);
        CandidateDossierViewDTO view = viewService.loadDossier(examId, sbd);
        if (view.getProfile() == null) {
            response.sendRedirect("candidatecall");
            return;
        }

        // Bind hồ sơ + phí + cờ in tự động
        boolean autoPrint = "true".equalsIgnoreCase(request.getParameter("print"));
        if (view.getProfile() != null) {
            request.setAttribute("profile", view.getProfile());
            request.setAttribute("examSummary", view.getExam());
            request.setAttribute("hasPhotoFile", view.isHasPhotoFile());
            request.setAttribute("payment", null);
            if (view.getFees() != null) {
                request.setAttribute("feeLines", view.getFees().getFeeLines());
                request.setAttribute("feeTotal", view.getFees().getFeeTotal());
                request.setAttribute("feesFromPayment", view.getFees().isFeesFromPayment());
            }
            request.setAttribute("dossierTitle", view.getDossierTitle());
            request.setAttribute("dossierSubtitle", view.getDossierSubtitle());
            request.setAttribute("autoPrint", autoPrint);
        }
        request.getRequestDispatcher("/views/staff/examstaff/candidate-dossier.jsp").forward(request, response);
    }
}

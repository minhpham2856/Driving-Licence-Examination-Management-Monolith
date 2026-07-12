package controller.staff.examstaff;

import dto.AllocateResultDTO;
import dto.ServiceResult;
import dto.ExamViewDTO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import model.ExamArea;
import model.ExamEnrollment;
import service.AllocationService;
import service.impl.AllocationServiceImpl;

import java.io.IOException;
import java.util.List;

@WebServlet("/staff/examstaff/allocation")
public class AllocationServlet extends HttpServlet {

    // Controller talks to the service layer only. No DAO or DB access here.
    private final AllocationService allocationService = new AllocationServiceImpl();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession();
        request.removeAttribute("errorMsg");
        request.removeAttribute("warningMsg");
        request.removeAttribute("alertMsg");

        // 0. Load all sessions for the session dropdown.
        List<ExamViewDTO> allExams = allocationService.getAllExams();
        request.setAttribute("allExams", allExams);

        // 1. Resolve the selected session id (request param wins, then session).
        String sessIdParam = request.getParameter("examId");
        int examId = 2; // Default session, matching the branch default.
        if (sessIdParam != null && !sessIdParam.isEmpty()) {
            try {
                examId = Integer.parseInt(sessIdParam);
            } catch (NumberFormatException e) {
                // Keep the default when the param is not a valid number.
            }
        } else if (session.getAttribute("selectedExamId") != null) {
            examId = (Integer) session.getAttribute("selectedExamId");
        }
        session.setAttribute("selectedExamId", examId);

        // Current session details for the header.
        ExamViewDTO currentExam = null;
        for (ExamViewDTO s : allExams) {
            if (s.getId() == examId) {
                currentExam = s;
                break;
            }
        }
        request.setAttribute("currentExam", currentExam);

        // 2. Load the candidate queue for this session.
        List<ExamEnrollment> candidateQueue = allocationService.getCandidatesByExam(examId);
        request.setAttribute("candidateQueue", candidateQueue);

        // Active theory rooms used by the manual room selector.
        List<ExamArea> activeTheoryRooms = allocationService.getActiveTheoryRooms();
        request.setAttribute("activeTheoryRooms", activeTheoryRooms);

        // 3. Handle pipeline action triggers.
        String action = request.getParameter("action");
        String idStr = request.getParameter("id");

        if (action != null) {
            try {
                if ("autoAllocate".equals(action)) {
                    ServiceResult<AllocateResultDTO> result =
                            allocationService.autoAllocateExam(examId);
                    if (result.isSuccess()) {
                        AllocateResultDTO data = result.getData();
                        int allocated = (data != null) ? data.getAllocatedCount() : 0;
                        if (allocated > 0) {
                            request.setAttribute("alertMsg",
                                    "Tự động phân bổ thành công " + allocated
                                            + " thí sinh vào phòng thi lý thuyết!");
                        } else if (data != null && data.getWarningMessage() != null) {
                            request.setAttribute("warningMsg", data.getWarningMessage());
                        } else {
                            request.setAttribute("warningMsg",
                                    "Không có thí sinh nào cần phân phòng!");
                        }
                    } else {
                        request.setAttribute("errorMsg", result.getMessage());
                    }
                    // Reload the queue to reflect the latest state.
                    candidateQueue = allocationService.getCandidatesByExam(examId);
                    request.setAttribute("candidateQueue", candidateQueue);

                } else if ("checkin".equals(action) && idStr != null) {
                    int candidateId = Integer.parseInt(idStr);
                    ServiceResult<Boolean> result = allocationService.checkInCandidate(candidateId);
                    if (result.isSuccess()) {
                        request.setAttribute("alertMsg",
                                "Đã điểm danh thí sinh (SBD " + candidateId + ").");
                    } else {
                        request.setAttribute("errorMsg", result.getMessage());
                    }

                } else if (idStr != null
                        && ("allocateRoom".equals(action) || "submitTheoryScore".equals(action)
                            || "submitPracticalScore".equals(action) || "submitRoadScore".equals(action)
                            || "callCandidate".equals(action) || "quickComplete".equals(action))) {
                    // These actions depend on candidate fields that do not exist in
                    // main's schema (allocated room, theory/practical/road scores,
                    // photo capture, payment, call board). Reported as unsupported.
                    request.setAttribute("warningMsg",
                            "Tính năng này chưa được hỗ trợ trong phiên bản này "
                                    + "(dữ liệu tương ứng chưa có trong hệ thống chính).");
                }
            } catch (NumberFormatException e) {
                request.setAttribute("errorMsg", "Tham số không hợp lệ: " + e.getMessage());
            }
        }

        request.getRequestDispatcher("/views/staff/examstaff/allocation.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        doGet(request, response);
    }
}

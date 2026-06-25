package Services.Impl;

import DAO.ExamRegistrationDAO;
import DAO.ProfileDAO;
import DAO.RegistrantDAO;
import DAO.Impl.ExamRegistrationDAOImpl;
import DAO.Impl.ProfileDAOImpl;
import DAO.Impl.RegistrantDAOImpl;
import Models.Profile;
import Models.RegistrantMyExamRow;
import Models.User;
import Services.RegistrantMyExamsService;
import Utils.RegistrantAuditHelper;
import Utils.RegistrantFilterSupport;
import Utils.RegistrantFilterSupport.ExamListFilterState;
import Utils.RegistrantListFilter;
import Utils.RegistrantProfileSupport;
import Controllers.Registrant.RegistrantServletSupport;
import jakarta.servlet.http.HttpServletRequest;
import java.util.List;

public class RegistrantMyExamsServiceImpl implements RegistrantMyExamsService {

    public static final String FLASH_CANCEL_ERROR_ATTR = "myExamsCancelError";
    public static final String FLASH_CANCEL_SUCCESS_ATTR = "myExamsCancelSuccess";

    private final RegistrantDAO registrantDAO = new RegistrantDAOImpl();
    private final ProfileDAO profileDAO = new ProfileDAOImpl();
    private final ExamRegistrationDAO examRegistrationDAO = new ExamRegistrationDAOImpl();

    @Override
    public List<RegistrantMyExamRow> listExams(User user) {
        return registrantDAO.listMyExamsByUserId(user.getId());
    }

    @Override
    public void copyMyExamsToRequest(User user, HttpServletRequest request, String selectedExamId) {
        RegistrantServletSupport.consumeFlash(request, FLASH_CANCEL_ERROR_ATTR, "errorMessage");
        RegistrantServletSupport.consumeFlash(request, FLASH_CANCEL_SUCCESS_ATTR, "successMessage");

        List<RegistrantMyExamRow> allExams = listExams(user);
        ExamListFilterState filterState = RegistrantFilterSupport.parseMyExamFilter(request, allExams);
        List<RegistrantMyExamRow> exams = RegistrantListFilter.filterMyExams(
                allExams, filterState.getSearchQuery(), filterState.getStatusFilter(), filterState.getLicenceFilter());

        request.setAttribute("myExamList", exams);
        RegistrantFilterSupport.applyExamListFilter(request, filterState);
        request.setAttribute("totalExamCount", allExams.size());
        request.setAttribute("passedExamCount", countPassed(allExams));
        request.setAttribute("upcomingExamCount", countUpcoming(allExams));
        request.setAttribute("filteredExamCount", exams.size());

        RegistrantMyExamRow selected = resolveSelectedExam(exams, selectedExamId);
        if (selected == null && selectedExamId != null) {
            selected = resolveSelectedExam(allExams, selectedExamId);
        }
        if (selected != null) {
            request.setAttribute("selectedExamId", String.valueOf(selected.getCandidateId()));
        }
        request.setAttribute("selectedExam", selected);
        request.setAttribute("showExamDetails", selected != null);
    }

    @Override
    public RegistrantMyExamRow findSelectedExam(User user, String selectedExamId) {
        return resolveSelectedExam(listExams(user), selectedExamId);
    }

    @Override
    public String requestCancellation(User user, HttpServletRequest request) {
        int candidateId = RegistrantServletSupport.parsePositiveInt(request.getParameter("candidateId"));
        if (candidateId <= 0) {
            return "Không xác định được đăng ký cần hủy.";
        }

        Profile profile = RegistrantProfileSupport.resolveProfile(profileDAO, user);
        if (profile == null) {
            return "Không tìm thấy hồ sơ cá nhân.";
        }

        RegistrantMyExamRow exam = registrantDAO.findMyExamByCandidateId(user.getId(), candidateId);
        if (exam == null) {
            return "Không tìm thấy đăng ký thi hoặc bạn không có quyền thao tác.";
        }
        if (!exam.isCanRequestCancellation()) {
            return "Chỉ có thể gửi yêu cầu hủy khi đăng ký đang chờ xét duyệt và chưa được cấp SBD chính thức.";
        }

        String reason = request.getParameter("cancelReason");
        if (!examRegistrationDAO.requestExamCancellation(candidateId, profile.getId(), reason)) {
            return "Không thể gửi yêu cầu hủy. Vui lòng thử lại sau.";
        }

        String examLabel = exam.getExamTitle()
                + (exam.getExamSectionName() != null ? " — " + exam.getExamSectionName() : "");
        RegistrantAuditHelper.logExamCancellationRequest(
                request.getSession(), profile.getId(), examLabel, reason);
        return null;
    }

    public static String buildMyExamsRedirect(HttpServletRequest request) {
        StringBuilder url = new StringBuilder(request.getContextPath()).append("/registrant/my-exams?");
        RegistrantServletSupport.appendQueryParam(url, "examId", request.getParameter("candidateId"));
        RegistrantServletSupport.appendQueryParam(url, "q", request.getParameter("q"));
        RegistrantServletSupport.appendQueryParam(url, "status", request.getParameter("status"));
        RegistrantServletSupport.appendQueryParam(url, "licence", request.getParameter("licence"));
        RegistrantServletSupport.trimTrailingAmpersand(url);
        if (url.charAt(url.length() - 1) == '?') {
            url.append("cancel=1");
        }
        url.append("#exam-details");
        return url.toString();
    }

    private static long countPassed(List<RegistrantMyExamRow> exams) {
        return exams.stream().filter(e -> "approved".equals(e.getStatusClass())).count();
    }

    private static long countUpcoming(List<RegistrantMyExamRow> exams) {
        return exams.stream()
                .filter(e -> RegistrantFilterSupport.matchesMyExamStatus(e, "approved_waiting"))
                .count();
    }

    private RegistrantMyExamRow resolveSelectedExam(List<RegistrantMyExamRow> exams, String selectedExamId) {
        int candidateId = RegistrantServletSupport.parsePositiveInt(selectedExamId);
        if (candidateId <= 0 || exams.isEmpty()) {
            return null;
        }
        for (RegistrantMyExamRow exam : exams) {
            if (exam.getCandidateId() == candidateId) {
                return exam;
            }
        }
        return null;
    }
}

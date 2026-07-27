package registrant.service.impl;

import registrant.dao.RegistrantDAO;
import registrant.dao.impl.RegistrantDAOImpl;
import registrant.dto.RegistrantMyExamRow;
import auth.dto.UserDTO;
import registrant.service.RegistrantMyExamsService;
import registrant.util.RegistrantExamSupport;
import registrant.util.RegistrantFilterSupport;
import registrant.util.RegistrantFilterSupport.ExamListFilterState;
import registrant.util.RegistrantListFilter;
import registrant.controller.RegistrantServletSupport;
import jakarta.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * Triển khai RegistrantMyExamsService — trang my-exams.jsp.
 * Liệt kê ca thi qua RegistrantDAO.listMyExamsByUserId: nguyện vọng RegistrationDates và ca chính thức khi đã có Candidate/SBD; gắn bộ lọc tìm kiếm/trạng thái/hạng lên request.
 */
public class RegistrantMyExamsServiceImpl implements RegistrantMyExamsService {

    private final RegistrantDAO registrantdao = new RegistrantDAOImpl();

    /** Tải toàn bộ ca thi (nguyện vọng + chính thức) của user. */
    @Override
    public List<RegistrantMyExamRow> listExams(UserDTO user) {
        return registrantdao.listMyExamsByUserId(user.getUserId());
    }

    /** Gắn danh sách ca thi, bộ lọc và ca đang chọn vào request để render JSP. */
    @Override
    public void copyMyExamsToRequest(UserDTO user, HttpServletRequest request, String selectedExamId) {
        List<RegistrantMyExamRow> allExams = listExams(user);
        List<String> allLicenceValues = RegistrantFilterSupport.collectLicenceCodesFromCatalogue(
                registrantdao.listOpenLicenceOptions());
        ExamListFilterState filterState = RegistrantFilterSupport.parseMyExamFilter(
                request, allExams, allLicenceValues);
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

    /** Đếm số ca có trạng thái Đạt. */
    private static long countPassed(List<RegistrantMyExamRow> exams) {
        return exams.stream().filter(e -> "approved".equals(e.getStatusClass())).count();
    }

    /** Đếm số ca sắp tới / nguyện vọng active / chờ ngày thi (loại nguyện vọng đã hủy). */
    private static long countUpcoming(List<RegistrantMyExamRow> exams) {
        return exams.stream()
                .filter(e -> RegistrantExamSupport.isActivePreferredMyExam(e)
                        || RegistrantFilterSupport.matchesMyExamStatus(e, "approved_waiting")
                        || RegistrantFilterSupport.matchesMyExamStatus(e, "pending"))
                .filter(e -> e.getExamDate() != null)
                .count();
    }

    /** Tìm ca trong danh sách theo candidateId (hoặc RegistrationDateId âm). */
    private RegistrantMyExamRow resolveSelectedExam(List<RegistrantMyExamRow> exams, String selectedExamId) {
        int candidateId = RegistrantServletSupport.parsePositiveInt(selectedExamId);
        if (candidateId == 0 || exams.isEmpty()) {
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

package Services.Impl;

import DAO.ExamRegistrationDAO;
import DAO.Impl.ExamRegistrationDAOImpl;
import Models.MyExamRowView;
import Models.User;
import Services.RegistrantMyExamsService;
import jakarta.servlet.http.HttpServletRequest;
import java.util.List;

/** Danh sách lịch thi (filter/pagination) và chi tiết kỳ thi tách riêng. */
public class RegistrantMyExamsServiceImpl implements RegistrantMyExamsService {

    private final ExamRegistrationDAO examRegistrationDAO = new ExamRegistrationDAOImpl();

    @Override
    public void populateExamList(HttpServletRequest request, User user) {
        Integer personId = user.getPersonId();
        if (personId == null) {
            setEmptyList(request);
            return;
        }

        List<MyExamRowView> allRows = examRegistrationDAO.findExamRowsByPersonId(personId);
        request.setAttribute("totalExamsCount",
                RegistrantExamSupport.formatCount(RegistrantExamSupport.countActive(allRows)));
        request.setAttribute("upcomingExamsCount",
                RegistrantExamSupport.formatCount(RegistrantExamSupport.countUpcoming(allRows)));
        request.setAttribute("passedExamsCount", RegistrantExamSupport.formatCount(
                examRegistrationDAO.countPassedRegistrationsByPersonId(personId)));

        String statusFilter = RegistrantExamSupport.normalizeFilter(request.getParameter("status"));
        String query = trim(request.getParameter("q"));
        int page = RegistrantExamSupport.parsePage(request.getParameter("page"));

        request.setAttribute("filterStatus", statusFilter);
        request.setAttribute("filterQuery", query != null ? query : "");

        List<MyExamRowView> filtered = RegistrantExamSupport.filterRows(allRows, statusFilter, query);
        var examPage = RegistrantExamSupport.paginate(filtered, page, RegistrantExamSupport.DEFAULT_PAGE_SIZE);

        request.setAttribute("examRows", examPage.getItems());
        request.setAttribute("examListPage", examPage);
    }

    @Override
    public void populateExamDetail(HttpServletRequest request, User user) {
        Integer personId = user.getPersonId();
        if (personId == null) {
            request.setAttribute("detailError", "Vui lòng hoàn thiện hồ sơ cá nhân trước.");
            return;
        }

        String examIdRaw = request.getParameter("examId");
        if (examIdRaw == null || examIdRaw.isBlank()) {
            request.setAttribute("detailError", "Thiếu mã đăng ký. Vui lòng chọn kỳ thi từ danh sách.");
            return;
        }

        try {
            int registrationId = Integer.parseInt(examIdRaw.trim());
            var detailOpt = examRegistrationDAO.findExamDetailByRegistrationId(personId, registrationId);
            if (detailOpt.isPresent()) {
                request.setAttribute("examDetail", detailOpt.get());
                request.setAttribute("selectedExamId", examIdRaw);
                return;
            }
            request.setAttribute("detailError",
                    "Không tìm thấy đăng ký thi hoặc bạn không có quyền xem chi tiết này.");
        } catch (NumberFormatException ex) {
            request.setAttribute("detailError", "Mã đăng ký không hợp lệ.");
        }
    }

    private void setEmptyList(HttpServletRequest request) {
        request.setAttribute("examRows", List.of());
        request.setAttribute("totalExamsCount", "00");
        request.setAttribute("upcomingExamsCount", "00");
        request.setAttribute("passedExamsCount", "00");
        request.setAttribute("filterStatus", RegistrantExamSupport.FILTER_ALL);
        request.setAttribute("filterQuery", "");
        request.setAttribute("examListPage", RegistrantExamSupport.paginate(List.of(), 1, RegistrantExamSupport.DEFAULT_PAGE_SIZE));
    }

    private String trim(String value) {
        return value != null ? value.trim() : null;
    }
}

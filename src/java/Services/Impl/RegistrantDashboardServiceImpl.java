package Services.Impl;

import DAO.ExamRegistrationDAO;
import DAO.Impl.ExamRegistrationDAOImpl;
import DAO.PaymentDAO;
import DAO.Impl.PaymentDAOImpl;
import Models.DashboardActivity;
import Models.MyExamRowView;
import Models.Person;
import Models.User;
import Services.RegistrantDashboardService;
import jakarta.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.sql.Time;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

/** Dashboard thí sinh — dùng chung nguồn lịch thi với {@link RegistrantMyExamsServiceImpl}. */
public class RegistrantDashboardServiceImpl implements RegistrantDashboardService {

    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm");
    private static final DateTimeFormatter DISPLAY_FORMAT =
            DateTimeFormatter.ofPattern("dd/MM/yyyy, HH:mm", Locale.forLanguageTag("vi-VN"));

    private final ExamRegistrationDAO examRegistrationDAO = new ExamRegistrationDAOImpl();
    private final PaymentDAO paymentDAO = new PaymentDAOImpl();

    @Override
    public void populateDashboard(HttpServletRequest request, User user) {
        String registrantName = resolveDisplayName(user);
        request.setAttribute("registrantName", registrantName);

        Integer personId = user.getPersonId();
        if (personId == null) {
            setEmptyDashboard(request);
            return;
        }

        Person person = user.getPerson();
        String approvalStatus = person != null ? person.getApprovalStatus() : "Pending";
        applyProfileStatus(request, approvalStatus, examRegistrationDAO.countDocumentsByPersonId(personId));

        List<MyExamRowView> examRows = examRegistrationDAO.findExamRowsByPersonId(personId);
        int registeredExams = RegistrantExamSupport.countActive(examRows);
        int upcomingExams = RegistrantExamSupport.countUpcoming(examRows);
        int examResults = examRegistrationDAO.countResultsByPersonId(personId);
        BigDecimal totalFee = paymentDAO.sumCompletedByPersonId(personId);

        request.setAttribute("registeredExams", registeredExams);
        request.setAttribute("upcomingExams", upcomingExams);
        request.setAttribute("examResults", examResults);
        request.setAttribute("totalFee", totalFee);

        applyExamStatsBadges(request, registeredExams, upcomingExams, examResults);

        String statusFilter = RegistrantExamSupport.normalizeFilter(request.getParameter("status"));
        String query = request.getParameter("q");
        int page = RegistrantExamSupport.parsePage(request.getParameter("page"));
        request.setAttribute("filterStatus", statusFilter);
        request.setAttribute("filterQuery", query != null ? query.trim() : "");

        List<MyExamRowView> filtered = RegistrantExamSupport.filterRows(examRows, statusFilter, query);
        var examPage = RegistrantExamSupport.paginate(filtered, page, RegistrantExamSupport.DEFAULT_PAGE_SIZE);
        request.setAttribute("registeredExamList", examPage.getItems());
        request.setAttribute("examListPage", examPage);

        RegistrantExamSupport.findNextUpcoming(examRows).ifPresent(upcoming -> applyUpcomingExam(request, upcoming));

        List<DashboardActivity> activityList = mergeRecentActivities(personId, 5);
        activityList.forEach(activity -> activity.setTime(formatActivityTime(activity.getOccurredAt())));
        request.setAttribute("activityList", activityList);
    }

    private void applyUpcomingExam(HttpServletRequest request, MyExamRowView upcoming) {
        request.setAttribute("upcomingExamName", upcoming.getTitle());
        request.setAttribute("upcomingExamLabel",
                "Hạng " + upcoming.getLicenceCode() + " — "
                        + RegistrantExamSupport.translateExamType(upcoming.getExamTypeName()));
        request.setAttribute("upcomingExamDate", upcoming.getExamDate());
        request.setAttribute("upcomingExamTime",
                formatShiftTime(upcoming.getShiftStartTime(), upcoming.getShiftEndTime()));
        request.setAttribute("upcomingExamLocation",
                upcoming.getLocation() != null ? upcoming.getLocation() : upcoming.getRoomLabel());
        request.setAttribute("upcomingExamDays", calculateDaysUntil(upcoming.getExamDate()));
        request.setAttribute("upcomingExamId", upcoming.getRegistrationId());
    }

    private List<DashboardActivity> mergeRecentActivities(int personId, int limit) {
        List<DashboardActivity> merged = new ArrayList<>();
        merged.addAll(examRegistrationDAO.findRecentRegistrationActivitiesByPersonId(personId, limit));
        merged.addAll(paymentDAO.findRecentPaymentActivitiesByPersonId(personId, limit));
        merged.sort(Comparator.comparing(
                DashboardActivity::getOccurredAt,
                Comparator.nullsLast(Comparator.reverseOrder())));
        if (merged.size() <= limit) {
            return merged;
        }
        return new ArrayList<>(merged.subList(0, limit));
    }

    private void setEmptyDashboard(HttpServletRequest request) {
        request.setAttribute("profileDocumentCount", 0);
        request.setAttribute("profileStatusBadge", "Chưa có hồ sơ");
        request.setAttribute("profileStatusBadgeClass", "neutral");
        request.setAttribute("registeredExams", 0);
        request.setAttribute("upcomingExams", 0);
        request.setAttribute("examResults", 0);
        request.setAttribute("totalFee", BigDecimal.ZERO);
        request.setAttribute("examStatBadge", "Chưa có");
        request.setAttribute("examStatBadgeClass", "neutral");
        request.setAttribute("resultStatBadge", "Chưa có");
        request.setAttribute("resultStatBadgeClass", "neutral");
        request.setAttribute("filterStatus", RegistrantExamSupport.FILTER_ALL);
        request.setAttribute("filterQuery", "");
        request.setAttribute("registeredExamList", List.of());
        request.setAttribute("examListPage",
                RegistrantExamSupport.paginate(List.of(), 1, RegistrantExamSupport.DEFAULT_PAGE_SIZE));
    }

    private String resolveDisplayName(User user) {
        if (user.getPerson() != null && user.getPerson().getFullName() != null
                && !user.getPerson().getFullName().isBlank()) {
            return user.getPerson().getFullName();
        }
        return user.getUsername();
    }

    private void applyProfileStatus(HttpServletRequest request, String approvalStatus, int documentCount) {
        request.setAttribute("profileDocumentCount", documentCount);

        switch (approvalStatus != null ? approvalStatus : "Pending") {
            case "Approved" -> {
                request.setAttribute("profileStatusBadge", "Đã duyệt");
                request.setAttribute("profileStatusBadgeClass", "success");
            }
            case "Rejected" -> {
                request.setAttribute("profileStatusBadge", "Bị từ chối");
                request.setAttribute("profileStatusBadgeClass", "pending");
            }
            default -> {
                request.setAttribute("profileStatusBadge", "Chờ duyệt");
                request.setAttribute("profileStatusBadgeClass", "info");
            }
        }
    }

    private void applyExamStatsBadges(HttpServletRequest request, int registeredExams, int upcomingExams, int examResults) {
        if (upcomingExams > 0) {
            request.setAttribute("examStatBadge", "Sắp thi");
            request.setAttribute("examStatBadgeClass", "info");
        } else if (registeredExams > 0) {
            request.setAttribute("examStatBadge", "Đã đăng ký");
            request.setAttribute("examStatBadgeClass", "success");
        } else {
            request.setAttribute("examStatBadge", "Chưa có");
            request.setAttribute("examStatBadgeClass", "neutral");
        }

        if (examResults > 0) {
            request.setAttribute("resultStatBadge", "Đã có kết quả");
            request.setAttribute("resultStatBadgeClass", "success");
        } else if (registeredExams > 0) {
            request.setAttribute("resultStatBadge", "Chờ kết quả");
            request.setAttribute("resultStatBadgeClass", "pending");
        } else {
            request.setAttribute("resultStatBadge", "Chưa có");
            request.setAttribute("resultStatBadgeClass", "neutral");
        }
    }

    private String formatShiftTime(Time start, Time end) {
        if (start == null || end == null) {
            return "";
        }
        LocalDateTime startTime = start.toLocalTime().atDate(LocalDate.now());
        LocalDateTime endTime = end.toLocalTime().atDate(LocalDate.now());
        return startTime.format(TIME_FORMAT) + " — " + endTime.format(TIME_FORMAT);
    }

    private long calculateDaysUntil(java.sql.Date examDate) {
        if (examDate == null) {
            return 0;
        }
        LocalDate target = examDate.toLocalDate();
        LocalDate today = LocalDate.now();
        return Math.max(0, ChronoUnit.DAYS.between(today, target));
    }

    private String formatActivityTime(java.sql.Timestamp occurredAt) {
        if (occurredAt == null) {
            return "";
        }

        LocalDateTime time = occurredAt.toLocalDateTime();
        LocalDate today = LocalDate.now();
        LocalDate date = time.toLocalDate();

        if (date.equals(today)) {
            return "Hôm nay, " + time.format(TIME_FORMAT);
        }
        if (date.equals(today.minusDays(1))) {
            return "Hôm qua, " + time.format(TIME_FORMAT);
        }
        return time.format(DISPLAY_FORMAT);
    }
}

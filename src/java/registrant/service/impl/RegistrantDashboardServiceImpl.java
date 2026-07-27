package registrant.service.impl;

import registrant.dao.DocumentDAO;
import payment.dao.PaymentDAO;
import auth.dao.ProfileDAO;
import registrant.dao.RegistrantDAO;
import registrant.dao.impl.DocumentDAOImpl;
import payment.dao.impl.PaymentDAOImpl;
import auth.dao.impl.ProfileDAOImpl;
import registrant.dao.impl.RegistrantDAOImpl;
import registrant.enums.ProfileRegistrationStatus;
import shared.model.Profile;
import registrant.dto.RegistrantDashboardActionItem;
import registrant.dto.RegistrantDashboardActivity;
import registrant.dto.RegistrantDocumentView;
import registrant.dto.RegistrantRegisteredExamRow;
import auth.dto.UserDTO;
import registrant.service.RegistrantDashboardService;
import registrant.util.RegistrantDashboardActionItemsBuilder;
import registrant.util.RegistrantExamSupport;
import registrant.util.RegistrantFilterSupport;
import registrant.util.RegistrantFilterSupport.ExamListFilterState;
import registrant.util.RegistrantListFilter;
import registrant.util.RegistrantProfileSupport;
import registrant.controller.RegistrantServletSupport;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Dashboard cá nhân: gom stats + ca thi + hoạt động + CTA.
 * totalFee lấy từ PaymentDAO.sumCompletedPaymentsByUserId — không phải API SePay; chỉ cộng các dòng Payment đã hoàn tất (tiền mặt/SePay desk).
 */
public class RegistrantDashboardServiceImpl implements RegistrantDashboardService {

    private final ProfileDAO profiledao = new ProfileDAOImpl();
    private final RegistrantDAO registrantdao = new RegistrantDAOImpl();
    private final PaymentDAO paymentdao = new PaymentDAOImpl();
    private final DocumentDAO documentdao = new DocumentDAOImpl();

    /** Gom stats, danh sách đăng ký đã lọc, hoạt động, upcoming và việc cần làm. */
    @Override
    public Map<String, Object> buildDashboardModel(UserDTO user, HttpServletRequest request) {
        Map<String, Object> model = new HashMap<>();
        Profile profile = RegistrantProfileSupport.resolveProfile(profiledao, user);
        int profileId = profile != null ? profile.getProfileId() : 0;

        if (profile != null) {
            model.put("registrantName", profile.getFullName());
        }

        Map<String, Object> stats = registrantdao.loadDashboardStats(user.getUserId(), profileId);
        model.putAll(stats);
        // Payment.TotalAmount hoàn tất, join Profile.GovernmentIdNumber = Candidate.GovernmentIdNumber
        model.put("totalFee", paymentdao.sumCompletedPaymentsByUserId(user.getUserId()));

        String registrationStatus = resolveRegistrationStatus(profileId);
        RegistrantProfileSupport.applyRegistrationStatus(model, registrationStatus);

        List<RegistrantRegisteredExamRow> allExams = profileId > 0
                ? registrantdao.listRegisteredExamsByProfileId(profileId, 50)
                : registrantdao.listRegisteredExamsByUserId(user.getUserId(), 50);

        List<String> allLicenceValues = RegistrantFilterSupport.collectLicenceCodesFromCatalogue(
                registrantdao.listOpenLicenceOptions());
        ExamListFilterState filterState = RegistrantFilterSupport.parseRegisteredExamFilter(
                request, allExams, allLicenceValues);
        List<RegistrantRegisteredExamRow> filteredExams = RegistrantListFilter.filterRegisteredExams(
                allExams, filterState.getSearchQuery(), filterState.getStatusFilter(), filterState.getLicenceFilter());
        List<RegistrantDashboardActivity> allActivities = registrantdao.listRecentActivities(profileId, 20);
        List<RegistrantDashboardActivity> filteredActivities =
                RegistrantListFilter.filterActivities(allActivities, filterState.getSearchQuery());

        model.put("registeredExamList", filteredExams);
        model.put("activityList", filteredActivities);
        RegistrantFilterSupport.applyExamListFilter(model, filterState);
        model.put("filteredExamCount", filteredExams.size());
        model.put("totalRegisteredExamCount", allExams.size());
        model.put("filteredActivityCount", filteredActivities.size());
        model.put("totalActivityCount", allActivities.size());

        RegistrantRegisteredExamRow upcoming = profileId > 0
                ? registrantdao.findUpcomingExamByProfileId(profileId)
                : registrantdao.findUpcomingExamByUserId(user.getUserId());
        putUpcomingExam(model, upcoming, registrantdao);

        List<RegistrantDocumentView> documents = profileId > 0
                ? documentdao.listByProfileId(profileId)
                : Collections.emptyList();
        int registeredExams = toIntStat(stats.get("registeredExams"));
        int examResults = toIntStat(stats.get("examResults"));
        boolean hasCancelledPreferredWithoutActive = profileId > 0
                && registrantdao.hasAnyCancelledPreferredExamDateWithoutActive(profileId);
        List<RegistrantDashboardActionItem> actionItems = RegistrantDashboardActionItemsBuilder.build(
                profile, registrationStatus, documents, registeredExams, examResults, upcoming,
                hasCancelledPreferredWithoutActive);
        model.put("dashboardActionItems", actionItems);
        model.put("dashboardActionsComplete", actionItems.isEmpty());
        return model;
    }

    /** Copy model dashboard sang request attribute. */
    @Override
    public void copyToRequest(Map<String, Object> model, HttpServletRequest request) {
        RegistrantServletSupport.copyModelToRequest(model, request);
    }

    private String resolveRegistrationStatus(int profileId) {
        if (profileId <= 0) {
            return ProfileRegistrationStatus.DRAFT;
        }
        return registrantdao.findProfileDocumentRegistrationStatus(profileId);
    }

    private static void putUpcomingExam(Map<String, Object> model, RegistrantRegisteredExamRow upcoming,
            RegistrantDAO registrantdao) {
        if (upcoming == null) {
            return;
        }
        model.put("upcomingExamName", upcoming.getExamName());
        model.put("upcomingExamDate", upcoming.getExamDate());
        model.put("upcomingExamLocation", upcoming.getLocation());
        Integer days = registrantdao.daysUntil(upcoming.getExamDate());
        model.put("upcomingExamDays", days != null ? days : 0);
        model.put("upcomingSessionTimePublished", upcoming.isSessionTimePublished());
        if (upcoming.isSessionTimePublished()) {
            model.put("upcomingExamTime", RegistrantExamSupport.formatSessionTimeRange(
                    upcoming.getSessionStart(), upcoming.getSessionEnd()));
        }
    }

    private static int toIntStat(Object value) {
        return value instanceof Number ? ((Number) value).intValue() : 0;
    }
}

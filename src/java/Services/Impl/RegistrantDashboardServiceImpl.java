package Services.Impl;

import DAO.DocumentDAO;
import DAO.PaymentDAO;
import DAO.ProfileDAO;
import DAO.RegistrantDAO;
import DAO.Impl.DocumentDAOImpl;
import DAO.Impl.PaymentDAOImpl;
import DAO.Impl.ProfileDAOImpl;
import DAO.Impl.RegistrantDAOImpl;
import Constants.ProfileRegistrationStatus;
import Models.Profile;
import Models.RegistrantDashboardActionItem;
import Models.RegistrantDashboardActivity;
import Models.RegistrantDocumentView;
import Models.RegistrantRegisteredExamRow;
import Models.User;
import Services.RegistrantDashboardService;
import Utils.RegistrantDashboardActionItemsBuilder;
import Utils.RegistrantExamSupport;
import Utils.RegistrantFilterSupport;
import Utils.RegistrantFilterSupport.ExamListFilterState;
import Utils.RegistrantListFilter;
import Utils.RegistrantProfileSupport;
import Controllers.Registrant.RegistrantServletSupport;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** Xây dựng dữ liệu dashboard cá nhân từ DB. */
public class RegistrantDashboardServiceImpl implements RegistrantDashboardService {

    private final ProfileDAO profileDAO = new ProfileDAOImpl();
    private final RegistrantDAO registrantDAO = new RegistrantDAOImpl();
    private final PaymentDAO paymentDAO = new PaymentDAOImpl();
    private final DocumentDAO documentDAO = new DocumentDAOImpl();

    @Override
    public Map<String, Object> buildDashboardModel(User user, HttpServletRequest request) {
        Map<String, Object> model = new HashMap<>();
        Profile profile = RegistrantProfileSupport.resolveProfile(profileDAO, user);
        int profileId = profile != null ? profile.getId() : 0;

        if (profile != null) {
            model.put("registrantName", profile.getFullName());
        }

        Map<String, Object> stats = registrantDAO.loadDashboardStats(user.getId(), profileId);
        model.putAll(stats);
        model.put("totalFee", paymentDAO.sumCompletedPaymentsByUserId(user.getId()));

        String registrationStatus = resolveRegistrationStatus(profileId);
        RegistrantProfileSupport.applyRegistrationStatus(model, registrationStatus);

        List<RegistrantRegisteredExamRow> allExams = profileId > 0
                ? registrantDAO.listRegisteredExamsByProfileId(profileId, 50)
                : registrantDAO.listRegisteredExamsByUserId(user.getId(), 50);

        ExamListFilterState filterState = RegistrantFilterSupport.parseRegisteredExamFilter(request, allExams);
        List<RegistrantRegisteredExamRow> filteredExams = RegistrantListFilter.filterRegisteredExams(
                allExams, filterState.getSearchQuery(), filterState.getStatusFilter(), filterState.getLicenceFilter());
        List<RegistrantDashboardActivity> allActivities = registrantDAO.listRecentActivities(profileId, 20);
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
                ? registrantDAO.findUpcomingExamByProfileId(profileId)
                : registrantDAO.findUpcomingExamByUserId(user.getId());
        putUpcomingExam(model, upcoming, registrantDAO);

        List<RegistrantDocumentView> documents = profileId > 0
                ? documentDAO.listByProfileId(profileId)
                : Collections.emptyList();
        int registeredExams = toIntStat(stats.get("registeredExams"));
        int examResults = toIntStat(stats.get("examResults"));
        List<RegistrantDashboardActionItem> actionItems = RegistrantDashboardActionItemsBuilder.build(
                profile, registrationStatus, documents, registeredExams, examResults, upcoming);
        model.put("dashboardActionItems", actionItems);
        model.put("dashboardActionsComplete", actionItems.isEmpty());
        return model;
    }

    @Override
    public void copyToRequest(Map<String, Object> model, HttpServletRequest request) {
        RegistrantServletSupport.copyModelToRequest(model, request);
    }

    private String resolveRegistrationStatus(int profileId) {
        if (profileId <= 0) {
            return ProfileRegistrationStatus.DRAFT;
        }
        return registrantDAO.findProfileDocumentRegistrationStatus(profileId);
    }

    private static void putUpcomingExam(Map<String, Object> model, RegistrantRegisteredExamRow upcoming,
            RegistrantDAO registrantDAO) {
        if (upcoming == null) {
            return;
        }
        model.put("upcomingExamName", upcoming.getExamName());
        model.put("upcomingExamDate", upcoming.getExamDate());
        model.put("upcomingExamLocation", upcoming.getLocation());
        Integer days = registrantDAO.daysUntil(upcoming.getExamDate());
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

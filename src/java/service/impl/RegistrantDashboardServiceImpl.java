package service.impl;

import dao.DocumentDAO;
import dao.PaymentDAO;
import dao.ProfileDAO;
import dao.RegistrantDAO;
import dao.impl.DocumentDAOImpl;
import dao.impl.PaymentDAOImpl;
import dao.impl.ProfileDAOImpl;
import dao.impl.RegistrantDAOImpl;
import enums.registrant.ProfileRegistrationStatus;
import model.user.Profile;
import dto.registrant.RegistrantDashboardActionItem;
import dto.registrant.RegistrantDashboardActivity;
import dto.registrant.RegistrantDocumentView;
import dto.registrant.RegistrantRegisteredExamRow;
import model.user.User;
import service.RegistrantDashboardService;
import util.registrant.RegistrantDashboardActionItemsBuilder;
import util.registrant.RegistrantExamSupport;
import util.registrant.RegistrantFilterSupport;
import util.registrant.RegistrantFilterSupport.ExamListFilterState;
import util.registrant.RegistrantListFilter;
import util.registrant.RegistrantProfileSupport;
import controller.registrant.RegistrantServletSupport;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** Xây dựng dữ liệu dashboard cá nhân từ DB. */
public class RegistrantDashboardServiceImpl implements RegistrantDashboardService {

    private final ProfileDAO profiledao = new ProfileDAOImpl();
    private final RegistrantDAO registrantdao = new RegistrantDAOImpl();
    private final PaymentDAO paymentdao = new PaymentDAOImpl();
    private final DocumentDAO documentdao = new DocumentDAOImpl();

    @Override
    public Map<String, Object> buildDashboardModel(User user, HttpServletRequest request) {
        Map<String, Object> model = new HashMap<>();
        Profile profile = RegistrantProfileSupport.resolveProfile(profiledao, user);
        int profileId = profile != null ? profile.getId() : 0;

        if (profile != null) {
            model.put("registrantName", profile.getFullName());
        }

        Map<String, Object> stats = registrantdao.loadDashboardStats(user.getId(), profileId);
        model.putAll(stats);
        model.put("totalFee", paymentdao.sumCompletedPaymentsByUserId(user.getId()));

        String registrationStatus = resolveRegistrationStatus(profileId);
        RegistrantProfileSupport.applyRegistrationStatus(model, registrationStatus);

        List<RegistrantRegisteredExamRow> allExams = profileId > 0
                ? registrantdao.listRegisteredExamsByProfileId(profileId, 50)
                : registrantdao.listRegisteredExamsByUserId(user.getId(), 50);

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
                : registrantdao.findUpcomingExamByUserId(user.getId());
        putUpcomingExam(model, upcoming, registrantdao);

        List<RegistrantDocumentView> documents = profileId > 0
                ? documentdao.listByProfileId(profileId)
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

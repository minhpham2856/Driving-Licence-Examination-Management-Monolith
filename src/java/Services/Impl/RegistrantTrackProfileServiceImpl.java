package Services.Impl;

import DAO.AuditLogDAO;
import DAO.DocumentDAO;
import DAO.ProfileDAO;
import DAO.RegistrantDAO;
import DAO.Impl.AuditLogDAOImpl;
import DAO.Impl.DocumentDAOImpl;
import DAO.Impl.ProfileDAOImpl;
import DAO.Impl.RegistrantDAOImpl;
import Constants.ProfileRegistrationStatus;
import Models.AuditLog;
import Models.RegistrantDocumentSummary;
import Models.RegistrantProfileProgressStep;
import Models.RegistrantRegisteredExamRow;
import Models.RegistrantTrackingLog;
import Models.User;
import Services.RegistrantTrackProfileService;
import Utils.RegistrantAuditMapper;
import Utils.RegistrantDocumentStatusHelper;
import Utils.RegistrantProfileProgressBuilder;
import Utils.RegistrantProfileSupport;
import Utils.RegistrantTrackingFilter;
import Utils.RegistrantTrackingFilter.TrackingFilterState;
import jakarta.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class RegistrantTrackProfileServiceImpl implements RegistrantTrackProfileService {

    private static final int TRACKING_PAGE_SIZE = 50;
    private static final int TRACKING_FETCH_LIMIT = 500;

    private final ProfileDAO profileDAO = new ProfileDAOImpl();
    private final RegistrantDAO registrantDAO = new RegistrantDAOImpl();
    private final DocumentDAO documentDAO = new DocumentDAOImpl();
    private final AuditLogDAO auditLogDAO = new AuditLogDAOImpl();

    @Override
    public void copyTrackingToRequest(User user, HttpServletRequest request) {
        var ctx = RegistrantProfileSupport.loadContext(profileDAO, documentDAO, registrantDAO, user);
        if (!ctx.hasProfile()) {
            return;
        }

        int profileId = ctx.getProfileId();
        List<AuditLog> auditLogs = auditLogDAO.getLogsByProfileId(profileId, TRACKING_FETCH_LIMIT);
        List<RegistrantTrackingLog> auditTracking = RegistrantAuditMapper.toTrackingLogs(auditLogs);
        List<RegistrantTrackingLog> legacyTracking =
                registrantDAO.buildProfileTrackingLogs(profileId, user.getId());
        List<RegistrantTrackingLog> unified = mergeTrackingLogs(auditTracking, legacyTracking);

        TrackingFilterState filterState = RegistrantTrackingFilter.parse(request, unified);
        List<RegistrantTrackingLog> filtered = RegistrantTrackingFilter.apply(unified, filterState);
        int totalPages = Math.max(1, (int) Math.ceil(filtered.size() / (double) TRACKING_PAGE_SIZE));
        int safePage = Math.min(filterState.getPage(), totalPages);
        List<RegistrantTrackingLog> pageLogs =
                RegistrantTrackingFilter.paginate(filtered, safePage, TRACKING_PAGE_SIZE);

        request.setAttribute("profileTrackingLogs", pageLogs);
        request.setAttribute("auditViewRows", RegistrantAuditMapper.toAuditViewRows(auditLogs));
        RegistrantTrackingFilter.applyToRequest(request, filterState, filtered.size(), unified.size(), totalPages);

        String registrationStatus = ctx.getRegistrationStatus();
        RegistrantDocumentSummary documentSummary = RegistrantDocumentStatusHelper.summarize(
                ctx.getDocuments(), documentDAO.typeLabels(), registrationStatus);
        request.setAttribute("documentSummary", documentSummary);
        RegistrantProfileSupport.applyRegistrationStatus(request, registrationStatus);
        request.setAttribute("showSupplementAlert",
                ProfileRegistrationStatus.REJECTED.equalsIgnoreCase(registrationStatus));

        List<RegistrantRegisteredExamRow> registeredExams =
                registrantDAO.listRegisteredExamsByProfileId(profileId, 20);
        List<RegistrantProfileProgressStep> progressSteps = RegistrantProfileProgressBuilder.build(
                registrationStatus, documentSummary, unified, registeredExams);
        request.setAttribute("profileProgressSteps", progressSteps);
    }

    private static List<RegistrantTrackingLog> mergeTrackingLogs(
            List<RegistrantTrackingLog> primary, List<RegistrantTrackingLog> supplemental) {
        List<RegistrantTrackingLog> merged = new ArrayList<>(primary);
        Set<String> seen = new LinkedHashSet<>();
        for (RegistrantTrackingLog log : primary) {
            seen.add(trackingKey(log));
        }
        for (RegistrantTrackingLog log : supplemental) {
            String key = trackingKey(log);
            if (!seen.contains(key)) {
                merged.add(log);
                seen.add(key);
            }
        }
        merged.sort(Comparator.comparing(RegistrantTrackingLog::getTimestamp,
                Comparator.nullsLast(Comparator.reverseOrder())));
        return merged;
    }

    private static String trackingKey(RegistrantTrackingLog log) {
        return (log.getCategory() != null ? log.getCategory() : "")
                + "|" + (log.getEventTitle() != null ? log.getEventTitle() : "")
                + "|" + (log.getRemarks() != null ? log.getRemarks() : "");
    }
}

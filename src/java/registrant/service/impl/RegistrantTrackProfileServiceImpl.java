package registrant.service.impl;

import registrant.dao.AuditLogDAO;
import registrant.dao.DocumentDAO;
import auth.dao.ProfileDAO;
import registrant.dao.RegistrantDAO;
import registrant.dao.impl.AuditLogDAOImpl;
import registrant.dao.impl.DocumentDAOImpl;
import auth.dao.impl.ProfileDAOImpl;
import registrant.dao.impl.RegistrantDAOImpl;
import registrant.enums.ProfileRegistrationStatus;
import registrant.dto.AuditLogEntry;
import registrant.dto.RegistrantDocumentSummary;
import registrant.dto.RegistrantProfileProgressStep;
import registrant.dto.RegistrantRegisteredExamRow;
import registrant.dto.RegistrantTrackingLog;
import auth.dto.UserDTO;
import registrant.service.RegistrantTrackProfileService;
import registrant.util.RegistrantAuditHelper;
import registrant.util.RegistrantDocumentStatusHelper;
import registrant.util.RegistrantProfileProgressBuilder;
import registrant.util.RegistrantProfileSupport;
import registrant.util.RegistrantTrackingFilter;
import registrant.util.RegistrantTrackingFilter.TrackingFilterState;
import jakarta.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class RegistrantTrackProfileServiceImpl implements RegistrantTrackProfileService {

    private static final int TRACKING_PAGE_SIZE = 50;
    private static final int TRACKING_FETCH_LIMIT = 500;

    private final ProfileDAO profiledao = new ProfileDAOImpl();
    private final RegistrantDAO registrantdao = new RegistrantDAOImpl();
    private final DocumentDAO documentdao = new DocumentDAOImpl();
    private final AuditLogDAO auditLogdao = new AuditLogDAOImpl();

    /** Build progress steps + nhật ký đã lọc/phân trang cho track-profile. */
    @Override
    public void copyTrackingToRequest(UserDTO user, HttpServletRequest request) {
        var ctx = RegistrantProfileSupport.loadContext(profiledao, documentdao, registrantdao, user);
        if (!ctx.hasProfile()) {
            return;
        }

        int profileId = ctx.getProfileId();
        List<AuditLogEntry> auditLogs = auditLogdao.getLogsByProfileId(profileId, TRACKING_FETCH_LIMIT);
        List<RegistrantTrackingLog> auditTracking = RegistrantAuditHelper.toTrackingLogs(auditLogs);
        List<RegistrantTrackingLog> legacyTracking =
                registrantdao.buildProfileTrackingLogs(profileId, user.getUserId());
        List<RegistrantTrackingLog> unified = mergeTrackingLogs(auditTracking, legacyTracking);

        TrackingFilterState filterState = RegistrantTrackingFilter.parse(request, unified);
        List<RegistrantTrackingLog> filtered = RegistrantTrackingFilter.apply(unified, filterState);
        int totalPages = Math.max(1, (int) Math.ceil(filtered.size() / (double) TRACKING_PAGE_SIZE));
        int safePage = Math.min(filterState.getPage(), totalPages);
        List<RegistrantTrackingLog> pageLogs =
                RegistrantTrackingFilter.paginate(filtered, safePage, TRACKING_PAGE_SIZE);

        request.setAttribute("profileTrackingLogs", pageLogs);
        RegistrantTrackingFilter.applyToRequest(request, filterState, filtered.size(), unified.size(), totalPages);

        String registrationStatus = ctx.getRegistrationStatus();
        RegistrantDocumentSummary documentSummary = RegistrantDocumentStatusHelper.summarize(
                ctx.getDocuments(), documentdao.typeLabels(), registrationStatus);
        request.setAttribute("documentSummary", documentSummary);
        RegistrantProfileSupport.applyRegistrationStatus(request, registrationStatus);
        request.setAttribute("showSupplementAlert",
                ProfileRegistrationStatus.REJECTED.equalsIgnoreCase(registrationStatus));

        List<RegistrantRegisteredExamRow> registeredExams =
                registrantdao.listRegisteredExamsByProfileId(profileId, 20);
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

package service.impl;

import dao.AuditLogDAO;
import dao.DocumentDAO;
import dao.ProfileDAO;
import dao.RegistrantDAO;
import dao.impl.AuditLogDAOImpl;
import dao.impl.DocumentDAOImpl;
import dao.impl.ProfileDAOImpl;
import dao.impl.RegistrantDAOImpl;
import enums.registrant.ProfileRegistrationStatus;
import model.user.AuditLog;
import dto.registrant.RegistrantDocumentSummary;
import dto.registrant.RegistrantProfileProgressStep;
import dto.registrant.RegistrantRegisteredExamRow;
import dto.registrant.RegistrantTrackingLog;
import model.user.User;
import service.RegistrantTrackProfileService;
import util.registrant.RegistrantAuditHelper;
import util.registrant.RegistrantDocumentStatusHelper;
import util.registrant.RegistrantProfileProgressBuilder;
import util.registrant.RegistrantProfileSupport;
import util.registrant.RegistrantTrackingFilter;
import util.registrant.RegistrantTrackingFilter.TrackingFilterState;
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

    @Override
    public void copyTrackingToRequest(User user, HttpServletRequest request) {
        var ctx = RegistrantProfileSupport.loadContext(profiledao, documentdao, registrantdao, user);
        if (!ctx.hasProfile()) {
            return;
        }

        int profileId = ctx.getProfileId();
        List<AuditLog> auditLogs = auditLogdao.getLogsByProfileId(profileId, TRACKING_FETCH_LIMIT);
        List<RegistrantTrackingLog> auditTracking = RegistrantAuditHelper.toTrackingLogs(auditLogs);
        List<RegistrantTrackingLog> legacyTracking =
                registrantdao.buildProfileTrackingLogs(profileId, user.getId());
        List<RegistrantTrackingLog> unified = mergeTrackingLogs(auditTracking, legacyTracking);

        TrackingFilterState filterState = RegistrantTrackingFilter.parse(request, unified);
        List<RegistrantTrackingLog> filtered = RegistrantTrackingFilter.apply(unified, filterState);
        int totalPages = Math.max(1, (int) Math.ceil(filtered.size() / (double) TRACKING_PAGE_SIZE));
        int safePage = Math.min(filterState.getPage(), totalPages);
        List<RegistrantTrackingLog> pageLogs =
                RegistrantTrackingFilter.paginate(filtered, safePage, TRACKING_PAGE_SIZE);

        request.setAttribute("profileTrackingLogs", pageLogs);
        request.setAttribute("auditViewRows", RegistrantAuditHelper.toAuditViewRows(auditLogs));
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

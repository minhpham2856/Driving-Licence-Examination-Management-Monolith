package Services.Impl;

import DAO.DocumentDAO;
import DAO.ExamRegistrationDAO;
import DAO.ProfileDAO;
import DAO.RegistrantDAO;
import DAO.Impl.DocumentDAOImpl;
import DAO.Impl.ExamRegistrationDAOImpl;
import DAO.Impl.ProfileDAOImpl;
import DAO.Impl.RegistrantDAOImpl;
import Models.ExamRegistration;
import Models.Profile;
import Models.RegistrantDocumentSummary;
import Models.RegistrantExamSessionOption;
import Models.RegistrantLicenceOption;
import Models.RegistrantProfileContext;
import Models.User;
import Services.RegistrantRegisterExamService;
import Utils.RegistrantAuditHelper;
import Utils.RegistrantDocumentStatusHelper;
import Utils.RegistrantExamRegistrationRules;
import Utils.RegistrantExamSupport;
import Utils.RegistrantFilterSupport;
import Utils.RegistrantFilterSupport.SessionListFilterState;
import Utils.RegistrantListFilter;
import Utils.RegistrantProfileSupport;
import Controllers.Registrant.RegistrantServletSupport;
import jakarta.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * Luồng đăng ký đợt thi: load hạng/ca từ DB và tạo Candidate khi POST.
 * Yêu cầu tài liệu bắt buộc đã được phê duyệt; thanh toán xử lý ở module khác.
 */
public class RegistrantRegisterExamServiceImpl implements RegistrantRegisterExamService {

    public static final String FLASH_ERROR_ATTR = "registerExamError";

    private final RegistrantDAO registrantDAO = new RegistrantDAOImpl();
    private final ProfileDAO profileDAO = new ProfileDAOImpl();
    private final ExamRegistrationDAO examRegistrationDAO = new ExamRegistrationDAOImpl();
    private final DocumentDAO documentDAO = new DocumentDAOImpl();

    @Override
    public void loadRegisterExamPage(User user, HttpServletRequest request) {
        RegistrantServletSupport.consumeFlash(request, FLASH_ERROR_ATTR, "error");
        request.setAttribute("sbdPendingDisplay", RegistrantExamSupport.SBD_PENDING_MESSAGE);

        List<RegistrantLicenceOption> licences = registrantDAO.listOpenLicenceOptions();
        request.setAttribute("licenceClassesList", licences);

        String licenceSelect = resolveLicenceSelect(request, licences);
        request.setAttribute("selectedLicenceCode", licenceSelect);
        licences.stream()
                .filter(l -> licenceSelect.equals(l.getCode()))
                .findFirst()
                .ifPresent(l -> request.setAttribute("selectedLicence", l));

        List<RegistrantExamSessionOption> allSessions =
                registrantDAO.listOpenExamSessionsByLicenceCode(licenceSelect);
        SessionListFilterState filterState = RegistrantFilterSupport.parseSessionFilter(request, allSessions);
        List<RegistrantExamSessionOption> sessions = RegistrantListFilter.filterExamSessions(
                allSessions, filterState.getSearchQuery(), filterState.getLocationFilter(),
                filterState.getFromDate(), filterState.getToDate());
        request.setAttribute("examSessionsList", sessions);
        RegistrantFilterSupport.applySessionListFilter(request, filterState);
        request.setAttribute("filteredSessionCount", sessions.size());
        request.setAttribute("totalSessionCount", allSessions.size());

        RegistrantExamSessionOption selectedSession = resolveSelectedSession(request, licenceSelect, sessions);
        boolean sessionChosen = selectedSession != null;
        request.setAttribute("sessionChosen", sessionChosen);
        request.setAttribute("selectedSessionCode", sessionChosen ? selectedSession.getId() : "");
        if (sessionChosen) {
            request.setAttribute("selectedSession", selectedSession);
        }

        attachDocumentGate(user, request);
        attachRegistrationRules(user, request, licenceSelect, selectedSession, sessionChosen);
    }

    @Override
    public String buildRegisterExamPageUrl(HttpServletRequest request, String fragment) {
        StringBuilder url = new StringBuilder(request.getContextPath())
                .append("/registrant/register-exam?");
        RegistrantServletSupport.appendQueryParam(url, "licenceSelect", request.getParameter("licenceSelect"));
        RegistrantServletSupport.appendQueryParam(url, "sessionSelect", request.getParameter("sessionSelect"));
        RegistrantServletSupport.appendQueryParam(url, "q", request.getParameter("q"));
        RegistrantServletSupport.appendQueryParam(url, "location", request.getParameter("location"));
        RegistrantServletSupport.appendQueryParam(url, "fromDate", request.getParameter("fromDate"));
        RegistrantServletSupport.appendQueryParam(url, "toDate", request.getParameter("toDate"));
        RegistrantServletSupport.trimTrailingAmpersand(url);
        if (fragment != null && !fragment.isBlank()) {
            url.append('#').append(fragment);
        }
        return url.toString();
    }

    @Override
    public String submitRegistration(User user, HttpServletRequest request) {
        String licenceSelect = request.getParameter("licenceSelect");
        String sessionSelect = request.getParameter("sessionSelect");

        if (RegistrantProfileSupport.isBlank(licenceSelect)) {
            return "Vui lòng chọn hạng bằng lái.";
        }
        if (RegistrantProfileSupport.isBlank(sessionSelect)) {
            return "Vui lòng chọn đợt thi.";
        }

        Profile profile = RegistrantProfileSupport.resolveProfile(profileDAO, user);
        if (profile == null) {
            return "Không tìm thấy hồ sơ cá nhân.";
        }

        String documentBlock = checkDocumentEligibility(profile.getId());
        if (documentBlock != null) {
            return documentBlock;
        }

        RegistrantExamSessionOption sessionOpt = registrantDAO.findExamSessionByCode(sessionSelect);
        if (sessionOpt == null) {
            return "Đợt thi không tồn tại hoặc đã đóng đăng ký.";
        }

        Integer existing = examRegistrationDAO.findCandidateIdByProfileAndSession(
                profile.getId(), sessionOpt.getSessionId());
        if (existing != null) {
            return "Bạn đã đăng ký đợt thi này rồi.";
        }

        int licenceId = registrantDAO.resolveLicenceIdByUiCode(licenceSelect.trim());
        if (licenceId <= 0) {
            return "Hạng GPLX không hợp lệ.";
        }
        String sectionConflict = RegistrantExamRegistrationRules.validateNewSessionRegistration(
                examRegistrationDAO, profile.getId(), sessionOpt.getSessionId(), licenceId, licenceSelect.trim());
        if (sectionConflict != null) {
            return sectionConflict;
        }

        ExamRegistration reg = new ExamRegistration();
        reg.setExamSessionId(sessionOpt.getSessionId());
        reg.setPersonId(profile.getId());
        reg.setCandidateNo(0);
        reg.setRegistrationType("PreRegistered");
        reg.setIsPaymentCompleted(false);

        if (!examRegistrationDAO.insert(reg)) {
            String detail = examRegistrationDAO.getLastInsertError();
            if (detail != null && !detail.isBlank()) {
                return detail;
            }
            return "Không thể ghi nhận đăng ký. Vui lòng thử lại sau.";
        }

        String examLabel = sessionOpt.getExamName() + " (" + sessionOpt.getExamCode() + ")";
        RegistrantAuditHelper.logExamRegistration(request.getSession(), profile.getId(), examLabel);
        return null;
    }

    private static String resolveLicenceSelect(HttpServletRequest request,
            List<RegistrantLicenceOption> licences) {
        String paramLicence = request.getParameter("licenceSelect");
        if (paramLicence != null && !paramLicence.isBlank()) {
            return paramLicence.trim();
        }
        return licences.isEmpty() ? "" : licences.get(0).getCode();
    }

    private RegistrantExamSessionOption resolveSelectedSession(HttpServletRequest request,
            String licenceSelect, List<RegistrantExamSessionOption> sessions) {
        String paramSession = request.getParameter("sessionSelect");
        if (paramSession == null || paramSession.isBlank()) {
            return null;
        }
        RegistrantExamSessionOption resolved = registrantDAO.findExamSessionByCode(paramSession.trim());
        if (resolved == null || !licenceSelect.equals(resolved.getLicenceClass())) {
            return null;
        }
        boolean inFilteredList = sessions.stream().anyMatch(s -> resolved.getId().equals(s.getId()));
        return inFilteredList ? resolved : null;
    }

    private void attachRegistrationRules(User user, HttpServletRequest request, String licenceSelect,
            RegistrantExamSessionOption selectedSession, boolean sessionChosen) {
        Profile profile = RegistrantProfileSupport.resolveProfile(profileDAO, user);
        if (profile == null || !sessionChosen || selectedSession == null) {
            request.setAttribute("canConfirmRegistration", false);
            return;
        }
        int licenceId = registrantDAO.resolveLicenceIdByUiCode(licenceSelect);
        if (licenceId <= 0) {
            request.setAttribute("canConfirmRegistration", false);
            return;
        }
        String conflict = RegistrantExamRegistrationRules.validateNewSessionRegistration(
                examRegistrationDAO, profile.getId(), selectedSession.getSessionId(), licenceId, licenceSelect);
        if (conflict != null) {
            request.setAttribute("registrationConflictMessage", conflict);
            request.setAttribute("canConfirmRegistration", false);
        } else {
            request.setAttribute("canConfirmRegistration", true);
        }
    }

    private void attachDocumentGate(User user, HttpServletRequest request) {
        RegistrantProfileContext ctx = RegistrantProfileSupport.loadContext(
                profileDAO, documentDAO, registrantDAO, user);
        if (!ctx.hasProfile()) {
            request.setAttribute("canRegisterExam", false);
            request.setAttribute("documentGateMessage",
                    "Chưa có hồ sơ cá nhân. Vui lòng bổ sung hồ sơ trước khi đăng ký thi.");
            return;
        }

        String registrationStatus = ctx.getRegistrationStatus();
        RegistrantDocumentSummary summary = RegistrantDocumentStatusHelper.summarize(
                ctx.getDocuments(), documentDAO.typeLabels(), registrationStatus);
        boolean eligible = RegistrantDocumentStatusHelper.isEligibleForExamRegistration(
                registrationStatus, ctx.getDocuments());

        request.setAttribute("documentSummary", summary);
        request.setAttribute("canRegisterExam", eligible);
        RegistrantProfileSupport.applyRegistrationStatus(request, registrationStatus);
        request.setAttribute("showProfileApprovedNotice", eligible);

        String blockMsg = RegistrantDocumentStatusHelper.examRegistrationBlockMessage(
                registrationStatus, ctx.getDocuments(), summary);
        if (blockMsg != null) {
            request.setAttribute("documentGateMessage", blockMsg);
        }
    }

    private String checkDocumentEligibility(int profileId) {
        var docs = documentDAO.listByProfileId(profileId);
        String registrationStatus = registrantDAO.findProfileDocumentRegistrationStatus(profileId);
        if (RegistrantDocumentStatusHelper.isEligibleForExamRegistration(registrationStatus, docs)) {
            return null;
        }
        RegistrantDocumentSummary summary = RegistrantDocumentStatusHelper.summarize(
                docs, documentDAO.typeLabels(), registrationStatus);
        return RegistrantDocumentStatusHelper.examRegistrationBlockMessage(
                registrationStatus, docs, summary);
    }
}

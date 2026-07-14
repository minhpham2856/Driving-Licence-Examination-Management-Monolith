package registrant.service.impl;

import registrant.dao.DocumentDAO;
import registrant.dao.ExamRegistrationDAO;
import auth.dao.ProfileDAO;
import registrant.dao.RegistrantDAO;
import registrant.dao.impl.DocumentDAOImpl;
import registrant.dao.impl.ExamRegistrationDAOImpl;
import auth.dao.impl.ProfileDAOImpl;
import registrant.dao.impl.RegistrantDAOImpl;
import registrant.dto.exam.ExamRegistration;
import shared.model.Profile;
import registrant.dto.RegistrantDocumentSummary;
import registrant.dto.RegistrantDocumentView;
import registrant.dto.RegistrantExamSessionOption;
import registrant.dto.RegistrantLicenceOption;
import registrant.dto.RegistrantProfileContext;
import auth.dto.UserDTO;
import registrant.service.RegistrantRegisterExamService;
import registrant.util.RegistrantAuditHelper;
import registrant.util.RegistrantDocumentStatusHelper;
import registrant.util.RegistrantExamSupport;
import registrant.util.RegistrantFilterSupport;
import registrant.util.RegistrantFilterSupport.SessionListFilterState;
import registrant.util.RegistrantListFilter;
import registrant.util.RegistrantProfileSupport;
import registrant.controller.RegistrantServletSupport;
import jakarta.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * Luồng đăng ký đợt thi: load hạng/ca từ DB và tạo Candidate khi POST.
 * Yêu cầu tài liệu bắt buộc đã được phê duyệt; thanh toán xử lý ở module khác.
 */
public class RegistrantRegisterExamServiceImpl implements RegistrantRegisterExamService {

    public static final String FLASH_ERROR_ATTR = "registerExamError";

    private final RegistrantDAO registrantdao = new RegistrantDAOImpl();
    private final ProfileDAO profiledao = new ProfileDAOImpl();
    private final ExamRegistrationDAO examRegistrationdao = new ExamRegistrationDAOImpl();
    private final DocumentDAO documentdao = new DocumentDAOImpl();

    @Override
    public void loadRegisterExamPage(UserDTO user, HttpServletRequest request) {
        RegistrantServletSupport.consumeFlash(request, FLASH_ERROR_ATTR, "error");
        request.setAttribute("sbdPendingDisplay", RegistrantExamSupport.SBD_PENDING_MESSAGE);

        RegistrantProfileContext ctx = RegistrantProfileSupport.loadContext(
                profiledao, documentdao, registrantdao, user);
        var docs = ctx.hasProfile() ? ctx.getDocuments() : List.<RegistrantDocumentView>of();

        List<RegistrantLicenceOption> licences = registrantdao.listOpenLicenceOptions();
        request.setAttribute("licenceClassesList", licences);

        String licenceSelect = resolveLicenceSelect(request, licences, docs);
        request.setAttribute("selectedLicenceCode", licenceSelect);
        licences.stream()
                .filter(l -> licenceSelect.equals(l.getCode()))
                .findFirst()
                .ifPresent(l -> request.setAttribute("selectedLicence", l));

        boolean selectedLicenceAllowed = licenceSelect != null && !licenceSelect.isBlank()
                && RegistrantDocumentStatusHelper.isLicenceAllowedWithDocuments(licenceSelect, docs);
        request.setAttribute("selectedLicenceDocumentAllowed", selectedLicenceAllowed);

        List<RegistrantExamSessionOption> allSessions = selectedLicenceAllowed
                ? registrantdao.listOpenExamSessionsByLicenceCode(licenceSelect)
                : List.of();
        SessionListFilterState filterState = RegistrantFilterSupport.parseSessionFilter(request, allSessions);
        List<RegistrantExamSessionOption> sessions = RegistrantListFilter.filterExamSessions(
                allSessions, filterState.getSearchQuery(), filterState.getLocationFilter(),
                filterState.hasFilterDateError() ? null : filterState.getFromDateParsed(),
                filterState.hasFilterDateError() ? null : filterState.getToDateParsed());
        request.setAttribute("examSessionsList", sessions);
        RegistrantFilterSupport.applySessionListFilter(request, filterState);
        request.setAttribute("filteredSessionCount", sessions.size());
        request.setAttribute("totalSessionCount", allSessions.size());

        RegistrantExamSessionOption selectedSession = selectedLicenceAllowed
                ? resolveSelectedSession(request, licenceSelect, sessions)
                : null;
        boolean sessionChosen = selectedSession != null;
        request.setAttribute("sessionChosen", sessionChosen);
        request.setAttribute("selectedSessionCode", sessionChosen ? selectedSession.getId() : "");
        if (sessionChosen) {
            request.setAttribute("selectedSession", selectedSession);
        }

        attachDocumentGate(user, request);
        attachLicenceDocumentRules(request, licences, ctx);
        attachRegistrationRules(user, request, licenceSelect, selectedSession, sessionChosen, docs);
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
    public String submitRegistration(UserDTO user, HttpServletRequest request) {
        String licenceSelect = request.getParameter("licenceSelect");
        String sessionSelect = request.getParameter("sessionSelect");

        if (RegistrantProfileSupport.isBlank(licenceSelect)) {
            return "Vui lòng chọn hạng bằng lái.";
        }
        if (RegistrantProfileSupport.isBlank(sessionSelect)) {
            return "Vui lòng chọn đợt thi.";
        }

        Profile profile = RegistrantProfileSupport.resolveProfile(profiledao, user);
        if (profile == null) {
            return "Không tìm thấy hồ sơ cá nhân.";
        }

        String documentBlock = checkDocumentEligibility(profile.getProfileId());
        if (documentBlock != null) {
            return documentBlock;
        }

        var docs = documentdao.listByProfileId(profile.getProfileId());
        String licenceBlock = RegistrantDocumentStatusHelper.licenceClassBlockMessage(licenceSelect.trim(), docs);
        if (licenceBlock != null) {
            return licenceBlock;
        }

        RegistrantExamSessionOption sessionOpt = registrantdao.findExamSessionByCode(sessionSelect);
        if (sessionOpt == null) {
            return "Đợt thi không tồn tại hoặc đã đóng đăng ký.";
        }

        Integer existing = examRegistrationdao.findCandidateIdByProfileAndSession(
                profile.getProfileId(), sessionOpt.getSessionId());
        if (existing != null) {
            return "Bạn đã đăng ký đợt thi này rồi.";
        }

        int licenceId = registrantdao.resolveLicenceIdByUiCode(licenceSelect.trim());
        if (licenceId <= 0) {
            return "Hạng GPLX không hợp lệ.";
        }
        String sectionConflict = RegistrantExamSupport.validateNewSessionRegistration(
                examRegistrationdao, profile.getProfileId(), sessionOpt.getSessionId(), licenceId, licenceSelect.trim());
        if (sectionConflict != null) {
            return sectionConflict;
        }

        ExamRegistration reg = new ExamRegistration();
        reg.setExamSessionId(sessionOpt.getSessionId());
        reg.setPersonId(profile.getProfileId());
        reg.setCandidateNo(0);
        reg.setRegistrationType("PreRegistered");
        reg.setIsPaymentCompleted(false);

        if (!examRegistrationdao.insert(reg)) {
            String detail = examRegistrationdao.getLastInsertError();
            if (detail != null && !detail.isBlank()) {
                return detail;
            }
            return "Không thể ghi nhận đăng ký. Vui lòng thử lại sau.";
        }

        String examLabel = sessionOpt.getExamName() + " (" + sessionOpt.getExamCode() + ")";
        RegistrantAuditHelper.logExamRegistration(request.getSession(), profile.getProfileId(), examLabel);
        return null;
    }

    private static String resolveLicenceSelect(HttpServletRequest request,
            List<RegistrantLicenceOption> licences, List<RegistrantDocumentView> docs) {
        String paramLicence = request.getParameter("licenceSelect");
        if (paramLicence != null && !paramLicence.isBlank()) {
            return paramLicence.trim();
        }
        for (RegistrantLicenceOption option : licences) {
            if (option.getCode() != null
                    && RegistrantDocumentStatusHelper.isLicenceAllowedWithDocuments(option.getCode(), docs)) {
                return option.getCode().trim();
            }
        }
        return licences.isEmpty() ? "" : licences.get(0).getCode();
    }

    private RegistrantExamSessionOption resolveSelectedSession(HttpServletRequest request,
            String licenceSelect, List<RegistrantExamSessionOption> sessions) {
        String paramSession = request.getParameter("sessionSelect");
        if (paramSession == null || paramSession.isBlank()) {
            return null;
        }
        RegistrantExamSessionOption resolved = registrantdao.findExamSessionByCode(paramSession.trim());
        if (resolved == null || !licenceSelect.equals(resolved.getLicenceClass())) {
            return null;
        }
        boolean inFilteredList = sessions.stream().anyMatch(s -> resolved.getId().equals(s.getId()));
        return inFilteredList ? resolved : null;
    }

    private void attachRegistrationRules(UserDTO user, HttpServletRequest request, String licenceSelect,
            RegistrantExamSessionOption selectedSession, boolean sessionChosen,
            List<RegistrantDocumentView> docs) {
        Profile profile = RegistrantProfileSupport.resolveProfile(profiledao, user);
        if (profile == null || !sessionChosen || selectedSession == null) {
            request.setAttribute("canConfirmRegistration", false);
            return;
        }

        String licenceBlock = RegistrantDocumentStatusHelper.licenceClassBlockMessage(licenceSelect, docs);
        if (licenceBlock != null) {
            request.setAttribute("licenceGateMessage", licenceBlock);
            request.setAttribute("canConfirmRegistration", false);
            return;
        }

        int licenceId = registrantdao.resolveLicenceIdByUiCode(licenceSelect);
        if (licenceId <= 0) {
            request.setAttribute("canConfirmRegistration", false);
            return;
        }
        String conflict = RegistrantExamSupport.validateNewSessionRegistration(
                examRegistrationdao, profile.getProfileId(), selectedSession.getSessionId(), licenceId, licenceSelect);
        if (conflict != null) {
            request.setAttribute("registrationConflictMessage", conflict);
            request.setAttribute("canConfirmRegistration", false);
        } else {
            request.setAttribute("canConfirmRegistration", true);
        }
    }

    private void attachDocumentGate(UserDTO user, HttpServletRequest request) {
        RegistrantProfileContext ctx = RegistrantProfileSupport.loadContext(
                profiledao, documentdao, registrantdao, user);
        if (!ctx.hasProfile()) {
            request.setAttribute("canRegisterExam", false);
            request.setAttribute("documentGateMessage",
                    "Chưa có hồ sơ cá nhân. Vui lòng bổ sung hồ sơ trước khi đăng ký thi.");
            return;
        }

        String registrationStatus = ctx.getRegistrationStatus();
        RegistrantDocumentSummary summary = RegistrantDocumentStatusHelper.summarize(
                ctx.getDocuments(), documentdao.typeLabels(), registrationStatus);
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

    private void attachLicenceDocumentRules(HttpServletRequest request,
            List<RegistrantLicenceOption> licences, RegistrantProfileContext ctx) {
        if (!ctx.hasProfile()) {
            return;
        }

        List<RegistrantDocumentView> docs = ctx.getDocuments();
        List<String> codes = licences.stream()
                .map(RegistrantLicenceOption::getCode)
                .filter(code -> code != null && !code.isBlank())
                .toList();
        request.setAttribute("licenceDocumentAllowed",
                RegistrantDocumentStatusHelper.buildLicenceDocumentAllowedMap(codes, docs));
        request.setAttribute("licenceDocumentBlockMessages",
                RegistrantDocumentStatusHelper.buildLicenceDocumentBlockMessageMap(codes, docs));
        request.setAttribute("hasOtherDocuments",
                RegistrantDocumentStatusHelper.hasUploadedOtherDocuments(docs));
        request.setAttribute("basicDocsOnlyLicenceCodes",
                RegistrantDocumentStatusHelper.BASIC_DOCS_ONLY_LICENCE_CODES);

        if (!RegistrantDocumentStatusHelper.isEligibleForExamRegistration(
                ctx.getRegistrationStatus(), docs)) {
            return;
        }

        String selectedLicence = (String) request.getAttribute("selectedLicenceCode");
        if (selectedLicence != null && !selectedLicence.isBlank()) {
            String licenceBlock = RegistrantDocumentStatusHelper.licenceClassBlockMessage(
                    selectedLicence, docs);
            if (licenceBlock != null) {
                request.setAttribute("licenceGateMessage", licenceBlock);
            }
        }
    }

    private String checkDocumentEligibility(int profileId) {
        var docs = documentdao.listByProfileId(profileId);
        String registrationStatus = registrantdao.findProfileDocumentRegistrationStatus(profileId);
        if (RegistrantDocumentStatusHelper.isEligibleForExamRegistration(registrationStatus, docs)) {
            return null;
        }
        RegistrantDocumentSummary summary = RegistrantDocumentStatusHelper.summarize(
                docs, documentdao.typeLabels(), registrationStatus);
        return RegistrantDocumentStatusHelper.examRegistrationBlockMessage(
                registrationStatus, docs, summary);
    }
}

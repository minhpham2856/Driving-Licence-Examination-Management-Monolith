package registrant.service.impl;

import registrant.dao.DocumentDAO;
import auth.dao.ProfileDAO;
import registrant.dao.RegistrantDAO;
import registrant.dao.impl.DocumentDAOImpl;
import auth.dao.impl.ProfileDAOImpl;
import registrant.dao.impl.RegistrantDAOImpl;
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
import java.util.ArrayList;
import java.util.List;

/**
 * Đăng ký ngày thi dự kiến sau khi hồ sơ + hạng được duyệt.
 * <p>
 * Luồng:
 * <ol>
 *   <li>{@link #loadRegisterExamPage} — đọc ExamDates / Licence / Document Approved → request attrs</li>
 *   <li>{@link #submitRegistration} — validate → {@code RegistrantDAO.registerPreferredExamDate}
 *       → MERGE {@code RegistrationDates} (không ghi Payment)</li>
 * </ol>
 */
public class RegistrantRegisterExamServiceImpl implements RegistrantRegisterExamService {

    public static final String FLASH_ERROR_ATTR = "registerExamError";

    private final RegistrantDAO registrantdao = new RegistrantDAOImpl();
    private final ProfileDAO profiledao = new ProfileDAOImpl();
    private final DocumentDAO documentdao = new DocumentDAOImpl();

    /** Chuẩn bị form đăng ký: hạng được duyệt, danh sách ExamDates, bộ lọc địa điểm/ngày. */
    @Override
    public void loadRegisterExamPage(UserDTO user, HttpServletRequest request) {
        RegistrantServletSupport.consumeFlash(request, FLASH_ERROR_ATTR, "error");
        request.setAttribute("sbdPendingDisplay", RegistrantExamSupport.SBD_PENDING_MESSAGE);

        RegistrantProfileContext ctx = RegistrantProfileSupport.loadContext(
                profiledao, documentdao, registrantdao, user);
        var docs = ctx.hasProfile() ? ctx.getDocuments() : List.<RegistrantDocumentView>of();
        List<String> approvedLicences = ctx.hasProfile()
                ? registrantdao.listApprovedDocumentLicenceCodes(ctx.getProfileId())
                : List.of();
        request.setAttribute("approvedDocumentLicenceCodes", approvedLicences);

        List<RegistrantLicenceOption> licences = listLicenceOptionsWithFallback();
        request.setAttribute("licenceClassesList", licences);

        String licenceSelect = resolveLicenceSelect(request, licences, docs, approvedLicences);
        request.setAttribute("selectedLicenceCode", licenceSelect);
        licences.stream()
                .filter(l -> licenceSelect.equals(l.getCode()))
                .findFirst()
                .ifPresent(l -> request.setAttribute("selectedLicence", l));

        boolean selectedLicenceAllowed = licenceSelect != null && !licenceSelect.isBlank()
                && RegistrantDocumentStatusHelper.isLicenceAllowedWithDocuments(
                        licenceSelect, docs, approvedLicences);
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

    /** Dựng URL PRG giữ licence/session và tham số lọc hiện tại. */
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

    /** Submit nguyện vọng ngày thi (ExamDates → RegistrationDates); null nếu OK. */
    @Override
    public String submitRegistration(UserDTO user, HttpServletRequest request) {
        String licenceSelect = request.getParameter("licenceSelect");
        String sessionSelect = request.getParameter("sessionSelect");

        if (RegistrantProfileSupport.isBlank(licenceSelect)) {
            return "Vui lòng chọn hạng bằng lái.";
        }
        if (RegistrantProfileSupport.isBlank(sessionSelect)) {
            return "Vui lòng chọn ngày thi dự kiến.";
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
        List<String> approvedLicences = registrantdao.listApprovedDocumentLicenceCodes(profile.getProfileId());
        String licenceBlock = RegistrantDocumentStatusHelper.licenceClassBlockMessage(
                licenceSelect.trim(), docs, approvedLicences);
        if (licenceBlock != null) {
            return licenceBlock;
        }

        RegistrantExamSessionOption sessionOpt = registrantdao.findExamSessionByCode(sessionSelect);
        if (sessionOpt == null) {
            return "Ngày thi không tồn tại hoặc không còn mở đăng ký.";
        }
        if (sessionOpt.getLicenceClass() != null
                && !sessionOpt.getLicenceClass().equalsIgnoreCase(licenceSelect.trim())) {
            return "Ngày thi không khớp hạng bằng đã chọn.";
        }

        int licenceId = registrantdao.resolveLicenceIdByUiCode(licenceSelect.trim());
        if (licenceId <= 0) {
            return "Hạng GPLX không hợp lệ.";
        }

        String registerError = registrantdao.registerPreferredExamDate(
                profile.getProfileId(), sessionOpt.getSessionId(), licenceId);
        if (registerError != null) {
            return registerError;
        }

        String examLabel = sessionOpt.getExamName() + " — "
                + (sessionOpt.getExamDate() != null ? sessionOpt.getExamDate() : sessionOpt.getExamCode());
        RegistrantAuditHelper.logExamRegistration(request.getSession(), profile.getProfileId(), examLabel);
        return null;
    }

    /** Luôn có hạng để hiển thị card (kèm hint khóa); fallback A1/A/B1 nếu bảng Licence trống. */
    private List<RegistrantLicenceOption> listLicenceOptionsWithFallback() {
        List<RegistrantLicenceOption> fromDb = registrantdao.listOpenLicenceOptions();
        if (fromDb != null && !fromDb.isEmpty()) {
            return fromDb;
        }
        List<RegistrantLicenceOption> fallback = new ArrayList<>();
        for (String code : List.of("A1", "A", "B1")) {
            RegistrantLicenceOption opt = new RegistrantLicenceOption();
            opt.setCode(code);
            opt.setName(switch (code) {
                case "A1" -> "Xe mô tô hai bánh có dung tích xi-lanh đến 125 cm³";
                case "A" -> "Xe mô tô hai bánh có dung tích xi-lanh trên 125 cm³";
                default -> "Xe mô tô ba bánh";
            });
            opt.setExamFee(RegistrantExamSupport.defaultExamFee(code));
            opt.setVehicleType(RegistrantExamSupport.inferVehicleType(code));
            fallback.add(opt);
        }
        return fallback;
    }

    private static String resolveLicenceSelect(HttpServletRequest request,
            List<RegistrantLicenceOption> licences, List<RegistrantDocumentView> docs,
            List<String> approvedLicences) {
        String paramLicence = request.getParameter("licenceSelect");
        if (paramLicence != null && !paramLicence.isBlank()) {
            return paramLicence.trim();
        }
        for (RegistrantLicenceOption option : licences) {
            if (option.getCode() != null
                    && RegistrantDocumentStatusHelper.isLicenceAllowedWithDocuments(
                            option.getCode(), docs, approvedLicences)) {
                return option.getCode().trim();
            }
        }
        return licences.isEmpty() || licences.get(0).getCode() == null
                ? ""
                : licences.get(0).getCode();
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

        List<String> approvedLicences = registrantdao.listApprovedDocumentLicenceCodes(profile.getProfileId());
        String licenceBlock = RegistrantDocumentStatusHelper.licenceClassBlockMessage(
                licenceSelect, docs, approvedLicences);
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
        request.setAttribute("canConfirmRegistration", true);
    }

    /** Cổng tài liệu: Eligible = RegistrationStatus Approved + đủ 4 giấy + có ≥1 hạng đã duyệt kèm hồ sơ. */
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
        List<String> approvedLicences = registrantdao.listApprovedDocumentLicenceCodes(ctx.getProfileId());
        boolean eligible = RegistrantDocumentStatusHelper.isEligibleForExamRegistration(
                registrationStatus, ctx.getDocuments())
                && approvedLicences != null && !approvedLicences.isEmpty();

        request.setAttribute("documentSummary", summary);
        request.setAttribute("canRegisterExam", eligible);
        RegistrantProfileSupport.applyRegistrationStatus(request, registrationStatus);
        request.setAttribute("showProfileApprovedNotice", eligible);

        String blockMsg = null;
        if (!eligible) {
            blockMsg = RegistrantDocumentStatusHelper.examRegistrationBlockMessage(
                    registrationStatus, ctx.getDocuments(), summary);
            if (blockMsg == null
                    && RegistrantDocumentStatusHelper.isEligibleForExamRegistration(
                            registrationStatus, ctx.getDocuments())) {
                blockMsg = "Hồ sơ đã đủ giấy tờ nhưng chưa có hạng nào được ban quản lý duyệt kèm yêu cầu. "
                        + "Vào Quản lý tài liệu → chọn hạng khi Gửi yêu cầu duyệt.";
            }
        }
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
        List<String> approvedLicences = registrantdao.listApprovedDocumentLicenceCodes(ctx.getProfileId());
        List<String> codes = licences.stream()
                .map(RegistrantLicenceOption::getCode)
                .filter(code -> code != null && !code.isBlank())
                .toList();
        request.setAttribute("licenceDocumentAllowed",
                RegistrantDocumentStatusHelper.buildLicenceDocumentAllowedMap(
                        codes, docs, approvedLicences));
        request.setAttribute("licenceDocumentBlockMessages",
                RegistrantDocumentStatusHelper.buildLicenceDocumentBlockMessageMap(
                        codes, docs, approvedLicences));
        request.setAttribute("hasOtherDocuments",
                RegistrantDocumentStatusHelper.hasUploadedOtherDocuments(docs));
        request.setAttribute("basicDocsOnlyLicenceCodes",
                RegistrantDocumentStatusHelper.BASIC_DOCS_ONLY_LICENCE_CODES);

        boolean profileDocsOk = RegistrantDocumentStatusHelper.isEligibleForExamRegistration(
                ctx.getRegistrationStatus(), docs);
        if (!profileDocsOk || approvedLicences == null || approvedLicences.isEmpty()) {
            return;
        }

        String selectedLicence = (String) request.getAttribute("selectedLicenceCode");
        if (selectedLicence != null && !selectedLicence.isBlank()) {
            String licenceBlock = RegistrantDocumentStatusHelper.licenceClassBlockMessage(
                    selectedLicence, docs, approvedLicences);
            if (licenceBlock != null) {
                request.setAttribute("licenceGateMessage", licenceBlock);
            }
        }
    }

    private String checkDocumentEligibility(int profileId) {
        var docs = documentdao.listByProfileId(profileId);
        String registrationStatus = registrantdao.findProfileDocumentRegistrationStatus(profileId);
        List<String> approvedLicences = registrantdao.listApprovedDocumentLicenceCodes(profileId);
        if (RegistrantDocumentStatusHelper.isEligibleForExamRegistration(registrationStatus, docs)
                && approvedLicences != null && !approvedLicences.isEmpty()) {
            return null;
        }
        RegistrantDocumentSummary summary = RegistrantDocumentStatusHelper.summarize(
                docs, documentdao.typeLabels(), registrationStatus);
        String block = RegistrantDocumentStatusHelper.examRegistrationBlockMessage(
                registrationStatus, docs, summary);
        if (block != null) {
            return block;
        }
        return "Chưa có hạng bằng nào được duyệt kèm hồ sơ. Vui lòng gửi yêu cầu duyệt và chọn hạng tại Quản lý tài liệu.";
    }
}

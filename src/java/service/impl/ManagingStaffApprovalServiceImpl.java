package service.impl;

import dao.DocumentDAO;
import dao.ProfileDAO;
import dao.RegistrantDAO;
import dao.impl.DocumentDAOImpl;
import dao.impl.ProfileDAOImpl;
import dao.impl.RegistrantDAOImpl;
import enums.registrant.ProfileRegistrationStatus;
import dto.staff.ManagingStaffApprovalView;
import model.user.Profile;
import dto.registrant.RegistrantDocumentView;
import model.user.User;
import service.ManagingStaffApprovalService;
import util.AuditLogHelper;
import util.registrant.RegistrantDocumentHelper;
import util.registrant.RegistrantDocumentStatusHelper;
import util.registrant.RegistrantProfileSupport;
import jakarta.servlet.http.HttpSession;
import java.sql.Date;
import java.util.List;
import java.util.Map;

/**
 * Duyệt hồ sơ tài liệu thí sinh — tách hồ sơ gốc ({@code #PROFILE_DOC#}) và request bổ sung ({@code #SUPPLEMENT_DOC#}).
 */
public class ManagingStaffApprovalServiceImpl implements ManagingStaffApprovalService {

    private final DocumentDAO documentdao = new DocumentDAOImpl();
    private final ProfileDAO profiledao = new ProfileDAOImpl();
    private final RegistrantDAO registrantdao = new RegistrantDAOImpl();

    @Override
    public List<ManagingStaffApprovalView> listPendingApprovals() {
        return documentdao.listPendingApprovals();
    }

    @Override
    public ManagingStaffApprovalView loadApprovalDetail(int profileId) {
        return loadApprovalDetail(profileId, 0);
    }

    @Override
    public ManagingStaffApprovalView loadApprovalDetail(int profileId, int workflowExamRegistrationId) {
        Profile profile = profiledao.getById(profileId);
        if (profile == null) {
            return null;
        }

        String primaryStatus = registrantdao.findProfileDocumentRegistrationStatus(profileId);
        Integer pendingSupplementErId = registrantdao.findPendingSupplementExamRegistrationId(profileId);
        boolean supplementMode = false;
        int activeErId = workflowExamRegistrationId;

        if (activeErId > 0) {
            supplementMode = true;
        } else if (ProfileRegistrationStatus.PENDING.equalsIgnoreCase(primaryStatus)) {
            supplementMode = false;
        } else if (pendingSupplementErId != null) {
            supplementMode = true;
            activeErId = pendingSupplementErId;
        } else {
            return null;
        }

        List<RegistrantDocumentView> docs = documentdao.listByProfileId(profileId);
        Map<Integer, String> supplementErStatuses = registrantdao.mapSupplementRegistrationStatuses(profileId);
        if (!supplementErStatuses.isEmpty()) {
            documentdao.reconcileOtherDocumentsWithSupplementEr(profileId, supplementErStatuses);
            docs = documentdao.listByProfileId(profileId);
        }
        ManagingStaffApprovalView view = buildBaseView(profile);

        if (supplementMode) {
            if (pendingSupplementErId == null && activeErId <= 0) {
                return null;
            }
            int erId = activeErId > 0 ? activeErId : pendingSupplementErId;
            view.setWorkflowExamRegistrationId(erId);
            view.setSupplementApproval(true);
            RegistrantDocumentStatusHelper.applyDocumentLabelsFromRegistrationStatus(
                    docs, ProfileRegistrationStatus.APPROVED);
            view.setDocumentsByType(RegistrantDocumentHelper.mergeRequiredDocumentSlots(documentdao, docs));
            view.setOtherDocuments(RegistrantDocumentHelper.collectSupplementReviewTargets(docs, erId));
            return view;
        }

        if (!ProfileRegistrationStatus.PENDING.equalsIgnoreCase(primaryStatus)) {
            return null;
        }
        view.setSupplementApproval(false);
        RegistrantDocumentStatusHelper.applyDocumentLabelsFromRegistrationStatus(
                docs, ProfileRegistrationStatus.PENDING);
        view.setDocumentsByType(RegistrantDocumentHelper.mergeRequiredDocumentSlots(documentdao, docs));
        view.setOtherDocuments(RegistrantDocumentHelper.listOtherDocuments(docs));
        return view;
    }

    @Override
    public String reviewDocuments(User staff, int profileId, boolean approved, String staffNote,
            HttpSession session) {
        return reviewDocuments(staff, profileId, 0, approved, staffNote, session);
    }

    public String reviewDocuments(User staff, int profileId, int workflowExamRegistrationId,
            boolean approved, String staffNote, HttpSession session) {
        if (profileId <= 0) {
            return "Hồ sơ không hợp lệ.";
        }
        if (!approved && (staffNote == null || staffNote.isBlank())) {
            return "Vui lòng nhập lý do từ chối.";
        }

        ManagingStaffApprovalView detail = loadApprovalDetail(profileId, workflowExamRegistrationId);
        if (detail == null) {
            return "Không tìm thấy hồ sơ đang chờ duyệt.";
        }

        if (detail.isSupplementApproval()) {
            return reviewSupplementRequest(staff, profileId, detail.getWorkflowExamRegistrationId(),
                    approved, staffNote, session);
        }
        return reviewPrimaryRequest(staff, profileId, approved, staffNote, session);
    }

    private String reviewPrimaryRequest(User staff, int profileId, boolean approved, String staffNote,
            HttpSession session) {
        if (!documentdao.reviewProfileDocuments(profileId, approved, staffNote)) {
            return "Không thể cập nhật trạng thái duyệt.";
        }

        List<RegistrantDocumentView> updatedDocs = documentdao.listByProfileId(profileId);
        String newStatus = approved ? ProfileRegistrationStatus.APPROVED : ProfileRegistrationStatus.REJECTED;
        RegistrantProfileSupport.updateRegistrationStatus(
                profileId, newStatus, updatedDocs, registrantdao);

        logReview(session, profileId, approved, staffNote, false);
        return null;
    }

    private String reviewSupplementRequest(User staff, int profileId, int supplementExamRegistrationId,
            boolean approved, String staffNote, HttpSession session) {
        if (!documentdao.reviewSupplementDocuments(profileId, supplementExamRegistrationId, approved, staffNote)) {
            return "Không thể cập nhật trạng thái duyệt hồ sơ bổ sung.";
        }

        String newStatus = approved ? ProfileRegistrationStatus.APPROVED : ProfileRegistrationStatus.REJECTED;
        String notes = approved
                ? RegistrantDocumentHelper.buildSupplementExamRegistrationNotes(
                        "Ban quản lý đã duyệt hồ sơ bổ sung.")
                : RegistrantDocumentHelper.buildSupplementExamRegistrationNotes(
                        "Từ chối: " + (staffNote != null ? staffNote.trim() : "Không đạt yêu cầu"));
        if (!registrantdao.syncSupplementDocumentRegistration(supplementExamRegistrationId, newStatus, notes)) {
            return "Không thể cập nhật trạng thái request bổ sung.";
        }

        logReview(session, profileId, approved, staffNote, true);
        return null;
    }

    private void logReview(HttpSession session, int profileId, boolean approved, String staffNote,
            boolean supplement) {
        String kind = supplement ? " hồ sơ bổ sung" : " hồ sơ tài liệu";
        String action = approved ? "APPROVE on Document" : "REJECT on Document";
        String details = approved
                ? "Ban quản lý duyệt" + kind + " HS-" + profileId
                : "Ban quản lý từ chối" + kind + " HS-" + profileId;
        AuditLogHelper.persistChange(session, action, details, "Chờ duyệt",
                approved ? "Đã duyệt" : "Từ chối", staffNote, profileId);
    }

    private ManagingStaffApprovalView buildBaseView(Profile profile) {
        ManagingStaffApprovalView view = new ManagingStaffApprovalView();
        view.setId(profile.getId());
        view.setUserId(profile.getUserId());
        view.setCode("HS-" + profile.getId());
        view.setFullName(profile.getFullName());
        view.setCccd(profile.getGovIdNo());
        Date dob = profile.getDateOfBirth();
        view.setDob(dob != null ? dob.toString() : "—");
        view.setGender(profile.isGender() ? "Nữ" : "Nam");
        view.setPhone(profile.getPhoneNo());
        view.setLicenseClass(registrantdao.resolveLatestLicenceClassByProfileId(profile.getId()));
        view.setType("student");
        view.setTypeName("Thí sinh");
        view.setRegisterDate("—");
        return view;
    }
}

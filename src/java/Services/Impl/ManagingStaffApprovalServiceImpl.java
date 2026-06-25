package Services.Impl;

import DAO.DocumentDAO;
import DAO.ProfileDAO;
import DAO.RegistrantDAO;
import DAO.Impl.DocumentDAOImpl;
import DAO.Impl.ProfileDAOImpl;
import DAO.Impl.RegistrantDAOImpl;
import Constants.ProfileRegistrationStatus;
import Models.ManagingStaffApprovalView;
import Models.Profile;
import Models.RegistrantDocumentView;
import Models.User;
import Services.ManagingStaffApprovalService;
import Utils.AuditLogHelper;
import Utils.RegistrantDocumentStatusHelper;
import Utils.RegistrantProfileRegistrationSync;
import jakarta.servlet.http.HttpSession;
import java.sql.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Duyệt hồ sơ tài liệu thí sinh cho ban quản lý.
 */
public class ManagingStaffApprovalServiceImpl implements ManagingStaffApprovalService {

    private final DocumentDAO documentDAO = new DocumentDAOImpl();
    private final ProfileDAO profileDAO = new ProfileDAOImpl();
    private final RegistrantDAO registrantDAO = new RegistrantDAOImpl();

    @Override
    public List<ManagingStaffApprovalView> listPendingApprovals() {
        return documentDAO.listPendingApprovals();
    }

    @Override
    public ManagingStaffApprovalView loadApprovalDetail(int profileId) {
        Profile profile = profileDAO.getById(profileId);
        if (profile == null) {
            return null;
        }

        List<RegistrantDocumentView> docs = documentDAO.listByProfileId(profileId);
        String status = registrantDAO.findProfileDocumentRegistrationStatus(profileId);
        if (!ProfileRegistrationStatus.PENDING.equalsIgnoreCase(status)) {
            return null;
        }

        ManagingStaffApprovalView view = new ManagingStaffApprovalView();
        view.setId(profileId);
        view.setUserId(profile.getUserId());
        view.setCode("HS-" + profileId);
        view.setFullName(profile.getFullName());
        view.setCccd(profile.getGovIdNo());
        Date dob = profile.getDateOfBirth();
        view.setDob(dob != null ? dob.toString() : "—");
        view.setGender(profile.isGender() ? "Nữ" : "Nam");
        view.setPhone(profile.getPhoneNo());
        view.setLicenseClass(registrantDAO.resolveLatestLicenceClassByProfileId(profileId));
        view.setType("student");
        view.setTypeName("Thí sinh");
        view.setRegisterDate("—");
        RegistrantDocumentStatusHelper.applyDocumentLabelsFromRegistrationStatus(
                docs, ProfileRegistrationStatus.PENDING);
        view.setDocumentsByType(mergeDocumentSlots(docs));
        view.setOtherDocuments(listOtherDocuments(docs));
        return view;
    }

    @Override
    public String reviewDocuments(User staff, int profileId, boolean approved, String staffNote, HttpSession session) {
        if (profileId <= 0) {
            return "Hồ sơ không hợp lệ.";
        }
        if (!approved && (staffNote == null || staffNote.isBlank())) {
            return "Vui lòng nhập lý do từ chối.";
        }

        ManagingStaffApprovalView detail = loadApprovalDetail(profileId);
        if (detail == null) {
            return "Không tìm thấy hồ sơ đang chờ duyệt.";
        }

        if (!documentDAO.reviewProfileDocuments(profileId, approved, staffNote)) {
            return "Không thể cập nhật trạng thái duyệt.";
        }

        List<RegistrantDocumentView> updatedDocs = documentDAO.listByProfileId(profileId);
        String newStatus = approved ? ProfileRegistrationStatus.APPROVED : ProfileRegistrationStatus.REJECTED;
        RegistrantProfileRegistrationSync.updateRegistrationStatus(
                profileId, newStatus, updatedDocs, registrantDAO);

        String action = approved ? "APPROVE on Document" : "REJECT on Document";
        String details = approved
                ? "Ban quản lý duyệt hồ sơ tài liệu HS-" + profileId
                : "Ban quản lý từ chối hồ sơ tài liệu HS-" + profileId;
        AuditLogHelper.persistChange(session, action, details, "Chờ duyệt",
                approved ? "Đã duyệt" : "Từ chối", staffNote, profileId);
        return null;
    }

    private Map<String, RegistrantDocumentView> mergeDocumentSlots(List<RegistrantDocumentView> docs) {
        Map<String, RegistrantDocumentView> slots = documentDAO.defaultDocumentSlots();
        for (RegistrantDocumentView doc : docs) {
            if (DocumentDAOImpl.isOtherType(doc.getDocumentType())) {
                continue;
            }
            if (slots.containsKey(doc.getDocumentType())) {
                slots.put(doc.getDocumentType(), doc);
            }
        }
        return slots;
    }

    private static List<RegistrantDocumentView> listOtherDocuments(List<RegistrantDocumentView> docs) {
        List<RegistrantDocumentView> others = new java.util.ArrayList<>();
        for (RegistrantDocumentView doc : docs) {
            if (DocumentDAOImpl.isOtherType(doc.getDocumentType())) {
                others.add(doc);
            }
        }
        return others;
    }
}

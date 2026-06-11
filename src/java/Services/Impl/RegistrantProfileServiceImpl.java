package Services.Impl;

import DAO.CandidateDocumentDAO;
import DAO.ExamRegistrationDAO;
import DAO.LicenseTypeDAO;
import DAO.PersonDAO;
import DAO.UserDAO;
import DAO.Impl.CandidateDocumentDAOImpl;
import DAO.Impl.ExamRegistrationDAOImpl;
import DAO.Impl.LicenseTypeDAOImpl;
import DAO.Impl.PersonDAOImpl;
import DAO.Impl.UserDAOImpl;
import Models.Person;
import Models.ProfileDocumentItem;
import Models.User;
import Services.RegistrantProfileService;
import jakarta.servlet.http.HttpServletRequest;
import java.sql.Date;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/** Hồ sơ Person: form thông tin, checklist giấy tờ, trạng thái duyệt (Approved/Pending/Rejected). */
public class RegistrantProfileServiceImpl implements RegistrantProfileService {

    private final PersonDAO personDAO = new PersonDAOImpl();
    private final UserDAO userDAO = new UserDAOImpl();
    private final CandidateDocumentDAO documentDAO = new CandidateDocumentDAOImpl();
    private final LicenseTypeDAO licenseTypeDAO = new LicenseTypeDAOImpl();
    private final ExamRegistrationDAO examRegistrationDAO = new ExamRegistrationDAOImpl();

    /**
     * Nạp dữ liệu hồ sơ lên request: danh sách hạng GPLX, thông tin Person,
     * checklist 4 loại giấy tờ và widget trạng thái duyệt (Approved/Pending/Rejected).
     */
    @Override
    public void populateProfile(HttpServletRequest request, User user) {
        request.setAttribute("licenseTypes", licenseTypeDAO.findAll());

        Integer personId = user.getPersonId();
        if (personId == null) {
            applyEmptyProfile(request, user);
            return;
        }

        Person person = personDAO.getById(personId);
        if (person == null) {
            applyEmptyProfile(request, user);
            return;
        }

        applyPersonAttributes(request, person);
        request.setAttribute("licenceClass", examRegistrationDAO.findLatestLicenseCodeByPersonId(personId));
        applyStatusWidgets(request, person, documentDAO.countGroupedByType(personId));
    }

    /**
     * Validate và lưu Person từ form.
     * @return null nếu thành công; chuỗi lỗi tiếng Việt nếu validation/DB thất bại
     */
    @Override
    public String saveProfile(HttpServletRequest request, User user) {
        String fullName = trim(request.getParameter("fullName"));
        String dob = trim(request.getParameter("dob"));
        String genderValue = trim(request.getParameter("gender"));
        String phone = trim(request.getParameter("phone"));
        String email = trim(request.getParameter("email"));
        String address = trim(request.getParameter("address"));
        String govIdNo = trim(request.getParameter("idCard"));

        if (fullName == null || fullName.isEmpty()) {
            return "Vui lòng nhập họ và tên.";
        }
        if (dob == null || dob.isEmpty()) {
            return "Vui lòng chọn ngày sinh.";
        }
        if (phone == null || phone.isEmpty()) {
            return "Vui lòng nhập số điện thoại.";
        }

        Person person;
        Integer personId = user.getPersonId();

        if (personId != null) {
            person = personDAO.getById(personId);
            if (person == null) {
                return "Không tìm thấy hồ sơ cá nhân.";
            }
        } else {
            person = new Person();
            person.setApprovalStatus("Pending");
            person.setIsWalkIn(false);
        }

        person.setFullName(fullName);
        person.setDateOfBirth(Date.valueOf(dob));
        person.setGender("Nữ".equalsIgnoreCase(genderValue));
        person.setPhoneNo(phone);
        person.setEmail(emptyToNull(email));
        person.setAddress(emptyToNull(address));
        person.setGovIdNo(emptyToNull(govIdNo));

        boolean saved;
        if (personId != null) {
            saved = personDAO.update(person);
        } else {
            saved = personDAO.insert(person);
            if (saved) {
                userDAO.updatePersonId(user.getId(), person.getId());
            }
        }

        if (!saved) {
            return "Không thể lưu hồ sơ. Vui lòng thử lại.";
        }

        return null;
    }

    /** Tải lại User sau khi gán personId mới — cập nhật session trong servlet. */
    @Override
    public User reloadUser(int userId) {
        return userDAO.getById(userId);
    }

    /** Trạng thái khi User chưa liên kết Person — khuyến khích tạo hồ sơ. */
    private void applyEmptyProfile(HttpServletRequest request, User user) {
        request.setAttribute("registrantName", user.getUsername());
        request.setAttribute("hasProfile", false);
        request.setAttribute("profileStatusBadge", "Chưa có hồ sơ");
        request.setAttribute("profileStatusBadgeClass", "neutral");
        request.setAttribute("profileStatusMessage",
                "Bạn chưa tạo hồ sơ cá nhân. Nhấn Chỉnh sửa để nhập thông tin và lưu hồ sơ.");
        request.setAttribute("documentList", buildDocumentChecklist(Map.of(), "Pending"));
        request.setAttribute("showRejectionAlert", false);
        request.setAttribute("profileRoleLabel", "Thí sinh");
    }

    private void applyPersonAttributes(HttpServletRequest request, Person person) {
        request.setAttribute("hasProfile", true);
        request.setAttribute("registrantName", person.getFullName());
        request.setAttribute("birthday", person.getDateOfBirth() != null ? person.getDateOfBirth().toString() : "");
        request.setAttribute("gender", person.isGender() ? "Nữ" : "Nam");
        request.setAttribute("phone", person.getPhoneNo());
        request.setAttribute("email", person.getEmail() != null ? person.getEmail() : "");
        request.setAttribute("address", person.getAddress() != null ? person.getAddress() : "");
        request.setAttribute("idCardNumber", person.getGovIdNo() != null ? person.getGovIdNo() : "");

        String photoUrl = person.getPhotoUrl();
        if (photoUrl != null && !photoUrl.isBlank()) {
            request.setAttribute("photoUrl", photoUrl);
        }
    }

    /**
     * Map approvalStatus → badge/message; build checklist giấy tờ với trạng thái từng mục.
     * Rejected + rejectionReason → hiển thị alert bổ sung.
     */
    private void applyStatusWidgets(HttpServletRequest request, Person person, Map<String, Integer> docCounts) {
        String approvalStatus = person.getApprovalStatus() != null ? person.getApprovalStatus() : "Pending";

        switch (approvalStatus) {
            case "Approved" -> {
                request.setAttribute("profileStatusBadge", "Đã duyệt");
                request.setAttribute("profileStatusBadgeClass", "success");
                request.setAttribute("profileStatusMessage",
                        "Hồ sơ của bạn đã được duyệt. Bạn có thể tiếp tục đăng ký đợt thi.");
            }
            case "Rejected" -> {
                request.setAttribute("profileStatusBadge", "Bị từ chối");
                request.setAttribute("profileStatusBadgeClass", "pending");
                request.setAttribute("profileStatusMessage",
                        "Hồ sơ cần được bổ sung hoặc chỉnh sửa trước khi xét duyệt lại.");
            }
            default -> {
                request.setAttribute("profileStatusBadge", "Chờ duyệt");
                request.setAttribute("profileStatusBadgeClass", "info");
                request.setAttribute("profileStatusMessage",
                        buildPendingMessage(docCounts));
            }
        }

        boolean showAlert = "Rejected".equals(approvalStatus)
                && person.getRejectionReason() != null
                && !person.getRejectionReason().isBlank();
        request.setAttribute("showRejectionAlert", showAlert);
        request.setAttribute("rejectionReason", person.getRejectionReason());
        request.setAttribute("documentList", buildDocumentChecklist(docCounts, approvalStatus));

        String licenceClass = (String) request.getAttribute("licenceClass");
        request.setAttribute("profileRoleLabel", licenceClass != null ? "Thí sinh hạng " + licenceClass : "Thí sinh");
    }

    private String buildPendingMessage(Map<String, Integer> docCounts) {
        int healthCount = docCounts.getOrDefault("Health_Cert", 0);
        if (healthCount == 0) {
            return "Hãy bổ sung Giấy khám sức khỏe hợp lệ để đủ điều kiện xét duyệt đợt thi.";
        }
        return "Hồ sơ của bạn đang chờ cán bộ xét duyệt.";
    }

    private List<ProfileDocumentItem> buildDocumentChecklist(Map<String, Integer> docCounts, String approvalStatus) {
        List<ProfileDocumentItem> items = new ArrayList<>();

        items.add(buildDocumentItem("Ảnh chân dung 3x4", "Photo", docCounts, approvalStatus));
        items.add(buildIdCardItem("Ảnh mặt trước CCCD", docCounts, approvalStatus, 1));
        items.add(buildIdCardItem("Ảnh mặt sau CCCD", docCounts, approvalStatus, 2));
        items.add(buildDocumentItem("Giấy khám sức khỏe", "Health_Cert", docCounts, approvalStatus));

        return items;
    }

    private ProfileDocumentItem buildDocumentItem(String label, String type, Map<String, Integer> docCounts, String approvalStatus) {
        ProfileDocumentItem item = new ProfileDocumentItem();
        int count = docCounts.getOrDefault(type, 0);
        boolean uploaded = count > 0;

        item.setLabel(label);
        item.setUploaded(uploaded);
        applyDocumentStatus(item, uploaded, approvalStatus, "Health_Cert".equals(type));
        return item;
    }

    private ProfileDocumentItem buildIdCardItem(String label, Map<String, Integer> docCounts, String approvalStatus, int requiredCount) {
        ProfileDocumentItem item = new ProfileDocumentItem();
        int idCount = docCounts.getOrDefault("ID_Card", 0);
        boolean uploaded = idCount >= requiredCount;

        item.setLabel(label);
        item.setUploaded(uploaded);
        applyDocumentStatus(item, uploaded, approvalStatus, false);
        return item;
    }

    private void applyDocumentStatus(ProfileDocumentItem item, boolean uploaded, String approvalStatus, boolean showUploadWhenMissing) {
        if (!uploaded) {
            item.setDotClass("pending");
            item.setStatusClass("neutral");
            item.setStatusLabel("Chưa có");
            item.setShowUploadLink(showUploadWhenMissing);
            return;
        }

        item.setDotClass("checked");
        item.setShowUploadLink(false);

        if ("Approved".equals(approvalStatus)) {
            item.setStatusClass("success");
            item.setStatusLabel("Đã duyệt");
        } else if ("Rejected".equals(approvalStatus)) {
            item.setStatusClass("pending");
            item.setStatusLabel("Cần bổ sung");
        } else {
            item.setStatusClass("info");
            item.setStatusLabel("Đã tải lên");
        }
    }

    private String trim(String value) {
        return value != null ? value.trim() : null;
    }

    private String emptyToNull(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }
}

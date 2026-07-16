package managingstaff.dto;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/** View model dedicated to the Managing Staff dossier screens. */
public class DossierDTO {

    private UserView user;
    private ProfileView profile;
    private int registrationId;
    private String status = "Draft";
    private String notes;
    private String licenceClass;
    private final Map<String, DocumentView> documents = new LinkedHashMap<>();

    public UserView getUser() { return user; }
    public void setUser(UserView user) { this.user = user; }
    public ProfileView getProfile() { return profile; }
    public void setProfile(ProfileView profile) { this.profile = profile; }
    public int getRegistrationId() { return registrationId; }
    public void setRegistrationId(int registrationId) { this.registrationId = registrationId; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
    public String getLicenceClass() { return licenceClass; }
    public void setLicenceClass(String licenceClass) { this.licenceClass = licenceClass; }
    public String getLicenceDisplayClass() { return licenceClass; }
    public Map<String, DocumentView> getDocuments() { return documents; }

    public int getDocumentCount() {
        int count = 0;
        if (documents.containsKey("PORTRAIT")) count++;
        if (documents.containsKey("ID_FRONT")) count++;
        if (documents.containsKey("ID_BACK")) count++;
        if (documents.containsKey("HEALTH_CERTIFICATE")) count++;
        return count;
    }

    public int getRequiredDocumentTotal() { return 4; }

    public String getStatusLabel() {
        if ("WaitingExam".equals(status)) return "Chờ lịch thi chính thức";
        if ("OfficialScheduled".equals(status)) return "Đã có lịch thi chính thức";
        return switch (status == null ? "" : status) {
            case "Draft" -> "Bản nháp";
            case "Pending", "Submitted" -> "Chờ duyệt";
            case "NeedSupplement" -> "Cần bổ sung";
            case "Approved" -> "Đã duyệt";
            case "Rejected" -> "Đã từ chối";
            case "Present", "CheckedIn" -> "Đang tham gia thi";
            case "Completed" -> "Đã thi xong";
            default -> status == null || status.isBlank() ? "Bản nháp" : status;
        };
    }

    public String getStatusKey() {
        if ("WaitingExam".equals(status) || "OfficialScheduled".equals(status)) return "success";
        return switch (status == null ? "" : status) {
            case "Approved", "Present", "CheckedIn", "Completed" -> "success";
            case "Rejected" -> "danger";
            case "NeedSupplement", "Pending", "Submitted" -> "warning";
            default -> "info";
        };
    }

    public String getSourceLabel() {
        if (notes == null) return "Chưa xác định";
        if (notes.contains("SOURCE=STAFF")) return "Tiếp nhận tại quầy";
        if (notes.contains("SOURCE=SELF")) return "Đăng ký trực tuyến";
        if (notes.toLowerCase(Locale.ROOT).contains("tự do")) return "Hồ sơ tự do";
        return "Dữ liệu hệ thống";
    }

    public boolean isReviewable() {
        return registrationId > 0 && Set.of(
                "Draft", "Pending", "Submitted", "NeedSupplement")
                .contains(status);
    }

    public String getReviewMessage() {
        if (notes == null || notes.isBlank()) return "";
        int marker = notes.lastIndexOf("MESSAGE=");
        if (marker < 0) return notes;
        String message = notes.substring(marker + "MESSAGE=".length());
        int separator = message.indexOf(';');
        return separator >= 0 ? message.substring(0, separator) : message;
    }

    public boolean isComplete() { return getMissingRequiredDocumentLabels().isEmpty(); }

    public List<String> getMissingRequiredDocumentLabels() {
        List<String> missing = new ArrayList<>();
        if (!documents.containsKey("PORTRAIT")) missing.add("Ảnh chân dung 3x4");
        if (!documents.containsKey("ID_FRONT")) missing.add("CCCD mặt trước");
        if (!documents.containsKey("ID_BACK")) missing.add("CCCD mặt sau");
        if (!documents.containsKey("HEALTH_CERTIFICATE")) missing.add("Giấy khám sức khỏe");
        return missing;
    }

    public boolean isMotorcycleLicence() {
        String value = licenceClass == null ? "" : licenceClass.trim().toUpperCase(Locale.ROOT);
        return Set.of("A1", "A", "B1").contains(value);
    }

    public boolean isPendingReview() {
        return "Pending".equals(status) || "Submitted".equals(status);
    }

    public boolean isReminderEligible() {
        return isPendingReview() || "Rejected".equals(status);
    }

    public boolean isGraduationCertificateRequired() { return false; }

    public static final class UserView {
        private int id;
        private String username;
        private String email;
        private boolean active;

        public int getId() { return id; }
        public void setId(int id) { this.id = id; }
        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
        public boolean isActive() { return active; }
        public void setActive(boolean active) { this.active = active; }
    }

    public static final class ProfileView {
        private int id;
        private int userId;
        private String fullName;
        private Timestamp dateOfBirth;
        private String phoneNo;
        private String gender;
        private String govIdNo;
        private String address;

        public int getId() { return id; }
        public void setId(int id) { this.id = id; }
        public int getUserId() { return userId; }
        public void setUserId(int userId) { this.userId = userId; }
        public String getFullName() { return fullName; }
        public void setFullName(String fullName) { this.fullName = fullName; }
        public Timestamp getDateOfBirth() { return dateOfBirth; }
        public void setDateOfBirth(Timestamp dateOfBirth) { this.dateOfBirth = dateOfBirth; }
        public String getPhoneNo() { return phoneNo; }
        public void setPhoneNo(String phoneNo) { this.phoneNo = phoneNo; }
        public String getGender() { return gender; }
        public void setGender(String gender) { this.gender = gender; }
        public String getSex() { return gender; }
        public String getGovIdNo() { return govIdNo; }
        public void setGovIdNo(String govIdNo) { this.govIdNo = govIdNo; }
        public String getAddress() { return address; }
        public void setAddress(String address) { this.address = address; }
    }

    public static final class DocumentView {
        private int documentId;
        private String documentType;
        private String documentUrl;
        private String notes;
        private int profileId;

        public DocumentView() {}

        public DocumentView(int documentId, String documentType, String documentUrl,
                String notes, int profileId) {
            this.documentId = documentId;
            this.documentType = documentType;
            this.documentUrl = documentUrl;
            this.notes = notes;
            this.profileId = profileId;
        }

        public int getDocumentId() { return documentId; }
        public String getDocumentType() { return documentType; }
        public String getDocumentUrl() { return documentUrl; }
        public String getNotes() { return notes; }
        public int getProfileId() { return profileId; }
    }
}

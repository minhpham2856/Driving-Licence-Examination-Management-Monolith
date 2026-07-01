package dto;
import enums.RegistrationStatus;
import model.Document;
import model.Profile;
import model.User;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
public class DossierDTO {
    private User user;
    private Profile profile;
    private int registrationId;
    private String status = RegistrationStatus.BAN_NHAP.getDisplayName();
    private String notes;
    private String licenceClass;
    private final Map<String, Document> documents = new LinkedHashMap<>();
    public User getUser() {
        return user;
    }
    public void setUser(User user) {
        this.user = user;
    }
    public Profile getProfile() {
        return profile;
    }
    public void setProfile(Profile profile) {
        this.profile = profile;
    }
    public int getRegistrationId() {
        return registrationId;
    }
    public void setRegistrationId(int registrationId) {
        this.registrationId = registrationId;
    }
    public String getStatus() {
        return status;
    }
    public void setStatus(String status) {
        this.status = RegistrationStatus.normalizeDisplayName(status);
    }
    public String getNotes() {
        return notes;
    }
    public void setNotes(String notes) {
        this.notes = notes;
    }
    public String getLicenceClass() {
        return licenceClass;
    }
    public void setLicenceClass(String licenceClass) {
        this.licenceClass = licenceClass;
    }
    public String getLicenceDisplayClass() {
        return switch (normalisedLicenceClass()) {
            case "A" -> "A2";
            case "B" -> "B2";
            default -> licenceClass;
        };
    }
    public Map<String, Document> getDocuments() {
        return documents;
    }
    public int getDocumentCount() {
        int count = 0;
        if (documents.containsKey("PORTRAIT")) {
            count++;
        }
        if (documents.containsKey("ID_FRONT")) {
            count++;
        }
        if (documents.containsKey("ID_BACK")) {
            count++;
        }
        if (documents.containsKey("HEALTH_CERTIFICATE")) {
            count++;
        }
        if (isGraduationCertificateRequired() && documents.containsKey("GRADUATION_CERTIFICATE")) {
            count++;
        }
        return count;
    }
    public int getRequiredDocumentTotal() {
        return isGraduationCertificateRequired() ? 5 : 4;
    }
    public String getStatusLabel() {
        return RegistrationStatus.normalizeDisplayName(status);
    }
    public String getStatusKey() {
        return RegistrationStatus.badgeKey(status);
    }
    public String getSourceLabel() {
        if (notes == null) {
            return "Chưa xác định";
        }
        if (notes.contains("SOURCE=STAFF")) {
            return "Tiếp nhận tại quầy";
        }
        if (notes.contains("SOURCE=SELF")) {
            return "Đăng ký trực tuyến";
        }
        if (notes.toLowerCase().contains("tự do")) {
            return "Hồ sơ tự do";
        }
        return "Dữ liệu hệ thống";
    }
    public boolean isReviewable() {
        return registrationId > 0 && RegistrationStatus.isReviewable(status);
    }
    public String getReviewMessage() {
        if (notes == null || notes.isBlank()) {
            return "";
        }
        int marker = notes.lastIndexOf("MESSAGE=");
        if (marker < 0) {
            return notes;
        }
        String message = notes.substring(marker + "MESSAGE=".length());
        int separator = message.indexOf(';');
        return separator >= 0 ? message.substring(0, separator) : message;
    }
    public boolean isComplete() {
        return getMissingRequiredDocumentLabels().isEmpty();
    }
    public List<String> getMissingRequiredDocumentLabels() {
        List<String> missing = new ArrayList<>();
        if (!documents.containsKey("PORTRAIT")) {
            missing.add("Ảnh chân dung 3x4");
        }
        if (!documents.containsKey("ID_FRONT")) {
            missing.add("CCCD mặt trước");
        }
        if (!documents.containsKey("ID_BACK")) {
            missing.add("CCCD mặt sau");
        }
        if (!documents.containsKey("HEALTH_CERTIFICATE")) {
            missing.add("Giấy khám sức khỏe");
        }
        if (isGraduationCertificateRequired() && !documents.containsKey("GRADUATION_CERTIFICATE")) {
            missing.add("Giấy tốt nghiệp / chứng chỉ đào tạo");
        }
        return missing;
    }
    public boolean isMotorcycleLicence() {
        String value = normalisedLicenceClass();
        return "A1".equals(value) || "A2".equals(value) || "A".equals(value);
    }
    public boolean isGraduationCertificateRequired() {
        return licenceClass != null && !licenceClass.isBlank() && !isMotorcycleLicence();
    }
    private String normalisedLicenceClass() {
        return licenceClass == null ? "" : licenceClass.trim().toUpperCase(java.util.Locale.ROOT);
    }
}

package DTOs;

import Models.Document;
import Models.Profile;
import Models.User;
import java.util.LinkedHashMap;
import java.util.Map;

public class DossierDTO {
    private User user;
    private Profile profile;
    private int registrationId;
    private String status = "Draft";
    private String notes;
    private String licenceClass;
    private final Map<String, Document> documents = new LinkedHashMap<>();

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
    public Profile getProfile() { return profile; }
    public void setProfile(Profile profile) { this.profile = profile; }
    public int getRegistrationId() { return registrationId; }
    public void setRegistrationId(int registrationId) { this.registrationId = registrationId; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
    public String getLicenceClass() { return licenceClass; }
    public void setLicenceClass(String licenceClass) { this.licenceClass = licenceClass; }
    public Map<String, Document> getDocuments() { return documents; }
    public int getDocumentCount() {
        int count = 0;
        if (documents.containsKey("PORTRAIT")) count++;
        if (documents.containsKey("ID_FRONT")) count++;
        if (documents.containsKey("ID_BACK")) count++;
        if (documents.containsKey("HEALTH_CERTIFICATE")) count++;
        return count;
    }
    public String getStatusLabel() {
        return switch (status == null ? "" : status) {
            case "Draft" -> "Bản nháp";
            case "Pending", "Submitted" -> "Chờ duyệt";
            case "NeedSupplement" -> "Cần bổ sung";
            case "Approved" -> "Đã duyệt";
            case "Rejected" -> "Đã từ chối";
            case "Present" -> "Đang tham gia thi";
            case "Completed" -> "Đã hoàn thành thi";
            default -> status;
        };
    }
    public String getStatusKey() {
        return switch (status == null ? "" : status) {
            case "Approved" -> "success";
            case "Rejected" -> "danger";
            case "NeedSupplement", "Pending", "Submitted" -> "warning";
            case "Present", "Completed" -> "success";
            default -> "info";
        };
    }
    public String getSourceLabel() {
        if (notes == null) return "Chưa xác định";
        if (notes.contains("SOURCE=STAFF")) return "Tiếp nhận tại quầy";
        if (notes.contains("SOURCE=SELF")) return "Đăng ký trực tuyến";
        if (notes.toLowerCase().contains("tự do")) return "Hồ sơ tự do";
        return "Dữ liệu hệ thống";
    }
    public boolean isReviewable() {
        return registrationId > 0
                && java.util.Set.of("Draft", "Pending", "Submitted", "NeedSupplement", "Rejected")
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
    public boolean isComplete() {
        return documents.containsKey("PORTRAIT")
                && documents.containsKey("ID_FRONT")
                && documents.containsKey("ID_BACK")
                && documents.containsKey("HEALTH_CERTIFICATE");
    }
}

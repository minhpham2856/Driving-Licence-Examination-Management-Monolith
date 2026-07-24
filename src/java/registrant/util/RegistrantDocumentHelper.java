package registrant.util;

import registrant.dao.DocumentDAO;
import registrant.dao.impl.DocumentDAOImpl;
import registrant.dto.RegistrantDocumentView;
import jakarta.servlet.http.Part;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Tiện ích workflow tài liệu hồ sơ thí sinh — marker Notes và validate upload.
 * <p>
 * Hằng số {@code #PROFILE_DOC#}, {@code #SUPPLEMENT_DOC#}, {@code #LICENCE_DOC#}, {@code #SUPPLEMENT_ER#id#}
 * trên {@code ExamRegistration}/{@code Document}; giới hạn 5MB và loại bắt buộc Portrait/IdFront/IdBack/HealthCertificate/Other.
 */
public final class RegistrantDocumentHelper {

    // --- Workflow markers trên ExamRegistration / Document.Notes ---

    public static final String MARK_PROFILE_DOC = "#PROFILE_DOC#";
    public static final String MARK_SUPPLEMENT_DOC = "#SUPPLEMENT_DOC#";
    /** ER xin duyệt thêm hạng (tái sử dụng 4 giấy đã duyệt). */
    public static final String MARK_LICENCE_DOC = "#LICENCE_DOC#";
    /** Prefix gắn Document.Notes với ExamRegistrationId bổ sung: {@code #SUPPLEMENT_ER#42#}. */
    public static final String MARK_SUPPLEMENT_ER_PREFIX = "#SUPPLEMENT_ER#";

    // --- Upload validation ---

    public static final long MAX_UPLOAD_BYTES = 5L * 1024 * 1024;

    private static final Set<String> ALLOWED_MANDATORY_TYPES = Set.of(
            "Portrait", "IdFront", "IdBack", "HealthCertificate", "Other");

    private RegistrantDocumentHelper() {
    }

    /** Đảm bảo notes ER hồ sơ gốc luôn có #PROFILE_DOC# (idempotent). */
    public static String ensureProfileDocMarker(String notes) {
        String base = notes != null ? notes.trim() : "";
        if (base.contains(MARK_PROFILE_DOC)) {
            return base;
        }
        if (base.isBlank()) {
            return MARK_PROFILE_DOC;
        }
        return MARK_PROFILE_DOC + " " + base;
    }

    /** Ghép notes khi tạo ER bổ sung: #SUPPLEMENT_DOC# + message. */
    public static String buildSupplementExamRegistrationNotes(String message) {
        String body = message != null && !message.isBlank() ? message.trim() : "Yêu cầu duyệt hồ sơ bổ sung.";
        return MARK_SUPPLEMENT_DOC + " " + body;
    }

    /** Ghép notes xin duyệt thêm hạng (tái sử dụng hồ sơ đã duyệt). */
    public static String buildLicenceDocExamRegistrationNotes(String message) {
        String body = message != null && !message.isBlank() ? message.trim() : "Xin duyệt hạng với hồ sơ đã có.";
        return MARK_LICENCE_DOC + " " + body;
    }

    /** Mã hóa: #SUPPLEMENT_ER#<examRegistrationId># */
    public static String encodeSupplementErMarker(int examRegistrationId) {
        if (examRegistrationId <= 0) {
            return "";
        }
        return MARK_SUPPLEMENT_ER_PREFIX + examRegistrationId + "#";
    }

    /** Giải mã ExamRegistrationId từ marker Notes, vd. "... | #SUPPLEMENT_ER#42#" → 42. */
    public static Integer parseSupplementErId(String notes) {
        if (notes == null || !notes.contains(MARK_SUPPLEMENT_ER_PREFIX)) {
            return null;
        }
        int start = notes.indexOf(MARK_SUPPLEMENT_ER_PREFIX) + MARK_SUPPLEMENT_ER_PREFIX.length();
        int end = notes.indexOf('#', start);
        if (end <= start) {
            return null;
        }
        try {
            int id = Integer.parseInt(notes.substring(start, end).trim());
            return id > 0 ? id : null;
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    /** Gắn marker ER bổ sung vào Notes (không trùng nếu đã có). */
    public static String appendSupplementErMarker(String notes, int examRegistrationId) {
        if (examRegistrationId <= 0) {
            return notes;
        }
        String marker = encodeSupplementErMarker(examRegistrationId);
        if (notes == null || notes.isBlank()) {
            return marker;
        }
        if (notes.contains(marker)) {
            return notes;
        }
        return notes.trim() + " | " + marker;
    }

    // --- Slot map: 4 giấy bắt buộc + Hồ sơ khác ---

    /** Ghép slot UI mặc định với tài liệu DB — mỗi DocumentType bắt buộc đúng 1 slot trên form upload. */
    public static Map<String, RegistrantDocumentView> mergeRequiredDocumentSlots(
            DocumentDAO documentdao, List<RegistrantDocumentView> docs) {
        Map<String, RegistrantDocumentView> slots = documentdao.defaultDocumentSlots();
        applyUploadedRequiredDocs(slots, docs);
        return slots;
    }

    /** Ghi đè slot bắt buộc bằng tài liệu đã upload từ DB. */
    private static void applyUploadedRequiredDocs(Map<String, RegistrantDocumentView> slots,
            List<RegistrantDocumentView> docs) {
        if (slots == null || docs == null) {
            return;
        }
        for (RegistrantDocumentView doc : docs) {
            if (DocumentDAOImpl.isOtherType(doc.getDocumentType())) {
                continue;
            }
            if (slots.containsKey(doc.getDocumentType())) {
                slots.put(doc.getDocumentType(), doc);
            }
        }
    }

    /** Tạo map 4 slot bắt buộc rồi merge tài liệu đã có. */
    public static Map<String, RegistrantDocumentView> buildRequiredSlots(List<RegistrantDocumentView> allDocs) {
        Map<String, RegistrantDocumentView> slots = new LinkedHashMap<>();
        for (String type : RegistrantDocumentStatusHelper.REQUIRED_TYPES) {
            RegistrantDocumentView empty = new RegistrantDocumentView();
            empty.setDocumentType(type);
            empty.setStatusClass("pending");
            empty.setStatusLabel("Chưa tải lên");
            slots.put(type, empty);
        }
        applyUploadedRequiredDocs(slots, allDocs);
        return slots;
    }

    /** Lọc các DocumentType Other/Other_* từ danh sách. */
    public static List<RegistrantDocumentView> listOtherDocuments(List<RegistrantDocumentView> docs) {
        List<RegistrantDocumentView> others = new ArrayList<>();
        if (docs == null) {
            return others;
        }
        for (RegistrantDocumentView doc : docs) {
            if (DocumentDAOImpl.isOtherType(doc.getDocumentType())) {
                others.add(doc);
            }
        }
        return others;
    }

    /** Đếm số slot bắt buộc đã có file URL. */
    public static int countUploadedRequired(Map<String, RegistrantDocumentView> slots) {
        if (slots == null) {
            return 0;
        }
        int count = 0;
        for (RegistrantDocumentView doc : slots.values()) {
            if (doc.getDocumentUrl() != null && !doc.getDocumentUrl().isBlank()) {
                count++;
            }
        }
        return count;
    }

    // --- Upload validation ---

    /** True nếu DocumentType thuộc tập được phép upload. */
    public static boolean isAllowedDocumentType(String documentType) {
        return documentType != null && ALLOWED_MANDATORY_TYPES.contains(documentType);
    }

    /** Validate Part giấy tờ bắt buộc (size/đuôi file); null nếu OK. */
    public static String validateMandatoryUpload(Part filePart, String documentType) {
        if (!isAllowedDocumentType(documentType) || "Other".equals(documentType)) {
            return "Loại tài liệu không hợp lệ.";
        }
        String sizeError = validatePartSize(filePart, false);
        if (sizeError != null) {
            return sizeError;
        }
        String ext = extractExtension(filePart != null ? filePart.getSubmittedFileName() : null);
        if (!isAllowedExtension(documentType, ext)) {
            return "Chỉ chấp nhận ảnh PNG, JPG hoặc JPEG.";
        }
        return null;
    }

    /** Validate Part Hồ sơ khác (PNG/JPG/PDF ≤5MB). */
    public static String validateOtherUpload(Part filePart) {
        String sizeError = validatePartSize(filePart, true);
        if (sizeError != null) {
            return sizeError;
        }
        String ext = extractExtension(filePart != null ? filePart.getSubmittedFileName() : null);
        if (!isAllowedExtension("Other", ext)) {
            return "Hồ sơ khác chấp nhận PNG, JPG, JPEG hoặc PDF.";
        }
        return null;
    }

    /** Kiểm tra Part khác rỗng và không vượt MAX_UPLOAD_BYTES. */
    public static String validatePartSize(Part filePart, boolean includeFileNameInMessage) {
        if (filePart == null || filePart.getSize() <= 0) {
            return "Vui lòng chọn tệp để tải lên.";
        }
        if (filePart.getSize() > MAX_UPLOAD_BYTES) {
            if (includeFileNameInMessage) {
                String name = filePart.getSubmittedFileName();
                return "Tệp " + (name != null ? name : "") + " vượt quá 5MB.";
            }
            return "Dung lượng tệp vượt quá 5MB.";
        }
        return null;
    }

    /** Lấy phần mở rộng tệp (mặc định jpg). */
    public static String extractExtension(String fileName) {
        if (fileName == null) {
            return "jpg";
        }
        int dot = fileName.lastIndexOf('.');
        return dot >= 0 ? fileName.substring(dot + 1).toLowerCase() : "jpg";
    }

    /** True nếu đuôi file hợp lệ theo loại tài liệu. */
    public static boolean isAllowedExtension(String documentType, String ext) {
        if ("Other".equals(documentType)) {
            return "png".equals(ext) || "jpg".equals(ext) || "jpeg".equals(ext) || "pdf".equals(ext);
        }
        return "png".equals(ext) || "jpg".equals(ext) || "jpeg".equals(ext);
    }
}

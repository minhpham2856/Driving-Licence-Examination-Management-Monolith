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

/** Tiện ích tài liệu hồ sơ — markers Notes (#PROFILE_DOC#, #SUPPLEMENT_DOC#, #SUPPLEMENT_ER#id#, #PENDING#/#APPROVED#/#LICENCE#) cho workflow chính và bổ sung. */
public final class RegistrantDocumentHelper {

    // --- Workflow markers trên ExamRegistration / Document.Notes ---

    public static final String MARK_PROFILE_DOC = "#PROFILE_DOC#";
    public static final String MARK_SUPPLEMENT_DOC = "#SUPPLEMENT_DOC#";
    /** Prefix gắn Document.Notes với ExamRegistrationId bổ sung: {@code #SUPPLEMENT_ER#42#}. */
    public static final String MARK_SUPPLEMENT_ER_PREFIX = "#SUPPLEMENT_ER#";

    // --- Upload validation ---

    public static final long MAX_UPLOAD_BYTES = 5L * 1024 * 1024;

    private static final Set<String> ALLOWED_MANDATORY_TYPES = Set.of(
            "Portrait", "IdFront", "IdBack", "HealthCertificate", "Other");

    private RegistrantDocumentHelper() {
    }

    /** ER notes chứa #SUPPLEMENT_DOC# → đây là dòng đăng ký hồ sơ bổ sung (không phải hồ sơ gốc). */
    public static boolean isSupplementExamRegistrationNotes(String notes) {
        return notes != null && notes.contains(MARK_SUPPLEMENT_DOC);
    }

    /** ER hồ sơ gốc: notes trống hoặc không có marker supplement. */
    public static boolean isPrimaryExamRegistrationNotes(String notes) {
        return notes == null || notes.isBlank() || !isSupplementExamRegistrationNotes(notes);
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

    public static void applyUploadedRequiredDocs(Map<String, RegistrantDocumentView> slots,
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

    /** Other cần staff review cho ER bổ sung: ưu tiên #SUPPLEMENT_ER#id#; legacy thì Other đang #PENDING#. */
    public static List<RegistrantDocumentView> collectSupplementReviewTargets(
            List<RegistrantDocumentView> docs, int supplementExamRegistrationId) {
        List<RegistrantDocumentView> linked = new ArrayList<>();
        List<RegistrantDocumentView> legacyPending = new ArrayList<>();
        if (docs == null || supplementExamRegistrationId <= 0) {
            return linked;
        }
        for (RegistrantDocumentView doc : docs) {
            if (!DocumentDAOImpl.isOtherType(doc.getDocumentType()) || !hasUploadedFile(doc)) {
                continue;
            }
            Integer linkedEr = parseSupplementErId(doc.getNotes());
            if (linkedEr != null && linkedEr == supplementExamRegistrationId) {
                linked.add(doc);
            } else if (linkedEr == null && DocumentDAOImpl.isPendingReview(doc.getNotes())) {
                legacyPending.add(doc);
            }
        }
        return linked.isEmpty() ? legacyPending : linked;
    }

    // --- Upload validation ---

    public static boolean isAllowedDocumentType(String documentType) {
        return documentType != null && ALLOWED_MANDATORY_TYPES.contains(documentType);
    }

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

    public static String extractExtension(String fileName) {
        if (fileName == null) {
            return "jpg";
        }
        int dot = fileName.lastIndexOf('.');
        return dot >= 0 ? fileName.substring(dot + 1).toLowerCase() : "jpg";
    }

    public static boolean isAllowedExtension(String documentType, String ext) {
        if ("Other".equals(documentType)) {
            return "png".equals(ext) || "jpg".equals(ext) || "jpeg".equals(ext) || "pdf".equals(ext);
        }
        return "png".equals(ext) || "jpg".equals(ext) || "jpeg".equals(ext);
    }

    private static boolean hasUploadedFile(RegistrantDocumentView doc) {
        return doc != null && doc.getDocumentUrl() != null && !doc.getDocumentUrl().isBlank();
    }
}

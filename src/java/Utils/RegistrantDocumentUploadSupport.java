package Utils;

import jakarta.servlet.http.Part;
import java.util.Set;

/** Validation upload tài liệu hồ sơ thí sinh. */
public final class RegistrantDocumentUploadSupport {

    public static final long MAX_BYTES = 5L * 1024 * 1024;

    private static final Set<String> ALLOWED_TYPES = Set.of(
            "Portrait", "IdFront", "IdBack", "HealthCertificate", "Other");

    private RegistrantDocumentUploadSupport() {
    }

    public static boolean isAllowedDocumentType(String documentType) {
        return documentType != null && ALLOWED_TYPES.contains(documentType);
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
        if (filePart.getSize() > MAX_BYTES) {
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
}

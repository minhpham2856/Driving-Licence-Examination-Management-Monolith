package examstaff.util;

/**
 * Suy tên entity / chuẩn hóa mã action cho audit log exam staff.
 * Dùng trước khi ghi nhật ký để gom các cụm action/details khác nhau về entity & mã chuẩn.
 */
public final class AuditLogHelper {

    /** Không cho khởi tạo — chỉ dùng static. */
    private AuditLogHelper() {
    }

    /**
     * Suy tên entity kỹ thuật từ action + details (ScoreEntryQueue, Payment, …).
     * <p>
     * Luồng ưu tiên (return ngay khi khớp):
     * <ol>
     *   <li>ScoreEntry / hàng đợi → {@code ScoreEntryQueue}</li>
     *   <li>ExamDevice / thiết bị → {@code ExamDevice}</li>
     *   <li>IMPORT → {@code ExamRegistration}</li>
     *   <li>PAYMENT → {@code Payment}</li>
     *   <li>PERSON / PROFILE → {@code Profile}</li>
     *   <li>EXAMINER / ASSIGN / REMOVE → {@code ExaminerSchedule}</li>
     *   <li>Điểm / lý thuyết / thực hành / ExamScore → {@code ExamScore}</li>
     *   <li>ExamRegistration / ALLOCATE → {@code Candidate}</li>
     *   <li>EXAM (không dính các nhánh trên) → {@code Exam}</li>
     *   <li>Mặc định → {@code Candidate}</li>
     * </ol>
     *
     * @param action  mã/cụm action
     * @param details mô tả chi tiết
     * @return tên entity (mặc định {@code Candidate})
     */
    public static String resolveEntityName(String action, String details) {
        // Bước 1: chuẩn hóa chữ hoa để so khớp từ khóa
        String upper = action != null ? action.toUpperCase() : "";
        String detailUpper = details != null ? details.toUpperCase() : "";

        // Bước 2: lần lượt kiểm tra các nhóm nghiệp vụ (ưu tiên hẹp → rộng)
        if (upper.contains("SCOREENTRY") || detailUpper.contains("HÀNG ĐỢI")) {
            return "ScoreEntryQueue";
        }
        if (upper.contains("EXAMDEVICE") || detailUpper.contains("THIẾT BỊ")) {
            return "ExamDevice";
        }
        if (upper.contains("IMPORT")) {
            return "ExamRegistration";
        }
        if (upper.contains("PAYMENT")) {
            return "Payment";
        }
        if (upper.contains("PERSON") || upper.contains("PROFILE")) {
            return "Profile";
        }
        if (upper.contains("EXAMINER") || upper.contains("ASSIGN") || upper.contains("REMOVE")) {
            return "ExaminerSchedule";
        }
        if (detailUpper.contains("ĐIỂM") || detailUpper.contains("DIEM")
                || upper.contains("EXAMSCORE") || detailUpper.contains("LÝ THUYẾT")
                || detailUpper.contains("THỰC HÀNH") || detailUpper.contains("ĐƯỜNG TRƯỜNG")) {
            return "ExamScore";
        }
        if (upper.contains("EXAMREGISTRATION") || upper.contains("ALLOCATE")) {
            return "Candidate";
        }
        if (upper.contains("EXAM") && !upper.contains("EXAMINER") && !upper.contains("EXAMREGISTRATION")
                && !upper.contains("EXAMSCORE") && !upper.contains("EXAMDEVICE")) {
            return "Exam";
        }
        // Bước 3: fallback an toàn
        return "Candidate";
    }

    /**
     * Chuẩn hóa action thô về một trong: INSERT / UPDATE / DELETE / IMPORT / EXPORT / ASSIGN.
     * <p>
     * Luồng: null → UPDATE; rồi kiểm tra chứa IMPORT → INSERT → DELETE|REMOVE → EXPORT → ASSIGN;
     * mọi trường hợp còn lại → UPDATE.
     *
     * @param rawAct action gốc (null → UPDATE)
     * @return mã action chuẩn
     */
    public static String normalizeAction(String rawAct) {
        // Bước 1: thiếu action → coi như cập nhật
        if (rawAct == null) {
            return "UPDATE";
        }
        String upper = rawAct.toUpperCase();
        // Bước 2: map theo từ khóa chứa trong chuỗi (ưu tiên IMPORT trước INSERT)
        if (upper.contains("IMPORT")) {
            return "IMPORT";
        }
        if (upper.contains("INSERT")) {
            return "INSERT";
        }
        if (upper.contains("DELETE") || upper.contains("REMOVE")) {
            return "DELETE";
        }
        if (upper.contains("EXPORT")) {
            return "EXPORT";
        }
        if (upper.contains("ASSIGN")) {
            return "ASSIGN";
        }
        return "UPDATE";
    }
}

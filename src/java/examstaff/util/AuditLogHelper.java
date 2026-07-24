package examstaff.util;

/**
 * Utility suy tên entity và chuẩn hóa mã action trước khi ghi nhật ký audit ExamStaff.
 * Gom các cụm action/details khác nhau về entity kỹ thuật và mã CRUD chuẩn.
 *
 * Vai trò trong luồng examstaff:
 * Khi staff thao tác (phân bổ, thu phí, gọi thí sinh, nhập điểm, …), BLL ghi audit với
 * action/details thô. Helper map sang tên bảng/entity (Candidate, Payment,
 * ExamScore, …) và action chuẩn (INSERT/UPDATE/ASSIGN/…)
 * trước khi persist qua AuditLogDAO.
 *
 * Cách hoạt động:
 * - resolveEntityName — ưu tiên từ khóa trong action/details (ScoreEntry → ScoreEntryQueue,
 *       Payment → Payment, ALLOCATE → Candidate, …); mặc định Candidate.
 * - normalizeAction — null → UPDATE; chứa IMPORT/INSERT/DELETE/EXPORT/ASSIGN → mã tương ứng.
 *
 * Ai gọi:
 * StaffAuditLogServiceImpl, AuditLogDAOImpl — mọi điểm ghi audit từ allocation,
 * procedure, call board, examiner assignment, exam control.
 */
public final class AuditLogHelper {

    /** Không cho khởi tạo — chỉ dùng static. */
    private AuditLogHelper() {
    }

    /**
     * Suy tên entity kỹ thuật từ action + details (ScoreEntryQueue, Payment, …).
     * <p>
 *
     * Luồng ưu tiên (return ngay khi khớp):
     * - ScoreEntry / hàng đợi → ScoreEntryQueue
     * - ExamDevice / thiết bị → ExamDevice
     * - IMPORT → ExamRegistration
     * - PAYMENT → Payment
     * - PERSON / PROFILE → Profile
     * - EXAMINER / ASSIGN / REMOVE → ExaminerSchedule
     * - Điểm / lý thuyết / thực hành / ExamScore → ExamScore
     * - ExamRegistration / ALLOCATE → Candidate
     * - EXAM (không dính các nhánh trên) → Exam
     * - Mặc định → Candidate
     * @param action  mã/cụm action
     * @param details mô tả chi tiết
     * @return tên entity (mặc định Candidate)
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

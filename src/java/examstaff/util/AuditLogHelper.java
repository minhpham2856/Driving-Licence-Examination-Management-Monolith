package examstaff.util;

/** Suy tên entity / chuẩn hóa mã action cho audit log exam staff. */
public final class AuditLogHelper {

    private AuditLogHelper() {
    }

    /**
     * Suy tên entity kỹ thuật từ action + details (ScoreEntryQueue, Payment, …).
     *
     * @param action  mã/cụm action
     * @param details mô tả chi tiết
     * @return tên entity (mặc định {@code Candidate})
     */
    public static String resolveEntityName(String action, String details) {
        String upper = action != null ? action.toUpperCase() : "";
        String detailUpper = details != null ? details.toUpperCase() : "";

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
        return "Candidate";
    }

    /**
     * Chuẩn hóa action thô về INSERT/UPDATE/DELETE/IMPORT/EXPORT/ASSIGN.
     *
     * @param rawAct action gốc (null → UPDATE)
     * @return mã action chuẩn
     */
    public static String normalizeAction(String rawAct) {
        if (rawAct == null) {
            return "UPDATE";
        }
        String upper = rawAct.toUpperCase();
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

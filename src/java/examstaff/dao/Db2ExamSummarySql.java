package examstaff.dao;

/**
 * Hằng SQL SELECT tóm tắt kỳ thi ({@code Exam} JOIN {@code Licence}) dùng chung.
 *
 * Ai dùng?:
 * - {@code ExamDAOImpl#getById} — {@code EXAM_SUMMARY_SELECT + " WHERE e.ExamId = ?"}
 * - {@code ExamViewDAOImpl} — list / picker kỳ thi (thêm {@code ORDER BY} / filter status)
 * Một chỗ định nghĩa cột → map {@code ExamSummaryDTO} không lệch giữa “đọc 1 kỳ” và “list kỳ”.
 *
 * Cột quan trọng:
 * - {@code id} / {@code examId} — cùng {@code Exam.ExamId} (alias kép cho mapper cũ)
 * - {@code examName} — ưu tiên {@code ExamCode}; trống thì ghép {@code Hạng + LicenceClass + ngày}
 * - {@code status} — {@code Exam.Status} (DB), khác pause runtime trên Call Board
 * - {@code licenseCode} — hạng GPLX từ {@code Licence}
 * <p>Caller <b>phải</b> gắn {@code WHERE} / {@code ORDER BY} khi chạy — hằng này chỉ là phần SELECT…FROM…JOIN.
 */
public final class Db2ExamSummarySql {

    private Db2ExamSummarySql() {
    }

    /**
     * SELECT một hàng kỳ thi: alias {@code id} và {@code examId} cùng trỏ {@code ExamId}.
     * Caller gắn {@code WHERE e.ExamId = ?} hoặc {@code ORDER BY ...} khi chạy.
     */
    public static final String EXAM_SUMMARY_SELECT = """
            SELECT e.ExamId AS id,
                   e.ExamId AS examId,
                   COALESCE(NULLIF(LTRIM(RTRIM(e.ExamCode)), N''),
                     N'Hạng ' + l.LicenceClass + N' - ' + CONVERT(NVARCHAR(10), e.ExamDate, 103)) AS examName,
                   1 AS examTypeId,
                   CAST(e.ExamDate AS DATE) AS examDate,
                   CAST(e.StartTime AS TIME) AS shiftStartTime,
                   CAST(e.EndTime AS TIME) AS shiftEndTime,
                   e.StartTime AS scheduledStartAt,
                   e.EndTime AS scheduledEndAt,
                   e.[Status] AS status,
                   e.StartTime AS createdAt,
                   l.LicenceClass AS licenseCode,
                   e.ExamCode AS examCode,
                   N'Lý thuyết + Thực hành' AS examTypeName
            FROM Exam e
            JOIN Licence l ON l.LicenceId = e.LicenceId
            """;
}

package examstaff.dao;

/**
 * Hằng SQL SELECT tóm tắt kỳ thi (Exam JOIN Licence) dùng chung.
 *
 * Ai dùng?:
 * - ExamDAOImpl#getById — EXAM_SUMMARY_SELECT + " WHERE e.ExamId = ?"
 * - ExamViewDAOImpl — list / picker kỳ thi (thêm ORDER BY / filter status)
 * Một chỗ định nghĩa cột → map ExamSummaryDTO không lệch giữa “đọc 1 kỳ” và “list kỳ”.
 *
 * Cột quan trọng:
 * - id / examId — cùng Exam.ExamId (alias kép cho mapper cũ)
 * - examName — ưu tiên ExamCode; trống thì ghép Hạng + LicenceClass + ngày
 * - status — Exam.Status (DB), khác pause runtime trên Call Board
 * - licenseCode — hạng GPLX từ Licence
 * <p>Caller <b>phải</b> gắn WHERE / ORDER BY khi chạy — hằng này chỉ là phần SELECT…FROM…JOIN.
 */
public final class Db2ExamSummarySql {

    private Db2ExamSummarySql() {
    }

    /**
     * SELECT một hàng kỳ thi: alias id và examId cùng trỏ ExamId.
     * Caller gắn WHERE e.ExamId = ? hoặc ORDER BY ... khi chạy.
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

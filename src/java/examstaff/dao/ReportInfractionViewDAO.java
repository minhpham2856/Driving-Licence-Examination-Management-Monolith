package examstaff.dao;

import java.util.List;
import java.util.Map;

/**
 * View DAO thống kê lỗi trừ điểm (infraction) theo kỳ thi — <b>read-only</b>.
 *
 * Vai trò trong kiến trúc:
 * Cung cấp dữ liệu top lý do trừ điểm phần thực hành cho biểu đồ báo cáo sau ca thi.
 * Gom {@code OccurrenceCount} theo {@code ScoreDeduction.Reason}, chỉ section TH.
 * <pre>
 *   Report / dashboard servlet
 *            │  findTopInfractions(examId, limit)
 *            ▼
 *      ReportInfractionViewDAO  ◄── ReportInfractionViewDAOImpl
 *            │
 *            ▼  DeductionRecord → ScoreDeduction → ExamScore → ExamSection (TH)
 *      List&lt;Map reason, count, percentage&gt;
 * </pre>
 *
 * Lọc section thực hành:
 * Dùng {@link Db2ExamSchemaSql#PRACTICAL_SECTION_TYPES} nhúng tường minh trong SQL —
 * không splice CSV runtime (Approach B).
 *
 * Triển khai mặc định:
 * {@link examstaff.dao.impl.ReportInfractionViewDAOImpl}.
 */
public interface ReportInfractionViewDAO {

    /**
     * Lấy top lỗi trừ điểm của phần thực hành trong một kỳ thi.
     * Thực thi SELECT TOP (?) nhóm theo {@code Reason} trên chuỗi JOIN
     * DeductionRecord → ScoreDeduction → ExamScore → ExamSection → ExamResult → ExamEnrollment;
     * lọc section thực hành và {@code ExamId}, tính count cùng tỉ lệ phần trăm.
     * @param examId mã kỳ thi ({@code ExamEnrollment.ExamId}) cần thống kê
     * @param limit  số dòng tối đa trả về; nếu ≤ 0 thì dùng mặc định (thường là 3)
     * @return danh sách {@link Map} mỗi phần tử gồm khóa {@code reason}, {@code count},
     *         {@code percentage}; rỗng nếu kỳ thi không có lỗi trừ điểm
     */
    List<Map<String, Object>> findTopInfractions(int examId, int limit);
}

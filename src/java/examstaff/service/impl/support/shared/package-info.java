/**
 * Package support dùng chung xuyên các subdomain Exam Staff (allocation, call, procedure, audit, …).
 *
 * Vai trò trong kiến trúc examstaff:
 * Chứa helper và service mỏng không thuộc một luồng nghiệp vụ đơn lẻ: quy tắc kỳ thi,
 * lịch giờ thi, chuẩn hóa hạng GPLX, gộp enrollment trùng thí sinh. Tránh duplicate logic
 * giữa {@code support.allocation}, {@code support.call} và {@code support.procedure}.
 *
 * Thành phần chính:
 * - {@link examstaff.service.impl.support.shared.ExamStaffExamRules} / {@link examstaff.service.impl.support.shared.ExamStaffExamQueryServiceImpl}
 *       — đọc và lọc {@code ExamSummaryDTO} cho sidebar.
 * - {@link examstaff.service.impl.support.shared.ExamScheduleRules} — mốc giờ bắt đầu ca.
 * - {@link examstaff.service.impl.support.shared.LicenseClassRules} — A1/A/B1, mô tô vs ô tô.
 * - {@link examstaff.service.impl.support.shared.ExamEnrollmentMerge} — dedupe thí sinh trên list DTO.
 *
 * Phụ thuộc lân cận:
 * Logic phí thủ tục chi tiết, thanh toán SePay và báo cáo cuối ngày nằm ở
 * {@code examstaff.service.impl.support.procedure}; phân công giám thị ở {@code support.assign}.
 */
package examstaff.service.impl.support.shared;

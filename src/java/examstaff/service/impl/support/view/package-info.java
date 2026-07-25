/**
 * Support dựng trang Exam Staff (đọc / hiển thị).
 *
 * Luồng end-to-end:
 * <pre>
 *   Servlet (Dashboard / Dossier / Report / ExamSelect / …)
 *        │  bind session + ExamStaffPageCommand
 *        ▼
 *   ExamStaffViewServiceImpl (consolidator)
 *        ├── ExamStaffSelectionServiceImpl  — resolve/ensure examId, processSelection
 *        ├── ExamStaffPageServiceImpl       — picker, page context, queue refresh
 *        ├── ExamStaffDashboardServiceImpl  — KPI giám khảo đã phân công
 *        ├── CandidateDossierServiceImpl    — hồ sơ thí sinh (phí, hạng, ảnh)
 *        ├── CandidatePhotoServiceImpl      — chuẩn hoá / stream ảnh chân dung
 *        ├── ExamReportStatsServiceImpl     — thống kê đỗ/trượt theo hạng
 *        └── ExamReportProcedureStatusServiceImpl — trạng thái thủ tục báo cáo
 * </pre>
 *
 * Nhóm chức năng:
 * - <b>Chọn kỳ / context trang</b> — examstaff.service.impl.support.view.ExamStaffSelectionServiceImpl,
 *       examstaff.service.impl.support.view.ExamStaffPageServiceImpl
 * - <b>Dashboard</b> — examstaff.service.impl.support.view.ExamStaffDashboardServiceImpl
 * - <b>Hồ sơ & ảnh</b> — examstaff.service.impl.support.view.CandidateDossierServiceImpl,
 *       examstaff.service.impl.support.view.CandidatePhotoServiceImpl
 * - <b>Báo cáo</b> — examstaff.service.impl.support.view.ExamReportStatsServiceImpl,
 *       examstaff.service.impl.support.view.ExamReportProcedureStatusServiceImpl
 * <p>Không gọi trực tiếp từ servlet — luôn qua consolidator ExamStaffViewServiceImpl.
 */
package examstaff.service.impl.support.view;

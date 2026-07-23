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
 * - <b>Chọn kỳ / context trang</b> — {@link examstaff.service.impl.support.view.ExamStaffSelectionServiceImpl},
 *       {@link examstaff.service.impl.support.view.ExamStaffPageServiceImpl}
 * - <b>Dashboard</b> — {@link examstaff.service.impl.support.view.ExamStaffDashboardServiceImpl}
 * - <b>Hồ sơ &amp; ảnh</b> — {@link examstaff.service.impl.support.view.CandidateDossierServiceImpl},
 *       {@link examstaff.service.impl.support.view.CandidatePhotoServiceImpl}
 * - <b>Báo cáo</b> — {@link examstaff.service.impl.support.view.ExamReportStatsServiceImpl},
 *       {@link examstaff.service.impl.support.view.ExamReportProcedureStatusServiceImpl}
 * <p>Không gọi trực tiếp từ servlet — luôn qua consolidator {@code ExamStaffViewServiceImpl}.
 */
package examstaff.service.impl.support.view;

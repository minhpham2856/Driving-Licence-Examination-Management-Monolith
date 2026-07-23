/**
 * Support nhật ký audit cán bộ (đọc / ghi / xuất).
 *
 * Luồng end-to-end:
 * <pre>
 *   AuditServlet / AuditExportServlet
 *        │  GET trang / POST xuất Excel
 *        ▼
 *   AuditService (consolidator)
 *        ├── StaffAuditPageServiceImpl    — buildPage: phân trang + KPI + nhãn VI
 *        │         └── StaffAuditQueryServiceImpl — count / list / KPI qua AuditLogDAO
 *        ├── StaffAuditLogServiceImpl     — logAction: ghi hành động staff
 *        └── StaffAuditExportServiceImpl  — exportAuditLog: workbook Excel 2 sheet
 * </pre>
 *
 * Vai trò từng lớp:
 * - {@link examstaff.service.impl.support.audit.StaffAuditQueryServiceImpl} — truy vấn DAO thuần;
 *       đếm, phân trang, KPI thủ tục theo nhân viên/ngày
 * - {@link examstaff.service.impl.support.audit.StaffAuditPageServiceImpl} — ghép view trang audit:
 *       clamp page, {@code PageSlice}, gắn nhãn {@code ExamStaffLabels}
 * - {@link examstaff.service.impl.support.audit.StaffAuditLogServiceImpl} — insert audit khi staff
 *       thao tác (assign, thủ tục, …); map entity qua {@code AuditLogHelper}
 * - {@link examstaff.service.impl.support.audit.StaffAuditExportServiceImpl} — xuất Excel
 *       sheet Tổng quan + Chi tiết nhật ký (Apache POI)
 * <p>Không gọi trực tiếp từ servlet — luôn qua {@code AuditServiceImpl}.
 */
package examstaff.service.impl.support.audit;

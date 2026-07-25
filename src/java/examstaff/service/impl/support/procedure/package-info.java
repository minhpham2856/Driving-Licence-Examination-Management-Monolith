/**
 * Support subdomain thủ tục hồ sơ / thu lệ phí / ảnh chân dung tại bàn.
 * <p>
 * Điều phối bước thủ tục (hồ sơ → ảnh → thanh toán). Consolidator
 * ProcedureService ủy quyền xuống đây; presentation không gọi DAO trực tiếp.
 *
 * Luồng end-to-end:
 * <pre>
 *   ProcedureServlet
 *        │  SBD + step (1/2/3)
 *        ▼
 *   ProcedureService (consolidator)
 *        ├── ProcedureStepHelper           — suy bước UI + thông báo lỗi
 *        ├── ProcedureWorkflowServiceImpl  — hồ sơ / ảnh / pay / reset
 *        │         ├── ProcedurePaymentServiceImpl  — preview + Payment CASH
 *        │         ├── CandidatePhotoServiceImpl    — ảnh chân dung
 *        │         └── ExaminerAllocationServiceImpl — auto sau trả phí
 *        └── ProcedureFeeQueryServiceImpl    — Fee/Payment cho preview + report
 * </pre>
 *
 * Thành phần chính:
 * - examstaff.service.impl.support.procedure.ProcedureWorkflowServiceImpl — tìm/chuẩn bị hồ sơ, ảnh, confirm pay, reset
 * - examstaff.service.impl.support.procedure.ProcedurePaymentServiceImpl — preview phí + ghi Payment CASH
 * - examstaff.service.impl.support.procedure.ProcedureFeeQueryServiceImpl — đọc Fee/Payment dùng chung report
 * - examstaff.service.impl.support.procedure.ProcedureStepHelper — suy bước 1–3 và thông báo lỗi UI
 */
package examstaff.service.impl.support.procedure;

/**
 * Support subdomain thủ tục hồ sơ / thu lệ phí / ảnh chân dung tại bàn.
 * <p>
 * Vai trò: điều phối bước thủ tục (hồ sơ → ảnh → thanh toán), preview phí và ghi nhận
 * thanh toán tiền mặt. Consolidator {@code ProcedureService} ủy quyền xuống đây;
 * presentation không gọi DAO trực tiếp.
 * </p>
 * <ul>
 *   <li>{@link examstaff.service.impl.support.procedure.ProcedureWorkflowServiceImpl} — tìm/chuẩn bị hồ sơ, ảnh, confirm pay, reset</li>
 *   <li>{@link examstaff.service.impl.support.procedure.ProcedurePaymentServiceImpl} — preview phí + ghi Payment CASH</li>
 *   <li>{@link examstaff.service.impl.support.procedure.ProcedureFeeQueryServiceImpl} — đọc Fee/Payment dùng chung report</li>
 *   <li>{@link examstaff.service.impl.support.procedure.ProcedureStepHelper} — suy bước 1–3 và thông báo lỗi UI</li>
 * </ul>
 */
package examstaff.service.impl.support.procedure;

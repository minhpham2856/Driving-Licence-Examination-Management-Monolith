/**
 * Support subdomain gọi số / hàng đợi / bảng gọi thí sinh.
 * <p>
 * Vai trò: logic thuần BLL cho màn gọi thí sinh (staff Call page, CallBoard, Public Call).
 * Orchestrator consolidator gọi các service trong package này; không gọi từ servlet trực tiếp.
 * </p>
 * <ul>
 *   <li>{@link examstaff.service.impl.support.call.CandidateCallPageServiceImpl} — dựng view trang gọi</li>
 *   <li>{@link examstaff.service.impl.support.call.CandidateCallWorkflowServiceImpl} — dispatch action (gọi / vắng / đóng ca)</li>
 *   <li>{@link examstaff.service.impl.support.call.CandidateQueueServiceImpl} — làm mới / sắp / resolve SBD đang gọi</li>
 *   <li>{@link examstaff.service.impl.support.call.CandidateQueueQueryServiceImpl} — đọc danh sách thí sinh kỳ thi</li>
 *   <li>{@link examstaff.service.impl.support.call.CandidateAttendanceServiceImpl} — đánh vắng / đình chỉ / khôi phục</li>
 *   <li>{@link examstaff.service.impl.support.call.CallQueueRules} / {@link examstaff.service.impl.support.call.CallBoardRules} — quy tắc thuần không HTTP</li>
 * </ul>
 */
package examstaff.service.impl.support.call;

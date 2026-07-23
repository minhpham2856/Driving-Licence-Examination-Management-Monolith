/**
 * Support subdomain gọi số / hàng đợi / bảng gọi thí sinh.
 * <p>
 * Logic BLL cho màn staff Call page, CallBoard TV và Public Call.
 * Orchestrator consolidator gọi các service trong package này; servlet không import trực tiếp.
 *
 * Luồng end-to-end:
 * <pre>
 *   CandidateCallServlet / PublicCallServlet
 *        │  command / poll
 *        ▼
 *   StaffCallService (consolidator)
 *        ├── CandidateCallPageServiceImpl     — dựng view + sync board
 *        ├── CandidateCallWorkflowServiceImpl — action gọi / vắng / đóng ca
 *        ├── CandidateQueueServiceImpl        — refresh / resolve SBD
 *        ├── CandidateQueueQueryServiceImpl   — đọc thí sinh kỳ thi
 *        ├── CandidateAttendanceServiceImpl   — vắng / đình chỉ / khôi phục
 *        ├── CallBoardRules / CallQueueRules  — mutate board + queue (pure)
 *        └── CallBoardDAO                     — persist board state
 * </pre>
 *
 * Thành phần chính:
 * - {@link examstaff.service.impl.support.call.CandidateCallPageServiceImpl} — dựng view trang gọi
 * - {@link examstaff.service.impl.support.call.CandidateCallWorkflowServiceImpl} — dispatch action (gọi / vắng / đóng ca)
 * - {@link examstaff.service.impl.support.call.CandidateQueueServiceImpl} — làm mới / sắp / resolve SBD đang gọi
 * - {@link examstaff.service.impl.support.call.CandidateQueueQueryServiceImpl} — đọc danh sách thí sinh kỳ thi
 * - {@link examstaff.service.impl.support.call.CandidateAttendanceServiceImpl} — đánh vắng / đình chỉ / khôi phục
 * - {@link examstaff.service.impl.support.call.CallQueueRules} / {@link examstaff.service.impl.support.call.CallBoardRules} — quy tắc thuần không HTTP
 */
package examstaff.service.impl.support.call;

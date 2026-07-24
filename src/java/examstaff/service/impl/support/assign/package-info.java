/**
 * Support phân công sát hạch viên vào phòng/sân theo kỳ thi.
 *
 * Luồng end-to-end:
 * <pre>
 *   ExaminerAllocationServlet
 *        │  GET → buildAllocationView / POST → assignExaminer | removeExaminer
 *        ▼
 *   ExaminerAssignService (consolidator)
 *        ├── ExaminerAllocationDeskServiceImpl  — view bàn + gán/gỡ + audit message
 *        │         └── ExaminerAllocationServiceImpl — DAO + auto-allocate thí sinh
 *        └── ExaminerAssignmentRules             — pure: loại khu vực, slot staffed, coverage
 * </pre>
 *
 * Vai trò từng lớp:
 * - examstaff.service.impl.support.assign.ExaminerAssignmentRules — nhận diện LT/TH,
 *       lọc phòng/sân đã có SHV, kiểm tra coverage trước khi bắt đầu kỳ
 * - examstaff.service.impl.support.assign.ExaminerAllocationServiceImpl — CRUD phân công,
 *       auto-allocate phòng LT / sân TH (cân bằng tải, chỉ khu đã staffed)
 * - examstaff.service.impl.support.assign.ExaminerAllocationDeskServiceImpl — dựng
 *       ExaminerAllocationViewDTO, tách available/busy SHV, gán·gỡ kèm audit
 * <p>Được support.allocation và AllocationActionServiceImpl gọi để biết phòng/sân
 * nào đủ điều kiện gán thí sinh (chỉ khu vực đã có sát hạch viên).
 */
package examstaff.service.impl.support.assign;

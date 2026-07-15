/**
 * Support phân công sát hạch viên vào phòng/sân theo kỳ thi.
 *
 * <p>Nhóm này chứa quy tắc nhận diện loại khu vực / slot đã có giám khảo
 * ({@link examstaff.service.impl.support.assign.ExaminerAssignmentRules}),
 * dịch vụ phân công + auto-allocate thí sinh vào phòng/sân
 * ({@link examstaff.service.impl.support.assign.ExaminerAllocationServiceImpl}),
 * và lớp bàn làm việc dựng view / gán·gỡ giám khảo
 * ({@link examstaff.service.impl.support.assign.ExaminerAllocationDeskServiceImpl}).
 *
 * <p>Được {@code support.allocation} gọi để biết phòng/sân nào đủ điều kiện gán thí sinh
 * (chỉ khu vực đã có sát hạch viên).
 */
package examstaff.service.impl.support.assign;

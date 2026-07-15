/**
 * Support phân bổ thí sinh theo phòng/sân và stage view.
 *
 * <p>Nhóm này chứa quy tắc đạt điểm ({@link examstaff.service.impl.support.allocation.AllocationPassRules}),
 * lọc/phân trang theo giai đoạn ({@link examstaff.service.impl.support.allocation.AllocationStageHelper}),
 * dựng DTO view ({@link examstaff.service.impl.support.allocation.AllocationStageViewServiceImpl}),
 * truy vấn phòng/sân đã có sát hạch viên
 * ({@link examstaff.service.impl.support.allocation.ExamAreaQueryServiceImpl}),
 * và thao tác auto-allocate / đổi phòng thủ công
 * ({@link examstaff.service.impl.support.allocation.AllocationActionServiceImpl}).
 *
 * <p>Phụ thuộc subdomain {@code support.assign} để biết phòng/sân nào đã có giám khảo trước khi gán thí sinh.
 */
package examstaff.service.impl.support.allocation;

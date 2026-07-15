package examstaff.dto;

import examstaff.dto.ExaminerSlotDTO;
import examstaff.dto.UserDTO;

import java.util.List;
import java.util.Map;

/**
 * View-model trang phân công sát hạch viên.
 *
 * <h2>Vai trò</h2>
 * Mang danh sách slot trong ngày, SHV all / available / busy và options khu vực để form assign.
 *
 * <h2>Ai tạo / tiêu thụ</h2>
 * {@code ExaminerAllocationDeskServiceImpl} → {@code ExaminerAllocationServlet} → {@code examiner-allocation.jsp}.
 */
public class ExaminerAllocationViewDTO {

    private List<ExaminerSlotDTO> dayAssignments;
    private List<UserDTO> allExaminers;
    private List<UserDTO> availableExaminers;
    private List<UserDTO> busyExaminers;
    private List<Map<String, Object>> areaAssignOptions;

    /** Các slot SHV đã phân trong ngày / kỳ đang xem. */
    public List<ExaminerSlotDTO> getDayAssignments() {
        return dayAssignments;
    }

    /** Gán danh sách assignment trong ngày. */
    public void setDayAssignments(List<ExaminerSlotDTO> dayAssignments) {
        this.dayAssignments = dayAssignments;
    }

    /** Toàn bộ SHV có thể chọn (danh mục). */
    public List<UserDTO> getAllExaminers() {
        return allExaminers;
    }

    /** Gán danh sách mọi SHV. */
    public void setAllExaminers(List<UserDTO> allExaminers) {
        this.allExaminers = allExaminers;
    }

    /** SHV còn trống (chưa bận slot xung đột). */
    public List<UserDTO> getAvailableExaminers() {
        return availableExaminers;
    }

    /** Gán danh sách SHV available. */
    public void setAvailableExaminers(List<UserDTO> availableExaminers) {
        this.availableExaminers = availableExaminers;
    }

    /** SHV đang bận slot khác. */
    public List<UserDTO> getBusyExaminers() {
        return busyExaminers;
    }

    /** Gán danh sách SHV busy. */
    public void setBusyExaminers(List<UserDTO> busyExaminers) {
        this.busyExaminers = busyExaminers;
    }

    /** Tùy chọn khu vực + loại thi để form phân công (map key/value). */
    public List<Map<String, Object>> getAreaAssignOptions() {
        return areaAssignOptions;
    }

    /** Gán options khu vực assign. */
    public void setAreaAssignOptions(List<Map<String, Object>> areaAssignOptions) {
        this.areaAssignOptions = areaAssignOptions;
    }
}

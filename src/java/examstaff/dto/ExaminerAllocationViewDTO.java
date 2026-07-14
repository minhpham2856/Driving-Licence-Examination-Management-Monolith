package examstaff.dto;

import examstaff.dto.ExaminerSlotDTO;
import examstaff.dto.UserDTO;

import java.util.List;
import java.util.Map;

/**
 * View-model trang phân công sát hạch viên: slot, danh sách SHV và tùy chọn khu vực.
 */
public class ExaminerAllocationViewDTO {

    private List<ExaminerSlotDTO> dayAssignments;
    private List<UserDTO> allExaminers;
    private List<UserDTO> availableExaminers;
    private List<UserDTO> busyExaminers;
    private List<Map<String, Object>> areaAssignOptions;

    public List<ExaminerSlotDTO> getDayAssignments() {
        return dayAssignments;
    }

    public void setDayAssignments(List<ExaminerSlotDTO> dayAssignments) {
        this.dayAssignments = dayAssignments;
    }

    public List<UserDTO> getAllExaminers() {
        return allExaminers;
    }

    public void setAllExaminers(List<UserDTO> allExaminers) {
        this.allExaminers = allExaminers;
    }

    public List<UserDTO> getAvailableExaminers() {
        return availableExaminers;
    }

    public void setAvailableExaminers(List<UserDTO> availableExaminers) {
        this.availableExaminers = availableExaminers;
    }

    public List<UserDTO> getBusyExaminers() {
        return busyExaminers;
    }

    public void setBusyExaminers(List<UserDTO> busyExaminers) {
        this.busyExaminers = busyExaminers;
    }

    public List<Map<String, Object>> getAreaAssignOptions() {
        return areaAssignOptions;
    }

    public void setAreaAssignOptions(List<Map<String, Object>> areaAssignOptions) {
        this.areaAssignOptions = areaAssignOptions;
    }
}

package dto.examstaff;

import dto.ExaminerSlotDTO;
import dto.SessionDTO;
import dto.UserDTO;
import model.ExamArea;
import model.ExamDevice;

import java.util.List;
import java.util.Map;

public class ExaminerAllocationViewDTO {

    private List<SessionDTO> daySessions;
    private List<ExaminerSlotDTO> dayAssignments;
    private List<ExaminerSlotDTO> sessionAssignments;
    private List<UserDTO> allExaminers;
    private List<UserDTO> availableExaminers;
    private List<UserDTO> busyExaminers;
    private List<ExamArea> sessionAreas;
    private Map<Integer, List<ExamDevice>> devicesByArea;
    private Map<String, List<ExamArea>> areasBySession;
    private List<Map<String, Object>> areaAssignOptions;

    public List<SessionDTO> getDaySessions() {
        return daySessions;
    }

    public void setDaySessions(List<SessionDTO> daySessions) {
        this.daySessions = daySessions;
    }

    public List<ExaminerSlotDTO> getDayAssignments() {
        return dayAssignments;
    }

    public void setDayAssignments(List<ExaminerSlotDTO> dayAssignments) {
        this.dayAssignments = dayAssignments;
    }

    public List<ExaminerSlotDTO> getSessionAssignments() {
        return sessionAssignments;
    }

    public void setSessionAssignments(List<ExaminerSlotDTO> sessionAssignments) {
        this.sessionAssignments = sessionAssignments;
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

    public List<ExamArea> getSessionAreas() {
        return sessionAreas;
    }

    public void setSessionAreas(List<ExamArea> sessionAreas) {
        this.sessionAreas = sessionAreas;
    }

    public Map<Integer, List<ExamDevice>> getDevicesByArea() {
        return devicesByArea;
    }

    public void setDevicesByArea(Map<Integer, List<ExamDevice>> devicesByArea) {
        this.devicesByArea = devicesByArea;
    }

    public Map<String, List<ExamArea>> getAreasBySession() {
        return areasBySession;
    }

    public void setAreasBySession(Map<String, List<ExamArea>> areasBySession) {
        this.areasBySession = areasBySession;
    }

    public List<Map<String, Object>> getAreaAssignOptions() {
        return areaAssignOptions;
    }

    public void setAreaAssignOptions(List<Map<String, Object>> areaAssignOptions) {
        this.areaAssignOptions = areaAssignOptions;
    }
}

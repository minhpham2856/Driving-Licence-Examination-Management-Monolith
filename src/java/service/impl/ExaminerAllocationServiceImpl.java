package service.impl;
import dao.ExamAreaDAO;
import dao.ExamDeviceDAO;
import dao.ExaminerScheduleDAO;
import dao.SessionDAO;
import dao.UserDAO;
import dao.impl.ExamAreaDAOImpl;
import dao.impl.ExamDeviceDAOImpl;
import dao.impl.ExaminerScheduleDAOImpl;
import dao.impl.SessionDAOImpl;
import dao.impl.UserDAOImpl;
import dto.AutoAllocateResultDTO;
import dto.ExaminerSlotDTO;
import dto.SessionDTO;
import dto.UserDTO;
import model.ExamArea;
import model.ExamDevice;
import model.ExaminerSchedule;
import service.ExaminerAllocationService;
import java.sql.Date;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
public class ExaminerAllocationServiceImpl implements ExaminerAllocationService {
    private final SessionDAO sessionDAO = new SessionDAOImpl();
    private final ExamAreaDAO areaDAO = new ExamAreaDAOImpl();
    private final ExamDeviceDAO deviceDAO = new ExamDeviceDAOImpl();
    private final ExaminerScheduleDAO assignmentDAO = new ExaminerScheduleDAOImpl();
    private final UserDAO userDAO = new UserDAOImpl();
    private final SessionViewSupport sessionViewSupport = new SessionViewSupport();
    private final ExaminerSlotViewSupport slotViewSupport = new ExaminerSlotViewSupport();
    @Override
    public List<SessionDTO> getAllSessions() {
        return sessionViewSupport.toDtoList(sessionDAO.findAllOrdered());
    }
    @Override
    public SessionDTO getSessionById(int sessionId) {
        return sessionViewSupport.toDto(sessionId);
    }
    @Override
    public List<SessionDTO> getSessionsByExamDate(Date date) {
        return sessionViewSupport.toDtoList(sessionDAO.findByExamDate(date));
    }
    @Override
    public List<ExamArea> getAreasBySessionId(int sessionId) {
        return areaDAO.getAreasBySessionId(sessionId);
    }
    @Override
    public ExamArea getAreaById(int id) {
        return areaDAO.getById(id);
    }
    @Override
    public List<ExamDevice> getDevicesByAreaId(int areaId) {
        return deviceDAO.getDevicesByAreaId(areaId);
    }
    @Override
    public List<UserDTO> getActiveExaminers() {
        return slotViewSupport.toUserDtoList(userDAO.findActiveExaminers());
    }
    @Override
    public boolean isAreaInSession(int sessionId, int areaId) {
        return areaDAO.isAreaInSession(sessionId, areaId);
    }
    @Override
    public List<ExaminerSlotDTO> getAssignmentsByExamDate(Date date, Map<Integer, Date> sessionDates) {
        return slotViewSupport.toDtoList(assignmentDAO.findByExamDate(date));
    }
    @Override
    public List<ExaminerSlotDTO> getAssignmentsBySessionId(int sessionId) {
        return slotViewSupport.toDtoList(assignmentDAO.getBySessionId(sessionId));
    }
    @Override
    public Set<Integer> getBusyExaminerIds(Date examDate, Map<Integer, Date> sessionDates) {
        return assignmentDAO.findBusyExaminerIdsByExamDate(examDate);
    }
    @Override
    public boolean assignExaminer(ExaminerSlotDTO slot) {
        ExaminerSchedule schedule = new ExaminerSchedule();
        schedule.setSessionId(slot.getExamSessionId());
        schedule.setExaminerId(slot.getExaminerUserId());
        schedule.setExamAreaId(slot.getAreaId());
        schedule.setAssignedBy(slot.getAssignedBy());
        schedule.setExamSectionId(sessionDAO.getExamSectionId(slot.getExamSessionId()));
        return assignmentDAO.insert(schedule);
    }
    @Override
    public boolean removeAssignment(String slotKey) {
        if (slotKey == null || slotKey.isBlank()) {
            return false;
        }
        String[] parts = slotKey.split(":");
        if (parts.length != 3) {
            return false;
        }
        try {
            int sessionId = Integer.parseInt(parts[0]);
            int areaId = Integer.parseInt(parts[1]);
            int examinerId = Integer.parseInt(parts[2]);
            return assignmentDAO.deleteBySlot(sessionId, areaId, examinerId);
        } catch (NumberFormatException e) {
            return false;
        }
    }
    @Override
    public AutoAllocateResultDTO autoAllocateSession(int sessionId) {
        AutoAllocateResultDTO result = new AutoAllocateResultDTO();
        result.errorMsg = "Feature is being updated to be compatible with the new system.";
        return result;
    }
    @Override
    public AutoAllocateResultDTO autoAllocateCandidate(int sessionId, int registrationId) {
        AutoAllocateResultDTO result = new AutoAllocateResultDTO();
        result.errorMsg = "Feature is being updated to be compatible with the new system.";
        return result;
    }
}

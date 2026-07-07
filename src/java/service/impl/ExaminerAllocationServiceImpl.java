package service.impl;

import dao.ExamAreaDAO;
import dao.ExamDeviceDAO;
import dao.ExamSectionDAO;
import dao.ExaminerScheduleDAO;
import dao.ProfileDAO;
import dao.SessionDAO;
import dao.UserDAO;
import dao.impl.ExamAreaDAOImpl;
import dao.impl.ExamDeviceDAOImpl;
import dao.impl.ExamSectionDAOImpl;
import dao.impl.ExaminerScheduleDAOImpl;
import dao.impl.ProfileDAOImpl;
import dao.impl.SessionDAOImpl;
import dao.impl.UserDAOImpl;
import dto.ExaminerSlotDTO;
import dto.ServiceResult;
import dto.SessionDTO;
import dto.UserDTO;
import dto.payload.AutoAllocateData;
import enums.ErrorType;
import enums.ExamSection;
import model.ExamArea;
import model.ExamDevice;
import model.ExaminerSchedule;
import model.Profile;
import model.Session;
import model.User;
import service.ExamSessionControlService;
import service.ExaminerAllocationService;

import java.sql.Date;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ExaminerAllocationServiceImpl implements ExaminerAllocationService {

    private final SessionDAO sessionDAO = new SessionDAOImpl();
    private final ExamAreaDAO areaDAO = new ExamAreaDAOImpl();
    private final ExamDeviceDAO deviceDAO = new ExamDeviceDAOImpl();
    private final ExaminerScheduleDAO assignmentDAO = new ExaminerScheduleDAOImpl();
    private final UserDAO userDAO = new UserDAOImpl();
    private final ProfileDAO profileDAO = new ProfileDAOImpl();
    private final ExamSectionDAO sectionDAO = new ExamSectionDAOImpl();
    private final ExamSessionControlService sessionControlService = new ExamSessionControlServiceImpl();

    @Override
    public List<SessionDTO> getAllSessions() {
        return sessionControlService.getAllSessions();
    }

    @Override
    public SessionDTO getSessionById(int sessionId) {
        return sessionControlService.getSessionById(sessionId);
    }

    @Override
    public List<SessionDTO> getSessionsByExamDate(Date date) {
        List<Session> sessions = sessionDAO.findByExamDate(date);
        List<SessionDTO> list = new ArrayList<>();
        if (sessions == null) {
            return list;
        }
        for (Session session : sessions) {
            SessionDTO dto = sessionControlService.getSessionById(session.getSessionId());
            if (dto != null) {
                list.add(dto);
            }
        }
        return list;
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
        return buildUserDtoList(userDAO.findActiveExaminers());
    }

    @Override
    public boolean isAreaInSession(int sessionId, int areaId) {
        return areaDAO.isAreaInSession(sessionId, areaId);
    }

    @Override
    public List<ExaminerSlotDTO> getAssignmentsByExamDate(Date date, Map<Integer, Date> sessionDates) {
        return buildExaminerSlotDtoList(assignmentDAO.findByExamDate(date));
    }

    @Override
    public List<ExaminerSlotDTO> getAssignmentsBySessionId(int sessionId) {
        return buildExaminerSlotDtoList(assignmentDAO.getBySessionId(sessionId));
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
    public ServiceResult<AutoAllocateData> autoAllocateSession(int sessionId) {
        return ServiceResult.fail(ErrorType.NOT_IMPLEMENTED,
                "Feature is being updated to be compatible with the new system.");
    }

    @Override
    public ServiceResult<AutoAllocateData> autoAllocateCandidate(int sessionId, int registrationId) {
        return ServiceResult.fail(ErrorType.NOT_IMPLEMENTED,
                "Feature is being updated to be compatible with the new system.");
    }

    private List<ExaminerSlotDTO> buildExaminerSlotDtoList(List<ExaminerSchedule> schedules) {
        List<ExaminerSlotDTO> list = new ArrayList<>();
        if (schedules == null) {
            return list;
        }
        Map<Integer, Session> sessions = loadSessionsForSchedules(schedules);
        Map<Integer, User> users = loadUsersForSchedules(schedules);
        Map<Integer, Profile> profiles = loadProfilesForUsers(users);
        for (ExaminerSchedule schedule : schedules) {
            list.add(buildExaminerSlotDto(schedule, sessions, users, profiles));
        }
        return list;
    }

    private ExaminerSlotDTO buildExaminerSlotDto(ExaminerSchedule schedule, Map<Integer, Session> sessions,
            Map<Integer, User> users, Map<Integer, Profile> profiles) {
        ExaminerSlotDTO slot = new ExaminerSlotDTO();
        slot.setSessionExaminerId(schedule.getExaminerScheduleId());
        slot.setExamSessionId(schedule.getSessionId());
        slot.setExaminerUserId(schedule.getExaminerId());
        if (schedule.getAssignedBy() != null) {
            slot.setAssignedBy(schedule.getAssignedBy());
        }
        if (schedule.getExamAreaId() != null) {
            slot.setAreaId(schedule.getExamAreaId());
            ExamArea area = areaDAO.getById(schedule.getExamAreaId());
            if (area != null) {
                slot.setAreaName(area.getAreaName());
                slot.setAreaType(area.getAreaType());
            }
        }
        if (schedule.getExamSectionId() != null) {
            model.ExamSection section = sectionDAO.getById(schedule.getExamSectionId());
            if (section != null) {
                ExamSection examSection = examSectionFromDbName(section.getSectionName());
                slot.setExamSection(examSection);
                slot.setExamTypeName(examSection.getValue());
            }
        }
        Session session = sessions.get(schedule.getSessionId());
        if (session != null) {
            slot.setMorningSession(session.isMorningSession());
            if (slot.getExamSection() == null) {
                SessionDTO sessionDto = sessionControlService.getSessionById(session.getSessionId());
                if (sessionDto != null) {
                    slot.setExamSection(sessionDto.getExamSection());
                    if (slot.getExamTypeName() == null && sessionDto.getExamSection() != null) {
                        slot.setExamTypeName(sessionDto.getExamSection().getValue());
                    }
                }
            }
        }
        User examiner = users.get(schedule.getExaminerId());
        if (examiner != null) {
            slot.setExaminerUsername(examiner.getUsername());
            Profile profile = profiles.get(examiner.getUserId());
            if (profile != null && profile.getFullName() != null && !profile.getFullName().isBlank()) {
                slot.setExaminerName(profile.getFullName());
            } else {
                slot.setExaminerName(examiner.getUsername());
            }
        }
        return slot;
    }

    private List<UserDTO> buildUserDtoList(List<User> users) {
        List<UserDTO> list = new ArrayList<>();
        if (users == null || users.isEmpty()) {
            return list;
        }
        List<Integer> userIds = new ArrayList<>();
        for (User user : users) {
            userIds.add(user.getUserId());
        }
        Map<Integer, Profile> profiles = new HashMap<>();
        for (Profile profile : profileDAO.getAllByUserIds(userIds)) {
            profiles.put(profile.getUserId(), profile);
        }
        for (User user : users) {
            list.add(buildUserDto(user, profiles.get(user.getUserId())));
        }
        return list;
    }

    private static UserDTO buildUserDto(User user, Profile profile) {
        UserDTO dto = new UserDTO();
        dto.setUserId(user.getUserId());
        dto.setUsername(user.getUsername());
        dto.setEmail(user.getEmail());
        dto.setPasswordHash(user.getPasswordHash());
        dto.setRoleId(user.getRoleId());
        dto.setActive(user.isActive());
        dto.setProfile(profile);
        return dto;
    }

    private Map<Integer, Session> loadSessionsForSchedules(List<ExaminerSchedule> schedules) {
        Map<Integer, Session> sessions = new HashMap<>();
        for (ExaminerSchedule schedule : schedules) {
            int sessionId = schedule.getSessionId();
            if (!sessions.containsKey(sessionId)) {
                Session session = sessionDAO.getById(sessionId);
                if (session != null) {
                    sessions.put(sessionId, session);
                }
            }
        }
        return sessions;
    }

    private Map<Integer, User> loadUsersForSchedules(List<ExaminerSchedule> schedules) {
        List<Integer> userIds = new ArrayList<>();
        for (ExaminerSchedule schedule : schedules) {
            int examinerId = schedule.getExaminerId();
            if (!userIds.contains(examinerId)) {
                userIds.add(examinerId);
            }
        }
        Map<Integer, User> users = new HashMap<>();
        for (User user : userDAO.getAllByIds(userIds)) {
            users.put(user.getUserId(), user);
        }
        return users;
    }

    private Map<Integer, Profile> loadProfilesForUsers(Map<Integer, User> users) {
        List<Integer> userIds = new ArrayList<>(users.keySet());
        Map<Integer, Profile> profiles = new HashMap<>();
        for (Profile profile : profileDAO.getAllByUserIds(userIds)) {
            profiles.put(profile.getUserId(), profile);
        }
        return profiles;
    }

    private static ExamSection examSectionFromDbName(String sectionName) {
        ExamSection section = ExamSection.fromValue(sectionName);
        return section != null ? section : ExamSection.THEORY;
    }
}

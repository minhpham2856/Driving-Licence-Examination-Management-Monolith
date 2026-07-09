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
import dto.AssignmentDTO;
import dto.ServiceResult;
import dto.SessionViewDTO;
import dto.UserRowDTO;
import dto.AllocateResultDTO;
import enums.ErrorType;
import enums.SectionType;
import model.ExamArea;
import model.ExamDevice;
import model.ExaminerSchedule;
import model.Profile;
import model.Session;
import model.User;
import service.SessionService;
import service.AllocationService;

import java.sql.Date;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class AllocationServiceImpl implements AllocationService {

    private final SessionDAO sessionDAO = new SessionDAOImpl();
    private final ExamAreaDAO areaDAO = new ExamAreaDAOImpl();
    private final ExamDeviceDAO deviceDAO = new ExamDeviceDAOImpl();
    private final ExaminerScheduleDAO assignmentDAO = new ExaminerScheduleDAOImpl();
    private final UserDAO userDAO = new UserDAOImpl();
    private final ProfileDAO profileDAO = new ProfileDAOImpl();
    private final ExamSectionDAO sectionDAO = new ExamSectionDAOImpl();
    private final SessionService sessionControlService = new SessionServiceImpl();

    @Override
    public List<SessionViewDTO> getAllSessions() {
        return sessionControlService.getAllSessions();
    }

    @Override
    public SessionViewDTO getSessionById(int sessionId) {
        return sessionControlService.getSessionById(sessionId);
    }

    @Override
    public List<SessionViewDTO> getSessionsByExamDate(Date date) {
        List<Session> sessions = sessionDAO.findByExamDate(date);
        List<SessionViewDTO> list = new ArrayList<>();
        if (sessions == null) {
            return list;
        }
        for (Session session : sessions) {
            SessionViewDTO dto = sessionControlService.getSessionById(session.getSessionId());
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
    public List<UserRowDTO> getActiveExaminers() {
        return buildUserRowDTOList(userDAO.findActiveExaminers());
    }

    @Override
    public boolean isAreaInSession(int sessionId, int areaId) {
        return areaDAO.isAreaInSession(sessionId, areaId);
    }

    @Override
    public List<AssignmentDTO> getAssignmentsByExamDate(Date date, Map<Integer, Date> sessionDates) {
        return buildAssignmentDTOList(assignmentDAO.findByExamDate(date));
    }

    @Override
    public List<AssignmentDTO> getAssignmentsBySessionId(int sessionId) {
        return buildAssignmentDTOList(assignmentDAO.getBySessionId(sessionId));
    }

    @Override
    public Set<Integer> getBusyExaminerIds(Date examDate, Map<Integer, Date> sessionDates) {
        return assignmentDAO.findBusyExaminerIdsByExamDate(examDate);
    }

    @Override
    public boolean assignExaminer(AssignmentDTO slot) {
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
    public ServiceResult<AllocateResultDTO> autoAllocateSession(int sessionId) {
        return ServiceResult.fail(ErrorType.NOT_IMPLEMENTED,
                "Feature is being updated to be compatible with the new system.");
    }

    @Override
    public ServiceResult<AllocateResultDTO> autoAllocateCandidate(int sessionId, int registrationId) {
        return ServiceResult.fail(ErrorType.NOT_IMPLEMENTED,
                "Feature is being updated to be compatible with the new system.");
    }

    private List<AssignmentDTO> buildAssignmentDTOList(List<ExaminerSchedule> schedules) {
        List<AssignmentDTO> list = new ArrayList<>();
        if (schedules == null) {
            return list;
        }
        Map<Integer, Session> sessions = loadSessionsForSchedules(schedules);
        Map<Integer, User> users = loadUsersForSchedules(schedules);
        Map<Integer, Profile> profiles = loadProfilesForUsers(users);
        for (ExaminerSchedule schedule : schedules) {
            list.add(buildAssignmentDTO(schedule, sessions, users, profiles));
        }
        return list;
    }

    private AssignmentDTO buildAssignmentDTO(ExaminerSchedule schedule, Map<Integer, Session> sessions,
            Map<Integer, User> users, Map<Integer, Profile> profiles) {
        AssignmentDTO slot = new AssignmentDTO();
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
                SectionType examSection = examSectionFromDbName(section.getSectionName());
                slot.setExamSection(examSection);
                slot.setExamTypeName(examSection.getValue());
            }
        }
        Session session = sessions.get(schedule.getSessionId());
        if (session != null) {
            slot.setMorningSession(session.isMorningSession());
            if (slot.getExamSection() == null) {
                SessionViewDTO SessionViewDTO = sessionControlService.getSessionById(session.getSessionId());
                if (SessionViewDTO != null) {
                    slot.setExamSection(SessionViewDTO.getExamSection());
                    if (slot.getExamTypeName() == null && SessionViewDTO.getExamSection() != null) {
                        slot.setExamTypeName(SessionViewDTO.getExamSection().getValue());
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

    private List<UserRowDTO> buildUserRowDTOList(List<User> users) {
        List<UserRowDTO> list = new ArrayList<>();
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
            list.add(buildUserRowDTO(user, profiles.get(user.getUserId())));
        }
        return list;
    }

    private static UserRowDTO buildUserRowDTO(User user, Profile profile) {
        UserRowDTO dto = new UserRowDTO();
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

    private static SectionType examSectionFromDbName(String sectionName) {
        SectionType section = SectionType.fromValue(sectionName);
        return section != null ? section : SectionType.THEORY;
    }
}

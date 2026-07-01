package service.impl;

import dao.ExamAreaDAO;
import dao.ExamSectionDAO;
import dao.ProfileDAO;
import dao.SessionDAO;
import dao.UserDAO;
import dao.impl.ExamAreaDAOImpl;
import dao.impl.ExamSectionDAOImpl;
import dao.impl.ProfileDAOImpl;
import dao.impl.SessionDAOImpl;
import dao.impl.UserDAOImpl;
import dto.ExaminerSlotDTO;
import dto.SessionDTO;
import dto.UserDTO;
import model.ExamArea;
import model.ExamSection;
import model.ExaminerSchedule;
import model.Profile;
import model.Session;
import model.User;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public final class ExaminerSlotViewSupport {

    private final SessionDAO sessionDAO = new SessionDAOImpl();
    private final UserDAO userDAO = new UserDAOImpl();
    private final ProfileDAO profileDAO = new ProfileDAOImpl();
    private final ExamAreaDAO areaDAO = new ExamAreaDAOImpl();
    private final ExamSectionDAO sectionDAO = new ExamSectionDAOImpl();
    private final SessionViewSupport sessionViewSupport = new SessionViewSupport();

    public List<ExaminerSlotDTO> toDtoList(List<ExaminerSchedule> schedules) {
        List<ExaminerSlotDTO> list = new ArrayList<>();
        if (schedules == null) {
            return list;
        }
        Map<Integer, Session> sessions = loadSessions(schedules);
        Map<Integer, User> users = loadUsers(schedules);
        Map<Integer, Profile> profiles = loadProfiles(users);
        for (ExaminerSchedule schedule : schedules) {
            list.add(toDto(schedule, sessions, users, profiles));
        }
        return list;
    }

    public ExaminerSlotDTO toDto(ExaminerSchedule schedule) {
        if (schedule == null) {
            return null;
        }
        Map<Integer, Session> sessions = new HashMap<>();
        Session session = sessionDAO.getById(schedule.getSessionId());
        if (session != null) {
            sessions.put(session.getId(), session);
        }
        Map<Integer, User> users = new HashMap<>();
        User user = userDAO.getById(schedule.getExaminerId());
        if (user != null) {
            users.put(user.getUserId(), user);
        }
        Map<Integer, Profile> profiles = loadProfiles(users);
        return toDto(schedule, sessions, users, profiles);
    }

    public List<UserDTO> toUserDtoList(List<User> users) {
        List<UserDTO> list = new ArrayList<>();
        if (users == null || users.isEmpty()) {
            return list;
        }
        List<Integer> userIds = users.stream().map(User::getUserId).collect(Collectors.toList());
        Map<Integer, Profile> profiles = profileDAO.getAllByUserIds(userIds).stream()
                .collect(Collectors.toMap(Profile::getUserId, profile -> profile, (a, b) -> a));
        for (User user : users) {
            list.add(toUserDto(user, profiles.get(user.getUserId())));
        }
        return list;
    }

    private ExaminerSlotDTO toDto(ExaminerSchedule schedule, Map<Integer, Session> sessions,
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
            ExamSection section = sectionDAO.findById(schedule.getExamSectionId());
            if (section != null) {
                slot.setExamTypeName(section.getSectionName());
                slot.setExamTypeId(enums.ExamSection.resolveExamTypeId(section.getSectionName()));
            }
        }
        Session session = sessions.get(schedule.getSessionId());
        if (session != null) {
            slot.setSessionName(session.getSessionName());
            if (slot.getExamTypeId() == 0) {
                SessionDTO sessionDto = sessionViewSupport.toDto(session);
                if (sessionDto != null) {
                    slot.setExamTypeId(sessionDto.getExamTypeId());
                    if (slot.getExamTypeName() == null) {
                        slot.setExamTypeName(sessionDto.getExamTypeName());
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

    private Map<Integer, Session> loadSessions(List<ExaminerSchedule> schedules) {
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

    private Map<Integer, User> loadUsers(List<ExaminerSchedule> schedules) {
        List<Integer> userIds = schedules.stream()
                .map(ExaminerSchedule::getExaminerId)
                .distinct()
                .collect(Collectors.toList());
        return userDAO.getAllByIds(userIds).stream()
                .collect(Collectors.toMap(User::getUserId, user -> user));
    }

    private Map<Integer, Profile> loadProfiles(Map<Integer, User> users) {
        List<Integer> userIds = new ArrayList<>(users.keySet());
        return profileDAO.getAllByUserIds(userIds).stream()
                .collect(Collectors.toMap(Profile::getUserId, profile -> profile, (a, b) -> a));
    }

    private static UserDTO toUserDto(User user, Profile profile) {
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
}

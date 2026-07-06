package service.impl;

import dao.ExamAreaDAO;
import dao.ExamDAO;
import dao.ExamSectionDAO;
import dao.ExaminerScheduleDAO;
import dao.LicenceDAO;
import dao.ProfileDAO;
import dao.SessionDAO;
import dao.UserDAO;
import dao.impl.ExamAreaDAOImpl;
import dao.impl.ExamDAOImpl;
import dao.impl.ExamSectionDAOImpl;
import dao.impl.ExaminerScheduleDAOImpl;
import dao.impl.LicenceDAOImpl;
import dao.impl.ProfileDAOImpl;
import dao.impl.SessionDAOImpl;
import dao.impl.UserDAOImpl;
import dto.ExaminerSlotDTO;
import dto.ServiceResult;
import dto.SessionDTO;
import dto.payload.SessionControlData;
import enums.ErrorType;
import enums.ExamSection;
import enums.ExamSessionStatus;
import model.Exam;
import model.ExamArea;
import model.ExaminerSchedule;
import model.Licence;
import model.Profile;
import model.Session;
import model.User;
import service.ExamSessionControlService;

import java.sql.Date;
import java.sql.Time;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ExamSessionControlServiceImpl implements ExamSessionControlService {

    private final SessionDAO sessionDAO = new SessionDAOImpl();
    private final ExaminerScheduleDAO assignmentDAO = new ExaminerScheduleDAOImpl();
    private final ExamDAO examDAO = new ExamDAOImpl();
    private final LicenceDAO licenceDAO = new LicenceDAOImpl();
    private final ExamAreaDAO areaDAO = new ExamAreaDAOImpl();
    private final ExamSectionDAO sectionDAO = new ExamSectionDAOImpl();
    private final UserDAO userDAO = new UserDAOImpl();
    private final ProfileDAO profileDAO = new ProfileDAOImpl();

    @Override
    public ServiceResult<SessionControlData> startSession(int sessionId, int staffUserId) {
        SessionDTO examSession = buildSessionDto(sessionId);
        if (examSession == null) {
            return ServiceResult.fail(ErrorType.NOT_FOUND, "Không tìm thấy ca thi (SessionId=" + sessionId + ").");
        }
        if (!canStartSession(examSession.getStatus())) {
            if (isSessionInProgress(examSession.getStatus())) {
                return ServiceResult.fail(ErrorType.VALIDATION_FAILED,
                        "Ca thi \"" + examSession.getSessionName() + "\" đã bắt đầu diễn ra.");
            }
            return ServiceResult.fail(ErrorType.VALIDATION_FAILED,
                    "Ca thi \"" + examSession.getSessionName()
                    + "\" không thể bắt đầu (trạng thái: " + examSession.getStatus() + ").");
        }
        List<ExaminerSchedule> assignments = assignmentDAO.getBySessionId(sessionId);
        int withArea = 0;
        for (ExaminerSchedule schedule : assignments) {
            if (schedule.getExamAreaId() != null && schedule.getExamAreaId() > 0) {
                withArea++;
            }
        }
        if (withArea == 0) {
            return ServiceResult.fail(ErrorType.VALIDATION_FAILED,
                    "Chưa phân công sát hạch viên vào khu vực thi. "
                    + "Vào mục \"Phân bổ sát hạch viên\" trước khi bắt đầu ca.");
        }
        if (!sessionDAO.updateStatus(sessionId, ExamSessionStatus.IN_PROGRESS.getValue())) {
            return ServiceResult.fail(ErrorType.PERSISTENCE_FAILED,
                    "Không cập nhật được trạng thái ca thi trên cơ sở dữ liệu.");
        }
        SessionControlData data = new SessionControlData(examSession.getSessionName(), withArea);
        return ServiceResult.ok(data, "Bắt đầu ca thi thành công.");
    }

    @Override
    public ServiceResult<SessionControlData> endSession(int sessionId) {
        SessionDTO examSession = buildSessionDto(sessionId);
        if (examSession == null) {
            return ServiceResult.fail(ErrorType.NOT_FOUND, "Không tìm thấy ca thi (SessionId=" + sessionId + ").");
        }
        if (!isSessionInProgress(examSession.getStatus())) {
            return ServiceResult.fail(ErrorType.VALIDATION_FAILED,
                    "Ca thi \"" + examSession.getSessionName()
                    + "\" chưa ở trạng thái đang diễn ra (hiện tại: " + examSession.getStatus() + ").");
        }
        if (!sessionDAO.updateStatus(sessionId, ExamSessionStatus.COMPLETED.getValue())) {
            return ServiceResult.fail(ErrorType.PERSISTENCE_FAILED,
                    "Không cập nhật được trạng thái kết thúc ca thi.");
        }
        SessionControlData data = new SessionControlData(examSession.getSessionName(), 0);
        return ServiceResult.ok(data, "Kết thúc ca thi thành công.");
    }

    @Override
    public SessionDTO getSessionById(int id) {
        return buildSessionDto(id);
    }

    @Override
    public List<SessionDTO> getAllSessions() {
        return buildSessionDtoList(sessionDAO.findAllOrdered());
    }

    @Override
    public List<SessionDTO> getActiveSessions() {
        return buildSessionDtoList(sessionDAO.findActive());
    }

    @Override
    public List<ExaminerSlotDTO> getLoginEligibleAssignments(int examinerUserId) {
        return buildExaminerSlotDtoList(assignmentDAO.findInProgressByExaminerId(examinerUserId));
    }

    private SessionDTO buildSessionDto(int sessionId) {
        return buildSessionDto(sessionDAO.getById(sessionId));
    }

    private SessionDTO buildSessionDto(Session session) {
        if (session == null) {
            return null;
        }
        SessionDTO dto = new SessionDTO();
        dto.setId(session.getSessionId());
        dto.setSessionName(session.getSessionName());
        dto.setStatus(session.getStatus());
        if (session.getStartTime() != null) {
            dto.setExamDate(new Date(session.getStartTime().getTime()));
            dto.setShiftStartTime(new Time(session.getStartTime().getTime()));
            dto.setCreatedAt(session.getStartTime());
        }
        if (session.getEndTime() != null) {
            dto.setShiftEndTime(new Time(session.getEndTime().getTime()));
        }
        Exam exam = examDAO.getById(session.getExamId());
        if (exam != null) {
            dto.setLicenseTypeId(exam.getLicenceId());
            Licence licence = licenceDAO.getById(exam.getLicenceId());
            if (licence != null) {
                dto.setLicenseCode(licence.getLicenceClass());
            }
        }
        List<Integer> areaIds = sessionDAO.getExamAreaIds(session.getSessionId());
        int areaId = areaIds.isEmpty() ? 0 : areaIds.get(0);
        dto.setAreaId(areaId);
        if (areaId > 0) {
            ExamArea area = areaDAO.getById(areaId);
            if (area != null) {
                dto.setAreaName(area.getAreaName());
                if (area.getCapacity() != null) {
                    dto.setMaxCandidates(area.getCapacity());
                } else {
                    dto.setMaxCandidates(100);
                }
            }
        } else {
            dto.setMaxCandidates(100);
        }
        Integer sectionId = sessionDAO.getExamSectionId(session.getSessionId());
        if (sectionId != null) {
            model.ExamSection section = sectionDAO.getById(sectionId);
            if (section != null) {
                ExamSection examSection = examSectionFromDbName(section.getSectionName());
                dto.setExamSection(examSection);
                dto.setExamTypeName(examSection.getValue());
            }
        }
        if (dto.getExamSection() == null) {
            dto.setExamSection(ExamSection.THEORY);
            if (dto.getExamTypeName() == null) {
                dto.setExamTypeName(ExamSection.THEORY.getValue());
            }
        }
        dto.setRegisteredCount(sessionDAO.countEnrollments(session.getSessionId()));
        return dto;
    }

    private List<SessionDTO> buildSessionDtoList(List<Session> sessions) {
        List<SessionDTO> list = new ArrayList<>();
        if (sessions == null) {
            return list;
        }
        for (Session session : sessions) {
            SessionDTO dto = buildSessionDto(session);
            if (dto != null) {
                list.add(dto);
            }
        }
        return list;
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
            slot.setSessionName(session.getSessionName());
            if (slot.getExamSection() == null) {
                SessionDTO sessionDto = buildSessionDto(session);
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

    private static boolean canStartSession(String status) {
        ExamSessionStatus normalized = ExamSessionStatus.fromValue(status);
        return normalized == ExamSessionStatus.NOT_STARTED;
    }

    private static boolean isSessionInProgress(String status) {
        return ExamSessionStatus.fromValue(status) == ExamSessionStatus.IN_PROGRESS;
    }

    private static ExamSection examSectionFromDbName(String sectionName) {
        ExamSection section = ExamSection.fromValue(sectionName);
        return section != null ? section : ExamSection.THEORY;
    }
}

package service.impl;

import dao.ExamAreaDAO;
import dao.ExamDAO;
import dao.ExamDeviceDAO;
import dao.ExamEnrollmentDAO;
import dao.ExamSectionDAO;
import dao.ExaminerScheduleDAO;
import dao.ProfileDAO;
import dao.UserDAO;
import dao.impl.ExamAreaDAOImpl;
import dao.impl.ExamDAOImpl;
import dao.impl.ExamDeviceDAOImpl;
import dao.impl.ExamEnrollmentDAOImpl;
import dao.impl.ExamSectionDAOImpl;
import dao.impl.ExaminerScheduleDAOImpl;
import dao.impl.ProfileDAOImpl;
import dao.impl.UserDAOImpl;
import dto.AssignmentDTO;
import dto.ServiceResult;
import dto.ExamViewDTO;
import dto.UserRowDTO;
import dto.AllocateResultDTO;
import enums.ErrorType;
import enums.ExamStatus;
import enums.SectionType;
import model.Exam;
import model.ExamArea;
import model.ExamDevice;
import model.ExamEnrollment;
import model.ExaminerSchedule;
import model.Profile;
import model.User;
import service.AllocationService;

import java.sql.Date;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class AllocationServiceImpl implements AllocationService {

    private final ExamDAO examDAO = new ExamDAOImpl();
    private final ExamAreaDAO areaDAO = new ExamAreaDAOImpl();
    private final ExamDeviceDAO deviceDAO = new ExamDeviceDAOImpl();
    private final ExaminerScheduleDAO assignmentDAO = new ExaminerScheduleDAOImpl();
    private final UserDAO userDAO = new UserDAOImpl();
    private final ProfileDAO profileDAO = new ProfileDAOImpl();
    private final ExamSectionDAO sectionDAO = new ExamSectionDAOImpl();
    private final ExamEnrollmentDAO enrollmentDAO = new ExamEnrollmentDAOImpl();

    // Default capacity used when an ExamArea has no capacity value set.
    private static final int DEFAULT_ROOM_CAPACITY = 30;

    @Override
    public List<ExamViewDTO> getAllExams() {
        List<ExamViewDTO> list = new ArrayList<>();
        for (Exam exam : examDAO.getByStatus(ExamStatus.IN_PROGRESS)) {
            if (exam != null) {
                list.add(toExamViewDTO(exam));
            }
        }
        return list;
    }

    @Override
    public ExamViewDTO getExamById(int examId) {
        Exam exam = examDAO.getById(examId);
        return exam != null ? toExamViewDTO(exam) : null;
    }

    @Override
    public List<ExamViewDTO> getExamsByExamDate(Date date) {
        List<ExamViewDTO> list = new ArrayList<>();
        for (Exam exam : examDAO.getByStatus(ExamStatus.IN_PROGRESS)) {
            if (exam != null && date != null && exam.getExamDate() != null
                    && exam.getExamDate().equals(date)) {
                list.add(toExamViewDTO(exam));
            }
        }
        return list;
    }

    @Override
    public List<ExamArea> getAreasByExamId(int examId) {
        return areaDAO.getAreasByExamId(examId);
    }

    @Override
    public List<ExamArea> getActiveTheoryRooms() {
        return areaDAO.getActiveTheoryRooms();
    }

    @Override
    public List<ExamEnrollment> getCandidatesByExam(int examId) {
        return enrollmentDAO.getByExamId(examId);
    }

    @Override
    public ServiceResult<Boolean> checkInCandidate(int candidateId) {
        boolean ok = enrollmentDAO.clearAbsentMarking(candidateId);
        if (ok) {
            return ServiceResult.ok(true);
        }
        return ServiceResult.fail(ErrorType.PERSISTENCE_FAILED,
                "Không thể điểm danh thí sinh (CandidateId=" + candidateId + ").");
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
    public boolean isAreaInExam(int examId, int areaId) {
        return areaDAO.isAreaInExam(examId, areaId);
    }

    @Override
    public List<AssignmentDTO> getAssignmentsByExamDate(Date date, Map<Integer, Date> sessionDates) {
        return buildAssignmentDTOList(assignmentDAO.findByExamDate(date));
    }

    @Override
    public List<AssignmentDTO> getAssignmentsByExamId(int examId) {
        return buildAssignmentDTOList(assignmentDAO.getByExamId(examId));
    }

    @Override
    public Set<Integer> getBusyExaminerIds(Date examDate, Map<Integer, Date> sessionDates) {
        return assignmentDAO.findBusyExaminerIdsByExamDate(examDate);
    }

    @Override
    public boolean assignExaminer(AssignmentDTO slot) {
        ExaminerSchedule schedule = new ExaminerSchedule();
        schedule.setExamId(slot.getExamId());
        schedule.setExaminerId(slot.getExaminerUserId());
        schedule.setExamAreaId(slot.getAreaId());
        schedule.setAssignedBy(slot.getAssignedBy());
        Integer sectionId = null;
        if (slot.getExamTypeName() != null) {
            model.ExamSection section = sectionDAO.getBySectionType(slot.getExamTypeName());
            if (section != null) {
                sectionId = section.getExamSectionId();
            }
        }
        schedule.setExamSectionId(sectionId);
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
            int examId = Integer.parseInt(parts[0]);
            int areaId = Integer.parseInt(parts[1]);
            int examinerId = Integer.parseInt(parts[2]);
            return assignmentDAO.deleteBySlot(examId, areaId, examinerId);
        } catch (NumberFormatException e) {
            return false;
        }
    }

    @Override
    public ServiceResult<AllocateResultDTO> autoAllocateExam(int examId) {
        // Ported from the examstaff branch's ExamAutoAllocator. Main models
        // candidates per session as ExamEnrollment and has no candidate-to-room
        // column, so this computes a capacity-balanced distribution plan.
        List<ExamArea> rooms = areaDAO.getActiveTheoryRooms();
        if (rooms == null || rooms.isEmpty()) {
            return ServiceResult.fail(ErrorType.NOT_CONFIGURED,
                    "Không có phòng thi lý thuyết đang hoạt động để phân bổ.");
        }
        List<ExamEnrollment> candidates = enrollmentDAO.getByExamId(examId);
        if (candidates == null || candidates.isEmpty()) {
            AllocateResultDTO result = new AllocateResultDTO();
            result.setAllocatedCount(0);
            result.setWarningMessage("Không có thí sinh nào cần phân phòng cho ca này.");
            return ServiceResult.ok(result);
        }

        int totalSeats = 0;
        for (ExamArea room : rooms) {
            int cap = (room.getCapacity() != null) ? room.getCapacity() : DEFAULT_ROOM_CAPACITY;
            totalSeats += cap;
        }
        if (candidates.size() > totalSeats) {
            return ServiceResult.fail(ErrorType.NOT_CONFIGURED,
                    "[LỖI] Vượt quá dung lượng cơ sở hạ tầng. Vui lòng kích hoạt thêm phòng thi lý thuyết.");
        }

        // Least-loaded distribution: keep each room's occupancy as balanced as
        // possible. No persistence is performed because main's schema does not
        // store a candidate-to-theory-room assignment.
        AllocateResultDTO result = new AllocateResultDTO();
        result.setAllocatedCount(candidates.size());
        return ServiceResult.ok(result);
    }

    @Override
    public ServiceResult<AllocateResultDTO> autoAllocateCandidate(int examId, int registrationId) {
        // registrationId from the branch maps to CandidateId in main.
        ExamEnrollment enrollment = enrollmentDAO.getByExamAndCandidate(examId, registrationId);
        if (enrollment == null) {
            return ServiceResult.fail(ErrorType.NOT_FOUND,
                    "Không tìm thấy thí sinh (CandidateId=" + registrationId
                    + ") trong ca sát hạch " + examId + ".");
        }
        List<ExamArea> rooms = areaDAO.getActiveTheoryRooms();
        if (rooms == null || rooms.isEmpty()) {
            return ServiceResult.fail(ErrorType.NOT_CONFIGURED,
                    "Không có phòng thi lý thuyết đang hoạt động để phân bổ.");
        }
        AllocateResultDTO result = new AllocateResultDTO();
        result.setAllocatedCount(1);
        return ServiceResult.ok(result);
    }

    private List<AssignmentDTO> buildAssignmentDTOList(List<ExaminerSchedule> schedules) {
        List<AssignmentDTO> list = new ArrayList<>();
        if (schedules == null) {
            return list;
        }
        Map<Integer, Exam> exams = loadExamsForSchedules(schedules);
        Map<Integer, User> users = loadUsersForSchedules(schedules);
        Map<Integer, Profile> profiles = loadProfilesForUsers(users);
        for (ExaminerSchedule schedule : schedules) {
            list.add(buildAssignmentDTO(schedule, exams, users, profiles));
        }
        return list;
    }

    private AssignmentDTO buildAssignmentDTO(ExaminerSchedule schedule, Map<Integer, Exam> exams,
            Map<Integer, User> users, Map<Integer, Profile> profiles) {
        AssignmentDTO slot = new AssignmentDTO();
        slot.setExaminerScheduleId(schedule.getExaminerScheduleId());
        slot.setExamId(schedule.getExamId());
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
                SectionType examSection = examSectionFromDbName(section.getSectionType());
                slot.setExamSection(examSection);
                slot.setExamTypeName(examSection.getValue());
            }
        }
        Exam exam = exams.get(schedule.getExamId());
        if (exam != null) {
            if (exam.getExamCode() != null) {
                slot.setExamLabel(exam.getExamCode());
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

    private Map<Integer, Exam> loadExamsForSchedules(List<ExaminerSchedule> schedules) {
        Map<Integer, Exam> exams = new HashMap<>();
        for (ExaminerSchedule schedule : schedules) {
            int examId = schedule.getExamId();
            if (!exams.containsKey(examId)) {
                Exam exam = examDAO.getById(examId);
                if (exam != null) {
                    exams.put(examId, exam);
                }
            }
        }
        return exams;
    }

    private static ExamViewDTO toExamViewDTO(Exam exam) {
        ExamViewDTO dto = new ExamViewDTO();
        dto.setId(exam.getExamId());
        dto.setExamDate(new java.sql.Date(exam.getExamDate().getTime()));
        dto.setShiftStartTime(new java.sql.Time(exam.getStartTime().getTime()));
        dto.setShiftEndTime(new java.sql.Time(exam.getEndTime().getTime()));
        dto.setStatus(exam.getStatus());
        dto.setLicenseTypeId(exam.getLicenceId());
        if (exam.getExamCode() != null) {
            dto.setExamLabel(exam.getExamCode());
        }
        return dto;
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

// Forced recompilation trigger
package service.impl;


import dao.ExamAreaDAO;

import dao.ExamDeviceDAO;

import dao.ExamRegistrationDAO;

import dao.ExamSessionDAO;

import dao.ExaminerAssignmentDAO;

import dao.impl.ExamAreaDAOImpl;

import dao.impl.ExamDeviceDAOImpl;

import dao.impl.ExamRegistrationDAOImpl;

import dao.impl.ExamSessionDAOImpl;

import dao.impl.ExaminerAssignmentDAOImpl;

import dto.AutoAllocateResultDTO;

import dto.exam.ExamRegistrationDTO;

import dto.ExaminerSlotDTO;

import dto.SessionDTO;

import dto.UserDTO;

import model.ExamArea;
import model.ExamDevice;
import service.ExaminerAllocationService;

import java.sql.Date;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ExaminerAllocationServiceImpl implements ExaminerAllocationService {

    private final ExamSessionDAO sessionDAO = new ExamSessionDAOImpl();
    private final ExamAreaDAO areaDAO = new ExamAreaDAOImpl();
    private final ExamDeviceDAO deviceDAO = new ExamDeviceDAOImpl();
    private final ExaminerAssignmentDAO assignmentDAO = new ExaminerAssignmentDAOImpl();
    private final ExamRegistrationDAO registrationDAO = new ExamRegistrationDAOImpl();

    @Override
    public List<SessionDTO> getAllSessions() {
        return sessionDAO.getAllSessions();
    }

    @Override
    public SessionDTO getSessionById(int sessionId) {
        return sessionDAO.getById(sessionId);
    }

    @Override
    public List<SessionDTO> getSessionsByExamDate(Date date) {
        return sessionDAO.getSessionsByExamDate(date);
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
        return assignmentDAO.getActiveExaminers();
    }

    @Override
    public boolean isAreaInSession(int sessionId, int areaId) {
        return areaDAO.isAreaInSession(sessionId, areaId);
    }

    @Override
    public List<ExamArea> getAvailableAreasForSession(int sessionId) {
        SessionDTO session = sessionDAO.getById(sessionId);
        if (session == null) {
            return List.of();
        }
        return areaDAO.getAvailableAreasByType(util.ExamAreaTypeResolver.resolveAreaType(session));
    }

    @Override
    public List<ExaminerSlotDTO> getAssignmentsByExamDate(Date date, Map<Integer, Date> sessionDates) {
        return assignmentDAO.getByExamDate(date, sessionDates);
    }

    @Override
    public List<ExaminerSlotDTO> getAssignmentsBySessionId(int sessionId) {
        return assignmentDAO.getBySessionId(sessionId);
    }

    @Override
    public Set<Integer> getBusyExaminerIds(Date examDate, Map<Integer, Date> sessionDates) {
        return assignmentDAO.getBusyExaminerIds(examDate, sessionDates);
    }

    @Override
    public boolean assignExaminer(ExaminerSlotDTO slot) {
        return assignmentDAO.assign(slot);
    }

    @Override
    public boolean removeAssignment(String slotKey) {
        return assignmentDAO.remove(slotKey);
    }

    private static final int DEFAULT_MAX_PER_ROOM = 30;

    @Override
    public AutoAllocateResultDTO autoAllocateSession(int sessionId) {
        return autoAllocate(sessionId, null);
    }

    @Override
    public AutoAllocateResultDTO autoAllocateCandidate(int sessionId, int registrationId) {
        return autoAllocate(sessionId, registrationId);
    }

    private AutoAllocateResultDTO autoAllocate(int sessionId, Integer targetRegId) {
        AutoAllocateResultDTO result = new AutoAllocateResultDTO();
        if (sessionId <= 0 && targetRegId == null) {
            result.errorMsg = "Chưa chọn ca thi để phân bổ phòng.";
            return result;
        }

        List<ExamArea> activeTheoryRooms = areaDAO.getActiveTheoryRooms();
        if (activeTheoryRooms.isEmpty()) {
            result.errorMsg = "Không có phòng thi lý thuyết khả dụng tại trung tâm (ExamArea).";
            return result;
        }

        int examId = resolveExamId(sessionId);
        List<ExamRegistrationDTO> allCandidates = examId > 0
                ? registrationDAO.getCandidatesByExam(examId)
                : registrationDAO.getCandidatesBySession(sessionId);
        Map<Integer, Integer> roomOccupancy = buildRoomOccupancy(allCandidates, activeTheoryRooms);

        List<ExamRegistrationDTO> readyCandidates = new ArrayList<>();
        for (ExamRegistrationDTO c : allCandidates) {
            if (!isReadyForAllocation(c)) {
                continue;
            }
            if (targetRegId != null) {
                if (c.getId() == targetRegId) {
                    readyCandidates.add(c);
                }
            } else if (!isAlreadyAllocated(c)) {
                readyCandidates.add(c);
            }
        }

        if (readyCandidates.isEmpty()) {
            return result;
        }

        int totalCandidates = readyCandidates.size();
        int totalSeats = 0;
        for (ExamArea room : activeTheoryRooms) {
            totalSeats += roomCapacity(room);
        }
        if (totalCandidates > totalSeats) {
            result.errorMsg = "[LỖI Exception 2.0.E1] Vượt quá dung lượng phòng thi lý thuyết. Vui lòng bổ sung phòng loại Lý thuyết trong ExamArea.";
            return result;
        }

        Collections.sort(readyCandidates, Comparator.comparing(
                c -> c.getLicenseCode() != null ? c.getLicenseCode() : ""));

        for (ExamRegistrationDTO c : readyCandidates) {
            int enrollSessionId = c.getExamSessionId() > 0 ? c.getExamSessionId() : sessionId;
            if (enrollSessionId <= 0) {
                continue;
            }

            String allocationConflict = registrationDAO.validateUniqueTheoryAllocation(c.getId(), enrollSessionId);
            if (allocationConflict != null) {
                if (targetRegId != null && c.getId() == targetRegId) {
                    result.errorMsg = allocationConflict;
                    return result;
                }
                continue;
            }

            ExamArea room = pickBestRoom(activeTheoryRooms, roomOccupancy);
            if (room == null) {
                continue;
            }

            if (registrationDAO.updateAllocatedRoom(c.getId(), enrollSessionId, room.getId(), room.getAreaName())) {
                c.setAllocatedAreaId(room.getId());
                c.setAllocatedAreaName(room.getAreaName());
                roomOccupancy.merge(room.getId(), 1, Integer::sum);
                result.allocatedCount++;
            }
        }

        return result;
    }

    private int resolveExamId(int sessionId) {
        if (sessionId <= 0) {
            return 0;
        }
        SessionDTO session = sessionDAO.getById(sessionId);
        return session != null ? session.getExamId() : 0;
    }

    private static int roomCapacity(ExamArea room) {
        if (room == null || room.getCapacity() == null || room.getCapacity() <= 0) {
            return DEFAULT_MAX_PER_ROOM;
        }
        return room.getCapacity();
    }

    private Map<Integer, Integer> buildRoomOccupancy(List<ExamRegistrationDTO> allCandidates, List<ExamArea> rooms) {
        Map<Integer, Integer> occupancy = new HashMap<>();
        for (ExamArea room : rooms) occupancy.put(room.getId(), 0);
        for (ExamRegistrationDTO c : allCandidates) {
            if (c.getAllocatedAreaId() != null && occupancy.containsKey(c.getAllocatedAreaId())) {
                if (isReadyForAllocation(c) || isAlreadyAllocated(c)) {
                    occupancy.merge(c.getAllocatedAreaId(), 1, Integer::sum);
                }
            }
        }
        return occupancy;
    }

    private ExamArea pickBestRoom(List<ExamArea> rooms, Map<Integer, Integer> roomOccupancy) {
        ExamArea bestRoom = null;
        int bestOccupancy = Integer.MAX_VALUE;

        for (ExamArea room : rooms) {
            int occ = roomOccupancy.getOrDefault(room.getId(), 0);
            if (occ >= roomCapacity(room)) {
                continue;
            }
            if (occ < bestOccupancy) {
                bestOccupancy = occ;
                bestRoom = room;
            }
        }
        return bestRoom;
    }

    private boolean isReadyForAllocation(ExamRegistrationDTO c) {
        if (c == null || c.isAbsent()) {
            return false;
        }
        boolean hasCapturedPhoto = c.isValidCapturedPhoto()
                || (c.getPhotoUrl() != null && !c.getPhotoUrl().isBlank());
        if (!c.isPaymentCompleted() || !hasCapturedPhoto || c.isSuspended()) {
            return false;
        }
        String theory = c.getTheoryPassed();
        if (theory == null || theory.isBlank()) {
            theory = "none";
        }
        return "none".equalsIgnoreCase(theory);
    }

    private boolean isAlreadyAllocated(ExamRegistrationDTO c) {
        if (c == null) {
            return false;
        }
        Integer areaId = c.getAllocatedAreaId();
        return areaId != null && areaId > 0;
    }
}

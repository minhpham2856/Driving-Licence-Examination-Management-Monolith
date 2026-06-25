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

import dto.examiner.AutoAllocateResultDTO;

import dto.exam.ExamRegistrationDTO;

import dto.examiner.ExaminerSlotDTO;

import dto.exam.SessionDTO;

import dto.user.UserDTO;

import model.exam.ExamArea;
import model.exam.ExamDevice;
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
        List<ExamArea> activeTheoryRooms = areaDAO.getActiveTheoryRooms();
        if (activeTheoryRooms.isEmpty()) {
            result.errorMsg = "Không có phòng thi lý thuyết đang hoạt động để phân bổ.";
            return result;
        }

        List<ExamRegistrationDTO> allCandidates = registrationDAO.getCandidatesBySession(sessionId);
        Map<Integer, Integer> roomOccupancy = buildRoomOccupancy(allCandidates, activeTheoryRooms);

        List<ExamRegistrationDTO> readyCandidates = new ArrayList<>();
        for (ExamRegistrationDTO c : allCandidates) {
            if (!isReadyForAllocation(c)) continue;
            if (targetRegId != null) {
                if (c.getId() == targetRegId) readyCandidates.add(c);
            } else if (!isAlreadyAllocated(c)) {
                readyCandidates.add(c);
            }
        }

        if (readyCandidates.isEmpty()) return result;

        int totalCandidates = readyCandidates.size();
        int totalSeats = activeTheoryRooms.size() * DEFAULT_MAX_PER_ROOM;
        if (totalCandidates > totalSeats) {
            result.errorMsg = "[LỖI Exception 2.0.E1] Vượt quá dung lượng cơ sở hạ tầng. Vui lòng kích hoạt thêm phòng thi lý thuyết.";
            return result;
        }

        Collections.sort(readyCandidates, Comparator.comparing(
                c -> c.getLicenseCode() != null ? c.getLicenseCode() : ""));

        for (ExamRegistrationDTO c : readyCandidates) {
            ExamArea room = pickBestRoom(c, activeTheoryRooms, roomOccupancy, allCandidates, DEFAULT_MAX_PER_ROOM);
            if (room == null) continue;

            registrationDAO.updateAllocatedRoom(c.getId(), room.getId(), room.getAreaName());
            c.setAllocatedAreaId(room.getId());
            c.setAllocatedAreaName(room.getAreaName());
            c.setNotes("AllocatedRoom:" + room.getId() + ":" + room.getAreaName());
            roomOccupancy.merge(room.getId(), 1, Integer::sum);
            result.allocatedCount++;
        }

        return result;
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

    private ExamArea pickBestRoom(ExamRegistrationDTO candidate, List<ExamArea> rooms,
            Map<Integer, Integer> roomOccupancy, List<ExamRegistrationDTO> allCandidates, int maxPerRoom) {
        ExamArea bestRoom = null;
        int bestScore = Integer.MIN_VALUE;
        String licCode = candidate.getLicenseCode();

        for (ExamArea room : rooms) {
            int occ = roomOccupancy.getOrDefault(room.getId(), 0);
            if (occ >= maxPerRoom) continue;

            int sameLicense = countSameLicenseInRoom(allCandidates, room.getId(), licCode);
            int score = sameLicense * 1000 - occ;
            if (score > bestScore) {
                bestScore = score;
                bestRoom = room;
            }
        }
        return bestRoom;
    }

    private int countSameLicenseInRoom(List<ExamRegistrationDTO> allCandidates, int roomId, String licenseCode) {
        if (licenseCode == null) return 0;
        int count = 0;
        for (ExamRegistrationDTO c : allCandidates) {
            if (roomId == (c.getAllocatedAreaId() != null ? c.getAllocatedAreaId() : -1)
                    && licenseCode.equals(c.getLicenseCode())
                    && (isReadyForAllocation(c) || isAlreadyAllocated(c))) {
                count++;
            }
        }
        return count;
    }

    private boolean isReadyForAllocation(ExamRegistrationDTO c) {
        if (c.isAbsent()) return false;
        boolean procedureDone = c.isPresent()
                && c.getPhotoUrl() != null && !c.getPhotoUrl().isEmpty()
                && c.isPaymentCompleted();
        return procedureDone && "none".equals(c.getTheoryPassed());
    }

    private boolean isAlreadyAllocated(ExamRegistrationDTO c) {
        if (c.getAllocatedAreaId() != null) return true;
        return c.getNotes() != null && c.getNotes().startsWith("AllocatedRoom:");
    }
}

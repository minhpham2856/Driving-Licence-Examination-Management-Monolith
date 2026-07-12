// Forced recompilation trigger
package examstaff.service.impl;


import dao.ExamAreaDAO;

import dao.ExamRegistrationDAO;

import dao.ExaminerAssignmentDAO;

import dao.impl.ExamAreaDAOImpl;

import dao.impl.ExamRegistrationDAOImpl;

import dao.impl.ExaminerAssignmentDAOImpl;

import examstaff.dto.AutoAllocateResultDTO;

import dto.exam.ExamRegistrationDTO;

import dto.ExaminerSlotDTO;

import dto.ExamSummaryDTO;

import dto.UserDTO;

import model.ExamArea;
import enums.ExamSection;
import examstaff.service.ExamStaffSessionQueryService;
import examstaff.service.ExaminerAllocationService;
import util.ExamAreaTypeResolver;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import examstaff.util.ExaminerAssignmentRules;

public class ExaminerAllocationServiceImpl implements ExaminerAllocationService {

    private final ExamStaffSessionQueryService sessionQuery = new ExamStaffSessionQueryServiceImpl();
    private final ExamAreaDAO areaDAO = new ExamAreaDAOImpl();
    private final ExaminerAssignmentDAO assignmentDAO = new ExaminerAssignmentDAOImpl();
    private final ExamRegistrationDAO registrationDAO = new ExamRegistrationDAOImpl();

    @Override
    public ExamSummaryDTO getSessionById(int sessionId) {
        return sessionQuery.findByExamId(sessionId);
    }

    @Override
    public ExamArea getAreaById(int id) {
        return areaDAO.getById(id);
    }

    @Override
    public List<UserDTO> getActiveExaminers() {
        return assignmentDAO.getActiveExaminers();
    }

    @Override
    public List<ExamArea> getAvailableAreasForSession(int sessionId) {
        if (sessionId <= 0) {
            return List.of();
        }
        List<ExamArea> linked = areaDAO.getAreasBySessionId(sessionId);
        if (!linked.isEmpty()) {
            return linked;
        }
        // Kỳ thi luôn gồm LT + TH — fallback lấy cả hai loại phòng/sân.
        List<ExamArea> areas = new ArrayList<>(areaDAO.getAvailableAreasByType(
                ExamSection.LY_THUYET.getDisplayName()));
        areas.addAll(areaDAO.getAvailableAreasByType(ExamAreaTypeResolver.PRACTICAL_AREA_TYPE));
        return areas;
    }

    @Override
    public List<ExaminerSlotDTO> getAssignmentsBySessionId(int sessionId) {
        return assignmentDAO.getByExamId(sessionId);
    }

    @Override
    public boolean assignExaminer(ExaminerSlotDTO slot) {
        return assignmentDAO.assign(slot);
    }

    @Override
    public boolean removeAssignment(String slotKey) {
        return assignmentDAO.remove(slotKey);
    }

    @Override
    public AutoAllocateResultDTO autoAllocateSession(int sessionId) {
        return autoAllocate(sessionId, null);
    }

    @Override
    public AutoAllocateResultDTO autoAllocateCandidate(int sessionId, int registrationId) {
        return autoAllocate(sessionId, registrationId);
    }

    @Override
    public AutoAllocateResultDTO autoAllocatePracticalSession(int sessionId) {
        return autoAllocatePractical(sessionId);
    }

    private AutoAllocateResultDTO autoAllocate(int sessionId, Integer targetRegId) {
        AutoAllocateResultDTO result = new AutoAllocateResultDTO();
        if (sessionId <= 0 && targetRegId == null) {
            result.errorMsg = "Chưa chọn kỳ thi để phân bổ phòng.";
            return result;
        }

        int examId = resolveExamId(sessionId);
        int effectiveSessionId = sessionId > 0 ? sessionId : examId;

        List<ExamArea> examRooms = effectiveSessionId > 0
                ? areaDAO.getAreasBySessionId(effectiveSessionId)
                : List.of();
        List<ExamArea> theoryRoomsForExam = examRooms.stream()
                .filter(ExaminerAssignmentRules::isTheoryRoom)
                .toList();
        if (theoryRoomsForExam.isEmpty()) {
            theoryRoomsForExam = areaDAO.getActiveTheoryRooms();
        }
        if (theoryRoomsForExam.isEmpty()) {
            result.errorMsg = "Không có phòng thi lý thuyết gắn với kỳ thi này.";
            return result;
        }

        Set<Integer> staffedTheoryAreaIds = ExaminerAssignmentRules.staffedTheoryAreaIds(
                effectiveSessionId > 0 ? assignmentDAO.getByExamId(effectiveSessionId) : List.of());
        List<ExamArea> eligibleTheoryRooms = ExaminerAssignmentRules.filterTheoryRoomsWithStaff(
                theoryRoomsForExam, staffedTheoryAreaIds);
        if (eligibleTheoryRooms.isEmpty()) {
            result.errorMsg = "Chưa có phòng lý thuyết nào được phân công giám khảo. "
                    + "Vào mục \"Phân bổ giám khảo\" trước khi tự động phân phòng thí sinh.";
            return result;
        }

        List<ExamRegistrationDTO> allCandidates = examId > 0
                ? registrationDAO.getCandidatesByExam(examId)
                : registrationDAO.getCandidatesBySession(sessionId);
        Map<Integer, Integer> roomOccupancy = buildRoomOccupancy(allCandidates, eligibleTheoryRooms);

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

        Collections.sort(readyCandidates, Comparator.comparing(
                c -> c.getLicenseCode() != null ? c.getLicenseCode() : ""));

        for (ExamRegistrationDTO c : readyCandidates) {
            int enrollSessionId = c.getExamId() > 0 ? c.getExamId() : sessionId;
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

            ExamArea room = pickBestRoom(eligibleTheoryRooms, roomOccupancy);
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

    private AutoAllocateResultDTO autoAllocatePractical(int sessionId) {
        AutoAllocateResultDTO result = new AutoAllocateResultDTO();
        if (sessionId <= 0) {
            result.errorMsg = "Chưa chọn kỳ thi để phân bổ sân thực hành.";
            return result;
        }

        int examId = resolveExamId(sessionId);
        int effectiveSessionId = sessionId > 0 ? sessionId : examId;

        List<ExamArea> examRooms = effectiveSessionId > 0
                ? areaDAO.getAreasBySessionId(effectiveSessionId)
                : List.of();
        List<ExamArea> practicalYards = examRooms.stream()
                .filter(ExaminerAssignmentRules::isPracticalRoom)
                .toList();
        if (practicalYards.isEmpty()) {
            result.errorMsg = "Không có sân thi thực hành gắn với kỳ thi này.";
            return result;
        }

        Set<Integer> staffedPracticalAreaIds = ExaminerAssignmentRules.staffedPracticalAreaIds(
                effectiveSessionId > 0 ? assignmentDAO.getByExamId(effectiveSessionId) : List.of());
        List<ExamArea> eligibleYards = ExaminerAssignmentRules.filterPracticalRoomsWithStaff(
                practicalYards, staffedPracticalAreaIds);
        if (eligibleYards.isEmpty()) {
            result.errorMsg = "Chưa có sân thực hành nào được phân công giám khảo. "
                    + "Vào mục \"Phân bổ giám khảo\" trước khi tự động phân sân thí sinh.";
            return result;
        }

        List<ExamRegistrationDTO> allCandidates = examId > 0
                ? registrationDAO.getCandidatesByExam(examId)
                : registrationDAO.getCandidatesBySession(sessionId);
        Map<Integer, Integer> yardOccupancy = buildPracticalOccupancy(allCandidates, eligibleYards);

        List<ExamRegistrationDTO> readyCandidates = new ArrayList<>();
        for (ExamRegistrationDTO c : allCandidates) {
            if (!isReadyForPracticalAllocation(c)) {
                continue;
            }
            if (!isAlreadyPracticalAllocated(c)) {
                readyCandidates.add(c);
            }
        }

        if (readyCandidates.isEmpty()) {
            return result;
        }

        Collections.sort(readyCandidates, Comparator.comparing(
                c -> c.getLicenseCode() != null ? c.getLicenseCode() : ""));

        for (ExamRegistrationDTO c : readyCandidates) {
            if (isAlreadyPracticalAllocated(c)) {
                continue;
            }

            int enrollSessionId = c.getExamId() > 0 ? c.getExamId() : sessionId;
            if (enrollSessionId <= 0) {
                continue;
            }

            ExamArea yard = pickBestRoom(eligibleYards, yardOccupancy);
            if (yard == null) {
                continue;
            }

            if (registrationDAO.updatePracticalAllocatedRoom(
                    c.getId(), enrollSessionId, yard.getId(), yard.getAreaName())) {
                c.setPracticalAllocatedAreaId(yard.getId());
                c.setPracticalAllocatedAreaName(yard.getAreaName());
                yardOccupancy.merge(yard.getId(), 1, Integer::sum);
                result.allocatedCount++;
            }
        }

        return result;
    }

    private int resolveExamId(int sessionId) {
        if (sessionId <= 0) {
            return 0;
        }
        ExamSummaryDTO session = sessionQuery.findByExamId(sessionId);
        if (session != null && session.getExamId() > 0) {
            return session.getExamId();
        }
        return sessionId;
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

    private Map<Integer, Integer> buildPracticalOccupancy(List<ExamRegistrationDTO> allCandidates,
            List<ExamArea> yards) {
        Map<Integer, Integer> occupancy = new HashMap<>();
        for (ExamArea yard : yards) {
            occupancy.put(yard.getId(), 0);
        }
        for (ExamRegistrationDTO c : allCandidates) {
            Integer yardId = c.getPracticalAllocatedAreaId();
            if (yardId != null && occupancy.containsKey(yardId)) {
                occupancy.merge(yardId, 1, Integer::sum);
            }
        }
        return occupancy;
    }

    private ExamArea pickBestRoom(List<ExamArea> rooms, Map<Integer, Integer> roomOccupancy) {
        ExamArea bestRoom = null;
        int bestOccupancy = Integer.MAX_VALUE;

        for (ExamArea room : rooms) {
            int occ = roomOccupancy.getOrDefault(room.getId(), 0);
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

    private boolean isReadyForPracticalAllocation(ExamRegistrationDTO c) {
        if (c == null || c.isAbsent() || c.skipsPractical()) {
            return false;
        }
        String theory = c.getTheoryPassed();
        return theory != null && "passed".equalsIgnoreCase(theory.trim());
    }

    private boolean isAlreadyPracticalAllocated(ExamRegistrationDTO c) {
        if (c == null) {
            return false;
        }
        Integer areaId = c.getPracticalAllocatedAreaId();
        return areaId != null && areaId > 0;
    }
}

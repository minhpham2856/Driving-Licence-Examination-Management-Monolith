package examstaff.service.impl;


import examstaff.dao.ExamAreaDAO;

import examstaff.dao.ExamRegistrationDAO;

import examstaff.dao.ExaminerAssignmentDAO;

import examstaff.dao.impl.ExamAreaDAOImpl;

import examstaff.dao.impl.ExamRegistrationDAOImpl;

import examstaff.dao.impl.ExaminerAssignmentDAOImpl;

import examstaff.dto.AutoAllocateResultDTO;

import examstaff.dto.exam.ExamRegistrationDTO;

import examstaff.dto.ExaminerSlotDTO;

import examstaff.dto.ExamSummaryDTO;

import examstaff.dto.UserDTO;

import shared.model.ExamArea;
import examstaff.enums.ExamSection;
import examstaff.service.ExamStaffExamQueryService;
import examstaff.service.ExaminerAllocationService;
import examstaff.util.ExamAreaTypeResolver;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import examstaff.util.ExaminerAssignmentRules;

/** Implementation: phân công giám khảo và tự động phân bổ phòng/sân thí sinh. */
public class ExaminerAllocationServiceImpl implements ExaminerAllocationService {

    private final ExamStaffExamQueryService examQuery = new ExamStaffExamQueryServiceImpl();
    private final ExamAreaDAO areaDAO = new ExamAreaDAOImpl();
    private final ExaminerAssignmentDAO assignmentDAO = new ExaminerAssignmentDAOImpl();
    private final ExamRegistrationDAO registrationDAO = new ExamRegistrationDAOImpl();

    /**
     * Lấy tóm tắt kỳ thi theo mã.
     *
     * @param examId mã kỳ thi
     * @return DTO kỳ thi hoặc null
     */
    @Override
    public ExamSummaryDTO getExamById(int examId) {
        return examQuery.findByExamId(examId);
    }

    /**
     * Lấy khu vực thi theo mã.
     *
     * @param id mã ExamArea
     * @return khu vực hoặc null
     */
    @Override
    public ExamArea getAreaById(int id) {
        return areaDAO.getById(id);
    }

    /**
     * Danh sách giám khảo đang active để chọn phân công.
     *
     * @return danh sách UserDTO giám khảo
     */
    @Override
    public List<UserDTO> getActiveExaminers() {
        return assignmentDAO.getActiveExaminers();
    }

    /**
     * Các khu vực được phép dùng cho kỳ thi (gắn Exam_ExamArea; fallback theo loại nếu chưa gắn).
     *
     * @param examId mã kỳ thi
     * @return danh sách ExamArea
     */
    @Override
    public List<ExamArea> getAvailableAreasForExam(int examId) {
        if (examId <= 0) {
            return List.of();
        }
        List<ExamArea> linked = areaDAO.getAreasByExamId(examId);
        if (!linked.isEmpty()) {
            return linked;
        }
        // Kỳ thi luôn gồm LT + TH - fallback lấy cả hai loại (schema Clean + alias SWP).
        Map<Integer, ExamArea> byId = new HashMap<>();
        for (String type : List.of(
                ExamAreaTypeResolver.theoryAreaTypeLabel(),
                ExamAreaTypeResolver.theoryAreaTypeAlias(),
                ExamAreaTypeResolver.practicalAreaTypeLabel(),
                ExamAreaTypeResolver.practicalAreaTypeAlias())) {
            for (ExamArea a : areaDAO.getAvailableAreasByType(type)) {
                byId.putIfAbsent(a.getExamAreaId(), a);
            }
        }
        return new ArrayList<>(byId.values());
    }

    /**
     * Các slot phân công giám khảo hiện có của kỳ thi.
     *
     * @param examId mã kỳ thi
     * @return danh sách slot
     */
    @Override
    public List<ExaminerSlotDTO> getAssignmentsByExamId(int examId) {
        return assignmentDAO.getByExamId(examId);
    }

    /**
     * Gán giám khảo vào một slot (kỳ + khu vực + phần thi).
     *
     * @param slot thông tin phân công
     * @return true nếu lưu thành công
     */
    @Override
    public boolean assignExaminer(ExaminerSlotDTO slot) {
        return assignmentDAO.assign(slot);
    }

    /**
     * Gỡ phân công giám khảo theo khóa slot.
     *
     * @param slotKey khóa slot
     * @return true nếu gỡ thành công
     */
    @Override
    public boolean removeAssignment(String slotKey) {
        return assignmentDAO.remove(slotKey);
    }

    /**
     * Tự động phân phòng lý thuyết cho toàn bộ thí sinh của kỳ.
     *
     * @param examId mã kỳ thi
     * @return kết quả phân bổ (số thành công / lỗi)
     */
    @Override
    public AutoAllocateResultDTO autoAllocateExam(int examId) {
        return autoAllocate(examId, null);
    }

    /**
     * Tự động phân phòng cho một thí sinh cụ thể.
     *
     * @param examId         mã kỳ thi
     * @param registrationId mã đăng ký thí sinh
     * @return kết quả phân bổ
     */
    @Override
    public AutoAllocateResultDTO autoAllocateCandidate(int examId, int registrationId) {
        return autoAllocate(examId, registrationId);
    }

    /**
     * Phân sân thực hành cho thí sinh đã đỗ lý thuyết (cân bằng tải trên sân có sát hạch viên). 
     */
    @Override
    public AutoAllocateResultDTO autoAllocatePracticalExam(int examId) {
        return autoAllocatePractical(examId);
    }

    /** Phân bổ tự động phòng lý thuyết cho toàn kỳ hoặc một thí sinh. */
    private AutoAllocateResultDTO autoAllocate(int examId, Integer targetRegId) {
        AutoAllocateResultDTO result = new AutoAllocateResultDTO();
        if (examId <= 0 && targetRegId == null) {
            result.errorMsg = "Chưa chọn kỳ thi để phân bổ phòng.";
            return result;
        }

        int resolvedExamId = resolveExamId(examId);
        int effectiveExamId = examId > 0 ? examId : resolvedExamId;

        List<ExamArea> examRooms = effectiveExamId > 0
                ? areaDAO.getAreasByExamId(effectiveExamId)
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
                effectiveExamId > 0 ? assignmentDAO.getByExamId(effectiveExamId) : List.of());
        List<ExamArea> eligibleTheoryRooms = ExaminerAssignmentRules.filterTheoryRoomsWithStaff(
                theoryRoomsForExam, staffedTheoryAreaIds);
        if (eligibleTheoryRooms.isEmpty()) {
            result.errorMsg = "Chưa có phòng lý thuyết nào được phân công sát hạch viên. "
                    + "Vào mục \"Phân bổ sát hạch viên\" trước khi tự động phân phòng thí sinh.";
            return result;
        }

        List<ExamRegistrationDTO> allCandidates = resolvedExamId > 0
                ? registrationDAO.getCandidatesByExam(resolvedExamId)
                : registrationDAO.getCandidatesByExam(examId);
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
            int enrollExamId = c.getExamId() > 0 ? c.getExamId() : examId;
            if (enrollExamId <= 0) {
                continue;
            }

            String allocationConflict = registrationDAO.validateUniqueTheoryAllocation(c.getId(), enrollExamId);
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

            if (registrationDAO.updateAllocatedRoom(c.getId(), enrollExamId, room.getId(), room.getAreaName())) {
                c.setAllocatedAreaId(room.getId());
                c.setAllocatedAreaName(room.getAreaName());
                roomOccupancy.merge(room.getId(), 1, Integer::sum);
                result.allocatedCount++;
            }
        }

        return result;
    }

    /** Phân bổ tự động sân thực hành cho thí sinh đã đậu lý thuyết. */
    private AutoAllocateResultDTO autoAllocatePractical(int examId) {
        AutoAllocateResultDTO result = new AutoAllocateResultDTO();
        if (examId <= 0) {
            result.errorMsg = "Chưa chọn kỳ thi để phân bổ sân thực hành.";
            return result;
        }

        int resolvedExamId = resolveExamId(examId);
        int effectiveExamId = examId > 0 ? examId : resolvedExamId;

        List<ExamArea> examRooms = effectiveExamId > 0
                ? areaDAO.getAreasByExamId(effectiveExamId)
                : List.of();
        List<ExamArea> practicalYards = examRooms.stream()
                .filter(ExaminerAssignmentRules::isPracticalRoom)
                .toList();
        if (practicalYards.isEmpty()) {
            result.errorMsg = "Không có sân thi thực hành gắn với kỳ thi này.";
            return result;
        }

        Set<Integer> staffedPracticalAreaIds = ExaminerAssignmentRules.staffedPracticalAreaIds(
                effectiveExamId > 0 ? assignmentDAO.getByExamId(effectiveExamId) : List.of());
        List<ExamArea> eligibleYards = ExaminerAssignmentRules.filterPracticalRoomsWithStaff(
                practicalYards, staffedPracticalAreaIds);
        if (eligibleYards.isEmpty()) {
            result.errorMsg = "Chưa có sân thực hành nào được phân công sát hạch viên. "
                    + "Vào mục \"Phân bổ sát hạch viên\" trước khi tự động phân sân thí sinh.";
            return result;
        }

        List<ExamRegistrationDTO> allCandidates = resolvedExamId > 0
                ? registrationDAO.getCandidatesByExam(resolvedExamId)
                : registrationDAO.getCandidatesByExam(examId);
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

            int enrollExamId = c.getExamId() > 0 ? c.getExamId() : examId;
            if (enrollExamId <= 0) {
                continue;
            }

            ExamArea yard = pickBestRoom(eligibleYards, yardOccupancy);
            if (yard == null) {
                continue;
            }

            if (registrationDAO.updatePracticalAllocatedRoom(
                    c.getId(), enrollExamId, yard.getId(), yard.getAreaName())) {
                c.setPracticalAllocatedAreaId(yard.getId());
                c.setPracticalAllocatedAreaName(yard.getAreaName());
                yardOccupancy.merge(yard.getId(), 1, Integer::sum);
                result.allocatedCount++;
            }
        }

        return result;
    }

    /** Resolve examId thực tế từ exam query. */
    private int resolveExamId(int examId) {
        if (examId <= 0) {
            return 0;
        }
        ExamSummaryDTO session = examQuery.findByExamId(examId);
        if (session != null && session.getExamId() > 0) {
            return session.getExamId();
        }
        return examId;
    }

    /** Đếm số thí sinh đang chiếm mỗi phòng lý thuyết. */
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

    /** Đếm số thí sinh đang chiếm mỗi sân thực hành. */
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

    /** Chọn phòng/sân ít thí sinh nhất. */
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

    /** Thí sinh sẵn sàng phân phòng LT (đã thu phí, có ảnh, chưa thi LT). */
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

    /** Đã có phòng lý thuyết được phân. */
    private boolean isAlreadyAllocated(ExamRegistrationDTO c) {
        if (c == null) {
            return false;
        }
        Integer areaId = c.getAllocatedAreaId();
        return areaId != null && areaId > 0;
    }

    /** Thí sinh sẵn sàng phân sân TH (đã đậu LT). */
    private boolean isReadyForPracticalAllocation(ExamRegistrationDTO c) {
        if (c == null || c.isAbsent() || c.skipsPractical()) {
            return false;
        }
        String theory = c.getTheoryPassed();
        return theory != null && "passed".equalsIgnoreCase(theory.trim());
    }

    /** Đã có sân thực hành được phân. */
    private boolean isAlreadyPracticalAllocated(ExamRegistrationDTO c) {
        if (c == null) {
            return false;
        }
        Integer areaId = c.getPracticalAllocatedAreaId();
        return areaId != null && areaId > 0;
    }
}

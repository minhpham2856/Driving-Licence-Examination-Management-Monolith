package examstaff.service.impl.support.assign;
import examstaff.service.impl.support.shared.ExamStaffExamQueryServiceImpl;


import examstaff.dao.ExamAreaDAO;

import examstaff.dao.ExamRegistrationDAO;

import examstaff.dao.ExaminerAssignmentDAO;

import examstaff.dao.impl.ExamAreaDAOImpl;

import examstaff.dao.impl.ExamRegistrationDAOImpl;

import examstaff.dao.impl.ExaminerAssignmentDAOImpl;

import examstaff.dto.AllocationActionResultDTO;

import examstaff.dto.ExamRegistrationDTO;

import examstaff.dto.ExaminerSlotDTO;

import examstaff.dto.ExamSummaryDTO;

import examstaff.dto.UserDTO;

import shared.model.ExamArea;
import examstaff.enums.ExamSection;
import examstaff.util.ExamAreaTypeResolver;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
/**
 * Dịch vụ nền phân công sát hạch viên và tự động phân bổ phòng/sân cho thí sinh.
 * <p>
 * Gọi trực tiếp ExamAreaDAO, ExaminerAssignmentDAO, ExamRegistrationDAO.
 * Được ExaminerAllocationDeskServiceImpl (gán thủ công) và
 * AllocationActionServiceImpl (auto sau thủ tục) sử dụng.
 *
 * Phân công sát hạch viên (CRUD):
 * - getActiveExaminers / getAssignmentsByExamId — đọc danh sách
 * - assignExaminer / removeAssignment — ghi slot qua DAO
 * - getAvailableAreasForExam — khu gắn kỳ; fallback theo loại LT+TH nếu chưa gắn
 *
 * Auto-allocate thí sinh:
 * - Phòng LT — autoAllocateExam / autoAllocateCandidate: chỉ phòng đã staffed
 *       (ExaminerAssignmentRules.filterTheoryRoomsWithStaff); chọn phòng ít tải nhất
 * - Sân TH — autoAllocatePracticalExam: thí sinh đậu LT hoặc bảo lưu LT;
 *       chỉ sân đã staffed; cân bằng tải tương tự
 * - Sau thủ tục — autoAllocateCandidate: bảo lưu LT → sân TH; còn lại → phòng LT
 *
 * Điều kiện sẵn sàng phân bổ:
 * Thí sinh cần đã thu phí, có ảnh, không vắng/đình chỉ; LT yêu cầu chưa có kết quả LT;
 * TH yêu cầu đậu LT (hoặc miễn LT sau thủ tục) và chưa gán sân.
 */
public class ExaminerAllocationServiceImpl {

    private final ExamStaffExamQueryServiceImpl examQuery = new ExamStaffExamQueryServiceImpl();
    private final ExamAreaDAO areaDAO = new ExamAreaDAOImpl();
    private final ExaminerAssignmentDAO assignmentDAO = new ExaminerAssignmentDAOImpl();
    private final ExamRegistrationDAO registrationDAO = new ExamRegistrationDAOImpl();

    /**
     * Lấy tóm tắt kỳ thi theo mã.
     * @param examId mã kỳ thi
     * @return DTO kỳ thi hoặc null
     */
    public ExamSummaryDTO getExamById(int examId) {
        return examQuery.findByExamId(examId);
    }

    /**
     * Lấy khu vực thi theo mã.
     * @param id mã ExamArea
     * @return khu vực hoặc null
     */
    public ExamArea getAreaById(int id) {
        return areaDAO.getById(id);
    }

    /**
     * Danh sách sát hạch viên đang active để chọn phân công.
     * @return danh sách UserDTO sát hạch viên
     */
    public List<UserDTO> getActiveExaminers() {
        return assignmentDAO.getActiveExaminers();
    }

    /**
     * Các khu vực được phép dùng cho kỳ thi (gắn Exam_ExamArea; fallback theo loại nếu chưa gắn).
     * @param examId mã kỳ thi
     * @return danh sách ExamArea
     */
    public List<ExamArea> getAvailableAreasForExam(int examId) {
        // validate
        if (examId <= 0) {
            return List.of();
        }
        // load: ưu tiên khu vực đã gắn kỳ
        List<ExamArea> linked = areaDAO.getAreasByExamId(examId);
        if (!linked.isEmpty()) {
            return linked;
        }
        // result fallback: kỳ luôn gồm LT + TH — lấy cả hai loại (schema Clean + alias SWP)
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
     * Các slot phân công sát hạch viên hiện có của kỳ thi.
     * @param examId mã kỳ thi
     * @return danh sách slot
     */
    public List<ExaminerSlotDTO> getAssignmentsByExamId(int examId) {
        return assignmentDAO.getByExamId(examId);
    }

    /**
     * Gán sát hạch viên vào một slot (kỳ + khu vực + phần thi).
     * @param slot thông tin phân công
     * @return true nếu lưu thành công
     */
    public boolean assignExaminer(ExaminerSlotDTO slot) {
        return assignmentDAO.assign(slot);
    }

    /**
     * Gỡ phân công sát hạch viên theo khóa slot.
     * @param slotKey khóa slot
     * @return true nếu gỡ thành công
     */
    public boolean removeAssignment(String slotKey) {
        return assignmentDAO.remove(slotKey);
    }

    /**
     * Tự động phân phòng lý thuyết cho toàn bộ thí sinh của kỳ.
     * @param examId mã kỳ thi
     * @return kết quả phân bổ (số thành công / lỗi)
     */
    public AllocationActionResultDTO autoAllocateExam(int examId) {
        return autoAllocate(examId, null);
    }

    /**
     * Tự động phân phòng cho một thí sinh sau thủ tục:
     * bảo lưu LT → sân thực hành; còn lại → phòng lý thuyết.
     * @param examId         mã kỳ thi
     * @param registrationId mã đăng ký thí sinh
     * @return kết quả phân bổ
     */
    public AllocationActionResultDTO autoAllocateCandidate(int examId, int registrationId) {
        ExamRegistrationDTO target = findCandidate(examId, registrationId);
        if (target != null && target.skipsTheory() && !target.skipsPractical()) {
            return autoAllocatePractical(examId, registrationId);
        }
        return autoAllocate(examId, registrationId);
    }

    /**
     * Phân sân thực hành cho thí sinh đã đỗ lý thuyết (cân bằng tải trên sân có sát hạch viên).
     * @param examId mã kỳ thi
     * @return kết quả phân bổ sân TH
     */
    public AllocationActionResultDTO autoAllocatePracticalExam(int examId) {
        return autoAllocatePractical(examId, null);
    }

    /**
     * Phân bổ tự động phòng lý thuyết cho toàn kỳ hoặc một thí sinh.
     * @param examId      mã kỳ thi
     * @param targetRegId null = toàn kỳ; khác null = chỉ thí sinh đó
     * @return kết quả phân bổ
     */
    private AllocationActionResultDTO autoAllocate(int examId, Integer targetRegId) {
        AllocationActionResultDTO result = new AllocationActionResultDTO();
        // validate đầu vào
        if (examId <= 0 && targetRegId == null) {
            result.setErrorMsg("Chưa chọn kỳ thi để phân bổ phòng.");
            return result;
        }

        // load examId thực tế + phòng LT gắn kỳ
        int resolvedExamId = resolveExamId(examId);
        int effectiveExamId = examId > 0 ? examId : resolvedExamId;

        List<ExamArea> examRooms = effectiveExamId > 0
                ? getAvailableAreasForExam(effectiveExamId)
                : List.of();
        List<ExamArea> theoryRoomsForExam = examRooms.stream()
                .filter(ExaminerAssignmentRules::isTheoryRoom)
                .toList();
        if (theoryRoomsForExam.isEmpty()) {
            theoryRoomsForExam = areaDAO.getActiveTheoryRooms();
        }
        if (theoryRoomsForExam.isEmpty()) {
            result.setErrorMsg("Không có phòng thi lý thuyết gắn với kỳ thi này.");
            return result;
        }

        // validate: chỉ phòng đã có SHV
        Set<Integer> staffedTheoryAreaIds = ExaminerAssignmentRules.staffedTheoryAreaIds(
                effectiveExamId > 0 ? assignmentDAO.getByExamId(effectiveExamId) : List.of());
        List<ExamArea> eligibleTheoryRooms = ExaminerAssignmentRules.filterTheoryRoomsWithStaff(
                theoryRoomsForExam, staffedTheoryAreaIds);
        if (eligibleTheoryRooms.isEmpty()) {
            result.setErrorMsg("Chưa có phòng lý thuyết nào được phân công sát hạch viên. "
                    + "Vào mục \"Phân bổ sát hạch viên\" trước khi tự động phân phòng thí sinh.");
            return result;
        }

        // load thí sinh + occupancy hiện tại
        List<ExamRegistrationDTO> allCandidates = resolvedExamId > 0
                ? registrationDAO.getCandidatesByExam(resolvedExamId)
                : registrationDAO.getCandidatesByExam(examId);
        Map<Integer, Integer> roomOccupancy = buildRoomOccupancy(allCandidates, eligibleTheoryRooms);

        // load hàng chờ sẵn sàng phân (một người hoặc chưa gán)
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

        // mutate: gán phòng ít tải nhất từng thí sinh
        for (ExamRegistrationDTO c : readyCandidates) {
            int enrollExamId = c.getExamId() > 0 ? c.getExamId() : examId;
            if (enrollExamId <= 0) {
                continue;
            }

            String allocationConflict = registrationDAO.validateUniqueTheoryAllocation(c.getId(), enrollExamId);
            if (allocationConflict != null) {
                if (targetRegId != null && c.getId() == targetRegId) {
                    result.setErrorMsg(allocationConflict);
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
                result.setAllocatedCount(result.getAllocatedCount() + 1);
            }
        }

        // result
        return result;
    }

    /**
     * Phân bổ tự động sân thực hành cho thí sinh đã đậu lý thuyết
     * (hoặc bảo lưu LT / TakeTheory = 0).
     * @param examId      mã kỳ thi
     * @param targetRegId null = toàn kỳ; khác null = chỉ thí sinh đó
     * @return kết quả phân bổ
     */
    private AllocationActionResultDTO autoAllocatePractical(int examId, Integer targetRegId) {
        AllocationActionResultDTO result = new AllocationActionResultDTO();
        // validate
        if (examId <= 0 && targetRegId == null) {
            result.setErrorMsg("Chưa chọn kỳ thi để phân bổ sân thực hành.");
            return result;
        }

        // load sân TH gắn kỳ
        int resolvedExamId = resolveExamId(examId);
        int effectiveExamId = examId > 0 ? examId : resolvedExamId;

        List<ExamArea> examRooms = effectiveExamId > 0
                ? getAvailableAreasForExam(effectiveExamId)
                : List.of();
        List<ExamArea> practicalYards = examRooms.stream()
                .filter(ExaminerAssignmentRules::isPracticalRoom)
                .toList();
        if (practicalYards.isEmpty()) {
            result.setErrorMsg("Không có sân thi thực hành gắn với kỳ thi này.");
            return result;
        }

        // validate: chỉ sân đã có SHV
        Set<Integer> staffedPracticalAreaIds = ExaminerAssignmentRules.staffedPracticalAreaIds(
                effectiveExamId > 0 ? assignmentDAO.getByExamId(effectiveExamId) : List.of());
        List<ExamArea> eligibleYards = ExaminerAssignmentRules.filterPracticalRoomsWithStaff(
                practicalYards, staffedPracticalAreaIds);
        if (eligibleYards.isEmpty()) {
            result.setErrorMsg("Chưa có sân thực hành nào được phân công sát hạch viên. "
                    + "Vào mục \"Phân bổ sát hạch viên\" trước khi tự động phân sân thí sinh.");
            return result;
        }

        // load thí sinh + occupancy sân
        List<ExamRegistrationDTO> allCandidates = resolvedExamId > 0
                ? registrationDAO.getCandidatesByExam(resolvedExamId)
                : registrationDAO.getCandidatesByExam(examId);
        Map<Integer, Integer> yardOccupancy = buildPracticalOccupancy(allCandidates, eligibleYards);

        List<ExamRegistrationDTO> readyCandidates = new ArrayList<>();
        for (ExamRegistrationDTO c : allCandidates) {
            if (!isReadyForPracticalAllocation(c)) {
                continue;
            }
            if (targetRegId != null) {
                if (c.getId() == targetRegId) {
                    readyCandidates.add(c);
                }
            } else if (!isAlreadyPracticalAllocated(c)) {
                readyCandidates.add(c);
            }
        }

        if (readyCandidates.isEmpty()) {
            return result;
        }

        Collections.sort(readyCandidates, Comparator.comparing(
                c -> c.getLicenseCode() != null ? c.getLicenseCode() : ""));

        // mutate: gán sân ít tải nhất
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
                result.setAllocatedCount(result.getAllocatedCount() + 1);
            }
        }

        // result
        return result;
    }

    /**
     * Resolve examId thực tế từ exam query.
     * @param examId mã kỳ đầu vào
     * @return examId đã resolve, hoặc 0 nếu không hợp lệ
     */
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

    /**
     * Đếm số thí sinh đang chiếm mỗi phòng lý thuyết.
     * @param allCandidates danh sách thí sinh kỳ
     * @param rooms         phòng LT đủ điều kiện
     * @return map areaId → số người
     */
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

    /**
     * Đếm số thí sinh đang chiếm mỗi sân thực hành.
     * @param allCandidates danh sách thí sinh kỳ
     * @param yards         sân TH đủ điều kiện
     * @return map areaId → số người
     */
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

    /**
     * Chọn phòng/sân ít thí sinh nhất.
     * @param rooms         danh sách phòng/sân
     * @param roomOccupancy map occupancy hiện tại
     * @return phòng/sân tối ưu, hoặc null
     */
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

    /**
     * Thí sinh sẵn sàng phân phòng LT (đã thu phí, có ảnh, chưa thi LT, không bảo lưu LT).
     * @param c hồ sơ
     * @return true nếu sẵn sàng
     */
    private boolean isReadyForAllocation(ExamRegistrationDTO c) {
        // validate cơ bản
        if (c == null || c.isAbsent() || c.skipsTheory()) {
            return false;
        }
        boolean hasCapturedPhoto = c.isValidCapturedPhoto()
                || (c.getPhotoUrl() != null && !c.getPhotoUrl().isBlank());
        if (!c.isPaymentCompleted() || !hasCapturedPhoto || c.isSuspended()) {
            return false;
        }
        // result: chưa có kết quả LT
        String theory = c.getTheoryPassed();
        if (theory == null || theory.isBlank()) {
            theory = "none";
        }
        return "none".equalsIgnoreCase(theory);
    }

    /**
     * Đã có phòng lý thuyết được phân.
     * @param c hồ sơ
     * @return true nếu đã gán phòng LT
     */
    private boolean isAlreadyAllocated(ExamRegistrationDTO c) {
        if (c == null) {
            return false;
        }
        Integer areaId = c.getAllocatedAreaId();
        return areaId != null && areaId > 0;
    }

    /**
     * Thí sinh sẵn sàng phân sân TH: đã đậu LT, hoặc bảo lưu LT sau thủ tục; không miễn TH.
     * @param c hồ sơ
     * @return true nếu sẵn sàng
     */
    private boolean isReadyForPracticalAllocation(ExamRegistrationDTO c) {
        // validate
        if (c == null || c.isAbsent() || c.skipsPractical()) {
            return false;
        }
        if (c.skipsTheory()) {
            boolean hasCapturedPhoto = c.isValidCapturedPhoto()
                    || (c.getPhotoUrl() != null && !c.getPhotoUrl().isBlank());
            return c.isPaymentCompleted() && hasCapturedPhoto && !c.isSuspended();
        }
        // result: phải đạt LT
        String theory = c.getTheoryPassed();
        return theory != null && "passed".equalsIgnoreCase(theory.trim());
    }

    /**
     * Tìm thí sinh theo mã đăng ký trong kỳ (phục vụ auto-allocate sau thủ tục).
     */
    private ExamRegistrationDTO findCandidate(int examId, int registrationId) {
        if (registrationId <= 0) {
            return null;
        }
        int resolvedExamId = resolveExamId(examId);
        List<ExamRegistrationDTO> all = resolvedExamId > 0
                ? registrationDAO.getCandidatesByExam(resolvedExamId)
                : (examId > 0 ? registrationDAO.getCandidatesByExam(examId) : List.of());
        for (ExamRegistrationDTO c : all) {
            if (c.getId() == registrationId) {
                return c;
            }
        }
        return null;
    }

    /**
     * Đã có sân thực hành được phân.
     * @param c hồ sơ
     * @return true nếu đã gán sân TH
     */
    private boolean isAlreadyPracticalAllocated(ExamRegistrationDTO c) {
        if (c == null) {
            return false;
        }
        Integer areaId = c.getPracticalAllocatedAreaId();
        return areaId != null && areaId > 0;
    }
}

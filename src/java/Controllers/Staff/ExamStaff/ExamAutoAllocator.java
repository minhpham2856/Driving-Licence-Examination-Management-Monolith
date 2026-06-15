package Controllers.Staff.ExamStaff;

import DAO.ExamAreaDAO;
import DAO.ExamRegistrationDAO;
import DAO.Impl.ExamAreaDAOImpl;
import DAO.Impl.ExamRegistrationDAOImpl;
import Models.ExamArea;
import Models.ExamRegistration;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Tự động phân bổ thí sinh vào phòng thi lý thuyết sau khi hoàn tất thủ tục.
 * Máy tính và thiết bị thi (ExamDevice) do Examiner quản lý — không xử lý tại đây.
 */
public class ExamAutoAllocator {

    private static final int DEFAULT_MAX_PER_ROOM = 30;

    private final ExamRegistrationDAO regDAO = new ExamRegistrationDAOImpl();
    private final ExamAreaDAO areaDAO = new ExamAreaDAOImpl();

    public static class Result {
        public int allocatedCount = 0;
        public String errorMsg;
        public String warningMsg;
    }

    public Result autoAllocateSession(int sessionId) {
        return autoAllocate(sessionId, null);
    }

    public Result autoAllocateCandidate(int sessionId, int registrationId) {
        return autoAllocate(sessionId, registrationId);
    }

    private Result autoAllocate(int sessionId, Integer targetRegId) {
        Result result = new Result();
        List<ExamArea> activeTheoryRooms = areaDAO.getActiveTheoryRooms();
        if (activeTheoryRooms.isEmpty()) {
            result.errorMsg = "Không có phòng thi lý thuyết đang hoạt động để phân bổ.";
            return result;
        }

        List<ExamRegistration> allCandidates = regDAO.getCandidatesBySession(sessionId);
        Map<Integer, Integer> roomOccupancy = buildRoomOccupancy(allCandidates, activeTheoryRooms);

        List<ExamRegistration> readyCandidates = new ArrayList<>();
        for (ExamRegistration c : allCandidates) {
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

        int maxPerRoom = DEFAULT_MAX_PER_ROOM;
        int totalCandidates = readyCandidates.size();
        int totalSeats = activeTheoryRooms.size() * maxPerRoom;
        if (totalCandidates > totalSeats) {
            result.errorMsg = "[LỖI Exception 2.0.E1] Vượt quá dung lượng cơ sở hạ tầng. Vui lòng kích hoạt thêm phòng thi lý thuyết.";
            return result;
        }

        Collections.sort(readyCandidates, Comparator.comparing(
                c -> c.getLicenseCode() != null ? c.getLicenseCode() : ""));

        for (ExamRegistration c : readyCandidates) {
            ExamArea room = pickBestRoom(c, activeTheoryRooms, roomOccupancy, allCandidates, maxPerRoom);
            if (room == null) {
                continue;
            }

            regDAO.updateAllocatedRoom(c.getId(), room.getId(), room.getAreaName());
            c.setAllocatedAreaId(room.getId());
            c.setAllocatedAreaName(room.getAreaName());
            c.setNotes("AllocatedRoom:" + room.getId() + ":" + room.getAreaName());
            roomOccupancy.merge(room.getId(), 1, Integer::sum);
            result.allocatedCount++;
        }

        return result;
    }

    private Map<Integer, Integer> buildRoomOccupancy(List<ExamRegistration> allCandidates, List<ExamArea> rooms) {
        Map<Integer, Integer> occupancy = new HashMap<>();
        for (ExamArea room : rooms) {
            occupancy.put(room.getId(), 0);
        }
        for (ExamRegistration c : allCandidates) {
            if (c.getAllocatedAreaId() != null && occupancy.containsKey(c.getAllocatedAreaId())) {
                if (isReadyForAllocation(c) || isAlreadyAllocated(c)) {
                    occupancy.merge(c.getAllocatedAreaId(), 1, Integer::sum);
                }
            }
        }
        return occupancy;
    }

    private ExamArea pickBestRoom(ExamRegistration candidate, List<ExamArea> rooms,
            Map<Integer, Integer> roomOccupancy, List<ExamRegistration> allCandidates, int maxPerRoom) {

        ExamArea bestRoom = null;
        int bestScore = Integer.MIN_VALUE;
        String licCode = candidate.getLicenseCode();

        for (ExamArea room : rooms) {
            int occ = roomOccupancy.getOrDefault(room.getId(), 0);
            if (occ >= maxPerRoom) {
                continue;
            }

            int sameLicense = countSameLicenseInRoom(allCandidates, room.getId(), licCode);
            int score = sameLicense * 1000 - occ;
            if (score > bestScore) {
                bestScore = score;
                bestRoom = room;
            }
        }
        return bestRoom;
    }

    private int countSameLicenseInRoom(List<ExamRegistration> allCandidates, int roomId, String licenseCode) {
        if (licenseCode == null) {
            return 0;
        }
        int count = 0;
        for (ExamRegistration c : allCandidates) {
            if (roomId == (c.getAllocatedAreaId() != null ? c.getAllocatedAreaId() : -1)
                    && licenseCode.equals(c.getLicenseCode())
                    && (isReadyForAllocation(c) || isAlreadyAllocated(c))) {
                count++;
            }
        }
        return count;
    }

    private boolean isReadyForAllocation(ExamRegistration c) {
        if (c.getNotes() != null && "Absent".equalsIgnoreCase(c.getNotes().trim())) {
            return false;
        }
        boolean procedureDone = c.isPresent() && c.isPaymentCompleted();
        return procedureDone && "none".equals(c.getTheoryPassed());
    }

    private boolean isAlreadyAllocated(ExamRegistration c) {
        if (c.getAllocatedAreaId() != null) {
            return true;
        }
        return c.getNotes() != null && c.getNotes().startsWith("AllocatedRoom:");
    }
}

package examstaff.util;

import examstaff.dto.ExamRegistrationDTO;
import examstaff.dto.ExamStaffCandidate;

import java.util.ArrayList;
import java.util.List;

/**
 * Map read-model {@link ExamStaffCandidate} → {@link ExamRegistrationDTO} dùng trên UI/controller.
 * Field tùy chọn (khu vực phân bổ, điểm) chỉ set khi nguồn khác null.
 */
public final class ExamStaffCandidateMapper {

    /** Không cho khởi tạo — chỉ dùng static. */
    private ExamStaffCandidateMapper() {
    }

    /**
     * Map một read-model thí sinh sang DTO đăng ký.
     * <p>
     * Luồng:
     * <ol>
     *   <li>null row → null</li>
     *   <li>Copy id / enrollment / SBD / loại ĐK / thanh toán / hiện diện / hồ sơ cá nhân</li>
     *   <li>Copy hạng GPLX, máy tính, cờ phần thi, ngày thi, vắng/đình chỉ, ghi chú</li>
     *   <li>Nếu có → copy khu vực LT/TH đã phân bổ</li>
     *   <li>Nếu có → copy điểm lý thuyết / thực hành</li>
     * </ol>
     *
     * @param row dòng nguồn (null → null)
     * @return DTO hoặc {@code null}
     */
    public static ExamRegistrationDTO toDto(ExamStaffCandidate row) {
        // Bước 1: null-safe
        if (row == null) {
            return null;
        }
        ExamRegistrationDTO dto = new ExamRegistrationDTO();
        // Bước 2: định danh & trạng thái đăng ký / thanh toán / hiện diện
        dto.setId(row.getCandidateId());
        dto.setExamId(row.getExamId());
        dto.setExamEnrollmentId(row.getExamEnrollmentId());
        dto.setCandidateNo(row.getCandidateNo());
        dto.setRegistrationType(row.getRegistrationType());
        dto.setIsPaymentCompleted(row.isPaymentCompleted());
        dto.setIsPresent(row.isPresent());
        dto.setPresentMarkedAt(row.getPresentMarkedAt());
        // Bước 3: thông tin cá nhân & liên hệ
        dto.setFullName(row.getFullName());
        dto.setGovIdNo(row.getGovIdNo());
        dto.setDateOfBirth(row.getDateOfBirth());
        dto.setPhoneNo(row.getPhoneNo());
        dto.setEmail(row.getEmail());
        dto.setPhotoUrl(row.getPhotoUrl());
        // Bước 4: hạng GPLX, máy, phần thi, ngày thi, cờ vắng/đình chỉ
        dto.setLicenseCode(row.getLicenseCode());
        dto.setComputerCode(row.getComputerCode());
        dto.setTakeTheory(row.getTakeTheory());
        dto.setTakePractical(row.getTakePractical());
        dto.setExamDate(row.getExamDate());
        dto.setAbsent(row.isAbsent());
        dto.setSuspended(row.isSuspended());
        dto.setNotes(row.getNotes());
        // Bước 5: khu vực đã phân bổ (chỉ khi có id)
        if (row.getAllocatedAreaId() != null) {
            dto.setAllocatedAreaId(row.getAllocatedAreaId());
            dto.setAllocatedAreaName(row.getAllocatedAreaName());
        }
        if (row.getPracticalAllocatedAreaId() != null) {
            dto.setPracticalAllocatedAreaId(row.getPracticalAllocatedAreaId());
            dto.setPracticalAllocatedAreaName(row.getPracticalAllocatedAreaName());
        }
        // Bước 6: điểm (chỉ set khi nguồn khác null)
        if (row.getTheoryScore() != null) {
            dto.setTheoryScore(row.getTheoryScore());
        }
        if (row.getPracticalScore() != null) {
            dto.setPracticalScore(row.getPracticalScore());
        }
        return dto;
    }

    /**
     * Map danh sách read-model → danh sách DTO (giữ thứ tự; null list → list rỗng).
     *
     * @param rows danh sách nguồn
     * @return danh sách DTO (không null)
     */
    public static List<ExamRegistrationDTO> toDtoList(List<ExamStaffCandidate> rows) {
        List<ExamRegistrationDTO> list = new ArrayList<>();
        if (rows == null) {
            return list;
        }
        // Map từng phần tử qua toDto
        for (ExamStaffCandidate row : rows) {
            list.add(toDto(row));
        }
        return list;
    }
}

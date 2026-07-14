package examstaff.util;

import examstaff.dto.exam.ExamRegistrationDTO;
import examstaff.dto.view.ExamStaffCandidate;

import java.util.ArrayList;
import java.util.List;

/** Map {@link ExamStaffCandidate} → {@link ExamRegistrationDTO}. */
public final class ExamStaffCandidateMapper {

    private ExamStaffCandidateMapper() {
    }

    /**
     * Map một read-model thí sinh sang DTO đăng ký.
     *
     * @param row dòng nguồn (null → null)
     * @return DTO hoặc {@code null}
     */
    public static ExamRegistrationDTO toDto(ExamStaffCandidate row) {
        if (row == null) {
            return null;
        }
        ExamRegistrationDTO dto = new ExamRegistrationDTO();
        dto.setId(row.getCandidateId());
        dto.setExamId(row.getExamId());
        dto.setExamEnrollmentId(row.getExamEnrollmentId());
        dto.setCandidateNo(row.getCandidateNo());
        dto.setRegistrationType(row.getRegistrationType());
        dto.setIsPaymentCompleted(row.isPaymentCompleted());
        dto.setIsPresent(row.isPresent());
        dto.setPresentMarkedAt(row.getPresentMarkedAt());
        dto.setFullName(row.getFullName());
        dto.setGovIdNo(row.getGovIdNo());
        dto.setDateOfBirth(row.getDateOfBirth());
        dto.setPhoneNo(row.getPhoneNo());
        dto.setEmail(row.getEmail());
        dto.setPhotoUrl(row.getPhotoUrl());
        dto.setLicenseCode(row.getLicenseCode());
        dto.setComputerCode(row.getComputerCode());
        dto.setTakeTheory(row.getTakeTheory());
        dto.setTakePractical(row.getTakePractical());
        dto.setExamDate(row.getExamDate());
        dto.setAbsent(row.isAbsent());
        dto.setSuspended(row.isSuspended());
        dto.setNotes(row.getNotes());
        if (row.getAllocatedAreaId() != null) {
            dto.setAllocatedAreaId(row.getAllocatedAreaId());
            dto.setAllocatedAreaName(row.getAllocatedAreaName());
        }
        if (row.getPracticalAllocatedAreaId() != null) {
            dto.setPracticalAllocatedAreaId(row.getPracticalAllocatedAreaId());
            dto.setPracticalAllocatedAreaName(row.getPracticalAllocatedAreaName());
        }
        if (row.getTheoryScore() != null) {
            dto.setTheoryScore(row.getTheoryScore());
        }
        if (row.getPracticalScore() != null) {
            dto.setPracticalScore(row.getPracticalScore());
        }
        return dto;
    }

    /**
     * Map danh sách read-model → danh sách DTO.
     *
     * @param rows danh sách nguồn
     * @return danh sách DTO (không null)
     */
    public static List<ExamRegistrationDTO> toDtoList(List<ExamStaffCandidate> rows) {
        List<ExamRegistrationDTO> list = new ArrayList<>();
        if (rows == null) {
            return list;
        }
        for (ExamStaffCandidate row : rows) {
            list.add(toDto(row));
        }
        return list;
    }
}

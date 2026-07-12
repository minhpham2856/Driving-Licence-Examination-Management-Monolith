package examstaff.util;

import dto.exam.ExamRegistrationDTO;
import examstaff.model.view.ExamStaffCandidate;

import java.util.ArrayList;
import java.util.List;

/** Map read model → DTO trình bày (tầng service/controller). */
public final class ExamStaffCandidateMapper {

    private ExamStaffCandidateMapper() {
    }

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
        dto.setGender(row.isMale());
        dto.setPhoneNo(row.getPhoneNo());
        dto.setEmail(row.getEmail());
        dto.setPhotoUrl(row.getPhotoUrl());
        dto.setLicenseCode(row.getLicenseCode());
        dto.setComputerCode(row.getComputerCode());
        dto.setAddress(row.getAddress());
        dto.setReasonForTaking(row.getReasonForTaking());
        dto.setTakeTheory(row.getTakeTheory());
        dto.setTakePractical(row.getTakePractical());
        dto.setExamDate(row.getExamDate());
        dto.setSectionStatus(row.getSectionStatus());
        dto.setSignaturePrinted(row.isSignaturePrinted());
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

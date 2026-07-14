package examstaff.util;

import examstaff.dto.ExamSummaryDTO;
import examstaff.dto.view.ExamSessionSummary;

import java.util.ArrayList;
import java.util.List;

public final class ExamSessionSummaryMapper {

    private ExamSessionSummaryMapper() {
    }

    public static ExamSummaryDTO toDto(ExamSessionSummary row) {
        if (row == null) {
            return null;
        }
        ExamSummaryDTO dto = new ExamSummaryDTO();
        dto.setId(row.getExamId());
        dto.setExamId(row.getExamId());
        dto.setMorningSession(row.isMorningSession());
        dto.setSessionName(row.getSessionName());
        dto.setLicenseTypeId(row.getLicenseTypeId());
        dto.setExamTypeId(row.getExamTypeId());
        dto.setExamDate(row.getExamDate());
        dto.setShiftStartTime(row.getShiftStartTime());
        dto.setShiftEndTime(row.getShiftEndTime());
        dto.setScheduledStartAt(row.getScheduledStartAt());
        dto.setScheduledEndAt(row.getScheduledEndAt());
        dto.setAreaId(row.getAreaId());
        dto.setStatus(row.getStatus());
        dto.setMaxCandidates(row.getMaxCandidates());
        dto.setRegisteredCount(row.getRegisteredCount());
        dto.setCreatedAt(row.getCreatedAt() != null ? row.getCreatedAt() : row.getScheduledStartAt());
        dto.setLicenseCode(row.getLicenseCode());
        dto.setExamCode(row.getExamCode());
        dto.setExamTypeName(row.getExamTypeName());
        dto.setAreaName(row.getAreaName());
        return dto;
    }

    public static List<ExamSummaryDTO> toDtoList(List<ExamSessionSummary> rows) {
        List<ExamSummaryDTO> list = new ArrayList<>();
        if (rows == null) {
            return list;
        }
        for (ExamSessionSummary row : rows) {
            list.add(toDto(row));
        }
        return list;
    }
}


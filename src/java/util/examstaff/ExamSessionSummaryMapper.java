package util.examstaff;

import dto.SessionDTO;
import model.view.ExamSessionSummary;

import java.util.ArrayList;
import java.util.List;

public final class ExamSessionSummaryMapper {

    private ExamSessionSummaryMapper() {
    }

    public static SessionDTO toDto(ExamSessionSummary row) {
        if (row == null) {
            return null;
        }
        SessionDTO dto = new SessionDTO();
        dto.setId(row.getSessionId());
        dto.setExamId(row.getExamId());
        dto.setMorningSession(row.isMorningSession());
        dto.setSessionName(row.getSessionName());
        dto.setLicenseTypeId(row.getLicenseTypeId());
        dto.setExamTypeId(row.getExamTypeId());
        dto.setExamDate(row.getExamDate());
        dto.setShiftStartTime(row.getShiftStartTime());
        dto.setShiftEndTime(row.getShiftEndTime());
        dto.setAreaId(row.getAreaId());
        dto.setStatus(row.getStatus());
        dto.setMaxCandidates(row.getMaxCandidates());
        dto.setRegisteredCount(row.getRegisteredCount());
        dto.setCreatedAt(row.getCreatedAt());
        dto.setLicenseCode(row.getLicenseCode());
        dto.setExamCode(row.getExamCode());
        dto.setExamTypeName(row.getExamTypeName());
        dto.setAreaName(row.getAreaName());
        return dto;
    }

    public static List<SessionDTO> toDtoList(List<ExamSessionSummary> rows) {
        List<SessionDTO> list = new ArrayList<>();
        if (rows == null) {
            return list;
        }
        for (ExamSessionSummary row : rows) {
            list.add(toDto(row));
        }
        return list;
    }
}

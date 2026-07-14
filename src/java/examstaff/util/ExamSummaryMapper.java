package examstaff.util;

import examstaff.dto.ExamSummaryDTO;
import examstaff.dto.view.ExamSummaryRow;

import java.util.ArrayList;
import java.util.List;

/** Map {@link ExamSummaryRow} → {@link ExamSummaryDTO}. */
public final class ExamSummaryMapper {

    private ExamSummaryMapper() {
    }

    /**
     * Map một dòng read-model sang DTO tóm tắt kỳ thi.
     *
     * @param row dòng nguồn (null → null)
     * @return DTO hoặc {@code null}
     */
    public static ExamSummaryDTO toDto(ExamSummaryRow row) {
        if (row == null) {
            return null;
        }
        ExamSummaryDTO dto = new ExamSummaryDTO();
        dto.setId(row.getExamId());
        dto.setExamId(row.getExamId());
        dto.setExamName(row.getExamName());
        dto.setExamTypeId(row.getExamTypeId());
        dto.setExamDate(row.getExamDate());
        dto.setShiftStartTime(row.getShiftStartTime());
        dto.setShiftEndTime(row.getShiftEndTime());
        dto.setScheduledStartAt(row.getScheduledStartAt());
        dto.setScheduledEndAt(row.getScheduledEndAt());
        dto.setStatus(row.getStatus());
        dto.setCreatedAt(row.getCreatedAt() != null ? row.getCreatedAt() : row.getScheduledStartAt());
        dto.setLicenseCode(row.getLicenseCode());
        dto.setExamCode(row.getExamCode());
        dto.setExamTypeName(row.getExamTypeName());
        return dto;
    }

    /**
     * Map danh sách row → danh sách DTO (bỏ qua null list → rỗng).
     *
     * @param rows danh sách nguồn
     * @return danh sách DTO (không null)
     */
    public static List<ExamSummaryDTO> toDtoList(List<ExamSummaryRow> rows) {
        List<ExamSummaryDTO> list = new ArrayList<>();
        if (rows == null) {
            return list;
        }
        for (ExamSummaryRow row : rows) {
            list.add(toDto(row));
        }
        return list;
    }
}

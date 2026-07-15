package examstaff.util;

import examstaff.dto.ExamSummaryDTO;

import java.util.ArrayList;
import java.util.List;

/**
 * Identity / list-copy helper cho {@link ExamSummaryDTO}.
 * <p>
 * DAO đã map sẵn field; lớp này chỉ đồng bộ cặp {@code id}/{@code examId}
 * và copy danh sách an toàn (null list → list rỗng).
 */
public final class ExamSummaryMapper {

    /** Không cho khởi tạo — chỉ dùng static. */
    private ExamSummaryMapper() {
    }

    /**
     * Pass-through DTO: đồng bộ {@code id} ↔ {@code examId} khi một phía thiếu.
     * <p>
     * Luồng:
     * <ol>
     *   <li>null → null</li>
     *   <li>id ≤ 0 và examId &gt; 0 → setId(examId)</li>
     *   <li>examId ≤ 0 và id &gt; 0 → setExamId(id)</li>
     *   <li>trả cùng instance (mutate tại chỗ)</li>
     * </ol>
     *
     * @param dto nguồn (có thể null)
     * @return cùng DTO hoặc null
     */
    public static ExamSummaryDTO toDto(ExamSummaryDTO dto) {
        // Bước 1: null-safe
        if (dto == null) {
            return null;
        }
        // Bước 2: đồng bộ id từ examId nếu thiếu
        if (dto.getId() <= 0 && dto.getExamId() > 0) {
            dto.setId(dto.getExamId());
        }
        // Bước 3: đồng bộ examId từ id nếu thiếu
        if (dto.getExamId() <= 0 && dto.getId() > 0) {
            dto.setExamId(dto.getId());
        }
        return dto;
    }

    /**
     * Copy danh sách DTO qua {@link #toDto} từng phần tử.
     *
     * @param rows danh sách nguồn (null → rỗng)
     * @return danh sách DTO (không null)
     */
    public static List<ExamSummaryDTO> toDtoList(List<ExamSummaryDTO> rows) {
        List<ExamSummaryDTO> list = new ArrayList<>();
        if (rows == null) {
            return list;
        }
        // Duyệt và map từng dòng (toDto xử lý null phần tử)
        for (ExamSummaryDTO row : rows) {
            list.add(toDto(row));
        }
        return list;
    }
}

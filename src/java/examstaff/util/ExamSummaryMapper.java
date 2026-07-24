package examstaff.util;

import examstaff.dto.ExamSummaryDTO;

import java.util.ArrayList;
import java.util.List;

/**
 * Utility identity / copy danh sách cho ExamSummaryDTO — đồng bộ cặp id/examId
 * sau khi DAO map JDBC (một số query chỉ populate một phía).
 *
 * Vai trò trong luồng examstaff:
 * Sidebar, chọn ca và dashboard làm việc với ExamSummaryDTO; code downstream so sánh
 * cả getId() và getExamId(). Mapper đảm bảo hai field luôn khớp trước khi
 * bind session hoặc truyền sang ExamStaffExamRules.
 *
 * Cách hoạt động:
 * - toDto — null → null; id ≤ 0 && examId > 0 → setId; ngược lại setExamId;
 *       mutate tại chỗ, trả cùng instance.
 * - toDtoList — map từng phần tử; null list → list rỗng.
 *
 * Ai gọi:
 * ExamStaffViewServiceImpl, ExamStaffPageBinder, ExamStaffExamQueryServiceImpl,
 * DashboardServlet, ExamSelectServlet — nạp danh sách kỳ thi cho sidebar/UI.
 */
public final class ExamSummaryMapper {

    /** Không cho khởi tạo — chỉ dùng static. */
    private ExamSummaryMapper() {
    }

    /**
     * Pass-through DTO: đồng bộ id ↔ examId khi một phía thiếu.
     * <p>
 *
     * Luồng:
     * - null → null
     * - id ≤ 0 và examId > 0 → setId(examId)
     * - examId ≤ 0 và id > 0 → setExamId(id)
     * - trả cùng instance (mutate tại chỗ)
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
     * Copy danh sách DTO qua toDto từng phần tử.
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

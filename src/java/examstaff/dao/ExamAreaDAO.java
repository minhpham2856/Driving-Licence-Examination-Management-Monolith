package examstaff.dao;

import shared.model.ExamArea;
import java.util.List;

/**
 * Cổng truy cập khu vực / phòng thi ({@link ExamArea}).
 *
 * Vai trò trong kiến trúc:
 * Cung cấp danh sách phòng lý thuyết, sân thực hành và khu vực gắn với kỳ thi
 * cho màn phân bổ (allocation), phân công giám khảo và chọn phòng trên UI.
 * Chỉ đọc — không ghi {@code ExamArea}.
 *
 * Hai nguồn dữ liệu:
 * - Bảng {@code ExamArea} — master phòng/sân ({@code AreaType}, {@code Capacity}…)
 * - Bảng {@code Exam_ExamArea} — khu vực được gán cho từng {@code ExamId}
 *
 * Phòng lý thuyết vs loại AreaType:
 * {@link #getActiveTheoryRooms} gộp hai schema tên loại ({@code Lý thuyết} và {@code Phòng thi})
 * để tương thích DLEM / SWP. {@link #getAvailableAreasByType} lọc chính xác theo {@code AreaType}.
 *
 * Triển khai mặc định:
 * {@link examstaff.dao.impl.ExamAreaDAOImpl} — JDBC SELECT đơn giản, map {@link ExamArea}.
 */
public interface ExamAreaDAO {

    /**
     * Lấy khu vực theo mã.
     * Thực thi {@code SELECT * FROM ExamArea WHERE ExamAreaId = ?}.
     * @param examAreaId mã khu vực ({@code ExamArea.ExamAreaId})
     * @return entity {@link ExamArea} nếu tìm thấy; {@code null} nếu không có
     */
    ExamArea getById(int examAreaId);

    /**
     * Lấy danh sách phòng lý thuyết đang dùng được (gộp theo schema / loại AreaType).
     * SELECT các {@code ExamArea} thuộc nhóm phòng lý thuyết active, sắp xếp theo tên.
     * @return danh sách phòng lý thuyết; rỗng nếu không cấu hình phòng
     */
    List<ExamArea> getActiveTheoryRooms();

    /**
     * Lấy khu vực theo loại ({@code AreaType}).
     * Thực thi {@code SELECT * FROM ExamArea WHERE AreaType = ? ORDER BY AreaName}.
     * @param areaType loại khu vực (ví dụ Theory, Practical…); trống → danh sách rỗng
     * @return danh sách {@link ExamArea} đúng loại; rỗng nếu {@code areaType} trống hoặc không khớp
     */
    List<ExamArea> getAvailableAreasByType(String areaType);

    /**
     * Lấy khu vực được gán cho một kỳ thi.
     * Thực thi SELECT {@code ExamArea} JOIN {@code Exam_ExamArea}
     * với điều kiện {@code ExamId = ?}.
     * @param examId mã kỳ thi cần lấy danh sách khu vực
     * @return danh sách {@link ExamArea} của kỳ thi; rỗng nếu chưa gắn khu vực
     */
    List<ExamArea> getAreasByExamId(int examId);
}

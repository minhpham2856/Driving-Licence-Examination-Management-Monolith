package examstaff.dao;

import shared.model.ExamArea;
import java.util.List;

/**
 * Cổng truy cập khu vực / phòng thi (ExamArea).
 *
 * Vai trò trong kiến trúc:
 * Cung cấp danh sách phòng lý thuyết, sân thực hành và khu vực gắn với kỳ thi
 * cho màn phân bổ (allocation), phân công giám khảo và chọn phòng trên UI.
 * Chỉ đọc — không ghi ExamArea.
 *
 * Hai nguồn dữ liệu:
 * - Bảng ExamArea — master phòng/sân (AreaType, Capacity…)
 * - Bảng Exam_ExamArea — khu vực được gán cho từng ExamId
 *
 * Phòng lý thuyết vs loại AreaType:
 * getActiveTheoryRooms gộp hai schema tên loại (Lý thuyết và Phòng thi)
 * để tương thích DLEM / SWP. getAvailableAreasByType lọc chính xác theo AreaType.
 *
 * Triển khai mặc định:
 * examstaff.dao.impl.ExamAreaDAOImpl — JDBC SELECT đơn giản, map ExamArea.
 */
public interface ExamAreaDAO {

    /**
     * Lấy khu vực theo mã.
     * Thực thi SELECT * FROM ExamArea WHERE ExamAreaId = ?.
     * @param examAreaId mã khu vực (ExamArea.ExamAreaId)
     * @return entity ExamArea nếu tìm thấy; null nếu không có
     */
    ExamArea getById(int examAreaId);

    /**
     * Lấy danh sách phòng lý thuyết đang dùng được (gộp theo schema / loại AreaType).
     * SELECT các ExamArea thuộc nhóm phòng lý thuyết active, sắp xếp theo tên.
     * @return danh sách phòng lý thuyết; rỗng nếu không cấu hình phòng
     */
    List<ExamArea> getActiveTheoryRooms();

    /**
     * Lấy khu vực theo loại (AreaType).
     * Thực thi SELECT * FROM ExamArea WHERE AreaType = ? ORDER BY AreaName.
     * @param areaType loại khu vực (ví dụ Theory, Practical…); trống → danh sách rỗng
     * @return danh sách ExamArea đúng loại; rỗng nếu areaType trống hoặc không khớp
     */
    List<ExamArea> getAvailableAreasByType(String areaType);

    /**
     * Lấy khu vực được gán cho một kỳ thi.
     * Thực thi SELECT ExamArea JOIN Exam_ExamArea
     * với điều kiện ExamId = ?.
     * @param examId mã kỳ thi cần lấy danh sách khu vực
     * @return danh sách ExamArea của kỳ thi; rỗng nếu chưa gắn khu vực
     */
    List<ExamArea> getAreasByExamId(int examId);
}

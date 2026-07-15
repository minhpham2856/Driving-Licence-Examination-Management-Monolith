package examstaff.dao;

import shared.model.ExamArea;
import java.util.List;

/**
 * DAO truy vấn khu vực / phòng thi ({@link ExamArea}).
 * Thực thi SELECT trên bảng {@code ExamArea} và liên kết {@code Exam_ExamArea}.
 */
public interface ExamAreaDAO {

    /**
     * Lấy khu vực theo mã.
     * Thực thi {@code SELECT * FROM ExamArea WHERE ExamAreaId = ?}.
     *
     * @param examAreaId mã khu vực ({@code ExamArea.ExamAreaId})
     * @return entity {@link ExamArea} nếu tìm thấy; {@code null} nếu không có
     */
    ExamArea getById(int examAreaId);

    /**
     * Lấy danh sách phòng lý thuyết đang dùng được (gộp theo schema / loại AreaType).
     * SELECT các {@code ExamArea} thuộc nhóm phòng lý thuyết active, sắp xếp theo tên.
     *
     * @return danh sách phòng lý thuyết; rỗng nếu không cấu hình phòng
     */
    List<ExamArea> getActiveTheoryRooms();

    /**
     * Lấy khu vực theo loại ({@code AreaType}).
     * Thực thi {@code SELECT * FROM ExamArea WHERE AreaType = ? ORDER BY AreaName}.
     *
     * @param areaType loại khu vực (ví dụ Theory, Practical…); trống → danh sách rỗng
     * @return danh sách {@link ExamArea} đúng loại; rỗng nếu {@code areaType} trống hoặc không khớp
     */
    List<ExamArea> getAvailableAreasByType(String areaType);

    /**
     * Lấy khu vực được gán cho một kỳ thi.
     * Thực thi SELECT {@code ExamArea} JOIN {@code Exam_ExamArea}
     * với điều kiện {@code ExamId = ?}.
     *
     * @param examId mã kỳ thi cần lấy danh sách khu vực
     * @return danh sách {@link ExamArea} của kỳ thi; rỗng nếu chưa gắn khu vực
     */
    List<ExamArea> getAreasByExamId(int examId);
}

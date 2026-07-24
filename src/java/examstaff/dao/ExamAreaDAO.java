package examstaff.dao;

import shared.model.ExamArea;
import java.util.List;

/**
 * DAO truy vấn khu vực / phòng thi ({@link ExamArea}).
 */
public interface ExamAreaDAO {

    /**
     * Lấy khu vực theo mã.
     *
     * @param examAreaId mã khu vực
     * @return entity hoặc {@code null} nếu không tìm thấy
     */
    ExamArea getById(int examAreaId);

    /**
     * Lấy danh sách phòng lý thuyết đang dùng được (gộp theo schema).
     *
     * @return danh sách phòng lý thuyết
     */
    List<ExamArea> getActiveTheoryRooms();

    /**
     * Lấy khu vực theo loại ({@code AreaType}).
     *
     * @param areaType loại khu vực
     * @return danh sách khu vực, rỗng nếu {@code areaType} trống
     */
    List<ExamArea> getAvailableAreasByType(String areaType);

    /**
     * Lấy khu vực được gán cho một kỳ thi.
     *
     * @param examId mã kỳ thi
     * @return danh sách khu vực của kỳ thi
     */
    List<ExamArea> getAreasByExamId(int examId);
}

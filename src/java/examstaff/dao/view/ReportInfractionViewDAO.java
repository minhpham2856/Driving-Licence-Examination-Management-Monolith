package examstaff.dao.view;

import java.util.List;
import java.util.Map;

/**
 * View DAO thống kê lỗi trừ điểm (infraction) theo kỳ thi.
 */
public interface ReportInfractionViewDAO {

    /**
     * Lấy top lỗi trừ điểm của phần thực hành trong kỳ thi.
     *
     * @param examId mã kỳ thi
     * @param limit  số dòng tối đa (mặc định 3 nếu &le; 0)
     * @return danh sách map gồm {@code reason}, {@code count}, {@code percentage}
     */
    List<Map<String, Object>> findTopInfractions(int examId, int limit);
}

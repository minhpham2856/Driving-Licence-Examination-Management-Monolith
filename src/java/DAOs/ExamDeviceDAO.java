package DAOs;

import Models.ExamDevice;
import java.util.List;

/**
 * DAO cho thao tác với thiết bị thi (ExamDevice) - dùng cho phân bổ thiết bị cho thí sinh.
 * Cung cấp các phương thức CRUD, tìm kiếm, thống kê và quản lý trạng thái thiết bị.
 */
public interface ExamDeviceDAO {

    /**
     * Tìm kiếm thiết bị theo từ khóa, mã phòng và trạng thái.
     *
     * @param keyword từ khóa tìm kiếm (tên/mã thiết bị)
     * @param roomId  mã phòng thi (có thể null)
     * @param status  trạng thái (có thể null)
     * @return danh sách ExamDevice phù hợp
     */
    List<ExamDevice> search(String keyword, Integer roomId, String status);

    /**
     * Lấy thông tin thiết bị theo mã.
     *
     * @param examDeviceId mã thiết bị
     * @return ExamDevice model, hoặc null nếu không tìm thấy
     */
    ExamDevice findById(int examDeviceId);

    /**
     * Thêm mới một thiết bị.
     *
     * @param device đối tượng ExamDevice chứa thông tin thiết bị
     * @return mã ExamDevice được tạo, hoặc -1 nếu thất bại
     */
    int insert(ExamDevice device);

    /**
     * Cập nhật thông tin thiết bị.
     *
     * @param device đối tượng ExamDevice chứa thông tin cập nhật
     * @return true nếu cập nhật thành công
     */
    boolean update(ExamDevice device);

    /**
     * Xóa một thiết bị theo mã.
     *
     * @param examDeviceId mã thiết bị cần xóa
     * @return true nếu xóa thành công
     */
    boolean delete(int examDeviceId);

    /**
     * Đếm tổng số thiết bị.
     *
     * @return tổng số lượng thiết bị
     */
    int countAll();

    /**
     * Đếm số thiết bị theo trạng thái.
     *
     * @param status trạng thái ('active', 'maintenance', 'broken')
     * @return số lượng thiết bị có trạng thái tương ứng
     */
    int countByStatus(String status);

    /**
     * Cập nhật trạng thái của thiết bị.
     *
     * @param examDeviceId mã thiết bị
     * @param status       trạng thái mới ('active', 'maintenance', 'broken')
     * @return true nếu cập nhật thành công
     */
    boolean updateStatus(int examDeviceId, String status);

    /**
     * Lấy danh sách thiết bị theo mã khu vực.
     *
     * @param examAreaId mã khu vực
     * @return danh sách ExamDevice thuộc khu vực
     */
    List<ExamDevice> getDevicesByAreaId(int examAreaId);
}

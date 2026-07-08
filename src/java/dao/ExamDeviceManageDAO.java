package dao;


import dto.ExamDeviceViewDTO;

import java.util.List;

/**
 * Admin CRUD cho thiết bị thi (ExamDevice) - quản lý danh mục thiết bị.
 * Được đặt tên riêng biệt để không xung đột với DAO.ExamDeviceDAO
 * (dùng cho use case phân bổ thiết bị). Cung cấp các phương thức CRUD
 * có ghi nhận người thực hiện thao tác.
 */
public interface ExamDeviceManageDAO {

    /**
     * Tìm kiếm thiết bị theo từ khóa và trạng thái.
     *
     * @param keyword từ khóa tìm kiếm (tên/mã thiết bị)
     * @param status  trạng thái lọc (có thể null)
     * @return danh sách ExamDeviceViewDTO phù hợp
     */
    List<ExamDeviceViewDTO> search(String keyword, String status);

    /**
     * Lấy thông tin thiết bị theo mã.
     *
     * @param examDeviceId mã thiết bị
     * @return ExamDeviceViewDTO, hoặc null nếu không tìm thấy
     */
    ExamDeviceViewDTO findById(int examDeviceId);

    /**
     * Thêm mới một thiết bị, ghi nhận người tạo.
     *
     * @param device    đối tượng ExamDeviceViewDTO chứa thông tin thiết bị
     * @param createdBy mã người dùng thực hiện thao tác
     * @return mã ExamDevice được tạo, hoặc -1 nếu thất bại
     */
    int insert(ExamDeviceViewDTO device, Integer createdBy);

    /**
     * Cập nhật thông tin thiết bị, ghi nhận người cập nhật.
     *
     * @param device    đối tượng ExamDeviceViewDTO chứa thông tin cập nhật
     * @param updatedBy mã người dùng thực hiện thao tác
     * @return true nếu cập nhật thành công
     */
    boolean update(ExamDeviceViewDTO device, Integer updatedBy);

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
}

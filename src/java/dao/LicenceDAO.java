package dao;


import model.licence.Licence;
import java.util.List;

/**
 * DAO cho thao tác với hạng giấy phép lái xe (Licence).
 * Cung cấp các phương thức CRUD, tìm kiếm, kiểm tra tồn tại và thống kê
 * các loại hạng giấy phép lái xe trong hệ thống.
 */
public interface LicenceDAO {

    /**
     * Lấy danh sách tất cả các hạng giấy phép lái xe.
     *
     * @return danh sách tất cả Licence
     */
    List<Licence> findAll();

    /**
     * Tìm kiếm hạng giấy phép lái xe theo từ khóa.
     *
     * @param keyword từ khóa tìm kiếm (tên hạng, mô tả, ...)
     * @return danh sách Licence phù hợp
     */
    List<Licence> search(String keyword);

    /**
     * Lấy thông tin hạng giấy phép lái xe theo mã.
     *
     * @param licenceId mã hạng giấy phép
     * @return Licence model, hoặc null nếu không tìm thấy
     */
    Licence findById(int licenceId);

    /**
     * Kiểm tra xem hạng giấy phép đã tồn tại chưa (dùng khi thêm mới hoặc cập nhật).
     *
     * @param licenceClass tên hạng giấy phép
     * @param excludeId    mã cần loại trừ (khi cập nhật), 0 nếu thêm mới
     * @return true nếu đã tồn tại
     */
    boolean existsByClass(String licenceClass, int excludeId);

    /**
     * Thêm mới một hạng giấy phép lái xe.
     *
     * @param licence đối tượng Licence chứa thông tin hạng giấy phép
     * @return mã Licence được tạo, hoặc -1 nếu thất bại
     */
    int insert(Licence licence);

    /**
     * Cập nhật thông tin hạng giấy phép lái xe.
     *
     * @param licence đối tượng Licence chứa thông tin cập nhật
     * @return true nếu cập nhật thành công
     */
    boolean update(Licence licence);

    /**
     * Đếm tổng số hạng giấy phép lái xe.
     *
     * @return tổng số lượng
     */
    int countAll();
}

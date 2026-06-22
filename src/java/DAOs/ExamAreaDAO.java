package DAOs;

import Models.ExamArea;
import java.util.List;

// DAO cho thao tác với khu vực thi (ExamArea) trong hệ thống.
// Cung cấp các phương thức CRUD, tìm kiếm, thống kê và truy vấn
// các khu vực/phòng thi theo kỳ thi.
public interface ExamAreaDAO {

    // Tìm kiếm khu vực thi theo từ khóa và loại khu vực.
    // 
    // @param keyword  từ khóa tìm kiếm (tên, mã khu vực)
    // @param areaType loại khu vực (theory/practical) hoặc null để lấy tất cả
    // @return danh sách ExamArea phù hợp
    List<ExamArea> search(String keyword, String areaType);

    // Thêm mới một khu vực thi.
    // 
    // @param area đối tượng ExamArea chứa thông tin khu vực
    // @return mã ExamArea được tạo, hoặc -1 nếu thất bại
    int insert(ExamArea area);

    // Cập nhật thông tin khu vực thi.
    // 
    // @param area đối tượng ExamArea chứa thông tin cập nhật
    // @return true nếu cập nhật thành công
    boolean update(ExamArea area);

    // Xóa một khu vực thi theo mã.
    // 
    // @param examAreaId mã khu vực cần xóa
    // @return true nếu xóa thành công
    boolean delete(int examAreaId);

    // Đếm tổng số khu vực thi.
    // 
    // @return tổng số lượng khu vực thi
    int countAll();

    // Lấy thông tin khu vực thi theo mã.
    // 
    // @param examAreaId mã khu vực
    // @return ExamArea model, hoặc null nếu không tìm thấy
    ExamArea getById(int examAreaId);

    // Lấy danh sách các phòng thi lý thuyết đang hoạt động.
    // 
    // @return danh sách ExamArea là phòng thi lý thuyết
    List<ExamArea> getActiveTheoryRooms();

    // Lấy danh sách khu vực thi theo mã kỳ thi.
    // 
    // @param sessionId mã kỳ thi
    // @return danh sách ExamArea thuộc kỳ thi
    List<ExamArea> getBySessionId(int sessionId);

    // Kiểm tra xem khu vực thi có thuộc kỳ thi hay không.
    // 
    // @param sessionId  mã kỳ thi
    // @param examAreaId mã khu vực
    // @return true nếu khu vực thuộc kỳ thi
    boolean isAreaInSession(int sessionId, int examAreaId);
}

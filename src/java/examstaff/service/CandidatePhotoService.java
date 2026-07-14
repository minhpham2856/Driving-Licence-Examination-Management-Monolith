package examstaff.service;

import examstaff.dto.exam.ExamRegistrationDTO;
import examstaff.dto.CandidatePhotoStreamDTO;

import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * Nghiệp vụ ảnh thí sinh: chuẩn hóa đường dẫn, kiểm tra tồn tại và ghi file ảnh.
 */
public interface CandidatePhotoService {

    /**
     * Chuẩn hóa thông tin ảnh trên cả hàng đợi (đường dẫn + trạng thái còn file).
     *
     * @param webRoot thư mục gốc web
     * @param queue   hàng đợi thí sinh
     */
    void normalizeQueue(String webRoot, List<ExamRegistrationDTO> queue);

    /**
     * Chuẩn hóa đường dẫn ảnh trên hàng đợi về dạng dùng được trên web.
     *
     * @param webRoot thư mục gốc web
     * @param queue   hàng đợi thí sinh
     */
    void normalizePhotoPaths(String webRoot, List<ExamRegistrationDTO> queue);

    /**
     * Kiểm tra hồ sơ đã có bản ghi ảnh (có URL ảnh) hay chưa.
     *
     * @param reg hồ sơ đăng ký
     * @return true nếu đã có photoUrl
     */
    boolean hasPhotoRecord(ExamRegistrationDTO reg);

    /**
     * Kiểm tra và gắn trạng thái ảnh đã chụp có file vật lý hay không.
     *
     * @param webRoot thư mục gốc web
     * @param reg     hồ sơ đăng ký
     * @return true nếu tìm thấy file ảnh hợp lệ
     */
    boolean resolveCapturedPhoto(String webRoot, ExamRegistrationDTO reg);

    /**
     * Kiểm tra file ảnh có tồn tại trên disk theo URL web.
     *
     * @param webRoot  thư mục gốc web
     * @param photoUrl đường dẫn ảnh dạng web
     * @return true nếu file tồn tại
     */
    boolean photoFileExists(String webRoot, String photoUrl);

    /**
     * Tìm {@link File} ảnh tương ứng URL web.
     *
     * @param webRoot  thư mục gốc web
     * @param photoUrl đường dẫn ảnh dạng web
     * @return file ảnh, hoặc null nếu không tìm thấy
     */
    File findPhotoFile(String webRoot, String photoUrl);

    /**
     * Ghi bytes ảnh ra file dưới web root.
     *
     * @param webRoot    thư mục gốc web
     * @param fileName   tên file đích
     * @param imageBytes dữ liệu ảnh
     * @throws IOException nếu ghi file thất bại
     */
    void writePhotoFile(String webRoot, String fileName, byte[] imageBytes) throws IOException;

    /**
     * Đổi tên file ảnh thành đường dẫn web dùng lưu vào hồ sơ.
     *
     * @param fileName tên file
     * @return đường dẫn dạng web
     */
    String toWebPhotoPath(String fileName);

    /**
     * Tra cứu file ảnh để stream theo kỳ thi và SBD.
     *
     * @param webRoot        thư mục gốc web
     * @param examId         mã kỳ ưu tiên
     * @param fallbackExamId mã kỳ dự phòng
     * @param sbd            số báo danh
     * @return thông tin stream ảnh (status FOUND hoặc mặc định)
     */
    CandidatePhotoStreamDTO resolvePhoto(String webRoot, int examId, int fallbackExamId, String sbd);
}

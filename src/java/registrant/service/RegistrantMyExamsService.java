package registrant.service;

import registrant.dto.RegistrantMyExamRow;
import auth.dto.UserDTO;
import jakarta.servlet.http.HttpServletRequest;
import java.util.List;

/** Service trang Lịch thi & kết quả của thí sinh. */
public interface RegistrantMyExamsService {

    /** Tải toàn bộ ca thi (nguyện vọng + chính thức) của user. */
    List<RegistrantMyExamRow> listExams(UserDTO user);

    /** Gắn danh sách ca thi, bộ lọc và ca đang chọn vào request để render JSP. */
    void copyMyExamsToRequest(UserDTO user, HttpServletRequest request, String selectedExamId);
}

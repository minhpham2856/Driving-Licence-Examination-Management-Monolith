package registrant.service;

import registrant.dto.RegistrantMyExamRow;
import auth.dto.UserDTO;
import jakarta.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * Hợp đồng service trang lịch thi và kết quả ({@code MyExamsServlet}).
 * <p>
 * Join Profile → Candidate → ExamEnrollment → Exam + Payment qua {@code RegistrantDAO};
 * trả {@link registrant.dto.RegistrantMyExamRow} gồm nguyện vọng ngày thi và ca chính thức (SBD, điểm).
 */
public interface RegistrantMyExamsService {

    /** Tải toàn bộ ca thi (nguyện vọng + chính thức) của user. */
    List<RegistrantMyExamRow> listExams(UserDTO user);

    /** Gắn danh sách ca thi, bộ lọc và ca đang chọn vào request để render JSP. */
    void copyMyExamsToRequest(UserDTO user, HttpServletRequest request, String selectedExamId);
}

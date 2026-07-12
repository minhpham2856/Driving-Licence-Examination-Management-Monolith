package examstaff.dao;

import examstaff.dto.ExamSummaryDTO;
import java.sql.Timestamp;

public interface ExamDAO {

    ExamSummaryDTO getById(int id);

    boolean updateStatus(int examId, String status);

    /** Cap nhat trang thai va ghi thoi diem ket thuc ky thi (EndTime). */
    boolean finishExam(int examId, String status, Timestamp endTime);
}

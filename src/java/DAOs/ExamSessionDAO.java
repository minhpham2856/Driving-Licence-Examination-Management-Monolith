package DAOs;

import DTOs.SessionDTO;
import Models.Session;
import java.sql.Date;
import java.util.List;

// DAO cho thao tác với kỳ thi (Session).
public interface ExamSessionDAO {

    // Lấy thông tin kỳ thi theo mã, trả về DTO.
    SessionDTO getById(int id);

    // Lấy thông tin kỳ thi theo mã, trả về Model.
    Session findById(int id);

    // Lấy danh sách các kỳ thi đang hoạt động.
    List<SessionDTO> getActiveSessions();

    // Lấy danh sách tất cả kỳ thi.
    List<SessionDTO> getAllSessions();

    // Lấy danh sách kỳ thi theo ngày thi.
    List<SessionDTO> getSessionsByExamDate(Date examDate);

    // Cập nhật trạng thái kỳ thi.
    boolean updateStatus(int sessionId, String status);
}

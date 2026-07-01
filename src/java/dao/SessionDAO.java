package dao;
import model.Session;
import java.sql.Date;
import java.util.List;
public interface SessionDAO {
    Session getById(int id);
    List<Session> findActive();
    List<Session> findAllOrdered();
    List<Session> findByExamDate(Date examDate);
    boolean updateStatus(int sessionId, String status);
    List<Integer> getExamAreaIds(int sessionId);
    Integer getExamSectionId(int sessionId);
    int countEnrollments(int sessionId);
}

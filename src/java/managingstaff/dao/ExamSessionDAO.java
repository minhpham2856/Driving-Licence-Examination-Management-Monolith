package managingstaff.dao;

import java.util.List;
import managingstaff.dto.SessionDTO;
import managingstaff.dto.ExamRegistrationDTO;

public interface ExamSessionDAO {
    List<SessionDTO> getActiveSessions();
    List<SessionDTO> getAllSessions();
    boolean updateStatus(int examId, String status);
    SessionDTO findById(int examId);
    int create(SessionDTO session);
    boolean update(SessionDTO session);
    List<ExamRegistrationDTO> getCandidates(int examId);
    List<SessionDTO> findPage(String tab, List<Integer> years, int page, int pageSize);
    int count(String tab, List<Integer> years);
    List<Integer> findAvailableYears();
    boolean cancel(int examId);
}

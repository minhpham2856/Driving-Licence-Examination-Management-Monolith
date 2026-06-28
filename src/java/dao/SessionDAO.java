package dao;


import dto.exam.SessionDTO;

import model.exam.Session;
import java.sql.Date;
import java.util.List;


public interface SessionDAO {

    
    SessionDTO getById(int id);

    
    Session findById(int id);

    
    List<SessionDTO> getActiveSessions();

    
    List<SessionDTO> getAllSessions();

    
    List<SessionDTO> getSessionsByExamDate(Date examDate);

    
    boolean updateStatus(int sessionId, String status);

    
    List<Integer> getExamAreaIds(int sessionId);

    
    Integer getExamSectionId(int sessionId);
}

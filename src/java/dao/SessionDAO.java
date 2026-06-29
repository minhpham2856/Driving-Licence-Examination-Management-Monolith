package dao;


import dto.SessionDTO;

import model.Session;
import java.sql.Date;
import java.util.List;


public interface SessionDAO {

    
    SessionDTO getDtoById(int id);

    
    Session getById(int id);

    
    List<SessionDTO> getActiveSessions();

    
    List<SessionDTO> getAllSessions();

    
    List<SessionDTO> getSessionsByExamDate(Date examDate);

    
    boolean updateStatus(int sessionId, String status);

    
    List<Integer> getExamAreaIds(int sessionId);

    
    Integer getExamSectionId(int sessionId);
}

package dao;


import model.exam.ExamArea;
import java.util.List;


public interface ExamAreaDAO {

    
    List<ExamArea> search(String keyword, String areaType);

    
    ExamArea findById(int examAreaId);

    
    int insert(ExamArea area);

    
    boolean update(ExamArea area);

    
    boolean delete(int examAreaId);

    
    int countAll();

    
    ExamArea getById(int examAreaId);

    
    List<ExamArea> getActiveTheoryRooms();

    
    List<ExamArea> getAreasBySessionId(int sessionId);

    
    boolean isAreaInSession(int sessionId, int examAreaId);
}

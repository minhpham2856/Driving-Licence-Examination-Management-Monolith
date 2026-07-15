package admin.dao;

import admin.model.ExamRoom;
import java.util.List;

public interface ExamRoomDAO {
    List<ExamRoom> search(String keyword, Integer areaId, String type, String status);
    ExamRoom findById(int examRoomId);
    int insert(ExamRoom room);
    boolean update(ExamRoom room);
    boolean delete(int examRoomId);
    int countAll();
    int countByStatus(String status);   // status='active'
    int countByType(String type);       // 'theory' | 'practical'
}

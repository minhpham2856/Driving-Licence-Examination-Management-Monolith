package DAOs;

import Models.ExamDevice;
import java.util.List;

public interface ExamDeviceDAO {
    List<ExamDevice> search(String keyword, Integer roomId, String status);
    ExamDevice findById(int examDeviceId);
    int insert(ExamDevice device);
    boolean update(ExamDevice device);
    boolean delete(int examDeviceId);
    int countAll();
    int countByStatus(String status);   // 'active' | 'maintenance' | 'broken'
}

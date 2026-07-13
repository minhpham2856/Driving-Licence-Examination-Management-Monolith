package examstaff.dao;
import shared.model.ExamDevice;
import java.util.List;
public interface ExamDeviceDAO {
    ExamDevice getById(int examDeviceId);
    int insert(ExamDevice device);
    boolean update(ExamDevice device);
    boolean delete(int examDeviceId);
    int countAll();
    List<ExamDevice> search(String keyword, boolean isActive);
    int countByStatus(boolean isActive);
    boolean updateStatus(int examDeviceId, boolean isActive);
    List<ExamDevice> getDevicesByAreaId(int examAreaId);
    List<ExamDevice> getAllByAreaIds(List<Integer> areaIds);
}


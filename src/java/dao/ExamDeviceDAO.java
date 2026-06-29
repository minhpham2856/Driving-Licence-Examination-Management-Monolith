package dao;

import model.exam.ExamDevice;
import java.util.List;

public interface ExamDeviceDAO {

    ExamDevice findById(int examDeviceId);

    int insert(ExamDevice device);

    boolean update(ExamDevice device);

    boolean delete(int examDeviceId);

    int countAll();
    List<ExamDevice> search(String keyword, boolean isActive);
    int countByStatus(boolean isActive);

    boolean updateStatus(int examDeviceId, boolean isActive);

    List<ExamDevice> getDevicesByAreaId(int examAreaId);

    List<ExamDevice> findByAreaIds(List<Integer> areaIds);
}


package DAOs;

import Models.ExamDevice;
import java.util.List;

public interface ExamDeviceDAO {
    List<ExamDevice> getAvailableDevices(String typeFilter);
    /** Lọc theo từ khóa loại thiết bị: 'xận may' hoặc 'o to' */
    List<ExamDevice> getAvailableDevicesByCategory(String category);
    List<ExamDevice> getDevicesByAreaId(int areaId);
    boolean updateStatus(int id, String status);
}

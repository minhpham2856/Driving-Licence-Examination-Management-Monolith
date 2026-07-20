package admin.dao;

import admin.model.DeviceView;
import java.util.List;

public interface ExamDeviceManageDAO {
    List<DeviceView> search(String keyword, String deviceType, Integer zoneId, Integer areaId);
    DeviceView findById(int id);
    int insert(DeviceView d);
    boolean update(DeviceView d);
    boolean setActive(int id, boolean active);
    boolean delete(int id);
    int countAll();
}

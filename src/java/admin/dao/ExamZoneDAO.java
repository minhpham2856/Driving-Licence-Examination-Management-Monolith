package admin.dao;

import admin.model.ZoneView;
import java.util.List;

public interface ExamZoneDAO {
    List<ZoneView> search(String keyword, Boolean active);
    ZoneView findById(int id);
    int insert(ZoneView z);
    boolean update(ZoneView z);
    boolean setActive(int id, boolean active);
    boolean delete(int id);
    int countAll();
    /** Danh sách zone đang hoạt động, cho dropdown ở màn Phòng thi/Máy thi. */
    List<ZoneView> listActive();
}

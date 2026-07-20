package admin.dao;

import admin.model.AreaView;
import java.util.List;

public interface ExamAreaManageDAO {
    List<AreaView> search(String keyword, String areaType, Integer zoneId);
    List<AreaView> listByZone(int zoneId);
    AreaView findById(int id);
    int insert(AreaView a);
    boolean update(AreaView a);
    boolean delete(int id);
    int countAll();
}

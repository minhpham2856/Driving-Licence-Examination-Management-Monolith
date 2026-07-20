package admin.dao;

import admin.model.LicenceView;
import java.util.List;

public interface LicenceManageDAO {
    List<LicenceView> search(String keyword);
    List<LicenceView> listAll();
    LicenceView findById(int id);
    int insert(LicenceView l);
    boolean update(LicenceView l);
    boolean delete(int id);
    boolean classExists(String licenceClass, int excludeId);
    int countAll();
}

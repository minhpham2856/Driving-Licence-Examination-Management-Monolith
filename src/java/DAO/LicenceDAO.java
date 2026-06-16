package DAO;

import Models.Licence;
import java.util.List;

public interface LicenceDAO {
    List<Licence> findAll();
    List<Licence> search(String keyword);
    Licence findById(int licenceId);
    boolean existsByClass(String licenceClass, int excludeId);
    int insert(Licence licence);
    boolean update(Licence licence);
    int countAll();
}

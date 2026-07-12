package examiner.dao;
import shared.model.Licence;
import java.util.List;
public interface LicenceDAO {
    List<Licence> findAll();
    List<Licence> search(String keyword);
    Licence getById(int licenceId);

    Licence getByLicenceClass(String licenceClass);
    boolean existsByClass(String licenceClass, int excludeId);
    int insert(Licence licence);
    boolean update(Licence licence);
    int countAll();
}


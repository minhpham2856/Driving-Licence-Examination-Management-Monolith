package DAOs;

import Models.Licence;
import java.util.List;

public interface LicenceDAO {

    List<Licence> getAll();

    List<Licence> search(String keyword);

    Licence getById(int licenceId);

    boolean existsByClass(String licenceClass, int excludeId);

    int insert(Licence licence);

    boolean update(Licence licence);

    int countAll();
}

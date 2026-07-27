package examiner.dao;

import shared.model.Licence;
import java.util.List;

// DAO contract for Licence persistence; examiner module SQL boundary.
public interface LicenceDAO {

    // Lists all licence class rows.
    List<Licence> getAll();

    // Searches licence rows by class code or description keyword.
    List<Licence> getFiltered(String keyword);

    // Loads one licence row by primary key.
    Licence get(int licenceId);

    // Loads one licence row by class code string.
    Licence getByLicenceClass(String licenceClass);

    // Checks whether another licence row already uses this class code.
    boolean existsByClass(String licenceClass, int excludeId);

    // Inserts a new licence row and returns generated id.
    int add(Licence licence);

    // Updates an existing licence row.
    boolean update(Licence licence);

    // Returns total count of licence rows.
    int countAll();
}

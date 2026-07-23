package managingstaff.dao;

import java.util.List;
import shared.model.Licence;

public interface LicenceDAO {
    List<Licence> findAll();
    Licence findById(int licenceId);
}

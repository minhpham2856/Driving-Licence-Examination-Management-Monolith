package general.dao;

import shared.model.LicenceFee;
import java.util.List;
import java.util.Map;

public interface LicenceFeeDAO {

    // Fees keyed by LicenceId (NULL licence fees under key 0)
    Map<Integer, List<LicenceFee>> getAllGroupedByLicenceId();
}

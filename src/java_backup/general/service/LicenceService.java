package general.service;

import general.dto.ServiceResult;
import shared.model.Licence;
import java.util.List;

public interface LicenceService {

    ServiceResult<List<Licence>> getLicenceCategories();
}


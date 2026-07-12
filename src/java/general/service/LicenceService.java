package general.service;

import general.dto.LicenceSearchCriteriaDTO;
import general.dto.ServiceResult;
import shared.model.Licence;
import java.util.List;

public interface LicenceService {

    ServiceResult<List<Licence>> getLicenceCategories();

    ServiceResult<List<Licence>> searchLicenceCategories(LicenceSearchCriteriaDTO criteria);
}


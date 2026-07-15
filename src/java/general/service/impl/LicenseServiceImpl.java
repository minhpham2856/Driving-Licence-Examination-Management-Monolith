package general.service.impl;

import general.dao.LicenceDAO;
import general.dao.impl.LicenceDAOImpl;
import general.dto.LicenceSearchCriteriaDTO;
import general.dto.ServiceResult;
import shared.model.Licence;
import java.util.List;
import shared.enums.ErrorType;
import general.service.LicenceService;

public class LicenseServiceImpl implements LicenceService {

    private final LicenceDAO licenceDAO = new LicenceDAOImpl();

    @Override
    public ServiceResult<List<Licence>> getLicenceCategories() {
        return searchLicenceCategories(new LicenceSearchCriteriaDTO());
    }

    @Override
    public ServiceResult<List<Licence>> searchLicenceCategories(LicenceSearchCriteriaDTO criteria) {
        if (criteria == null) {
            criteria = new LicenceSearchCriteriaDTO();
        }

        List<Licence> list = licenceDAO.searchByCriteria(criteria);

        if (list == null) {
            return ServiceResult.fail(ErrorType.PERSISTENCE_FAILED, "Lỗi khi lấy dữ liệu hạng bằng.");
        }

        return ServiceResult.ok(list);
    }
}

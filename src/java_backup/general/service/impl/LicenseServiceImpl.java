package general.service.impl;

import general.dao.LicenceDAO;
import general.dao.impl.LicenceDAOImpl;
import general.dto.ServiceResult;
import shared.model.Licence;
import java.util.List;
import shared.enums.ErrorType;
import general.service.LicenceService;

public class LicenseServiceImpl implements LicenceService {

    private final LicenceDAO licenceDAO = new LicenceDAOImpl();

    @Override
    public ServiceResult<List<Licence>> getLicenceCategories() {

        List<Licence> list = licenceDAO.getAll();

        // validate list
        if (list == null) {
            return ServiceResult.fail(ErrorType.PERSISTENCE_FAILED, "Lá»—i khi láº¥y dá»¯ liá»‡u háº¡ng báº±ng.");
        }

        // return data
        return ServiceResult.ok(list);
    }
}


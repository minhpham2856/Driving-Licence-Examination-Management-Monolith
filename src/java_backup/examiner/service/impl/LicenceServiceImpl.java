package examiner.service.impl;

import examiner.dao.LicenceDAO;
import examiner.dao.impl.LicenceDAOImpl;
import examiner.dto.ServiceResult;
import examiner.dto.SaveResultDTO;
import examiner.enums.ErrorType;
import shared.model.Licence;
import examiner.service.LicenceService;

import java.util.List;

public class LicenceServiceImpl implements LicenceService {

    private final LicenceDAO dao = new LicenceDAOImpl();

    @Override
    public List<Licence> search(String keyword) {
        return dao.search(keyword);
    }

    @Override
    public List<Licence> findAll() {
        return dao.findAll();
    }

    @Override
    public Licence getById(int id) {
        return dao.getById(id);
    }

    @Override
    public int countAll() {
        return dao.countAll();
    }

    @Override
    public ServiceResult<SaveResultDTO> save(Licence licence, int adminUserId) {
        if (licence.getLicenceClass() == null || licence.getLicenceClass().isBlank()) {
            return ServiceResult.fail(ErrorType.VALIDATION_FAILED, "Vui lÃ²ng nháº­p mÃ£ háº¡ng (VD: A1, B2, C...).");
        }
        if (licence.getMinimumAge() <= 0) {
            return ServiceResult.fail(ErrorType.VALIDATION_FAILED, "Äá»™ tuá»•i tá»‘i thiá»ƒu pháº£i lá»›n hÆ¡n 0.");
        }
        if (licence.getValidForYears() <= 0) {
            return ServiceResult.fail(ErrorType.VALIDATION_FAILED, "Thá»i háº¡n (nÄƒm) pháº£i lá»›n hÆ¡n 0.");
        }
        boolean isEdit = licence.getLicenceId() > 0;
        if (dao.existsByClass(licence.getLicenceClass(), licence.getLicenceId())) {
            return ServiceResult.fail(ErrorType.VALIDATION_FAILED,
                    "MÃ£ Háº¡ng \"" + licence.getLicenceClass() + "\" Ä‘Ã£ tá»“n táº¡i.");
        }
        if (licence.getUpgradeFromLicenceId() != null && licence.getUpgradeFromLicenceId() == licence.getLicenceId()
                && isEdit) {
            return ServiceResult.fail(ErrorType.VALIDATION_FAILED, "Háº¡ng khÃ´ng thá»ƒ nÃ¢ng cáº¥p tá»« chÃ­nh nÃ³.");
        }
        if (isEdit) {
            if (dao.update(licence)) {
                SaveResultDTO result = new SaveResultDTO();
                result.setEntityId(licence.getLicenceId());
                result.setMessage("ÄÃ£ cáº­p nháº­t háº¡ng \"" + licence.getLicenceClass() + "\".");
                return ServiceResult.ok(result, result.getMessage());
            }
            return ServiceResult.fail(ErrorType.PERSISTENCE_FAILED, "Cáº­p nháº­t háº¡ng GPLX tháº¥t báº¡i.");
        }
        int newId = dao.insert(licence);
        if (newId > 0) {
            SaveResultDTO result = new SaveResultDTO();
            result.setEntityId(newId);
            result.setMessage("ÄÃ£ thÃªm háº¡ng \"" + licence.getLicenceClass() + "\".");
            return ServiceResult.ok(result, result.getMessage());
        }
        return ServiceResult.fail(ErrorType.PERSISTENCE_FAILED, "ThÃªm háº¡ng tháº¥t báº¡i.");
    }
}


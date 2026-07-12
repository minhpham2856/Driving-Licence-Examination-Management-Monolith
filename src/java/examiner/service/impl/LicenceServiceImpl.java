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
            return ServiceResult.fail(ErrorType.VALIDATION_FAILED, "Vui lòng nhập mã hạng (VD: A1, B2, C...).");
        }
        if (licence.getMinimumAge() <= 0) {
            return ServiceResult.fail(ErrorType.VALIDATION_FAILED, "Độ tuổi tối thiểu phải lớn hơn 0.");
        }
        if (licence.getValidForYears() <= 0) {
            return ServiceResult.fail(ErrorType.VALIDATION_FAILED, "Thời hạn (năm) phải lớn hơn 0.");
        }
        boolean isEdit = licence.getLicenceId() > 0;
        if (dao.existsByClass(licence.getLicenceClass(), licence.getLicenceId())) {
            return ServiceResult.fail(ErrorType.VALIDATION_FAILED,
                    "Mã Hạng \"" + licence.getLicenceClass() + "\" đã tồn tại.");
        }
        if (licence.getUpgradeFromLicenceId() != null && licence.getUpgradeFromLicenceId() == licence.getLicenceId()
                && isEdit) {
            return ServiceResult.fail(ErrorType.VALIDATION_FAILED, "Hạng không thể nâng cấp từ chính nó.");
        }
        if (isEdit) {
            if (dao.update(licence)) {
                SaveResultDTO result = new SaveResultDTO();
                result.setEntityId(licence.getLicenceId());
                result.setMessage("Đã cập nhật hạng \"" + licence.getLicenceClass() + "\".");
                return ServiceResult.ok(result, result.getMessage());
            }
            return ServiceResult.fail(ErrorType.PERSISTENCE_FAILED, "Cập nhật hạng GPLX thất bại.");
        }
        int newId = dao.insert(licence);
        if (newId > 0) {
            SaveResultDTO result = new SaveResultDTO();
            result.setEntityId(newId);
            result.setMessage("Đã thêm hạng \"" + licence.getLicenceClass() + "\".");
            return ServiceResult.ok(result, result.getMessage());
        }
        return ServiceResult.fail(ErrorType.PERSISTENCE_FAILED, "Thêm hạng thất bại.");
    }
}


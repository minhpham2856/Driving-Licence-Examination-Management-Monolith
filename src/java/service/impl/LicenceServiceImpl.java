package service.impl;

import dao.LicenceDAO;
import dao.impl.LicenceDAOImpl;
import dto.ServiceResult;
import dto.payload.SaveEntityData;
import enums.ErrorType;
import model.Licence;
import service.LicenceService;

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
    public ServiceResult<SaveEntityData> save(Licence licence, int adminUserId) {
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
                return ServiceResult.ok(new SaveEntityData(licence.getLicenceId()),
                        "Đã cập nhật hạng \"" + licence.getLicenceClass() + "\".");
            }
            return ServiceResult.fail(ErrorType.PERSISTENCE_FAILED, "Cập nhật hạng GPLX thất bại.");
        }
        int newId = dao.insert(licence);
        if (newId > 0) {
            return ServiceResult.ok(new SaveEntityData(newId),
                    "Đã thêm hạng \"" + licence.getLicenceClass() + "\".");
        }
        return ServiceResult.fail(ErrorType.PERSISTENCE_FAILED, "Thêm hạng thất bại.");
    }
}

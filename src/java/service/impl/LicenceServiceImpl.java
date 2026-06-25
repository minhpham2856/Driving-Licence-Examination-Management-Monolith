package service.impl;

import dao.LicenceDAO;
import dao.impl.LicenceDAOImpl;
import model.licence.Licence;
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
    public Licence findById(int id) {
        return dao.findById(id);
    }

    @Override
    public int countAll() {
        return dao.countAll();
    }

    @Override
    public SaveResult save(Licence l, int adminUserId) {
        if (l.getLicenceClass() == null || l.getLicenceClass().trim().isEmpty()) {
            return new SaveResult(false, "Vui lòng nhập mã hạng (VD: A1, B2, C...).", l.getLicenceId());
        }
        if (l.getMinimumAge() <= 0) {
            return new SaveResult(false, "Độ tuổi tối thiểu phải lớn hơn 0.", l.getLicenceId());
        }
        if (l.getValidForYears() <= 0) {
            return new SaveResult(false, "Thời hạn (năm) phải lớn hơn 0.", l.getLicenceId());
        }
        
        boolean isEdit = l.getLicenceId() > 0;
        if (dao.existsByClass(l.getLicenceClass(), l.getLicenceId())) {
            return new SaveResult(false, "Mã Hạng \"" + l.getLicenceClass() + "\" đã tồn tại.", l.getLicenceId());
        }
        if (l.getUpgradeFromLicenceId() != null && l.getUpgradeFromLicenceId() == l.getLicenceId() && isEdit) {
            return new SaveResult(false, "Hạng không thể nâng cấp từ chính nó.", l.getLicenceId());
        }

        if (isEdit) {
            l.setUpdatedByUserId(adminUserId);
            boolean ok = dao.update(l);
            if (ok) {
                return new SaveResult(true, "Đã cập nhật hạng \"" + l.getLicenceClass() + "\".", l.getLicenceId());
            } else {
                return new SaveResult(false, "Cập nhật hạng GPLX thất bại.", l.getLicenceId());
            }
        } else {
            l.setCreatedByUserId(adminUserId);
            l.setUpdatedByUserId(adminUserId);
            int newId = dao.insert(l);
            if (newId > 0) {
                return new SaveResult(true, "Đã thêm hạng \"" + l.getLicenceClass() + "\".", newId);
            } else {
                return new SaveResult(false, "Thêm hạng thất bại.", 0);
            }
        }
    }
}

package examiner.service.impl;

import examiner.dao.ExamAreaDAO;
import examiner.dao.impl.ExamAreaDAOImpl;
import examiner.dto.ServiceResult;
import examiner.dto.SaveResultDTO;
import examiner.enums.ExamAreaType;
import examiner.enums.ErrorType;
import shared.model.ExamArea;
import examiner.service.ExamAreaService;

import java.util.List;

public class ExamAreaServiceImpl implements ExamAreaService {

    private final ExamAreaDAO dao = new ExamAreaDAOImpl();

    @Override
    public ExamArea getById(int id) {
        return dao.getById(id);
    }

    @Override
    public List<ExamArea> search(String keyword, String type) {
        return dao.search(keyword, type);
    }

    @Override
    public List<ExamArea> getActiveTheoryRooms() {
        return dao.getActiveTheoryRooms();
    }

    @Override
    public int countAll() {
        return dao.countAll();
    }

    @Override
    public ServiceResult<SaveResultDTO> save(ExamArea area, int adminUserId) {
        if (area.getAreaName() == null || area.getAreaName().isBlank()) {
            return ServiceResult.fail(ErrorType.VALIDATION_FAILED, "Vui lÃ²ng nháº­p tÃªn khu vá»±c thi.");
        }
        if (area.getAreaType() == null || area.getAreaType().isBlank()) {
            return ServiceResult.fail(ErrorType.VALIDATION_FAILED, "Vui lÃ²ng chá»n loáº¡i khu vá»±c.");
        }
        if (ExamAreaType.fromValue(area.getAreaType()) == null) {
            return ServiceResult.fail(ErrorType.VALIDATION_FAILED, "Loáº¡i khu vá»±c khÃ´ng há»£p lá»‡.");
        }
        if (area.getLocation() == null || area.getLocation().isBlank()) {
            return ServiceResult.fail(ErrorType.VALIDATION_FAILED, "Vui lÃ²ng nháº­p Ä‘á»‹a chá»‰ khu vá»±c.");
        }
        if (area.getCapacity() != null && area.getCapacity() <= 0) {
            return ServiceResult.fail(ErrorType.VALIDATION_FAILED, "Sá»©c chá»©a pháº£i lá»›n hÆ¡n 0.");
        }
        if (area.getExamZoneId() <= 0) {
            return ServiceResult.fail(ErrorType.VALIDATION_FAILED, "Vui lÃ²ng chá»n khu vá»±c thi (ExamZone).");
        }
        boolean isEdit = area.getExamAreaId() > 0;
        if (isEdit) {
            if (dao.update(area)) {
                SaveResultDTO result = new SaveResultDTO();
                result.setEntityId(area.getExamAreaId());
                result.setMessage("ÄÃ£ cáº­p nháº­t khu vá»±c \"" + area.getAreaName() + "\".");
                return ServiceResult.ok(result, result.getMessage());
            }
            return ServiceResult.fail(ErrorType.PERSISTENCE_FAILED, "Cáº­p nháº­t khu vá»±c tháº¥t báº¡i.");
        }
        int newId = dao.insert(area);
        if (newId > 0) {
            SaveResultDTO result = new SaveResultDTO();
            result.setEntityId(newId);
            result.setMessage("ÄÃ£ thÃªm khu vá»±c \"" + area.getAreaName() + "\".");
            return ServiceResult.ok(result, result.getMessage());
        }
        return ServiceResult.fail(ErrorType.PERSISTENCE_FAILED, "ThÃªm khu vá»±c tháº¥t báº¡i.");
    }

    @Override
    public ServiceResult<Void> delete(int id, int adminUserId) {
        ExamArea area = dao.getById(id);
        if (area == null) {
            return ServiceResult.fail(ErrorType.NOT_FOUND, "Khu vá»±c khÃ´ng tá»“n táº¡i.");
        }
        if (id > 0 && dao.delete(id)) {
            return ServiceResult.ok(null, "ÄÃ£ xÃ³a khu vá»±c thi.");
        }
        return ServiceResult.fail(ErrorType.PERSISTENCE_FAILED,
                "KhÃ´ng thá»ƒ xÃ³a khu vá»±c nÃ y (cÃ³ thá»ƒ Ä‘ang Ä‘Æ°á»£c sá»­ dá»¥ng bá»Ÿi phÃ²ng/thiáº¿t bá»‹/ká»³ thi).");
    }
}


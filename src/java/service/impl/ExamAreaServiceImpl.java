package service.impl;

import dao.ExamAreaDAO;
import dao.impl.ExamAreaDAOImpl;
import dto.ServiceResult;
import dto.payload.SaveEntityData;
import enums.ExamAreaType;
import enums.ErrorType;
import model.ExamArea;
import service.ExamAreaService;

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
    public ServiceResult<SaveEntityData> save(ExamArea area, int adminUserId) {
        if (area.getAreaName() == null || area.getAreaName().isBlank()) {
            return ServiceResult.fail(ErrorType.VALIDATION_FAILED, "Vui lòng nhập tên khu vực thi.");
        }
        if (area.getAreaType() == null || area.getAreaType().isBlank()) {
            return ServiceResult.fail(ErrorType.VALIDATION_FAILED, "Vui lòng chọn loại khu vực.");
        }
        if (ExamAreaType.fromValue(area.getAreaType()) == null) {
            return ServiceResult.fail(ErrorType.VALIDATION_FAILED, "Loại khu vực không hợp lệ.");
        }
        if (area.getLocation() == null || area.getLocation().isBlank()) {
            return ServiceResult.fail(ErrorType.VALIDATION_FAILED, "Vui lòng nhập địa chỉ khu vực.");
        }
        if (area.getCapacity() != null && area.getCapacity() <= 0) {
            return ServiceResult.fail(ErrorType.VALIDATION_FAILED, "Sức chứa phải lớn hơn 0.");
        }
        if (area.getExamZoneId() <= 0) {
            return ServiceResult.fail(ErrorType.VALIDATION_FAILED, "Vui lòng chọn khu vực thi (ExamZone).");
        }
        boolean isEdit = area.getExamAreaId() > 0;
        if (isEdit) {
            if (dao.update(area)) {
                return ServiceResult.ok(new SaveEntityData(area.getExamAreaId()),
                        "Đã cập nhật khu vực \"" + area.getAreaName() + "\".");
            }
            return ServiceResult.fail(ErrorType.PERSISTENCE_FAILED, "Cập nhật khu vực thất bại.");
        }
        int newId = dao.insert(area);
        if (newId > 0) {
            return ServiceResult.ok(new SaveEntityData(newId),
                    "Đã thêm khu vực \"" + area.getAreaName() + "\".");
        }
        return ServiceResult.fail(ErrorType.PERSISTENCE_FAILED, "Thêm khu vực thất bại.");
    }

    @Override
    public ServiceResult<Void> delete(int id, int adminUserId) {
        ExamArea area = dao.getById(id);
        if (area == null) {
            return ServiceResult.fail(ErrorType.NOT_FOUND, "Khu vực không tồn tại.");
        }
        if (id > 0 && dao.delete(id)) {
            return ServiceResult.ok(null, "Đã xóa khu vực thi.");
        }
        return ServiceResult.fail(ErrorType.PERSISTENCE_FAILED,
                "Không thể xóa khu vực này (có thể đang được sử dụng bởi phòng/thiết bị/kỳ thi).");
    }
}

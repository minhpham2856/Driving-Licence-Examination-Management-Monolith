package service.impl;

import dao.ExamAreaDAO;
import dao.impl.ExamAreaDAOImpl;
import model.exam.ExamArea;
import service.ExamAreaService;

import java.util.List;

public class ExamAreaServiceImpl implements ExamAreaService {
    
    private final ExamAreaDAO dao = new ExamAreaDAOImpl();

    @Override
    public ExamArea findById(int id) {
        return dao.findById(id);
    }

    @Override
    public List<ExamArea> search(String keyword, String type) {
        return dao.search(keyword, type);
    }

    @Override
    public int countAll() {
        return dao.countAll();
    }

    @Override
    public SaveResult save(ExamArea area, int adminUserId) {
        if (area.getAreaName() == null || area.getAreaName().trim().isEmpty()) {
            return new SaveResult(false, "Vui lòng nhập tên khu vực thi.", area.getExamAreaId());
        }
        if (area.getAreaType() == null || area.getAreaType().trim().isEmpty()) {
            return new SaveResult(false, "Vui lòng chọn loại khu vực.", area.getExamAreaId());
        }
        if (area.getLocation() == null || area.getLocation().trim().isEmpty()) {
            return new SaveResult(false, "Vui lòng nhập địa chỉ khu vực.", area.getExamAreaId());
        }
        if (area.getCapacity() <= 0) {
            return new SaveResult(false, "Sức chứa phải lớn hơn 0.", area.getExamAreaId());
        }

        boolean isEdit = area.getExamAreaId() > 0;
        if (isEdit) {
            
            boolean ok = dao.update(area);
            if (ok) {
                return new SaveResult(true, "da cap nhat khu vuc \"" + area.getAreaName() + "\".", area.getExamAreaId());
            } else {
                return new SaveResult(false, "cap nhat khu vuc that bai", area.getExamAreaId());
            }
        } else {
            
            
            int newId = dao.insert(area);
            boolean ok = newId > 0;
            if (ok) {
                return new SaveResult(true, "Đã thêm khu vực \"" + area.getAreaName() + "\".", newId);
            } else {
                return new SaveResult(false, "them khu vuc that bai", 0);
            }
        }
    }

    @Override
    public DeleteResult delete(int id, int adminUserId) {
        ExamArea area = dao.findById(id);
        if (area == null) {
            return new DeleteResult(false, "Khu vực không tồn tại.");
        }
        boolean ok = id > 0 && dao.delete(id);
        if (ok) {
            return new DeleteResult(true, "Đã xóa khu vực thi.");
        } else {
            return new DeleteResult(false, "Không thể xóa khu vực này (có thể đang được sử dụng bởi phòng/thiết bị/kỳ thi).");
        }
    }
}


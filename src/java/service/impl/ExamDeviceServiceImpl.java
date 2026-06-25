package service.impl;

import dao.ExamDeviceManageDAO;
import dao.impl.ExamDeviceManageDAOImpl;
import dto.exam.ExamDeviceViewDTO;
import service.ExamDeviceService;

import java.util.List;

public class ExamDeviceServiceImpl implements ExamDeviceService {
    
    private final ExamDeviceManageDAO dao = new ExamDeviceManageDAOImpl();

    @Override
    public List<ExamDeviceViewDTO> search(String keyword, String status) {
        return dao.search(keyword, status);
    }

    @Override
    public int countAll() {
        return dao.countAll();
    }

    @Override
    public int countByStatus(String status) {
        return dao.countByStatus(status);
    }

    @Override
    public SaveResult save(ExamDeviceViewDTO dev, Integer adminUserId) {
        if (dev.getDeviceName() == null || dev.getDeviceName().trim().isEmpty()) {
            return new SaveResult(false, "Vui lòng nhập tên máy thi.", dev.getExamDeviceId());
        }
        if (dev.getDeviceType() == null || dev.getDeviceType().trim().isEmpty()) {
            return new SaveResult(false, "Vui lòng nhập loại thiết bị.", dev.getExamDeviceId());
        }
        if (dev.getStatus() == null || dev.getStatus().trim().isEmpty()) {
            return new SaveResult(false, "Vui lòng chọn tình trạng máy.", dev.getExamDeviceId());
        }
        if (dev.getExamAreaId() <= 0) {
            return new SaveResult(false, "Vui lòng chọn khu vực thi.", dev.getExamDeviceId());
        }

        boolean isEdit = dev.getExamDeviceId() > 0;
        if (isEdit) {
            boolean ok = dao.update(dev, adminUserId);
            if (ok) {
                return new SaveResult(true, "Đã cập nhật máy \"" + dev.getDeviceName() + "\".", dev.getExamDeviceId());
            } else {
                return new SaveResult(false, "Cập nhật máy thi thất bại.", dev.getExamDeviceId());
            }
        } else {
            int newId = dao.insert(dev, adminUserId);
            boolean ok = newId > 0;
            if (ok) {
                return new SaveResult(true, "Đã thêm máy \"" + dev.getDeviceName() + "\".", newId);
            } else {
                return new SaveResult(false, "Thêm máy thi thất bại.", 0);
            }
        }
    }

    @Override
    public DeleteResult delete(int id, Integer adminUserId) {
        ExamDeviceViewDTO dev = dao.findById(id);
        if (dev == null) {
            return new DeleteResult(false, "Máy thi không tồn tại.");
        }
        boolean ok = id > 0 && dao.delete(id);
        if (ok) {
            return new DeleteResult(true, "Đã xóa máy thi.");
        } else {
            return new DeleteResult(false, "Xóa máy thi thất bại.");
        }
    }
}

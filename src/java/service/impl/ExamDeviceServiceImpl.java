package service.impl;

import dao.ExamDeviceManageDAO;
import dao.impl.ExamDeviceManageDAOImpl;
import dao.ExamAreaDAO;
import dao.impl.ExamAreaDAOImpl;
import dto.exam.ExamDeviceViewDTO;
import model.exam.ExamArea;
import model.exam.ExamDevice;
import service.ExamDeviceService;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ExamDeviceServiceImpl implements ExamDeviceService {
    
    private final ExamDeviceManageDAO dao = new ExamDeviceManageDAOImpl();
    private final ExamAreaDAO areaDao = new ExamAreaDAOImpl();

    @Override
    public List<ExamDeviceViewDTO> search(String keyword, String status) {
        List<ExamDevice> devices = dao.search(keyword, status);
        if (devices.isEmpty()) {
            return new ArrayList<>();
        }

        // Stitch AreaName
        List<ExamArea> areas = areaDao.search(null, null);
        Map<Integer, String> areaMap = areas.stream()
                .collect(Collectors.toMap(ExamArea::getExamAreaId, ExamArea::getAreaName));

        List<ExamDeviceViewDTO> dtos = new ArrayList<>();
        for (ExamDevice d : devices) {
            ExamDeviceViewDTO dto = mapToDTO(d);
            dto.setAreaName(areaMap.get(d.getExamAreaId()));
            dtos.add(dto);
        }
        return dtos;
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

        ExamDevice model = new ExamDevice();
        model.setExamDeviceId(dev.getExamDeviceId());
        model.setDeviceName(dev.getDeviceName());
        model.setDeviceType(dev.getDeviceType());
        model.setStatus(dev.getStatus());
        model.setExamAreaId(dev.getExamAreaId());

        boolean isEdit = dev.getExamDeviceId() > 0;
        if (isEdit) {
            boolean ok = dao.update(model, adminUserId);
            if (ok) {
                return new SaveResult(true, "Đã cập nhật máy \"" + dev.getDeviceName() + "\".", dev.getExamDeviceId());
            } else {
                return new SaveResult(false, "Cập nhật máy thi thất bại.", dev.getExamDeviceId());
            }
        } else {
            int newId = dao.insert(model, adminUserId);
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
        ExamDevice dev = dao.findById(id);
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

    private ExamDeviceViewDTO mapToDTO(ExamDevice model) {
        ExamDeviceViewDTO dto = new ExamDeviceViewDTO();
        dto.setExamDeviceId(model.getExamDeviceId());
        dto.setDeviceName(model.getDeviceName());
        dto.setDeviceType(model.getDeviceType());
        dto.setStatus(model.getStatus());
        dto.setExamAreaId(model.getExamAreaId());
        return dto;
    }
}

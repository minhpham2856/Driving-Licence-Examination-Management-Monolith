package service.impl;

import dao.ExamDeviceDAO;
import dao.impl.ExamDeviceDAOImpl;
import dao.AuditDAO;
import dao.impl.AuditDAOImpl;
import dao.ExamAreaDAO;
import dao.impl.ExamAreaDAOImpl;
import dto.ExamDeviceViewDTO;
import dto.ServiceResult;
import dto.payload.DeleteExamDeviceCommand;
import dto.payload.SaveExamDeviceCommand;
import dto.payload.SaveExamDeviceData;
import model.Audit;
import model.ExamArea;
import model.ExamDevice;
import service.ExamDeviceService;
import enums.DeviceStatus;
import enums.ErrorType;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ExamDeviceServiceImpl implements ExamDeviceService {

    private final ExamDeviceDAO dao = new ExamDeviceDAOImpl();
    private final ExamAreaDAO areaDao = new ExamAreaDAOImpl();
    private final AuditDAO auditDAO = new AuditDAOImpl();

    @Override
    public List<ExamDeviceViewDTO> search(String keyword, String status) {
        boolean isActive = status == null || status.isBlank() || isActiveDeviceStatus(status);
        List<ExamDevice> devices = dao.search(keyword, isActive);
        if (devices.isEmpty()) {
            return new ArrayList<>();
        }
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
        if (status == null || status.isBlank()) {
            return dao.countByStatus(true);
        }
        return dao.countByStatus(isActiveDeviceStatus(status));
    }

    @Override
    public ServiceResult<SaveExamDeviceData> save(SaveExamDeviceCommand command) {
        ExamDeviceViewDTO dev = command.getDevice();
        if (dev == null) {
            return ServiceResult.fail(ErrorType.VALIDATION_FAILED, "Thiết bị không hợp lệ.");
        }
        if (dev.getDeviceName() == null || dev.getDeviceName().isBlank()) {
            return ServiceResult.fail(ErrorType.VALIDATION_FAILED, "Vui lòng nhập tên máy thi.");
        }
        if (dev.getDeviceType() == null || dev.getDeviceType().isBlank()) {
            return ServiceResult.fail(ErrorType.VALIDATION_FAILED, "Vui lòng nhập loại thiết bị.");
        }
        if (dev.getStatus() == null || dev.getStatus().isBlank()) {
            return ServiceResult.fail(ErrorType.VALIDATION_FAILED, "Vui lòng chọn tình trạng máy.");
        }
        if (dev.getExamAreaId() <= 0) {
            return ServiceResult.fail(ErrorType.VALIDATION_FAILED, "Vui lòng chọn khu vực thi.");
        }
        ExamDevice model = new ExamDevice();
        model.setExamDeviceId(dev.getExamDeviceId());
        model.setDeviceName(dev.getDeviceName());
        model.setDeviceType(dev.getDeviceType());
        model.setActive(isActiveDeviceStatus(dev.getStatus()));
        model.setExamAreaId(dev.getExamAreaId());
        Integer adminUserId = command.getAdminUserId();
        if (dev.getExamDeviceId() > 0) {
            if (!dao.update(model)) {
                return ServiceResult.fail(ErrorType.PERSISTENCE_FAILED, "Cập nhật máy thi thất bại.");
            }
            logAudit(adminUserId, "UPDATE", "ExamDevice", String.valueOf(dev.getExamDeviceId()), "Giám khảo cập nhật máy thi");
            String message = "Đã cập nhật máy \"" + dev.getDeviceName() + "\".";
            return ServiceResult.ok(new SaveExamDeviceData(dev.getExamDeviceId(), message), message);
        }
        int newId = dao.insert(model);
        if (newId <= 0) {
            return ServiceResult.fail(ErrorType.PERSISTENCE_FAILED, "Thêm máy thi thất bại.");
        }
        logAudit(adminUserId, "INSERT", "ExamDevice", String.valueOf(newId), "Giám khảo thêm máy thi");
        String message = "Đã thêm máy \"" + dev.getDeviceName() + "\".";
        return ServiceResult.ok(new SaveExamDeviceData(newId, message), message);
    }

    @Override
    public ServiceResult<Void> delete(DeleteExamDeviceCommand command) {
        int id = command.getDeviceId();
        ExamDevice dev = dao.getById(id);
        if (dev == null) {
            return ServiceResult.fail(ErrorType.NOT_FOUND, "Máy thi không tồn tại.");
        }
        if (id <= 0 || !dao.delete(id)) {
            return ServiceResult.fail(ErrorType.PERSISTENCE_FAILED, "Xóa máy thi thất bại.");
        }
        logAudit(command.getAdminUserId(), "DELETE", "ExamDevice", String.valueOf(id), "Giám khảo xóa máy thi");
        return ServiceResult.ok(null, "Đã xóa máy thi.");
    }

    private ExamDeviceViewDTO mapToDTO(ExamDevice model) {
        ExamDeviceViewDTO dto = new ExamDeviceViewDTO();
        dto.setExamDeviceId(model.getExamDeviceId());
        dto.setDeviceName(model.getDeviceName());
        dto.setDeviceType(model.getDeviceType());
        dto.setStatus(model.isActive() ? DeviceStatus.ACTIVE.getValue() : DeviceStatus.MAINTENANCE.getValue());
        dto.setExamAreaId(model.getExamAreaId());
        return dto;
    }

    private void logAudit(Integer userId, String action, String entityName, String entityId, String reason) {
        Audit audit = new Audit();
        audit.setUserId(userId != null ? userId : 3);
        audit.setAction(action);
        audit.setEntityName(entityName);
        audit.setEntityId(entityId);
        audit.setReason(reason);
        auditDAO.insert(audit);
    }

    private static boolean isActiveDeviceStatus(String status) {
        DeviceStatus deviceStatus = DeviceStatus.fromValue(status);
        return deviceStatus == DeviceStatus.ACTIVE;
    }
}

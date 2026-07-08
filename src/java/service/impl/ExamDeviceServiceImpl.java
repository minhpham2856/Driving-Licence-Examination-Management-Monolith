package service.impl;

import dao.ExamDeviceDAO;
import dao.impl.ExamDeviceDAOImpl;
import dao.AuditDAO;
import dao.impl.AuditDAOImpl;
import dao.ExamAreaDAO;
import dao.impl.ExamAreaDAOImpl;
import dto.DeviceRowDTO;
import dto.ServiceResult;
import dto.SaveResultDTO;
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
    public List<DeviceRowDTO> search(String keyword, String status) {
        boolean isActive = status == null || status.isBlank() || isActiveDeviceStatus(status);
        List<ExamDevice> devices = dao.search(keyword, isActive);
        if (devices.isEmpty()) {
            return new ArrayList<>();
        }
        List<ExamArea> areas = areaDao.search(null, null);
        Map<Integer, String> areaMap = areas.stream()
                .collect(Collectors.toMap(ExamArea::getExamAreaId, ExamArea::getAreaName));
        List<DeviceRowDTO> dtos = new ArrayList<>();
        for (ExamDevice d : devices) {
            DeviceRowDTO dto = mapToDTO(d);
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
    public ServiceResult<SaveResultDTO> save(DeviceRowDTO dev, Integer adminUserId) {
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
        Integer adminId = adminUserId;
        if (dev.getExamDeviceId() > 0) {
            if (!dao.update(model)) {
                return ServiceResult.fail(ErrorType.PERSISTENCE_FAILED, "Cập nhật máy thi thất bại.");
            }
            logAudit(adminId, "UPDATE", "ExamDevice", String.valueOf(dev.getExamDeviceId()), "Giám khảo cập nhật máy thi");
            String message = "Đã cập nhật máy \"" + dev.getDeviceName() + "\".";
            SaveResultDTO result = new SaveResultDTO();
            result.setEntityId(dev.getExamDeviceId());
            result.setMessage(message);
            return ServiceResult.ok(result, message);
        }
        int newId = dao.insert(model);
        if (newId <= 0) {
            return ServiceResult.fail(ErrorType.PERSISTENCE_FAILED, "Thêm máy thi thất bại.");
        }
        logAudit(adminId, "INSERT", "ExamDevice", String.valueOf(newId), "Giám khảo thêm máy thi");
        String message = "Đã thêm máy \"" + dev.getDeviceName() + "\".";
        SaveResultDTO result = new SaveResultDTO();
        result.setEntityId(newId);
        result.setMessage(message);
        return ServiceResult.ok(result, message);
    }

    @Override
    public ServiceResult<Void> delete(int deviceId, Integer adminUserId) {
        int id = deviceId;
        ExamDevice dev = dao.getById(id);
        if (dev == null) {
            return ServiceResult.fail(ErrorType.NOT_FOUND, "Máy thi không tồn tại.");
        }
        if (id <= 0 || !dao.delete(id)) {
            return ServiceResult.fail(ErrorType.PERSISTENCE_FAILED, "Xóa máy thi thất bại.");
        }
        logAudit(adminUserId, "DELETE", "ExamDevice", String.valueOf(id), "Giám khảo xóa máy thi");
        return ServiceResult.ok(null, "Đã xóa máy thi.");
    }

    private DeviceRowDTO mapToDTO(ExamDevice model) {
        DeviceRowDTO dto = new DeviceRowDTO();
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

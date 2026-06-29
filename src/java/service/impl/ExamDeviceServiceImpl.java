package service.impl;

import dao.ExamDeviceDAO;
import dao.impl.ExamDeviceDAOImpl;
import dao.AuditDAO;
import dao.impl.AuditDAOImpl;
import dao.ExamAreaDAO;
import dao.impl.ExamAreaDAOImpl;
import dto.exam.ExamDeviceViewDTO;
import model.user.Audit;
import model.exam.ExamArea;
import model.exam.ExamDevice;
import service.ExamDeviceService;

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
        List<ExamDevice> devices = dao.search(keyword, "active".equalsIgnoreCase(status));
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
        return dao.countByStatus("active".equalsIgnoreCase(status));
    }

    @Override
    public SaveResult save(ExamDeviceViewDTO dev, Integer adminUserId) {
        if (dev.getDeviceName() == null || dev.getDeviceName().trim().isEmpty()) {
            return new SaveResult(false, "Vui lÃƒÂ²ng nhÃ¡ÂºÂ­p tÃƒÂªn mÃƒÂ¡y thi.", dev.getExamDeviceId());
        }
        if (dev.getDeviceType() == null || dev.getDeviceType().trim().isEmpty()) {
            return new SaveResult(false, "Vui lÃƒÂ²ng nhÃ¡ÂºÂ­p loÃ¡ÂºÂ¡i thiÃ¡ÂºÂ¿t bÃ¡Â»â€¹.", dev.getExamDeviceId());
        }
        if (dev.getStatus() == null || dev.getStatus().trim().isEmpty()) {
            return new SaveResult(false, "Vui lÃƒÂ²ng chÃ¡Â»Ân tÃƒÂ¬nh trÃ¡ÂºÂ¡ng mÃƒÂ¡y.", dev.getExamDeviceId());
        }
        if (dev.getExamAreaId() <= 0) {
            return new SaveResult(false, "Vui lÃƒÂ²ng chÃ¡Â»Ân khu vÃ¡Â»Â±c thi.", dev.getExamDeviceId());
        }

        ExamDevice model = new ExamDevice();
        model.setExamDeviceId(dev.getExamDeviceId());
        model.setDeviceName(dev.getDeviceName());
        model.setDeviceType(dev.getDeviceType());
        model.setActive("active".equalsIgnoreCase(dev.getStatus()));
        model.setExamAreaId(dev.getExamAreaId());

        boolean isEdit = dev.getExamDeviceId() > 0;
        if (isEdit) {
            boolean ok = dao.update(model);
            if (ok) {
                logAudit(adminUserId, "UPDATE", "ExamDevice", String.valueOf(dev.getExamDeviceId()), "GiÃƒÂ¡m khÃ¡ÂºÂ£o cÃ¡ÂºÂ­p nhÃ¡ÂºÂ­t mÃƒÂ¡y thi");
                return new SaveResult(true, "Ã„ÂÃƒÂ£ cÃ¡ÂºÂ­p nhÃ¡ÂºÂ­t mÃƒÂ¡y \"" + dev.getDeviceName() + "\".", dev.getExamDeviceId());
            } else {
                return new SaveResult(false, "CÃ¡ÂºÂ­p nhÃ¡ÂºÂ­t mÃƒÂ¡y thi thÃ¡ÂºÂ¥t bÃ¡ÂºÂ¡i.", dev.getExamDeviceId());
            }
        } else {
            int newId = dao.insert(model);
            boolean ok = newId > 0;
            if (ok) {
                logAudit(adminUserId, "INSERT", "ExamDevice", String.valueOf(newId), "GiÃƒÂ¡m khÃ¡ÂºÂ£o thÃƒÂªm mÃƒÂ¡y thi");
                return new SaveResult(true, "Ã„ÂÃƒÂ£ thÃƒÂªm mÃƒÂ¡y \"" + dev.getDeviceName() + "\".", newId);
            } else {
                return new SaveResult(false, "ThÃƒÂªm mÃƒÂ¡y thi thÃ¡ÂºÂ¥t bÃ¡ÂºÂ¡i.", 0);
            }
        }
    }

    @Override
    public DeleteResult delete(int id, Integer adminUserId) {
        ExamDevice dev = dao.findById(id);
        if (dev == null) {
            return new DeleteResult(false, "MÃƒÂ¡y thi khÃƒÂ´ng tÃ¡Â»â€œn tÃ¡ÂºÂ¡i.");
        }
        boolean ok = id > 0 && dao.delete(id);
        if (ok) {
            logAudit(adminUserId, "DELETE", "ExamDevice", String.valueOf(id), "GiÃƒÂ¡m khÃ¡ÂºÂ£o xÃƒÂ³a mÃƒÂ¡y thi");
            return new DeleteResult(true, "Ã„ÂÃƒÂ£ xÃƒÂ³a mÃƒÂ¡y thi.");
        } else {
            return new DeleteResult(false, "XÃƒÂ³a mÃƒÂ¡y thi thÃ¡ÂºÂ¥t bÃ¡ÂºÂ¡i.");
        }
    }

    private ExamDeviceViewDTO mapToDTO(ExamDevice model) {
        ExamDeviceViewDTO dto = new ExamDeviceViewDTO();
        dto.setExamDeviceId(model.getExamDeviceId());
        dto.setDeviceName(model.getDeviceName());
        dto.setDeviceType(model.getDeviceType());
        dto.setStatus(model.isActive() ? "active" : "inactive");
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
}



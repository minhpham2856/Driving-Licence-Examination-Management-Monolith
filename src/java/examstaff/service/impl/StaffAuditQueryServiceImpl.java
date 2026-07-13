package examstaff.service.impl;

import dao.AuditLogDAO;
import dao.impl.AuditLogDAOImpl;
import dto.staff.StaffProcedureKpiDTO;
import dto.user.AuditDTO;
import examstaff.service.StaffAuditQueryService;

import java.util.ArrayList;
import java.util.List;

public class StaffAuditQueryServiceImpl implements StaffAuditQueryService {

    private final AuditLogDAO auditLogDAO = new AuditLogDAOImpl();

    @Override
    public int countLogsByUserAndDate(int userId, String filterDate) {
        return auditLogDAO.getLogsCountByUserAndDate(userId, filterDate);
    }

    @Override
    public List<AuditDTO> listLogsByUserAndDatePaginated(int userId, String filterDate, int page, int pageSize) {
        try {
            return auditLogDAO.getLogsByUserAndDatePaginated(userId, filterDate, page, pageSize);
        } catch (Exception e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    @Override
    public List<AuditDTO> listLogsByUserAndDate(int userId, String filterDate) {
        try {
            if (filterDate != null && !filterDate.isBlank()) {
                return auditLogDAO.getLogsByUserAndDate(userId, filterDate);
            }
            return auditLogDAO.getLogsByUserAndDate(userId, null);
        } catch (Exception e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    @Override
    public StaffProcedureKpiDTO getStaffProcedureKpi(int userId, String filterDate) {
        return auditLogDAO.getStaffProcedureKpi(userId, filterDate);
    }
}

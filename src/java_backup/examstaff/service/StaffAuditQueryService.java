package examstaff.service;

import examstaff.dto.staff.StaffProcedureKpiDTO;
import examstaff.dto.user.AuditDTO;

import java.util.List;

public interface StaffAuditQueryService {

    int countLogsByUserAndDate(int userId, String filterDate);

    List<AuditDTO> listLogsByUserAndDatePaginated(int userId, String filterDate, int page, int pageSize);

    List<AuditDTO> listLogsByUserAndDate(int userId, String filterDate);

    StaffProcedureKpiDTO getStaffProcedureKpi(int userId, String filterDate);
}

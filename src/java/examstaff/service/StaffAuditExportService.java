package examstaff.service;

import examstaff.dto.user.AuditDTO;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

public interface StaffAuditExportService {

    void exportAuditLog(OutputStream out, List<AuditDTO> logs, int completedProcedures,
            double totalFees, String staffName, String filterDateLabel) throws IOException;
}

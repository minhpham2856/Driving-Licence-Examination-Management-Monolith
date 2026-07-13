package Services;

import DTOs.AuditDTO;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

public interface AuditLogExcelService {

    void writeAuditLogs(List<AuditDTO> logs, OutputStream output) throws IOException;
}

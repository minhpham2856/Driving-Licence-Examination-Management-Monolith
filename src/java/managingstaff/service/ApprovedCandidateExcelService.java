package managingstaff.service;

import managingstaff.dto.DossierDTO;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

public interface ApprovedCandidateExcelService {

    void writeApprovedCandidates(String licenceClass, List<DossierDTO> dossiers, OutputStream output)
            throws IOException;
}

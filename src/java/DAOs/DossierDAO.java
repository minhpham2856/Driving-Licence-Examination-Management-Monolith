package DAOs;

import DTOs.DossierDTO;
import java.util.List;

public interface DossierDAO {
    DossierDTO findByUserId(int userId);
    DossierDTO findByRegistrationId(int registrationId);
    List<DossierDTO> findAllRegistrants();
    List<DossierDTO> findSubmitted();
    int ensureRegistration(int profileId, String licenceClass, String source, String applicantType);
    boolean saveDocument(int profileId, String documentType, String documentUrl);
    boolean updateStatus(int registrationId, String status, String message, int actorUserId);
}

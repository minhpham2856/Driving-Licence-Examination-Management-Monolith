package DAOs;

import DTOs.DossierDTO;
import java.util.List;
import java.util.Map;

public interface DossierDAO {
    DossierDTO findByUserId(int userId);
    DossierDTO findByRegistrationId(int registrationId);
    List<DossierDTO> findAllRegistrants();
    List<DossierDTO> findRegistrantsByStatus(String statusFilter);
    List<DossierDTO> findRegistrantsByFilters(String statusFilter, String licenceClass);
    List<DossierDTO> findRegistrantPage(String statusFilter, String licenceClass,
            String keyword, String accountStatus, int page, int pageSize);
    int countRegistrants(String statusFilter, String licenceClass,
            String keyword, String accountStatus);
    Map<String, Integer> countRegistrantStatuses();
    Map<String, Integer> countApprovedByLicence();
    Map<String, Integer> countRegistrantsByLicence();
    int countLockedRegistrants();
    int countCompleteRegistrants();
    List<DossierDTO> findSubmitted();
    List<DossierDTO> findSubmittedPage(int page, int pageSize);
    int countSubmitted();
    int ensureRegistration(int profileId, String licenceClass, String source, String applicantType);
    boolean saveDocument(int profileId, String documentType, String documentUrl);
    boolean updateStatus(int registrationId, String status, String message, int actorUserId);
}

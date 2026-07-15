package managingstaff.dao;

import java.util.List;
import java.util.Map;
import managingstaff.dto.DossierDTO;
import managingstaff.dto.DossierDTO.DocumentView;

public interface DossierDAO {
    DossierDTO findByUserId(int userId);
    DossierDTO findByRegistrationId(int registrationId);
    List<DossierDTO> findRegistrantsByFilters(String statusFilter, String licenceClass);
    List<DossierDTO> findRegistrantPage(String statusFilter, String licenceClass,
            String fullName, String govIdNo, String email, String phoneNo,
            String accountStatus, int page, int pageSize);
    int countRegistrants(String statusFilter, String licenceClass,
            String fullName, String govIdNo, String email, String phoneNo,
            String accountStatus);
    Map<String, Integer> countRegistrantStatuses();
    Map<String, Integer> countApprovedByLicence();
    Map<String, Integer> countRegistrantsByLicence();
    int countLockedRegistrants();
    int countCompleteRegistrants();
    List<DossierDTO> findSubmittedPage(int page, int pageSize);
    int countSubmitted();
    int ensureRegistration(int profileId, String licenceClass, String source, String applicantType);
    boolean saveDocument(int profileId, String documentType, String documentUrl);
    DocumentView findDocumentById(int documentId);
    boolean updateStatus(int registrationId, String status, String message, int actorUserId);
    boolean setUserActive(int userId, boolean active);
}

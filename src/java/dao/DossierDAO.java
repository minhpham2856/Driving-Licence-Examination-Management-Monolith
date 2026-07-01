package dao;
public interface DossierDAO {
    int ensureRegistration(int profileId, String licenceClass, String source, String applicantType);
    boolean saveDocument(int profileId, String documentType, String documentUrl);
    boolean updateStatus(int registrationId, String status, String message, int actorUserId);
}

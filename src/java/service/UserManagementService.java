package service;
import dto.CreateUserResultDTO;
import java.util.Map;
public interface UserManagementService {
    CreateUserResultDTO createUser(
            String fullName, String cccd, String phone, String email,
            String dob, String sex, String address, String userType, String licenseClass);
    CreateUserResultDTO saveManagedDossier(
            int profileId, String licenseClass, String applicantType,
            Map<String, String> documentsByType, int actorUserId);
}

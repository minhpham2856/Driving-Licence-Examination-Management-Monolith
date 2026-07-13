package service;

import dto.CreateUserResultDTO;
import dto.ServiceResult;

import java.util.List;
import java.util.Map;

public interface UserService {

    List<Map<String, Object>> searchAccounts(String keyword, String roleFilter, String statusFilter);

    int countByRoleKey(String roleKey);

    ServiceResult<CreateUserResultDTO> createUser(String fullName, String cccd, String phone, String email,
            String dob, String sex, String address, String userType, String licenceClass);

    ServiceResult<Void> saveManagedDossier(int profileId, String licenceClass, String applicantType,
            Map<String, String> documents, int actorUserId);
}

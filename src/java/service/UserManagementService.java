package service;

import dto.user.CreateUserResultDTO;

public interface UserManagementService {

    CreateUserResultDTO createUser(
            String fullName, String cccd, String phone, String email,
            String dob, String gender, String address, String userType, String licenseClass);
}

package service;

import dto.registration.RegisterResultDTO;

public interface UserManagementService {

    CreateUserResult createUser(
            String fullName, String cccd, String phone, String email,
            String dob, String gender, String address, String userType, String licenseClass);

    public static class CreateUserResult {
        public final boolean success;
        public final String message;
        public final String username;
        public final String password;
        
        public CreateUserResult(boolean success, String message, String username, String password) {
            this.success = success;
            this.message = message;
            this.username = username;
            this.password = password;
        }
    }
}

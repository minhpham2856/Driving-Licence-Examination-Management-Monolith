package service;


import dto.registration.RegisterResultDTO;

import model.user.User;

public interface AuthService {

    /**
     * Registers new registrant.
     */
    RegisterResultDTO register(String govIdNo, String fullName, String phoneNo, String dateOfBirth,
            String address, String email, boolean gender);

    /**
     * Validates credentials
     *
     * @return User if credentials match, else null
     */
    User login(String identifier, String password);

    /**
     * Handles forgot password requests by generating a temporary password and updating the DB.
     *
     * @return Status or error msg.
     */
    String forgotPassword(String email);

    /**
     * Changes password
     */
    ChangePasswordResult changePassword(int userId, String currentPwd, String newPwd, String confirmPwd);

    public static class ChangePasswordResult {
        public final boolean success;
        public final String message;
        public ChangePasswordResult(boolean success, String message) {
            this.success = success;
            this.message = message;
        }
    }
}

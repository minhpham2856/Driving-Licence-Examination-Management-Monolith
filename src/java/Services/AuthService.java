package Services;

import Models.RegisterResult;
import Models.User;

public interface AuthService {

    /**
     * Registers new registrant.
     */
    RegisterResult register(String govIdNo, String fullName, String phoneNo, String dateOfBirth,
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
}

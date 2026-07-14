package auth.util;

import org.mindrot.jbcrypt.BCrypt;

public final class PasswordUtil {

    // hash a plain password
    public static String hash(String password) {
        if (password == null) {
            return null;
        }

        return BCrypt.hashpw(password, BCrypt.gensalt());
    }

    // verify a password against its hash
    public static boolean matches(String rawPassword, String hashedPassword) {
        if (rawPassword == null || hashedPassword == null) {
            return false;
        }

        return BCrypt.checkpw(rawPassword, hashedPassword);
    }

}

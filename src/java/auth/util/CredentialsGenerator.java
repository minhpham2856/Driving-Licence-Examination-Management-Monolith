package auth.util;

import java.text.Normalizer;
import java.util.concurrent.ThreadLocalRandom;

public final class UsernameGenerator {

    public static String generateFromFullName(String fullName) {
        String[] parts = fullName.trim().split("\\s+");
        if (parts.length == 0 || parts[0].isEmpty()) {
            return "user" + randomDigits(6);
        }

        String givenName = normalize(parts[parts.length - 1]).toLowerCase();

        StringBuilder initials = new StringBuilder();
        for (int i = 0; i < parts.length - 1; i++) {
            String part = normalize(parts[i]);
            if (!part.isEmpty()) {
                initials.append(Character.toLowerCase(part.charAt(0)));
            }
        }

        return givenName + initials + randomDigits(6);
    }

    public static String randomPassword(int length) {
        String chars = "ABCDEFGHJKLMNPQRSTUVWXYZabcdefghjkmnpqrstuvwxyz1234567890";
        StringBuilder password = new StringBuilder(length);
        ThreadLocalRandom random = ThreadLocalRandom.current();
        for (int i = 0; i < length; i++) {
            password.append(chars.charAt(random.nextInt(chars.length())));
        }
        return password.toString();
    }

    private static String randomDigits(int count) {
        int min = (int) Math.pow(10, count - 1);
        int max = (int) Math.pow(10, count) - 1;
        return String.valueOf(ThreadLocalRandom.current().nextInt(min, max + 1));
    }

    private static String normalize(String input) {
        String result = Normalizer.normalize(input, Normalizer.Form.NFD);
        result = result.replaceAll("\\p{M}", "");
        result = result.replace('đ', 'd');
        result = result.replace('Đ', 'D');
        result = result.replaceAll("[^a-zA-Z]", "");
        return result;
    }
}

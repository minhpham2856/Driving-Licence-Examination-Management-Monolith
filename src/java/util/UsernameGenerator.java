package util;

import java.text.Normalizer;
import java.util.concurrent.ThreadLocalRandom;

public final class UsernameGenerator {

    private UsernameGenerator() {
    }

    /**
     * e.g. "Nguyễn Văn Bình" -> "binhnv738274" (given name + other initials + 6 digits)
     */
    public static String generateFromFullName(String fullName) {
        String[] parts = fullName.trim().split("\\s+");
        if (parts.length == 0 || parts[0].isEmpty()) {
            return "user" + randomDigits(6);
        }

        String givenName = removeAccents(parts[parts.length - 1]).toLowerCase();
        StringBuilder initials = new StringBuilder();
        for (int i = 0; i < parts.length - 1; i++) {
            String part = removeAccents(parts[i]);
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

    private static String removeAccents(String input) {
        return Normalizer.normalize(input, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .replace('đ', 'd')
                .replace('Đ', 'D')
                .replaceAll("[^a-zA-Z]", "");
    }
}

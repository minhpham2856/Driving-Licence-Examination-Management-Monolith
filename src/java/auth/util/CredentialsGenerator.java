package auth.util;

import java.text.Normalizer;
import java.util.concurrent.ThreadLocalRandom;

public final class CredentialsGenerator {

    public static String generateUsername(String fullName) {
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
        int size = Math.max(length, 8);
        String upper = "ABCDEFGHJKLMNPQRSTUVWXYZ";
        String lower = "abcdefghjkmnpqrstuvwxyz";
        String digits = "23456789";
        String special = "!@#$%&*";
        String all = upper + lower + digits + special;
        ThreadLocalRandom random = ThreadLocalRandom.current();
        char[] password = new char[size];
        // Bắt buộc có hoa, thường, số, ký tự đặc biệt
        password[0] = upper.charAt(random.nextInt(upper.length()));
        password[1] = lower.charAt(random.nextInt(lower.length()));
        password[2] = digits.charAt(random.nextInt(digits.length()));
        password[3] = special.charAt(random.nextInt(special.length()));
        for (int i = 4; i < size; i++) {
            password[i] = all.charAt(random.nextInt(all.length()));
        }
        // Xáo trộn để không cố định vị trí
        for (int i = size - 1; i > 0; i--) {
            int j = random.nextInt(i + 1);
            char tmp = password[i];
            password[i] = password[j];
            password[j] = tmp;
        }
        return new String(password);
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

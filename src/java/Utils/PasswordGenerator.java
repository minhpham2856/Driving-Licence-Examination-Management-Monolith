package Utils;

import java.security.SecureRandom;

public final class PasswordGenerator {

    private PasswordGenerator() {}

    private static final String UPPER = "ABCDEFGHJKLMNPQRSTUVWXYZ";
    private static final String LOWER = "abcdefghijkmnpqrstuvwxyz";
    private static final String DIGIT = "23456789";
    private static final String SPECIAL = "@#$%*";
    private static final SecureRandom RANDOM = new SecureRandom();

    public static String generate() { return generate(10); }

    public static String generate(int length) {
        if (length < 8) length = 8;
        String all = UPPER + LOWER + DIGIT + SPECIAL;
        StringBuilder sb = new StringBuilder(length);
        // Đảm bảo có đủ 4 nhóm ký tự
        sb.append(UPPER.charAt(RANDOM.nextInt(UPPER.length())));
        sb.append(LOWER.charAt(RANDOM.nextInt(LOWER.length())));
        sb.append(DIGIT.charAt(RANDOM.nextInt(DIGIT.length())));
        sb.append(SPECIAL.charAt(RANDOM.nextInt(SPECIAL.length())));
        for (int i = 4; i < length; i++) sb.append(all.charAt(RANDOM.nextInt(all.length())));
        // Xáo trộn để 4 ký tự bắt buộc không bị dồn ở đầu chuỗi
        char[] a = sb.toString().toCharArray();
        for (int i = a.length - 1; i > 0; i--) {
            int j = RANDOM.nextInt(i + 1);
            char t = a[i]; a[i] = a[j]; a[j] = t;
        }
        return new String(a);
    }
}
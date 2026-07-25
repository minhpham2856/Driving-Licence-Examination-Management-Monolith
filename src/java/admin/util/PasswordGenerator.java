package admin.util;

import java.security.SecureRandom;

/** Sinh mật khẩu tạm ngẫu nhiên (thỏa Validator.password). Bỏ ký tự dễ nhầm. */
public final class PasswordGenerator {
    private PasswordGenerator() {}
    private static final String UPPER = "ABCDEFGHJKLMNPQRSTUVWXYZ";
    private static final String LOWER = "abcdefghijkmnpqrstuvwxyz";
    private static final String DIGIT = "23456789";
    private static final String SPECIAL = "@#$%*";
    private static final SecureRandom RANDOM = new SecureRandom();

    private static final String DIGITS_ALL = "0123456789";

    /** Sinh mật khẩu tạm gồm toàn chữ số (dùng gửi qua email). Mặc định 6 số. */
    public static String generateNumeric(int length) {
        if (length < 1) length = 6;
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) sb.append(DIGITS_ALL.charAt(RANDOM.nextInt(DIGITS_ALL.length())));
        return sb.toString();
    }

    public static String generate() { return generate(10); }
    public static String generate(int length) {
        if (length < 8) length = 8;
        String all = UPPER + LOWER + DIGIT + SPECIAL;
        StringBuilder sb = new StringBuilder(length);
        sb.append(UPPER.charAt(RANDOM.nextInt(UPPER.length())));
        sb.append(LOWER.charAt(RANDOM.nextInt(LOWER.length())));
        sb.append(DIGIT.charAt(RANDOM.nextInt(DIGIT.length())));
        sb.append(SPECIAL.charAt(RANDOM.nextInt(SPECIAL.length())));
        for (int i = 4; i < length; i++) sb.append(all.charAt(RANDOM.nextInt(all.length())));
        char[] a = sb.toString().toCharArray();
        for (int i = a.length - 1; i > 0; i--) {
            int j = RANDOM.nextInt(i + 1);
            char t = a[i]; a[i] = a[j]; a[j] = t;
        }
        return new String(a);
    }
}

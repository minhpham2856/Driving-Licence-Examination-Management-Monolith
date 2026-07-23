package shared.service.impl;

import shared.ConfigManager;
import shared.dto.ExamAccessOtpDTO;
import shared.service.ExamAccessOtpService;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

public class ExamAccessOtpServiceImpl implements ExamAccessOtpService {

    private static final long WINDOW_SECONDS = 120L;
    private static final int CODE_MODULUS = 1_000_000;

    @Override
    public ExamAccessOtpDTO getCurrent(int examId, int examSectionId, int examAreaId) {
        validateScope(examId, examSectionId, examAreaId);
        long now = System.currentTimeMillis() / 1000L;
        long bucket = now / WINDOW_SECONDS;
        String code = generateCode(examId, examSectionId, examAreaId, bucket);
        return new ExamAccessOtpDTO(code, (bucket + 1L) * WINDOW_SECONDS);
    }

    @Override
    public boolean verify(int examId, int examSectionId, int examAreaId, String submittedCode) {
        if (submittedCode == null || !submittedCode.matches("\\d{6}")) {
            return false;
        }
        String current = getCurrent(examId, examSectionId, examAreaId).getCode();
        return MessageDigest.isEqual(
                current.getBytes(StandardCharsets.US_ASCII),
                submittedCode.getBytes(StandardCharsets.US_ASCII));
    }

    private String generateCode(int examId, int examSectionId, int examAreaId, long bucket) {
        String secret = ConfigManager.get("EXAM_ACCESS_OTP_SECRET");
        if (secret == null || secret.length() < 32) {
            throw new IllegalStateException("EXAM_ACCESS_OTP_SECRET must contain at least 32 characters.");
        }
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            ByteBuffer payload = ByteBuffer.allocate(Integer.BYTES * 3 + Long.BYTES);
            payload.putInt(examId);
            payload.putInt(examSectionId);
            payload.putInt(examAreaId);
            payload.putLong(bucket);
            byte[] digest = mac.doFinal(payload.array());
            int offset = digest[digest.length - 1] & 0x0f;
            int binary = ((digest[offset] & 0x7f) << 24)
                    | ((digest[offset + 1] & 0xff) << 16)
                    | ((digest[offset + 2] & 0xff) << 8)
                    | (digest[offset + 3] & 0xff);
            return String.format("%06d", binary % CODE_MODULUS);
        } catch (Exception ex) {
            throw new IllegalStateException("Cannot generate exam access OTP.", ex);
        }
    }

    private void validateScope(int examId, int examSectionId, int examAreaId) {
        if (examId <= 0 || examSectionId <= 0 || examAreaId <= 0) {
            throw new IllegalArgumentException("OTP scope is incomplete.");
        }
    }
}

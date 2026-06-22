package Utils;

import Enums.CandidateStatus;

public class CandidateUtils {

    public static String candidateStatusLabel(String status) {
        if (status == null || status.trim().isEmpty()) {
            return "-";
        }
        String s = status.trim();
        if (CandidateStatus.PENDING.getValue().equalsIgnoreCase(s)) {
            return "Chờ thi";
        } else if (CandidateStatus.TESTING.getValue().equalsIgnoreCase(s)) {
            return "Đang thi";
        } else if (CandidateStatus.AWAITING_SIGNATURE.getValue().equalsIgnoreCase(s)) {
            return "Chờ ký tên";
        } else if (CandidateStatus.DONE.getValue().equalsIgnoreCase(s)) {
            return "Hoàn thành";
        }
        return status;
    }

    public static boolean isCandidateAwaitingSignature(String status) {
        if (status == null || status.trim().isEmpty()) {
            return false;
        }
        return CandidateStatus.AWAITING_SIGNATURE.getValue().equalsIgnoreCase(status.trim());
    }

    public static boolean isCandidateDone(String status) {
        if (status == null || status.trim().isEmpty()) {
            return false;
        }
        return CandidateStatus.DONE.getValue().equalsIgnoreCase(status.trim());
    }

    public static int parseCandidateNo(String candidateNumber) {
        if (candidateNumber == null || candidateNumber.isBlank()) {
            return 0;
        }
        try {
            int dashIndex = candidateNumber.lastIndexOf('-');
            if (dashIndex >= 0 && dashIndex < candidateNumber.length() - 1) {
                return Integer.parseInt(candidateNumber.substring(dashIndex + 1).trim());
            }
            return Integer.parseInt(candidateNumber.trim());
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    public static String buildCandidateNumber(String licenseCode, int candidateNo) {
        String safeCode = (licenseCode == null || licenseCode.isBlank()) ? "UNKNOWN" : licenseCode.trim();
        return safeCode + "-" + formatSbd(candidateNo);
    }

    public static String formatSbd(int candidateNo) {
        return String.format("%03d", candidateNo);
    }

    public static boolean isPresentStatus(String registrationStatus) {
        if (registrationStatus == null || registrationStatus.trim().isEmpty()) {
            return false;
        }
        String s = registrationStatus.trim();
        return "DaDongTien".equalsIgnoreCase(s)
                || "DatLyThuyet".equalsIgnoreCase(s)
                || "DatThucHanh".equalsIgnoreCase(s)
                || "KhongDatLyThuyet".equalsIgnoreCase(s)
                || "KhongDatThucHanh".equalsIgnoreCase(s)
                || "VangLyThuyet".equalsIgnoreCase(s)
                || "VangThucHanh".equalsIgnoreCase(s);
    }
}

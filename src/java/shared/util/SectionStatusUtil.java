package shared.util;

import shared.enums.CandidateStatus;

public final class SectionStatusUtil {

    private SectionStatusUtil() {
    }

    // Maps legacy DB values (Pending) to examiner-facing Vietnamese statuses.
    public static String normalize(String status) {
        if (status == null || status.isBlank()) {
            return CandidateStatus.NOT_STARTED.getValue();
        }
        String trimmed = status.trim();
        if ("Pending".equalsIgnoreCase(trimmed)) {
            return CandidateStatus.NOT_STARTED.getValue();
        }
        CandidateStatus known = CandidateStatus.fromValue(trimmed);
        if (known != null) {
            return known.getValue();
        }
        return trimmed;
    }
}

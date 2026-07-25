package candidate.util;

/**
 * Cấu hình số câu / ngưỡng đạt / thời gian thi lý thuyết theo hạng GPLX.
 * Hệ thống hiện chỉ hỗ trợ A1, A và B1 — cả ba đều 25 câu, đạt ≥ 21,
 * và mỗi đề luôn có 1 câu điểm liệt ({@code IsCritical}); sai câu này là trượt
 * bất kể tổng điểm.
 */
public final class TheoryExamRules {

    public static final class Rule {
        public final int numQuestions;
        public final int passThreshold;
        public final int durationMinutes;
        public Rule(int n, int pass, int minutes) {
            this.numQuestions = n;
            this.passThreshold = pass;
            this.durationMinutes = minutes;
        }
    }

    /** Quy tắc chung cho A1 / A / B1. */
    private static final Rule STANDARD = new Rule(25, 21, 19);

    private TheoryExamRules() {}

    public static Rule resolve(String licenceClass) {
        if (licenceClass == null) {
            return STANDARD;
        }
        String c = licenceClass.trim().toUpperCase().replaceAll("\\s+", "");
        if (c.startsWith("A1") || c.startsWith("A") || c.startsWith("B1")) {
            return STANDARD;
        }
        // Hạng ngoài phạm vi hỗ trợ — vẫn áp ngưỡng chuẩn để không nới lỏng điều kiện đạt.
        return STANDARD;
    }
}

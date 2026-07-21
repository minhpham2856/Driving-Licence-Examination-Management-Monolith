package candidate.util;

/**
 * Cấu hình số câu / ngưỡng đạt / thời gian thi lý thuyết theo hạng GPLX (chuẩn bộ 600 câu VN).
 * Mỗi đề luôn có 1 câu điểm liệt (IsCritical); sai câu này là trượt bất kể tổng điểm.
 * Chỉnh số ở đây nếu quy định thay đổi.
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

    private static final Rule DEFAULT = new Rule(35, 32, 22); // như hạng B

    private TheoryExamRules() {}

    public static Rule resolve(String licenceClass) {
        if (licenceClass == null) return DEFAULT;
        String c = licenceClass.trim().toUpperCase().replaceAll("\\s+", "");
        if (c.startsWith("A1")) return new Rule(25, 21, 19);
        if (c.startsWith("A"))  return new Rule(25, 21, 19);   // A2, A3, A4
        if (c.startsWith("B1")) return new Rule(30, 27, 20);
        if (c.startsWith("B"))  return new Rule(35, 32, 22);   // B2, B
        if (c.startsWith("C"))  return new Rule(40, 36, 24);
        if (c.startsWith("D"))  return new Rule(45, 41, 26);
        if (c.startsWith("E"))  return new Rule(45, 41, 26);
        if (c.startsWith("F"))  return new Rule(45, 41, 26);
        return DEFAULT;
    }
}

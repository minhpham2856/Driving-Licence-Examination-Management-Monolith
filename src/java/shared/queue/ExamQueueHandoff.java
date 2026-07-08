package shared.queue;

// Payload when a candidate is passed from procedure desk into an exam room queue.
public final class ExamQueueHandoff {

    private final int examId;
    private final int candidateId;
    private final int sbd;
    private final int enrollmentId;
    private final boolean takeTheory;
    private final boolean takeLayout;

    public ExamQueueHandoff(int examId, int candidateId, int sbd, int enrollmentId,
            boolean takeTheory, boolean takeLayout) {
        this.examId = examId;
        this.candidateId = candidateId;
        this.sbd = sbd;
        this.enrollmentId = enrollmentId;
        this.takeTheory = takeTheory;
        this.takeLayout = takeLayout;
    }

    public int getExamId() {
        return examId;
    }

    public int getCandidateId() {
        return candidateId;
    }

    public int getSbd() {
        return sbd;
    }

    public int getEnrollmentId() {
        return enrollmentId;
    }

    public boolean isTakeTheory() {
        return takeTheory;
    }

    public boolean isTakeLayout() {
        return takeLayout;
    }
}

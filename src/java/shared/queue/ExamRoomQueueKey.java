package shared.queue;

import shared.enums.SectionType;
import java.util.Objects;

// Identity of one waiting/testing queue: one exam + section + room/ground.
public final class ExamRoomQueueKey {

    private final int examId;
    private final SectionType sectionType;
    private final int examAreaId;

    public ExamRoomQueueKey(int examId, SectionType sectionType, int examAreaId) {
        this.examId = examId;
        this.sectionType = sectionType == null ? SectionType.THEORY : sectionType;
        this.examAreaId = examAreaId;
    }

    public int getExamId() {
        return examId;
    }

    public SectionType getSectionType() {
        return sectionType;
    }

    public int getExamAreaId() {
        return examAreaId;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof ExamRoomQueueKey)) {
            return false;
        }
        ExamRoomQueueKey key = (ExamRoomQueueKey) other;
        return examId == key.examId
                && examAreaId == key.examAreaId
                && sectionType == key.sectionType;
    }

    @Override
    public int hashCode() {
        return Objects.hash(examId, sectionType, examAreaId);
    }
}

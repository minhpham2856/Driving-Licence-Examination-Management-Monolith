package controller.examiner;

import enums.ExamSection;
import util.ExamQueue;
import util.ExamQueue.Lane;
import java.util.List;

public final class ExaminerScoreEntryQueue {

    private ExaminerScoreEntryQueue() {
    }

    public static void syncQueue(ExamSection examSection, List<Integer> eligibleSbds) {
        ExamQueue.sync(ExamQueue.laneFor(examSection), eligibleSbds);
    }

    public static List<Integer> getQueue(ExamSection examSection) {
        return ExamQueue.asList(ExamQueue.laneFor(examSection));
    }

    public static Integer getActiveSbd(ExamSection examSection) {
        return ExamQueue.getActiveSbd(ExamQueue.laneFor(examSection));
    }

    public static void setActiveSbd(ExamSection examSection, Integer sbd) {
        ExamQueue.setActiveSbd(ExamQueue.laneFor(examSection), sbd);
    }

    public static Integer getCalledSbd(ExamSection examSection) {
        return ExamQueue.getCalledSbd(ExamQueue.laneFor(examSection));
    }

    public static void setCalledSbd(ExamSection examSection, Integer sbd) {
        ExamQueue.setCalledSbd(ExamQueue.laneFor(examSection), sbd);
    }

    public static Integer firstInQueue(ExamSection examSection) {
        return ExamQueue.peekFirst(ExamQueue.laneFor(examSection));
    }

    public static Integer nextInQueueAfter(ExamSection examSection, int sbd) {
        return ExamQueue.nextAfter(ExamQueue.laneFor(examSection), sbd);
    }

    public static Integer moveToBottom(ExamSection examSection, int sbd) {
        return ExamQueue.moveToBottom(ExamQueue.laneFor(examSection), sbd);
    }

    public static Lane laneFor(ExamSection examSection) {
        return ExamQueue.laneFor(examSection);
    }
}

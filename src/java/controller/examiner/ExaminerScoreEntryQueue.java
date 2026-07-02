package controller.examiner;
import util.ExamQueue;
import util.ExamQueue.Lane;
import java.util.List;
public final class ExaminerScoreEntryQueue {
    private ExaminerScoreEntryQueue() {
    }
    public static void syncQueue(boolean isTheory, String sectionName, List<Integer> eligibleSbds) {
        ExamQueue.sync(ExamQueue.resolveLane(isTheory, sectionName), eligibleSbds);
    }
    public static List<Integer> getQueue(boolean isTheory, String sectionName) {
        return ExamQueue.asList(ExamQueue.resolveLane(isTheory, sectionName));
    }
    public static Integer getActiveSbd(boolean isTheory, String sectionName) {
        return ExamQueue.getActiveSbd(ExamQueue.resolveLane(isTheory, sectionName));
    }
    public static void setActiveSbd(boolean isTheory, String sectionName, Integer sbd) {
        ExamQueue.setActiveSbd(ExamQueue.resolveLane(isTheory, sectionName), sbd);
    }
    public static Integer getCalledSbd(boolean isTheory, String sectionName) {
        return ExamQueue.getCalledSbd(ExamQueue.resolveLane(isTheory, sectionName));
    }
    public static void setCalledSbd(boolean isTheory, String sectionName, Integer sbd) {
        ExamQueue.setCalledSbd(ExamQueue.resolveLane(isTheory, sectionName), sbd);
    }
    public static Integer firstInQueue(boolean isTheory, String sectionName) {
        return ExamQueue.peekFirst(ExamQueue.resolveLane(isTheory, sectionName));
    }
    public static Integer nextInQueueAfter(boolean isTheory, String sectionName, int sbd) {
        return ExamQueue.nextAfter(ExamQueue.resolveLane(isTheory, sectionName), sbd);
    }
    public static Integer moveToBottom(boolean isTheory, String sectionName, int sbd) {
        return ExamQueue.moveToBottom(ExamQueue.resolveLane(isTheory, sectionName), sbd);
    }
    public static Lane resolveLane(boolean isTheory, String sectionName) {
        return ExamQueue.resolveLane(isTheory, sectionName);
    }
}

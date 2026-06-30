package controller.examiner;

import enums.SectionType;
import util.ExamQueue;
import util.ExamQueue.Lane;

import java.util.List;

public final class ExaminerScoreEntryQueue {

    private ExaminerScoreEntryQueue() {
    }

    public static void syncQueue(SectionType sectionType, String sectionName, List<Integer> eligibleSbds) {
        ExamQueue.sync(ExamQueue.resolveLane(sectionType, sectionName), eligibleSbds);
    }

    public static List<Integer> getQueue(SectionType sectionType, String sectionName) {
        return ExamQueue.asList(ExamQueue.resolveLane(sectionType, sectionName));
    }

    public static Integer getActiveSbd(SectionType sectionType, String sectionName) {
        return ExamQueue.getActiveSbd(ExamQueue.resolveLane(sectionType, sectionName));
    }

    public static void setActiveSbd(SectionType sectionType, String sectionName, Integer sbd) {
        ExamQueue.setActiveSbd(ExamQueue.resolveLane(sectionType, sectionName), sbd);
    }

    public static Integer getCalledSbd(SectionType sectionType, String sectionName) {
        return ExamQueue.getCalledSbd(ExamQueue.resolveLane(sectionType, sectionName));
    }

    public static void setCalledSbd(SectionType sectionType, String sectionName, Integer sbd) {
        ExamQueue.setCalledSbd(ExamQueue.resolveLane(sectionType, sectionName), sbd);
    }

    public static Integer firstInQueue(SectionType sectionType, String sectionName) {
        return ExamQueue.peekFirst(ExamQueue.resolveLane(sectionType, sectionName));
    }

    public static Integer nextInQueueAfter(SectionType sectionType, String sectionName, int sbd) {
        return ExamQueue.nextAfter(ExamQueue.resolveLane(sectionType, sectionName), sbd);
    }

    public static Integer moveToBottom(SectionType sectionType, String sectionName, int sbd) {
        return ExamQueue.moveToBottom(ExamQueue.resolveLane(sectionType, sectionName), sbd);
    }

    public static Lane resolveLane(SectionType sectionType, String sectionName) {
        return ExamQueue.resolveLane(sectionType, sectionName);
    }
}

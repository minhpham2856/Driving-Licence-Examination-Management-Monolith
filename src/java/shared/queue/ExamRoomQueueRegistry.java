package shared.queue;

import shared.enums.SectionType;
import shared.model.ExamArea;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

// In-memory multi-queue registry keyed by (examId, sectionType, examAreaId).
public final class ExamRoomQueueRegistry {

    private static final Map<ExamRoomQueueKey, ExamRoomQueueState> QUEUES = new ConcurrentHashMap<>();
    private static final Map<ExamRoomQueueKey, Object> LOCKS = new ConcurrentHashMap<>();

    private ExamRoomQueueRegistry() {
    }

    public static void ensureQueues(int examId, SectionType sectionType, List<ExamArea> areas) {
        if (examId <= 0 || sectionType == null || areas == null) {
            return;
        }
        for (ExamArea area : areas) {
            if (area == null || area.getExamAreaId() <= 0) {
                continue;
            }
            ExamRoomQueueKey key = new ExamRoomQueueKey(examId, sectionType, area.getExamAreaId());
            ExamRoomQueueState state = QUEUES.computeIfAbsent(key, ignored -> new ExamRoomQueueState());
            Integer capacity = area.getCapacity();
            state.setMaxCapacity(capacity != null && capacity > 0 ? capacity : 1);
            LOCKS.computeIfAbsent(key, ignored -> new Object());
        }
    }

    public static int load(int examId, int examAreaId, SectionType sectionType) {
        ExamRoomQueueState state = stateOf(examId, examAreaId, sectionType);
        if (state == null) {
            return 0;
        }
        synchronized (lockOf(examId, examAreaId, sectionType)) {
            return state.loadCount();
        }
    }

    public static boolean enqueue(int examId, int examAreaId, SectionType sectionType, int sbd) {
        if (examId <= 0 || examAreaId <= 0 || sbd <= 0 || sectionType == null) {
            return false;
        }
        ExamRoomQueueState state = stateOf(examId, examAreaId, sectionType);
        if (state == null) {
            return false;
        }
        synchronized (lockOf(examId, examAreaId, sectionType)) {
            return state.enqueue(sbd);
        }
    }

    public static Integer tryPromote(int examId, int examAreaId, SectionType sectionType) {
        ExamRoomQueueState state = stateOf(examId, examAreaId, sectionType);
        if (state == null) {
            return null;
        }
        synchronized (lockOf(examId, examAreaId, sectionType)) {
            return state.tryPromote();
        }
    }

    public static Integer completeTesting(int examId, int examAreaId, SectionType sectionType, int sbd) {
        ExamRoomQueueState state = stateOf(examId, examAreaId, sectionType);
        if (state == null) {
            return null;
        }
        synchronized (lockOf(examId, examAreaId, sectionType)) {
            return state.completeTesting(sbd);
        }
    }

    public static void removeCandidate(int examId, int sbd) {
        if (examId <= 0 || sbd <= 0) {
            return;
        }
        List<ExamRoomQueueKey> keys = keysForExam(examId);
        for (ExamRoomQueueKey key : keys) {
            ExamRoomQueueState state = QUEUES.get(key);
            if (state == null) {
                continue;
            }
            synchronized (lockOf(key.getExamId(), key.getExamAreaId(), key.getSectionType())) {
                state.removeCandidate(sbd);
            }
        }
    }

    // TBD: return candidate to examstaff procedure queue.
    public static void passBackCandidate(int examId, int sbd) {
        removeCandidate(examId, sbd);
    }

    public static List<Integer> displayOrder(int examId, int examAreaId, SectionType sectionType) {
        ExamRoomQueueState state = stateOf(examId, examAreaId, sectionType);
        if (state == null) {
            return new ArrayList<>();
        }
        synchronized (lockOf(examId, examAreaId, sectionType)) {
            return state.displayOrder();
        }
    }

    public static boolean isTesting(int examId, int examAreaId, SectionType sectionType, int sbd) {
        ExamRoomQueueState state = stateOf(examId, examAreaId, sectionType);
        if (state == null) {
            return false;
        }
        synchronized (lockOf(examId, examAreaId, sectionType)) {
            return state.isTesting(sbd);
        }
    }

    private static ExamRoomQueueState stateOf(int examId, int examAreaId, SectionType sectionType) {
        if (examId <= 0 || examAreaId <= 0 || sectionType == null) {
            return null;
        }
        return QUEUES.get(new ExamRoomQueueKey(examId, sectionType, examAreaId));
    }

    private static Object lockOf(int examId, int examAreaId, SectionType sectionType) {
        ExamRoomQueueKey key = new ExamRoomQueueKey(examId, sectionType, examAreaId);
        return LOCKS.computeIfAbsent(key, ignored -> new Object());
    }

    private static List<ExamRoomQueueKey> keysForExam(int examId) {
        List<ExamRoomQueueKey> keys = new ArrayList<>();
        for (ExamRoomQueueKey key : QUEUES.keySet()) {
            if (key.getExamId() == examId) {
                keys.add(key);
            }
        }
        return keys;
    }
}

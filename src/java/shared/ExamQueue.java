package shared;

import shared.enums.SectionType;
import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicReference;

public class ExamQueue {

    private static Queue<Integer> queueTheory = new ConcurrentLinkedQueue<>();
    private static Queue<Integer> queueLayout = new ConcurrentLinkedQueue<>();

    public enum Lane {
        LY_THUYET, THUC_HANH_TRONG_HINH
    }
    private static final Object LOCK_THEORY = new Object();
    private static final Object LOCK_LAYOUT = new Object();
    private static final Map<Lane, AtomicReference<Integer>> ACTIVE = new EnumMap<>(Lane.class);
    private static final Map<Lane, AtomicReference<Integer>> CALLED = new EnumMap<>(Lane.class);

    static {
        for (Lane lane : Lane.values()) {
            ACTIVE.put(lane, new AtomicReference<>());
            CALLED.put(lane, new AtomicReference<>());
        }
    }

    private ExamQueue() {
    }

    public static Queue<Integer> queueFor(Lane lane) {
        if (lane == Lane.THUC_HANH_TRONG_HINH) {
            return queueLayout;
        }
        return queueTheory;
    }

    public static Lane laneFor(SectionType section) {
        if (section == null || section == SectionType.THEORY) {
            return Lane.LY_THUYET;
        }
        return Lane.THUC_HANH_TRONG_HINH;
    }

    public static List<Integer> asList(Lane lane) {
        synchronized (lockFor(lane)) {
            return new ArrayList<>(queueFor(lane));
        }
    }

    public static void sync(Lane lane, Collection<Integer> eligibleSbds) {
        if (lane == null || eligibleSbds == null) {
            return;
        }
        synchronized (lockFor(lane)) {
            Queue<Integer> queue = queueFor(lane);
            Set<Integer> seen = new LinkedHashSet<>(queue);
            for (Integer sbd : eligibleSbds) {
                if (sbd == null || sbd <= 0 || seen.contains(sbd)) {
                    continue;
                }
                queue.offer(sbd);
                seen.add(sbd);
            }
            clearStateIfMissing(lane, seen);
        }
    }

    public static boolean offer(Lane lane, int sbd) {
        if (lane == null || sbd <= 0) {
            return false;
        }
        synchronized (lockFor(lane)) {
            if (queueFor(lane).contains(sbd)) {
                return false;
            }
            return queueFor(lane).offer(sbd);
        }
    }

    public static boolean remove(Lane lane, int sbd) {
        if (lane == null || sbd <= 0) {
            return false;
        }
        synchronized (lockFor(lane)) {
            boolean removed = queueFor(lane).remove(sbd);
            if (removed) {
                clearStateIfMissing(lane, new LinkedHashSet<>(queueFor(lane)));
            }
            return removed;
        }
    }

    public static void handoff(Lane from, Lane to, int sbd) {
        if (from == null || to == null || sbd <= 0) {
            return;
        }
        remove(from, sbd);
        offer(to, sbd);
    }

    public static Integer peekFirst(Lane lane) {
        synchronized (lockFor(lane)) {
            return queueFor(lane).peek();
        }
    }

    public static Integer nextAfter(Lane lane, int sbd) {
        List<Integer> queue = asList(lane);
        if (queue.isEmpty()) {
            return null;
        }
        if (sbd <= 0) {
            return queue.get(0);
        }
        int idx = queue.indexOf(sbd);
        if (idx < 0) {
            return queue.get(0);
        }
        if (idx + 1 < queue.size()) {
            return queue.get(idx + 1);
        }
        return null;
    }

    public static Integer moveToBottom(Lane lane, int sbd) {
        if (lane == null || sbd <= 0) {
            return peekFirst(lane);
        }
        synchronized (lockFor(lane)) {
            Queue<Integer> queue = queueFor(lane);
            if (!queue.remove(sbd)) {
                return queue.peek();
            }
            queue.offer(sbd);
            CALLED.get(lane).set(null);
            Integer active = ACTIVE.get(lane).get();
            if (active != null && active == sbd) {
                ACTIVE.get(lane).set(null);
            }
            return queue.peek();
        }
    }

    public static Integer getActiveSbd(Lane lane) {
        if (lane == null) {
            return null;
        }
        return ACTIVE.get(lane).get();
    }

    public static void setActiveSbd(Lane lane, Integer sbd) {
        if (lane == null) {
            return;
        }
        if (sbd == null || sbd <= 0) {
            ACTIVE.get(lane).set(null);
            return;
        }
        ACTIVE.get(lane).set(sbd);
    }

    public static Integer getCalledSbd(Lane lane) {
        if (lane == null) {
            return null;
        }
        return CALLED.get(lane).get();
    }

    public static void setCalledSbd(Lane lane, Integer sbd) {
        if (lane == null) {
            return;
        }
        if (sbd == null || sbd <= 0) {
            CALLED.get(lane).set(null);
            return;
        }
        CALLED.get(lane).set(sbd);
    }

    private static Object lockFor(Lane lane) {
        if (lane == Lane.THUC_HANH_TRONG_HINH) {
            return LOCK_LAYOUT;
        }
        return LOCK_THEORY;
    }

    private static void clearStateIfMissing(Lane lane, Set<Integer> allowed) {
        Integer active = ACTIVE.get(lane).get();
        if (active != null && !allowed.contains(active)) {
            ACTIVE.get(lane).set(null);
        }
        Integer called = CALLED.get(lane).get();
        if (called != null && !allowed.contains(called)) {
            CALLED.get(lane).set(null);
        }
    }
}

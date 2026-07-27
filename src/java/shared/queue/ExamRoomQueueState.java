package shared.queue;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;

// One room/yard queue: waiting FIFO + active testing set capped by maxCapacity.
final class ExamRoomQueueState {

    private final Queue<Integer> waiting = new ConcurrentLinkedQueue<>();
    private final Set<Integer> testing = new LinkedHashSet<>();
    private int maxCapacity = 1;

    void setMaxCapacity(int capacity) {
        if (capacity > 0) {
            maxCapacity = capacity;
        }
    }

    int getMaxCapacity() {
        return maxCapacity;
    }

    int loadCount() {
        return waiting.size() + testing.size();
    }

    boolean enqueue(int sbd) {
        if (sbd <= 0) {
            return false;
        }
        if (waiting.contains(sbd) || testing.contains(sbd)) {
            return false;
        }
        return waiting.offer(sbd);
    }

    Integer tryPromote() {
        if (testing.size() >= maxCapacity || waiting.isEmpty()) {
            return null;
        }
        Integer sbd = waiting.poll();
        if (sbd == null || sbd <= 0) {
            return null;
        }
        testing.add(sbd);
        return sbd;
    }

    Integer completeTesting(int sbd) {
        if (sbd <= 0) {
            return null;
        }
        testing.remove(sbd);
        waiting.remove(sbd);
        return tryPromote();
    }

    boolean removeCandidate(int sbd) {
        if (sbd <= 0) {
            return false;
        }
        boolean removed = waiting.remove(sbd) || testing.remove(sbd);
        return removed;
    }

    boolean moveToTail(int sbd) {
        if (sbd <= 0 || testing.contains(sbd) || !waiting.remove(sbd)) {
            return false;
        }
        return waiting.offer(sbd);
    }

    List<Integer> displayOrder() {
        List<Integer> order = new ArrayList<>();
        order.addAll(testing);
        order.addAll(waiting);
        return order;
    }

    List<Integer> waitingOrder() {
        return new ArrayList<>(waiting);
    }

    boolean isTesting(int sbd) {
        return testing.contains(sbd);
    }
}

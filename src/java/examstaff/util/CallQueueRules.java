package examstaff.util;

import dto.exam.ExamRegistrationDTO;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/** Quy tắc hàng đợi gọi thí sinh — không phụ thuộc HTTP. */
public final class CallQueueRules {

    private CallQueueRules() {
    }

    public static List<ExamRegistrationDTO> filterActiveCallQueue(List<ExamRegistrationDTO> queue) {
        List<ExamRegistrationDTO> active = new ArrayList<>();
        if (queue == null) {
            return active;
        }
        for (ExamRegistrationDTO c : queue) {
            if (c != null && !c.isAbsent() && !c.isSuspended()) {
                active.add(c);
            }
        }
        return active;
    }

    public static boolean isCallablePending(ExamRegistrationDTO c) {
        if (c == null || c.isAbsent() || c.isSuspended()) {
            return false;
        }
        return !c.isProcedureComplete();
    }

    public static ExamRegistrationDTO findBySbd(List<ExamRegistrationDTO> queue, String sbd) {
        if (queue == null || sbd == null || sbd.isBlank()) {
            return null;
        }
        String trimmed = sbd.trim();
        for (ExamRegistrationDTO c : queue) {
            if (c != null && trimmed.equals(c.getSbd())) {
                return c;
            }
        }
        return null;
    }

    public static String resolveNextCallingSbd(List<ExamRegistrationDTO> fullQueue, String afterSbd) {
        if (fullQueue == null || fullQueue.isEmpty()) {
            return null;
        }
        List<ExamRegistrationDTO> pending = new ArrayList<>();
        for (ExamRegistrationDTO c : fullQueue) {
            if (isCallablePending(c)) {
                pending.add(c);
            }
        }
        if (pending.isEmpty()) {
            return null;
        }
        if (afterSbd == null || afterSbd.isBlank()) {
            return pending.get(0).getSbd();
        }
        boolean seen = false;
        for (ExamRegistrationDTO c : pending) {
            if (seen) {
                return c.getSbd();
            }
            if (afterSbd.equals(c.getSbd())) {
                seen = true;
            }
        }
        return pending.get(0).getSbd();
    }

    public static List<ExamRegistrationDTO> applyQueueOrder(List<ExamRegistrationDTO> queue,
            List<String> orderSbds) {
        if (queue == null || queue.isEmpty() || orderSbds == null || orderSbds.isEmpty()) {
            return queue;
        }
        java.util.Map<String, ExamRegistrationDTO> bySbd = new java.util.LinkedHashMap<>();
        for (ExamRegistrationDTO c : queue) {
            if (c != null && c.getSbd() != null) {
                bySbd.put(c.getSbd(), c);
            }
        }
        List<ExamRegistrationDTO> reordered = new ArrayList<>();
        for (String sbd : orderSbds) {
            ExamRegistrationDTO c = bySbd.remove(sbd);
            if (c != null) {
                reordered.add(c);
            }
        }
        reordered.addAll(bySbd.values());
        return reordered;
    }

    public static List<ExamRegistrationDTO> listWaitingTop(List<ExamRegistrationDTO> queue, int limit) {
        List<ExamRegistrationDTO> result = new ArrayList<>();
        if (queue == null || limit <= 0) {
            return result;
        }
        for (ExamRegistrationDTO c : queue) {
            if (isCallablePending(c)) {
                result.add(c);
                if (result.size() >= limit) {
                    break;
                }
            }
        }
        return result;
    }

    public static List<String> extractSbdOrder(List<ExamRegistrationDTO> queue) {
        List<String> order = new ArrayList<>();
        if (queue == null) {
            return order;
        }
        for (ExamRegistrationDTO c : queue) {
            if (c != null && c.getSbd() != null && !c.getSbd().isBlank()) {
                order.add(c.getSbd());
            }
        }
        return order;
    }

    public static List<ExamRegistrationDTO> listSuspendedInSession(List<ExamRegistrationDTO> queue) {
        List<ExamRegistrationDTO> suspended = new ArrayList<>();
        if (queue == null) {
            return suspended;
        }
        for (ExamRegistrationDTO c : queue) {
            if (c != null && c.isSuspended()) {
                suspended.add(c);
            }
        }
        return suspended;
    }

    public static List<ExamRegistrationDTO> listProcedureDoneNewestFirst(List<ExamRegistrationDTO> queue) {
        List<ExamRegistrationDTO> done = new ArrayList<>();
        if (queue == null) {
            return done;
        }
        for (ExamRegistrationDTO c : queue) {
            if (c != null && c.isPaymentCompleted() && c.isValidCapturedPhoto()) {
                done.add(c);
            }
        }
        done.sort(Comparator.comparing(ExamRegistrationDTO::getSbd).reversed());
        return done;
    }
}

package examstaff.util;

import examstaff.dto.exam.ExamRegistrationDTO;

import java.util.ArrayList;
import java.util.List;

/** Quy tắc hàng đợi gọi thí sinh - không phụ thuộc HTTP. */
public final class CallQueueRules {

    private CallQueueRules() {
    }

    /**
     * Thí sinh còn trong hàng đợi gọi được: chưa vắng, chưa đình chỉ, chưa xong thủ tục.
     *
     * @param c thí sinh đăng ký
     * @return true nếu còn gọi được
     */
    public static boolean isCallablePending(ExamRegistrationDTO c) {
        if (c == null || c.isAbsent() || c.isSuspended()) {
            return false;
        }
        return !c.isProcedureComplete();
    }

    /**
     * Tìm thí sinh theo SBD trong hàng đợi.
     *
     * @param queue hàng đợi
     * @param sbd   số báo danh
     * @return DTO khớp hoặc null
     */
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

    /**
     * Lấy SBD pending tiếp theo sau {@code afterSbd} (không wrap về đầu nếu đã hết hàng).
     *
     * @param fullQueue hàng đợi đầy đủ
     * @param afterSbd  SBD mốc (null = lấy người đầu pending)
     * @return SBD kế tiếp hoặc null
     */
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
        // Da gap afterSbd nhung khong con ai sau -> het hang (khong wrap ve chinh nguoi dang ban/dang goi).
        if (seen) {
            return null;
        }
        // afterSbd khong con trong pending (da xong/vang) -> lay dau hang con lai.
        return pending.get(0).getSbd();
    }

    /**
     * Sắp lại hàng đợi theo danh sách thứ tự SBD (SBD không có trong order nằm cuối).
     *
     * @param queue     hàng đợi gốc
     * @param orderSbds thứ tự SBD mong muốn
     * @return hàng đợi đã reorder
     */
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

    /**
     * Lấy tối đa {@code limit} thí sinh pending đầu hàng (dùng Public Call waiting list).
     *
     * @param queue hàng đợi
     * @param limit số phần tử tối đa
     * @return danh sách chờ (có thể rỗng)
     */
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

    /**
     * Trích thứ tự SBD hiện tại của hàng đợi để lưu lên CallBoard.
     *
     * @param queue hàng đợi
     * @return danh sách SBD theo thứ tự
     */
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

    /**
     * Lọc thí sinh bị đình chỉ trong hàng đợi kỳ thi.
     *
     * @param queue hàng đợi
     * @return danh sách suspended
     */
    public static List<ExamRegistrationDTO> listSuspendedInExam(List<ExamRegistrationDTO> queue) {
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
}

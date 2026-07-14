package examstaff.controller.staff.exam.binder;

import examstaff.controller.staff.exam.http.ExamStaffSessionKeys;
import examstaff.dto.ExamSummaryDTO;
import examstaff.dto.exam.ExamRegistrationDTO;
import examstaff.dto.CandidateQueueSnapshotDTO;
import examstaff.dto.ExamStaffPickerViewDTO;
import examstaff.dto.ProcedureFeeResultDTO;
import examstaff.enums.ExamStatus;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.sql.Timestamp;
import examstaff.util.ExamScheduleRules;
import examstaff.util.LicenseClassRules;

/**
 * Chỉ bind request/session attributes. Không tạo *Impl và không chứa nghiệp vụ.
 * Dữ liệu đã được service/helper chuẩn bị sẵn.
 */
public final class ExamStaffPageBinder {

    private ExamStaffPageBinder() {
    }

    /** Chuẩn hóa mã hạng GPLX theo quy tắc managed; fallback trim+upper. */
    private static String normalizeLicenseForExamstaff(String raw) {
        if (raw == null) {
            return null;
        }
        String normalized = LicenseClassRules.normalizeManaged(raw);
        if (normalized != null && !normalized.isBlank()) {
            return normalized;
        }
        return raw.trim().toUpperCase(Locale.ROOT);
    }

    private static void normalizeExam(ExamSummaryDTO s) {
        if (s == null) return;
        s.setLicenseCode(normalizeLicenseForExamstaff(s.getLicenseCode()));
    }

    private static void normalizeCandidate(ExamRegistrationDTO c) {
        if (c == null) return;
        c.setLicenseCode(normalizeLicenseForExamstaff(c.getLicenseCode()));
    }

    /**
     * Bind picker kỳ thi: {@code examOptions}, {@code allExams}, {@code currentExam},
     * {@code selectedExamId}, {@code pickerCommittedExamId} (+ shift context).
     */
    public static void bindPickerView(HttpServletRequest request, ExamStaffPickerViewDTO picker) {
        if (request == null || picker == null) {
            return;
        }
        if (picker.getExamOptions() != null) {
            for (ExamSummaryDTO s : picker.getExamOptions()) {
                normalizeExam(s);
            }
        }
        if (picker.getAllExams() != null) {
            for (ExamSummaryDTO s : picker.getAllExams()) {
                normalizeExam(s);
            }
        }
        normalizeExam(picker.getCurrentExam());
        bindExamShiftContext(request, picker.getCurrentExam());
        request.setAttribute("examOptions", picker.getExamOptions());
        request.setAttribute("allExams", picker.getAllExams());
        request.setAttribute("currentExam", picker.getCurrentExam());
        int selectedExamId = picker.getExamId() > 0
                ? picker.getExamId()
                : (picker.getSelectedExamId() != null ? picker.getSelectedExamId() : 0);
        request.setAttribute(ExamStaffSessionKeys.SELECTED_EXAM_ID, selectedExamId > 0 ? selectedExamId : null);
        Integer committedExamId = picker.getPickerCommittedExamId();
        if (committedExamId != null) {
            request.setAttribute("pickerCommittedExamId", committedExamId);
        }
    }

    /** Publish queue từ snapshot DTO (ủy quyền overload đầy đủ). */
    public static void publishQueue(HttpServletRequest request, HttpSession session,
            CandidateQueueSnapshotDTO snapshot) {
        if (snapshot == null) {
            return;
        }
        publishQueue(request, session, snapshot.getFullQueue(), snapshot.getActiveQueue(),
                snapshot.getProcedureDone(), snapshot.getResolvedExamId(), snapshot.getResolvedExamId(),
                null);
    }

    /** Publish queue không kèm {@code currentExam}. */
    public static void publishQueue(HttpServletRequest request, HttpSession session,
            List<ExamRegistrationDTO> candidateQueue, List<ExamRegistrationDTO> active,
            List<ExamRegistrationDTO> done, int examId, int fallbackExamId) {
        publishQueue(request, session, candidateQueue, active, done, examId, fallbackExamId, null);
    }

    /**
     * Set queue đầy đủ/active/procedure-done lên request+session cùng
     * loaded/selected examId và {@code currentExam}.
     */
    public static void publishQueue(HttpServletRequest request, HttpSession session,
            List<ExamRegistrationDTO> candidateQueue, List<ExamRegistrationDTO> active,
            List<ExamRegistrationDTO> done, int examId, int fallbackExamId, ExamSummaryDTO currentExam) {
        if (candidateQueue == null) {
            candidateQueue = List.of();
        }
        if (active == null) {
            active = List.of();
        }
        if (done == null) {
            done = List.of();
        }

        normalizeExam(currentExam);
        for (ExamRegistrationDTO c : candidateQueue) normalizeCandidate(c);
        for (ExamRegistrationDTO c : active) normalizeCandidate(c);
        for (ExamRegistrationDTO c : done) normalizeCandidate(c);

        if (session != null) {
            session.setAttribute(ExamStaffSessionKeys.CANDIDATE_QUEUE, candidateQueue);
            session.setAttribute(ExamStaffSessionKeys.ACTIVE_CALL_QUEUE, active);
            session.setAttribute(ExamStaffSessionKeys.PROCEDURE_DONE_CANDIDATES, done);
            session.setAttribute(ExamStaffSessionKeys.LOADED_EXAM_ID, examId);
            int resolvedExamId = examId > 0 ? examId : fallbackExamId;
            if (resolvedExamId > 0) {
                session.setAttribute(ExamStaffSessionKeys.SELECTED_EXAM_ID, resolvedExamId);
                session.setAttribute(ExamStaffSessionKeys.LAST_LOADED_EXAM_ID, resolvedExamId);
            }
        }
        if (request != null) {
            request.setAttribute(ExamStaffSessionKeys.CANDIDATE_QUEUE, candidateQueue);
            request.setAttribute(ExamStaffSessionKeys.ACTIVE_CALL_QUEUE, active);
            request.setAttribute(ExamStaffSessionKeys.PROCEDURE_DONE_CANDIDATES, done);
            request.setAttribute(ExamStaffSessionKeys.LOADED_EXAM_ID, examId);
            int resolvedExamId = examId > 0 ? examId : fallbackExamId;
            request.setAttribute(ExamStaffSessionKeys.SELECTED_EXAM_ID,
                    resolvedExamId > 0 ? resolvedExamId : null);
            if (currentExam != null) {
                request.setAttribute("currentExam", currentExam);
                bindExamShiftContext(request, currentExam);
            }
        }
    }

    /**
     * Bind cờ UI lịch thi + đồng bộ cờ ca từ Status DB.
     * Phần ghi session tách rõ trong {@link #syncShiftFlagsFromExamStatus}.
     */
    public static void bindExamShiftContext(HttpServletRequest request, ExamSummaryDTO sessionExam) {
        if (request == null || sessionExam == null) {
            return;
        }
        Timestamp scheduledStart = sessionExam.getScheduledStartAt() != null
                ? sessionExam.getScheduledStartAt()
                : sessionExam.getCreatedAt();
        request.setAttribute("examCanStartNow", ExamScheduleRules.canStartNow(scheduledStart));
        if (scheduledStart != null) {
            request.setAttribute("examScheduledStartLabel",
                    ExamScheduleRules.formatScheduledStart(scheduledStart));
        }
        boolean mutationsLocked = ExamStatus.isLockedForStaffMutation(sessionExam.getStatus());
        request.setAttribute("examMutationsLocked", mutationsLocked);
        HttpSession httpSession = request.getSession(false);
        syncShiftFlagsFromExamStatus(httpSession, sessionExam);
    }

    /**
     * Ghi cờ ca ({@code shiftPaused}/{@code shiftEnded}) theo trạng thái kỳ trên DB.
     * Tách khỏi bind UI để hội đồng thấy: bind request ≠ ghi ca.
     */
    public static void syncShiftFlagsFromExamStatus(HttpSession httpSession, ExamSummaryDTO sessionExam) {
        if (httpSession == null || sessionExam == null) {
            return;
        }
        String status = sessionExam.getStatus();
        if (ExamStatus.isPaused(status)) {
            httpSession.setAttribute(ExamStaffSessionKeys.SHIFT_PAUSED, ExamStaffSessionKeys.FLAG_TRUE);
            httpSession.removeAttribute(ExamStaffSessionKeys.SHIFT_ENDED);
        } else if (ExamStatus.isInProgress(status)) {
            httpSession.removeAttribute(ExamStaffSessionKeys.SHIFT_PAUSED);
        } else if (ExamStatus.normalize(status) == ExamStatus.HOAN_TAT) {
            httpSession.setAttribute(ExamStaffSessionKeys.SHIFT_ENDED, ExamStaffSessionKeys.FLAG_TRUE);
            httpSession.removeAttribute(ExamStaffSessionKeys.SHIFT_PAUSED);
        }
    }

    /**
     * Bind thuộc tính trang gọi: {@code callingCandidate}, {@code suspendedCount},
     * {@code currentExam}/{@code selectedExamId} (+ shift context).
     */
    public static void bindCandidateCallPage(HttpServletRequest request, int examId,
            ExamRegistrationDTO callingCandidate, int selectedExamId, int suspendedCount,
            ExamSummaryDTO currentExam) {
        if (request == null) {
            return;
        }
        normalizeCandidate(callingCandidate);
        normalizeExam(currentExam);
        request.setAttribute("callingCandidate", callingCandidate);
        request.setAttribute("suspendedCount", suspendedCount);
        if (currentExam != null) {
            request.setAttribute("currentExam", currentExam);
            bindExamShiftContext(request, currentExam);
        }
        request.setAttribute(ExamStaffSessionKeys.SELECTED_EXAM_ID, examId > 0 ? examId : selectedExamId);
    }

    /** Bind phí thủ tục: {@code feeLines}, {@code feeTotal}, {@code feesFromPayment}. */
    public static void bindProcedureFees(HttpServletRequest request, ProcedureFeeResultDTO fees) {
        if (request == null || fees == null) {
            return;
        }
        request.setAttribute("feeLines", fees.getFeeLines());
        request.setAttribute("feeTotal", fees.getFeeTotal());
        request.setAttribute("feesFromPayment", fees.isFeesFromPayment());
    }

    /** Đọc {@code selectedExamId} &gt; 0 từ session; null nếu không có. */
    public static Integer readSelectedExamId(HttpSession session) {
        if (session == null) {
            return null;
        }
        Integer selected = (Integer) session.getAttribute(ExamStaffSessionKeys.SELECTED_EXAM_ID);
        if (selected != null && selected > 0) {
            return selected;
        }
        return null;
    }

    /** Đọc {@code callQueueOrderExamId} &gt; 0 từ session. */
    public static Integer readCallQueueOrderExamId(HttpSession session) {
        if (session == null) {
            return null;
        }
        Integer examId = (Integer) session.getAttribute(ExamStaffSessionKeys.CALL_QUEUE_ORDER_EXAM_ID);
        if (examId != null && examId > 0) {
            return examId;
        }
        return null;
    }

    /** Đọc {@code examStaffLoadedExamId} &gt; 0 từ session. */
    public static Integer readLoadedExamId(HttpSession session) {
        if (session == null) {
            return null;
        }
        Integer loaded = (Integer) session.getAttribute(ExamStaffSessionKeys.LOADED_EXAM_ID);
        if (loaded != null && loaded > 0) {
            return loaded;
        }
        return null;
    }

    /** Ghi {@code selectedExamId} (ưu tiên examId, fallback fallbackExamId). */
    public static void persistExamSelection(HttpSession session, int fallbackExamId, int examId) {
        if (session == null) {
            return;
        }
        int resolvedExamId = examId > 0 ? examId : fallbackExamId;
        if (resolvedExamId > 0) {
            session.setAttribute(ExamStaffSessionKeys.SELECTED_EXAM_ID, resolvedExamId);
        }
    }

    /** Xóa cache queue thí sinh trên session (candidate/active/done/order…). */
    public static void clearCandidateCache(HttpSession session) {
        if (session == null) {
            return;
        }
        session.removeAttribute(ExamStaffSessionKeys.CANDIDATE_QUEUE);
        session.removeAttribute(ExamStaffSessionKeys.ACTIVE_CALL_QUEUE);
        session.removeAttribute(ExamStaffSessionKeys.PROCEDURE_DONE_CANDIDATES);
        session.removeAttribute(ExamStaffSessionKeys.LOADED_EXAM_ID);
        session.removeAttribute(ExamStaffSessionKeys.LAST_LOADED_EXAM_ID);
        session.removeAttribute(ExamStaffSessionKeys.CALL_QUEUE_ORDER);
    }

    /**
     * Khi đổi kỳ: xóa procedure/calling/shift state + cache queue, rồi persist selection mới.
     */
    public static void clearProcedureStateOnExamChange(HttpSession session, int newExamId, int newFallbackExamId) {
        if (session == null) {
            return;
        }
        session.removeAttribute(ExamStaffSessionKeys.CALLING_SBD);
        session.removeAttribute(ExamStaffSessionKeys.LAST_SELECTED_SBD);
        session.removeAttribute(ExamStaffSessionKeys.PROCEDURE_STEP);
        session.removeAttribute(ExamStaffSessionKeys.PROCEDURE_JUST_PAID);
        session.removeAttribute(ExamStaffSessionKeys.PROCEDURE_JUST_PAID_SBD);
        session.removeAttribute(ExamStaffSessionKeys.SHIFT_ENDED);
        session.removeAttribute(ExamStaffSessionKeys.SHIFT_PAUSED);
        session.removeAttribute(ExamStaffSessionKeys.PERMANENT_ABSENTS);
        clearCandidateCache(session);
        if (newExamId > 0 || newFallbackExamId > 0) {
            persistExamSelection(session, newFallbackExamId, newExamId);
        }
    }

    /**
     * Đồng bộ thứ tự gọi số: set {@code callQueueOrder} (list SBD) và {@code callQueueOrderExamId}.
     */
    public static void syncCallQueueOrder(HttpSession session, int examId, List<ExamRegistrationDTO> queue) {
        if (session == null || queue == null) {
            return;
        }
        List<String> order = new ArrayList<>();
        for (ExamRegistrationDTO c : queue) {
            if (c != null && c.getSbd() != null) {
                order.add(c.getSbd());
            }
        }
        session.setAttribute(ExamStaffSessionKeys.CALL_QUEUE_ORDER, order);
        session.setAttribute(ExamStaffSessionKeys.CALL_QUEUE_ORDER_EXAM_ID, examId);
    }
}

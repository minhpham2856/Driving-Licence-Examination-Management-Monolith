package examstaff.controller.staff.exam.binder;

import examstaff.dto.ExamSummaryDTO;
import examstaff.dto.exam.ExamRegistrationDTO;
import examstaff.dto.CandidateQueueSnapshotDTO;
import examstaff.dto.ExamStaffPickerViewDTO;
import examstaff.dto.ProcedureFeeResultDTO;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.sql.Timestamp;
import examstaff.util.ExamScheduleRules;
import examstaff.util.LicenseClassRules;

/**
 * Chi bind request/session attributes. Khong tao *Impl va khong chua nghiep vu.
 * Du lieu da duoc service/helper chuan bi san.
 */
public final class ExamStaffPageBinder {

    private ExamStaffPageBinder() {
    }

    private static String normalizeLicenseForExamstaff(String raw) {
        if (raw == null) {
            return null;
        }
        String normalized = LicenseClassRules.normalizeManaged(raw);
        if (normalized != null && !normalized.isBlank()) {
            return normalized;
        }
        // Fallback: giữ nguyên giá trị (trim + upper) khi không thuộc tập đã biết.
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
        request.setAttribute("selectedExamId", selectedExamId > 0 ? selectedExamId : null);
        Integer committedExamId = picker.getPickerCommittedExamId();
        if (committedExamId != null) {
            request.setAttribute("pickerCommittedExamId", committedExamId);
        }
    }

    public static void publishQueue(HttpServletRequest request, HttpSession session,
            CandidateQueueSnapshotDTO snapshot) {
        if (snapshot == null) {
            return;
        }
        publishQueue(request, session, snapshot.getFullQueue(), snapshot.getActiveQueue(),
                snapshot.getProcedureDone(), snapshot.getResolvedExamId(), snapshot.getResolvedExamId(),
                null);
    }

    public static void publishQueue(HttpServletRequest request, HttpSession session,
            List<ExamRegistrationDTO> qList, List<ExamRegistrationDTO> active,
            List<ExamRegistrationDTO> done, int examId, int fallbackExamId) {
        publishQueue(request, session, qList, active, done, examId, fallbackExamId, null);
    }

    public static void publishQueue(HttpServletRequest request, HttpSession session,
            List<ExamRegistrationDTO> qList, List<ExamRegistrationDTO> active,
            List<ExamRegistrationDTO> done, int examId, int fallbackExamId, ExamSummaryDTO currentExam) {
        if (qList == null) {
            qList = List.of();
        }
        if (active == null) {
            active = List.of();
        }
        if (done == null) {
            done = List.of();
        }

        normalizeExam(currentExam);
        for (ExamRegistrationDTO c : qList) normalizeCandidate(c);
        for (ExamRegistrationDTO c : active) normalizeCandidate(c);
        for (ExamRegistrationDTO c : done) normalizeCandidate(c);

        if (session != null) {
            session.setAttribute("candidateQueue", qList);
            session.setAttribute("activeCallQueue", active);
            session.setAttribute("procedureDoneCandidates", done);
            session.setAttribute("examStaffLoadedExamId", examId);
            int resolvedExamId = examId > 0 ? examId : fallbackExamId;
            if (resolvedExamId > 0) {
                session.setAttribute("selectedExamId", resolvedExamId);
                session.setAttribute("lastLoadedExamId", resolvedExamId);
            }
        }
        if (request != null) {
            request.setAttribute("candidateQueue", qList);
            request.setAttribute("activeCallQueue", active);
            request.setAttribute("procedureDoneCandidates", done);
            request.setAttribute("examStaffLoadedExamId", examId);
            int resolvedExamId = examId > 0 ? examId : fallbackExamId;
            request.setAttribute("selectedExamId", resolvedExamId > 0 ? resolvedExamId : null);
            if (currentExam != null) {
                request.setAttribute("currentExam", currentExam);
                bindExamShiftContext(request, currentExam);
            }
        }
    }

    /** Bind cờ UI điều khiển bắt đầu/kết thúc kỳ (dựa trên StartTime trong DB). */
    public static void bindExamShiftContext(HttpServletRequest request, ExamSummaryDTO session) {
        if (request == null || session == null) {
            return;
        }
        Timestamp scheduledStart = session.getScheduledStartAt() != null
                ? session.getScheduledStartAt()
                : session.getCreatedAt();
        request.setAttribute("examCanStartNow", ExamScheduleRules.canStartNow(scheduledStart));
        if (scheduledStart != null) {
            request.setAttribute("examScheduledStartLabel",
                    ExamScheduleRules.formatScheduledStart(scheduledStart));
        }
    }

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
        request.setAttribute("selectedExamId", examId > 0 ? examId : selectedExamId);
    }

    public static void bindProcedureFees(HttpServletRequest request, ProcedureFeeResultDTO fees) {
        if (request == null || fees == null) {
            return;
        }
        request.setAttribute("feeLines", fees.getFeeLines());
        request.setAttribute("feeTotal", fees.getFeeTotal());
        request.setAttribute("feesFromPayment", fees.isFeesFromPayment());
    }

    public static Integer readSelectedExamId(HttpSession session) {
        if (session == null) {
            return null;
        }
        Integer selected = (Integer) session.getAttribute("selectedExamId");
        if (selected != null && selected > 0) {
            return selected;
        }
        return null;
    }

    public static Integer readCallQueueOrderExamId(HttpSession session) {
        if (session == null) {
            return null;
        }
        Integer examId = (Integer) session.getAttribute("callQueueOrderExamId");
        if (examId != null && examId > 0) {
            return examId;
        }
        return null;
    }

    public static Integer readLoadedExamId(HttpSession session) {
        if (session == null) {
            return null;
        }
        Integer loaded = (Integer) session.getAttribute("examStaffLoadedExamId");
        if (loaded != null && loaded > 0) {
            return loaded;
        }
        return null;
    }

    public static void persistExamSelection(HttpSession session, int fallbackExamId, int examId) {
        if (session == null) {
            return;
        }
        int resolvedExamId = examId > 0 ? examId : fallbackExamId;
        if (resolvedExamId > 0) {
            session.setAttribute("selectedExamId", resolvedExamId);
        }
    }

    public static void clearCandidateCache(HttpSession session) {
        if (session == null) {
            return;
        }
        session.removeAttribute("candidateQueue");
        session.removeAttribute("activeCallQueue");
        session.removeAttribute("procedureDoneCandidates");
        session.removeAttribute("examStaffLoadedExamId");
        session.removeAttribute("lastLoadedExamId");
        session.removeAttribute("callQueueOrder");
    }

    public static void clearProcedureStateOnExamChange(HttpSession session, int newExamId, int newFallbackExamId) {
        if (session == null) {
            return;
        }
        session.removeAttribute("callingSbd");
        session.removeAttribute("lastSelectedSbd");
        session.removeAttribute("procedureStep");
        session.removeAttribute("procedureJustPaid");
        session.removeAttribute("procedureJustPaidSbd");
        session.removeAttribute("shiftEnded");
        session.removeAttribute("shiftPaused");
        session.removeAttribute("permanentAbsents");
        clearCandidateCache(session);
        if (newExamId > 0 || newFallbackExamId > 0) {
            persistExamSelection(session, newFallbackExamId, newExamId);
        }
    }

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
        session.setAttribute("callQueueOrder", order);
        session.setAttribute("callQueueOrderExamId", examId);
    }
}

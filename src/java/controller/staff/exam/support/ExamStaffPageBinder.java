package controller.staff.exam.support;

import dto.SessionDTO;
import dto.exam.ExamRegistrationDTO;
import dto.examstaff.CandidateQueueSnapshotDTO;
import dto.examstaff.ExamStaffPickerViewDTO;
import dto.examstaff.ProcedureFeeResultDTO;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

import java.util.ArrayList;
import java.util.List;

/**
 * Chỉ bind request/session attributes. Không tạo *Impl và không chứa nghiệp vụ.
 * Dữ liệu đã được service/helper chuẩn bị sẵn.
 */
public final class ExamStaffPageBinder {

    private ExamStaffPageBinder() {
    }

    public static void bindPickerView(HttpServletRequest request, ExamStaffPickerViewDTO picker) {
        if (request == null || picker == null) {
            return;
        }
        request.setAttribute("examOptions", picker.getExamOptions());
        request.setAttribute("allSessions", picker.getAllSessions());
        request.setAttribute("currentSession", picker.getCurrentSession());
        request.setAttribute("selectedExamId", picker.getExamId());
        request.setAttribute("selectedSessionId", picker.getSelectedSessionId());
        if (picker.getPickerCommittedSessionId() != null) {
            request.setAttribute("pickerCommittedSessionId", picker.getPickerCommittedSessionId());
        }
        if (picker.getPickerCommittedExamId() != null) {
            request.setAttribute("pickerCommittedExamId", picker.getPickerCommittedExamId());
        }
    }

    public static void publishQueue(HttpServletRequest request, HttpSession session,
            CandidateQueueSnapshotDTO snapshot) {
        if (snapshot == null) {
            return;
        }
        publishQueue(request, session, snapshot.getFullQueue(), snapshot.getActiveQueue(),
                snapshot.getProcedureDone(), snapshot.getResolvedExamId(), snapshot.getResolvedSessionId(),
                null);
    }

    public static void publishQueue(HttpServletRequest request, HttpSession session,
            List<ExamRegistrationDTO> qList, List<ExamRegistrationDTO> active,
            List<ExamRegistrationDTO> done, int examId, int sessionId) {
        publishQueue(request, session, qList, active, done, examId, sessionId, null);
    }

    public static void publishQueue(HttpServletRequest request, HttpSession session,
            List<ExamRegistrationDTO> qList, List<ExamRegistrationDTO> active,
            List<ExamRegistrationDTO> done, int examId, int sessionId, SessionDTO currentSession) {
        if (qList == null) {
            qList = List.of();
        }
        if (active == null) {
            active = List.of();
        }
        if (done == null) {
            done = List.of();
        }

        if (session != null) {
            session.setAttribute("candidateQueue", qList);
            session.setAttribute("activeCallQueue", active);
            session.setAttribute("procedureDoneCandidates", done);
            session.setAttribute("examStaffLoadedExamId", examId);
            session.setAttribute("examStaffLoadedSessionId", sessionId);
            if (examId > 0) {
                session.setAttribute("selectedExamId", examId);
                session.setAttribute("lastLoadedExamId", examId);
            }
            if (sessionId > 0) {
                session.setAttribute("selectedSessionId", sessionId);
                session.setAttribute("lastLoadedSessionId", sessionId);
            }
        }
        if (request != null) {
            request.setAttribute("candidateQueue", qList);
            request.setAttribute("activeCallQueue", active);
            request.setAttribute("procedureDoneCandidates", done);
            request.setAttribute("examStaffLoadedExamId", examId);
            request.setAttribute("examStaffLoadedSessionId", sessionId);
            request.setAttribute("selectedExamId", examId);
            request.setAttribute("selectedSessionId", sessionId > 0 ? sessionId : null);
            if (currentSession != null) {
                request.setAttribute("currentSession", currentSession);
            }
        }
    }

    public static void bindCandidateCallPage(HttpServletRequest request, int examId,
            ExamRegistrationDTO callingCandidate, int sessionId, int suspendedCount,
            SessionDTO currentSession) {
        if (request == null) {
            return;
        }
        request.setAttribute("callingCandidate", callingCandidate);
        request.setAttribute("suspendedCount", suspendedCount);
        if (currentSession != null) {
            request.setAttribute("currentSession", currentSession);
        }
        request.setAttribute("selectedExamId", examId);
        request.setAttribute("selectedSessionId", sessionId > 0 ? sessionId : null);
    }

    public static void bindProcedureFees(HttpServletRequest request, ProcedureFeeResultDTO fees) {
        if (request == null || fees == null) {
            return;
        }
        request.setAttribute("feeLines", fees.getFeeLines());
        request.setAttribute("feeTotal", fees.getFeeTotal());
        request.setAttribute("feesFromPayment", fees.isFeesFromPayment());
    }

    public static void bindImportExam(HttpServletRequest request, SessionDTO currentSession, int examId) {
        if (request == null) {
            return;
        }
        if (currentSession != null) {
            request.setAttribute("currentSession", currentSession);
            if (currentSession.getLicenseCode() != null && !currentSession.getLicenseCode().isBlank()) {
                request.setAttribute("importExamLicense", currentSession.getLicenseCode());
            }
        }
        request.setAttribute("selectedExamId", examId);
    }

    public static void persistExamSelection(HttpSession session, int sessionId, int examId) {
        if (session == null) {
            return;
        }
        if (examId > 0) {
            session.setAttribute("selectedExamId", examId);
        }
        if (sessionId > 0) {
            session.setAttribute("selectedSessionId", sessionId);
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
        session.removeAttribute("examStaffLoadedSessionId");
        session.removeAttribute("lastLoadedExamId");
        session.removeAttribute("lastLoadedSessionId");
        session.removeAttribute("callQueueOrder");
    }

    public static void clearProcedureStateOnExamChange(HttpSession session, int newExamId, int newSessionId) {
        if (session == null) {
            return;
        }
        session.removeAttribute("callingSbd");
        session.removeAttribute("lastSelectedSbd");
        session.removeAttribute("procedureStep");
        session.removeAttribute("procedureJustPaid");
        session.removeAttribute("procedureJustPaidSbd");
        session.removeAttribute("shiftEnded");
        session.removeAttribute("permanentAbsents");
        clearCandidateCache(session);
        if (newExamId > 0 && newSessionId > 0) {
            persistExamSelection(session, newSessionId, newExamId);
        }
    }

    public static void syncCallQueueOrder(HttpSession session, int sessionId, List<ExamRegistrationDTO> queue) {
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
        session.setAttribute("callQueueOrderSessionId", sessionId);
    }
}

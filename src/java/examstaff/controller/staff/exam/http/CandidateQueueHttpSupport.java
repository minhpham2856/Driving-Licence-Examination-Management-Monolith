package examstaff.controller.staff.exam.http;

import examstaff.controller.staff.exam.adapter.ExamStaffSelectionFacade;
import examstaff.controller.staff.exam.binder.ExamStaffPageBinder;
import examstaff.dto.CandidateQueueSnapshotDTO;
import examstaff.dto.ExamStaffQueueRefreshInput;
import examstaff.dto.ExamSummaryDTO;
import examstaff.dto.exam.ExamRegistrationDTO;
import examstaff.service.CandidateQueueService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

import java.util.List;

/**
 * Helper Presentation: refresh/publish hàng đợi thí sinh — không nghiệp vụ.
 */
public final class CandidateQueueHttpSupport {

    private CandidateQueueHttpSupport() {
    }

    /**
     * Làm mới snapshot từ DB/service rồi publish vào session (và request nếu có).
     *
     * @return full queue sau refresh, hoặc list rỗng nếu session null
     */
    public static List<ExamRegistrationDTO> refreshAndPublish(
            HttpServletRequest request,
            HttpSession session,
            CandidateQueueService candidateQueueService,
            int examId,
            int queueExamId,
            String webRoot,
            List<ExamSummaryDTO> allExams) {
        if (session == null || candidateQueueService == null) {
            return List.of();
        }
        ExamStaffQueueRefreshInput input = new ExamStaffQueueRefreshInput();
        input.setExamId(queueExamId > 0 ? queueExamId : examId);
        input.setWebRoot(webRoot);
        input.setAllExams(allExams);
        input.setSelectedExamId(ExamStaffPageBinder.readSelectedExamId(session));
        @SuppressWarnings("unchecked")
        List<String> order = (List<String>) session.getAttribute(ExamStaffSessionKeys.CALL_QUEUE_ORDER);
        input.setCallQueueOrder(order);
        input.setCallQueueOrderExamId(ExamStaffPageBinder.readCallQueueOrderExamId(session));

        CandidateQueueSnapshotDTO snapshot = candidateQueueService.refreshQueue(input);
        ExamStaffPageBinder.publishQueue(request, session, snapshot);
        return snapshot.getFullQueue() != null ? snapshot.getFullQueue() : List.of();
    }

    /**
     * Publish full/active/procedure-done queue lên request + session từ danh sách đã có.
     */
    public static void publishLists(
            HttpServletRequest request,
            HttpSession session,
            CandidateQueueService candidateQueueService,
            ExamStaffSelectionFacade selectionFacade,
            List<ExamRegistrationDTO> qList,
            int examId) {
        if (candidateQueueService == null) {
            return;
        }
        CandidateQueueSnapshotDTO snapshot = candidateQueueService.buildSnapshot(qList, examId, examId);
        ExamSummaryDTO current = null;
        if (selectionFacade != null) {
            current = selectionFacade.findExamById(selectionFacade.loadAllExams(), examId);
            if (current == null && examId > 0) {
                current = selectionFacade.representativeExam(selectionFacade.loadAllExams(), examId);
            }
        }
        ExamStaffPageBinder.publishQueue(request, session, snapshot.getFullQueue(), snapshot.getActiveQueue(),
                snapshot.getProcedureDone(), examId, examId, current);
    }
}

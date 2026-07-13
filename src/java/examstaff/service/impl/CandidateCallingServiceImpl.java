package examstaff.service.impl;

import dto.exam.ExamRegistrationDTO;
import examstaff.model.view.CallBoardState;
import examstaff.service.CandidateCallingService;
import examstaff.service.CandidateQueueService;
import examstaff.service.impl.CandidateQueueServiceImpl;

import java.util.List;

public class CandidateCallingServiceImpl implements CandidateCallingService {

    private final CandidateQueueService queueService;

    public CandidateCallingServiceImpl() {
        this(new CandidateQueueServiceImpl());
    }

    public CandidateCallingServiceImpl(CandidateQueueService queueService) {
        this.queueService = queueService;
    }

    @Override
    public ExamRegistrationDTO resolveCallingCandidate(String callingSbd, List<ExamRegistrationDTO> queue) {
        if (queue == null || callingSbd == null || callingSbd.isBlank()) {
            return null;
        }
        for (ExamRegistrationDTO c : queue) {
            if (!callingSbd.equals(c.getSbd())) {
                continue;
            }
            if (!c.isProcedureComplete()) {
                return c;
            }
            String nextSbd = queueService.resolveNextCallingSbd(queue, callingSbd);
            return nextSbd != null ? queueService.findBySbd(queue, nextSbd) : null;
        }
        return null;
    }

    @Override
    public String resolveSyncedCallingSbd(String sessionCallingSbd, CallBoardState callBoard,
            List<ExamRegistrationDTO> queue) {
        String boardCalling = callBoard != null ? callBoard.getCallingSbd() : null;
        String callingSbd = sessionCallingSbd != null && !sessionCallingSbd.isBlank()
                ? sessionCallingSbd
                : boardCalling;
        if (callingSbd != null && !callingSbd.isBlank() && queue != null) {
            ExamRegistrationDTO atDesk = queueService.findBySbd(queue, callingSbd);
            if (atDesk == null || atDesk.isProcedureComplete() || atDesk.isSuspended() || atDesk.isAbsent()) {
                callingSbd = queueService.resolveNextCallingSbd(queue, callingSbd);
            }
        }
        return callingSbd;
    }

    @Override
    public String advanceCallingIfDone(String callingSbd, List<ExamRegistrationDTO> candidateQueue) {
        if (candidateQueue == null || callingSbd == null || callingSbd.isBlank()) {
            return callingSbd;
        }
        ExamRegistrationDTO current = queueService.findBySbd(candidateQueue, callingSbd);
        if (current != null && !current.isSuspended() && !current.isProcedureComplete()) {
            return callingSbd;
        }
        return queueService.resolveNextCallingSbd(candidateQueue, callingSbd);
    }
}

package service.impl;

import dao.CallBoardDAO;
import dto.examstaff.CandidateCallBoardStateDTO;
import dto.exam.ExamRegistrationDTO;
import model.view.CallBoardState;
import service.CallBoardSyncService;
import service.CandidateCallBoardService;

import java.util.List;

public class CandidateCallBoardServiceImpl implements CandidateCallBoardService {

    private final CallBoardSyncService syncService = new CallBoardSyncServiceImpl();

    @Override
    public CandidateCallBoardStateDTO getState(CallBoardDAO callBoardDAO, int examSessionId) {
        CallBoardState state = syncService.getState(callBoardDAO, examSessionId);
        return state != null ? toDto(state) : null;
    }

    @Override
    public void sync(CallBoardDAO callBoardDAO, int examSessionId, String callingSbd,
            List<ExamRegistrationDTO> queue, boolean shiftEnded) {
        syncService.sync(callBoardDAO, examSessionId, callingSbd, queue, shiftEnded);
    }

    @Override
    public void occupyDesk(CallBoardDAO callBoardDAO, int examSessionId, String deskSbd,
            List<ExamRegistrationDTO> queue, boolean shiftEnded) {
        syncService.occupyDesk(callBoardDAO, examSessionId, deskSbd, queue, shiftEnded);
    }

    @Override
    public void releaseDeskAndCall(CallBoardDAO callBoardDAO, int examSessionId, String callingSbd,
            List<ExamRegistrationDTO> queue, boolean shiftEnded) {
        syncService.releaseDeskAndCall(callBoardDAO, examSessionId, callingSbd, queue, shiftEnded);
    }

    private static CandidateCallBoardStateDTO toDto(CallBoardState state) {
        CandidateCallBoardStateDTO dto = new CandidateCallBoardStateDTO();
        dto.setCallingSbd(state.getCallingSbd());
        dto.setNextSbd(state.getNextSbd());
        dto.setShiftEnded(state.isShiftEnded());
        dto.setUpdatedAtMs(state.getUpdatedAtMs());
        return dto;
    }
}

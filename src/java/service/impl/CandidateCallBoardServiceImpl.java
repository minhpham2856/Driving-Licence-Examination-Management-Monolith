package service.impl;

import dto.examstaff.CandidateCallBoardStateDTO;
import dto.exam.ExamRegistrationDTO;
import model.view.CallBoardState;
import repository.CallBoardRepository;
import service.CallBoardSyncService;
import service.CandidateCallBoardService;

import java.util.List;

public class CandidateCallBoardServiceImpl implements CandidateCallBoardService {

    private final CallBoardSyncService syncService = new CallBoardSyncServiceImpl();

    @Override
    public CandidateCallBoardStateDTO getState(CallBoardRepository repository, int examSessionId) {
        CallBoardState state = syncService.getState(repository, examSessionId);
        return state != null ? toDto(state) : null;
    }

    @Override
    public void sync(CallBoardRepository repository, int examSessionId, String callingSbd,
            List<ExamRegistrationDTO> queue, boolean shiftEnded) {
        syncService.sync(repository, examSessionId, callingSbd, queue, shiftEnded);
    }

    @Override
    public void occupyDesk(CallBoardRepository repository, int examSessionId, String deskSbd,
            List<ExamRegistrationDTO> queue, boolean shiftEnded) {
        syncService.occupyDesk(repository, examSessionId, deskSbd, queue, shiftEnded);
    }

    @Override
    public void releaseDeskAndCall(CallBoardRepository repository, int examSessionId, String callingSbd,
            List<ExamRegistrationDTO> queue, boolean shiftEnded) {
        syncService.releaseDeskAndCall(repository, examSessionId, callingSbd, queue, shiftEnded);
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

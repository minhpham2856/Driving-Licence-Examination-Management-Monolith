package service.impl;

import controller.staff.exam.CandidateCallBoard;
import dto.candidate.CandidateCallBoardStateDTO;
import dto.exam.ExamRegistrationDTO;
import service.CandidateCallBoardService;
import jakarta.servlet.ServletContext;
import jakarta.servlet.http.HttpSession;

import java.util.List;

public class CandidateCallBoardServiceImpl implements CandidateCallBoardService {

    @Override
    public CandidateCallBoardStateDTO getState(ServletContext ctx, int examSessionId) {
        if (ctx == null || examSessionId <= 0) {
            return null;
        }
        CandidateCallBoard.State state = CandidateCallBoard.getState(ctx, examSessionId);
        if (state == null) {
            return null;
        }
        return toDto(state);
    }

    @Override
    public void sync(ServletContext ctx, int examSessionId, String callingSbd,
            List<ExamRegistrationDTO> queue, boolean shiftEnded) {
        CandidateCallBoard.sync(ctx, examSessionId, callingSbd, queue, shiftEnded);
    }

    @Override
    public void syncFromSession(ServletContext ctx, HttpSession session, List<ExamRegistrationDTO> queue) {
        CandidateCallBoard.syncFromSession(ctx, session, queue);
    }

    private static CandidateCallBoardStateDTO toDto(CandidateCallBoard.State state) {
        CandidateCallBoardStateDTO dto = new CandidateCallBoardStateDTO();
        dto.setCallingSbd(state.getCallingSbd());
        dto.setNextSbd(state.getNextSbd());
        dto.setShiftEnded(state.isShiftEnded());
        dto.setUpdatedAtMs(state.getUpdatedAtMs());
        return dto;
    }
}

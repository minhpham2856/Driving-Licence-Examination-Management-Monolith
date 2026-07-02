package service;

import dto.CandidateCallBoardStateDTO;
import dto.exam.ExamRegistrationDTO;
import jakarta.servlet.ServletContext;
import jakarta.servlet.http.HttpSession;

import java.util.List;

public interface CandidateCallBoardService {

    CandidateCallBoardStateDTO getState(ServletContext ctx, int examSessionId);

    void sync(ServletContext ctx, int examSessionId, String callingSbd,
            List<ExamRegistrationDTO> queue, boolean shiftEnded);

    void syncFromSession(ServletContext ctx, HttpSession session, List<ExamRegistrationDTO> queue);
}

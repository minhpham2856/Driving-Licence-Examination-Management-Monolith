package service.impl;


import dto.candidate.CandidateCallBoardStateDTO;

import service.CandidateCallBoardService;
import jakarta.servlet.ServletContext;
import java.util.HashMap;
import java.util.Map;

public class CandidateCallBoardServiceImpl implements CandidateCallBoardService {
    private static final String CONTEXT_KEY = "candidateCallBoards";

    @SuppressWarnings("unchecked")
    private Map<Integer, CandidateCallBoardStateDTO> getBoards(ServletContext ctx) {
        Map<Integer, CandidateCallBoardStateDTO> boards = (Map<Integer, CandidateCallBoardStateDTO>) ctx.getAttribute(CONTEXT_KEY);
        if (boards == null) {
            synchronized (CandidateCallBoardServiceImpl.class) {
                boards = (Map<Integer, CandidateCallBoardStateDTO>) ctx.getAttribute(CONTEXT_KEY);
                if (boards == null) {
                    boards = new HashMap<>();
                    ctx.setAttribute(CONTEXT_KEY, boards);
                }
            }
        }
        return boards;
    }

    @Override
    public CandidateCallBoardStateDTO getState(ServletContext ctx, int examSessionId) {
        if (examSessionId <= 0) return null;
        return getBoards(ctx).computeIfAbsent(examSessionId, id -> new CandidateCallBoardStateDTO());
    }
}

package service;


import dto.CandidateCallBoardStateDTO;

import jakarta.servlet.ServletContext;

public interface CandidateCallBoardService {
    CandidateCallBoardStateDTO getState(ServletContext ctx, int examSessionId);
}

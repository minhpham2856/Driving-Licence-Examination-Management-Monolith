package service;


import dto.candidate.CandidateCallBoardStateDTO;

import jakarta.servlet.ServletContext;

public interface CandidateCallBoardService {
    CandidateCallBoardStateDTO getState(ServletContext ctx, int examSessionId);
}

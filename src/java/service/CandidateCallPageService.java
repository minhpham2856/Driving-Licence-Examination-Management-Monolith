package service;

import dto.examstaff.CandidateCallPageCommand;
import dto.examstaff.CandidateCallPageViewDTO;

public interface CandidateCallPageService {

    CandidateCallPageViewDTO preparePage(CandidateCallPageCommand command);
}

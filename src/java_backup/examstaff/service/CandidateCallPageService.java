package examstaff.service;

import examstaff.dto.CandidateCallPageCommand;
import examstaff.dto.CandidateCallPageViewDTO;

public interface CandidateCallPageService {

    CandidateCallPageViewDTO preparePage(CandidateCallPageCommand command);
}

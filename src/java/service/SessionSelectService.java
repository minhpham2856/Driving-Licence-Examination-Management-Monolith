package service;

import dto.examstaff.SessionSelectRequestDTO;
import dto.examstaff.SessionSelectResultDTO;

public interface SessionSelectService {

    SessionSelectResultDTO processSelection(SessionSelectRequestDTO request);
}

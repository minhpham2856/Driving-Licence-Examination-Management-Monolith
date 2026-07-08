package service;

import dto.examstaff.CandidateDossierViewDTO;

public interface CandidateDossierService {

    CandidateDossierViewDTO loadDossier(int examId, String sbd, String webRoot);
}

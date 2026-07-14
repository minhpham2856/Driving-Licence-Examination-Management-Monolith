package examstaff.service;

import examstaff.dto.CandidateDossierViewDTO;

public interface CandidateDossierService {

    CandidateDossierViewDTO loadDossier(int examId, String sbd, String webRoot);
}

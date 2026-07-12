package examiner.service;

import examiner.dto.SaveResultDTO;
import examiner.dto.ServiceResult;
import shared.model.Licence;

import java.util.List;

public interface LicenceService {

    List<Licence> search(String keyword);

    List<Licence> findAll();

    Licence getById(int id);

    int countAll();

    ServiceResult<SaveResultDTO> save(Licence licence, int adminUserId);
}


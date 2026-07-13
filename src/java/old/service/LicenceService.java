package service;

import dto.SaveResultDTO;
import dto.ServiceResult;
import model.Licence;

import java.util.List;

public interface LicenceService {

    List<Licence> search(String keyword);

    List<Licence> findAll();

    Licence getById(int id);

    int countAll();

    ServiceResult<SaveResultDTO> save(Licence licence, int adminUserId);
}

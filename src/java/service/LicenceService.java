package service;

import dto.ServiceResult;
import dto.payload.SaveEntityData;
import model.Licence;

import java.util.List;

public interface LicenceService {

    List<Licence> search(String keyword);

    List<Licence> findAll();

    Licence getById(int id);

    int countAll();

    ServiceResult<SaveEntityData> save(Licence licence, int adminUserId);
}

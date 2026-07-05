package service;

import dto.ServiceResult;
import dto.payload.CreateManagedUserCommand;
import dto.payload.CreateUserData;
import dto.payload.ManagedDossierCommand;

public interface UserManagementService {

    ServiceResult<CreateUserData> createUser(CreateManagedUserCommand command);

    ServiceResult<Void> saveManagedDossier(ManagedDossierCommand command);
}

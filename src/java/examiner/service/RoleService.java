package examiner.service;

import shared.model.Role;

// Service contract for resolving role records.
public interface RoleService {

    // Loads one role row by primary key.
    Role get(int id);

    // Loads one role row by display name.
    Role getByName(String name);
}

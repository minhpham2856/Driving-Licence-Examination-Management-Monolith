package examiner.dao;
import shared.model.Role;

// DAO contract for Role persistence; examiner module SQL boundary.
public interface RoleDAO {

    // Loads one role row by primary key.
    Role get(int id);

    // Loads one role row by role name string.
    Role getByName(String roleName);
}

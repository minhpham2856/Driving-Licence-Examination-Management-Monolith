package examiner.dao;

import shared.model.Profile;
import java.util.List;

// DAO contract for Profile persistence; examiner module SQL boundary.
public interface ProfileDAO {

    // Loads one profile row by government id number.
    Profile getByGovIdNo(String govIdNo);

    // Loads one profile row by phone number.
    Profile getByPhoneNo(String phoneNo);

    // Inserts a new profile row linked to a user.
    boolean add(Profile profile);

    // Updates an existing profile row.
    boolean update(Profile profile);

    // Batch-loads profile rows for a list of user ids.
    List<Profile> getAllByUserIds(List<Integer> userIds);
}

package DAOs;

import Models.Profile;

public interface ProfileDAO {

    Profile getById(int id);

    Profile getByGovIdNo(String govIdNo);

    Profile getByPhoneNo(String phoneNo);

    boolean insert(Profile profile);

    boolean update(Profile profile);
}

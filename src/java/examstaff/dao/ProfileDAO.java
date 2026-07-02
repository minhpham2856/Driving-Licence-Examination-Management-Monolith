package examstaff.dao;

import examstaff.model.Profile;
import java.util.List;

public interface ProfileDAO {

    Profile getByGovIdNo(String govIdNo);

    Profile getByPhoneNo(String phoneNo);

    boolean insert(Profile profile);

    boolean update(Profile profile);

    List<Profile> getAllByUserIds(List<Integer> userIds);
}

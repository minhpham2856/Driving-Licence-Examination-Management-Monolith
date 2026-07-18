package auth.dao;

import shared.model.Profile;
import java.util.List;

public interface ProfileDAO {

    Profile getByUserId(int userId);

    Profile getByGovIdNo(String govIdNo);

    Profile getByPhoneNo(String phoneNo);

    boolean insert(Profile profile);

    boolean update(Profile profile);

    List<Profile> getAllByUserIds(List<Integer> userIds);
}

package dao;
import java.util.*;
import model.Profile;
public interface ProfileDAO {
    Profile getById(int id);
    Profile getByGovIdNo(String govIdNo);
    Profile getByPhoneNo(String phoneNo);
    boolean insert(Profile profile);
    boolean update(Profile profile);
    List<Profile> getAllByUserIds(List<Integer> userIds);
}

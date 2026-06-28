package dao;


import model.user.Profile;


public interface ProfileDAO {

    
    Profile getById(int id);

    
    Profile getByGovIdNo(String govIdNo);

    
    Profile getByPhoneNo(String phoneNo);

    
    boolean insert(Profile profile);

    
    boolean update(Profile profile);

    
    java.util.List<Profile> findByUserIds(java.util.List<Integer> userIds);
}

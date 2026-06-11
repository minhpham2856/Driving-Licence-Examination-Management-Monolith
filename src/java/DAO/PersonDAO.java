package DAO;

import Models.Person;

public interface PersonDAO {

    Person getById(int id);

    Person getByEmail(String email);

    Person getByGovIdNo(String govIdNo);

    Person getByPhoneNo(String phoneNo);

    boolean insert(Person person);

    boolean update(Person person);

    boolean updatePhotoUrl(int personId, String photoUrl);

    boolean markPendingReview(int personId);
}

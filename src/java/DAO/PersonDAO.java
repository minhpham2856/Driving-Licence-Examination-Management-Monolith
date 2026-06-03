package DAO;

import Models.Person;

public interface PersonDAO {

    Person getById(int id);

    Person getByEmail(String email);

    Person getByGovIdNo(String govIdNo);

    boolean insert(Person person);

    boolean update(Person person);
}

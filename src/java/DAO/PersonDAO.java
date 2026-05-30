package DAO;

import Models.Person;

public interface PersonDAO {

    Person getById(int id);

    Person getByEmail(String email);

    boolean insert(Person person);

    boolean update(Person person);
}

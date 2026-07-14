package DAO;

import Models.CandidateDTO;
import Models.Person;
import java.util.List;

public interface PersonDAO {

    Person getById(int id);

    Person getByEmail(String email);

    boolean insert(Person person);

    boolean update(Person person);
    
    Person getByGovIdNo(String govIdNo);
    
    // 2. Xử lý lưu danh sách thí sinh từ Excel (Chạy vòng lặp insert)
    boolean insertCandidateList(List<CandidateDTO> listCandidates);
}

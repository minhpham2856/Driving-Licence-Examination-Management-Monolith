package dao;

import model.Candidate;
import java.util.List;

public interface CandidateDAO {

    Candidate getById(int candidateId);

    Candidate getByNumber(String candidateNumber);

    List<Candidate> getAllByIds(List<Integer> candidateIds);

    int insert(Candidate candidate);

    boolean update(Candidate candidate);

    boolean delete(int candidateId);
    
    List<Candidate> findAll();
}

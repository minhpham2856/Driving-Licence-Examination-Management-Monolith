package dao;

import model.candidate.Candidate;
import java.util.List;

public interface CandidateDAO {

    Candidate findById(int candidateId);

    Candidate findByNumber(String candidateNumber);

    List<Candidate> findByIds(List<Integer> candidateIds);

    int insert(Candidate candidate);

    boolean update(Candidate candidate);

    boolean delete(int candidateId);
    
    List<Candidate> findAll();
}

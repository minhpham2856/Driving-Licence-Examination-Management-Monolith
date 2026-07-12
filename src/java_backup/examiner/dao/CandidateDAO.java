package examiner.dao;



import shared.model.Candidate;

import java.sql.Date;

import java.util.List;



public interface CandidateDAO {



    Candidate getById(int candidateId);



    List<Candidate> getAllByIds(List<Integer> candidateIds);



    int insert(Candidate candidate);



    boolean update(Candidate candidate);



    boolean updateAbsent(int candidateId, boolean absent);



    boolean updateExaminerProfile(int candidateId, String fullName, Date dateOfBirth, String governmentIdNumber,

            String phoneNumber, String address, boolean sex, String reasonForTaking);

}



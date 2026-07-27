package examiner.dao;

import shared.model.Candidate;

import java.util.List;

// DAO contract for Candidate persistence; examiner module SQL boundary.
public interface CandidateDAO {

    // Loads one candidate row by primary key.
    Candidate get(int candidateId);

    // Loads candidate rows for a list of ids.
    List<Candidate> getAllByIds(List<Integer> candidateIds);

    // Inserts a new candidate and returns generated CandidateId.
    int add(Candidate candidate);

    // Updates all columns on an existing candidate row.
    boolean update(Candidate candidate);

    // Updates only the IsAbsent flag on a candidate row.
    boolean updateAbsent(int candidateId, boolean absent);

    // Targeted update for suspend / undo (avoids full-row rewrite).
    boolean updateSuspended(int candidateId, boolean suspended);

}

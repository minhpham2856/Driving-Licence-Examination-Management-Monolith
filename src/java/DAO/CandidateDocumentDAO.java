package DAO;

import Models.CandidateDocument;
import java.util.List;
import java.util.Map;

public interface CandidateDocumentDAO {

    int countByPersonIdAndType(int personId, String documentType);

    Map<String, Integer> countGroupedByType(int personId);

    List<CandidateDocument> findIdCardsByPersonId(int personId);

    CandidateDocument findLatestByPersonIdAndType(int personId, String documentType);

    boolean insert(CandidateDocument document);

    boolean deleteById(int id);

    boolean deleteByPersonIdAndType(int personId, String documentType);
}

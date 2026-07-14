package examiner.service;

import examiner.enums.SectionType;

public interface ExamScoreService {

    boolean upsertTheoryCorrectCount(int candidateId, int correct, int passThreshold);

    boolean upsertSectionScore(int candidateId, SectionType section, double score, boolean passed);
}

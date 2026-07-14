package examiner.service;

import shared.enums.SectionType;

public interface ExamScoreService {

    boolean upsertSectionScore(int candidateId, SectionType section, double score, boolean passed);
}

package service;

import enums.ExamSection;

public interface ExamScoreService {

    boolean upsertTheoryCorrectCount(int candidateId, int correct, int passThreshold);

    boolean upsertSectionScore(int candidateId, ExamSection section, double score, boolean passed);
}

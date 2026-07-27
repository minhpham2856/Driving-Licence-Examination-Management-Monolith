package examiner.service;

import shared.enums.SectionType;

// Service contract for persisting section scores linked to exam results.
public interface ScoreService {

    // Creates or updates a section score row and syncs the exam result pass flag.
    boolean update(int candidateId, SectionType section, double score, boolean passed);
}

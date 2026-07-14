package examstaff.dao;

import shared.model.ExamSection;
import java.util.List;

public interface ExamSectionDAO {

    // --- mainTest methods ---
    ExamSection getById(int examSectionId);

    ExamSection getBySectionType(String sectionType);

    // --- CleanMyBranch methods ---
    /** Alias của {@link #getById(int)} */
    ExamSection findById(int examSectionId);

    List<ExamSection> findAll();

    List<ExamSection> findByExamId(int examId);
}


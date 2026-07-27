package examiner.dao;

import shared.model.ExamSection;
import java.util.List;

// DAO contract for ExamSection persistence; examiner module SQL boundary.
public interface ExamSectionDAO {

    // Loads one exam section row by primary key.
    ExamSection get(int examSectionId);

    // Loads the first exam section row matching a section type string.
    ExamSection getBySectionType(String sectionType);

    // All sections defined for one exam day.
    List<ExamSection> getAllByExamId(int examId);
}

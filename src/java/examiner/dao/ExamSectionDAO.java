package examiner.dao;

import examiner.model.ExamSection;

public interface ExamSectionDAO {

    ExamSection getById(int examSectionId);

    ExamSection getBySectionType(String sectionType);
}

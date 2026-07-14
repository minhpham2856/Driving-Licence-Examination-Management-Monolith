package examiner.dao;

import shared.model.ExamSection;

public interface ExamSectionDAO {

    ExamSection getById(int examSectionId);

    ExamSection getBySectionType(String sectionType);
}


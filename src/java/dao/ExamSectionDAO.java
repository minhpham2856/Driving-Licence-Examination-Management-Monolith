package dao;

import model.ExamSection;

public interface ExamSectionDAO {

    ExamSection getById(int examSectionId);

    ExamSection getBySectionName(String sectionName);
}

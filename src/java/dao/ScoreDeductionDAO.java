package dao;

import java.util.List;
import model.exam.ScoreDeduction;

public interface ScoreDeductionDAO {

    List<ScoreDeduction> findAll();

    List<ScoreDeduction> findBySectionId(int examSectionId);
}

package dao;

import model.exam.Question;
import java.util.List;

public interface QuestionDAO {
    List<Question> findByIds(List<Integer> questionIds);
}

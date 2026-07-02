package dao.impl;
import dao.ExamScoreDAO;
import model.ExamScore;
import java.util.ArrayList;
import java.util.List;
public class ExamScoreDAOImpl implements ExamScoreDAO {
    @Override
    public ExamScore findById(int examScoreId) { return null; }
    @Override
    public int insert(ExamScore score) { return 0; }
    @Override
    public boolean update(ExamScore score) { return false; }
    @Override
    public boolean delete(int examScoreId) { return false; }
    @Override
    public int countAll() { return 0; }
    @Override
    public ExamScore getByCandidateId(int candidateId) { return null; }
    @Override
    public boolean updateScores(int candidateId, int t, String tR, int p, String pR) { return false; }
}

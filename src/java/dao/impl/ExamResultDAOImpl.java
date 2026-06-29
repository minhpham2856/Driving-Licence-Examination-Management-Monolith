package dao.impl;

import dao.ExamResultDAO;
import model.exam.ExamResult;
import java.util.ArrayList;
import java.util.List;

public class ExamResultDAOImpl implements ExamResultDAO {

    @Override
    public ExamResult findById(int examResultId) { return null; }

    @Override
    public int insert(ExamResult result) { return 0; }

    @Override
    public boolean update(ExamResult result) { return false; }

    @Override
    public boolean delete(int examResultId) { return false; }

    @Override
    public int countAll() { return 0; }

    @Override
    public ExamResult getByCandidateId(int candidateId) { return null; }

    @Override
    public boolean updateTheoryCorrectCount(int candidateId, int correct, int passThreshold) { return false; }
}


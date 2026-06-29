package dao.impl;

import dao.ExaminerScheduleDAO;
import model.exam.ExaminerSchedule;
import dto.examiner.ExaminerSlotDTO;
import java.sql.Date;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.HashSet;
import dto.user.UserDTO;

public class ExaminerScheduleDAOImpl implements ExaminerScheduleDAO {

    @Override
    public boolean insert(ExaminerSchedule schedule) { return false; }

    @Override
    public boolean delete(int examinerScheduleId) { return false; }

    @Override
    public List<ExaminerSchedule> getBySessionId(int sessionId) { return new ArrayList<>(); }

    @Override
    public List<ExaminerSchedule> getByExaminerId(int examinerId) { return new ArrayList<>(); }

    @Override
    public List<ExaminerSchedule> getBySessionIds(List<Integer> sessionIds) { return new ArrayList<>(); }

    @Override
    public List<UserDTO> getActiveExaminers() { return new ArrayList<>(); }

    @Override
    public List<ExaminerSlotDTO> getByExamDate(Date date, Map<Integer, Date> sessionDates) { return new ArrayList<>(); }

    @Override
    public Set<Integer> getBusyExaminerIds(Date examDate, Map<Integer, Date> sessionDates) { return new HashSet<>(); }

    @Override
    public boolean assign(ExaminerSlotDTO slot) { return false; }

    @Override
    public boolean remove(String slotKey) { return false; }
    public List<ExaminerSlotDTO> getInProgressAssignmentsForExaminer(int examinerUserId) { return new ArrayList<>(); }
}


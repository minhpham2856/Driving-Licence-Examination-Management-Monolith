package examiner.service.impl;

import examiner.dao.ExamAreaDAO;
import examiner.dao.ExamDAO;
import examiner.dao.ExamSectionDAO;
import examiner.dao.ExaminerScheduleDAO;
import examiner.dao.LicenceDAO;
import examiner.dao.impl.ExamAreaDAOImpl;
import examiner.dao.impl.ExamDAOImpl;
import examiner.dao.impl.ExamSectionDAOImpl;
import examiner.dao.impl.ExaminerScheduleDAOImpl;
import examiner.dao.impl.LicenceDAOImpl;
import shared.model.Exam;
import shared.model.ExamArea;
import shared.model.ExamSection;
import shared.model.ExaminerSchedule;
import shared.model.Licence;
import examiner.service.ExamService;

import java.util.List;

// Loads exam-related entities through examiner DAO copies.
public class ExamServiceImpl implements ExamService {

    private final ExamDAO examDAO = new ExamDAOImpl();
    private final ExamSectionDAO examSectionDAO = new ExamSectionDAOImpl();
    private final ExamAreaDAO examAreaDAO = new ExamAreaDAOImpl();
    private final LicenceDAO licenceDAO = new LicenceDAOImpl();
    private final ExaminerScheduleDAO examinerScheduleDAO = new ExaminerScheduleDAOImpl();

    @Override
    public Exam get(int examId) {
        return examDAO.get(examId);
    }

    @Override
    public ExamSection getBySectionId(int sectionId) {
        return examSectionDAO.get(sectionId);
    }

    @Override
    public ExamArea getByAreaId(int areaId) {
        return examAreaDAO.get(areaId);
    }

    @Override
    public Licence getByLicenceId(int licenceId) {
        return licenceDAO.get(licenceId);
    }

    @Override
    public ExaminerSchedule getByScheduleId(int scheduleId) {
        return examinerScheduleDAO.get(scheduleId);
    }

    @Override
    public List<ExaminerSchedule> getAllByExaminer(int examinerUserId) {
        return examinerScheduleDAO.getAllByExaminer(examinerUserId);
    }

    @Override
    public ExaminerSchedule getIfByExaminerAndExam(int examinerId, int examId) {
        return examinerScheduleDAO.getIfByExaminerAndExam(examinerId, examId);
    }
}

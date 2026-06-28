package service.impl;

import dao.ExaminerScheduleDAO;
import service.ExamSessionControlService;

import dto.examiner.ExaminerSlotDTO;
import dao.SessionDAO;
import dao.impl.ExaminerScheduleDAOImpl;
import dao.impl.SessionDAOImpl;
import dto.exam.SessionDTO;
import java.util.List;
import service.EnumMappingService;

public class ExamSessionControlServiceImpl implements ExamSessionControlService {
     private final EnumMappingService enumMappingService = new EnumMappingServiceImpl();

    private final SessionDAO sessionDAO = new SessionDAOImpl();
    private final ExaminerScheduleDAO assignmentDAO = new ExaminerScheduleDAOImpl();

    @Override
    public StartResult startSession(int sessionId, int staffUserId) {
        SessionDTO examSession = sessionDAO.getById(sessionId);
        if (examSession == null) {
            return StartResult.fail("KhAA'ng tAAm thAAA,Ay ca thi (SessionId=" + sessionId + ").");
        }
        if (!enumMappingService.canStartSession(examSession.getStatus())) {
            if (enumMappingService.isSessionInProgress(examSession.getStatus())) {
                return StartResult.fail("Ca thi \"" + examSession.getSessionName() + "\" A?zA,EaA? bAAA,AA,A_t A?zA,EoAAA,AA,A u.");
            }
            return StartResult.fail("Ca thi \"" + examSession.getSessionName()
                    + "\" khAA'ng thAAA' bAAA,AA,A_t A?zA,EoAAA,AA,A u (trAAng thAAi: " + examSession.getStatus() + ").");
        }

        List<ExaminerSlotDTO> assignments = assignmentDAO.getBySessionId(sessionId);
        long withArea = assignments.stream().filter(s -> s.getAreaId() > 0).count();
        if (withArea == 0) {
            return StartResult.fail("ChA?A,Aa phAAn cA'ng sAAt hAAch viA'A,An vAAo khu vA?A,Ac thi. "
                    + "VAAo mA?A,Ac \"PhAAn bAAA' sAAt hAAch viA'A,An\" trA?A,AA?c khi bAAA,AA,A_t A?zA,EoAAA,AA,A u ca.");
        }

        if (!sessionDAO.updateStatus(sessionId, enums.ExamSessionStatus.IN_PROGRESS.getStatus())) {
            return StartResult.fail("KhAA'ng cAA,AAAp nhAA,AAAt A?zA,EaA?A?A,Ac trAAng thAAi ca thi trA'A,An cA? sAAA dA?A,A liAAA,AAAu.");
        }

        return StartResult.ok(examSession.getSessionName(), (int) withArea);
    }

    @Override
    public EndResult endSession(int sessionId) {
        SessionDTO examSession = sessionDAO.getById(sessionId);
        if (examSession == null) {
            return EndResult.fail("KhAA'ng tAAm thAAA,Ay ca thi (SessionId=" + sessionId + ").");
        }
        if (!enumMappingService.isSessionInProgress(examSession.getStatus())) {
            return EndResult.fail("Ca thi \"" + examSession.getSessionName()
                    + "\" chA?A,Aa AAA trAAng thAAi A?zA,Eoang diAAA,AAA,An ra (hiAAA,AAAn tAAi: " + examSession.getStatus() + ").");
        }
        if (!sessionDAO.updateStatus(sessionId, enums.ExamSessionStatus.COMPLETED.getStatus())) {
            return EndResult.fail("KhAA'ng cAA,AAAp nhAA,AAAt A?zA,EaA?A?A,Ac trAAng thAAi kAAA,AA,At thA'A,Ac ca thi.");
        }
        return EndResult.ok(examSession.getSessionName());
    }

    @Override
    public List<ExaminerSlotDTO> getLoginEligibleAssignments(int examinerUserId) {
        return assignmentDAO.getInProgressAssignmentsForExaminer(examinerUserId);
    }
}

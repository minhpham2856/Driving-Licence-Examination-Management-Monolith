package controller.examiner;

import java.util.*;

import model.*;

import model.User;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.servlet.http.HttpServlet;
import util.ExaminerUtil;
import service.ExaminerDataService;
import service.impl.ExaminerDataServiceImpl;
import service.ExaminerActionsService;
import service.impl.ExaminerActionsServiceImpl;

import java.io.IOException;

@WebServlet("/views/examiner/candidate-call")
public class ExaminerCandidateCallServlet extends HttpServlet {

    protected final ExaminerDataService viewDataService = new ExaminerDataServiceImpl();
    protected final ExaminerActionsService examinerService = new ExaminerActionsServiceImpl();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = ExaminerUtil.requireSession(request, response);
        if (session == null) {
            return;
        }

        Integer sessionId = ExaminerUtil.activeSessionId(session);
        String sbd = request.getParameter("sbd");
        String search = request.getParameter("q");
        String action = request.getParameter("action");

        if (sessionId != null && sessionId > 0) {
            
            if (ExaminerUtil.isTheorySection(request) && request.getParameter("error") != null && request.getParameter("error").equals("theoryNoResultEdit")) {
                
            }

            if ("1".equals(request.getParameter("absenceConfirmed"))) {
                examinerService.markAbsent(sessionId, sbd, ((User) session.getAttribute("user")).getUserId());
                response.sendRedirect(request.getContextPath() + "/views/examiner/candidate-call?absentDone=" + ExaminerUtil.urlEncode(sbd));
                return;
            }

            if (action != null) {
                if (handleCallAction(request, response, session, sessionId, action, sbd)) {
                    return;
                }
            }

            Map<String, Object> data = viewDataService.getCandidateCallData(sessionId, sbd, search);
            for (Map.Entry<String, Object> mapEntry : data.entrySet()) {
                request.setAttribute(mapEntry.getKey(), mapEntry.getValue());
            }
        }

        request.getRequestDispatcher("/views/examiner/candidate-call.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = ExaminerUtil.requireSession(request, response);
        if (session == null) {
            return;
        }

        Integer sessionId = ExaminerUtil.activeSessionId(session);
        if (sessionId == null || sessionId <= 0) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        }

        if ("callSelected".equals(request.getParameter("action"))) {
            User user = (User) session.getAttribute("user");
            String[] sbds = request.getParameterValues("sbd");
            int count = examinerService.callSelectedCandidates(sessionId, sbds, user, ((User) session.getAttribute("user")).getUserId(), ExaminerUtil.resolveSectionType(session), ExaminerUtil.resolveSectionName(session), ExaminerUtil.resolveCallDestination(session));
            if (count <= 0) {
                response.sendRedirect(request.getContextPath() + "/views/examiner/candidate-call?error=callSelectedFailed");
                return;
            }
            response.sendRedirect(request.getContextPath() + "/views/examiner/candidate-call?calledBatch=" + count);
            return;
        }

        doGet(request, response);
    }

    private boolean handleCallAction(HttpServletRequest request, HttpServletResponse response,
            HttpSession session, int sessionId, String action, String sbd) throws IOException {
        User user = (User) session.getAttribute("user");
        switch (action) {
            case "call" -> {
                if (sbd == null || sbd.isBlank()) {
                    String calledSbd = examinerService.callNextCandidate(sessionId, user, ((User) session.getAttribute("user")).getUserId(), ExaminerUtil.resolveSectionType(session), ExaminerUtil.resolveSectionName(session), ExaminerUtil.resolveCallDestination(session));
                    if (calledSbd == null) {
                        response.sendRedirect(request.getContextPath() + "/views/examiner/candidate-call?error=noCandidate");
                        return true;
                    }
                    response.sendRedirect(request.getContextPath() + "/views/examiner/candidate-call?called=" + ExaminerUtil.urlEncode(calledSbd));
                    return true;
                }
                if (!examinerService.callCandidate(sessionId, sbd, user, ((User) session.getAttribute("user")).getUserId(), ExaminerUtil.resolveSectionType(session), ExaminerUtil.resolveSectionName(session), ExaminerUtil.resolveCallDestination(session))) {
                    response.sendRedirect(request.getContextPath() + "/views/examiner/candidate-call?error=callFailed&sbd=" + ExaminerUtil.urlEncode(sbd));
                    return true;
                }
                response.sendRedirect(request.getContextPath() + "/views/examiner/candidate-call?called=" + ExaminerUtil.urlEncode(sbd));
                return true;
            }
            case "undoAbsent" -> {
                if (sbd == null || sbd.isBlank()) {
                    response.sendRedirect(request.getContextPath() + "/views/examiner/candidate-call?error=noSbd");
                    return true;
                }
                if (!examinerService.undoAbsent(sessionId, sbd, ((User) session.getAttribute("user")).getUserId())) {
                    response.sendRedirect(request.getContextPath() + "/views/examiner/candidate-call?error=undoAbsentFailed&sbd=" + ExaminerUtil.urlEncode(sbd));
                    return true;
                }
                response.sendRedirect(request.getContextPath() + "/views/examiner/candidate-call?undoAbsent=" + ExaminerUtil.urlEncode(sbd));
                return true;
            }
            case "markAbsent" -> {
                if (sbd == null || sbd.isBlank()) {
                    response.sendRedirect(request.getContextPath() + "/views/examiner/candidate-call?error=noSbd");
                    return true;
                }
                if (!examinerService.markAbsent(sessionId, sbd, ((User) session.getAttribute("user")).getUserId())) {
                    response.sendRedirect(request.getContextPath() + "/views/examiner/candidate-call?error=absentFailed&sbd=" + ExaminerUtil.urlEncode(sbd));
                    return true;
                }
                response.sendRedirect(request.getContextPath() + "/views/examiner/candidate-call?absentDone=" + ExaminerUtil.urlEncode(sbd));
                return true;
            }
            case "printSignature" -> {
                if (sbd == null || sbd.isBlank()) {
                    response.sendRedirect(request.getContextPath() + "/views/examiner/candidate-call?error=noSbd");
                    return true;
                }
                if (!examinerService.printSignatureForm(sessionId, sbd, ((User) session.getAttribute("user")).getUserId())) {
                    response.sendRedirect(request.getContextPath() + "/views/examiner/candidate-call?error=signaturePrintFailed&sbd=" + ExaminerUtil.urlEncode(sbd));
                    return true;
                }
                response.sendRedirect(request.getContextPath() + "/views/examiner/print-documents?sbd=" + ExaminerUtil.urlEncode(sbd) + "&signatureMarked=1");
                return true;
            }
            case "completeSection" -> {
                if (sbd == null || sbd.isBlank()) {
                    response.sendRedirect(request.getContextPath() + "/views/examiner/candidate-call?error=noSbd");
                    return true;
                }
                String completeError = examinerService.completeCandidateSection(sessionId, sbd, ((User) session.getAttribute("user")).getUserId());
                if ("needSignaturePrint".equals(completeError)) {
                    response.sendRedirect(request.getContextPath() + "/views/examiner/candidate-call?error=needSignaturePrint&sbd=" + ExaminerUtil.urlEncode(sbd));
                    return true;
                }
                if (completeError != null) {
                    response.sendRedirect(request.getContextPath() + "/views/examiner/candidate-call?error=completeFailed&sbd=" + ExaminerUtil.urlEncode(sbd));
                    return true;
                }
                response.sendRedirect(request.getContextPath() + "/views/examiner/candidate-call?completeDone=" + ExaminerUtil.urlEncode(sbd));
                return true;
            }
            default -> {
                return false;
            }
        }
    }
}

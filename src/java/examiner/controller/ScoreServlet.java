package examiner.controller;

import shared.enums.SectionType;
import static shared.enums.SectionType.THEORY;
import examiner.filter.ExaminerFilter;
import auth.dto.UserDTO;
import shared.Attributes;
import shared.model.User;
import examiner.service.ActionService;
import examiner.service.ExamViewService;
import examiner.service.impl.ActionServiceImpl;
import examiner.service.impl.ExamViewServiceImpl;
import examiner.dto.CandidateRowDTO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import static shared.util.FormatUtil.formatPositiveInteger;
import static examiner.util.FormatUtil.formatSbdFromRequest;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import shared.model.ExaminerSchedule;

@WebServlet("/examiner/score-entry")
// Practical score entry: load fault lists, adjust deductions, change vehicle, finalize scores, and complete section.
public class ScoreServlet extends HttpServlet {

    protected final ExamViewService viewService = new ExamViewServiceImpl();
    protected final ActionService actionService = new ActionServiceImpl();

    // Serve score-entry data and handle GET-triggered actions (invoke, adjust deduction, vehicle change, print).
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        if (session == null) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        Integer activeExamId = (Integer) session.getAttribute(ExaminerFilter.ATTR_ACTIVE_EXAM_ID);
        Integer sbd = formatPositiveInteger(request.getParameter("sbd"));

        String action = request.getParameter("action");
        UserDTO userDto = (UserDTO) session.getAttribute(Attributes.Session.USER);
        User user = userDto != null ? userDto.toUser() : null;
        SectionType sectionType = ExaminerFilter.resolveSectionType(session);

        if (activeExamId != null && activeExamId > 0) {
            // Score entry is layout-only; theory examiners use the call-board instead.
            if (sectionType == THEORY && request.getParameter("error") == null) {
                response.sendRedirect(request.getContextPath() + "/examiner/action?error=theoryNoScoreEntry");
                return;
            }

            if (action != null) {
                // Score-entry fault changes are local-only; GET actions are limited to page-level actions.
                if (handleScoreEntryAction(request, response, session, activeExamId, action, sbd, user, sectionType)) {
                    return;
                }
            }

            // Always load queue + licence-scoped fault list (even before selecting SBD).
            Map<String, Object> data = viewService.getScoreEntryViewByExam(activeExamId, sbd, sectionType);
            if (data != null) {
                for (Map.Entry<String, Object> mapEntry : data.entrySet()) {
                    request.setAttribute(mapEntry.getKey(), mapEntry.getValue());
                }
                // JSP expects sessionVehicles; service provides examVehicles.
                if (request.getAttribute("sessionVehicles") == null
                        && request.getAttribute("examVehicles") != null) {
                    request.setAttribute("sessionVehicles", request.getAttribute("examVehicles"));
                }
                Object selectedCandidate = request.getAttribute("candidate");
                if (selectedCandidate instanceof CandidateRowDTO candidate
                        && !candidate.isPracticalEntryAllowed()) {
                    response.sendRedirect(request.getContextPath() + "/examiner/action?error=practicalNotAllowed");
                    return;
                }
            }
            request.setAttribute("activeExamId", activeExamId);
            request.setAttribute("examAreaId", resolveExamAreaId(session));
            request.setAttribute("scoreFromAction", "action".equals(request.getParameter("from")));
            if (sectionType != THEORY && sbd != null && sbd > 0) {
                String scoreToken = UUID.randomUUID().toString();
                session.setAttribute("scoreSubmissionToken", scoreToken);
                request.setAttribute("scoreSubmissionToken", scoreToken);
            }
        }

        request.getRequestDispatcher("/views/examiner/score-entry.jsp").forward(request, response);
    }

    // Handle POST actions: deduction adjustment, finalize, vehicle change, print, complete section, or device status.
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        if (session == null) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        Integer activeExamId = (Integer) session.getAttribute(ExaminerFilter.ATTR_ACTIVE_EXAM_ID);
        if (activeExamId == null || activeExamId <= 0) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        }

        SectionType sectionType = ExaminerFilter.resolveSectionType(session);
        String action = request.getParameter("action");
        // POST handles form submissions that change score state or device assignment.
        if ("markPresent".equals(action) || "startCandidate".equals(action)) {
            Integer sbd = formatPositiveInteger(request.getParameter("sbd"));
            if (sbd == null) {
                response.sendRedirect(request.getContextPath() + "/examiner/score-entry?error=noSbd");
                return;
            }
            UserDTO userDto = (UserDTO) session.getAttribute(Attributes.Session.USER);
            Integer userId = userDto != null ? userDto.getUserId() : null;
            if (!actionService.markPresent(activeExamId, sbd, userId, sectionType).isSuccess()) {
                response.sendRedirect(request.getContextPath() + "/examiner/score-entry?sbd="
                        + urlEncode(sbd) + "&error=presentFailed");
                return;
            }
            response.sendRedirect(request.getContextPath() + "/examiner/score-entry?sbd="
                    + urlEncode(sbd) + "&presentDone=" + urlEncode(sbd));
            return;
        }

        if ("savePracticalScore".equals(action)) {
            Integer sbd = formatPositiveInteger(request.getParameter("sbd"));
            if (sbd == null || sectionType == THEORY
                    || !consumeScoreToken(session, request.getParameter("submissionToken"))) {
                response.sendRedirect(request.getContextPath() + "/examiner/score-entry?error=scorePayloadInvalid");
                return;
            }
            Integer deviceId = formatPositiveInteger(request.getParameter("deviceId"));
            Integer elapsedSeconds = formatPositiveInteger(request.getParameter("elapsedSeconds"));
            UserDTO userDto = (UserDTO) session.getAttribute(Attributes.Session.USER);
            Integer userId = userDto != null ? userDto.getUserId() : null;
            Map<Integer, Integer> occurrences = parseOccurrences(request);
            if (deviceId == null || elapsedSeconds == null
                    || !actionService.savePracticalScore(activeExamId, resolveExamAreaId(session), sbd, deviceId,
                            elapsedSeconds, occurrences, userId).isSuccess()) {
                response.sendRedirect(request.getContextPath() + "/examiner/score-entry?sbd="
                        + urlEncode(sbd) + "&from=action&error=scoreFailed");
                return;
            }
            response.sendRedirect(request.getContextPath() + "/examiner/score-entry?sbd="
                    + urlEncode(sbd) + "&from=action&scoreSaved=1");
            return;
        }

        if ("finalize".equals(action)) {
            Integer sbd = formatPositiveInteger(request.getParameter("sbd"));
            if (sbd == null) {
                response.sendRedirect(request.getContextPath() + "/examiner/score-entry?error=noSbd");
                return;
            }

            UserDTO userDto = (UserDTO) session.getAttribute(Attributes.Session.USER);
            Integer userId = userDto != null ? userDto.getUserId() : null;
            if (!actionService.finalizeScoreEntry(activeExamId, sbd, userId, sectionType).isSuccess()) {
                response.sendRedirect(request.getContextPath() + "/examiner/score-entry?sbd="
                        + urlEncode(sbd) + "&error=finalizeFailed");
                return;
            }

            response.sendRedirect(request.getContextPath() + "/examiner/score-entry?sbd="
                    + urlEncode(sbd) + "&finalized=1");
            return;
        }

        if ("changeVehicle".equals(action)) {
            Integer sbd = formatSbdFromRequest(request);
            int deviceId;
            try {
                deviceId = Integer.parseInt(request.getParameter("deviceId"));
            } catch (NumberFormatException e) {
                response.sendRedirect(buildScoreEntryUrl(request, sbd, "error=invalidDevice"));
                return;
            }
            if (sbd == null) {
                response.sendRedirect(request.getContextPath() + "/examiner/score-entry?error=noSbd");
                return;
            }
            UserDTO userDto = (UserDTO) session.getAttribute(Attributes.Session.USER);
            Integer userId = userDto != null ? userDto.getUserId() : null;
            if (!actionService.changeCandidateVehicle(activeExamId, sbd, deviceId, userId, sectionType).isSuccess()) {
                response.sendRedirect(buildScoreEntryUrl(request, sbd, "error=vehicleFailed"));
                return;
            }
            response.sendRedirect(buildScoreEntryUrl(request, sbd, "vehicleChanged=1"));
            return;
        }

        if ("printResult".equals(action) || "printSignature".equals(action)) {
            Integer sbd = formatSbdFromRequest(request);
            if (sbd == null) {
                response.sendRedirect(request.getContextPath() + "/examiner/score-entry?error=noSbd");
                return;
            }
            UserDTO userDto = (UserDTO) session.getAttribute(Attributes.Session.USER);
            Integer userId = userDto != null ? userDto.getUserId() : null;
            if (!actionService.printResultForm(activeExamId, sbd, userId, sectionType).isSuccess()) {
                response.sendRedirect(buildScoreEntryUrl(request, sbd, "error=resultPrintFailed"));
                return;
            }
            response.sendRedirect(request.getContextPath() + "/examiner/print?type=result&sbd="
                    + urlEncode(sbd));
            return;
        }

        if ("completeSectionScore".equals(action) || "completeSection".equals(action)) {
            Integer sbd = formatSbdFromRequest(request);
            if (sbd == null) {
                response.sendRedirect(request.getContextPath() + "/examiner/score-entry?error=noSbd");
                return;
            }
            UserDTO userDto = (UserDTO) session.getAttribute(Attributes.Session.USER);
            Integer userId = userDto != null ? userDto.getUserId() : null;
            examiner.dto.ServiceResult<Void> res = actionService.completeCandidateSection(
                    activeExamId, sbd, userId, null, sectionType);
            if (res != null && "needResultPrint".equals(res.getMessage())) {
                response.sendRedirect(buildScoreEntryUrl(request, sbd, "error=needResultPrint"));
                return;
            }
            if (res != null && !res.isSuccess()) {
                response.sendRedirect(buildScoreEntryUrl(request, sbd, "error=completeFailed"));
                return;
            }
            response.sendRedirect(request.getContextPath() + "/examiner/action?completeDone="
                    + urlEncode(sbd));
            return;
        }

        if ("maintenance".equals(action) || "operational".equals(action)) {
            Integer sbd = formatSbdFromRequest(request);
            int deviceId;
            try {
                deviceId = Integer.parseInt(request.getParameter("deviceId"));
            } catch (NumberFormatException e) {
                response.sendRedirect(buildScoreEntryUrl(request, sbd, "error=invalidDevice"));
                return;
            }
            UserDTO userDto = (UserDTO) session.getAttribute(Attributes.Session.USER);
            Integer userId = userDto != null ? userDto.getUserId() : null;
            boolean updated;
            if ("operational".equals(action)) {
                updated = actionService.setDeviceAvailable(deviceId, userId).isSuccess();
            } else {
                updated = actionService.setDeviceMaintenance(deviceId, userId).isSuccess();
            }
            if (!updated) {
                response.sendRedirect(buildScoreEntryUrl(request, sbd, "error=maintenanceFailed"));
                return;
            }
            String flash = "operational".equals(action) ? "operationalDone=" + deviceId
                    : "maintenanceDone=" + deviceId;
            response.sendRedirect(buildScoreEntryUrl(request, sbd, flash));
            return;
        }

        doGet(request, response);
    }

    // Dispatch GET-triggered score-entry actions and redirect with flash query params on success or failure.
    private boolean handleScoreEntryAction(HttpServletRequest request, HttpServletResponse response,
            HttpSession session, int activeExamId, String action, Integer sbd, User user,
            SectionType sectionType) throws IOException {

        String destination = resolveActionDestination(session);

        // GET-side shortcuts mirror POST handlers but keep bookmarkable URLs.
        switch (action) {
            case "invoke" -> {
                if (sbd == null) {
                    response.sendRedirect(request.getContextPath() + "/examiner/score-entry?error=noCandidate");
                    return true;
                }

                if (!actionService.actionScoreEntryCandidate(activeExamId, sbd, user, user.getUserId(),
                        sectionType, destination, true).isSuccess()) {
                    response.sendRedirect(request.getContextPath() + "/examiner/score-entry?error=invokeFailed&sbd="
                            + urlEncode(sbd));
                    return true;
                }

                response.sendRedirect(request.getContextPath() + "/examiner/score-entry?sbd="
                        + urlEncode(sbd) + "&scoreInvoked=1");
                return true;
            }
            case "changeVehicle" -> {
                if (sbd == null) {
                    response.sendRedirect(request.getContextPath() + "/examiner/score-entry?error=noSbd");
                    return true;
                }
                int deviceId;
                try {
                    deviceId = Integer.parseInt(request.getParameter("deviceId"));
                } catch (NumberFormatException e) {
                    response.sendRedirect(buildScoreEntryUrl(request, sbd, "error=invalidDevice"));
                    return true;
                }
                Integer userId = user != null ? user.getUserId() : null;
                if (!actionService.changeCandidateVehicle(activeExamId, sbd, deviceId, userId, sectionType).isSuccess()) {
                    response.sendRedirect(buildScoreEntryUrl(request, sbd, "error=vehicleFailed"));
                    return true;
                }
                response.sendRedirect(buildScoreEntryUrl(request, sbd, "vehicleChanged=1"));
                return true;
            }
            case "printResult", "printSignature" -> {
                if (sbd == null) {
                    response.sendRedirect(request.getContextPath() + "/examiner/score-entry?error=noSbd");
                    return true;
                }
                Integer userId = user != null ? user.getUserId() : null;
                if (!actionService.printResultForm(activeExamId, sbd, userId, sectionType).isSuccess()) {
                    response.sendRedirect(buildScoreEntryUrl(request, sbd, "error=resultPrintFailed"));
                    return true;
                }
                response.sendRedirect(request.getContextPath() + "/examiner/print?type=result&sbd="
                        + urlEncode(sbd));
                return true;
            }
            case "completeSectionScore", "completeSection" -> {
                if (sbd == null) {
                    response.sendRedirect(request.getContextPath() + "/examiner/score-entry?error=noSbd");
                    return true;
                }
                Integer userId = user != null ? user.getUserId() : null;
                examiner.dto.ServiceResult<Void> res = actionService.completeCandidateSection(
                        activeExamId, sbd, userId, null, sectionType);
                if (res != null && "needResultPrint".equals(res.getMessage())) {
                    response.sendRedirect(buildScoreEntryUrl(request, sbd, "error=needResultPrint"));
                    return true;
                }
                if (res != null && !res.isSuccess()) {
                    response.sendRedirect(buildScoreEntryUrl(request, sbd, "error=completeFailed"));
                    return true;
                }
                response.sendRedirect(request.getContextPath() + "/examiner/action?completeDone="
                        + urlEncode(sbd));
                return true;
            }
            default -> {
                return false;
            }
        }
    }

    // Build a score-entry redirect URL preserving sbd and optional extra query string.
    private String buildScoreEntryUrl(HttpServletRequest request, Integer sbd, String extraQuery) {
        StringBuilder url = new StringBuilder(request.getContextPath()).append("/examiner/score-entry");
        boolean hasParam = false;
        if (sbd != null) {
            url.append("?sbd=").append(urlEncode(sbd));
            hasParam = true;
        }
        if (extraQuery != null && !extraQuery.isBlank()) {
            url.append(hasParam ? "&" : "?").append(extraQuery);
        }
        return url.toString();
    }

    private String urlEncode(int value) {
        return URLEncoder.encode(String.valueOf(value), StandardCharsets.UTF_8);
    }

    private int resolveExamAreaId(HttpSession session) {
        ExaminerSchedule schedule = (ExaminerSchedule) session.getAttribute(ExaminerFilter.ATTR_EXAMINER_SCHEDULE);
        return schedule != null && schedule.getExamAreaId() != null ? schedule.getExamAreaId() : 0;
    }

    private boolean consumeScoreToken(HttpSession session, String submitted) {
        Object stored = session.getAttribute("scoreSubmissionToken");
        if (submitted == null || stored == null || !submitted.equals(stored.toString())) {
            return false;
        }
        session.removeAttribute("scoreSubmissionToken");
        return true;
    }

    private Map<Integer, Integer> parseOccurrences(HttpServletRequest request) {
        Map<Integer, Integer> occurrences = new HashMap<>();
        for (Map.Entry<String, String[]> item : request.getParameterMap().entrySet()) {
            if (!item.getKey().startsWith("deduction_")) {
                continue;
            }
            try {
                int deductionId = Integer.parseInt(item.getKey().substring("deduction_".length()));
                int count = Integer.parseInt(item.getValue()[0]);
                occurrences.put(deductionId, count);
            } catch (NumberFormatException ignored) {
            }
        }
        return occurrences;
    }

    // Resolve the Vietnamese section label used in audit messages for call-board actions.
    private String resolveActionDestination(HttpSession session) {
        SectionType sectionType = ExaminerFilter.resolveSectionType(session);
        if (sectionType != null) {
            return sectionType.getValue();
        }
        return "Khu vực thi";
    }
}

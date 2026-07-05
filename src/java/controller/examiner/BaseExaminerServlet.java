package controller.examiner;

import dto.ExaminerSlotDTO;
import dto.payload.AdjustScoreDeductionCommand;
import dto.payload.CallCandidateCommand;
import dto.payload.CandidateSessionCommand;
import dto.payload.DeviceActionCommand;
import dto.payload.RecordViolationCommand;
import dto.payload.ScoreEditCommand;
import enums.ExamSection;
import filter.ExaminerFilter;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import model.User;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

abstract class BaseExaminerServlet extends HttpServlet {

    protected HttpSession requireSession(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        HttpSession session = request.getSession(false);
        if (session == null) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
        }
        return session;
    }

    protected Integer getActiveSessionId(HttpSession session) {
        if (session == null) {
            return null;
        }
        return (Integer) session.getAttribute(ExaminerFilter.ATTR_ACTIVE_SESSION_ID);
    }

    protected Integer parseSbdParam(String raw) {
        if (raw == null || raw.isBlank()) {
            return null;
        }
        try {
            int sbd = Integer.parseInt(raw.trim());
            return sbd > 0 ? sbd : null;
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    protected int[] parseSbdParams(String[] values) {
        if (values == null || values.length == 0) {
            return new int[0];
        }
        List<Integer> parsed = new ArrayList<>();
        for (String value : values) {
            Integer sbd = parseSbdParam(value);
            if (sbd != null) {
                parsed.add(sbd);
            }
        }
        int[] result = new int[parsed.size()];
        for (int i = 0; i < parsed.size(); i++) {
            result[i] = parsed.get(i);
        }
        return result;
    }

    protected String encodeSbd(int sbd) {
        return URLEncoder.encode(String.valueOf(sbd), StandardCharsets.UTF_8);
    }

    protected String stripContextPath(HttpServletRequest request) {
        String uri = request.getRequestURI();
        String ctx = request.getContextPath();
        if (ctx != null && !ctx.isEmpty() && uri.startsWith(ctx)) {
            return uri.substring(ctx.length());
        }
        return uri;
    }

    protected ExamSection getExamSection(HttpSession session) {
        if (session == null) {
            return ExamSection.THEORY;
        }
        Object sectionObj = session.getAttribute(ExaminerFilter.ATTR_EXAM_SECTION);
        if (sectionObj instanceof ExamSection) {
            return (ExamSection) sectionObj;
        }
        Object slotObj = session.getAttribute(ExaminerFilter.ATTR_SLOT);
        if (slotObj instanceof ExaminerSlotDTO) {
            ExamSection fromSlot = ((ExaminerSlotDTO) slotObj).getExamSection();
            if (fromSlot != null) {
                return fromSlot;
            }
        }
        return ExamSection.THEORY;
    }

    protected String getSectionDisplayName(HttpSession session) {
        if (session == null) {
            return null;
        }
        Object slotObj = session.getAttribute(ExaminerFilter.ATTR_SLOT);
        if (slotObj instanceof ExaminerSlotDTO) {
            return ((ExaminerSlotDTO) slotObj).getExamTypeName();
        }
        Object name = session.getAttribute(ExaminerFilter.ATTR_EXAM_SECTION_NAME);
        return name != null ? String.valueOf(name) : null;
    }

    protected String getCallDestination(HttpSession session) {
        if (session == null) {
            return "Khu vực thi";
        }
        Object slotObj = session.getAttribute(ExaminerFilter.ATTR_SLOT);
        if (slotObj instanceof ExaminerSlotDTO) {
            ExaminerSlotDTO slot = (ExaminerSlotDTO) slotObj;
            if (slot.getAreaName() != null && !slot.getAreaName().isBlank()) {
                return slot.getAreaName();
            }
        }
        Object sectionName = session.getAttribute(ExaminerFilter.ATTR_EXAM_SECTION_NAME);
        if (sectionName != null && !String.valueOf(sectionName).isBlank()) {
            return String.valueOf(sectionName);
        }
        return "Khu vực thi thực hành";
    }

    protected static void applyModelAttributes(HttpServletRequest request, java.util.Map<String, Object> data) {
        if (data == null) {
            return;
        }
        for (java.util.Map.Entry<String, Object> entry : data.entrySet()) {
            request.setAttribute(entry.getKey(), entry.getValue());
        }
    }

    protected CallCandidateCommand buildCallCommand(HttpSession session, User user, int sessionId,
            Integer sbd, int[] sbds, boolean scoreEntry) {
        CallCandidateCommand command = new CallCandidateCommand();
        command.setSessionId(sessionId);
        command.setSbd(sbd);
        command.setSbds(sbds);
        command.setUser(user);
        command.setActionUserId(user != null ? user.getUserId() : null);
        ExamSection examSection = getExamSection(session);
        command.setExamSection(examSection);
        command.setTheory(examSection == ExamSection.THEORY);
        command.setSectionName(examSection.getValue());
        command.setCallDestination(getCallDestination(session));
        command.setScoreEntry(scoreEntry);
        return command;
    }

    protected CandidateSessionCommand buildSessionCommand(int sessionId, int sbd, Integer actionUserId) {
        CandidateSessionCommand command = new CandidateSessionCommand();
        command.setSessionId(sessionId);
        command.setSbd(sbd);
        command.setActionUserId(actionUserId);
        return command;
    }

    protected CandidateSessionCommand buildSessionCommand(int sessionId, int sbd, Integer actionUserId,
            Boolean sectionPassedHint) {
        CandidateSessionCommand command = buildSessionCommand(sessionId, sbd, actionUserId);
        command.setSectionPassedHint(sectionPassedHint);
        return command;
    }

    protected CandidateSessionCommand buildFinalizeCommand(int sessionId, int sbd, Integer actionUserId,
            String sectionKeyword) {
        CandidateSessionCommand command = buildSessionCommand(sessionId, sbd, actionUserId);
        command.setSectionKeyword(sectionKeyword);
        return command;
    }

    protected AdjustScoreDeductionCommand buildAdjustDeductionCommand(int sessionId, int sbd,
            int deductionId, int delta, Integer actionUserId) {
        AdjustScoreDeductionCommand command = new AdjustScoreDeductionCommand();
        command.setSessionId(sessionId);
        command.setSbd(sbd);
        command.setDeductionId(deductionId);
        command.setDelta(delta);
        command.setActionUserId(actionUserId);
        return command;
    }

    protected DeviceActionCommand buildDeviceActionCommand(int deviceId, Integer actionUserId) {
        DeviceActionCommand command = new DeviceActionCommand();
        command.setDeviceId(deviceId);
        command.setActionUserId(actionUserId);
        return command;
    }

    protected RecordViolationCommand buildViolationCommand(HttpSession session, int sessionId, int sbd,
            User user, String reasonCode, String reasonDetail, String evidencePath, int[] deductionIds) {
        RecordViolationCommand command = new RecordViolationCommand();
        command.setSessionId(sessionId);
        command.setSbd(sbd);
        command.setReasonCode(reasonCode);
        command.setReasonDetail(reasonDetail);
        command.setEvidencePath(evidencePath);
        command.setDeductionIds(deductionIds);
        command.setActionUserId(user != null ? user.getUserId() : null);
        ExamSection examSection = getExamSection(session);
        command.setTheory(examSection == ExamSection.THEORY);
        command.setSectionName(examSection.getValue());
        return command;
    }

    protected ScoreEditCommand buildScoreEditCommand(int sessionId, int sbd, Integer newScore,
            String reasonCode, String reasonDetail, User user, String password, Integer actionUserId) {
        ScoreEditCommand command = new ScoreEditCommand();
        command.setSessionId(sessionId);
        command.setSbd(sbd);
        command.setNewScore(newScore);
        command.setReasonCode(reasonCode);
        command.setReasonDetail(reasonDetail);
        command.setUser(user);
        command.setPassword(password);
        command.setActionUserId(actionUserId);
        return command;
    }
}

package controller.staff.exam;

import jakarta.servlet.http.HttpServlet;
import dto.SessionViewDTO;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import service.SessionService;

import java.util.List;

public abstract class BaseStaffExamServlet extends HttpServlet {

    protected int readSessionId(HttpServletRequest request, HttpSession httpSession, SessionService sessionService) {
        int paramId = parsePositiveInt(request.getParameter("sessionId"));
        if (paramId > 0) {
            return paramId;
        }
        Integer selected = (Integer) httpSession.getAttribute("selectedSessionId");
        if (selected != null && selected > 0) {
            return selected;
        }
        int active = sessionService.getActiveSessionId();
        if (active > 0) {
            return active;
        }
        List<SessionViewDTO> activeSessions = sessionService.getActiveSessions();
        if (!activeSessions.isEmpty()) {
            return activeSessions.get(0).getId();
        }
        List<SessionViewDTO> all = sessionService.getAllSessions();
        if (!all.isEmpty()) {
            return all.get(0).getId();
        }
        return 0;
    }

    protected int parsePositiveInt(String raw) {
        if (raw == null || raw.isBlank()) {
            return 0;
        }
        try {
            int value = Integer.parseInt(raw.trim());
            return value > 0 ? value : 0;
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    protected int readStaffUserId(HttpSession httpSession) {
        model.User user = (model.User) httpSession.getAttribute("user");
        return (user != null && user.getUserId() > 0) ? user.getUserId() : 0;
    }
}

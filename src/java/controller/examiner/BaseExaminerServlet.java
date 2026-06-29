package controller.examiner;

import java.net.*;
import java.nio.charset.*;

import dto.*;


import service.ExaminerActionsService;
import service.ExaminerSessionContextService;
import service.impl.ExaminerActionsServiceImpl;

import service.impl.ExaminerDataServiceImpl;

import enums.SectionType;
import dto.ExaminerSlotDTO;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import service.ExaminerDataService;

// Base servlet containing shared dependencies and utilities for the Examiner portal.
public abstract class BaseExaminerServlet extends HttpServlet {

    // Services required for data fetching and logic execution
    protected final ExaminerDataService viewDataService = new ExaminerDataServiceImpl();
    protected final ExaminerActionsService examinerService = new ExaminerActionsServiceImpl();

    // Ensures a valid HTTP session exists, otherwise sends 401 Unauthorized.
    protected HttpSession requireSession(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        HttpSession session = request.getSession(false);
        if (session == null) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "BÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â¡ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚ÂºÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â¡n cÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â¡ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚ÂºÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â§n ÃƒÆ’Ã†â€™ÃƒÂ¢Ã¢â€šÂ¬Ã…Â¾ÃƒÆ’Ã‚Â¢ÃƒÂ¢Ã¢â‚¬Å¡Ã‚Â¬Ãƒâ€¹Ã…â€œÃƒÆ’Ã†â€™ÃƒÂ¢Ã¢â€šÂ¬Ã…Â¾ÃƒÆ’Ã¢â‚¬Â ÃƒÂ¢Ã¢â€šÂ¬Ã¢â€žÂ¢ng nhÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â¡ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚ÂºÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â­p.");
        }
        return session;
    }

    // Returns the active session ID from the session context.
    protected Integer activeSessionId(HttpSession session) {
        return (Integer) session.getAttribute(ExaminerSessionContextService.ATTR_ACTIVE_SESSION_ID);
    }

    // Identifies if the current session section is of Theory type
    protected boolean isTheorySection(HttpServletRequest request) {
        Object value = request.getAttribute(ExaminerSessionContextService.ATTR_SECTION_TYPE);
        if (value instanceof SectionType) {
            return value == SectionType.THEORY;
        }
        return Boolean.TRUE.equals(request.getAttribute(ExaminerSessionContextService.ATTR_SECTION_THEORY));
    }

    protected SectionType resolveSectionType(HttpSession session) {
        if (session == null) return SectionType.THEORY;
        Object value = session.getAttribute(ExaminerSessionContextService.ATTR_SECTION_TYPE);
        if (value instanceof SectionType) {
            return (SectionType) value;
        }
        return SectionType.THEORY;
    }

    protected String resolveSectionName(HttpSession session) {
        if (session == null) return null;
        Object slotObj = session.getAttribute(ExaminerSessionContextService.ATTR_SLOT);
        if (slotObj instanceof ExaminerSlotDTO) {
            return ((ExaminerSlotDTO) slotObj).getExamTypeName();
        }
        Object name = session.getAttribute(ExaminerSessionContextService.ATTR_EXAM_SECTION_NAME);
        return name != null ? String.valueOf(name) : null;
    }

    protected String resolveCallDestination(HttpSession session) {
        if (session == null) return "Khu vÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â¡ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â»ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â±c thi";
        Object slotObj = session.getAttribute(ExaminerSessionContextService.ATTR_SLOT);
        if (slotObj instanceof ExaminerSlotDTO slot && slot.getAreaName() != null && !slot.getAreaName().isBlank()) {
            return slot.getAreaName();
        }
        Object sectionName = session.getAttribute(ExaminerSessionContextService.ATTR_EXAM_SECTION_NAME);
        if (sectionName != null && !String.valueOf(sectionName).isBlank()) {
            return String.valueOf(sectionName);
        }
        return "Khu vÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â¡ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â»ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â±c thi thÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â¡ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â»ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â±c hÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â nh";
    }

    // Helper to forward the request to a JSP.
    protected void forward(HttpServletRequest request, HttpServletResponse response, String jsp)
            throws ServletException, IOException {
        request.getRequestDispatcher(jsp).forward(request, response);
    }

    // Helper to send an HTTP redirect.
    protected void redirect(HttpServletResponse response, HttpServletRequest request, String path)
            throws IOException {
        response.sendRedirect(request.getContextPath() + path);
    }

    // Strips the context path from the request URI.
    protected String stripContextPath(HttpServletRequest request) {
        String uri = request.getRequestURI();
        String ctx = request.getContextPath();
        if (ctx != null && !ctx.isEmpty() && uri.startsWith(ctx)) {
            return uri.substring(ctx.length());
        }
        return uri;
    }

    // URL-encodes a string parameter for redirect URLs.
    protected String urlEncode(String value) {
        if (value == null) {
            return "";
        }
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }

    // Parses an array of deduction ID strings into an int array.
    protected int[] parseDeductionIds(String[] values) {
        if (values == null || values.length == 0) {
            return new int[0];
        }
        int[] ids = new int[values.length];
        int count = 0;
        for (String value : values) {
            try {
                int id = Integer.parseInt(value.trim());
                if (id > 0) {
                    ids[count++] = id;
                }
            } catch (NumberFormatException ignored) {
            }
        }
        if (count == ids.length) {
            return ids;
        }
        int[] trimmed = new int[count];
        System.arraycopy(ids, 0, trimmed, 0, count);
        return trimmed;
    }
}










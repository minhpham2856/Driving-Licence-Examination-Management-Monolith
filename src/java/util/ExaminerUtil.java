package util;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import enums.SectionType;
import dto.ExaminerSlotDTO;
import service.ExaminerSessionContextService;

public class ExaminerUtil {

    public static HttpSession requireSession(HttpServletRequest request, HttpServletResponse response) throws IOException {
        HttpSession session = request.getSession(false);
        if (session == null) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
        }
        return session;
    }

    public static Integer activeSessionId(HttpSession session) {
        if (session == null) return null;
        return (Integer) session.getAttribute(ExaminerSessionContextService.ATTR_ACTIVE_SESSION_ID);
    }

    public static boolean isTheorySection(HttpServletRequest request) {
        Object value = request.getAttribute(ExaminerSessionContextService.ATTR_SECTION_TYPE);
        if (value instanceof SectionType) {
            return value == SectionType.THEORY;
        }
        return Boolean.TRUE.equals(request.getAttribute(ExaminerSessionContextService.ATTR_SECTION_THEORY));
    }

    public static SectionType resolveSectionType(HttpSession session) {
        if (session == null) return SectionType.THEORY;
        Object value = session.getAttribute(ExaminerSessionContextService.ATTR_SECTION_TYPE);
        if (value instanceof SectionType) {
            return (SectionType) value;
        }
        return SectionType.THEORY;
    }

    public static String resolveSectionName(HttpSession session) {
        if (session == null) return null;
        Object slotObj = session.getAttribute(ExaminerSessionContextService.ATTR_SLOT);
        if (slotObj instanceof ExaminerSlotDTO) {
            return ((ExaminerSlotDTO) slotObj).getExamTypeName();
        }
        Object name = session.getAttribute(ExaminerSessionContextService.ATTR_EXAM_SECTION_NAME);
        return name != null ? String.valueOf(name) : null;
    }

    public static String resolveCallDestination(HttpSession session) {
        if (session == null) return "Khu vực thi";
        Object slotObj = session.getAttribute(ExaminerSessionContextService.ATTR_SLOT);
        if (slotObj instanceof ExaminerSlotDTO slot && slot.getAreaName() != null && !slot.getAreaName().isBlank()) {
            return slot.getAreaName();
        }
        Object sectionName = session.getAttribute(ExaminerSessionContextService.ATTR_EXAM_SECTION_NAME);
        if (sectionName != null && !String.valueOf(sectionName).isBlank()) {
            return String.valueOf(sectionName);
        }
        return "Khu vực thi thực hành";
    }

    public static String stripContextPath(HttpServletRequest request) {
        String uri = request.getRequestURI();
        String ctx = request.getContextPath();
        if (ctx != null && !ctx.isEmpty() && uri.startsWith(ctx)) {
            return uri.substring(ctx.length());
        }
        return uri;
    }

    public static String urlEncode(String value) {
        if (value == null) {
            return "";
        }
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }

    public static int[] parseDeductionIds(String[] values) {
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

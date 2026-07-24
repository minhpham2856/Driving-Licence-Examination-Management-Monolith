package examiner.util;

import examiner.dto.CandidateRowDTO;
import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import shared.Attributes;

// Shared pagination, search, and sort wiring for examiner candidate list screens.
public final class ListUtil {

    private ListUtil() {
    }

    // Apply sort and search for examiner workflow.
    public static void applySortAndSearch(HttpServletRequest request, List<CandidateRowDTO> candidates) {
        if (candidates == null) {
            return;
        }
        String search = request.getParameter("q");
        if (search != null && !search.isBlank()) {
            request.setAttribute(Attributes.Examiner.SEARCH_ACTIVE, true);
            request.setAttribute(Attributes.Request.SEARCH_QUERY, search.trim());
        }
        SortUtil.Spec spec = SortUtil.parse(
                request.getParameter("sort"), request.getParameter("dir"));
        SortUtil.sort(candidates, spec);
        request.setAttribute(Attributes.Request.SORT_BY, spec.getColumn());
        request.setAttribute(Attributes.Request.SORT_DIR, spec.isAscending() ? "asc" : "desc");
    }

    // Normalize search for examiner workflow.
    public static String normalizeSearch(String search) {
        if (search == null || search.isBlank()) {
            return null;
        }
        return search.trim();
    }
}

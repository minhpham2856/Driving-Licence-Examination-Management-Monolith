package controller.landing;

import service.LicenceService;
import service.impl.LicenceServiceImpl;
import model.Licence;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

@WebServlet(urlPatterns = {"/license-categories", "/license-grades"})
public class LicenseCategoriesServlet extends HttpServlet {

    private static final List<String> SORT_COLUMNS
            = List.of("licenceClass", "minimumAge", "validForYears");
    private final LicenceService licenceService = new LicenceServiceImpl();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String keyword = text(request.getParameter("q"));
        String sortBy = text(request.getParameter("sortBy"));
        String sortDir = text(request.getParameter("sortDir"));

        if (sortBy.isEmpty() || !SORT_COLUMNS.contains(sortBy)) {
            sortBy = "licenceClass";
        }

        if (sortDir.isEmpty()) {
            sortDir = "asc";
        }

        List<Licence> licences = licenceService.search(keyword.isEmpty() ? null : keyword);
        
        // anonymous params
        String finalSortBy = sortBy;
        String finalSortDir = sortDir;
        Collections.sort(licences, (Licence a, Licence b) -> {
            int cmp = 0;
            switch (finalSortBy) {
                case "minimumAge":
                    cmp = Integer.compare(a.getMinimumAge(), b.getMinimumAge());
                    break;
                case "validForYears":
                    cmp = Integer.compare(a.getValidForYears(), b.getValidForYears());
                    break;
                default:
                    cmp = a.getLicenceClass().compareToIgnoreCase(b.getLicenceClass());
                    break;
            }
            return "desc".equals(finalSortDir) ? -cmp : cmp;
        });
        request.setAttribute("licences", licences);
        request.setAttribute("searchQuery", keyword);
        request.setAttribute("sortBy", sortBy);
        request.setAttribute("sortDir", sortDir);
        request.getRequestDispatcher("/views/landing/license-categories.jsp").forward(request, response);
    }

    private static String text(String str) {
        return str == null ? "" : str.trim();
    }
}

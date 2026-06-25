package Controllers.Landing;

import DAOs.LicenceDAO;
import DAOs.Impl.LicenceDAOImpl;
import Models.Licence;
import Utils.Sanitize;
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

    private static final List<String> ALLOWED_SORT_COLUMNS =
        List.of("licenceClass", "minimumAge", "validForYears");

    private final LicenceDAO licenceDAO = new LicenceDAOImpl();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String keyword = Sanitize.text(request.getParameter("q"));
        String sortBy = Sanitize.text(request.getParameter("sortBy"));
        String sortDir = Sanitize.text(request.getParameter("sortDir"));

        if (sortBy.isEmpty() || !ALLOWED_SORT_COLUMNS.contains(sortBy)) {
            sortBy = "licenceClass";
        }
        if (sortDir.isEmpty()) {
            sortDir = "asc";
        }

        List<Licence> licences = licenceDAO.search(keyword.isEmpty() ? null : keyword);

        String finalSortBy = sortBy;
        String finalSortDir = sortDir;
        Collections.sort(licences, new Comparator<Licence>() {
            @Override
            public int compare(Licence a, Licence b) {
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
            }
        });

        request.setAttribute("licences", licences);
        request.setAttribute("searchQuery", keyword);
        request.setAttribute("sortBy", sortBy);
        request.setAttribute("sortDir", sortDir);
        request.getRequestDispatcher("/views/landing/license-categories.jsp").forward(request, response);
    }
}

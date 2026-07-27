package general.controller;

import general.dto.LicenceSearchCriteriaDTO;
import general.dto.ServiceResult;
import shared.Attributes;
import shared.model.Licence;
import general.service.LicenceService;
import general.service.impl.LicenseServiceImpl;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

@WebServlet("/license-categories")
public class LicenceCategoriesServlet extends HttpServlet {

    private final LicenceService licenceService = new LicenseServiceImpl();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        LicenceSearchCriteriaDTO criteria = buildCriteria(request);
        ServiceResult<List<Licence>> result = licenceService.searchLicenceCategories(criteria);

        if (result.isSuccess()) {
            request.setAttribute(Attributes.Request.LICENCES, result.getData());
        } else {
            request.setAttribute(Attributes.Request.ERROR, result.getMessage());
        }

        request.setAttribute(Attributes.Request.SEARCH_QUERY, criteria.getKeyword());
        request.setAttribute(Attributes.Request.SORT_BY, criteria.getSortBy());
        request.setAttribute(Attributes.Request.SORT_DIR, criteria.getSortDir());

        request.getRequestDispatcher("/views/general/license-categories.jsp").forward(request, response);
    }

    private LicenceSearchCriteriaDTO buildCriteria(HttpServletRequest request) {
        LicenceSearchCriteriaDTO criteria = new LicenceSearchCriteriaDTO();

        String keyword = request.getParameter("q");
        if (keyword != null && !keyword.isBlank()) {
            criteria.setKeyword(keyword.trim());
        }

        String[] durations = request.getParameterValues("duration");
        if (durations != null && durations.length > 0) {
            criteria.setDurations(Arrays.asList(durations));
        }

        String sortBy = request.getParameter("sortBy");
        if (sortBy != null && !sortBy.isBlank()) {
            criteria.setSortBy(sortBy.trim());
        }

        String sortDir = request.getParameter("sortDir");
        if (sortDir != null && !sortDir.isBlank()) {
            criteria.setSortDir(sortDir.trim());
        }

        return criteria;
    }
}

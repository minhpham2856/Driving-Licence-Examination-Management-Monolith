package general.controller;

import general.dao.LicenceFeeDAO;
import general.dao.impl.LicenceFeeDAOImpl;
import general.dto.LicenceSearchCriteriaDTO;
import general.dto.ServiceResult;
import shared.Attributes;
import shared.model.Licence;
import shared.model.LicenceFee;
import general.service.LicenceService;
import general.service.impl.LicenseServiceImpl;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@WebServlet("/license-categories")
public class LicenceCategoriesServlet extends HttpServlet {

    private final LicenceService licenceService = new LicenseServiceImpl();
    private final LicenceFeeDAO licenceFeeDAO = new LicenceFeeDAOImpl();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        LicenceSearchCriteriaDTO criteria = buildCriteria(request);
        ServiceResult<List<Licence>> result = licenceService.searchLicenceCategories(criteria);

        if (result.isSuccess()) {
            List<Licence> licences = result.getData();
            request.setAttribute(Attributes.Request.LICENCES, licences);
            request.setAttribute("licenceFeesByLicenceId", buildFeesByLicence(licences));
        } else {
            request.setAttribute(Attributes.Request.ERROR, result.getMessage());
        }

        request.setAttribute(Attributes.Request.SEARCH_QUERY, criteria.getKeyword());
        request.setAttribute(Attributes.Request.SORT_BY, criteria.getSortBy());
        request.setAttribute(Attributes.Request.SORT_DIR, criteria.getSortDir());

        request.getRequestDispatcher("/views/general/license-categories.jsp").forward(request, response);
    }

    private Map<Integer, List<LicenceFee>> buildFeesByLicence(List<Licence> licences) {
        Map<Integer, List<LicenceFee>> grouped = licenceFeeDAO.getAllGroupedByLicenceId();
        Map<Integer, List<LicenceFee>> byLicence = new HashMap<>();
        if (licences == null) {
            return byLicence;
        }
        for (Licence licence : licences) {
            if (licence == null) {
                continue;
            }
            List<LicenceFee> specific = grouped.get(licence.getLicenceId());
            byLicence.put(licence.getLicenceId(),
                    specific != null ? specific : new ArrayList<>());
        }
        return byLicence;
    }

    private LicenceSearchCriteriaDTO buildCriteria(HttpServletRequest request) {
        LicenceSearchCriteriaDTO criteria = new LicenceSearchCriteriaDTO();

        String keyword = request.getParameter("q");
        if (keyword != null && !keyword.isBlank()) {
            criteria.setKeyword(keyword.trim());
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

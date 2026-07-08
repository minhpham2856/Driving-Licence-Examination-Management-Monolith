package controller.staff.exam;



import dto.CandidateRowDTO;

import dto.EnrollmentDTO;

import dto.SessionViewDTO;

import enums.CandidateStatus;

import jakarta.servlet.ServletException;

import jakarta.servlet.annotation.WebServlet;

import jakarta.servlet.http.HttpServletRequest;

import jakarta.servlet.http.HttpServletResponse;

import jakarta.servlet.http.HttpSession;

import service.RegistrationService;

import service.SessionService;

import service.impl.RegistrationServiceImpl;

import service.impl.SessionServiceImpl;

import util.FormatUtil;



import java.io.IOException;

import java.util.ArrayList;

import java.util.List;



@WebServlet("/views/staff/exam/candidates")

public class CandidateListServlet extends BaseStaffExamServlet {



    private final RegistrationService regService = new RegistrationServiceImpl();

    private final SessionService sessionService = new SessionServiceImpl();



    @Override

    protected void doGet(HttpServletRequest request, HttpServletResponse response)

            throws ServletException, IOException {

        HttpSession session = request.getSession();

        int sessionId = readSessionId(request, session, sessionService);

        if (sessionId > 0) {

            session.setAttribute("selectedSessionId", sessionId);

        }

        SessionViewDTO sessionView = sessionId > 0 ? sessionService.getSessionById(sessionId) : null;

        String shiftLabel = sessionView != null ? sessionView.getCaLabel() : "";

        String licenseCode = sessionView != null && sessionView.getLicenseCode() != null

                ? sessionView.getLicenseCode() : "";



        String keyword = FormatUtil.text(request.getParameter("searchKeyword"));

        List<EnrollmentDTO> enrollments = sessionId > 0

                ? regService.getCandidatesBySession(sessionId) : new ArrayList<>();

        List<CandidateRowDTO> examinees = new ArrayList<>();

        for (EnrollmentDTO row : enrollments) {

            if (keyword != null && !keyword.isBlank()) {

                String kw = keyword.toLowerCase();

                String haystack = (row.getFullName() + " " + row.getGovIdNo() + " " + row.getSbd()).toLowerCase();

                if (!haystack.contains(kw)) {

                    continue;

                }

            }

            CandidateRowDTO item = new CandidateRowDTO();

            item.setEnrollmentId(row.getId());

            item.setSbd(row.getSbd());

            item.setFullName(row.getFullName());

            item.setUsername("sbd" + row.getSbd());

            item.setCccd(row.getGovIdNo());

            item.setLicenseClass(licenseCode);

            item.setCaLabel(shiftLabel);

            String sectionStatus = row.getSectionStatus();

            item.setStatus(sectionStatus != null ? sectionStatus : CandidateStatus.NOT_STARTED.getValue());

            item.setStatusKey(mapStatusKey(sectionStatus));

            examinees.add(item);

        }

        request.setAttribute("examinees", examinees);

        request.setAttribute("totalExaminees", examinees.size());

        request.getRequestDispatcher("/views/staff/exam/candidatelist.jsp").forward(request, response);

    }



    private static String mapStatusKey(String sectionStatus) {

        if (sectionStatus == null) {

            return "info";

        }

        CandidateStatus status = CandidateStatus.fromValue(sectionStatus);

        if (status == CandidateStatus.COMPLETED) {

            return "success";

        }

        if (status == CandidateStatus.IN_PROGRESS) {

            return "info";

        }

        if (status == CandidateStatus.AWAITING_SIGNATURE) {

            return "warning";

        }

        return "info";

    }

}


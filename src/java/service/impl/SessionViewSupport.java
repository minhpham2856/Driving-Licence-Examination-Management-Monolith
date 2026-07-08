package service.impl;

import dao.ExamAreaDAO;
import dao.ExamDAO;
import dao.ExamSectionDAO;
import dao.LicenceDAO;
import dao.SessionDAO;
import dao.impl.ExamAreaDAOImpl;
import dao.impl.ExamDAOImpl;
import dao.impl.ExamSectionDAOImpl;
import dao.impl.LicenceDAOImpl;
import dao.impl.SessionDAOImpl;
import dto.SessionDTO;
import model.Exam;
import model.ExamArea;
import model.ExamSection;
import model.Licence;
import model.Session;
import util.examstaff.SessionLabel;
import java.sql.Date;
import java.sql.Time;
import java.util.ArrayList;
import java.util.List;

public final class SessionViewSupport {

    private final SessionDAO sessionDAO = new SessionDAOImpl();
    private final ExamDAO examDAO = new ExamDAOImpl();
    private final LicenceDAO licenceDAO = new LicenceDAOImpl();
    private final ExamAreaDAO areaDAO = new ExamAreaDAOImpl();
    private final ExamSectionDAO sectionDAO = new ExamSectionDAOImpl();

    public SessionDTO toDto(int sessionId) {
        return toDto(sessionDAO.getById(sessionId));
    }

    public SessionDTO toDto(Session session) {
        if (session == null) {
            return null;
        }
        SessionDTO dto = new SessionDTO();
        dto.setId(session.getId());
        dto.setExamId(session.getExamId());
        dto.setMorningSession(session.isMorningSession());
        dto.setStatus(session.getStatus());
        if (session.getStartTime() != null) {
            dto.setExamDate(new Date(session.getStartTime().getTime()));
            dto.setShiftStartTime(new Time(session.getStartTime().getTime()));
            dto.setCreatedAt(session.getStartTime());
        }
        if (session.getEndTime() != null) {
            dto.setShiftEndTime(new Time(session.getEndTime().getTime()));
        }
        Exam exam = examDAO.getById(session.getExamId());
        if (exam != null) {
            dto.setExamCode(exam.getExamCode());
            dto.setLicenseTypeId(exam.getLicenceId());
            Licence licence = licenceDAO.getById(exam.getLicenceId());
            if (licence != null) {
                dto.setLicenseCode(licence.getLicenceClass());
            }
        }
        List<Integer> areaIds = sessionDAO.getExamAreaIds(session.getId());
        int areaId = areaIds.isEmpty() ? 0 : areaIds.get(0);
        dto.setAreaId(areaId);
        if (areaId > 0) {
            ExamArea area = areaDAO.getById(areaId);
            if (area != null) {
                dto.setAreaName(area.getAreaName());
                if (area.getCapacity() != null) {
                    dto.setMaxCandidates(area.getCapacity());
                } else {
                    dto.setMaxCandidates(100);
                }
            }
        } else {
            dto.setMaxCandidates(100);
        }
        String sectionName = null;
        Integer sectionId = sessionDAO.getExamSectionId(session.getId());
        if (sectionId != null) {
            ExamSection section = sectionDAO.findById(sectionId);
            if (section != null) {
                sectionName = section.getSectionName();
                dto.setExamTypeName(sectionName);
                dto.setExamTypeId(enums.ExamSection.resolveExamTypeId(sectionName));
            }
        }
        if (dto.getExamTypeId() == 0) {
            dto.setExamTypeId(1);
        }
        dto.setSessionName(SessionLabel.of(session.isMorningSession(), sectionName));
        dto.setRegisteredCount(sessionDAO.countEnrollments(session.getId()));
        return dto;
    }

    public List<SessionDTO> toDtoList(List<Session> sessions) {
        List<SessionDTO> list = new ArrayList<>();
        if (sessions == null) {
            return list;
        }
        for (Session session : sessions) {
            SessionDTO dto = toDto(session);
            if (dto != null) {
                list.add(dto);
            }
        }
        return list;
    }
}

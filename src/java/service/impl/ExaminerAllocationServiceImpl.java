
package service.impl;
import dto.*;
import model.*;

import java.util.*;


import dao.ExamAreaDAO;
import dao.ExamDeviceDAO;

import dao.SessionDAO;
import dao.ExaminerScheduleDAO;
import dao.RoleDAO;
import dao.UserDAO;
import dao.impl.ExamAreaDAOImpl;
import dao.impl.ExamDeviceDAOImpl;

import dao.impl.SessionDAOImpl;
import dao.impl.ExaminerScheduleDAOImpl;

import dto.AutoAllocateResultDTO;



import dto.ExaminerSlotDTO;

import dto.SessionDTO;

import dto.UserDTO;

import model.ExamArea;
import model.ExamDevice;
import service.ExaminerAllocationService;

import java.sql.Date;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ExaminerAllocationServiceImpl implements ExaminerAllocationService {

    private final SessionDAO sessionDAO = new SessionDAOImpl();
    private final ExamAreaDAO areaDAO = new ExamAreaDAOImpl();
    private final ExamDeviceDAO deviceDAO = new ExamDeviceDAOImpl();
    private final ExaminerScheduleDAO assignmentDAO = new ExaminerScheduleDAOImpl();
    

    @Override
    public List<SessionDTO> getAllSessions() {
        return sessionDAO.getAllSessions();
    }

    @Override
    public SessionDTO getSessionById(int sessionId) {
        return sessionDAO.getDtoById(sessionId);
    }

    @Override
    public List<SessionDTO> getSessionsByExamDate(Date date) {
        return sessionDAO.getSessionsByExamDate(date);
    }

    @Override
    public List<ExamArea> getAreasBySessionId(int sessionId) {
        return areaDAO.getAreasBySessionId(sessionId);
    }

    @Override
    public ExamArea getAreaById(int id) {
        return areaDAO.getById(id);
    }

    @Override
    public List<ExamDevice> getDevicesByAreaId(int areaId) {
        return deviceDAO.getDevicesByAreaId(areaId);
    }

    @Override
    public List<UserDTO> getActiveExaminers() {
        return assignmentDAO.getActiveExaminers();
    }

    @Override
    public boolean isAreaInSession(int sessionId, int areaId) {
        return areaDAO.isAreaInSession(sessionId, areaId);
    }

    @Override
    public List<ExaminerSlotDTO> getAssignmentsByExamDate(Date date, Map<Integer, Date> sessionDates) {
        return assignmentDAO.getByExamDate(date, sessionDates);
    }

    @Override
    public List<ExaminerSlotDTO> getAssignmentsBySessionId(int sessionId) {
        return new ArrayList<>(); /* return assignmentDAO.getBySessionId(sessionId); */
    }

    @Override
    public Set<Integer> getBusyExaminerIds(Date examDate, Map<Integer, Date> sessionDates) {
        return assignmentDAO.getBusyExaminerIds(examDate, sessionDates);
    }

    @Override
    public boolean assignExaminer(ExaminerSlotDTO slot) {
        return assignmentDAO.assign(slot);
    }

    @Override
    public boolean removeAssignment(String slotKey) {
        return assignmentDAO.remove(slotKey);
    }

        @Override
    public AutoAllocateResultDTO autoAllocateSession(int sessionId) {
        AutoAllocateResultDTO result = new AutoAllocateResultDTO();
        result.errorMsg = "Feature is being updated to be compatible with the new system.";
        return result;
    }

    @Override
    public AutoAllocateResultDTO autoAllocateCandidate(int sessionId, int registrationId) {
        AutoAllocateResultDTO result = new AutoAllocateResultDTO();
        result.errorMsg = "Feature is being updated to be compatible with the new system.";
        return result;
    }
}



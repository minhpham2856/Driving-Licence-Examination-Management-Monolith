package service;



import dto.EnrollmentDTO;

import dto.ServiceResult;

import dto.UploadRowDTO;

import model.Payment;

import model.Profile;

import model.User;



import java.sql.Date;

import java.util.List;

import java.util.Map;



public interface RegistrationService {



    EnrollmentDTO getBySessionAndSbd(int sessionId, int sbd);



    List<EnrollmentDTO> getCandidatesBySession(int sessionId);



    ServiceResult<Void> updateProfile(int candidateId, String fullName, Date dateOfBirth,

            String governmentIdNumber, String phoneNumber);



    ServiceResult<Void> updatePhoto(int candidateId, String photoUrl);



    ServiceResult<Void> markAbsent(int candidateId);



    ServiceResult<Void> clearAbsentMarking(int candidateId);



    ServiceResult<Void> markSuspended(int candidateId);



    ServiceResult<Void> undoSuspension(int candidateId);



    List<Map<String, Object>> findAppliedScoreDeductions(int candidateId, int sessionId);



    ServiceResult<Void> updateScores(int candidateId, Integer theoryScore, String theoryResult,

            Integer practicalScore, String practicalResult);



    ServiceResult<Void> updatePresent(int candidateId, boolean isPresent);



    ServiceResult<Void> updateAllocatedRoom(int candidateId, int areaId, String areaName);



    ServiceResult<Void> updatePayment(int candidateId, boolean isPaid);



    boolean insertPayment(Payment payment);



    EnrollmentDTO getById(int candidateId);



    Integer findCandidateIdByGovIdAndSession(String governmentIdNumber, int sessionId);



    ServiceResult<Void> insert(UploadRowDTO dto);



    boolean insertProfile(Profile profile);



    boolean updateProfile(Profile profile);



    Profile getProfileByGovId(String govId);



    boolean insertUser(User user);



    User getUserByUsername(String username);



    ServiceResult<Void> updateRoadScore(int candidateId, int score, String passed);

}


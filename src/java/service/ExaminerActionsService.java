package service;

import dto.CandidateEnrollmentDTO;
import dto.ServiceResult;
import dto.payload.AdjustScoreDeductionCommand;
import dto.payload.CallCandidateCommand;
import dto.payload.CandidateSessionCommand;
import dto.payload.ChangeCandidateVehicleCommand;
import dto.payload.DeviceActionCommand;
import dto.payload.RecordViolationCommand;
import dto.payload.ScoreEditCommand;
import dto.payload.UpdateCandidateProfileCommand;
import model.User;

public interface ExaminerActionsService {

    CandidateEnrollmentDTO getRegistration(int sessionId, int sbd);

    ServiceResult<Void> updateCandidateProfile(UpdateCandidateProfileCommand command);

    ServiceResult<Void> callCandidate(CallCandidateCommand command);

    ServiceResult<Integer> callNextCandidate(CallCandidateCommand command);

    ServiceResult<Integer> callSelectedCandidates(CallCandidateCommand command);

    ServiceResult<Void> callScoreEntryCandidate(CallCandidateCommand command);

    ServiceResult<Void> adjustScoreDeduction(AdjustScoreDeductionCommand command);

    ServiceResult<Void> finalizeScoreEntry(CandidateSessionCommand command);

    ServiceResult<Void> setDeviceMaintenance(DeviceActionCommand command);

    ServiceResult<Void> setDeviceAvailable(DeviceActionCommand command);

    ServiceResult<Void> changeCandidateVehicle(ChangeCandidateVehicleCommand command);

    ServiceResult<Void> updateTheoryScore(ScoreEditCommand command);

    ServiceResult<Void> logPracticalScoreEditReason(ScoreEditCommand command);

    ServiceResult<Void> recordViolation(RecordViolationCommand command);

    boolean verifyPassword(User user, String password);

    ServiceResult<Void> printSignatureForm(CandidateSessionCommand command);

    ServiceResult<Void> markPresent(CandidateSessionCommand command);

    ServiceResult<Void> undoPresent(CandidateSessionCommand command);

    ServiceResult<Void> sendWrongInfoToProcedure(CandidateSessionCommand command);

    ServiceResult<Void> completeCandidateSection(CandidateSessionCommand command);
}

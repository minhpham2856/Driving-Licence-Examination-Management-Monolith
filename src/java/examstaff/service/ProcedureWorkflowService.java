package examstaff.service;

import dto.ExamSummaryDTO;
import dto.exam.ExamRegistrationDTO;
import examstaff.dto.ProcedurePaymentOutcomeDTO;
import examstaff.dto.ProcedurePhotoSaveOutcomeDTO;
import examstaff.dto.ProcedureProfilePrepareResultDTO;
import examstaff.dto.ProcedureResetOutcomeDTO;

import java.sql.Date;
import java.util.List;

public interface ProcedureWorkflowService {

    ExamRegistrationDTO findProfile(String webRoot, int examId, int sessionId,
            String sbd, List<ExamRegistrationDTO> queue);

    ProcedureProfilePrepareResultDTO prepareProfileForDesk(String webRoot, int examId, int sessionId,
            ExamRegistrationDTO profile, List<ExamRegistrationDTO> queue);

    ExamRegistrationDTO reloadProfile(String webRoot, int examId, int candidateId,
            String sbd, List<ExamRegistrationDTO> queue);

    boolean saveProfile(int candidateId, String fullName, Date dob,
            String govIdNo, String email, String phoneNo);

    ExamRegistrationDTO recapturePhoto(int candidateId, String webRoot, int examId,
            String sbd, List<ExamRegistrationDTO> queue);

    ProcedurePhotoSaveOutcomeDTO saveCapturedPhoto(String webRoot, String sbd, int examId,
            String base64Data, List<ExamRegistrationDTO> queue);

    ProcedurePaymentOutcomeDTO confirmPayment(ExamRegistrationDTO profile, String sbd,
            int examId, String webRoot, List<ExamSummaryDTO> allSessions);

    ProcedureResetOutcomeDTO resetProcedure(String sbd, int examId, String webRoot);

    void clearAbsentAfterPayment(ExamRegistrationDTO profile);
}

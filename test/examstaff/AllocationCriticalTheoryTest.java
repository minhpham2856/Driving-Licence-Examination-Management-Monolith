//package examstaff;
//
//import examstaff.dto.ExamRegistrationDTO;
//import examstaff.dto.ExamStaffCandidate;
//import examstaff.service.impl.support.allocation.AllocationPassRules;
//import examstaff.util.ExamStaffCandidateMapper;
//import org.junit.Test;
//
//import static org.junit.Assert.assertEquals;
//import static org.junit.Assert.assertTrue;
//
//public class AllocationCriticalTheoryTest {
//
//    @Test
//    public void enoughScoreButWrongCriticalQuestionMustFail() {
//        ExamRegistrationDTO candidate = new ExamRegistrationDTO();
//        candidate.setLicenseCode("A1");
//        candidate.setTheoryScore(21);
//        candidate.setWrongCriticalTheory(true);
//
//        AllocationPassRules.applyToCandidate(candidate);
//
//        assertEquals("failed", candidate.getTheoryPassed());
//    }
//
//    @Test
//    public void enoughScoreWithoutWrongCriticalQuestionStillPasses() {
//        ExamRegistrationDTO candidate = new ExamRegistrationDTO();
//        candidate.setLicenseCode("A1");
//        candidate.setTheoryScore(21);
//
//        AllocationPassRules.applyToCandidate(candidate);
//
//        assertEquals("passed", candidate.getTheoryPassed());
//    }
//
//    @Test
//    public void candidateViewMapperPreservesWrongCriticalFlag() {
//        ExamStaffCandidate row = new ExamStaffCandidate();
//        row.setTheoryScore(21);
//        row.setWrongCriticalTheory(true);
//
//        ExamRegistrationDTO candidate = ExamStaffCandidateMapper.toDto(row);
//
//        assertTrue(candidate.hasWrongCriticalTheory());
//    }
//}

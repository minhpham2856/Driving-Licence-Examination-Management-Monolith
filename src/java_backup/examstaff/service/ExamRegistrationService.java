package examstaff.service;


import examstaff.dto.exam.ExamRegistrationDTO;

import shared.model.ExamRegistration;
import java.util.List;

/**
 * DAO cho thao tÃ¡c vá»›i Ä‘Äƒng kÃ½ thi (ExamRegistration).
 * Cung cáº¥p cÃ¡c phÆ°Æ¡ng thá»©c CRUD, cáº­p nháº­t Ä‘iá»ƒm danh, thanh toÃ¡n, thiáº¿t bá»‹,
 * Ä‘iá»ƒm sá»‘ vÃ  xá»­ lÃ½ nghiá»‡p vá»¥ Ä‘Äƒng kÃ½ tham gia ká»³ thi sÃ¡t háº¡ch lÃ¡i xe.
 * getById() tráº£ vá» DTO; findById() tráº£ vá» Model.
 */
public interface ExamRegistrationService {

    /**
     * Láº¥y thÃ´ng tin Ä‘Äƒng kÃ½ thi theo mÃ£, tráº£ vá» DTO.
     *
     * @param id mÃ£ Ä‘Äƒng kÃ½ thi
     * @return ExamRegistrationDTO, hoáº·c null náº¿u khÃ´ng tÃ¬m tháº¥y
     */
    ExamRegistrationDTO getById(int id);

    /**
     * Láº¥y thÃ´ng tin Ä‘Äƒng kÃ½ thi theo mÃ£, tráº£ vá» Model.
     *
     * @param id mÃ£ Ä‘Äƒng kÃ½ thi
     * @return ExamRegistration model, hoáº·c null náº¿u khÃ´ng tÃ¬m tháº¥y
     */
    ExamRegistration findById(int id);

    /**
     * Láº¥y thÃ´ng tin Ä‘Äƒng kÃ½ thi theo ká»³ thi vÃ  sá»‘ bÃ¡o danh.
     *
     * @param sessionId mÃ£ ká»³ thi
     * @param sbd       sá»‘ bÃ¡o danh
     * @return ExamRegistrationDTO, hoáº·c null náº¿u khÃ´ng tÃ¬m tháº¥y
     */
    ExamRegistrationDTO getBySessionAndSbd(int sessionId, String sbd);

    /**
     * Láº¥y danh sÃ¡ch thÃ­ sinh Ä‘Ã£ Ä‘Äƒng kÃ½ theo mÃ£ ká»³ thi.
     *
     * @param sessionId mÃ£ ká»³ thi
     * @return danh sÃ¡ch ExamRegistrationDTO
     */
    List<ExamRegistrationDTO> getCandidatesBySession(int sessionId);

    List<ExamRegistrationDTO> getCandidatesByExam(int examId);

    ExamRegistrationDTO getByExamAndSbd(int examId, String sbd);

    /**
     * Cáº­p nháº­t tráº¡ng thÃ¡i Ä‘iá»ƒm danh cho Ä‘Äƒng kÃ½ thi.
     *
     * @param id        mÃ£ Ä‘Äƒng kÃ½ thi
     * @param isPresent true náº¿u cÃ³ máº·t
     * @return true náº¿u cáº­p nháº­t thÃ nh cÃ´ng
     */
    boolean updatePresent(int id, boolean isPresent);

    /**
     * Cáº­p nháº­t tráº¡ng thÃ¡i thanh toÃ¡n cho Ä‘Äƒng kÃ½ thi.
     *
     * @param id                mÃ£ Ä‘Äƒng kÃ½ thi
     * @param isPaymentCompleted true náº¿u Ä‘Ã£ thanh toÃ¡n
     * @return true náº¿u cáº­p nháº­t thÃ nh cÃ´ng
     */
    boolean updatePayment(int id, boolean isPaymentCompleted);

    /**
     * Cáº­p nháº­t mÃ£ mÃ¡y tÃ­nh cho Ä‘Äƒng kÃ½ thi.
     *
     * @param id           mÃ£ Ä‘Äƒng kÃ½ thi
     * @param computerCode mÃ£ mÃ¡y tÃ­nh
     * @return true náº¿u cáº­p nháº­t thÃ nh cÃ´ng
     */
    boolean updateComputer(int id, String computerCode);

    /**
     * Cáº­p nháº­t phÃ²ng thi Ä‘Ã£ phÃ¢n cho Ä‘Äƒng kÃ½ thi.
     *
     * @param id       mÃ£ Ä‘Äƒng kÃ½ thi
     * @param areaId   mÃ£ khu vá»±c
     * @param areaName tÃªn khu vá»±c
     * @return true náº¿u cáº­p nháº­t thÃ nh cÃ´ng
     */
    boolean updateAllocatedRoom(int candidateId, int sessionId, int areaId, String areaName);

    boolean updatePracticalAllocatedRoom(int candidateId, int sessionId, int areaId, String areaName);

    /**
     * @return thÃ´ng bÃ¡o lá»—i náº¿u thÃ­ sinh Ä‘Ã£ cÃ³ phÃ²ng á»Ÿ ca khÃ¡c trong cÃ¹ng ká»³ thi; null náº¿u há»£p lá»‡
     */
    String validateUniqueTheoryAllocation(int candidateId, int sessionId);

    /**
     * Cáº­p nháº­t thiáº¿t bá»‹ cho Ä‘Äƒng kÃ½ thi.
     *
     * @param id         mÃ£ Ä‘Äƒng kÃ½ thi
     * @param deviceCode mÃ£ thiáº¿t bá»‹
     * @return true náº¿u cáº­p nháº­t thÃ nh cÃ´ng
     */
    boolean updateDevice(int id, String deviceCode);

    /**
     * Cáº­p nháº­t Ä‘iá»ƒm sá»‘ cho Ä‘Äƒng kÃ½ thi.
     *
     * @param id               mÃ£ Ä‘Äƒng kÃ½ thi
     * @param theoryScore      Ä‘iá»ƒm lÃ½ thuyáº¿t (cÃ³ thá»ƒ null)
     * @param theoryPassed     káº¿t quáº£ Ä‘á»—/trÆ°á»£t lÃ½ thuyáº¿t
     * @param practicalScore   Ä‘iá»ƒm thá»±c hÃ nh (cÃ³ thá»ƒ null)
     * @param practicalPassed  káº¿t quáº£ Ä‘á»—/trÆ°á»£t thá»±c hÃ nh
     * @return true náº¿u cáº­p nháº­t thÃ nh cÃ´ng
     */
    boolean updateScores(int id, Integer theoryScore, String theoryPassed, Integer practicalScore, String practicalPassed);

    boolean updateScores(int id, int sessionId, Integer theoryScore, String theoryPassed,
            Integer practicalScore, String practicalPassed);

    /**
     * Cáº­p nháº­t Ä‘iá»ƒm lÃ½ thuyáº¿t dÆ°á»›i dáº¡ng sá»‘ cÃ¢u Ä‘Ãºng (0â€“35) vá»›i ngÆ°á»¡ng Ä‘áº¡t.
     *
     * @param id            mÃ£ Ä‘Äƒng kÃ½ thi
     * @param correctCount  sá»‘ cÃ¢u tráº£ lá»i Ä‘Ãºng
     * @param passThreshold ngÆ°á»¡ng sá»‘ cÃ¢u Ä‘Ãºng tá»‘i thiá»ƒu Ä‘á»ƒ Ä‘áº¡t
     * @return true náº¿u cáº­p nháº­t thÃ nh cÃ´ng
     */
    boolean updateTheoryCorrectCount(int id, int correctCount, int passThreshold);

    /**
     * Cáº­p nháº­t thÃ´ng tin há»“ sÆ¡ cÆ¡ báº£n cá»§a Ä‘Äƒng kÃ½ thi.
     *
     * @param id       mÃ£ Ä‘Äƒng kÃ½ thi
     * @param fullName há» vÃ  tÃªn
     * @param dob      ngÃ y sinh
     * @param govIdNo  sá»‘ CMND/CCCD
     * @param email    Ä‘á»‹a chá»‰ email
     * @param phoneNo  sá»‘ Ä‘iá»‡n thoáº¡i
     * @return true náº¿u cáº­p nháº­t thÃ nh cÃ´ng
     */
    boolean updateProfile(int id, String fullName, java.sql.Date dob, String govIdNo, String email, String phoneNo);


    /**
     * Cáº­p nháº­t áº£nh cho Ä‘Äƒng kÃ½ thi.
     *
     * @param id       mÃ£ Ä‘Äƒng kÃ½ thi
     * @param photoUrl Ä‘Æ°á»ng dáº«n áº£nh
     * @return true náº¿u cáº­p nháº­t thÃ nh cÃ´ng
     */
    boolean updatePhoto(int id, String photoUrl);

    boolean clearCompletedPayments(int candidateId);

    /**
     * ThÃªm má»›i má»™t Ä‘Äƒng kÃ½ thi.
     *
     * @param reg Ä‘á»‘i tÆ°á»£ng ExamRegistrationDTO chá»©a thÃ´ng tin Ä‘Äƒng kÃ½
     * @return true náº¿u thÃªm thÃ nh cÃ´ng
     */
    boolean insert(ExamRegistrationDTO reg);

    /**
     * Láº¥y danh sÃ¡ch táº¥t cáº£ Ä‘Äƒng kÃ½ thi.
     *
     * @return danh sÃ¡ch táº¥t cáº£ ExamRegistrationDTO
     */
    List<ExamRegistrationDTO> getAllCandidates();

    /**
     * ÄÃ¡nh dáº¥u thÃ­ sinh váº¯ng máº·t trong Ä‘Äƒng kÃ½ thi.
     *
     * @param candidateId mÃ£ thÃ­ sinh
     * @return true náº¿u Ä‘Ã¡nh dáº¥u thÃ nh cÃ´ng
     */
    boolean markAbsent(int candidateId);

    /**
     * Há»§y Ä‘Ã¡nh dáº¥u váº¯ng máº·t cho thÃ­ sinh trong Ä‘Äƒng kÃ½ thi.
     *
     * @param candidateId mÃ£ thÃ­ sinh
     * @return true náº¿u há»§y thÃ nh cÃ´ng
     */
    boolean clearAbsentMarking(int candidateId);

    /**
     * TÃ¬m mÃ£ Ä‘Äƒng kÃ½ thi theo mÃ£ há»“ sÆ¡ vÃ  mÃ£ ká»³ thi.
     *
     * @param profileId mÃ£ há»“ sÆ¡
     * @param sessionId mÃ£ ká»³ thi
     * @return Integer mÃ£ Ä‘Äƒng kÃ½ thi, hoáº·c null náº¿u khÃ´ng tÃ¬m tháº¥y
     */
    Integer findCandidateIdByProfileAndSession(int profileId, int sessionId);

    /**
     * TÃ¬m CandidateId theo CCCD vÃ  ca thi.
     *
     * @param govId     sá»‘ CCCD
     * @param sessionId mÃ£ ca thi
     * @return CandidateId hoáº·c null
     */
    Integer findCandidateIdByGovIdAndSession(String govId, int sessionId);

    /**
     * Ãp dá»¥ng cÃ¡c khoáº£n trá»« Ä‘iá»ƒm cho má»™t pháº§n thi vÃ  tÃ­nh láº¡i ExamScore.
     *
     * @param candidateId    mÃ£ thÃ­ sinh
     * @param deductionIds   máº£ng mÃ£ cÃ¡c khoáº£n trá»« Ä‘iá»ƒm
     * @param sectionKeyword tá»« khÃ³a xÃ¡c Ä‘á»‹nh pháº§n thi (theory/practical)
     * @return true náº¿u Ã¡p dá»¥ng thÃ nh cÃ´ng
     */
    boolean applyScoreDeductions(int candidateId, int[] deductionIds, String sectionKeyword);

    /**
     * Äiá»u chá»‰nh sá»‘ láº§n xuáº¥t hiá»‡n (+1 / -1) cá»§a má»™t khoáº£n trá»« Ä‘iá»ƒm trong cháº¥m thá»±c hÃ nh.
     *
     * @param candidateId mÃ£ thÃ­ sinh
     * @param sessionId   mÃ£ ká»³ thi
     * @param deductionId mÃ£ khoáº£n trá»« Ä‘iá»ƒm
     * @param delta       giÃ¡ trá»‹ Ä‘iá»u chá»‰nh (+1 hoáº·c -1)
     * @return true náº¿u Ä‘iá»u chá»‰nh thÃ nh cÃ´ng
     */
    boolean adjustScoreDeductionOccurrence(int candidateId, int sessionId, int deductionId, int delta);

    /**
     * TÃ­nh láº¡i Ä‘iá»ƒm thá»±c hÃ nh tá»« cÃ¡c khoáº£n trá»« vÃ  Ä‘Ã¡nh dáº¥u pháº§n thi Ä‘ang chá» kÃ½.
     *
     * @param candidateId    mÃ£ thÃ­ sinh
     * @param sessionId      mÃ£ ká»³ thi
     * @param sectionKeyword tá»« khÃ³a xÃ¡c Ä‘á»‹nh pháº§n thi
     * @return true náº¿u hoÃ n táº¥t thÃ nh cÃ´ng
     */
    boolean finalizeScoreEntry(int candidateId, int sessionId, String sectionKeyword);

    /**
     * Láº¥y danh sÃ¡ch cÃ¡c khoáº£n trá»« Ä‘iá»ƒm Ä‘Ã£ Ã¡p dá»¥ng cho thÃ­ sinh trong ká»³ thi.
     *
     * @param candidateId mÃ£ thÃ­ sinh
     * @param sessionId   mÃ£ ká»³ thi
     * @return danh sÃ¡ch Map chá»©a thÃ´ng tin cÃ¡c khoáº£n trá»« Ä‘iá»ƒm
     */
    java.util.List<java.util.Map<String, Object>> findAppliedScoreDeductions(int candidateId, int sessionId);

    /**
     * ÄÃ¡nh dáº¥u thÃ­ sinh bá»‹ Ä‘Ã¬nh chá»‰ thi trong Ä‘Äƒng kÃ½ thi.
     *
     * @param candidateId mÃ£ thÃ­ sinh
     * @return true náº¿u Ä‘Ã¡nh dáº¥u thÃ nh cÃ´ng
     */
    boolean markSuspended(int candidateId);

    /**
     * Há»§y Ä‘Ã¬nh chá»‰ thi cho thÃ­ sinh trong Ä‘Äƒng kÃ½ thi.
     *
     * @param candidateId mÃ£ thÃ­ sinh
     * @return true náº¿u há»§y thÃ nh cÃ´ng
     */
    boolean undoSuspension(int candidateId);

    /**
     * Äá»“ng bá»™ tráº¡ng thÃ¡i pháº§n thi cho táº¥t cáº£ thÃ­ sinh trong ká»³ thi.
     *
     * @param sessionId mÃ£ ká»³ thi
     */
    void syncSectionStatusesForSession(int sessionId);

    /**
     * ÄÃ¡nh dáº¥u Ä‘Ã£ in chá»¯ kÃ½ cho thÃ­ sinh.
     *
     * @param candidateId mÃ£ thÃ­ sinh
     * @param sessionId   mÃ£ ká»³ thi
     * @return true náº¿u Ä‘Ã¡nh dáº¥u thÃ nh cÃ´ng
     */
    boolean markSignaturePrinted(int candidateId, int sessionId);

    /**
     * HoÃ n táº¥t pháº§n thi cho thÃ­ sinh.
     *
     * @param candidateId mÃ£ thÃ­ sinh
     * @param sessionId   mÃ£ ká»³ thi
     * @return true náº¿u hoÃ n táº¥t thÃ nh cÃ´ng
     */
    boolean completeSection(int candidateId, int sessionId);
}




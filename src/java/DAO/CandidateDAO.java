package DAO;



import Models.Candidate;

import java.util.Map;

import java.util.Optional;



/**

 * Chỉ truy vấn/ghi <b>SBD</b> (số báo danh) từ bảng {@code Candidate} (import Công an).

 * <p>Không dùng bảng này cho lịch thi, trạng thái đăng ký hay điểm — xem {@link ExamRegistrationDAO}.</p>

 */

public interface CandidateDAO {



    boolean isTableAvailable();



    Optional<String> findSbdByRegistrationId(int registrationId);



    Optional<String> findSbdByPersonAndSession(int personId, int examSessionId);



    /** examRegistrationId → SBD (bản ghi đã ghép với đăng ký). */

    Map<Integer, String> findSbdMapByRegistrationForPerson(int personId);



    /** examSessionId → SBD (bản ghi import chưa ghép examRegistrationId). */

    Map<Integer, String> findSbdMapBySessionForPerson(int personId);



    int insertImported(Candidate candidate);



    /**

     * Ghép bản ghi Candidate với Person (CCCD) và ExamRegistration cùng đợt thi.

     *

     * @return số bản ghi được cập nhật

     */

    int linkImportedToRegistrations(int examSessionId);

}


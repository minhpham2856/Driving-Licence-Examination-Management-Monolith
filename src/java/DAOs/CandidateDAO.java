package DAOs;

import DTOs.CandidateDTO;
import Models.Candidate;
import java.util.List;
import java.util.Map;

public interface CandidateDAO {

    // Lấy thông tin thí sinh theo id
    // 
    // @param id mã thí sinh
    // @return CandidateDTO chứa thông tin thí sinh, hoặc null nếu không tìm thấy
    CandidateDTO getById(int id);

    // Lấy thông tin thí sinh theo id
    // 
    // @param id mã thí sinh
    // @return Candidate model, hoặc null nếu không tìm thấy
    Candidate findById(int id);

    // Tìm thí sinh theo id ca thi và sbd
    // 
    // @param sessionId id ca thi
    // @param candidateNumber sbd
    // @return Candidate model, hoặc null nếu không tìm thấy
    Candidate findByNumber(int sessionId, String candidateNumber);

    // Lấy thông tin thí sinh theo kỳ thi và sbd
    // 
    // @param sessionId id ca thi
    // @param sbd sbd
    // @return CandidateDTO, hoặc null nếu không tìm thấy
    CandidateDTO getBySessionAndSbd(int sessionId, String sbd);

    // Lấy danh sách thí sinh theo id ca thi
    // 
    // @param sessionId id ca thi
    // @return danh sách CandidateDTO
    List<CandidateDTO> getCandidatesBySession(int sessionId);

    // Cập nhật trạng thái điểm danh của thí sinh
    // 
    // @param id mã thí sinh
    // @param isPresent true nếu có mặt
    // @return true nếu cập nhật thành công
    boolean updatePresent(int id, boolean isPresent);

    // Cập nhật trạng thái thanh toán của thí sinh.
    // 
    // @param id mã thí sinh
    // @param isPaymentCompleted true nếu đã thanh toán
    // @return true nếu cập nhật thành công
    boolean updatePayment(int id, boolean isPaymentCompleted);

    // Cập nhật mã máy tính cho thí sinh.
    // 
    // @param id mã thí sinh
    // @param computerCode mã máy tính
    // @return true nếu cập nhật thành công
    boolean updateComputer(int id, String computerCode);

    // Cập nhật phòng thi đã phân cho thí sinh.
    // 
    // @param id mã thí sinh
    // @param areaId mã khu vực
    // @param areaName tên khu vực
    // @return true nếu cập nhật thành công
    boolean updateAllocatedRoom(int id, int areaId, String areaName);

    // Cập nhật thiết bị cho thí sinh.
    // 
    // @param id mã thí sinh
    // @param deviceCode mã thiết bị
    // @return true nếu cập nhật thành công
    boolean updateDevice(int id, String deviceCode);

    // Cập nhật điểm số cho thí sinh.
    // 
    // @param id mã thí sinh
    // @param theoryScore điểm lý thuyết (có thể null)
    // @param theoryPassed kết quả đỗ/trượt lý thuyết
    // @param practicalScore điểm thực hành (có thể null)
    // @param practicalPassed kết quả đỗ/trượt thực hành
    // @return true nếu cập nhật thành công
    boolean updateScores(int id, Integer theoryScore, String theoryPassed, Integer practicalScore, String practicalPassed);

    // Cập nhật điểm lý thuyết dưới dạng số câu đúng (0–35) với ngưỡng đạt.
    // 
    // @param id mã thí sinh
    // @param correctCount số câu trả lời đúng
    // @param passThreshold ngưỡng số câu đúng tối thiểu để đạt
    // @return true nếu cập nhật thành công
    boolean updateTheoryCorrectCount(int id, int correctCount, int passThreshold);

    // Cập nhật điểm thi đường trường cho thí sinh.
    // 
    // @param id mã thí sinh
    // @param roadScore điểm đường trường (có thể null)
    // @param roadPassed kết quả đỗ/trượt đường trường
    // @return true nếu cập nhật thành công
    boolean updateRoadScore(int id, Integer roadScore, String roadPassed);

    // Cập nhật thông tin hồ sơ cơ bản của thí sinh.
    // 
    // @param id mã thí sinh
    // @param fullName họ và tên
    // @param dob ngày sinh
    // @param govIdNo số CMND/CCCD
    // @param email địa chỉ email
    // @param phoneNo số điện thoại
    // @return true nếu cập nhật thành công
    boolean updateProfile(int id, String fullName, java.sql.Date dob, String govIdNo, String email, String phoneNo);

    // Cập nhật toàn bộ thông tin hồ sơ thí sinh (dành cho sát hạch viên).
    // 
    // @param id mã thí sinh
    // @param fullName họ và tên
    // @param dob ngày sinh
    // @param govIdNo số CMND/CCCD
    // @param email địa chỉ email
    // @param phoneNo số điện thoại
    // @param address địa chỉ
    // @param sex giới tính
    // @param reasonForTaking lý do dự thi
    // @return true nếu cập nhật thành công
    boolean updateExaminerProfile(int id, String fullName, java.sql.Date dob, String govIdNo,
            String email, String phoneNo, String address, String sex, String reasonForTaking);

    // Cập nhật ảnh thí sinh.
    // 
    // @param id mã thí sinh
    // @param photoUrl đường dẫn ảnh
    // @return true nếu cập nhật thành công
    boolean updatePhoto(int id, String photoUrl);

    // Thêm mới một thí sinh.
    // 
    // @param reg đối tượng CandidateDTO chứa thông tin đăng ký
    // @return true nếu thêm thành công
    boolean insert(CandidateDTO reg);

    // Lấy danh sách tất cả thí sinh.
    // 
    // @return danh sách tất cả CandidateDTO
    List<CandidateDTO> getAll();

    // Đánh dấu thí sinh vắng mặt.
    // 
    // @param candidateId mã thí sinh
    // @return true nếu đánh dấu thành công
    boolean markAbsent(int candidateId);

    // Hủy đánh dấu vắng mặt cho thí sinh.
    // 
    // @param candidateId mã thí sinh
    // @return true nếu hủy thành công
    boolean clearAbsentMarking(int candidateId);

    // Tìm mã thí sinh theo mã hồ sơ và id ca thi.
    // 
    // @param profileId mã hồ sơ
    // @param sessionId id ca thi
    // @return Integer mã thí sinh, hoặc null nếu không tìm thấy
    Integer findCandidateIdByProfileAndSession(int profileId, int sessionId);

    // Áp dụng các khoản trừ điểm cho một phần thi của thí sinh và tính lại ExamScore.
    // 
    // @param candidateId mã thí sinh
    // @param deductionIds mảng mã các khoản trừ điểm
    // @param sectionKeyword từ khóa xác định phần thi (theory/practical/road)
    // @return true nếu áp dụng thành công
    boolean applyScoreDeductions(int candidateId, int[] deductionIds, String sectionKeyword);

    // Điều chỉnh số lần xuất hiện (+1 / -1) của một khoản trừ điểm trong quá trình chấm thực hành.
    // 
    // @param candidateId mã thí sinh
    // @param sessionId id ca thi
    // @param deductionId mã khoản trừ điểm
    // @param delta giá trị điều chỉnh (+1 hoặc -1)
    // @return true nếu điều chỉnh thành công
    boolean adjustScoreDeductionOccurrence(int candidateId, int sessionId, int deductionId, int delta);

    // Tính lại điểm thực hành từ các khoản trừ và đánh dấu phần thi đang chờ ký.
    // 
    // @param candidateId mã thí sinh
    // @param sessionId id ca thi
    // @param sectionKeyword từ khóa xác định phần thi
    // @return true nếu hoàn tất thành công
    boolean finalizeScoreEntry(int candidateId, int sessionId, String sectionKeyword);

    // Lấy danh sách các khoản trừ điểm đã áp dụng cho thí sinh trong kỳ thi.
    // 
    // @param candidateId mã thí sinh
    // @param sessionId id ca thi
    // @return danh sách Map chứa thông tin các khoản trừ điểm
    List<Map<String, Object>> findAppliedScoreDeductions(int candidateId, int sessionId);

    // Đánh dấu thí sinh bị đình chỉ thi.
    // 
    // @param candidateId mã thí sinh
    // @return true nếu đánh dấu thành công
    boolean markSuspended(int candidateId);

    // Hủy đình chỉ thi cho thí sinh.
    // 
    // @param candidateId mã thí sinh
    // @return true nếu hủy thành công
    boolean undoSuspension(int candidateId);

    // Đồng bộ trạng thái phần thi cho tất cả thí sinh trong kỳ thi.
    // 
    // @param sessionId id ca thi
    void syncSectionStatusesForSession(int sessionId);

    // Đánh dấu thí sinh đã in chữ ký.
    // 
    // @param candidateId mã thí sinh
    // @param sessionId id ca thi
    // @return true nếu đánh dấu thành công
    boolean markSignaturePrinted(int candidateId, int sessionId);

    // Hoàn tất phần thi cho thí sinh.
    // 
    // @param candidateId mã thí sinh
    // @param sessionId id ca thi
    // @return true nếu hoàn tất thành công
    boolean completeSection(int candidateId, int sessionId);
}

package examstaff.dao;


import examstaff.dto.candidate.CandidateCallDTO;


/**
 * DAO cho thao tác với lịch sử gọi thí sinh (CandidateCall).
 * Cung cấp phương thức ghi nhận việc gọi thí sinh vào phòng thi.
 */
public interface CandidateCallDAO {

    /**
     * Ghi nhận một lượt gọi thí sinh.
     *
     * @param call đối tượng CandidateCallDTO chứa thông tin gọi thí sinh
     * @return true nếu ghi thành công
     */
    boolean insert(CandidateCallDTO call);
}

package Services;

import jakarta.servlet.http.HttpSession;

/** Gửi email bảng điểm cho thí sinh qua Gmail (nếu đã cấu hình và được bật). */
public interface RegistrantExamResultEmailService {

    /**
     * Gửi bảng điểm mới nhất tới email tài khoản thí sinh.
     *
     * @param candidateId CandidateId
     * @param session     phiên thí sinh (để đọc tùy chọn Gmail); null → mặc định gửi
     * @return true nếu đã gửi thành công
     */
    boolean trySendScoreSheet(int candidateId, HttpSession session);
}

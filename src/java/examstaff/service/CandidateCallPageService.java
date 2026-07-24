package examstaff.service;

import examstaff.dto.CandidateCallPageCommand;
import examstaff.dto.CandidateCallPageViewDTO;

/**
 * Orchestrator nghiệp vụ trang Gọi thí sinh (staff) — không phụ thuộc Servlet API.
 */
public interface CandidateCallPageService {

    /**
     * Xử lý action (nếu có), đồng bộ hàng đợi / số đang gọi, trả view DTO để servlet bind.
     *
     * @param command input từ Presentation (action, SBD, session flags, board, cache queue…)
     * @return trạng thái trang: queue, alert, cờ sync/release/pause board, redirect…
     */
    CandidateCallPageViewDTO preparePage(CandidateCallPageCommand command);
}

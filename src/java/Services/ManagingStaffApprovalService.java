package Services;

import Models.ManagingStaffApprovalView;
import Models.User;
import jakarta.servlet.http.HttpSession;
import java.util.List;

public interface ManagingStaffApprovalService {

    List<ManagingStaffApprovalView> listPendingApprovals();

    ManagingStaffApprovalView loadApprovalDetail(int profileId);

    /** @return null nếu xử lý thành công. */
    String reviewDocuments(User staff, int profileId, boolean approved, String staffNote, HttpSession session);
}

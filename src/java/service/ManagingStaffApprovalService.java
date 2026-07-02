package service;

import dto.staff.ManagingStaffApprovalView;
import model.user.User;
import jakarta.servlet.http.HttpSession;
import java.util.List;

public interface ManagingStaffApprovalService {

    List<ManagingStaffApprovalView> listPendingApprovals();

    ManagingStaffApprovalView loadApprovalDetail(int profileId);

    ManagingStaffApprovalView loadApprovalDetail(int profileId, int workflowExamRegistrationId);

    /** @return null nếu xử lý thành công. */
    String reviewDocuments(User staff, int profileId, boolean approved, String staffNote, HttpSession session);

    String reviewDocuments(User staff, int profileId, int workflowExamRegistrationId,
            boolean approved, String staffNote, HttpSession session);
}

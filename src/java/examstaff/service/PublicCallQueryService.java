package examstaff.service;

import examstaff.dto.PublicCallSnapshotDTO;
import examstaff.dto.CallBoardState;

/**
 * Truy vấn dữ liệu chỉ-đọc cho màn hình Public Call (bảng LED / màn hình chờ).
 */
public interface PublicCallQueryService {

    /**
     * Ghép hàng đợi DB + trạng thái CallBoard thành snapshot hiển thị công khai.
     *
     * @param examId      mã kỳ thi đang active
     * @param webRootPath đường dẫn web root (chuẩn hóa URL ảnh)
     * @param board       trạng thái bảng gọi hiện tại (có thể null)
     * @return snapshot gồm thí sinh đang gọi, kế tiếp, hàng chờ và cờ pause/end
     */
    PublicCallSnapshotDTO loadSnapshot(int examId, String webRootPath, CallBoardState board);
}

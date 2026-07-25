package admin.util;

import java.util.ArrayList;
import java.util.List;

/**
 * Gom kết quả của một thao tác hàng loạt (import, xóa nhiều dòng...)
 * để dựng thông báo thống nhất cho người dùng.
 */
public final class BulkResult {

    /** Số dòng lỗi tối đa hiển thị trong thông báo, phần còn lại rút gọn. */
    private static final int MAX_SHOWN_ERRORS = 5;

    private int done;
    private final List<String> skipped = new ArrayList<>();

    /** Ghi nhận một mục xử lý thành công. */
    public void success() {
        done++;
    }

    /** Ghi nhận một mục bị bỏ qua kèm lý do hiển thị cho người dùng. */
    public void skip(String reason) {
        skipped.add(reason);
    }

    public int getDone() {
        return done;
    }

    public int getSkippedCount() {
        return skipped.size();
    }

    /** success khi làm được hết; warning khi làm được một phần; danger khi không làm được gì. */
    public String flashType() {
        if (done == 0) return "danger";
        return skipped.isEmpty() ? "success" : "warning";
    }

    /**
     * Dựng câu thông báo tổng kết.
     *
     * @param actionLabel động từ đã chia, ví dụ "đã xóa", "đã thêm"
     * @param subject     tên đối tượng số nhiều, ví dụ "tài khoản", "máy/thiết bị thi"
     * @param total       tổng số mục được yêu cầu xử lý
     */
    public String message(String actionLabel, String subject, int total) {
        StringBuilder msg = new StringBuilder();
        msg.append("Kết quả: ").append(actionLabel).append(' ')
           .append(done).append('/').append(total).append(' ').append(subject).append('.');

        if (!skipped.isEmpty()) {
            msg.append(" Bỏ qua ").append(skipped.size()).append(": ");
            int show = Math.min(skipped.size(), MAX_SHOWN_ERRORS);
            for (int i = 0; i < show; i++) {
                if (i > 0) msg.append(" | ");
                msg.append(skipped.get(i));
            }
            if (skipped.size() > show) {
                msg.append(" | ... và ").append(skipped.size() - show).append(" mục khác");
            }
        }
        return msg.toString();
    }
}

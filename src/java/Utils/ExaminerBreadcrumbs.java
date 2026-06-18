package Utils;

import jakarta.servlet.http.HttpServletRequest;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * Tiêu đề breadcrumb header giám khảo (khớp sidebar).
 */
public final class ExaminerBreadcrumbs {

    private ExaminerBreadcrumbs() {
    }

    public static final class Item {
        private String label;
        private String href;
        private boolean primary;

        public String getLabel() {
            return label;
        }

        public String getHref() {
            return href;
        }

        public boolean isPrimary() {
            return primary;
        }
    }

    public static List<Item> buildItems(HttpServletRequest request) {
        List<Item> items = new ArrayList<>();
        if (request == null) {
            items.add(primary("", "Sát hạch viên", "/views/examiner/dashboard"));
            return items;
        }

        String ctx = request.getContextPath() != null ? request.getContextPath() : "";
        String path = stripContextPath(request);
        String sbd = request.getParameter("sbd");
        String sbdQuery = sbdQuery(sbd);

        if (path.contains("/candidate-paper")) {
            items.add(primary(ctx, "Sửa thông tin", "/views/examiner/candidate-details"));
            items.add(child(ctx, "chi tiết", "/views/examiner/candidate-details-edit" + sbdQuery));
            items.add(child(ctx, "đề thi", "/views/examiner/candidate-paper" + sbdQuery));
            return items;
        }
        if (path.contains("/candidate-details-edit")) {
            items.add(primary(ctx, "Sửa thông tin", "/views/examiner/candidate-details"));
            items.add(child(ctx, "chi tiết", "/views/examiner/candidate-details-edit" + sbdQuery));
            return items;
        }
        if (path.contains("/candidate-details")) {
            items.add(primary(ctx, "Sửa thông tin", "/views/examiner/candidate-details"));
            return items;
        }
        if (path.contains("/result-details-edit")) {
            items.add(primary(ctx, "Sửa kết quả", "/views/examiner/result-details"));
            items.add(child(ctx, "chi tiết", "/views/examiner/result-details-edit" + sbdQuery));
            return items;
        }
        if (path.contains("/result-details")) {
            items.add(primary(ctx, "Sửa kết quả", "/views/examiner/result-details"));
            return items;
        }
        if (path.contains("/violation-confirm")) {
            items.add(primary(ctx, "Vi phạm", "/views/examiner/violations"));
            items.add(child(ctx, "đình chỉ", "/views/examiner/violation-confirm" + sbdQuery));
            return items;
        }
        if (path.contains("/violation-undo")) {
            items.add(primary(ctx, "Vi phạm", "/views/examiner/violations"));
            items.add(child(ctx, "hoàn tác đình chỉ", "/views/examiner/violation-undo" + sbdQuery));
            return items;
        }
        if (path.contains("/violations")) {
            items.add(primary(ctx, "Vi phạm", "/views/examiner/violations"));
            return items;
        }
        if (path.contains("/confirmation")) {
            items.add(primary(ctx, "Gọi thí sinh", "/views/examiner/candidate-call"));
            items.add(child(ctx, "xác nhận", "/views/examiner/confirmation" + sbdQuery));
            return items;
        }
        if (path.contains("/candidate-call")) {
            items.add(primary(ctx, "Gọi thí sinh", "/views/examiner/candidate-call"));
            return items;
        }
        if (path.contains("/score-entry")) {
            items.add(primary(ctx, "Nhập điểm", "/views/examiner/score-entry"));
            return items;
        }
        if (path.contains("/devices")) {
            items.add(primary(ctx, "Thiết bị", "/views/examiner/devices"));
            return items;
        }
        if (path.contains("/export")) {
            items.add(primary(ctx, "Xuất file", "/views/examiner/export"));
            return items;
        }
        if (path.contains("/print-documents")) {
            items.add(primary(ctx, "In văn bản", "/views/examiner/print-documents"));
            if (sbd != null && !sbd.isBlank()) {
                items.add(child(ctx, "biên bản kết quả thi", "/views/examiner/print-documents" + sbdQuery));
            }
            return items;
        }
        if (path.contains("/audit")) {
            items.add(primary(ctx, "Nhật ký", "/views/examiner/audit"));
            return items;
        }
        if (path.contains("/dashboard")) {
            items.add(primary(ctx, "Bảng điều khiển", "/views/examiner/dashboard"));
            return items;
        }

        Object headerTitle = request.getAttribute("headerTitle");
        if (headerTitle != null && !String.valueOf(headerTitle).isBlank()) {
            items.add(primary(ctx, String.valueOf(headerTitle).trim(), path));
            return items;
        }
        items.add(primary(ctx, "Sát hạch viên", "/views/examiner/dashboard"));
        return items;
    }

    public static String resolve(HttpServletRequest request) {
        List<Item> items = buildItems(request);
        if (items.isEmpty()) {
            return "Sát hạch viên";
        }
        StringBuilder text = new StringBuilder();
        for (int i = 0; i < items.size(); i++) {
            if (i > 0) {
                text.append(" > ");
            }
            text.append(items.get(i).getLabel());
        }
        return text.toString();
    }

    private static Item primary(String ctx, String label, String path) {
        Item item = new Item();
        item.label = label;
        item.href = ctx + path;
        item.primary = true;
        return item;
    }

    private static Item child(String ctx, String label, String path) {
        Item item = new Item();
        item.label = label;
        item.href = ctx + path;
        item.primary = false;
        return item;
    }

    private static String sbdQuery(String sbd) {
        if (sbd == null || sbd.isBlank()) {
            return "";
        }
        return "?sbd=" + URLEncoder.encode(sbd.trim(), StandardCharsets.UTF_8);
    }

    private static String stripContextPath(HttpServletRequest request) {
        String uri = request.getRequestURI();
        String ctx = request.getContextPath();
        if (ctx != null && !ctx.isEmpty() && uri.startsWith(ctx)) {
            return uri.substring(ctx.length());
        }
        return uri;
    }
}

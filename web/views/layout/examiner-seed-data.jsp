<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%
if (request.getAttribute("candidates") == null) {
    java.util.List<java.util.Map<String, Object>> candidates = new java.util.ArrayList<>();

    java.util.function.BiConsumer<String[], java.util.Map<String, Object>> add = (row, m) -> {
        m.put("sbd", row[0]);
        m.put("fullName", row[1]);
        m.put("dob", row[2]);
        m.put("governmentId", row[3]);
        m.put("address", row[4]);
        m.put("sex", row[5]);
        m.put("examDate", row[6]);
        m.put("licenceClass", row[7]);
        m.put("status", row[8]);
        m.put("statusLabel", row[9]);
        m.put("correct", row[10]);
        m.put("wrong", row[11]);
        m.put("unanswered", row[12]);
        m.put("passed", "true".equals(row[13]));
        m.put("resultLabel", row[14]);
        m.put("reasonForTaking", row[15]);
        m.put("scoreTheory", row[16]);
        m.put("scorePractical", row[17]);
        m.put("scoreRoadLayout", row[18]);
        m.put("scoreOnRoad", row[19]);
        candidates.add(m);
    };

    add.accept(new String[]{
        "046", "Trần Thị Bình", "22/08/1995", "079012345679",
        "45 Nguyễn Huệ, Q.1, TP.HCM", "Nữ", "01/06/2026", "B",
        "done", "Đã thi", "35", "0", "0", "true", "ĐẠT",
        "Thi lần đầu", "35/35", "—", "—", "—"
    }, new java.util.HashMap<>());

    add.accept(new String[]{
        "123", "Nguyễn Văn Quyết", "12/04/1992", "031092004581",
        "88 Lê Lợi, TP.HCM", "Nam", "01/06/2026", "B",
        "done", "Đã thi", "25", "10", "0", "false", "TRƯỢT",
        "Thi lại vì trượt lý thuyết", "25/35", "—", "—", "—"
    }, new java.util.HashMap<>());

    add.accept(new String[]{
        "456", "Phạm Văn Cường", "07/07/1990", "079012345682",
        "56 Hai Bà Trưng, Hà Nội", "Nam", "01/06/2026", "B",
        "testing", "Đang thi", "—", "—", "—", "false", "—",
        "Thi lần đầu", "—", "—", "—", "—"
    }, new java.util.HashMap<>());

    add.accept(new String[]{
        "045", "Nguyễn Văn A", "15/08/1995", "079012345678",
        "123 Nguyễn Văn Linh, P. Tân Phong, Q.7, TP.HCM", "Nam", "01/06/2026", "B",
        "pending", "Chưa thi", "—", "—", "—", "false", "—",
        "Thi lại vì trượt sa hình", "—", "—", "72/100", "—"
    }, new java.util.HashMap<>());

    add.accept(new String[]{
        "124", "Nguyễn Văn B", "18/02/1998", "079012345681",
        "12 Lý Thường Kiệt, Huế", "Nam", "01/06/2026", "B1",
        "done", "Đã thi", "30", "5", "0", "true", "ĐẠT",
        "Thi lại vì trừ hết điểm", "30/35", "—", "—", "—"
    }, new java.util.HashMap<>());

    request.setAttribute("candidates", candidates);

    java.util.Map<String, Object> examSummary = new java.util.HashMap<>();
    examSummary.put("examCode", "EX-B-20260601");
    examSummary.put("total", candidates.size());
    examSummary.put("done", 3);
    examSummary.put("testing", 1);
    examSummary.put("pending", 1);
    examSummary.put("passed", 2);
    examSummary.put("failed", 1);
    request.setAttribute("examSummary", examSummary);
}

if (request.getAttribute("auditLogs") == null) {
    java.util.List<java.util.Map<String, Object>> auditLogs = new java.util.ArrayList<>();

    java.util.function.Consumer<String[]> addAudit = row -> {
        java.util.Map<String, Object> m = new java.util.HashMap<>();
        m.put("username", row[0]);
        m.put("action", row[1]);
        m.put("actionBadge", row[2]);
        m.put("actionLabel", row[3]);
        m.put("entityName", row[4]);
        m.put("entityId", row[5]);
        m.put("info", row[6]);
        m.put("oldValue", row[7]);
        m.put("newValue", row[8]);
        m.put("newValueClass", row[9]);
        m.put("reason", row[10]);
        m.put("time", row[11]);
        m.put("date", row[12]);
        auditLogs.add(m);
    };

    addAudit.accept(new String[]{"admin", "UPDATE", "audit-badge--update", "CẬP NHẬT", "Thí sinh", "123", "Cập nhật điểm thi...", "28/30", "30/30", "audit-new--green", "Phúc khảo", "09:15:22", "2023-10-25"});
    addAudit.accept(new String[]{"system_auto", "SYSTEM", "audit-badge--system", "HỆ THỐNG", "Phòng thi", "-", "Mở khóa ca thi s...", "Khóa", "Mở", "audit-new--blue", "Theo lịch trình", "07:00:00", "2023-10-25"});
    addAudit.accept(new String[]{"examiner_tung", "WARNING", "audit-badge--warning", "CẢNH BÁO", "Thí sinh", "456", "Đánh dấu vi phạm...", "Bình thường", "Vi phạm", "audit-new--dark", "Mang điện thoại", "10:45:11", "2023-10-24"});
    addAudit.accept(new String[]{"admin", "DELETE", "audit-badge--delete", "XÓA", "", "789", "Xóa hồ sơ trùng ...", "Tồn tại", "Đã xóa", "audit-new--red", "Trùng CMND", "14:20:05", "2023-10-24"});
    addAudit.accept(new String[]{"admin", "UPDATE", "audit-badge--update", "CẬP NHẬT", "Thí sinh", "124", "Sửa lỗi sai tên đệm", "Nguyễn Văn A", "Nguyễn Văn B", "audit-new--green", "Yêu cầu từ Cục", "08:10:00", "2023-10-23"});

    request.setAttribute("auditLogs", auditLogs);
}

if (request.getAttribute("paperAnswers") == null) {
    java.util.List<java.util.Map<String, Object>> paperAnswers = new java.util.ArrayList<>();
    String[][] rows = {
        {"01", "https://res.cloudinary.com/dqwh0wcjh/image/upload/v1780127982/001_pb4uxc.png", "A", "A", "true"},
        {"02", "https://res.cloudinary.com/dqwh0wcjh/image/upload/v1780127982/002_xfqch7.png", "B", "A", "false"},
        {"03", "https://res.cloudinary.com/dqwh0wcjh/image/upload/v1780127983/003_f2kpqz.png", "C", "C", "true"},
        {"04", "https://res.cloudinary.com/dqwh0wcjh/image/upload/v1780127982/004_ype2gx.png", "A", "A", "true"},
        {"05", "https://res.cloudinary.com/dqwh0wcjh/image/upload/v1780127982/005_pnn5lk.png", "B", "C", "false"}
    };
    for (String[] row : rows) {
        java.util.Map<String, Object> m = new java.util.HashMap<>();
        m.put("questionNo", row[0]);
        m.put("imageUrl", row[1]);
        m.put("correctAnswer", row[2]);
        m.put("studentAnswer", row[3]);
        m.put("correct", "true".equals(row[4]));
        paperAnswers.add(m);
    }
    request.setAttribute("paperAnswers", paperAnswers);

    java.util.Map<String, Object> paperSummary = new java.util.HashMap<>();
    paperSummary.put("correctCount", 3);
    paperSummary.put("wrongCount", 2);
    request.setAttribute("paperSummary", paperSummary);
}

if (request.getAttribute("candidate") == null && request.getAttribute("candidates") != null) {
    @SuppressWarnings("unchecked")
    java.util.List<java.util.Map<String, Object>> list =
        (java.util.List<java.util.Map<String, Object>>) request.getAttribute("candidates");
    String sbd = request.getParameter("sbd");
    java.util.Map<String, Object> found = null;
    if (sbd != null) {
        for (java.util.Map<String, Object> item : list) {
            if (sbd.equals(item.get("sbd"))) {
                found = item;
                break;
            }
        }
    }
    if (found == null && !list.isEmpty()) {
        found = list.get(0);
    }
    request.setAttribute("candidate", found);
}
%>

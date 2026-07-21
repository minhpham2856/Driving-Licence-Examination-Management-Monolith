<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<c:set var="ctx" value="${pageContext.request.contextPath}" />

<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Kết quả thi | Lái Vui</title>
    <link rel="preconnect" href="https://fonts.googleapis.com">
    <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
    <link href="https://fonts.googleapis.com/css2?family=Inter:wght@400;500;700;800&family=JetBrains+Mono:wght@600&display=swap" rel="stylesheet">
    <style>
        * { box-sizing: border-box; }
        body { margin: 0; font-family: 'Inter', sans-serif; background: #eef2f7; color: #0f172a; }
        .results-main { max-width: 820px; margin: 1.5rem auto; padding: 6rem 1rem 5rem; }
        .info-card { background: #fff; border: 1px solid #e2e8f0; border-radius: 18px; padding: 1.5rem; margin-bottom: 1.25rem; }
        .card-title { margin: 0 0 1rem; font-size: 1.15rem; font-weight: 800; }
        .info-grid { display: grid; grid-template-columns: 1fr 1fr; gap: .8rem 1.5rem; }
        .info-item { display: flex; justify-content: space-between; gap: 10px; padding: .5rem 0; border-bottom: 1px dashed #eef2f7; }
        .info-label { color: #64748b; font-size: .88rem; }
        .info-value { font-weight: 700; text-align: right; }
        .results-header-row { display: flex; align-items: center; justify-content: space-between; }
        .status-badge { padding: 8px 22px; border-radius: 9999px; font-weight: 800; font-size: 1rem; letter-spacing: .5px; }
        .status-badge--pass { background: #ecfdf3; color: #027a48; border: 1px solid #abefc6; }
        .status-badge--fail { background: #fef3f2; color: #b42318; border: 1px solid #fecdca; }
        .crit-note { margin: .75rem 0 0; padding: .6rem .9rem; border-radius: 8px; background: rgba(239,68,68,.08); border: 1px solid rgba(239,68,68,.25); color: #b91c1c; font-weight: 600; font-size: .85rem; }
        .action-container { display: flex; align-items: center; justify-content: space-between; gap: 1rem; }
        .exit-btn { display: inline-flex; align-items: center; justify-content: center; height: 48px; padding: 0 2rem; border-radius: 12px; background: #0052cc; color: #fff; font-weight: 800; text-decoration: none; }
        .exit-btn:hover { background: #0041a3; }
        .notice-text { margin: 0; color: #64748b; font-weight: 600; }
        @media (max-width: 640px){ .info-grid { grid-template-columns: 1fr; } }
    </style>
</head>
<body>
    <jsp:include page="/views/layout/header-exam-time.jsp">
        <jsp:param name="timeLeft" value="00:00:00" />
    </jsp:include>

    <main class="results-main">
        <section class="info-card">
            <h2 class="card-title">Thông tin thí sinh</h2>
            <div class="info-grid">
                <div class="info-item"><span class="info-label">SBD:</span><span class="info-value">${candidate.sbd}</span></div>
                <div class="info-item"><span class="info-label">Hạng:</span><span class="info-value">${candidate.licenseClass}</span></div>
                <div class="info-item"><span class="info-label">Họ &amp; Tên:</span><span class="info-value">${candidate.fullName}</span></div>
                <div class="info-item"><span class="info-label">Địa chỉ:</span><span class="info-value">${empty candidate.address ? '—' : candidate.address}</span></div>
                <div class="info-item"><span class="info-label">Số CC:</span><span class="info-value">${candidate.citizenId}</span></div>
                <div class="info-item"><span class="info-label">Ngày sinh:</span><span class="info-value">${candidate.dob}</span></div>
            </div>
        </section>

        <section class="info-card">
            <div class="results-header-row">
                <h2 class="card-title" style="margin-bottom:0;">Kết quả thi</h2>
                <c:choose>
                    <c:when test="${result.passed}"><div class="status-badge status-badge--pass">ĐẠT</div></c:when>
                    <c:otherwise><div class="status-badge status-badge--fail">KHÔNG ĐẠT</div></c:otherwise>
                </c:choose>
            </div>

            <c:if test="${result.criticalFailed}">
                <p class="crit-note">Không đạt do sai câu điểm liệt (dù tổng số câu đúng có thể đã đủ).</p>
            </c:if>

            <div class="info-grid" style="margin-top:1rem;">
                <div class="info-item"><span class="info-label">Điểm:</span><span class="info-value">${result.score}/${result.totalQuestions}</span></div>
                <div class="info-item"><span class="info-label">Số câu đúng:</span><span class="info-value">${result.correctCount}/${result.totalQuestions}</span></div>
                <div class="info-item"><span class="info-label">Giờ làm bài:</span><span class="info-value">${result.startTime}</span></div>
                <div class="info-item"><span class="info-label">Số câu sai:</span><span class="info-value">${result.incorrectCount}/${result.totalQuestions}</span></div>
                <div class="info-item"><span class="info-label">Giờ nộp bài:</span><span class="info-value">${result.endTime}</span></div>
                <div class="info-item"><span class="info-label">Câu không trả lời:</span><span class="info-value">${result.unansweredCount}/${result.totalQuestions}</span></div>
            </div>
        </section>

        <div class="action-container">
            <a href="${ctx}/exam/entrance" class="exit-btn">Thoát</a>
            <p class="notice-text">Thí sinh di chuyển tới bàn ký tên</p>
        </div>
    </main>

    <jsp:include page="/views/layout/footer-exam.jsp">
        <jsp:param name="noticeTitle" value="Lưu ý:" />
        <jsp:param name="noticeText" value="Thí sinh di chuyển tới bàn ký tên để hoàn tất thủ tục sau khi xem kết quả." />
    </jsp:include>
</body>
</html>

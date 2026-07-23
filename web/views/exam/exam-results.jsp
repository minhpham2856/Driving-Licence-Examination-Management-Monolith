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
    <link href="https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700;800&family=JetBrains+Mono:wght@600;700&display=swap" rel="stylesheet">
    <link href="https://fonts.googleapis.com/css2?family=Material+Symbols+Outlined:opsz,wght,FILL,GRAD@24,400,0,0&display=swap" rel="stylesheet">
    <link href="${ctx}/assets/css/exam/exam-results.css" rel="stylesheet">
</head>
<body class="exam-result-body">
<main class="exam-result-shell">
    <section class="exam-result-card">
        <span class="material-symbols-outlined exam-result-icon">${result.passed ? 'verified' : 'cancel'}</span>
        <p class="exam-result-eyebrow">Kết quả bài thi lý thuyết</p>
        <h1 class="${result.passed ? 'is-pass' : 'is-fail'}">${result.passed ? 'Đạt' : 'Không đạt'}</h1>
        <div class="exam-result-stats">
            <div><span>Đúng</span><strong>${result.correct}</strong></div>
            <div><span>Sai</span><strong>${result.wrong}</strong></div>
            <div><span>Không trả lời</span><strong>${result.unanswered}</strong></div>
        </div>
        <c:if test="${result.criticalFailed}">
            <p class="exam-result-warning">Không đạt do trả lời sai câu điểm liệt.</p>
        </c:if>
        <p class="exam-result-note">Vui lòng chờ sát hạch viên in biên bản và hoàn tất phần thi.</p>
    </section>
</main>
</body>
</html>

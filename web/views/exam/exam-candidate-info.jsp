<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<c:set var="ctx" value="${pageContext.request.contextPath}" />
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Thông tin thí sinh | Lái Vui</title>
    <link rel="preconnect" href="https://fonts.googleapis.com">
    <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
    <link href="https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700;800&family=JetBrains+Mono:wght@600;700&display=swap" rel="stylesheet">
    <link href="https://fonts.googleapis.com/css2?family=Material+Symbols+Outlined:opsz,wght,FILL,GRAD@24,400,0,0&display=swap" rel="stylesheet">
    <link href="${ctx}/assets/css/exam/exam-entrance.css" rel="stylesheet">
</head>
<body class="exam-kiosk-body">
<main class="exam-kiosk-shell">
    <section class="exam-kiosk-card" aria-label="Thông tin thí sinh">
        <div class="exam-kiosk-brand">
            <img class="exam-kiosk-logo" src="${ctx}/assets/imgs/csgt-footer.png" alt="Logo CSGT">
            <div>
                <p class="exam-kiosk-eyebrow">Xác nhận thí sinh</p>
                <h1>Thông tin vào thi</h1>
            </div>
        </div>
        <div class="exam-info-grid">
            <div>
                <span>Số báo danh</span>
                <strong class="exam-info-mono"><c:out value="${candidateExam.candidateNumber}"/></strong>
            </div>
            <div>
                <span>Họ tên</span>
                <strong><c:out value="${candidateExam.fullName}"/></strong>
            </div>
            <div>
                <span>Kỳ thi</span>
                <strong><c:out value="${empty candidateExam.examCode ? '-' : candidateExam.examCode}"/></strong>
            </div>
            <div>
                <span>Hạng GPLX</span>
                <strong><c:out value="${empty candidateExam.licenceClass ? '-' : candidateExam.licenceClass}"/></strong>
            </div>
        </div>
        <form method="get" action="${ctx}/exam/questions" class="exam-kiosk-form">
            <button type="submit" class="exam-kiosk-button">
                <span class="material-symbols-outlined">play_arrow</span>
                Bắt đầu thi
            </button>
        </form>
    </section>
</main>
</body>
</html>

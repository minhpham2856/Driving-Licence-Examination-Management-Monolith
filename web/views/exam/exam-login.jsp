<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<c:set var="ctx" value="${pageContext.request.contextPath}" />
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Đăng nhập kỳ thi | Lái Vui</title>
    <link rel="preconnect" href="https://fonts.googleapis.com">
    <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
    <link href="https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700;800&family=JetBrains+Mono:wght@600;700&display=swap" rel="stylesheet">
    <link href="https://fonts.googleapis.com/css2?family=Material+Symbols+Outlined:opsz,wght,FILL,GRAD@24,400,0,0&display=swap" rel="stylesheet">
    <link href="${ctx}/assets/css/exam/exam-entrance.css" rel="stylesheet">
</head>
<body class="exam-kiosk-body">
<main class="exam-kiosk-shell">
    <section class="exam-kiosk-card" aria-label="Đăng nhập kỳ thi">
        <div class="exam-kiosk-brand">
            <img class="exam-kiosk-logo" src="${ctx}/assets/imgs/csgt-footer.png" alt="Logo CSGT">
            <div>
                <p class="exam-kiosk-eyebrow">Máy thi lý thuyết</p>
                <h1>Đăng nhập kỳ thi</h1>
            </div>
        </div>
        <p class="exam-kiosk-subtitle">Nhập mã kỳ thi và mật khẩu kỳ thi do cán bộ cấu hình để mở màn hình nhập SBD.</p>
        <c:if test="${not empty error}">
            <p class="exam-kiosk-alert" role="alert"><c:out value="${error}"/></p>
        </c:if>
        <form class="exam-kiosk-form" action="${ctx}/exam/login" method="post">
            <label for="examCode">Mã kỳ thi</label>
            <div class="exam-kiosk-input">
                <span class="material-symbols-outlined">tag</span>
                <input id="examCode" name="examCode" type="text"
                       autocomplete="off" required autofocus
                       placeholder="VD: A1-20260601-1000"
                       value="<c:out value='${empty param.examCode ? param.examId : param.examCode}'/>">
            </div>

            <label for="examPassword">Mật khẩu kỳ thi</label>
            <div class="exam-kiosk-input">
                <span class="material-symbols-outlined">lock</span>
                <input id="examPassword" name="examPassword" type="password"
                       autocomplete="current-password" required>
            </div>

            <button type="submit" class="exam-kiosk-button">
                <span class="material-symbols-outlined">login</span>
                Mở kỳ thi
            </button>
        </form>
    </section>
</main>
</body>
</html>

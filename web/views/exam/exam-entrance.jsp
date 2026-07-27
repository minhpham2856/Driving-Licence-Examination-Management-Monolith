<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<c:set var="ctx" value="${pageContext.request.contextPath}" />
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Vào thi lý thuyết | Lái Vui</title>
    <link rel="preconnect" href="https://fonts.googleapis.com">
    <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
    <link href="https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700;800&family=JetBrains+Mono:wght@600;700&display=swap" rel="stylesheet">
    <link href="https://fonts.googleapis.com/css2?family=Material+Symbols+Outlined:opsz,wght,FILL,GRAD@24,400,0,0&display=swap" rel="stylesheet">
    <link href="${ctx}/assets/css/exam/exam-entrance.css" rel="stylesheet">
</head>
<body class="exam-kiosk-body">
<main class="exam-kiosk-shell">
    <section class="exam-kiosk-card exam-kiosk-card--compact" aria-label="Nhập số báo danh">
        <div class="exam-kiosk-brand">
            <img class="exam-kiosk-logo" src="${ctx}/assets/imgs/csgt-footer.png" alt="Logo CSGT">
            <div>
                <p class="exam-kiosk-eyebrow">Vào thi lý thuyết</p>
                <h1>Nhập số báo danh</h1>
            </div>
        </div>
        <p class="exam-kiosk-subtitle">Thí sinh chỉ vào được khi đã được sát hạch viên điểm danh.</p>
        <c:if test="${not empty error}">
            <p class="exam-kiosk-alert" role="alert"><c:out value="${error}"/></p>
        </c:if>
        <form class="exam-kiosk-form" action="${ctx}/exam/entrance" method="post">
            <label for="sbdInput">Số báo danh</label>
            <div class="exam-kiosk-input exam-kiosk-input--large">
                <span class="material-symbols-outlined">pin</span>
                <input id="sbdInput" name="sbd" type="text"
                       autocomplete="off" maxlength="50" required autofocus
                       value="<c:out value='${param.sbd}'/>">
            </div>
            <button type="submit" class="exam-kiosk-button">
                <span class="material-symbols-outlined">how_to_reg</span>
                Kiểm tra thông tin
            </button>
        </form>
    </section>
</main>
</body>
</html>

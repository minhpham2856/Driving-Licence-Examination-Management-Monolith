<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Vào thi</title>
    <link href="${pageContext.request.contextPath}/assets/css/exam/exam-entrance.css" rel="stylesheet">
</head>
<body>
<main class="entrance-shell">
    <section class="entrance-panel" aria-label="Xác thực vào thi">
        <form class="sbd-card" action="${pageContext.request.contextPath}/exam/entrance" method="post">
            <h1>VÀO THI LÝ THUYẾT</h1>
            <c:if test="${not empty error}">
                <p class="exam-entrance-error"><c:out value="${error}"/></p>
            </c:if>
            <label class="sbd-label" for="sbdInput">SỐ BÁO DANH (SBD)</label>
            <div class="sbd-input-wrap">
                <input id="sbdInput" name="sbd" class="sbd-input" type="text"
                       autocomplete="username" maxlength="50" required value="<c:out value='${param.sbd}'/>">
            </div>
            <label class="sbd-label" for="otpInput">MÃ OTP</label>
            <div class="sbd-input-wrap">
                <input id="otpInput" name="otp" class="sbd-input" type="text"
                       inputmode="numeric" autocomplete="one-time-code" pattern="[0-9]{6}"
                       maxlength="6" required aria-describedby="otpHelp">
            </div>
            <small id="otpHelp">Nhập mã 6 số do sát hạch viên cung cấp.</small>
            <button type="submit" class="check-button">KIỂM TRA THÔNG TIN</button>
        </form>
    </section>
</main>
</body>
</html>

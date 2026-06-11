<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Chuyển đến SEPay - Lái Vui</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/style.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/layout.css">
</head>
<body class="has-side-nav-bar">
<jsp:include page="/views/layout/sidebar-registrant.jsp">
    <jsp:param name="activeSidebar" value="register-exam" />
</jsp:include>
<div class="dashboard-shell">
    <main class="main-content" id="main-content">
        <header class="page-header">
            <h1 class="page-title">Đang chuyển đến cổng thanh toán SEPay</h1>
            <p class="page-subtitle">Vui lòng đợi trong giây lát. Nếu không tự chuyển, bấm nút bên dưới.</p>
        </header>
        <form id="sepayCheckoutForm" method="POST" action="${sepayCheckoutUrl}">
            <c:forEach var="entry" items="${sepayCheckoutFields}">
                <input type="hidden" name="${entry.key}" value="${entry.value}">
            </c:forEach>
            <button type="submit" class="payment-submit-btn">Thanh toán trên SEPay</button>
        </form>
    </main>
    <jsp:include page="/views/layout/footer.jsp" />
</div>
<script>
    document.getElementById('sepayCheckoutForm').submit();
</script>
</body>
</html>

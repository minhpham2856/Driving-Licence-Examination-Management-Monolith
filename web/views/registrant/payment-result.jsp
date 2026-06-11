<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>${paymentResultTitle} - Lái Vui</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/style.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/layout.css">
</head>
<body class="has-side-nav-bar">
<jsp:include page="/views/layout/sidebar-registrant.jsp">
    <jsp:param name="activeSidebar" value="my-exams" />
</jsp:include>
<div class="dashboard-shell">
    <main class="main-content" id="main-content">
        <header class="page-header">
            <h1 class="page-title">${paymentResultTitle}</h1>
            <p class="page-subtitle">${paymentResultMessage}</p>
        </header>
        <p>
            <a href="${pageContext.request.contextPath}/registrant/my-exams" class="btn-header-primary">Xem kỳ thi của tôi</a>
            <a href="${pageContext.request.contextPath}/registrant/register-exam" class="btn-header-secondary" style="margin-left:0.5rem;">Đăng ký thêm</a>
        </p>
    </main>
    <jsp:include page="/views/layout/footer.jsp" />
</div>
</body>
</html>

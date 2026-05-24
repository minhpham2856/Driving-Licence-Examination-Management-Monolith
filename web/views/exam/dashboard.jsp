<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Quản lý thi - DS thí sinh</title>
    <link rel="preconnect" href="https://fonts.googleapis.com">
    <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
    <link href="https://fonts.googleapis.com/css2?family=Inter:wght@400;600;700;800&display=swap" rel="stylesheet">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/style.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/layout.css">
</head>
<body class="has-side-nav-bar">

<jsp:include page="/WEB-INF/views/layout/sidebar.jsp">
    <jsp:param name="activeSidebar" value="ds-thi-sinh" />
</jsp:include>

<div class="dashboard-shell">
    <main class="main-content">
        <h1 style="font-size: 1.5rem; font-weight: 800; color: #003d9b; margin-bottom: 1rem;">Danh sách thí sinh</h1>
        <p style="color: #434654;">Khu vực nội dung quản lý thi — sidebar và footer đã được style theo Figma.</p>
    </main>

    <jsp:include page="/WEB-INF/views/layout/footer.jsp">
        <jsp:param name="standalone" value="false" />
    </jsp:include>
</div>

</body>
</html>

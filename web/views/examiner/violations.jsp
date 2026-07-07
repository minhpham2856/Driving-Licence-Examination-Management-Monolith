<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>

<!--variables-->
<c:set var="ctx" value="${pageContext.request.contextPath}" />
<c:set var="headerTitle" value="Vi phạm" />
<c:set var="pageUrl" value="${ctx}/views/examiner/violations" scope="request" />
<c:set var="violationDetailUrl" value="${ctx}/views/examiner/violation-detail" scope="request" />

<!--page-->
<!DOCTYPE html>
<html lang="vi">
    <head>
        <meta charset="UTF-8">
        <meta name="viewport" content="width=device-width, initial-scale=1.0">
        <title>SÁT HẠCH</title>
        <link rel="preconnect" href="https://fonts.googleapis.com">
        <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
        <link href="https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700&family=JetBrains+Mono:wght@500;600;700&display=swap" rel="stylesheet">
        <link href="https://fonts.googleapis.com/css2?family=Material+Symbols+Outlined:opsz,wght,FILL,GRAD@24,400,0,0&display=swap" rel="stylesheet">
        <jsp:include page="/views/examiner/components/examiner-styles.jsp" />
    </head>
    <body class="has-side-nav-bar examiner-portal${empty examinerHasActiveSession or not examinerHasActiveSession ? ' examiner-portal--inactive' : ''}">

        <!--sidebar-->
        <jsp:include page="/views/examiner/components/sidebar.jsp">
            <jsp:param name="activeSidebar" value="violations" />
        </jsp:include>

        <!--shell-->
        <div class="examiner-shell">

            <!--header-->
            <jsp:include page="/views/examiner/components/header.jsp" />

            <!--main content-->
            <main class="examiner-main examiner-main--dashboard">

                <!--action message-->
                <jsp:include page="/views/examiner/components/examiner-messages.jsp" />

                <!--toolbar-->
                <jsp:include page="/views/examiner/components/toolbar.jsp">
                    <jsp:param name="btnSearch" value="right" />
                    <jsp:param name="searchPlaceholder" value="Tìm kiếm SBD, tên..." />
                    <jsp:param name="btnRefresh" value="right" />
                </jsp:include>

                <!--violation list-->
                <jsp:include page="/views/examiner/components/candidate-list.jsp">
                    <jsp:param name="title" value="Danh sách thí sinh" />
                    <jsp:param name="badgeText" value="Tổng: ${fn:length(candidates)} thí sinh" />
                    <jsp:param name="showStatus" value="true" />
                    <jsp:param name="showAddress" value="false" />
                    <jsp:param name="actionViewViolation" value="true" />
                </jsp:include>
            </main>
        </div>
    </body>
</html>

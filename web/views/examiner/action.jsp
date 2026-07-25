<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<c:set var="ctx" value="${pageContext.request.contextPath}" />
<c:set var="headerTitle" value="Thao tác" />
<c:set var="pageUrl" value="${ctx}/examiner/action" scope="request" />
<c:set var="detailViewUrl" value="${ctx}/examiner/candidate-details" scope="request" />
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Thao tác sát hạch</title>
    <link href="https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700&family=JetBrains+Mono:wght@600;700;800&display=swap" rel="stylesheet">
    <link href="https://fonts.googleapis.com/css2?family=Material+Symbols+Outlined" rel="stylesheet">
    <jsp:include page="/views/examiner/components/examiner-styles.jsp">
        <jsp:param name="pageCss" value="action.css" />
    </jsp:include>
</head>
<body class="has-side-nav-bar examiner-portal"
      data-context-path="${ctx}/examiner/">
<jsp:include page="/views/layout/sidebar-examiner.jsp">
    <jsp:param name="activeSidebar" value="action" />
</jsp:include>
<div class="examiner-shell">
    <jsp:include page="/views/layout/header-examiner.jsp" />
    <main class="examiner-main examiner-main--dashboard">
        <jsp:include page="/views/examiner/components/examiner-messages.jsp" />
        <jsp:include page="/views/examiner/components/toolbar.jsp">
            <jsp:param name="btnSearch" value="right" />
            <jsp:param name="searchWide" value="true" />
            <jsp:param name="searchPlaceholder" value="Tìm SBD, tên, số căn cước..." />
            <jsp:param name="btnRefresh" value="right" />
        </jsp:include>

        <jsp:include page="/views/examiner/components/candidate-list.jsp">
            <jsp:param name="title" value="Danh sách thí sinh" />
            <jsp:param name="layoutActionBoard" value="true" />
            <jsp:param name="showSbd" value="true" />
            <jsp:param name="showName" value="true" />
            <jsp:param name="showStatus" value="true" />
            <jsp:param name="showExamScore" value="true" />
            <jsp:param name="actionAttendance" value="true" />
            <jsp:param name="actionCall" value="true" />
            <jsp:param name="actionSuspend" value="true" />
            <jsp:param name="actionScoreEntry" value="true" />
            <jsp:param name="actionPrint" value="true" />
            <jsp:param name="actionComplete" value="true" />
            <jsp:param name="actionEditResult" value="true" />
        </jsp:include>

    </main>
</div>
<script src="${ctx}/assets/js/examiner-action.js"></script>
</body>
</html>

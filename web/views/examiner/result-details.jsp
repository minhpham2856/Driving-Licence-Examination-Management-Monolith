<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>

<!--variables-->
<c:set var="ctx" value="${pageContext.request.contextPath}" />
<c:set var="headerTitle" value="Sửa kết quả" />
<c:set var="pageUrl" value="${ctx}/examiner/result-details" scope="request" />
<c:set var="editUrl" value="${ctx}/examiner/result-details-edit" scope="request" />
<c:set var="exportResultUrl" value="${ctx}/examiner/export/result" scope="request" />
<c:set var="exportResultsUrl" value="${exportResultUrl}" />
<c:set var="exportCandidatesUrl" value="${ctx}/examiner/export/candidates" scope="request" />
<c:set var="exportDocxUrl" value="${ctx}/examiner/export/docx" />

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
    <body class="has-side-nav-bar examiner-portal${empty examinerHasActiveExam or not examinerHasActiveExam ? ' examiner-portal--inactive' : ''}">

        <!--sidebar-->
        <jsp:include page="/views/layout/sidebar-examiner.jsp">
            <jsp:param name="activeSidebar" value="result-details" />
        </jsp:include>

        <!--shell-->
        <div class="examiner-shell">

            <!--header-->
            <jsp:include page="/views/layout/header-examiner.jsp" />

            <!--main content-->
            <main class="examiner-main examiner-main--dashboard">

                <!--toolbar-->
                <jsp:include page="/views/examiner/components/toolbar.jsp">
                    <jsp:param name="btnPrintInfo" value="left" />
                    <jsp:param name="btnExportExcel" value="left" />
                    <jsp:param name="btnPrintList" value="left" />
                    <jsp:param name="btnExportDocx" value="left" />
                    <jsp:param name="btnExportCandidatesExcel" value="left" />
                    <jsp:param name="btnPrintResult" value="left" />
                    <jsp:param name="btnSearch" value="right" />
                    <jsp:param name="searchPlaceholder" value="Tìm kiếm SBD, Tên..." />
                    <jsp:param name="btnRefresh" value="right" />
                </jsp:include>

                <!--result list-->
                <jsp:include page="/views/examiner/components/candidate-list.jsp">
                    <jsp:param name="title" value="Danh sách kết quả" />
                    <jsp:param name="showCheckbox" value="true" />
                    <jsp:param name="showAddress" value="false" />
                    <jsp:param name="showTheoryScores" value="true" />
                    <jsp:param name="showResult" value="true" />
                    <jsp:param name="actionEditResult" value="true" />
                </jsp:include>
            </main>
        </div>

    </body>
</html>

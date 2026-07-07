<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>

<!--variables-->
<c:set var="ctx" value="${pageContext.request.contextPath}" />
<c:set var="headerTitle" value="Đề thi" />
<c:if test="${not empty candidate}">
    <c:set var="headerTitle" value="Đề thi - ${candidate.fullName}" />
</c:if>
<c:set var="backUrl" value="${ctx}/views/examiner/candidate-details-edit?sbd=${candidate.sbd}" scope="request" />
<c:set var="pageUrl" value="${ctx}/views/examiner/candidate-paper?sbd=${candidate.sbd}" scope="request" />
<c:set var="paperExportUrl" value="${ctx}/examiner/export/docx?type=BB1&amp;sbd=${candidate.sbd}" scope="request" />
<c:set var="paperPrintUrl" value="${ctx}/examiner/print/docx?type=BB1&amp;sbd=${candidate.sbd}" scope="request" />

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
        <jsp:include page="/views/examiner/components/examiner-styles.jsp">
            <jsp:param name="pageCss" value="paper.css,result-edit.css" />
        </jsp:include>
    </head>
    <body class="has-side-nav-bar examiner-portal${empty examinerHasActiveSession or not examinerHasActiveSession ? ' examiner-portal--inactive' : ''}">

        <!--sidebar-->
        <jsp:include page="/views/examiner/components/sidebar.jsp">
            <jsp:param name="activeSidebar" value="candidate-details" />
        </jsp:include>

        <!--shell-->
        <div class="examiner-shell">

            <!--header-->
            <jsp:include page="/views/examiner/components/header.jsp" />

            <!--main content-->
            <main class="examiner-main examiner-main--scroll">

                <!--toolbar-->
                <jsp:include page="/views/examiner/components/toolbar.jsp">
                    <jsp:param name="wrapperClass" value="examiner-toolbar" />
                    <jsp:param name="leftClass" value="exr-toolbar-left" />
                    <jsp:param name="rightClass" value="examiner-toolbar__actions" />
                    <jsp:param name="backClass" value="exr-back" />
                    <jsp:param name="btnBack" value="left" />
                    <jsp:param name="btnPaperExport" value="left" />
                    <jsp:param name="btnPaperPrint" value="left" />
                    <jsp:param name="btnPaperFilter" value="right" />
                    <jsp:param name="btnRefresh" value="right" />
                </jsp:include>

                <!--paper table-->
                <jsp:include page="/views/examiner/components/paper-result.jsp" />
            </main>
        </div>
    </body>
</html>

<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>

<%--context variable--%>
<c:set var="ctx" value="${pageContext.request.contextPath}" />

<%--page--%>
<!DOCTYPE html>
<html lang="vi">
    <head>
        <jsp:include page="/views/examiner/components/head.jsp">
            <jsp:param name="pageCss" value="export.css,print.css" />
        </jsp:include>
    </head>
    <body class="has-side-nav-bar portal${empty examinerHasActiveExam or not examinerHasActiveExam ? ' inactive' : ''}">

        <jsp:include page="/views/layout/sidebar-examiner.jsp">
            <jsp:param name="activeSidebar" value="print-documents" />
        </jsp:include>

        <div class="shell">
            <jsp:include page="/views/layout/header-examiner.jsp">
                <jsp:param name="title" value="Biên bản" />
            </jsp:include>
            <main class="main scroll">
                <jsp:include page="/views/examiner/components/messages.jsp" />
                <div class="export-card">
                    <jsp:include page="/views/examiner/components/document-rows.jsp" />
                </div>
            </main>
        </div>
    </body>
</html>

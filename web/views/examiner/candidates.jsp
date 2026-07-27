<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>

<%-- variables--%>
<c:set var="ctx" value="${pageContext.request.contextPath}" />
<c:set var="pageUrl" value="${ctx}/examiner/candidates" scope="request" />

<%--page--%>
<!DOCTYPE html>
<html lang="vi">
    <head>
        <jsp:include page="/views/examiner/components/head.jsp" />
    </head>
    <body class="has-side-nav-bar portal${empty examinerHasActiveExam or not examinerHasActiveExam ? ' inactive' : ''}">

        <%--sidebar--%>
        <jsp:include page="/views/layout/sidebar-examiner.jsp">
            <jsp:param name="activeSidebar" value="candidates" />
        </jsp:include>

        <div class="shell">

            <%--header--%>
            <jsp:include page="/views/layout/header-examiner.jsp">
                <jsp:param name="title" value="Thông tin thí sinh" />
            </jsp:include>

            <%--main content--%>
            <main class="main dash">

                <%--toolbar--%>
                <section class="toolbar toolbar-tools">
                    <div class="toolbar-group"></div>
                    <div class="toolbar-group search-form">
                        <jsp:include page="/views/examiner/components/search-form.jsp">
                            <jsp:param name="placeholder" value="Tìm kiếm SBD, tên, số căn cước..." />
                        </jsp:include>
                        <a href="${pageUrl}"
                           class="btn white icon-only"
                           title="Làm mới">
                            <span class="material-symbols-outlined">refresh</span>
                        </a>
                    </div>
                </section>

                <jsp:include page="/views/examiner/components/candidates-roster.jsp" />
            </main>
        </div>
    </body>
</html>

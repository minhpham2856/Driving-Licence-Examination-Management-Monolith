<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>

<%-- variables--%>
<c:set var="ctx" value="${pageContext.request.contextPath}" />
<c:set var="pageUrl" value="${ctx}/examiner/action" scope="request" />

<!DOCTYPE html>
<html lang="vi">
    <head>
        <jsp:include page="/views/examiner/components/head.jsp">
            <jsp:param name="pageCss" value="action.css" />
        </jsp:include>
    </head>
    <body class="has-side-nav-bar portal"
          data-context-path="${ctx}/examiner/">
        <jsp:include page="/views/layout/sidebar-examiner.jsp">
            <jsp:param name="activeSidebar" value="action" />
        </jsp:include>
        <div class="shell">
            <jsp:include page="/views/layout/header-examiner.jsp">
                <jsp:param name="title" value="Thao tác" />
            </jsp:include>
            <main class="main dash">
                <jsp:include page="/views/examiner/components/messages.jsp" />
                <section class="toolbar toolbar-tools">
                    <div class="toolbar-group"></div>
                    <div class="toolbar-group search-form">
                        <jsp:include page="/views/examiner/components/search-form.jsp">
                            <jsp:param name="wide" value="true" />
                            <jsp:param name="placeholder" value="Tìm SBD, tên, số căn cước..." />
                        </jsp:include>
                        <a href="${pageUrl}"
                           class="btn white icon-only"
                           title="Làm mới">
                            <span class="material-symbols-outlined">refresh</span>
                        </a>
                    </div>
                </section>

                <c:choose>
                    <%--case 1: theory roster--%>
                    <c:when test="${examinerSectionTheory}">
                        <jsp:include page="/views/examiner/components/theory-roster.jsp" />
                    </c:when>

                    <%--case 2: practical roster--%>
                    <c:otherwise>
                        <jsp:include page="/views/examiner/components/layout-roster.jsp" />
                    </c:otherwise>
                </c:choose>

            </main>
        </div>
        <script src="${ctx}/assets/js/examiner-action.js"></script>
    </body>
</html>

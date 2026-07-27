<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>

<%-- variables--%>
<c:set var="ctx" value="${pageContext.request.contextPath}" />
<c:set var="pageUrl" value="${ctx}/examiner/audit" scope="request" />

<!DOCTYPE html>
<html lang="vi">
    <head>
        <jsp:include page="/views/examiner/components/head.jsp">
            <jsp:param name="pageCss" value="audit.css" />
        </jsp:include>
    </head>
    <body class="has-side-nav-bar portal${empty examinerHasActiveExam or not examinerHasActiveExam ? ' inactive' : ''}">
        <jsp:include page="/views/layout/sidebar-examiner.jsp">
            <jsp:param name="activeSidebar" value="audit" />
        </jsp:include>

        <div class="shell">
            <jsp:include page="/views/layout/header-examiner.jsp">
                <jsp:param name="title" value="Nhật ký" />
            </jsp:include>

            <%-- toolbar --%>
            <main class="main dash">
                <section class="toolbar toolbar-tools">
                    <div class="toolbar-group"></div>

                    <%-- actions 1 --%>
                    <div class="toolbar-group search-form">

                        <%-- search --%>
                        <jsp:include page="/views/examiner/components/search-form.jsp">
                            <jsp:param name="wide" value="true" />
                            <jsp:param name="placeholder" value="Tìm kiếm..." />
                        </jsp:include>

                        <%-- refresh --%>
                        <a href="${pageUrl}"
                           class="btn white icon-only"
                           title="Làm mới">
                            <span class="material-symbols-outlined">refresh</span>
                        </a>
                    </div>
                </section>

                <div class="audit-card">
                    <div class="table-wrap">
                        <table class="table audit-table">
                            <%--headers--%>
                            <thead>
                                <tr>
                                    <th>Người dùng</th>
                                    <th>Thao tác</th>
                                    <th>Chi tiết</th>
                                    <th>Thời gian</th>
                                </tr>
                            </thead>

                            <%--body--%>
                            <tbody>
                                <c:choose>
                                    <%--case 1: empty logs--%>
                                    <c:when test="${empty auditLogs}">
                                        <tr>
                                            <td colspan="4" class="table-empty">Chưa có dữ liệu</td>
                                        </tr>
                                    </c:when>

                                    <%--case 2: has logs--%>
                                    <c:otherwise>
                                        <c:forEach items="${auditLogs}" var="log">
                                            <tr>
                                                <td>${log.username}</td>
                                                <td>
                                                    <span class="audit-badge ${log.actionBadge}">
                                                        ${log.actionLabel}
                                                    </span>
                                                </td>
                                                <td class="audit-td-detail">${log.detail}</td>
                                                <td class="table-mono-md">${log.timestamp}</td>
                                            </tr>
                                        </c:forEach>
                                    </c:otherwise>
                                </c:choose>
                            </tbody>
                        </table>
                    </div>

                    <div class="audit-pagination">
                        <nav class="audit-page-nav">
                            <%--prev / next page urls--%>
                            <c:url var="prevUrl" value="/examiner/audit">
                                <c:param name="page" value="${auditPage - 1}" />
                                <c:if test="${not empty searchQuery}">
                                    <c:param name="q" value="${searchQuery}" />
                                </c:if>
                            </c:url>
                            <c:url var="nextUrl" value="/examiner/audit">
                                <c:param name="page" value="${auditPage + 1}" />
                                <c:if test="${not empty searchQuery}">
                                    <c:param name="q" value="${searchQuery}" />
                                </c:if>
                            </c:url>
                            <c:choose>
                                <%--case 1: prev enabled--%>
                                <c:when test="${auditPage > 1}">
                                    <a href="${prevUrl}" class="audit-page-btn audit-page-btn-nav">
                                        <span class="material-symbols-outlined">chevron_left</span>
                                    </a>
                                </c:when>

                                <%--case 2: prev disabled--%>
                                <c:otherwise>
                                    <span class="audit-page-btn audit-page-btn-nav grey-out">
                                        <span class="material-symbols-outlined">chevron_left</span>
                                    </span>
                                </c:otherwise>
                            </c:choose>
                            <span class="audit-page-btn audit-page-btn-active">${auditPage} / ${auditTotalPages}</span>
                            <c:choose>
                                <%--case 1: next enabled--%>
                                <c:when test="${auditPage < auditTotalPages}">
                                    <a href="${nextUrl}" class="audit-page-btn audit-page-btn-nav">
                                        <span class="material-symbols-outlined">chevron_right</span>
                                    </a>
                                </c:when>

                                <%--case 2: next disabled--%>
                                <c:otherwise>
                                    <span class="audit-page-btn audit-page-btn-nav grey-out">
                                        <span class="material-symbols-outlined">chevron_right</span>
                                    </span>
                                </c:otherwise>
                            </c:choose>
                        </nav>
                    </div>
                </div>
            </main>
        </div>
    </body>
</html>

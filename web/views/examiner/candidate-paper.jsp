<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>

<%-- variables--%>
<c:set var="ctx" value="${pageContext.request.contextPath}" />
<c:set var="paperHeaderTitle" value="Đề thi" />
<c:if test="${not empty candidate}">
    <c:set var="paperHeaderTitle" value="Đề thi - ${candidate.fullName}" />
</c:if>

<%--urls--%>
<c:set var="backUrl"
       value="${ctx}/examiner/candidate-details?sbd=${candidate.candidateNumber}"
       scope="request" />
<c:set var="pageUrl"
       value="${ctx}/examiner/candidate-paper?sbd=${candidate.candidateNumber}"
       scope="request" />

<%--page--%>
<!DOCTYPE html>
<html lang="vi">
    <head>
        <jsp:include page="/views/examiner/components/head.jsp">
            <jsp:param name="pageCss" value="paper.css,result-edit.css" />
        </jsp:include>
    </head>
    <body class="has-side-nav-bar portal${empty examinerHasActiveExam or not examinerHasActiveExam ? ' inactive' : ''}">

        <%--sidebar--%>
        <jsp:include page="/views/layout/sidebar-examiner.jsp">
            <jsp:param name="activeSidebar" value="candidate-details" />
        </jsp:include>

        <%--shell--%>
        <div class="shell">

            <%--header--%>
            <jsp:include page="/views/layout/header-examiner.jsp">
                <jsp:param name="title" value="${paperHeaderTitle}" />
            </jsp:include>

            <%--main content--%>
            <main class="main scroll">

                <%--toolbar--%>
                <section class="toolbar">
                    <div class="toolbar-left">
                        <a href="${backUrl}" class="back">
                            <span class="material-symbols-outlined">arrow_back</span>Quay lại
                        </a>
                    </div>
                    <div class="toolbar-actions">
                        <%--filter / sort query for paper tabs--%>
                        <c:set var="currentFilter"
                               value="${empty param.filter ? 'all' : param.filter}" />
                        <c:set var="sortQuery"
                               value="${not empty param.sort ? '&amp;sort='.concat(param.sort) : ''}${not empty param.dir ? '&amp;dir='.concat(param.dir) : ''}" />
                        <div class="paper-filter-tabs">
                            <a href="${pageUrl}&amp;filter=all${sortQuery}"
                               class="paper-filter-tab paper-filter-tab-all ${currentFilter == 'all' ? 'is-active' : ''}">
                                <span class="material-symbols-outlined">apps</span>Tất cả (${empty paperSummary.totalCount ? 0 : paperSummary.totalCount})
                            </a>
                            <a href="${pageUrl}&amp;filter=correct${sortQuery}"
                               class="paper-filter-tab paper-filter-tab-correct ${currentFilter == 'correct' ? 'is-active' : ''}">
                                <span class="material-symbols-outlined">check</span>Câu đúng (${empty paperSummary.correctCount ? 0 : paperSummary.correctCount})
                            </a>
                            <a href="${pageUrl}&amp;filter=wrong${sortQuery}"
                               class="paper-filter-tab paper-filter-tab-wrong ${currentFilter == 'wrong' ? 'is-active' : ''}">
                                <span class="material-symbols-outlined">close</span>Câu sai (${empty paperSummary.wrongCount ? 0 : paperSummary.wrongCount})
                            </a>
                            <a href="${pageUrl}&amp;filter=unanswered${sortQuery}"
                               class="paper-filter-tab paper-filter-tab-skipped ${currentFilter == 'unanswered' ? 'is-active' : ''}">
                                <span class="material-symbols-outlined">remove</span>Bỏ (${empty paperSummary.unansweredCount ? 0 : paperSummary.unansweredCount})
                            </a>
                        </div>
                        <a href="${pageUrl}"
                           class="btn white icon-only"
                           title="Làm mới">
                            <span class="material-symbols-outlined">refresh</span>
                        </a>
                    </div>
                </section>

                <%--paper table--%>
                <jsp:include page="/views/examiner/components/paper-result.jsp" />
            </main>
        </div>
    </body>
</html>

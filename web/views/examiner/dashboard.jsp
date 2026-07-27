<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>

<%-- variables--%>
<c:set var="ctx" value="${pageContext.request.contextPath}" />
<c:set var="pageUrl" value="${ctx}/examiner/dashboard" scope="request" />

<%--page--%>
<!DOCTYPE html>
<html lang="vi">
    <head>
        <jsp:include page="/views/examiner/components/head.jsp">
            <jsp:param name="pageCss" value="dashboard.css" />
        </jsp:include>
    </head>
    <body class="has-side-nav-bar portal${empty examinerHasActiveExam or not examinerHasActiveExam ? ' inactive' : ''}">

        <%--sidebar--%>
        <jsp:include page="/views/layout/sidebar-examiner.jsp">
            <jsp:param name="activeSidebar" value="dashboard" />
        </jsp:include>

        <%--shell--%>
        <div class="shell">

            <%--header--%>
            <jsp:include page="/views/layout/header-examiner.jsp">
                <jsp:param name="title" value="Bảng điều khiển" />
            </jsp:include>

            <%--main content--%>
            <main class="main dash">

                <%--toolbar--%>
                <section class="toolbar toolbar-tools">
                    <div class="toolbar-group"></div>
                    <div class="toolbar-group search-form">
                        <jsp:include page="/views/examiner/components/search-form.jsp">
                            <jsp:param name="wide" value="true" />
                            <jsp:param name="placeholder" value="Tìm kiếm SBD, Tên, Căn cước..." />
                        </jsp:include>
                        <a href="${pageUrl}"
                           class="btn white icon-only"
                           title="Làm mới">
                            <span class="material-symbols-outlined">refresh</span>
                        </a>
                    </div>
                </section>

                <jsp:include page="/views/examiner/components/dashboard-roster.jsp" />

                <%--statistics--%>
                <section class="summary summary-dashboard">
                    <div class="summary-grid">
                        <div class="summary-course">
                            <p class="summary-label">Kỳ thi</p>
                            <p class="summary-value">
                                ${empty examSummary.examCode ? '' : examSummary.examCode}
                            </p>
                            <div class="summary-meta-row">
                                <p class="summary-label">Ngày thi</p>
                                <p class="summary-value sm">
                                    ${empty examSummary.examDate ? '' : examSummary.examDate}
                                </p>
                            </div>
                            <div class="summary-meta-row">
                                <p class="summary-label">Hạng GPLX</p>
                                <p class="summary-value sm">
                                    ${empty examSummary.licenceClass ? '' : examSummary.licenceClass}
                                </p>
                            </div>
                        </div>
                        <div class="summary-stat total">
                            <p class="summary-label">Tổng số</p>
                            <p class="summary-value sm">
                                ${empty examSummary.total ? 0 : examSummary.total}
                            </p>
                        </div>
                        <div class="summary-stat summary-stat-done">
                            <p class="summary-label">Đã thi</p>
                            <p class="summary-value sm blue">
                                ${empty examSummary.done ? 0 : examSummary.done}
                            </p>
                        </div>
                        <div class="summary-stat testing">
                            <p class="summary-label">Đang thi</p>
                            <p class="summary-value sm amber">
                                ${empty examSummary.testing ? 0 : examSummary.testing}
                            </p>
                        </div>
                        <div class="summary-stat pending">
                            <p class="summary-label">Chưa thi</p>
                            <p class="summary-value sm">
                                ${empty examSummary.pending ? 0 : examSummary.pending}
                            </p>
                        </div>
                        <div class="summary-stat summary-stat-pass">
                            <p class="summary-label">Thi đạt</p>
                            <p class="summary-value sm green">
                                ${empty examSummary.passed ? 0 : examSummary.passed}
                            </p>
                        </div>
                        <div class="summary-stat summary-stat-fail">
                            <p class="summary-label">Thi trượt</p>
                            <p class="summary-value sm summary-value-red">
                                ${empty examSummary.failed ? 0 : examSummary.failed}
                            </p>
                        </div>
                    </div>
                </section>
            </main>
        </div>

    </body>
</html>

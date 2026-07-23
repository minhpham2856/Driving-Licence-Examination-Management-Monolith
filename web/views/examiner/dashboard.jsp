<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>

<!--variables-->
<c:set var="ctx" value="${pageContext.request.contextPath}" />
<c:set var="headerTitle" value="Bảng điều khiển" />
<c:set var="pageUrl" value="${ctx}/examiner/dashboard" scope="request" />

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
            <jsp:param name="pageCss" value="dashboard.css" />
        </jsp:include>
    </head>
    <body class="has-side-nav-bar
          examiner-portal${empty examinerHasActiveExam or not examinerHasActiveExam ? ' examiner-portal--inactive' : ''}">

        <!--sidebar-->
        <jsp:include page="/views/layout/sidebar-examiner.jsp">
            <jsp:param name="activeSidebar" value="dashboard" />
        </jsp:include>

        <!--shell-->
        <div class="examiner-shell">

            <!--header-->
            <jsp:include page="/views/layout/header-examiner.jsp" />

            <!--main content-->
            <main class="examiner-main examiner-main--dashboard">

                <!--toolbar-->
                <jsp:include page="/views/examiner/components/toolbar.jsp">
                    <jsp:param name="btnSearch" value="right" />
                    <jsp:param name="searchWide" value="true" />
                    <jsp:param name="searchPlaceholder" value="Tìm kiếm SBD, Tên, Căn cước..." />
                    <jsp:param name="btnRefresh" value="right" />
                </jsp:include>


                <!--candidate list-->
                <jsp:include page="/views/examiner/components/candidate-list.jsp">
                    <jsp:param name="title" value="Danh sách thí sinh" />
                    <jsp:param name="showTheoryScores" value="false" />
                    <jsp:param name="showExamScore" value="true" />
                    <jsp:param name="showResult" value="true" />
                    <jsp:param name="showStatus" value="true" />
                    <jsp:param name="showAddress" value="false" />
                </jsp:include>

                <!--statistics-->
                <section class="examiner-summary examiner-summary--dashboard">
                    <div class="examiner-summary__grid">
                        <div class="examiner-summary__course">
                            <p class="examiner-summary__label">Kỳ thi</p>
                            <p class="examiner-summary__value">${empty examSummary.examCode ? '—' : examSummary.examCode}</p>
                            <div class="examiner-summary__meta-row">
                                <p class="examiner-summary__label">Ngày thi</p>
                                <p class="examiner-summary__value examiner-summary__value--sm">${empty examSummary.examDate ? '—' : examSummary.examDate}</p>
                            </div>
                            <div class="examiner-summary__meta-row">
                                <p class="examiner-summary__label">Hạng GPLX</p>
                                <p class="examiner-summary__value examiner-summary__value--sm">${empty examSummary.licenceClass ? '—' : examSummary.licenceClass}</p>
                            </div>
                        </div>
                        <div class="examiner-summary__stat examiner-summary__stat--total">
                            <p class="examiner-summary__label">Tổng số</p>
                            <p class="examiner-summary__value examiner-summary__value--sm">${empty examSummary.total ? 0 : examSummary.total}</p>
                        </div>
                        <div class="examiner-summary__stat examiner-summary__stat--done">
                            <p class="examiner-summary__label">Đã thi</p>
                            <p class="examiner-summary__value examiner-summary__value--sm examiner-summary__value--blue">${empty examSummary.done ? 0 : examSummary.done}</p>
                        </div>
                        <div class="examiner-summary__stat examiner-summary__stat--testing">
                            <p class="examiner-summary__label">Đang thi</p>
                            <p class="examiner-summary__value examiner-summary__value--sm examiner-summary__value--amber">${empty examSummary.testing ? 0 : examSummary.testing}</p>
                        </div>
                        <div class="examiner-summary__stat examiner-summary__stat--pending">
                            <p class="examiner-summary__label">Chưa thi</p>
                            <p class="examiner-summary__value examiner-summary__value--sm">${empty examSummary.pending ? 0 : examSummary.pending}</p>
                        </div>
                        <div class="examiner-summary__stat examiner-summary__stat--pass">
                            <p class="examiner-summary__label">Thi đạt</p>
                            <p class="examiner-summary__value examiner-summary__value--sm examiner-summary__value--green">${empty examSummary.passed ? 0 : examSummary.passed}</p>
                        </div>
                        <div class="examiner-summary__stat examiner-summary__stat--fail">
                            <p class="examiner-summary__label">Thi trượt</p>
                            <p class="examiner-summary__value examiner-summary__value--sm examiner-summary__value--red">${empty examSummary.failed ? 0 : examSummary.failed}</p>
                        </div>
                    </div>
                </section>
            </main>
        </div>

    </body>
</html>

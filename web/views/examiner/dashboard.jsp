<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>

<!--variables-->
<c:set var="ctx" value="${pageContext.request.contextPath}" />
<c:set var="headerTitle" value="Bảng điều khiển" />
<c:set var="pageUrl" value="${ctx}/views/examiner/dashboard" scope="request" />

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
        <jsp:include page="/views/examiner/partials/examiner-styles.jsp">
            <jsp:param name="pageCss" value="dashboard.css" />
        </jsp:include>
    </head>
    <body class="has-side-nav-bar
          examiner-portal${empty examinerHasActiveSession or not examinerHasActiveSession ? ' examiner-portal--inactive' : ''}">

        <!--sidebar-->
        <jsp:include page="/views/layout/sidebar-examiner.jsp">
            <jsp:param name="activeSidebar" value="dashboard" />
        </jsp:include>

        <!--main content-->
        <div class="examiner-shell">

            <!--header-->
            <jsp:include page="/views/layout/header-examiner.jsp" />

            <main class="examiner-main examiner-main--dashboard">

                <!--toolbar-->
                <section class="examiner-toolbar examiner-toolbar--tools">

                    <!--tb.actions-->
                    <form action="${pageUrl}" method="get" class="examiner-toolbar__group examiner-toolbar__search-form">
                        <c:if test="${not empty sortBy}"><input type="hidden" name="sort" value="${sortBy}"></c:if>
                        <c:if test="${not empty sortDir}"><input type="hidden" name="dir" value="${sortDir}"></c:if>
                            <div class="examiner-search examiner-search--wide">
                                <input type="text" name="q" class="examiner-search__input"
                                       placeholder="Tìm kiếm SBD, Tên, Căn cước..."
                                       value="${searchQuery}">
                        </div>
                        <button type="submit" class="examiner-btn examiner-btn--primary">
                            <span class="material-symbols-outlined">search</span>Tìm kiếm
                        </button>
                        <a href="${pageUrl}" class="examiner-btn examiner-btn--white examiner-btn--icon" title="Làm mới">
                            <span class="material-symbols-outlined">refresh</span>
                        </a>
                    </form>
                </section>


                <!--candidate list-->
                <section class="examiner-card examiner-card--dashboard-table">
                    <div class="examiner-table-wrap">
                        <table class="examiner-table">
                            <thead>
                                <tr>
                                    <jsp:include page="/views/examiner/partials/candidate-dashboard-head.jsp" />
                                </tr>
                            </thead>
                            <tbody>
                                <c:choose>
                                    <c:when test="${empty candidates}">
                                        <tr>
                                            <td colspan="${examinerSectionTheory ? 10 : 8}" class="examiner-table__empty">
                                                <c:choose>
                                                    <c:when test="${searchActive}">Không tìm thấy thí sinh phù hợp với ${searchQuery}.</c:when>
                                                    <c:otherwise>Chưa có dữ liệu thí sinh.</c:otherwise>
                                                </c:choose>
                                            </td>
                                        </tr>
                                    </c:when>
                                    <c:otherwise>
                                        <c:forEach items="${candidates}" var="c" varStatus="st">
                                            <c:set var="candidateRow" value="${c}" scope="request" />
                                            <c:set var="rowAlt" value="${st.index % 2 == 1}" scope="request" />
                                            <jsp:include page="/views/examiner/partials/candidate-dashboard-row.jsp" />
                                        </c:forEach>
                                    </c:otherwise>
                                </c:choose>
                            </tbody>
                        </table>
                    </div>
                </section>

                <!--statistics-->
                <section class="examiner-summary examiner-summary--dashboard">
                    <div class="examiner-summary__grid">
                        <div class="examiner-summary__course">
                            <p class="examiner-summary__label">Khoá thi</p>
                            <p class="examiner-summary__value">${empty examSummary.examCode ? '—' : examSummary.examCode}</p>
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

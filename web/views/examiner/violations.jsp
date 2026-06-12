<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>

<c:set var="ctx" value="${pageContext.request.contextPath}" />
<c:set var="headerTitle" value="Vi phạm" />
<c:set var="pageUrl" value="${ctx}/views/examiner/violations" scope="request" />
<c:set var="confirmUrl" value="${ctx}/views/examiner/violation-confirm" scope="request" />
<c:set var="undoUrl" value="${ctx}/views/examiner/violation-undo" scope="request" />

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
        <jsp:include page="/views/examiner/partials/examiner-styles.jsp" />
    </head>
    <body class="has-side-nav-bar examiner-portal${empty examinerHasActiveSession or not examinerHasActiveSession ? ' examiner-portal--inactive' : ''}">

        <jsp:include page="/views/layout/sidebar-examiner.jsp">
            <jsp:param name="activeSidebar" value="vi-pham" />
        </jsp:include>

        <div class="examiner-shell">
            <jsp:include page="/views/layout/header-examiner.jsp" />

            <main class="examiner-main examiner-main--dashboard">
                <jsp:include page="/views/examiner/partials/examiner-messages.jsp" />

                <section class="examiner-toolbar examiner-toolbar--tools">
                    <form action="${pageUrl}" method="get" class="examiner-toolbar__group examiner-toolbar__search-form">
                        <c:if test="${not empty sortBy}"><input type="hidden" name="sort" value="${sortBy}"></c:if>
                        <c:if test="${not empty sortDir}"><input type="hidden" name="dir" value="${sortDir}"></c:if>
                        <div class="examiner-search">
                            <input type="text" name="q" class="examiner-search__input"
                                   placeholder="Tìm kiếm SBD, tên..."
                                   value="${searchQuery}">
                        </div>
                        <button type="submit" class="examiner-btn examiner-btn--primary">
                            <span class="material-symbols-outlined">search</span>Tìm kiếm
                        </button>
                        <a href="${pageUrl}" class="examiner-btn examiner-btn--white examiner-btn--icon">
                            <span class="material-symbols-outlined">refresh</span>
                        </a>
                    </form>
                </section>

                <section class="examiner-card examiner-card--dashboard-table">
                    <div class="examiner-card__head">
                        <h3 class="examiner-card__title">Danh sách thí sinh</h3>
                        <span class="examiner-card__badge">Tổng: ${fn:length(candidates)} thí sinh</span>
                    </div>
                    <div class="examiner-table-wrap">
                        <table class="examiner-table">
                            <thead>
                                <tr>
                                    <jsp:include page="/views/examiner/partials/sort-th.jsp"><jsp:param name="sortColumn" value="fullName" /><jsp:param name="label" value="Tên" /></jsp:include>
                                    <jsp:include page="/views/examiner/partials/sort-th.jsp"><jsp:param name="sortColumn" value="sbd" /><jsp:param name="label" value="SBD" /></jsp:include>
                                    <jsp:include page="/views/examiner/partials/sort-th.jsp"><jsp:param name="sortColumn" value="dob" /><jsp:param name="label" value="Ngày sinh" /></jsp:include>
                                    <jsp:include page="/views/examiner/partials/sort-th.jsp"><jsp:param name="sortColumn" value="address" /><jsp:param name="label" value="Địa chỉ" /></jsp:include>
                                    <jsp:include page="/views/examiner/partials/sort-th.jsp"><jsp:param name="sortColumn" value="governmentId" /><jsp:param name="label" value="Số CC" /></jsp:include>
                                    <jsp:include page="/views/examiner/partials/sort-th.jsp"><jsp:param name="sortColumn" value="status" /><jsp:param name="label" value="Tình trạng" /></jsp:include>
                                    <th>Thao tác</th>
                                </tr>
                            </thead>
                            <tbody>
                                <c:choose>
                                    <c:when test="${empty candidates}">
                                        <tr><td colspan="7" class="examiner-table__empty">
                                            <c:choose>
                                                <c:when test="${searchActive}">Không tìm thấy thí sinh phù hợp.</c:when>
                                                <c:otherwise>Chưa có dữ liệu thí sinh.</c:otherwise>
                                            </c:choose>
                                        </td></tr>
                                    </c:when>
                                    <c:otherwise>
                                        <c:forEach items="${candidates}" var="c" varStatus="st">
                                            <c:set var="candidateRow" value="${c}" scope="request" />
                                            <c:set var="rowAlt" value="${st.index % 2 == 1}" scope="request" />
                                            <jsp:include page="/views/examiner/partials/violations-row.jsp" />
                                        </c:forEach>
                                    </c:otherwise>
                                </c:choose>
                            </tbody>
                        </table>
                    </div>
                </section>
            </main>
        </div>

    </body>
</html>

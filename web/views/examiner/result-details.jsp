<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<c:set var="ctx" value="${pageContext.request.contextPath}" />
<c:set var="headerTitle" value="Sửa kết quả" />
<c:set var="pageUrl" value="${ctx}/views/examiner/result-details" scope="request" />
<c:set var="editUrl" value="${ctx}/views/examiner/result-details-edit" />

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

        <!--sidebar-->
        <jsp:include page="/views/layout/sidebar-examiner.jsp">
            <jsp:param name="activeSidebar" value="sua-ket-qua" />
        </jsp:include>

        <div class="examiner-shell">
            <!--header-->
            <jsp:include page="/views/layout/header-examiner.jsp" />

            <main class="examiner-main examiner-main--dashboard">
                <!--toolbar-->
                <section class="examiner-toolbar examiner-toolbar--tools">
                    <!--tb.left-->
                    <div class="examiner-toolbar__group">
                        <a href="#" class="examiner-btn examiner-btn--white">
                            <span class="material-symbols-outlined">print</span>
                            In thông tin chi tiết
                        </a>
                        <a href="#" class="examiner-btn examiner-btn--white">
                            <span class="material-symbols-outlined">list</span>
                            In danh sách
                        </a>
                        <a href="#" class="examiner-btn examiner-btn--white">
                            <span class="material-symbols-outlined">description</span>
                            In kết quả
                        </a>
                    </div>

                    <!--tb.right-->
                    <form action="${pageUrl}" method="get" class="examiner-toolbar__group examiner-toolbar__search-form">
                        <c:if test="${not empty sortBy}"><input type="hidden" name="sort" value="${sortBy}"></c:if>
                        <c:if test="${not empty sortDir}"><input type="hidden" name="dir" value="${sortDir}"></c:if>
                        <div class="examiner-search">
                            <input type="text" name="q" class="examiner-search__input"
                                   placeholder="Tìm kiếm SBD, Tên..."
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

                <!--result list-->
                <section class="examiner-card examiner-card--dashboard-table">
                    <div class="examiner-card__head">
                        <h3 class="examiner-card__title">Danh sách kết quả</h3>
                    </div>
                    <div class="examiner-table-wrap">
                        <table class="examiner-table">
                            <thead>
                                <tr>
                                    <th><input type="checkbox" class="examiner-check"></th>
                                    <jsp:include page="/views/examiner/partials/sort-th.jsp"><jsp:param name="sortColumn" value="fullName" /><jsp:param name="label" value="Tên" /></jsp:include>
                                    <jsp:include page="/views/examiner/partials/sort-th.jsp"><jsp:param name="sortColumn" value="sbd" /><jsp:param name="label" value="SBD" /></jsp:include>
                                    <jsp:include page="/views/examiner/partials/sort-th.jsp"><jsp:param name="sortColumn" value="dob" /><jsp:param name="label" value="Ngày sinh" /></jsp:include>
                                    <jsp:include page="/views/examiner/partials/sort-th.jsp"><jsp:param name="sortColumn" value="governmentId" /><jsp:param name="label" value="Số căn cước" /></jsp:include>
                                    <jsp:include page="/views/examiner/partials/sort-th.jsp"><jsp:param name="sortColumn" value="correct" /><jsp:param name="label" value="Đúng" /></jsp:include>
                                    <jsp:include page="/views/examiner/partials/sort-th.jsp"><jsp:param name="sortColumn" value="wrong" /><jsp:param name="label" value="Sai" /></jsp:include>
                                    <jsp:include page="/views/examiner/partials/sort-th.jsp"><jsp:param name="sortColumn" value="unanswered" /><jsp:param name="label" value="Không TL" /></jsp:include>
                                    <jsp:include page="/views/examiner/partials/sort-th.jsp"><jsp:param name="sortColumn" value="result" /><jsp:param name="label" value="Kết quả" /></jsp:include>
                                    <th>Thao tác</th>
                                </tr>
                            </thead>
                            <tbody>
                                <c:choose>
                                    <c:when test="${empty candidates}">
                                        <tr><td colspan="10" class="examiner-table__empty">
                                            <c:choose>
                                                <c:when test="${searchActive}">Không tìm thấy thí sinh phù hợp.</c:when>
                                                <c:otherwise>Chưa có dữ liệu thí sinh.</c:otherwise>
                                            </c:choose>
                                        </td></tr>
                                    </c:when>
                                    <c:otherwise>
                                <c:forEach items="${candidates}" var="c" varStatus="st">
                                    <tr<c:if test="${st.index % 2 == 1}"> class="examiner-table__row--alt"</c:if>>
                                        <td><input type="checkbox" class="examiner-check"></td>
                                        <td class="examiner-table__name">${c.fullName}</td>
                                        <td class="examiner-table__mono-md">${c.sbd}</td>
                                        <td class="examiner-table__mono-md">${c.dob}</td>
                                        <td class="examiner-table__mono-md">${c.governmentId}</td>
                                        <td class="examiner-text-green examiner-table__mono-md">${c.correct}</td>
                                        <td class="examiner-text-red examiner-table__mono-md">${c.wrong}</td>
                                        <td class="examiner-table__mono-md">${c.unanswered}</td>
                                        <td>
                                            <c:choose>
                                                <c:when test="${c.passed}"><span class="examiner-tag examiner-tag--pass">${c.resultLabel}</span></c:when>
                                                <c:when test="${c.resultLabel != '—'}"><span class="examiner-tag examiner-tag--fail">${c.resultLabel}</span></c:when>
                                                <c:otherwise>—</c:otherwise>
                                            </c:choose>
                                        </td>
                                        <td><a href="${editUrl}?sbd=${c.sbd}" class="examiner-btn examiner-btn--white examiner-btn--compact">Sửa</a></td>
                                    </tr>
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

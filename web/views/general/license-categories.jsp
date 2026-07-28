<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>

<jsp:include page="/views/layout/header.jsp">
    <jsp:param name="title" value="Lái Vui - Hạng GPLX" />
    <jsp:param name="activeNav" value="hang-bang" />
</jsp:include>

<link rel="stylesheet"
      href="${pageContext.request.contextPath}/assets/css/landing/license-categories.css">

<%--filter / sort state--%>
<c:set var="ctx" value="${pageContext.request.contextPath}" />
<c:set var="searchQuery" value="${requestScope.searchQuery}" />
<c:set var="sortBy" value="${requestScope.sortBy}" />
<c:set var="sortDir" value="${requestScope.sortDir}" />
<c:set var="feesByLicence" value="${requestScope.licenceFeesByLicenceId}" />

<%--case 1: flash error--%>
<c:if test="${not empty requestScope.error}">
    <div class="examstaff-flash examstaff-flash--error" style="max-width:1200px;margin:1rem auto;">
        <c:out value="${requestScope.error}" />
    </div>
</c:if>

<%--first-load / filter param tokens--%>
<c:set var="isFirstLoad" value="${empty param.submit}" />

<main class="public-main categories-page">
    <div class="categories-container">
        <header class="page-header">
            <h1 class="page-title">Danh mục hạng GPLX</h1>
            <p class="page-subtitle">Ba hạng bằng mô tô hiện có tại trung tâm: A1, A và B1</p>
        </header>

        <div class="workspace-layout">
            <aside class="filter-sidebar-wrap">
                <form method="GET"
                      action="${ctx}/license-categories"
                      class="filter-card">
                    <input type="hidden" name="submit" value="true">

                    <div class="filter-card__header">
                        <div class="filter-card__header-title">
                            <span class="material-symbols-outlined filter-icon" aria-hidden="true">filter_list</span>
                            <span>Bộ lọc</span>
                        </div>
                        <%--case 1: show reset when filtered--%>
                        <c:if test="${not isFirstLoad}">
                            <a href="${ctx}/license-categories" class="btn-reset-text">Xóa tất cả</a>
                        </c:if>
                    </div>

                    <div class="filter-search">
                        <input type="text"
                               name="q"
                               class="filter-search__input"
                               placeholder="Tìm hạng bằng..."
                               value="<c:out value='${searchQuery}' />">
                    </div>

                    <button type="submit" class="btn-filter-submit">Áp dụng bộ lọc</button>
                </form>
            </aside>

            <section class="results-grid-wrap">
                <div class="results-toolbar">
                    <%--case 1: search hint--%>
                    <c:if test="${not empty searchQuery}">
                        <span class="results-toolbar__hint">
                            Kết quả tìm kiếm: <strong><c:out value="${searchQuery}" /></strong>
                            <a href="${ctx}/license-categories" class="results-toolbar__clear">✕</a>
                        </span>
                    </c:if>

                    <div class="sort-bar">
                        <span class="sort-bar__label">Sắp xếp:</span>

                        <%--preserve filter query for sort links--%>
                        <c:set var="preservedParams" value="submit=true" />
                        <c:if test="${not empty searchQuery}">
                            <c:set var="preservedParams"
                                   value="${preservedParams}&q=${fn:escapeXml(searchQuery)}" />
                        </c:if>

                        <c:set var="nextDirClass"
                               value="${sortBy eq 'licenceClass' and sortDir eq 'asc' ? 'desc' : 'asc'}" />

                        <a href="?sortBy=licenceClass&sortDir=${nextDirClass}&${preservedParams}"
                           class="sort-chip ${sortBy eq 'licenceClass' ? 'sort-chip--active' : ''}
                                  ${sortBy eq 'licenceClass' and sortDir eq 'desc' ? 'sort-chip--desc' : ''}">
                            Mã hạng
                        </a>
                    </div>
                </div>

                <c:set var="hasResults" value="false" scope="page" />
                <div class="results-grid">
                    <c:forEach var="licence" items="${requestScope.licences}">
                        <c:set var="lc" value="${licence.licenceClass}" />
                        <c:set var="hasResults" value="true" scope="page" />
                        <c:set var="fees" value="${feesByLicence[licence.licenceId]}" />

                        <article class="category-card">
                            <div class="category-card__body">
                                <p class="category-card__eyebrow">Hạng bằng</p>
                                <h2 class="category-card__title">Hạng <c:out value="${lc}" /></h2>
                                <p class="category-card__desc">
                                    <c:choose>
                                        <c:when test="${empty licence.description}">
                                            Phạm vi sát hạch theo quy định hiện hành.
                                        </c:when>
                                        <c:otherwise>
                                            <c:out value="${licence.description}" />
                                        </c:otherwise>
                                    </c:choose>
                                </p>
                            </div>
                            <div class="category-card__footer">
                                <c:choose>
                                    <c:when test="${empty fees}">
                                        <div class="category-card__info-row">
                                            <span class="info-label">Phí</span>
                                            <span class="info-value">Chưa cập nhật</span>
                                        </div>
                                    </c:when>
                                    <c:otherwise>
                                        <c:forEach var="lf" items="${fees}">
                                            <div class="category-card__info-row">
                                                <span class="info-label">
                                                    <c:out value="${lf.fee.feeName}" />
                                                </span>
                                                <span class="info-value info-value--blue">
                                                    <fmt:formatNumber value="${lf.amount}"
                                                                      type="number"
                                                                      groupingUsed="true"
                                                                      maxFractionDigits="0" />đ
                                                </span>
                                            </div>
                                        </c:forEach>
                                    </c:otherwise>
                                </c:choose>
                            </div>
                        </article>
                    </c:forEach>
                </div>

                <%--case 1: empty results--%>
                <c:if test="${not hasResults}">
                    <div class="no-results-panel">
                        <h3 class="no-results-title">Không tìm thấy kết quả phù hợp</h3>
                        <p class="no-results-desc">Thử đổi từ khóa tìm kiếm hoặc đặt lại để xem các hạng A1, A, B1.</p>
                        <a href="${ctx}/license-categories" class="btn-reset-filters">Đặt lại bộ lọc</a>
                    </div>
                </c:if>
            </section>
        </div>
    </div>
</main>

<jsp:include page="/views/layout/footer.jsp" />

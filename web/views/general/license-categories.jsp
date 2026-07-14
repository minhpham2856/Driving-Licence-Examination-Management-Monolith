<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>

<jsp:include page="/views/layout/header.jsp">
    <jsp:param name="title" value="Lái Vui - Hạng GPLX" />
    <jsp:param name="activeNav" value="hang-bang" />
</jsp:include>

<link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/landing/license-categories.css">

<c:set var="ctx" value="${pageContext.request.contextPath}" />
<c:set var="searchQuery" value="${requestScope.searchQuery}" />
<c:set var="sortBy" value="${requestScope.sortBy}" />
<c:set var="sortDir" value="${requestScope.sortDir}" />

<c:if test="${not empty requestScope.error}">
    <div class="examstaff-flash examstaff-flash--error" style="max-width:1200px;margin:1rem auto;">
        <c:out value="${requestScope.error}" />
    </div>
</c:if>

<c:set var="isFirstLoad" value="${empty param.submit}" />
<c:set var="typeParams" value="${fn:join(paramValues.type, ',')}" />
<c:set var="durationParams" value="${fn:join(paramValues.duration, ',')}" />

<main class="public-main categories-page">
    <div class="categories-container">
        <header class="page-header">
            <h1 class="page-title">Danh mục hạng GPLX</h1>
            <p class="page-subtitle">Ba hạng bằng mô tô hiện có tại trung tâm: A1, A và B1</p>
        </header>

        <div class="workspace-layout">
            <aside class="filter-sidebar-wrap">
                <form method="GET" action="${ctx}/license-categories" class="filter-card">
                    <input type="hidden" name="submit" value="true">

                    <div class="filter-card__header">
                        <div class="filter-card__header-title">
                            <span class="material-symbols-outlined filter-icon" aria-hidden="true">filter_list</span>
                            <span>Bộ lọc</span>
                        </div>
                        <c:if test="${not isFirstLoad}">
                            <a href="${ctx}/license-categories" class="btn-reset-text">Xóa tất cả</a>
                        </c:if>
                    </div>

                    <div class="filter-search">
                        <input type="text" name="q" class="filter-search__input"
                               placeholder="Tìm hạng bằng..." value="<c:out value='${searchQuery}' />">
                    </div>

                    <div class="filter-group">
                        <h3 class="filter-group__title">LOẠI PHƯƠNG TIỆN</h3>
                        <div class="filter-group__options">
                            <label class="filter-option">
                                <input type="checkbox" name="type" value="moto-2"
                                       <c:if test="${fn:contains(typeParams, 'moto-2')}">checked</c:if>>
                                <span class="filter-label-text">Mô tô 2 bánh</span>
                            </label>
                            <label class="filter-option">
                                <input type="checkbox" name="type" value="moto-3"
                                       <c:if test="${fn:contains(typeParams, 'moto-3')}">checked</c:if>>
                                <span class="filter-label-text">Mô tô 3 bánh</span>
                            </label>
                        </div>
                    </div>

                    <div class="filter-group filter-group--bordered">
                        <h3 class="filter-group__title">THỜI GIAN ĐÀO TẠO</h3>
                        <div class="filter-group__options">
                            <label class="filter-option">
                                <input type="checkbox" name="duration" value="duoi-3-thang"
                                       <c:if test="${fn:contains(durationParams, 'duoi-3-thang')}">checked</c:if>>
                                <span class="filter-label-text">Dưới 3 tháng</span>
                            </label>
                            <label class="filter-option">
                                <input type="checkbox" name="duration" value="tu-3-6-thang"
                                       <c:if test="${fn:contains(durationParams, 'tu-3-6-thang')}">checked</c:if>>
                                <span class="filter-label-text">Từ 3–6 tháng</span>
                            </label>
                        </div>
                    </div>

                    <button type="submit" class="btn-filter-submit">Áp dụng bộ lọc</button>
                </form>
            </aside>

            <section class="results-grid-wrap">
                <div class="results-toolbar">
                    <c:if test="${not empty searchQuery}">
                        <span class="results-toolbar__hint">
                            Kết quả tìm kiếm: <strong><c:out value="${searchQuery}" /></strong>
                            <a href="${ctx}/license-categories" class="results-toolbar__clear">✕</a>
                        </span>
                    </c:if>

                    <div class="sort-bar">
                        <span class="sort-bar__label">Sắp xếp:</span>

                        <c:set var="preservedParams" value="submit=true" />
                        <c:if test="${not empty searchQuery}">
                            <c:set var="preservedParams" value="${preservedParams}&q=${fn:escapeXml(searchQuery)}" />
                        </c:if>
                        <c:forTokens var="t" items="${typeParams}" delims=",">
                            <c:if test="${not empty t}">
                                <c:set var="preservedParams" value="${preservedParams}&type=${t}" />
                            </c:if>
                        </c:forTokens>
                        <c:forTokens var="d" items="${durationParams}" delims=",">
                            <c:if test="${not empty d}">
                                <c:set var="preservedParams" value="${preservedParams}&duration=${d}" />
                            </c:if>
                        </c:forTokens>

                        <c:set var="nextDirClass" value="${sortBy eq 'licenceClass' and sortDir eq 'asc' ? 'desc' : 'asc'}" />
                        <c:set var="nextDirAge" value="${sortBy eq 'minimumAge' and sortDir eq 'asc' ? 'desc' : 'asc'}" />
                        <c:set var="nextDirYears" value="${sortBy eq 'validForYears' and sortDir eq 'asc' ? 'desc' : 'asc'}" />

                        <a href="?sortBy=licenceClass&sortDir=${nextDirClass}&${preservedParams}"
                           class="sort-chip ${sortBy eq 'licenceClass' ? 'sort-chip--active' : ''} ${sortBy eq 'licenceClass' and sortDir eq 'desc' ? 'sort-chip--desc' : ''}">
                            Mã hạng
                        </a>
                        <a href="?sortBy=minimumAge&sortDir=${nextDirAge}&${preservedParams}"
                           class="sort-chip ${sortBy eq 'minimumAge' ? 'sort-chip--active' : ''} ${sortBy eq 'minimumAge' and sortDir eq 'desc' ? 'sort-chip--desc' : ''}">
                            Độ tuổi
                        </a>
                        <a href="?sortBy=validForYears&sortDir=${nextDirYears}&${preservedParams}"
                           class="sort-chip ${sortBy eq 'validForYears' ? 'sort-chip--active' : ''} ${sortBy eq 'validForYears' and sortDir eq 'desc' ? 'sort-chip--desc' : ''}">
                            Thời hạn
                        </a>
                    </div>
                </div>

                <c:set var="hasResults" value="false" scope="page" />
                <div class="results-grid">
                    <c:forEach var="licence" items="${requestScope.licences}">
                        <c:set var="lc" value="${licence.licenceClass}" />
                        <c:set var="hasResults" value="true" scope="page" />

                        <c:choose>
                            <c:when test="${lc eq 'A1' or lc eq 'A'}">
                                <c:set var="durationText" value="15 ngày" />
                            </c:when>
                            <c:when test="${lc eq 'B1'}">
                                <c:set var="durationText" value="3 tháng" />
                            </c:when>
                            <c:otherwise>
                                <c:set var="durationText" value="-" />
                            </c:otherwise>
                        </c:choose>

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
                                <div class="category-card__info-row">
                                    <span class="info-label">Thời gian đào tạo</span>
                                    <span class="info-value"><c:out value="${durationText}" /></span>
                                </div>
                                <div class="category-card__info-row">
                                    <span class="info-label">Độ tuổi tối thiểu</span>
                                    <span class="info-value">${licence.minimumAge} tuổi</span>
                                </div>
                                <div class="category-card__info-row">
                                    <span class="info-label">Thời hạn GPLX</span>
                                    <span class="info-value info-value--blue">
                                        <c:choose>
                                            <c:when test="${empty licence.validForYears or licence.validForYears le 0}">
                                                Vô thời hạn
                                            </c:when>
                                            <c:otherwise>
                                                ${licence.validForYears} năm
                                            </c:otherwise>
                                        </c:choose>
                                    </span>
                                </div>
                            </div>
                        </article>
                    </c:forEach>
                </div>

                <c:if test="${not hasResults}">
                    <div class="no-results-panel">
                        <h3 class="no-results-title">Không tìm thấy kết quả phù hợp</h3>
                        <p class="no-results-desc">Thử đổi bộ lọc hoặc đặt lại để xem các hạng A1, A, B1.</p>
                        <a href="${ctx}/license-categories" class="btn-reset-filters">Đặt lại bộ lọc</a>
                    </div>
                </c:if>
            </section>
        </div>
    </div>
</main>

<jsp:include page="/views/layout/footer.jsp" />

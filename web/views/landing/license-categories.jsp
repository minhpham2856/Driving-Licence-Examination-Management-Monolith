<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix = "c" uri = "http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix = "fn" uri = "http://java.sun.com/jsp/jstl/functions" %>

<jsp:include page="/views/layout/header.jsp">
    <jsp:param name="title" value="Lái Vui - Hạng GPLX" />
    <jsp:param name="activeNav" value="hang-bang" />
</jsp:include>

<link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/license-categories.css">

<c:set var="ctx" value="${pageContext.request.contextPath}" />
<c:set var="searchQuery" value="${requestScope.searchQuery}" />
<c:set var="sortBy" value="${requestScope.sortBy}" />
<c:set var="sortDir" value="${requestScope.sortDir}" />

<c:set var="isFirstLoad" value="${empty param.submit}" />
<c:set var="typeParams" value="${fn:join(paramValues.type, ',')}" />
<c:set var="durationParams" value="${fn:join(paramValues.duration, ',')}" />

<c:choose>
    <c:when test="${empty paramValues.type}">
        <c:set var="showXeMay" value="true" />
        <c:set var="showOToCon" value="true" />
        <c:set var="showXeTaiKhach" value="true" />
    </c:when>
    <c:otherwise>
        <c:set var="showXeMay" value="${fn:contains(typeParams, 'xe-may')}" />
        <c:set var="showOToCon" value="${fn:contains(typeParams, 'o-to-con')}" />
        <c:set var="showXeTaiKhach" value="${fn:contains(typeParams, 'xe-tai-khach')}" />
    </c:otherwise>
</c:choose>

<c:choose>
    <c:when test="${empty paramValues.duration}">
        <c:set var="showDuoi3Thang" value="true" />
        <c:set var="showTu3To6Thang" value="true" />
        <c:set var="showDurationOther" value="true" />
    </c:when>
    <c:otherwise>
        <c:set var="showDuoi3Thang" value="${fn:contains(durationParams, 'duoi-3-thang')}" />
        <c:set var="showTu3To6Thang" value="${fn:contains(durationParams, 'tu-3-6-thang')}" />
        <c:set var="showDurationOther" value="${fn:contains(durationParams, 'other')}" />
    </c:otherwise>
</c:choose>

<c:set var="hasResults" value="false" scope="page" />

<main class="public-main categories-page">
    <div class="categories-container">
        <header class="page-header">
            <h1 class="page-title">Danh mục hạng GPLX</h1>
            <p class="page-subtitle">Tìm kiếm và lựa chọn hạng bằng phù hợp với nhu cầu của bạn</p>
        </header>

        <div class="workspace-layout">
            <aside class="filter-sidebar-wrap">
                <form method="GET" action="${ctx}/license-categories" class="filter-card">
                    <input type="hidden" name="submit" value="true">

                    <div class="filter-card__header">
                        <div class="filter-card__header-title">
                            <svg class="filter-icon" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                            <path d="M4 6h16M7 12h10M10 18h4" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                            </svg>
                            <span>Bộ lọc</span>
                        </div>
                        <c:if test="${not isFirstLoad}">
                            <a href="${ctx}/license-categories" class="btn-reset-text">Xóa tất cả</a>
                        </c:if>
                    </div>

                    <div class="filter-search">
                        <input type="text" name="q" class="filter-search__input"
                               placeholder="Tìm hạng bằng..." value="${searchQuery}">
                    </div>

                    <div class="filter-group">
                        <h3 class="filter-group__title">LOẠI PHƯƠNG TIỆN</h3>
                        <div class="filter-group__options">
                            <label class="filter-option">
                                <input type="checkbox" name="type" value="xe-may"
                                       <c:if test="${fn:contains(typeParams, 'xe-may')}">checked</c:if>>
                                <span class="filter-label-text">Xe máy</span>
                            </label>
                            <label class="filter-option">
                                <input type="checkbox" name="type" value="o-to-con"
                                       <c:if test="${fn:contains(typeParams, 'o-to-con')}">checked</c:if>>
                                <span class="filter-label-text">Ô tô con</span>
                            </label>
                            <label class="filter-option">
                                <input type="checkbox" name="type" value="xe-tai-khach"
                                       <c:if test="${fn:contains(typeParams, 'xe-tai-khach')}">checked</c:if>>
                                <span class="filter-label-text">Xe tải / Khách</span>
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
                                <span class="filter-label-text">Từ 3-6 tháng</span>
                            </label>
                            <input type="checkbox" name="duration" value="other"
                                   <c:if test="${fn:contains(durationParams, 'other')}">checked</c:if> style="display:none;">
                        </div>
                    </div>

                    <button type="submit" class="btn-filter-submit">Áp dụng bộ lọc</button>
                </form>
            </aside>

            <section class="results-grid-wrap">
                <div class="results-toolbar">
                    <c:if test="${not empty searchQuery}">
                        <span class="results-toolbar__hint">
                            Kết quả tìm kiếm: <strong>${searchQuery}</strong>
                            <a href="${ctx}/license-categories" class="results-toolbar__clear">✕</a>
                        </span>
                    </c:if>

                    <div class="sort-bar">
                        <span class="sort-bar__label">Sắp xếp:</span>

                        <c:set var="preservedParams" value="submit=true" />
                        <c:if test="${not empty searchQuery}">
                            <c:set var="preservedParams" value="${preservedParams}&q=${searchQuery}" />
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

                <div class="results-grid">
                    <c:forEach var="licence" items="${requestScope.licences}">
                        <c:set var="lc" value="${licence.licenceClass}" />

                        <c:choose>
                            <c:when test="${lc eq 'A1' or lc eq 'A2' or lc eq 'A3' or lc eq 'A4'}">
                                <c:set var="vType" value="xe-may" />
                                <c:set var="durationCat" value="duoi-3-thang" />
                                <c:set var="iconSuffix" value="${lc eq 'A1' ? 'a1' : 'a2'}" />
                                <c:set var="badgeCls" value="${lc eq 'A1' ? 'category-card__badge--orange' : ''}" />
                                <c:set var="badgeText" value="${lc eq 'A1' ? 'Phổ biến' : ''}" />
                                <c:set var="durationText" value="15 ngày" />
                                <c:set var="tuition" value="${lc eq 'A1' ? '1.500.000' : '1.800.000'}" />
                                <c:set var="examFee" value="${lc eq 'A1' ? '550.000' : '600.000'}" />
                                <c:set var="conditionLabel" value="" />
                                <c:set var="conditionValue" value="" />
                            </c:when>
                            <c:when test="${lc eq 'B1'}">
                                <c:set var="vType" value="o-to-con" />
                                <c:set var="durationCat" value="tu-3-6-thang" />
                                <c:set var="iconSuffix" value="b1" />
                                <c:set var="badgeCls" value="" />
                                <c:set var="badgeText" value="" />
                                <c:set var="durationText" value="3 tháng" />
                                <c:set var="tuition" value="9.500.000" />
                                <c:set var="examFee" value="13.000.000" />
                                <c:set var="conditionLabel" value="" />
                                <c:set var="conditionValue" value="" />
                            </c:when>
                            <c:when test="${lc eq 'B2' or lc eq 'C1'}">
                                <c:set var="vType" value="o-to-con" />
                                <c:set var="durationCat" value="tu-3-6-thang" />
                                <c:set var="iconSuffix" value="b2" />
                                <c:set var="badgeCls" value="category-card__badge--green" />
                                <c:set var="badgeText" value="Chuyên nghiệp" />
                                <c:set var="durationText" value="3.5 tháng" />
                                <c:set var="tuition" value="11.500.000" />
                                <c:set var="examFee" value="14.500.000" />
                                <c:set var="conditionLabel" value="" />
                                <c:set var="conditionValue" value="" />
                            </c:when>
                            <c:when test="${lc eq 'C'}">
                                <c:set var="vType" value="xe-tai-khach" />
                                <c:set var="durationCat" value="tu-3-6-thang" />
                                <c:set var="iconSuffix" value="c" />
                                <c:set var="badgeCls" value="" />
                                <c:set var="badgeText" value="" />
                                <c:set var="durationText" value="5 tháng" />
                                <c:set var="tuition" value="12.500.000" />
                                <c:set var="examFee" value="16.000.000" />
                                <c:set var="conditionLabel" value="" />
                                <c:set var="conditionValue" value="" />
                            </c:when>
                            <c:when test="${lc eq 'D1'}">
                                <c:set var="vType" value="xe-tai-khach" />
                                <c:set var="durationCat" value="other" />
                                <c:set var="iconSuffix" value="d" />
                                <c:set var="badgeCls" value="" />
                                <c:set var="badgeText" value="" />
                                <c:set var="durationText" value="—" />
                                <c:set var="tuition" value="6.000.000" />
                                <c:set var="examFee" value="8.500.000" />
                                <c:set var="conditionLabel" value="Điều kiện:" />
                                <c:set var="conditionValue" value="Nâng hạng" />
                            </c:when>
                            <c:otherwise>
                                <c:set var="vType" value="xe-tai-khach" />
                                <c:set var="durationCat" value="other" />
                                <c:set var="iconSuffix" value="f" />
                                <c:set var="badgeCls" value="" />
                                <c:set var="badgeText" value="" />
                                <c:set var="durationText" value="—" />
                                <c:set var="tuition" value="9.000.000" />
                                <c:set var="examFee" value="12.000.000" />
                                <c:set var="conditionLabel" value="Yêu cầu:" />
                                <c:set var="conditionValue" value="Nâng hạng" />
                            </c:otherwise>
                        </c:choose>

                        <c:set var="showCard" value="false" />
                        <c:if test="${(vType eq 'xe-may' and showXeMay) or (vType eq 'o-to-con' and showOToCon) or (vType eq 'xe-tai-khach' and showXeTaiKhach)}">
                            <c:if test="${(durationCat eq 'duoi-3-thang' and showDuoi3Thang) or (durationCat eq 'tu-3-6-thang' and showTu3To6Thang) or (durationCat eq 'other' and showDurationOther)}">
                                <c:set var="showCard" value="true" />
                            </c:if>
                        </c:if>

                        <c:if test="${showCard}">
                            <c:set var="hasResults" value="true" scope="page" />
                            <div class="category-card">
                                <div class="category-card__header">
                                    <img src="${ctx}/assets/imgs/card_${iconSuffix}_icon.svg" alt="${lc}" class="category-card__icon"
                                         onerror="this.style.display='none'">
                                    <c:if test="${not empty badgeText}">
                                        <span class="category-card__badge ${badgeCls}">${badgeText}</span>
                                    </c:if>
                                </div>
                                <div class="category-card__body">
                                    <h2 class="category-card__title">Hạng ${lc}</h2>
                                    <p class="category-card__desc">${empty licence.description ? 'Phạm vi sát hạch quốc gia theo quy định của Bộ Giao thông Vận tải.' : licence.description}</p>
                                </div>
                                <div class="category-card__footer">
                                    <div class="category-card__info-row">
                                        <span class="info-label">Thời gian:</span>
                                        <span class="info-value">${durationText}</span>
                                    </div>
                                    <c:if test="${not empty conditionValue}">
                                        <div class="category-card__info-row">
                                            <span class="info-label">${conditionLabel}</span>
                                            <span class="info-value text-exp">${conditionValue}</span>
                                        </div>
                                    </c:if>
                                    <div class="category-card__info-row">
                                        <span class="info-label">Học phí:</span>
                                        <span class="info-value">${tuition}đ</span>
                                    </div>
                                    <div class="category-card__info-row">
                                        <span class="info-label">Lệ phí thi:</span>
                                        <span class="info-value info-value--blue">${examFee}đ</span>
                                    </div>
                                </div>
                            </div>
                        </c:if>
                    </c:forEach>
                </div>

                <c:if test="${not hasResults}">
                    <div class="no-results-panel">
                        <svg class="no-results-icon" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                        <circle cx="12" cy="12" r="10" stroke="currentColor" stroke-width="2"/>
                        <path d="M12 8v4M12 16h.01" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
                        </svg>
                        <h3 class="no-results-title">Không tìm thấy kết quả phù hợp</h3>
                        <p class="no-results-desc">Vui lòng thay đổi lựa chọn bộ lọc hoặc đặt lại bộ lọc để xem các hạng bằng khác.</p>
                        <a href="${ctx}/license-categories" class="btn-reset-filters">Đặt lại bộ lọc</a>
                    </div>
                </c:if>
            </section>
        </div>
    </div>
</main>

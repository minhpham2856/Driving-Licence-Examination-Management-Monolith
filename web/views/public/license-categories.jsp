<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix = "c" uri = "http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix = "fn" uri = "http://java.sun.com/jsp/jstl/functions" %>

<jsp:include page="/views/layout/header.jsp">
    <jsp:param name="title" value="Lái Vui - Hạng GPLX" />
    <jsp:param name="activeNav" value="hang-bang" />
</jsp:include>

<!-- Link custom stylesheet for this page -->
<link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/license-categories.css">

<!-- Server-side filtering using JSTL -->
<c:set var="isFirstLoad" value="${empty param.submit}" />
<c:set var="typeParams" value="${fn:join(paramValues.type, ',')}" />
<c:set var="durationParams" value="${fn:join(paramValues.duration, ',')}" />

<!-- Resolve active types -->
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

<!-- Resolve active durations -->
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
        <!-- Page Header -->
        <header class="page-header">
            <h1 class="page-title">Danh mục hạng GPLX</h1>
            <p class="page-subtitle">Tìm kiếm và lựa chọn hạng bằng phù hợp với nhu cầu của bạn</p>
        </header>

        <!-- Main Workspace: Sidebar + Grid -->
        <div class="workspace-layout">
            <!-- Sidebar: Filter Column -->
            <aside class="filter-sidebar-wrap">
                <form method="GET" action="${pageContext.request.contextPath}/license-categories" class="filter-card">
                    <input type="hidden" name="submit" value="true">

                    <div class="filter-card__header">
                        <div class="filter-card__header-title">
                            <svg class="filter-icon" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                            <path d="M4 6h16M7 12h10M10 18h4" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                            </svg>
                            <span>Bộ lọc</span>
                        </div>
                        <c:if test="${not isFirstLoad}">
                            <a href="${pageContext.request.contextPath}/license-categories" class="btn-reset-text">Xóa tất cả</a>
                        </c:if>
                    </div>

                    <!-- Group 1: Loại phương tiện -->
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
                                  <!-- Group 2: Thời gian đào tạo -->
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

                                <!-- Hidden checkbox to allow "other" duration categories when no duration filter is set -->
                                <input type="checkbox" name="duration" value="other"
                                       <c:if test="${fn:contains(durationParams, 'other')}">checked</c:if> style="display:none;">
                            </div>
                        </div>

                        <button type="submit" class="btn-filter-submit">Áp dụng bộ lọc</button>
                    </form>
                </aside>

                <!-- Results Grid Column -->
                <section class="results-grid-wrap">
                    <div class="results-grid">

                        <!-- Card 1: Hạng A1 -->
                    <c:if test="${showXeMay and showDuoi3Thang}">
                        <c:set var="hasResults" value="true" scope="page" />
                        <div class="category-card">
                            <div class="category-card__header">
                                <img src="${pageContext.request.contextPath}/assets/imgs/card_a1_icon.svg" alt="A1" class="category-card__icon">
                                <span class="category-card__badge category-card__badge--orange">Phổ biến</span>
                            </div>
                            <div class="category-card__body">
                                <h2 class="category-card__title">Hạng A1</h2>
                                <p class="category-card__desc">Xe mô tô 2 bánh có dung tích xi lanh từ 50cm3 đến dưới 175cm3.</p>
                            </div>
                            <div class="category-card__footer">
                                <div class="category-card__info-row">
                                    <span class="info-label">Thời gian:</span>
                                    <span class="info-value">15 ngày</span>
                                </div>
                                <div class="category-card__info-row">
                                    <span class="info-label">Học phí:</span>
                                    <span class="info-value">1.500.000đ</span>
                                </div>
                                <div class="category-card__info-row">
                                    <span class="info-label">Lệ phí thi:</span>
                                    <span class="info-value info-value--blue">550.000đ</span>
                                </div>
                            </div>
                        </div>
                    </c:if>

                    <!-- Card 2: Hạng B1 -->
                    <c:if test="${showOToCon and showTu3To6Thang}">
                        <c:set var="hasResults" value="true" scope="page" />
                        <div class="category-card">
                            <div class="category-card__header">
                                <img src="${pageContext.request.contextPath}/assets/imgs/card_b1_icon.svg" alt="B1" class="category-card__icon">
                            </div>
                            <div class="category-card__body">
                                <h2 class="category-card__title">Hạng B1</h2>
                                <p class="category-card__desc">Xe số tự động chở người đến 9 chỗ. Không được hành nghề lái xe.</p>
                            </div>
                            <div class="category-card__footer">
                                <div class="category-card__info-row">
                                    <span class="info-label">Thời gian:</span>
                                    <span class="info-value">3 tháng</span>
                                </div>
                                <div class="category-card__info-row">
                                    <span class="info-label">Học phí:</span>
                                    <span class="info-value">9.500.000đ</span>
                                </div>
                                <div class="category-card__info-row">
                                    <span class="info-label">Lệ phí thi:</span>
                                    <span class="info-value info-value--blue">13.000.000đ</span>
                                </div>
                            </div>
                        </div>
                    </c:if>

                    <!-- Card 3: Hạng C1 -->
                    <c:if test="${showOToCon and showTu3To6Thang}">
                        <c:set var="hasResults" value="true" scope="page" />
                        <div class="category-card">
                            <div class="category-card__header">
                                <img src="${pageContext.request.contextPath}/assets/imgs/card_b2_icon.svg" alt="C1" class="category-card__icon">
                                <span class="category-card__badge category-card__badge--green">Chuyên nghiệp</span>
                            </div>
                            <div class="category-card__body">
                                <h2 class="category-card__title">Hạng C1</h2>
                                <p class="category-card__desc">Ô tô chở người đến 9 chỗ, xe tải dưới 3.5 tấn. Được phép hành nghề lái xe.</p>
                            </div>
                            <div class="category-card__footer">
                                <div class="category-card__info-row">
                                    <span class="info-label">Thời gian:</span>
                                    <span class="info-value">3.5 tháng</span>
                                </div>
                                <div class="category-card__info-row">
                                    <span class="info-label">Học phí:</span>
                                    <span class="info-value">11.500.000đ</span>
                                </div>
                                <div class="category-card__info-row">
                                    <span class="info-label">Lệ phí thi:</span>
                                    <span class="info-value info-value--blue">14.500.000đ</span>
                                </div>
                            </div>
                        </div>
                    </c:if>

                    <!-- Card 4: Hạng C -->
                    <c:if test="${showXeTaiKhach and showTu3To6Thang}">
                        <c:set var="hasResults" value="true" scope="page" />
                        <div class="category-card">
                            <div class="category-card__header">
                                <img src="${pageContext.request.contextPath}/assets/imgs/card_c_icon.svg" alt="C" class="category-card__icon">
                            </div>
                            <div class="category-card__body">
                                <h2 class="category-card__title">Hạng C</h2>
                                <p class="category-card__desc">Xe tải trên 3.5 tấn, các loại xe hạng B1, B2. Được phép kinh doanh vận tải.</p>
                            </div>
                            <div class="category-card__footer">
                                <div class="category-card__info-row">
                                    <span class="info-label">Thời gian:</span>
                                    <span class="info-value">5 tháng</span>
                                </div>
                                <div class="category-card__info-row">
                                    <span class="info-label">Học phí:</span>
                                    <span class="info-value">12.500.000đ</span>
                                </div>
                                <div class="category-card__info-row">
                                    <span class="info-label">Lệ phí thi:</span>
                                    <span class="info-value info-value--blue">16.000.000đ</span>
                                </div>
                            </div>
                        </div>
                    </c:if>

                    <!-- Card 5: Hạng D1 -->
                    <c:if test="${showXeTaiKhach and showDurationOther}">
                        <c:set var="hasResults" value="true" scope="page" />
                        <div class="category-card">
                            <div class="category-card__header">
                                <img src="${pageContext.request.contextPath}/assets/imgs/card_d_icon.svg" alt="D1" class="category-card__icon">
                            </div>
                            <div class="category-card__body">
                                <h2 class="category-card__title">Hạng D1</h2>
                                <p class="category-card__desc">Ô tô chở người từ 10 đến 30 chỗ ngồi và các loại xe hạng B1, B2, C.</p>
                            </div>
                            <div class="category-card__footer">
                                <div class="category-card__info-row">
                                    <span class="info-label">Điều kiện:</span>
                                    <span class="info-value text-exp">Nâng hạng</span>
                                </div>
                                <div class="category-card__info-row">
                                    <span class="info-label">Học phí:</span>
                                    <span class="info-value">6.000.000đ</span>
                                </div>
                                <div class="category-card__info-row">
                                    <span class="info-label">Lệ phí thi:</span>
                                    <span class="info-value info-value--blue">8.500.000đ</span>
                                </div>
                            </div>
                        </div>
                    </c:if>

                    <!-- Card 6: Hạng D2 -->
                    <c:if test="${showXeTaiKhach and showDurationOther}">
                        <c:set var="hasResults" value="true" scope="page" />
                        <div class="category-card">
                            <div class="category-card__header">
                                <img src="${pageContext.request.contextPath}/assets/imgs/card_f_icon.svg" alt="D2" class="category-card__icon">
                            </div>
                            <div class="category-card__body">
                                <h2 class="category-card__title">Hạng D2</h2>
                                <p class="category-card__desc">Người đã có giấy phép lái xe hạng B, C, D để điều khiển các loại xe tương ứng kéo rơ moóc.</p>
                            </div>
                            <div class="category-card__footer">
                                <div class="category-card__info-row">
                                    <span class="info-label">Yêu cầu:</span>
                                    <span class="info-value text-exp">Nâng hạng</span>
                                </div>
                                <div class="category-card__info-row">
                                    <span class="info-label">Học phí:</span>
                                    <span class="info-value">9.000.000đ</span>
                                </div>
                                <div class="category-card__info-row">
                                    <span class="info-label">Lệ phí thi:</span>
                                    <span class="info-value info-value--blue">12.000.000đ</span>
                                </div>
                            </div>
                        </div>
                    </c:if>

                </div>

                <!-- Fallback: No Results -->
                <c:if test="${not hasResults}">
                    <div class="no-results-panel">
                        <svg class="no-results-icon" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                        <circle cx="12" cy="12" r="10" stroke="currentColor" stroke-width="2"/>
                        <path d="M12 8v4M12 16h.01" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
                        </svg>
                        <h3 class="no-results-title">Không tìm thấy kết quả phù hợp</h3>
                        <p class="no-results-desc">Vui lòng thay đổi lựa chọn bộ lọc hoặc đặt lại bộ lọc để xem các hạng bằng khác.</p>
                        <a href="${pageContext.request.contextPath}/license-categories" class="btn-reset-filters">Đặt lại bộ lọc</a>
                    </div>
                </c:if>
            </section>
        </div>
    </div>
</main>

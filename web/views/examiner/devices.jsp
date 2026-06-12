<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<c:set var="ctx" value="${pageContext.request.contextPath}" />
<c:set var="headerTitle" value="Quản lý thiết bị" />
<c:set var="pageUrl" value="${ctx}/views/examiner/devices" />

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
            <jsp:param name="pageCss" value="devices.css" />
        </jsp:include>
    </head>
    <body class="has-side-nav-bar examiner-portal${empty examinerHasActiveSession or not examinerHasActiveSession ? ' examiner-portal--inactive' : ''}">

        <jsp:include page="/views/layout/sidebar-examiner.jsp">
            <jsp:param name="activeSidebar" value="thiet-bi" />
        </jsp:include>

        <div class="examiner-shell">
            <jsp:include page="/views/layout/header-examiner.jsp" />

            <main class="examiner-main examiner-main--scroll">
                <jsp:include page="/views/examiner/partials/examiner-messages.jsp" />

                <section class="examiner-toolbar examiner-toolbar--tools">
                    <form action="${pageUrl}" method="get" class="examiner-toolbar__group examiner-toolbar__search-form">
                        <div class="examiner-search examiner-search--wide">
                            <input type="text" name="q" class="examiner-search__input"
                                   placeholder="Tìm theo tên, loại, trạng thái..."
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

                <section class="examiner-card examiner-card--dashboard-table">
                    <div class="examiner-card__head">
                        <h3 class="examiner-card__title">Máy thi</h3>
                        <span class="examiner-card__badge">Tổng: ${fn:length(devices)} máy</span>
                    </div>

                    <c:choose>
                        <c:when test="${empty devices}">
                            <p class="examiner-table__empty">Không có máy trong khu vực thi.</p>
                        </c:when>
                        <c:otherwise>
                            <div class="device-grid">
                                <c:forEach var="device" items="${devices}" varStatus="st">
                                    <c:set var="statusClass" value="device-grid-card--unknown" />
                                    <c:set var="statusLabel" value="${device.status}" />
                                    <c:choose>
                                        <c:when test="${device.status eq 'Available' or device.status eq 'Operational'}">
                                            <c:set var="statusClass" value="device-grid-card--available" />
                                            <c:set var="statusLabel" value="Sẵn sàng" />
                                        </c:when>
                                        <c:when test="${device.status eq 'InUse'}">
                                            <c:set var="statusClass" value="device-grid-card--inuse" />
                                            <c:set var="statusLabel" value="Đang dùng" />
                                        </c:when>
                                        <c:when test="${device.status eq 'Maintenance'}">
                                            <c:set var="statusClass" value="device-grid-card--maintenance" />
                                            <c:set var="statusLabel" value="Bảo trì" />
                                        </c:when>
                                    </c:choose>
                                    <article class="device-grid-card ${statusClass}">
                                        <span class="device-grid-card__icon material-symbols-outlined">computer</span>
                                        <h4 class="device-grid-card__name">${device.name}</h4>
                                        <p class="device-grid-card__area">${device.area}</p>
                                        <span class="device-grid-card__status">${statusLabel}</span>
                                        <div class="device-grid-card__actions">
                                            <c:choose>
                                                <c:when test="${device.status eq 'Maintenance'}">
                                                    <a href="${pageUrl}?action=operational&amp;deviceId=${device.id}"
                                                       class="examiner-link-action"
                                                       onclick="return confirm('Chuyển thiết bị ${device.name} sang sử dụng?');">Sử dụng</a>
                                                </c:when>
                                                <c:otherwise>
                                                    <a href="${pageUrl}?action=maintenance&amp;deviceId=${device.id}"
                                                       class="examiner-link-action"
                                                       onclick="return confirm('Chuyển thiết bị ${device.name} sang bảo trì?');">Bảo trì</a>
                                                </c:otherwise>
                                            </c:choose>
                                        </div>
                                    </article>
                                </c:forEach>
                            </div>
                        </c:otherwise>
                    </c:choose>
                </section>
            </main>
        </div>
    </body>
</html>

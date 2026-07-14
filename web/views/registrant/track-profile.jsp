<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>

<!DOCTYPE html>
<html lang="vi">
    <head>
        <meta charset="UTF-8">
        <meta name="viewport" content="width=device-width, initial-scale=1.0">
        <title>Theo Dõi Tiến Trình Hồ Sơ - Lái Vui</title>
        <meta name="description" content="Xem trạng thái xử lý hồ sơ đăng ký thi sát hạch GPLX của bạn theo thời gian thực tại Lái Vui.">

        <!-- Google Fonts -->
        <link rel="preconnect" href="https://fonts.googleapis.com">
        <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
        <link href="https://fonts.googleapis.com/css2?family=Be+Vietnam+Pro:wght@400;500;600;700;800&family=Inter:wght@400;500;600;700;800&display=swap" rel="stylesheet">

        <!-- Layout Stylesheets -->
        <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/style.css?v=<%= System.currentTimeMillis() %>">
        <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/layout.css?v=<%= System.currentTimeMillis() %>">
    </head>
    <body class="has-side-nav-bar">

        <%-- Inject registrant sidebar --%>
        <jsp:include page="/views/layout/sidebar-registrant.jsp">
            <jsp:param name="activeSidebar" value="track-profile" />
        </jsp:include>

        <div class="dashboard-shell">
            <main class="main-content" id="main-content">

                <%-- Breadcrumbs --%>
                <nav class="breadcrumbs" aria-label="Breadcrumb">
                    <a href="${pageContext.request.contextPath}/registrant/dashboard">Trang chủ</a>
                    <span class="breadcrumbs__separator" aria-hidden="true">/</span>
                    <span class="breadcrumbs__current" aria-current="page">Theo dõi tiến trình hồ sơ</span>
                </nav>

                <%-- Page Header --%>
                <header class="page-header">
                    <div class="page-title-wrap">
                        <h1 class="page-title">Theo dõi tiến trình hồ sơ</h1>
                        <p class="page-subtitle">Xem trạng thái kiểm duyệt hồ sơ đăng ký dự thi sát hạch lái xe của bạn</p>
                    </div>
                </header>

                <c:if test="${showSupplementAlert}">
                    <%-- Alert Banner (If in supplementary upload phase) --%>
                    <section class="p-alert-banner" aria-label="Khuyến cáo tiến trình">
                        <svg width="20" height="20" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                        <circle cx="12" cy="12" r="10" stroke="currentColor" stroke-width="2"></circle>
                        <path d="M12 8v5M12 16h.01" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"></path>
                        </svg>
                        <div class="p-alert-banner__content">
                            <span class="p-alert-banner__title">Yêu cầu bổ sung tài liệu đính kèm</span>
                            <span>
                                Hồ sơ của bạn cần bổ sung tài liệu trước khi tiếp tục xét duyệt.
                                <a href="${pageContext.request.contextPath}/registrant/upload-documents" class="profile-checklist-link">Upload ngay →</a>
                            </span>
                        </div>
                    </section>
                </c:if>

                <%-- Timeline tiến trình hồ sơ --%>
                <div class="tracking-card">
                    <jsp:include page="/views/registrant/partials/profile-progress-timeline.jsp"/>

                    <c:if test="${not empty documentSummary}">
                        <div class="tracking-summary tracking-summary--compact">
                            <div class="tracking-summary__head">
                                <h2 class="tracking-summary__title">Tài liệu đính kèm</h2>
                                <span class="status-badge status-badge--${documentSummary.overallStatusClass eq 'complete' ? 'approved' : (documentSummary.overallStatusClass eq 'danger' ? 'rejected' : (documentSummary.overallStatusClass eq 'pending' ? 'pending' : 'info'))}">
                                    ${documentSummary.overallStatusLabel}
                                </span>
                            </div>
                            <div class="profile-completion-card__progress-wrap tracking-summary__progress">
                                <div class="profile-completion-card__progress-text">
                                    <span>Giấy tờ bắt buộc</span>
                                    <span class="profile-completion-card__percentage">${documentSummary.requiredUploaded}/${documentSummary.requiredTotal}</span>
                                </div>
                                <div class="profile-completion-card__bar-bg">
                                    <div class="profile-completion-card__bar-fill" style="width: ${documentSummary.requiredProgressPercent}%;"></div>
                                </div>
                            </div>
                            <a href="${pageContext.request.contextPath}/registrant/upload-documents" class="profile-checklist-action">
                                Quản lý tài liệu đính kèm
                                <svg width="14" height="14" viewBox="0 0 24 24" fill="none" aria-hidden="true">
                                <path d="M5 12h14M13 6l6 6-6 6" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                                </svg>
                            </a>
                        </div>
                    </c:if>

                    <c:if test="${empty profileProgressSteps and empty documentSummary}">
                        <p style="color:#64748b;margin:0;">Chưa có dữ liệu tiến trình hồ sơ.</p>
                    </c:if>
                </div>

                <%-- Detailed Processing History Table Log --%>
                <div class="r-panel" id="panel-tracking-history">
                    <div class="r-panel__header">
                        <h2 class="r-form-title">
                            <svg width="18" height="18" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                            <circle cx="12" cy="12" r="9" stroke="currentColor" stroke-width="2"></circle>
                            <path d="M12 7v5l3 3" stroke="currentColor" stroke-width="2" stroke-linecap="round"></path>
                            </svg>
                            Lịch sử xử lý chi tiết
                        </h2>
                    </div>

                    <jsp:include page="/views/registrant/partials/audit-filter-form.jsp"/>

                    <div class="r-panel__body--noPad">
                        <div class="table-responsive">
                            <table class="audit-table" role="table" aria-label="Nhật ký xử lý hồ sơ">
                                <thead>
                                    <tr>
                                        <th scope="col">Thời gian</th>
                                        <th scope="col">Nội dung tác vụ</th>
                                        <th scope="col">Người xử lý</th>
                                        <th scope="col">Trạng thái</th>
                                        <th scope="col">Ghi chú / Chi tiết</th>
                                    </tr>
                                </thead>
                                <tbody>
                                    <c:if test="${not empty profileTrackingLogs}">
                                        <c:forEach var="log" items="${profileTrackingLogs}">
                                            <tr>
                                                <td>
                                                    <fmt:formatDate value="${log.timestamp}" pattern="dd/MM/yyyy HH:mm"/>
                                                </td>
                                                <td><strong>${log.eventTitle}</strong></td>
                                                <td>${log.actorRole}</td>
                                                <td>
                                                    <span class="status-badge status-badge--${log.statusClass}">${log.statusLabel}</span>
                                                </td>
                                                <td>${log.remarks}</td>
                                            </tr>
                                        </c:forEach>
                                    </c:if>

                                    <c:if test="${empty profileTrackingLogs}">
                                        <tr>
                                            <td colspan="5" style="text-align:center;padding:2rem;color:#64748b;">
                                                <c:choose>
                                                    <c:when test="${searchActive}">Không có nhật ký phù hợp với bộ lọc.</c:when>
                                                    <c:otherwise>Chưa có lịch sử xử lý hồ sơ.</c:otherwise>
                                                </c:choose>
                                            </td>
                                        </tr>
                                    </c:if>
                                </tbody>
                            </table>
                        </div>
                    </div>
                </div>


            </main>

        </div>

    </body>
</html>

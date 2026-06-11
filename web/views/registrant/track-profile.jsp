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
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/style.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/layout.css">
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
            <section class="p-alert-banner" aria-label="Khuyến cáo tiến trình">
                <div class="p-alert-banner__content">
                    <span class="p-alert-banner__title">Yêu cầu bổ sung hồ sơ</span>
                    <span>
                        ${not empty supplementAlertMessage ? supplementAlertMessage : 'Hồ sơ cần được bổ sung hoặc chỉnh sửa trước khi tiếp tục.'}
                        <a href="${pageContext.request.contextPath}/registrant/upload-documents" class="profile-checklist-link">Upload ngay →</a>
                    </span>
                </div>
            </section>
        </c:if>

        <%-- Visual Progress Timeline Card --%>
        <div class="tracking-card">
            <div class="tracking-timeline">
                
                <%-- Background and fill line --%>
                <div class="tracking-timeline__line-bg"></div>
                <div class="tracking-timeline__line-fill tracking-timeline__line-fill--step${timelineFillStep}"></div>

                <c:forEach var="step" items="${trackingSteps}">
                    <div class="tracking-node tracking-node--${step.state}">
                        <div class="tracking-node__circle">${step.stepNumber}</div>
                        <span class="tracking-node__title">${step.title}</span>
                        <span class="tracking-node__desc">${step.description}</span>
                    </div>
                </c:forEach>

            </div>
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
                                    <td colspan="5">Chưa có nhật ký xử lý hồ sơ.</td>
                                </tr>
                            </c:if>
                        </tbody>
                    </table>
                </div>
            </div>
        </div>


    </main>

    <%-- Footer --%>
    <jsp:include page="/views/layout/footer.jsp" />
</div>

</body>
</html>

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
            <a href="${pageContext.request.contextPath}/views/registrant/dashboard.jsp">Trang chủ</a>
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

        <%-- Alert Banner (If in supplementary upload phase) --%>
        <section class="p-alert-banner" aria-label="Khuyến cáo tiến trình">
            <svg width="20" height="20" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                <circle cx="12" cy="12" r="10" stroke="currentColor" stroke-width="2"></circle>
                <path d="M12 8v5M12 16h.01" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"></path>
            </svg>
            <div class="p-alert-banner__content">
                <span class="p-alert-banner__title">Yêu cầu bổ sung tài liệu đính kèm</span>
                <span>
                    Hồ sơ gốc của bạn đang dừng ở bước <strong>Duyệt hồ sơ gốc</strong>. Vui lòng tải lại ảnh chụp Giấy khám sức khỏe hợp lệ mới để tiếp tục quy trình xét duyệt sang bước tiếp theo.
                    <a href="${pageContext.request.contextPath}/views/registrant/upload-documents.jsp" class="profile-checklist-link">Upload ngay →</a>
                </span>
            </div>
        </section>

        <%-- Visual Progress Timeline Card --%>
        <div class="tracking-card">
            <div class="tracking-timeline">
                
                <%-- Background and fill line --%>
                <div class="tracking-timeline__line-bg"></div>
                <%-- Using step class for 42% width representing Step 3 active state --%>
                <div class="tracking-timeline__line-fill tracking-timeline__line-fill--step3"></div>

                <%-- Step 1: Đăng ký thành công --%>
                <div class="tracking-node tracking-node--completed">
                    <div class="tracking-node__circle">
                        <svg width="20" height="20" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                            <path d="M20 6L9 17l-5-5" stroke="currentColor" stroke-width="3" stroke-linecap="round" stroke-linejoin="round"></path>
                        </svg>
                    </div>
                    <span class="tracking-node__title">Đăng ký thành công</span>
                    <span class="tracking-node__desc">09:45 - 20/05/2025</span>
                </div>

                <%-- Step 2: Xác minh tài liệu --%>
                <div class="tracking-node tracking-node--completed">
                    <div class="tracking-node__circle">
                        <svg width="20" height="20" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                            <path d="M20 6L9 17l-5-5" stroke="currentColor" stroke-width="3" stroke-linecap="round" stroke-linejoin="round"></path>
                        </svg>
                    </div>
                    <span class="tracking-node__title">Xác minh định danh</span>
                    <span class="tracking-node__desc">14:30 - 21/05/2025</span>
                </div>

                <%-- Step 3: Duyệt hồ sơ gốc --%>
                <div class="tracking-node tracking-node--active">
                    <div class="tracking-node__circle">3</div>
                    <span class="tracking-node__title">Duyệt hồ sơ gốc</span>
                    <span class="tracking-node__desc">Yêu cầu bổ sung</span>
                </div>

                <%-- Step 4: Sắp xếp đợt thi --%>
                <div class="tracking-node">
                    <div class="tracking-node__circle">4</div>
                    <span class="tracking-node__title">Lập lịch dự thi</span>
                    <span class="tracking-node__desc">Chờ xếp phòng</span>
                </div>

                <%-- Step 5: Hoàn tất sát hạch --%>
                <div class="tracking-node">
                    <div class="tracking-node__circle">5</div>
                    <span class="tracking-node__title">Cấp chứng chỉ</span>
                    <span class="tracking-node__desc">Chờ kết quả</span>
                </div>

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
                                <%-- Fallback high-fidelity historical logs --%>
                                <tr>
                                    <td>22/05/2025 08:30</td>
                                    <td><strong>Kiểm duyệt Giấy khám sức khỏe</strong></td>
                                    <td>Cán bộ coi thi</td>
                                    <td><span class="status-badge status-badge--rejected">Từ chối</span></td>
                                    <td>Ảnh chụp bị mờ phần dấu giáp lai ảnh. Thí sinh cần chụp lại bản gốc sắc nét hơn.</td>
                                </tr>
                                <tr>
                                    <td>21/05/2025 14:30</td>
                                    <td><strong>Xác minh tài liệu định danh</strong></td>
                                    <td>Hệ thống tự động</td>
                                    <td><span class="status-badge status-badge--approved">Thành công</span></td>
                                    <td>CCCD mặt trước/mặt sau hợp lệ. Xác thực khuôn mặt thành công.</td>
                                </tr>
                                <tr>
                                    <td>20/05/2025 10:15</td>
                                    <td><strong>Thanh toán lệ phí đăng ký</strong></td>
                                    <td>Cổng thanh toán MoMo</td>
                                    <td><span class="status-badge status-badge--approved">Thành công</span></td>
                                    <td>Lệ phí sát hạch GPLX Hạng B2: 1.200.000 VNĐ đã được nhận.</td>
                                </tr>
                                <tr>
                                    <td>20/05/2025 09:45</td>
                                    <td><strong>Tải lên hồ sơ gốc</strong></td>
                                    <td>Thí sinh</td>
                                    <td><span class="status-badge status-badge--approved">Thành công</span></td>
                                    <td>Thí sinh gửi hồ sơ đăng ký dự thi trực tuyến.</td>
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

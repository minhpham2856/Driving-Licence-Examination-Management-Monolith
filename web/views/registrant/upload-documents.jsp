<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>

<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Upload Hồ Sơ Bổ Sung - Lái Vui</title>
    <meta name="description" content="Tải lên và cập nhật hồ sơ, ảnh chân dung, CCCD và giấy khám sức khỏe để đăng ký thi sát hạch lái xe tại Lái Vui.">

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
    <jsp:param name="activeSidebar" value="upload-documents" />
</jsp:include>

<div class="dashboard-shell">
    <main class="main-content" id="main-content">

        <%-- Breadcrumbs --%>
        <nav class="breadcrumbs" aria-label="Breadcrumb">
            <a href="${pageContext.request.contextPath}/views/registrant/dashboard.jsp">Trang chủ</a>
            <span class="breadcrumbs__separator" aria-hidden="true">/</span>
            <a href="${pageContext.request.contextPath}/views/registrant/profile.jsp">Hồ sơ cá nhân</a>
            <span class="breadcrumbs__separator" aria-hidden="true">/</span>
            <span class="breadcrumbs__current" aria-current="page">Upload hồ sơ bổ sung</span>
        </nav>

        <%-- Page Header --%>
        <header class="page-header">
            <div class="page-title-wrap">
                <h1 class="page-title">Upload hồ sơ bổ sung</h1>
                <p class="page-subtitle">Tải lên tài liệu đính kèm để hoàn tất thủ tục xét duyệt hồ sơ sát hạch</p>
            </div>
        </header>

        <%-- Notification Banner (File requirements) --%>
        <section class="p-alert-banner" aria-label="Hướng dẫn upload">
            <svg width="20" height="20" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                <circle cx="12" cy="12" r="10" stroke="currentColor" stroke-width="2"></circle>
                <path d="M12 16v-4M12 8h.01" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"></path>
            </svg>
            <div class="p-alert-banner__content">
                <span class="p-alert-banner__title">Yêu cầu định dạng tệp tin</span>
                <span>
                    Chấp nhận các định dạng ảnh: <strong>PNG, JPG, JPEG</strong>. Dung lượng tối đa: <strong>5MB mỗi tệp</strong>. Các bản quét hoặc ảnh chụp tài liệu cần rõ chữ, không mờ, không bị lóa và hiển thị đầy đủ thông tin hoặc con dấu (đối với Giấy khám sức khỏe).
                </span>
            </div>
        </section>

        <%-- Upload Cards Grid --%>
        <div class="upload-grid">
            
            <%-- 1. Ảnh chân dung 3x4 --%>
            <div class="upload-card upload-card--approved">
                <div class="upload-card__header">
                    <h2 class="upload-card__title">1. Ảnh chân dung 3x4</h2>
                    <span class="r-stat-card__badge r-stat-card__badge--success">Đã duyệt</span>
                </div>
                <div class="upload-card__preview-box">
                    <img src="${pageContext.request.contextPath}/assets/imgs/LOGO.png" alt="Ảnh chân dung đã duyệt" class="upload-card__preview-img" onerror="this.src='https://images.unsplash.com/photo-1534528741775-53994a69daeb?q=80&w=250&auto=format&fit=crop'">
                </div>
            </div>

            <%-- 2. Mặt trước CCCD --%>
            <div class="upload-card upload-card--approved">
                <div class="upload-card__header">
                    <h2 class="upload-card__title">2. Mặt trước CCCD / CMND</h2>
                    <span class="r-stat-card__badge r-stat-card__badge--success">Đã duyệt</span>
                </div>
                <div class="upload-card__preview-box">
                    <svg width="60" height="60" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg" class="upload-card__approved-icon">
                        <rect x="2" y="5" width="20" height="14" rx="2" stroke="currentColor" stroke-width="1.5"></rect>
                        <circle cx="7" cy="12" r="2.5" stroke="currentColor" stroke-width="1.5"></circle>
                        <path d="M12 9h7M12 12h5M12 15h6" stroke="currentColor" stroke-width="1.5" stroke-linecap="round"></path>
                    </svg>
                    <span class="upload-card__approved-label">[Mặt trước CCCD đã tải lên]</span>
                </div>
            </div>

            <%-- 3. Mặt sau CCCD --%>
            <div class="upload-card upload-card--approved">
                <div class="upload-card__header">
                    <h2 class="upload-card__title">3. Mặt sau CCCD / CMND</h2>
                    <span class="r-stat-card__badge r-stat-card__badge--success">Đã duyệt</span>
                </div>
                <div class="upload-card__preview-box">
                    <svg width="60" height="60" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg" class="upload-card__approved-icon">
                        <rect x="2" y="5" width="20" height="14" rx="2" stroke="currentColor" stroke-width="1.5"></rect>
                        <path d="M6 9h12M6 12h12M6 15h12" stroke="currentColor" stroke-width="1.5" stroke-linecap="round"></path>
                    </svg>
                    <span class="upload-card__approved-label">[Mặt sau CCCD đã tải lên]</span>
                </div>
            </div>

            <%-- 4. Giấy khám sức khỏe --%>
            <div class="upload-card upload-card--rejected">
                <div class="upload-card__header">
                    <h2 class="upload-card__title">4. Giấy khám sức khỏe lái xe</h2>
                    <span class="r-stat-card__badge r-stat-card__badge--danger">Yêu cầu bổ sung</span>
                </div>
                
                <%-- Drag & Drop Upload Zone --%>
                <div class="upload-card__preview-box">
                    <div class="upload-card__dropzone" id="medical-certificate-dropzone">
                        <svg width="40" height="40" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                            <path d="M12 16V8m0 8-3-3m3 3 3-3" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"></path>
                            <path d="M20 16.58A5 5 0 0 0 18 7h-1.26A8 8 0 1 0 4 15.25" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"></path>
                        </svg>
                        <span class="upload-card__dropzone-text">Kéo thả hoặc nhấp để chọn tệp tải lên</span>
                        <span class="upload-card__dropzone-sub">Kích thước ảnh chụp Giấy khám sức khỏe < 5MB</span>
                    </div>
                </div>

                <%-- Rejection Feedback Message --%>
                <div class="upload-card__feedback upload-card__feedback--error">
                    <svg width="14" height="14" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                        <circle cx="12" cy="12" r="10" stroke="currentColor" stroke-width="2"></circle>
                        <path d="M12 8v5M12 16h.01" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"></path>
                    </svg>
                    <span><strong>Lý do từ chối:</strong> Ảnh chụp Giấy khám sức khỏe bị mờ phần giáp lai ảnh. Vui lòng chụp lại rõ nét hơn, đặc biệt là vị trí đóng dấu giáp lai ảnh thẻ chân dung trên giấy khám.</span>
                </div>
            </div>

        </div>

        <%-- Submit Button Action Bar --%>
        <div class="upload-action-bar">
            <div class="upload-action-bar__info">
                <span class="upload-action-bar__title">Hoàn tất tải lên?</span>
                <span class="upload-action-bar__subtitle">Hồ sơ của bạn sẽ được chuyển đến Cán bộ coi thi kiểm tra và duyệt lại ngay sau khi bạn bấm gửi.</span>
            </div>
            <div class="upload-action-bar__buttons">
                <a href="${pageContext.request.contextPath}/views/registrant/profile.jsp" class="welcome-banner__btn welcome-banner__btn--outline upload-action-bar__btn-outline">
                    Quay lại hồ sơ
                </a>
                <button type="button" class="welcome-banner__btn welcome-banner__btn--primary upload-action-bar__btn-primary" id="btn-submit-documents">
                    Gửi duyệt hồ sơ bổ sung
                </button>
            </div>
        </div>


    </main>

    <%-- Footer --%>
    <jsp:include page="/views/layout/footer.jsp" />
</div>

</body>
</html>

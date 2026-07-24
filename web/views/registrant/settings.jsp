<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>

<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Cài Đặt Tài Khoản - Lái Vui</title>
    <meta name="description" content="Thiết lập mật khẩu, tùy chọn bảo mật và quản lý thông báo cho tài khoản của bạn tại Lái Vui.">

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
    <jsp:param name="activeSidebar" value="settings" />
</jsp:include>

<div class="dashboard-shell">
    <main class="main-content" id="main-content">

        <%-- Breadcrumbs --%>
        <nav class="breadcrumbs" aria-label="Breadcrumb">
            <a href="${pageContext.request.contextPath}/registrant/dashboard">Trang chủ</a>
            <span class="breadcrumbs__separator" aria-hidden="true">/</span>
            <span class="breadcrumbs__current" aria-current="page">Cài đặt tài khoản</span>
        </nav>

        <%-- Page Header --%>
        <header class="page-header">
            <div class="page-title-wrap">
                <h1 class="page-title">Cài đặt tài khoản</h1>
                <p class="page-subtitle">Đổi mật khẩu và xem thông tin nhận thông báo qua Gmail trên tài khoản.</p>
            </div>
        </header>

        <%-- Notification Banner --%>
        <c:if test="${not empty error}">
            <section class="p-alert-banner" aria-label="Thông báo lỗi">
                <div class="p-alert-banner__content">
                    <span class="p-alert-banner__title">Cập nhật thất bại</span>
                    <span>${error}</span>
                </div>
            </section>
        </c:if>
        <c:if test="${not empty param.success}">
            <section class="p-alert-banner" aria-label="Thông báo hệ thống">
                <svg width="20" height="20" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                    <circle cx="12" cy="12" r="10" stroke="currentColor" stroke-width="2"></circle>
                    <path d="M9 12l2 2 4-4" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"></path>
                </svg>
                <div class="p-alert-banner__content">
                    <span class="p-alert-banner__title">Cập nhật thành công</span>
                    <span>Cấu hình tài khoản của bạn đã được cập nhật thành công trên hệ thống.</span>
                </div>
            </section>
        </c:if>

        <%-- Layout Grid --%>
        <div class="profile-layout-grid">

            <%-- LEFT COLUMN: Forms Stack --%>
            <div class="settings-main-column">

                <%-- Card 1: Password Change Form --%>
                <section class="p-form-card" aria-label="Cài đặt mật khẩu">
                    <div class="p-form-header">
                        <h2 class="p-form-title">
                            <svg width="20" height="20" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                                <rect x="3" y="11" width="18" height="11" rx="2" stroke="currentColor" stroke-width="2"></rect>
                                <path d="M7 11V7a5 5 0 0 1 10 0v4" stroke="currentColor" stroke-width="2" stroke-linecap="round"></path>
                            </svg>
                            Đổi mật khẩu bảo mật
                        </h2>
                    </div>

                    <div class="p-form-body">
                        
                        <%-- Password Guidelines Banner --%>
                        <div class="exam-details-rules-banner" style="margin-top: 0; margin-bottom: 24px;">
                            <div class="rules-banner__icon">
                                <svg width="18" height="18" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg" aria-hidden="true">
                                    <circle cx="12" cy="12" r="10" stroke="currentColor" stroke-width="2"/>
                                    <line x1="12" y1="16" x2="12" y2="12" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
                                    <circle cx="12" cy="8" r="1.25" fill="currentColor"/>
                                </svg>
                            </div>
                            <div class="rules-banner__content">
                                <span class="rules-banner__title">Yêu cầu về mật khẩu an sau</span>
                                <p class="rules-banner__desc">
                                    Mật khẩu của bạn nên chứa <strong>tối thiểu 8 ký tự</strong>, bao gồm ít nhất một chữ hoa, một chữ thường, một chữ số và một ký tự đặc biệt (ví dụ: @, #, $, !...) để bảo vệ tài khoản tốt nhất.
                                </p>
                            </div>
                        </div>

                        <form action="${pageContext.request.contextPath}/registrant/settings" method="post" id="settings-password-form">
                            <input type="hidden" name="formId" value="password">
                            
                            <div class="p-form-grid p-form-grid--full">
                                
                                <%-- Current Password --%>
                                <div class="p-input-group">
                                    <label class="p-input-label" for="currentPassword">Mật khẩu hiện tại</label>
                                    <div class="p-input-wrapper">
                                        <span class="p-input-icon">
                                            <svg width="18" height="18" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                                                <rect x="3" y="11" width="18" height="11" rx="2" stroke="currentColor" stroke-width="1.8"/>
                                                <path d="M7 11V7a5 5 0 0 1 10 0v4" stroke="currentColor" stroke-width="1.8"/>
                                            </svg>
                                        </span>
                                        <input type="password" class="p-input-field" id="currentPassword" name="currentPassword" placeholder="Nhập mật khẩu hiện tại" required>
                                    </div>
                                </div>

                                <%-- New Password --%>
                                <div class="p-input-group">
                                    <label class="p-input-label" for="newPassword">Mật khẩu mới</label>
                                    <div class="p-input-wrapper">
                                        <span class="p-input-icon">
                                            <svg width="18" height="18" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                                                <path d="M12 22s8-4 8-10V5l-8-3-8 3v7c0 6 8 10 8 10z" stroke="currentColor" stroke-width="1.8" stroke-linejoin="round"/>
                                            </svg>
                                        </span>
                                        <input type="password" class="p-input-field" id="newPassword" name="newPassword" placeholder="Nhập mật khẩu mới" required>
                                    </div>
                                </div>

                                <%-- Confirm Password --%>
                                <div class="p-input-group">
                                    <label class="p-input-label" for="confirmPassword">Xác nhận mật khẩu mới</label>
                                    <div class="p-input-wrapper">
                                        <span class="p-input-icon">
                                            <svg width="18" height="18" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                                                <path d="M20 6L9 17l-5-5" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round"/>
                                            </svg>
                                        </span>
                                        <input type="password" class="p-input-field" id="confirmPassword" name="confirmPassword" placeholder="Xác nhận mật khẩu mới" required>
                                    </div>
                                </div>

                            </div>

                            <%-- Submit Button --%>
                            <div style="margin-top: 10px;">
                                <button type="submit" class="btn-header-primary" style="width: 100%; height: 46px; justify-content: center; font-size: 14px; border: none; cursor: pointer;">
                                    <svg width="16" height="16" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg" style="margin-right: 8px;">
                                        <path d="M19 21H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h11l5 5v11a2 2 0 0 1-2 2z" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                                        <polyline points="17 21 17 13 7 13 7 21" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                                        <polyline points="7 3 7 8 15 8" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                                    </svg>
                                    Cập nhật mật khẩu mới
                                </button>
                            </div>

                        </form>
                    </div>
                </section>

            </div>


            <%-- RIGHT COLUMN: Preferences & account summary --%>
            <div class="dashboard-sidebar-column">

                <%-- Gmail: chỉ thông báo (hệ thống luôn gửi khi đã cấu hình; không lưu tùy chọn) --%>
                <section class="p-form-card" aria-label="Thông báo qua Gmail">
                    <div class="p-form-header">
                        <h2 class="p-form-title">
                            <svg width="20" height="20" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                                <path d="M18 8A6 6 0 0 0 6 8c0 7-3 9-3 9h18s-3-2-3-9M13.73 21a2 2 0 0 1-3.46 0" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                            </svg>
                            Thông báo qua Gmail
                        </h2>
                    </div>

                    <div class="p-form-body">
                        <p class="settings-gmail-intro">
                            Hệ thống chỉ gửi thông báo qua <strong>Gmail</strong> tới địa chỉ đã đăng ký trên tài khoản.
                            Bạn không cần bật/tắt — khi máy chủ đã cấu hình Gmail, các thông báo dưới đây sẽ được gửi tự động.
                        </p>
                        <p class="settings-gmail-email">
                            <span class="settings-gmail-email__label">Email nhận thông báo</span>
                            <span class="settings-gmail-email__value">${not empty userEmail ? userEmail : '—'}</span>
                        </p>
                        <c:if test="${not emailServiceConfigured}">
                            <p class="settings-gmail-warning">
                                Dịch vụ Gmail chưa được cấu hình trên máy chủ — thông báo có thể không gửi được.
                            </p>
                        </c:if>

                        <div class="settings-gmail-info" role="list">
                            <div class="settings-gmail-info__item" role="listitem">
                                <span class="settings-gmail-info__badge" aria-hidden="true">✓</span>
                                <div class="settings-gmail-info__text">
                                    <span class="settings-gmail-info__title">Kết quả thi qua Gmail</span>
                                    <span class="settings-gmail-info__desc">Nhận bảng điểm lý thuyết, sa hình và đường trường sau khi hoàn thành phần thi.</span>
                                </div>
                            </div>
                            <div class="settings-gmail-info__item" role="listitem">
                                <span class="settings-gmail-info__badge" aria-hidden="true">✓</span>
                                <div class="settings-gmail-info__text">
                                    <span class="settings-gmail-info__title">Thông báo đổi mật khẩu qua Gmail</span>
                                    <span class="settings-gmail-info__desc">Gửi email xác nhận khi bạn đổi mật khẩu thành công trong phần cài đặt.</span>
                                </div>
                            </div>
                        </div>
                    </div>
                </section>

                <%-- Account summary (replaces hardcoded security status) --%>
                <section class="p-form-card" aria-label="Tóm tắt tài khoản">
                    <div class="p-form-header">
                        <h2 class="p-form-title">
                            <svg width="20" height="20" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                                <path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                                <circle cx="12" cy="7" r="4" stroke="currentColor" stroke-width="2"/>
                            </svg>
                            Tóm tắt tài khoản
                        </h2>
                    </div>

                    <div class="p-form-body">
                        <div class="security-audit-list">
                            <div class="security-audit-item">
                                <span class="security-audit-lbl">Họ tên</span>
                                <span class="security-audit-val">${accountDisplayName}</span>
                            </div>
                            <div class="security-audit-item">
                                <span class="security-audit-lbl">Tên đăng nhập</span>
                                <span class="security-audit-val" style="font-family: monospace; font-size: 12px;">${accountUsername}</span>
                            </div>
                            <div class="security-audit-item">
                                <span class="security-audit-lbl">Trạng thái hồ sơ</span>
                                <span class="security-audit-val settings-account-status settings-account-status--${profileRegistrationStatusClass}">${profileRegistrationStatusLabel}</span>
                            </div>
                            <div class="security-audit-item">
                                <span class="security-audit-lbl">Ảnh CCCD</span>
                                <span class="security-audit-val settings-account-status
                                    <c:choose>
                                        <c:when test="${cccdImagesComplete}"> settings-account-status--success</c:when>
                                        <c:when test="${hasProfile}"> settings-account-status--warning</c:when>
                                        <c:otherwise> settings-account-status--gray</c:otherwise>
                                    </c:choose>">${cccdStatusLabel}</span>
                            </div>
                            <div class="security-audit-item">
                                <span class="security-audit-lbl">Đăng ký thi đang active</span>
                                <span class="security-audit-val">
                                    <c:choose>
                                        <c:when test="${activeExamRegistrationCount > 0}">
                                            ${activeExamRegistrationCount} hạng
                                            <c:if test="${not empty activeLicenceClassesLabel}"> (${activeLicenceClassesLabel})</c:if>
                                        </c:when>
                                        <c:otherwise>Chưa có</c:otherwise>
                                    </c:choose>
                                </span>
                            </div>
                        </div>

                        <nav class="settings-account-links" aria-label="Liên kết nhanh">
                            <a href="${pageContext.request.contextPath}/registrant/profile" class="settings-account-links__item">Hồ sơ cá nhân</a>
                            <a href="${pageContext.request.contextPath}/registrant/upload-documents" class="settings-account-links__item">Tài liệu</a>
                            <a href="${pageContext.request.contextPath}/registrant/my-exams" class="settings-account-links__item">Lịch thi &amp; kết quả</a>
                        </nav>
                    </div>
                </section>

            </div>

        </div>

    </main>

    <%-- Footer --%>
    <jsp:include page="/views/layout/footer.jsp" />
</div>

</body>
</html>

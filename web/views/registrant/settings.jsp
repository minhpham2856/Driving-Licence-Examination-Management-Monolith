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
    <jsp:include page="/views/registrant/components/registrant-styles.jsp" />
</head>
<body class="has-side-nav-bar">

<%-- Inject registrant sidebar --%>
<jsp:include page="/views/registrant/components/sidebar.jsp">
    <jsp:param name="activeSidebar" value="settings" />
</jsp:include>

<div class="dashboard-shell">
    <main class="main-content" id="main-content">

        <%-- Breadcrumbs --%>
        <nav class="breadcrumbs" aria-label="Breadcrumb">
            <a href="${pageContext.request.contextPath}/views/registrant/dashboard.jsp">Trang chủ</a>
            <span class="breadcrumbs__separator" aria-hidden="true">/</span>
            <span class="breadcrumbs__current" aria-current="page">Cài đặt tài khoản</span>
        </nav>

        <%-- Page Header --%>
        <header class="page-header">
            <div class="page-title-wrap">
                <h1 class="page-title">Cài đặt tài khoản</h1>
                <p class="page-subtitle">Thiết lập mật khẩu và các tùy chọn bảo mật, thông báo cho tài khoản của bạn.</p>
            </div>
        </header>

        <%-- Notification Banner (Mock Alert for success/info) --%>
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

                        <form action="#" method="post" id="settings-password-form">
                            
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

                <%-- Card 2: Danger Zone / Deactivate Account --%>
                <section class="p-form-card p-form-card--danger" aria-label="Vô hiệu hóa tài khoản">
                    <div class="p-form-header">
                        <h2 class="p-form-title">
                            <svg width="20" height="20" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                                <path d="M18.36 6.64a9 9 0 1 1-12.73 0M12 2v10" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                            </svg>
                            Vô hiệu hóa tài khoản
                        </h2>
                    </div>

                    <div class="p-form-body">
                        <%-- Warning Banner --%>
                        <div class="exam-details-rules-banner" style="margin-top: 0; margin-bottom: 20px; background-color: #fff5f5; border-left-color: #ef4444;">
                            <div class="rules-banner__icon" style="color: #dc2626;">
                                <svg width="18" height="18" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg" aria-hidden="true">
                                    <path d="M10.29 3.86L1.82 18a2 2 0 0 0 1.71 3h16.94a2 2 0 0 0 1.71-3L13.71 3.86a2 2 0 0 0-3.42 0z" stroke="currentColor" stroke-width="2" stroke-linejoin="round"/>
                                    <line x1="12" y1="9" x2="12" y2="13" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
                                    <line x1="12" y1="17" x2="12.01" y2="17" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
                                </svg>
                            </div>
                            <div class="rules-banner__content">
                                <span class="rules-banner__title" style="color: #b91c1c;">Hành động này không thể hoàn tác</span>
                                <p class="rules-banner__desc" style="color: #dc2626;">
                                    Sau khi vô hiệu hóa tài khoản, toàn bộ dữ liệu đăng ký dự thi, thông tin cá nhân, hồ sơ bệnh án và lịch sử điểm số sát hạch của bạn sẽ bị đóng băng. Bạn sẽ không thể truy cập lại hệ thống Lái Vui.
                                </p>
                            </div>
                        </div>

                        <form action="#" method="post" id="settings-deactivate-form" onsubmit="return confirm('Bạn có chắc chắn muốn vô hiệu hóa tài khoản này không? Mọi lịch thi và hồ sơ hiện tại sẽ bị hủy bỏ hoàn toàn!');">
                            
                            <%-- Confirmation Checkbox --%>
                            <label class="danger-zone-checkbox-container">
                                <input type="checkbox" id="confirmDeactivate" name="confirmDeactivate" onchange="document.getElementById('btn-submit-deactivate').disabled = !this.checked;">
                                <span>Tôi xác nhận đã hiểu rõ các hệ lụy và đồng ý vô hiệu hóa tài khoản này vĩnh viễn.</span>
                            </label>

                            <%-- Submit Button --%>
                            <button type="submit" id="btn-submit-deactivate" class="p-btn-danger" disabled>
                                <svg width="16" height="16" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                                    <path d="M18.36 6.64a9 9 0 1 1-12.73 0M12 2v10" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                                </svg>
                                Xác nhận vô hiệu hóa tài khoản
                            </button>

                        </form>
                    </div>
                </section>

            </div>


            <%-- RIGHT COLUMN: Preferences & Security Status --%>
            <div class="dashboard-sidebar-column">

                <%-- System Notifications Preferences Card --%>
                <section class="p-form-card" aria-label="Tùy chọn hệ thống">
                    <div class="p-form-header">
                        <h2 class="p-form-title">
                            <svg width="20" height="20" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                                <path d="M18 8A6 6 0 0 0 6 8c0 7-3 9-3 9h18s-3-2-3-9M13.73 21a2 2 0 0 1-3.46 0" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                            </svg>
                            Cài đặt nhận thông báo
                        </h2>
                    </div>

                    <div class="p-form-body">
                        <form action="#" method="post" id="settings-prefs-form">
                            
                            <%-- SMS Notify --%>
                            <div class="toggle-switch-container">
                                <div class="toggle-switch-label-wrap">
                                    <span class="toggle-switch-title">Thông báo lịch thi qua SMS</span>
                                    <span class="toggle-switch-desc">Nhận tin nhắn nhắc lịch phòng thi và số báo danh trực tiếp về SĐT đã đăng ký.</span>
                                </div>
                                <label class="toggle-switch">
                                    <input type="checkbox" name="smsNotify" checked>
                                    <span class="toggle-slider"></span>
                                </label>
                            </div>

                            <%-- Email Notify --%>
                            <div class="toggle-switch-container">
                                <div class="toggle-switch-label-wrap">
                                    <span class="toggle-switch-title">Kết quả thi qua Email</span>
                                    <span class="toggle-switch-desc">Nhận bảng điểm chi tiết phần thi lý thuyết và sa hình ngay sau khi hoàn thành.</span>
                                </div>
                                <label class="toggle-switch">
                                    <input type="checkbox" name="emailNotify" checked>
                                    <span class="toggle-slider"></span>
                                </label>
                            </div>

                            <%-- 2FA Security --%>
                            <div class="toggle-switch-container">
                                <div class="toggle-switch-label-wrap">
                                    <span class="toggle-switch-title">Bảo mật hai lớp (2FA)</span>
                                    <span class="toggle-switch-desc">Yêu cầu mã xác thực gửi qua email khi đăng nhập trên thiết bị lạ.</span>
                                </div>
                                <label class="toggle-switch">
                                    <input type="checkbox" name="twoFactorAuth">
                                    <span class="toggle-slider"></span>
                                </label>
                            </div>

                            <%-- Save Prefs Button --%>
                            <div style="margin-top: 20px;">
                                <button type="submit" class="p-btn-edit" style="width: 100%; justify-content: center; height: 42px; border-color: #0052cc; color: #0052cc; font-size: 13px;">
                                    Lưu cấu hình hệ thống
                                </button>
                            </div>

                        </form>
                    </div>
                </section>

                <%-- Security Summary Card --%>
                <section class="p-form-card" aria-label="Trạng thái bảo mật">
                    <div class="p-form-header">
                        <h2 class="p-form-title">
                            <svg width="20" height="20" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                                <path d="M12 22s8-4 8-10V5l-8-3-8 3v7c0 6 8 10 8 10z" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                            </svg>
                            Trạng thái bảo mật
                        </h2>
                    </div>

                    <div class="p-form-body">
                        <div class="security-audit-list">
                            
                            <div class="security-audit-item">
                                <span class="security-audit-lbl">Trạng thái xác thực</span>
                                <span class="security-audit-val" style="color: #16a34a;">Đã liên kết CCCD</span>
                            </div>

                            <div class="security-audit-item">
                                <span class="security-audit-lbl">Đăng nhập lần cuối</span>
                                <span class="security-audit-val">Hôm nay, 13:58</span>
                            </div>

                            <div class="security-audit-item">
                                <span class="security-audit-lbl">Thiết bị hiện tại</span>
                                <span class="security-audit-val">Chrome (Windows)</span>
                            </div>

                            <div class="security-audit-item">
                                <span class="security-audit-lbl">Địa chỉ IP hiện tại</span>
                                <span class="security-audit-val">192.168.1.15</span>
                            </div>

                        </div>
                    </div>
                </section>

            </div>

        </div>

    </main>

    <%-- Footer --%>
    <jsp:include page="/views/landing/components/footer.jsp" />
</div>

</body>
</html>

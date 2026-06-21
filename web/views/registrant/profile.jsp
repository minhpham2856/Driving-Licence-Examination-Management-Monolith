<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>

<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Hồ Sơ Cá Nhân - Lái Vui</title>
    <meta name="description" content="Quản lý hồ sơ cá nhân và tài liệu đính kèm thi sát hạch lái xe tại Lái Vui.">

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
    <jsp:param name="activeSidebar" value="profile" />
</jsp:include>

<div class="dashboard-shell">
    <main class="main-content" id="main-content">

        <%-- Breadcrumbs --%>
        <nav class="breadcrumbs" aria-label="Breadcrumb">
            <a href="${pageContext.request.contextPath}/views/registrant/dashboard.jsp">Trang chủ</a>
            <span class="breadcrumbs__separator" aria-hidden="true">/</span>
            <span class="breadcrumbs__current" aria-current="page">Hồ sơ cá nhân</span>
        </nav>

        <%-- Page Header --%>
        <header class="page-header">
            <div class="page-title-wrap">
                <h1 class="page-title">Hồ sơ cá nhân</h1>
                <p class="page-subtitle">Xem thông tin cá nhân và quản lý trạng thái hồ sơ của bạn</p>
            </div>
        </header>

        <%-- Notification Banner (If Medical Certificate is rejected) --%>
        <section class="p-alert-banner" aria-label="Thông báo hồ sơ">
            <svg width="20" height="20" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                <circle cx="12" cy="12" r="10" stroke="currentColor" stroke-width="2"></circle>
                <path d="M12 8v5M12 16h.01" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"></path>
            </svg>
            <div class="p-alert-banner__content">
                <span class="p-alert-banner__title">Yêu cầu bổ sung hồ sơ</span>
                <span>
                    Giấy khám sức khỏe của bạn bị từ chối do <strong>thiếu dấu giáp lai ảnh chân dung</strong>. Vui lòng tải lại ảnh chụp Giấy khám sức khỏe mới hợp lệ tại mục <a href="${pageContext.request.contextPath}/views/registrant/upload-documents.jsp" class="profile-checklist-link">Tải lên hồ sơ</a>.
                </span>
            </div>
        </section>

        <%-- Profile Page Grid --%>
        <div class="profile-layout-grid">
            
            <%-- Left side: Personal Details Form --%>
            <section class="p-form-card" aria-label="Thông tin chi tiết">
                <div class="p-form-header">
                    <h2 class="p-form-title">
                        <svg width="20" height="20" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                            <rect x="3" y="3" width="18" height="18" rx="2" stroke="currentColor" stroke-width="2"></rect>
                            <path d="M7 8h10M7 12h10M7 16h6" stroke="currentColor" stroke-width="2" stroke-linecap="round"></path>
                        </svg>
                        Chi tiết hồ sơ đăng ký
                    </h2>
                    <button class="p-btn-edit" id="btn-edit-profile" type="button">
                        <svg width="14" height="14" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                            <path d="M12 20h8M16.5 3.5a2.121 2.121 0 1 1 3 3L7 19l-4 1 1-4L16.5 3.5Z" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"></path>
                        </svg>
                        Chỉnh sửa
                    </button>
                </div>
                <div class="p-form-body">
                    <form action="#" method="post" id="profile-form">
                        
                        <%-- Section 1: Thông tin cá nhân --%>
                        <h3 class="p-form-section-title">I. Thông tin cá nhân</h3>
                        <div class="p-form-grid">
                            
                            <%-- Họ và tên --%>
                            <div class="p-input-group">
                                <label class="p-input-label" for="fullName">Họ và tên thí sinh</label>
                                <div class="p-input-wrapper">
                                    <span class="p-input-icon">
                                        <svg width="18" height="18" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                                            <circle cx="12" cy="8" r="4" stroke="currentColor" stroke-width="1.8"></circle>
                                            <path d="M4 20C4 16.686 7.582 14 12 14C16.418 14 20 16.686 20 20" stroke="currentColor" stroke-width="1.8" stroke-linecap="round"></path>
                                        </svg>
                                    </span>
                                    <input type="text" class="p-input-field" id="fullName" name="fullName" disabled value="${empty registrantName ? 'Nguyễn Văn A' : registrantName}">
                                </div>
                            </div>

                            <%-- Ngày sinh --%>
                            <div class="p-input-group">
                                <label class="p-input-label" for="dob">Ngày sinh</label>
                                <div class="p-input-wrapper">
                                    <span class="p-input-icon">
                                        <svg width="18" height="18" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                                            <rect x="3" y="4" width="18" height="16" rx="2" stroke="currentColor" stroke-width="1.8"></rect>
                                            <path d="M16 2v4M8 2v4M3 10h18" stroke="currentColor" stroke-width="1.8" stroke-linecap="round"></path>
                                        </svg>
                                    </span>
                                    <input type="date" class="p-input-field" id="dob" name="dob" disabled value="${empty birthday ? '1995-10-15' : birthday}">
                                </div>
                            </div>

                            <%-- Giới tính --%>
                            <div class="p-input-group">
                                <label class="p-input-label" for="gender">Giới tính</label>
                                <div class="p-input-wrapper">
                                    <span class="p-input-icon">
                                        <svg width="18" height="18" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                                            <circle cx="12" cy="10" r="8" stroke="currentColor" stroke-width="1.8"></circle>
                                            <path d="M12 10v6M10 13h4" stroke="currentColor" stroke-width="1.8" stroke-linecap="round"></path>
                                        </svg>
                                    </span>
                                    <select class="p-input-field p-input-field--select" id="gender" name="gender" disabled>
                                        <option value="Nam" ${empty gender or gender eq 'Nam' ? 'selected' : ''}>Nam</option>
                                        <option value="Nữ" ${gender eq 'Nữ' ? 'selected' : ''}>Nữ</option>
                                        <option value="Khác" ${gender eq 'Khác' ? 'selected' : ''}>Khác</option>
                                    </select>
                                </div>
                            </div>

                            <%-- Số điện thoại --%>
                            <div class="p-input-group">
                                <label class="p-input-label" for="phone">Số điện thoại</label>
                                <div class="p-input-wrapper">
                                    <span class="p-input-icon">
                                        <svg width="18" height="18" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                                            <path d="M22 16.92v3a2 2 0 0 1-2.18 2 19.79 19.79 0 0 1-8.63-3.07 19.5 19.5 0 0 1-6-6 19.79 19.79 0 0 1-3.07-8.67A2 2 0 0 1 4.11 2h3a2 2 0 0 1 2 1.72 12.84 12.84 0 0 0 .7 2.81 2 2 0 0 1-.45 2.11L8.09 9.91a16 16 0 0 0 6 6l1.27-1.27a2 2 0 0 1 2.11-.45 12.84 12.84 0 0 0 2.81.7A2 2 0 0 1 22 16.92z" stroke="currentColor" stroke-width="1.8" stroke-linejoin="round"></path>
                                        </svg>
                                    </span>
                                    <input type="tel" class="p-input-field" id="phone" name="phone" disabled value="${empty phone ? '0912345678' : phone}">
                                </div>
                            </div>

                            <%-- Địa chỉ Email --%>
                            <div class="p-input-group">
                                <label class="p-input-label" for="email">Địa chỉ Email</label>
                                <div class="p-input-wrapper">
                                    <span class="p-input-icon">
                                        <svg width="18" height="18" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                                            <rect x="3" y="4" width="18" height="16" rx="2" stroke="currentColor" stroke-width="1.8"></rect>
                                            <path d="m21 8-9 6-9-6" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round"></path>
                                        </svg>
                                    </span>
                                    <input type="email" class="p-input-field" id="email" name="email" disabled value="${empty email ? 'nguyenvana@gmail.com' : email}">
                                </div>
                            </div>

                            <%-- Địa chỉ thường trú --%>
                            <div class="p-input-group">
                                <label class="p-input-label" for="address">Địa chỉ thường trú</label>
                                <div class="p-input-wrapper">
                                    <span class="p-input-icon">
                                        <svg width="18" height="18" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                                            <path d="M12 2C8.13 2 5 5.13 5 9C5 14.25 12 22 12 22C12 22 19 14.25 19 9C19 5.13 15.87 2 12 2Z" stroke="currentColor" stroke-width="1.8" stroke-linejoin="round"></path>
                                            <circle cx="12" cy="9" r="2.5" stroke="currentColor" stroke-width="1.8"></circle>
                                        </svg>
                                    </span>
                                    <input type="text" class="p-input-field" id="address" name="address" disabled value="${empty address ? 'Số 12, Ngõ 45, Đường Láng, Quận Đống Đa, Hà Nội' : address}">
                                </div>
                            </div>
                        </div>

                        <%-- Section 2: Chứng minh nhân dân / CCCD --%>
                        <h3 class="p-form-section-title">II. Căn cước công dân</h3>
                        <div class="p-form-grid">
                            
                            <%-- Số CCCD --%>
                            <div class="p-input-group">
                                <label class="p-input-label" for="idCard">Số Số căn cước</label>
                                <div class="p-input-wrapper">
                                    <span class="p-input-icon">
                                        <svg width="18" height="18" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                                            <rect x="2" y="5" width="20" height="14" rx="2" stroke="currentColor" stroke-width="1.8"></rect>
                                            <circle cx="8" cy="12" r="2.5" stroke="currentColor" stroke-width="1.8"></circle>
                                            <path d="M14 10h4M14 14h3" stroke="currentColor" stroke-width="1.8" stroke-linecap="round"></path>
                                        </svg>
                                    </span>
                                    <input type="text" class="p-input-field" id="idCard" name="idCard" disabled value="${empty idCardNumber ? '037095001234' : idCardNumber}">
                                </div>
                            </div>

                            <%-- Ngày cấp --%>
                            <div class="p-input-group">
                                <label class="p-input-label" for="issueDate">Ngày cấp</label>
                                <div class="p-input-wrapper">
                                    <span class="p-input-icon">
                                        <svg width="18" height="18" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                                            <rect x="3" y="4" width="18" height="16" rx="2" stroke="currentColor" stroke-width="1.8"></rect>
                                            <path d="M16 2v4M8 2v4M3 10h18" stroke="currentColor" stroke-width="1.8" stroke-linecap="round"></path>
                                        </svg>
                                    </span>
                                    <input type="date" class="p-input-field" id="issueDate" name="issueDate" disabled value="${empty idIssueDate ? '2020-05-20' : idIssueDate}">
                                </div>
                            </div>

                            <%-- Nơi cấp --%>
                            <div class="p-input-group">
                                <label class="p-input-label" for="issuePlace">Nơi cấp</label>
                                <div class="p-input-wrapper">
                                    <span class="p-input-icon">
                                        <svg width="18" height="18" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                                            <path d="M12 22c5.523 0 10-4.477 10-10S17.523 2 12 2 2 6.477 2 12s4.477 10 10 10Z" stroke="currentColor" stroke-width="1.8"></path>
                                            <path d="M3.6 9h16.8M3.6 15h16.8M12 2a15.3 15.3 0 0 1 4 10 15.3 15.3 0 0 1-4 10 15.3 15.3 0 0 1-4-10 15.3 15.3 0 0 1 4-10Z" stroke="currentColor" stroke-width="1.8"></path>
                                        </svg>
                                    </span>
                                    <input type="text" class="p-input-field" id="issuePlace" name="issuePlace" disabled value="${empty idIssuePlace ? 'Cục Cảnh sát QLHC về TTXH' : idIssuePlace}">
                                </div>
                            </div>

                            <%-- Hạng GPLX đăng ký --%>
                            <div class="p-input-group">
                                <label class="p-input-label" for="licenceRegistered">Hạng GPLX đăng ký sát hạch</label>
                                <div class="p-input-wrapper">
                                    <span class="p-input-icon">
                                        <svg width="18" height="18" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                                            <rect x="1" y="4" width="18" height="12" rx="2" stroke="currentColor" stroke-width="1.8"></rect>
                                            <circle cx="6" cy="10" r="2" stroke="currentColor" stroke-width="1.8"></circle>
                                            <path d="M10 7.5H16M10 10H14M10 12.5H15" stroke="currentColor" stroke-width="1.8" stroke-linecap="round"></path>
                                        </svg>
                                    </span>
                                    <select class="p-input-field p-input-field--select" id="licenceRegistered" name="licenceRegistered" disabled>
                                        <option value="A1" ${licenceClass eq 'A1' ? 'selected' : ''}>Hạng A1 (Mô tô 2 bánh dưới 175cc)</option>
                                        <option value="A2" ${licenceClass eq 'A2' ? 'selected' : ''}>Hạng A2 (Mô tô 2 bánh trên 175cc)</option>
                                        <option value="B1" ${licenceClass eq 'B1' ? 'selected' : ''}>Hạng B1 (Ô tô số tự động)</option>
                                        <option value="B2" ${empty licenceClass or licenceClass eq 'B2' ? 'selected' : ''}>Hạng B2 (Ô tô chở người đến 9 chỗ, tải dưới 3.5t)</option>
                                        <option value="C" ${licenceClass eq 'C' ? 'selected' : ''}>Hạng C (Xe tải trên 3.5t)</option>
                                    </select>
                                </div>
                            </div>
                        </div>

                    </form>
                </div>
            </section>

            <%-- Right side: Summary widgets --%>
            <div class="dashboard-sidebar-column">
                
                <%-- Widget 1: Profile Photo Card --%>
                <div class="profile-photo-card">
                    <div class="profile-photo-wrapper">
                        <img src="${pageContext.request.contextPath}/assets/imgs/LOGO.png" alt="Ảnh chân dung" class="profile-photo-img" onerror="this.src='https://images.unsplash.com/photo-1534528741775-53994a69daeb?q=80&w=250&auto=format&fit=crop'">
                        <div class="profile-photo-badge" aria-label="Đổi ảnh chân dung">
                            <svg width="14" height="14" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                                <path d="M23 19a2 2 0 0 1-2 2H3a2 2 0 0 1-2-2V8a2 2 0 0 1 2-2h4l2-3h6l2 3h4a2 2 0 0 1 2 2z" stroke="currentColor" stroke-width="2" stroke-linejoin="round"></path>
                                <circle cx="12" cy="13" r="4" stroke="currentColor" stroke-width="2"></circle>
                            </svg>
                        </div>
                    </div>
                    <div>
                        <h2 class="profile-photo-name">${empty registrantName ? 'Nguyễn Văn A' : registrantName}</h2>
                        <div class="profile-photo-role">Thí sinh B2</div>
                    </div>
                </div>

                <%-- Widget 2: Completeness Progress Card --%>
                <div class="profile-completion-card">
                    <div class="profile-completion-card__header">
                        <h2 class="profile-completion-card__title">Trạng thái hồ sơ</h2>
                        <span class="profile-completion-card__badge">Chờ duyệt</span>
                    </div>
                    <p class="profile-completion-card__status-msg">
                        Tài liệu định danh của bạn đã được xác minh thành công. Hãy bổ sung <strong>Giấy khám sức khỏe hợp lệ</strong> để đủ điều kiện xét duyệt đợt thi.
                    </p>
                </div>

                <%-- Widget 3: Upload Documents Status Checklist --%>
                <div class="profile-checklist-card">
                    <h3 class="profile-checklist-title">Danh sách tài liệu đính kèm</h3>
                    <div class="profile-checklist-list">
                        
                        <%-- Item 1: Ảnh 3x4 --%>
                        <div class="profile-checklist-item">
                            <div class="profile-checklist-label-wrap">
                                <div class="profile-checklist-dot profile-checklist-dot--checked" aria-hidden="true">
                                    <svg width="12" height="12" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                                        <path d="M20 6L9 17l-5-5" stroke="currentColor" stroke-width="3" stroke-linecap="round" stroke-linejoin="round"></path>
                                    </svg>
                                </div>
                                <span>Ảnh chân dung 3x4</span>
                            </div>
                            <span class="r-stat-card__badge r-stat-card__badge--success">Đã duyệt</span>
                        </div>

                        <%-- Item 2: Mặt trước CCCD --%>
                        <div class="profile-checklist-item">
                            <div class="profile-checklist-label-wrap">
                                <div class="profile-checklist-dot profile-checklist-dot--checked" aria-hidden="true">
                                    <svg width="12" height="12" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                                        <path d="M20 6L9 17l-5-5" stroke="currentColor" stroke-width="3" stroke-linecap="round" stroke-linejoin="round"></path>
                                    </svg>
                                </div>
                                <span>Ảnh mặt trước CCCD</span>
                            </div>
                            <span class="r-stat-card__badge r-stat-card__badge--success">Đã duyệt</span>
                        </div>

                        <%-- Item 3: Mặt sau CCCD --%>
                        <div class="profile-checklist-item">
                            <div class="profile-checklist-label-wrap">
                                <div class="profile-checklist-dot profile-checklist-dot--checked" aria-hidden="true">
                                    <svg width="12" height="12" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                                        <path d="M20 6L9 17l-5-5" stroke="currentColor" stroke-width="3" stroke-linecap="round" stroke-linejoin="round"></path>
                                    </svg>
                                </div>
                                <span>Ảnh mặt sau CCCD</span>
                            </div>
                            <span class="r-stat-card__badge r-stat-card__badge--success">Đã duyệt</span>
                        </div>

                        <%-- Item 4: Giấy khám sức khỏe --%>
                        <div class="profile-checklist-item">
                            <div class="profile-checklist-label-wrap">
                                <div class="profile-checklist-dot profile-checklist-dot--pending" aria-hidden="true">
                                    <svg width="12" height="12" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                                        <path d="M18 6L6 18M6 6l12 12" stroke="currentColor" stroke-width="3" stroke-linecap="round" stroke-linejoin="round"></path>
                                    </svg>
                                </div>
                                <span>Giấy khám sức khỏe</span>
                            </div>
                            <a href="${pageContext.request.contextPath}/views/registrant/upload-documents.jsp" class="profile-checklist-link">Bổ sung ngay</a>
                        </div>
                    </div>
                </div>

            </div>

        </div>

    </main>

    <%-- Footer --%>
    <jsp:include page="/views/layout/footer.jsp" />
</div>

</body>
</html>

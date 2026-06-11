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

    <link rel="preconnect" href="https://fonts.googleapis.com">
    <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
    <link href="https://fonts.googleapis.com/css2?family=Be+Vietnam+Pro:wght@400;500;600;700;800&family=Inter:wght@400;500;600;700;800&display=swap" rel="stylesheet">

    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/style.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/layout.css">
</head>
<body class="has-side-nav-bar">

<jsp:include page="/views/layout/sidebar-registrant.jsp">
    <jsp:param name="activeSidebar" value="profile" />
</jsp:include>

<div class="dashboard-shell">
    <main class="main-content" id="main-content">

        <nav class="breadcrumbs" aria-label="Breadcrumb">
            <a href="${pageContext.request.contextPath}/registrant/dashboard">Trang chủ</a>
            <span class="breadcrumbs__separator" aria-hidden="true">/</span>
            <span class="breadcrumbs__current" aria-current="page">Hồ sơ cá nhân</span>
        </nav>

        <header class="page-header">
            <div class="page-title-wrap">
                <h1 class="page-title">Hồ sơ cá nhân</h1>
                <p class="page-subtitle">Xem thông tin cá nhân và quản lý trạng thái hồ sơ của bạn</p>
            </div>
        </header>

        <c:if test="${not empty success}">
            <section class="p-alert-banner" style="background:#ecfdf5;border-color:#6ee7b7;color:#065f46;" aria-label="Thông báo thành công">
                <svg width="20" height="20" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                    <path d="M20 6L9 17l-5-5" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round"></path>
                </svg>
                <div class="p-alert-banner__content">
                    <span class="p-alert-banner__title">Thành công</span>
                    <span>${success}</span>
                </div>
            </section>
        </c:if>

        <c:if test="${not empty error}">
            <section class="p-alert-banner" aria-label="Thông báo lỗi">
                <svg width="20" height="20" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                    <circle cx="12" cy="12" r="10" stroke="currentColor" stroke-width="2"></circle>
                    <path d="M12 8v5M12 16h.01" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"></path>
                </svg>
                <div class="p-alert-banner__content">
                    <span class="p-alert-banner__title">Không thể lưu hồ sơ</span>
                    <span>${error}</span>
                </div>
            </section>
        </c:if>

        <c:if test="${showRejectionAlert}">
            <section class="p-alert-banner" aria-label="Thông báo hồ sơ">
                <svg width="20" height="20" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                    <circle cx="12" cy="12" r="10" stroke="currentColor" stroke-width="2"></circle>
                    <path d="M12 8v5M12 16h.01" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"></path>
                </svg>
                <div class="p-alert-banner__content">
                    <span class="p-alert-banner__title">Yêu cầu bổ sung hồ sơ</span>
                    <span>
                        ${rejectionReason}. Vui lòng cập nhật hồ sơ tại mục
                        <a href="${pageContext.request.contextPath}/registrant/upload-documents" class="profile-checklist-link">Tải lên hồ sơ</a>.
                    </span>
                </div>
            </section>
        </c:if>

        <div class="profile-layout-grid">

            <section class="p-form-card" aria-label="Thông tin chi tiết">
                <div class="p-form-header">
                    <h2 class="p-form-title">
                        <svg width="20" height="20" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                            <rect x="3" y="3" width="18" height="18" rx="2" stroke="currentColor" stroke-width="2"></rect>
                            <path d="M7 8h10M7 12h10M7 16h6" stroke="currentColor" stroke-width="2" stroke-linecap="round"></path>
                        </svg>
                        Chi tiết hồ sơ đăng ký
                    </h2>
                    <div style="display:flex;gap:8px;">
                        <button class="p-btn-edit" id="btn-edit-profile" type="button">
                            <svg width="14" height="14" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                                <path d="M12 20h8M16.5 3.5a2.121 2.121 0 1 1 3 3L7 19l-4 1 1-4L16.5 3.5Z" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"></path>
                            </svg>
                            Chỉnh sửa
                        </button>
                        <button class="p-btn-edit" id="btn-save-profile" type="submit" form="profile-form" style="display:none;background:#0052cc;color:#fff;border-color:#0052cc;">
                            Lưu hồ sơ
                        </button>
                        <button class="p-btn-edit" id="btn-cancel-profile" type="button" style="display:none;">
                            Hủy
                        </button>
                    </div>
                </div>
                <div class="p-form-body">
                    <form action="${pageContext.request.contextPath}/registrant/profile" method="post" id="profile-form">

                        <h3 class="p-form-section-title">I. Thông tin cá nhân</h3>
                        <div class="p-form-grid">

                            <div class="p-input-group">
                                <label class="p-input-label" for="fullName">Họ và tên thí sinh</label>
                                <div class="p-input-wrapper">
                                    <span class="p-input-icon">
                                        <svg width="18" height="18" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                                            <circle cx="12" cy="8" r="4" stroke="currentColor" stroke-width="1.8"></circle>
                                            <path d="M4 20C4 16.686 7.582 14 12 14C16.418 14 20 16.686 20 20" stroke="currentColor" stroke-width="1.8" stroke-linecap="round"></path>
                                        </svg>
                                    </span>
                                    <input type="text" class="p-input-field profile-editable" id="fullName" name="fullName" disabled required
                                           value="${registrantName}">
                                </div>
                            </div>

                            <div class="p-input-group">
                                <label class="p-input-label" for="dob">Ngày sinh</label>
                                <div class="p-input-wrapper">
                                    <span class="p-input-icon">
                                        <svg width="18" height="18" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                                            <rect x="3" y="4" width="18" height="16" rx="2" stroke="currentColor" stroke-width="1.8"></rect>
                                            <path d="M16 2v4M8 2v4M3 10h18" stroke="currentColor" stroke-width="1.8" stroke-linecap="round"></path>
                                        </svg>
                                    </span>
                                    <input type="date" class="p-input-field profile-editable" id="dob" name="dob" disabled required
                                           value="${birthday}">
                                </div>
                            </div>

                            <div class="p-input-group">
                                <label class="p-input-label" for="gender">Giới tính</label>
                                <div class="p-input-wrapper">
                                    <span class="p-input-icon">
                                        <svg width="18" height="18" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                                            <circle cx="12" cy="10" r="8" stroke="currentColor" stroke-width="1.8"></circle>
                                            <path d="M12 10v6M10 13h4" stroke="currentColor" stroke-width="1.8" stroke-linecap="round"></path>
                                        </svg>
                                    </span>
                                    <select class="p-input-field p-input-field--select profile-editable" id="gender" name="gender" disabled>
                                        <option value="Nam" ${gender eq 'Nam' ? 'selected' : ''}>Nam</option>
                                        <option value="Nữ" ${gender eq 'Nữ' ? 'selected' : ''}>Nữ</option>
                                    </select>
                                </div>
                            </div>

                            <div class="p-input-group">
                                <label class="p-input-label" for="phone">Số điện thoại</label>
                                <div class="p-input-wrapper">
                                    <span class="p-input-icon">
                                        <svg width="18" height="18" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                                            <path d="M22 16.92v3a2 2 0 0 1-2.18 2 19.79 19.79 0 0 1-8.63-3.07 19.5 19.5 0 0 1-6-6 19.79 19.79 0 0 1-3.07-8.67A2 2 0 0 1 4.11 2h3a2 2 0 0 1 2 1.72 12.84 12.84 0 0 0 .7 2.81 2 2 0 0 1-.45 2.11L8.09 9.91a16 16 0 0 0 6 6l1.27-1.27a2 2 0 0 1 2.11-.45 12.84 12.84 0 0 0 2.81.7A2 2 0 0 1 22 16.92z" stroke="currentColor" stroke-width="1.8" stroke-linejoin="round"></path>
                                        </svg>
                                    </span>
                                    <input type="tel" class="p-input-field profile-editable" id="phone" name="phone" disabled required
                                           value="${phone}">
                                </div>
                            </div>

                            <div class="p-input-group">
                                <label class="p-input-label" for="email">Địa chỉ Email</label>
                                <div class="p-input-wrapper">
                                    <span class="p-input-icon">
                                        <svg width="18" height="18" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                                            <rect x="3" y="4" width="18" height="16" rx="2" stroke="currentColor" stroke-width="1.8"></rect>
                                            <path d="m21 8-9 6-9-6" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round"></path>
                                        </svg>
                                    </span>
                                    <input type="email" class="p-input-field profile-editable" id="email" name="email" disabled
                                           value="${email}">
                                </div>
                            </div>

                            <div class="p-input-group">
                                <label class="p-input-label" for="address">Địa chỉ thường trú</label>
                                <div class="p-input-wrapper">
                                    <span class="p-input-icon">
                                        <svg width="18" height="18" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                                            <path d="M12 2C8.13 2 5 5.13 5 9C5 14.25 12 22 12 22C12 22 19 14.25 19 9C19 5.13 15.87 2 12 2Z" stroke="currentColor" stroke-width="1.8" stroke-linejoin="round"></path>
                                            <circle cx="12" cy="9" r="2.5" stroke="currentColor" stroke-width="1.8"></circle>
                                        </svg>
                                    </span>
                                    <input type="text" class="p-input-field profile-editable" id="address" name="address" disabled
                                           value="${address}">
                                </div>
                            </div>
                        </div>

                        <h3 class="p-form-section-title">II. Căn cước công dân</h3>
                        <div class="p-form-grid">

                            <div class="p-input-group">
                                <label class="p-input-label" for="idCard">Số CCCD / CMND</label>
                                <div class="p-input-wrapper">
                                    <span class="p-input-icon">
                                        <svg width="18" height="18" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                                            <rect x="2" y="5" width="20" height="14" rx="2" stroke="currentColor" stroke-width="1.8"></rect>
                                            <circle cx="8" cy="12" r="2.5" stroke="currentColor" stroke-width="1.8"></circle>
                                            <path d="M14 10h4M14 14h3" stroke="currentColor" stroke-width="1.8" stroke-linecap="round"></path>
                                        </svg>
                                    </span>
                                    <input type="text" class="p-input-field profile-editable" id="idCard" name="idCard" disabled
                                           value="${idCardNumber}">
                                </div>
                            </div>

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
                                        <c:choose>
                                            <c:when test="${empty licenceClass}">
                                                <option value="">Chưa đăng ký đợt thi</option>
                                            </c:when>
                                            <c:otherwise>
                                                <option value="${licenceClass}" selected>Hạng ${licenceClass}</option>
                                            </c:otherwise>
                                        </c:choose>
                                        <c:forEach var="licenseType" items="${licenseTypes}">
                                            <c:if test="${licenseType.licenseCode ne licenceClass}">
                                                <option value="${licenseType.licenseCode}">Hạng ${licenseType.licenseCode}</option>
                                            </c:if>
                                        </c:forEach>
                                    </select>
                                </div>
                                <p style="font-size:0.8rem;color:#64748b;margin:6px 0 0;">Hạng bằng lấy từ đợt thi đã đăng ký, không chỉnh sửa tại đây.</p>
                            </div>
                        </div>

                    </form>
                </div>
            </section>

            <div class="dashboard-sidebar-column">

                <div class="profile-photo-card">
                    <div class="profile-photo-wrapper">
                        <c:choose>
                            <c:when test="${not empty photoUrl}">
                                <c:choose>
                                    <c:when test="${fn:startsWith(photoUrl, 'http')}">
                                        <c:set var="displayPhoto" value="${photoUrl}" />
                                    </c:when>
                                    <c:when test="${fn:startsWith(photoUrl, '/')}">
                                        <c:set var="displayPhoto" value="${pageContext.request.contextPath}${photoUrl}" />
                                    </c:when>
                                    <c:otherwise>
                                        <c:set var="displayPhoto" value="${pageContext.request.contextPath}/${photoUrl}" />
                                    </c:otherwise>
                                </c:choose>
                            </c:when>
                            <c:otherwise>
                                <c:set var="displayPhoto" value="${pageContext.request.contextPath}/assets/imgs/LOGO.png" />
                            </c:otherwise>
                        </c:choose>
                        <img src="${displayPhoto}" alt="Ảnh chân dung" class="profile-photo-img"
                             onerror="this.src='${pageContext.request.contextPath}/assets/imgs/LOGO.png'">
                        <div class="profile-photo-badge" aria-hidden="true">
                            <svg width="14" height="14" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                                <path d="M23 19a2 2 0 0 1-2 2H3a2 2 0 0 1-2-2V8a2 2 0 0 1 2-2h4l2-3h6l2 3h4a2 2 0 0 1 2 2z" stroke="currentColor" stroke-width="2" stroke-linejoin="round"></path>
                                <circle cx="12" cy="13" r="4" stroke="currentColor" stroke-width="2"></circle>
                            </svg>
                        </div>
                    </div>
                    <div>
                        <h2 class="profile-photo-name">${registrantName}</h2>
                        <div class="profile-photo-role">${profileRoleLabel}</div>
                    </div>
                </div>

                <div class="profile-completion-card">
                    <div class="profile-completion-card__header">
                        <h2 class="profile-completion-card__title">Trạng thái hồ sơ</h2>
                        <span class="profile-completion-card__badge r-stat-card__badge r-stat-card__badge--${profileStatusBadgeClass}">${profileStatusBadge}</span>
                    </div>
                    <p class="profile-completion-card__status-msg">${profileStatusMessage}</p>
                </div>

                <div class="profile-checklist-card">
                    <h3 class="profile-checklist-title">Danh sách tài liệu đính kèm</h3>
                    <div class="profile-checklist-list">
                        <c:forEach var="doc" items="${documentList}">
                            <div class="profile-checklist-item">
                                <div class="profile-checklist-label-wrap">
                                    <div class="profile-checklist-dot profile-checklist-dot--${doc.dotClass}" aria-hidden="true">
                                        <c:choose>
                                            <c:when test="${doc.uploaded}">
                                                <svg width="12" height="12" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                                                    <path d="M20 6L9 17l-5-5" stroke="currentColor" stroke-width="3" stroke-linecap="round" stroke-linejoin="round"></path>
                                                </svg>
                                            </c:when>
                                            <c:otherwise>
                                                <svg width="12" height="12" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                                                    <path d="M18 6L6 18M6 6l12 12" stroke="currentColor" stroke-width="3" stroke-linecap="round" stroke-linejoin="round"></path>
                                                </svg>
                                            </c:otherwise>
                                        </c:choose>
                                    </div>
                                    <span>${doc.label}</span>
                                </div>
                                <c:choose>
                                    <c:when test="${doc.showUploadLink}">
                                        <a href="${pageContext.request.contextPath}/registrant/upload-documents" class="profile-checklist-link">Bổ sung ngay</a>
                                    </c:when>
                                    <c:otherwise>
                                        <span class="r-stat-card__badge r-stat-card__badge--${doc.statusClass}">${doc.statusLabel}</span>
                                    </c:otherwise>
                                </c:choose>
                            </div>
                        </c:forEach>
                    </div>
                </div>

            </div>
        </div>

    </main>

    <jsp:include page="/views/layout/footer.jsp" />
</div>

<script>
(function () {
    var editBtn = document.getElementById('btn-edit-profile');
    var saveBtn = document.getElementById('btn-save-profile');
    var cancelBtn = document.getElementById('btn-cancel-profile');
    var fields = document.querySelectorAll('.profile-editable');

    function setEditing(editing) {
        fields.forEach(function (field) {
            field.disabled = !editing;
        });
        editBtn.style.display = editing ? 'none' : 'inline-flex';
        saveBtn.style.display = editing ? 'inline-flex' : 'none';
        cancelBtn.style.display = editing ? 'inline-flex' : 'none';
    }

    editBtn.addEventListener('click', function () {
        setEditing(true);
    });

    cancelBtn.addEventListener('click', function () {
        window.location.href = '${pageContext.request.contextPath}/registrant/profile';
    });
})();
</script>

</body>
</html>

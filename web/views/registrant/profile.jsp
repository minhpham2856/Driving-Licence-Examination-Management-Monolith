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

        <c:if test="${showHealthAlert}">
        <section class="p-alert-banner" aria-label="Thông báo hồ sơ">
            <svg width="20" height="20" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                <circle cx="12" cy="12" r="10" stroke="currentColor" stroke-width="2"></circle>
                <path d="M12 8v5M12 16h.01" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"></path>
            </svg>
            <div class="p-alert-banner__content">
                <span class="p-alert-banner__title">Yêu cầu bổ sung hồ sơ</span>
                <span>
                    Giấy khám sức khỏe của bạn cần được bổ sung hoặc tải lại. Vui lòng cập nhật tại mục
                    <a href="${pageContext.request.contextPath}/registrant/upload-documents" class="profile-checklist-link">Tải lên hồ sơ</a>.
                </span>
            </div>
        </section>
        </c:if>

        <c:if test="${not empty param.success}">
            <section class="p-alert-banner" aria-label="Thông báo thành công">
                <div class="p-alert-banner__content">
                    <span class="p-alert-banner__title">Cập nhật thành công</span>
                    <span>Thông tin hồ sơ cá nhân đã được lưu.</span>
                </div>
            </section>
        </c:if>

        <c:if test="${not empty error and empty openEditModal}">
            <section class="p-alert-banner" aria-label="Thông báo lỗi">
                <div class="p-alert-banner__content">
                    <span class="p-alert-banner__title">Không thể cập nhật</span>
                    <span>${error}</span>
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
                    <c:choose>
                        <c:when test="${profileIncomplete}">
                            <button class="p-btn-edit" id="btn-edit-profile" type="button">
                                <svg width="14" height="14" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                                    <path d="M12 20h8M16.5 3.5a2.121 2.121 0 1 1 3 3L7 19l-4 1 1-4L16.5 3.5Z" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"></path>
                                </svg>
                                Bổ sung hồ sơ
                            </button>
                        </c:when>
                        <c:otherwise>
                            <button class="p-btn-edit" id="btn-edit-profile" type="button">
                                <svg width="14" height="14" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                                    <path d="M12 20h8M16.5 3.5a2.121 2.121 0 1 1 3 3L7 19l-4 1 1-4L16.5 3.5Z" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"></path>
                                </svg>
                                Chỉnh sửa
                            </button>
                        </c:otherwise>
                    </c:choose>
                </div>
                <div class="p-form-body" id="profile-view-panel">

                    <h3 class="p-form-section-title">I. Thông tin cá nhân</h3>
                    <div class="p-form-grid">
                        <div class="p-input-group">
                            <label class="p-input-label" for="view-fullName">Họ và tên thí sinh</label>
                            <div class="p-input-wrapper">
                                <input type="text" class="p-input-field p-input-field--no-icon" id="view-fullName" value="${registrantName}" disabled>
                            </div>
                        </div>
                        <div class="p-input-group">
                            <label class="p-input-label" for="view-dob">Ngày sinh</label>
                            <div class="p-input-wrapper">
                                <input type="date" class="p-input-field p-input-field--no-icon" id="view-dob" value="${birthday}" disabled>
                            </div>
                        </div>
                        <div class="p-input-group">
                            <label class="p-input-label" for="view-gender">Giới tính</label>
                            <div class="p-input-wrapper">
                                <select class="p-input-field p-input-field--select p-input-field--no-icon" id="view-gender" disabled>
                                    <option value="Nam" ${gender eq 'Nam' ? 'selected' : ''}>Nam</option>
                                    <option value="Nữ" ${gender eq 'Nữ' ? 'selected' : ''}>Nữ</option>
                                </select>
                            </div>
                        </div>
                        <div class="p-input-group">
                            <label class="p-input-label" for="view-phone">Số điện thoại</label>
                            <div class="p-input-wrapper">
                                <input type="tel" class="p-input-field p-input-field--no-icon" id="view-phone" value="${phone}" disabled>
                            </div>
                        </div>
                        <div class="p-input-group">
                            <label class="p-input-label" for="view-email">Địa chỉ Email</label>
                            <div class="p-input-wrapper">
                                <input type="email" class="p-input-field p-input-field--no-icon" id="view-email" value="${email}" disabled>
                            </div>
                        </div>
                        <div class="p-input-group">
                            <label class="p-input-label" for="view-address">Địa chỉ thường trú</label>
                            <div class="p-input-wrapper">
                                <input type="text" class="p-input-field p-input-field--no-icon" id="view-address" value="${address}" disabled>
                            </div>
                        </div>
                    </div>

                    <h3 class="p-form-section-title">II. Căn cước công dân</h3>
                    <div class="p-form-grid p-form-grid--single-row">
                        <div class="p-input-group">
                            <label class="p-input-label" for="view-idCard">Số căn cước / CMND</label>
                            <div class="p-input-wrapper">
                                <input type="text" class="p-input-field p-input-field--no-icon" id="view-idCard"
                                       value="${not empty idCardNumber ? idCardNumber : '-'}" disabled
                                       inputmode="numeric" autocomplete="off">
                            </div>
                            <c:if test="${not idCardEditable}">
                                <span class="profile-edit-hint">Số định danh đã khóa sau khi hồ sơ được duyệt.</span>
                            </c:if>
                            <c:if test="${idCardEditable}">
                                <span class="profile-edit-hint">Nhập đúng số in trên thẻ - Ban quản lý sẽ đối chiếu với ảnh căn cước.</span>
                            </c:if>
                        </div>
                    </div>

                    <div class="profile-cccd-note" aria-label="Thông tin trên thẻ căn cước">
                        <p class="profile-cccd-note__title">Ngày cấp &amp; nơi cấp trên thẻ</p>
                        <p class="profile-cccd-note__text">
                            Không cần nhập tay. Thông tin chi tiết trên căn cước được lưu qua
                            <strong>ảnh mặt trước và mặt sau</strong> tại mục tải hồ sơ - Ban quản lý đối chiếu khi duyệt.
                        </p>
                        <ul class="profile-cccd-note__list">
                            <li>
                                <span class="profile-cccd-note__dot profile-cccd-note__dot--${cccdFrontUploaded ? 'done' : 'pending'}"></span>
                                Mặt trước căn cước
                                <c:choose>
                                    <c:when test="${cccdFrontUploaded}"> - đã tải lên</c:when>
                                    <c:otherwise> - chưa tải</c:otherwise>
                                </c:choose>
                            </li>
                            <li>
                                <span class="profile-cccd-note__dot profile-cccd-note__dot--${cccdBackUploaded ? 'done' : 'pending'}"></span>
                                Mặt sau căn cước
                                <c:choose>
                                    <c:when test="${cccdBackUploaded}"> - đã tải lên</c:when>
                                    <c:otherwise> - chưa tải</c:otherwise>
                                </c:choose>
                            </li>
                        </ul>
                        <c:if test="${not cccdImagesComplete}">
                            <a href="${pageContext.request.contextPath}/registrant/upload-documents" class="profile-checklist-link">Tải ảnh căn cước</a>
                        </c:if>
                    </div>

                    <h3 class="p-form-section-title">III. Đăng ký sát hạch</h3>
                    <div class="profile-exam-block">
                        <p class="profile-exam-summary__hint">
                            Một thí sinh có thể đăng ký nhiều hạng GPLX. Hạng được xác định khi chọn đợt thi,
                            không lấy từ thông tin trên thẻ căn cước. Giữa các hạng khác nhau chỉ được thi vào ngày khác nhau.
                        </p>

                        <c:choose>
                            <c:when test="${hasActiveExamRegistrations}">
                                <ul class="profile-exam-list" aria-label="Danh sách đăng ký sát hạch">
                                    <c:forEach var="exam" items="${activeExamRegistrations}">
                                        <li class="profile-exam-list__item">
                                            <div class="profile-exam-list__head">
                                                <span class="profile-exam-list__licence">
                                                    <c:choose>
                                                        <c:when test="${not empty exam.licenceClassDescription}">
                                                            ${exam.licenceClassDescription}
                                                        </c:when>
                                                        <c:otherwise>Hạng ${exam.licenceClass}</c:otherwise>
                                                    </c:choose>
                                                </span>
                                                <span class="status-badge status-badge--${exam.statusClass}">${exam.statusLabel}</span>
                                            </div>
                                            <p class="profile-exam-list__session">${exam.examName}</p>
                                            <p class="profile-exam-list__meta">
                                                Ngày thi:
                                                <fmt:formatDate value="${exam.examDate}" pattern="dd/MM/yyyy"/>
                                                <c:choose>
                                                    <c:when test="${exam.sessionTimePublished}">
                                                        · Giờ ca:
                                                        <fmt:formatDate value="${exam.sessionStart}" pattern="HH:mm"/>
                                                        <c:if test="${not empty exam.sessionEnd}">
                                                            –<fmt:formatDate value="${exam.sessionEnd}" pattern="HH:mm"/>
                                                        </c:if>
                                                    </c:when>
                                                    <c:otherwise>
                                                        · Chờ Ban sát hạch mở ca
                                                    </c:otherwise>
                                                </c:choose>
                                                <c:if test="${not empty exam.location}">
                                                    · ${exam.location}
                                                </c:if>
                                            </p>
                                        </li>
                                    </c:forEach>
                                </ul>
                            </c:when>
                            <c:otherwise>
                                <p class="profile-exam-empty">Chưa có đăng ký sát hạch.</p>
                            </c:otherwise>
                        </c:choose>

                        <a href="${pageContext.request.contextPath}/registrant/register-exam" class="profile-checklist-link">
                            <c:choose>
                                <c:when test="${hasActiveExamRegistrations}">Đăng ký thêm / xem đợt thi</c:when>
                                <c:otherwise>Đăng ký sát hạch</c:otherwise>
                            </c:choose>
                        </a>
                    </div>
                </div>
            </section>

            <div class="dashboard-sidebar-column">
                <div class="profile-photo-card">
                    <div class="profile-photo-wrapper">
                        <img src="${pageContext.request.contextPath}/assets/imgs/LOGO.png" alt="Ảnh chân dung" class="profile-photo-img">
                    </div>
                    <div class="profile-photo-meta">
                        <h2 class="profile-photo-name">${not empty registrantName ? registrantName : '-'}</h2>

                        <div class="profile-photo-tags" aria-label="Vai trò thí sinh">
                            <span class="profile-photo-tag profile-photo-tag--role">Thí sinh</span>
                            <c:if test="${not empty activeLicenceClassesLabel}">
                                <span class="profile-photo-tag profile-photo-tag--licence">Hạng ${activeLicenceClassesLabel}</span>
                            </c:if>
                            <c:if test="${empty activeLicenceClassesLabel and not empty licenceClass}">
                                <span class="profile-photo-tag profile-photo-tag--licence">Hạng ${licenceClass}</span>
                            </c:if>
                        </div>
                    </div>
                </div>

                <div class="profile-completion-card profile-completion-card--${profileIncomplete ? 'incomplete' : (not empty documentSummary ? documentSummary.overallStatusClass : 'complete')}">
                    <div class="profile-completion-card__header">
                        <h2 class="profile-completion-card__title">Trạng thái hồ sơ</h2>
                        <c:if test="${not empty documentSummary}">
                            <span class="profile-completion-card__badge profile-completion-card__badge--${documentSummary.overallStatusClass}">
                                ${documentSummary.overallStatusLabel}
                            </span>
                        </c:if>
                    </div>
                    <c:if test="${not empty documentSummary and documentSummary.requiredTotal > 0}">
                        <div class="profile-completion-card__progress-wrap">
                            <div class="profile-completion-card__progress-text">
                                <span>Giấy tờ bắt buộc</span>
                                <span class="profile-completion-card__percentage">${documentSummary.requiredUploaded}/${documentSummary.requiredTotal}</span>
                            </div>
                            <div class="profile-completion-card__bar-bg">
                                <div class="profile-completion-card__bar-fill" style="width: ${documentSummary.requiredProgressPercent}%;"></div>
                            </div>
                        </div>
                    </c:if>
                    <p class="profile-completion-card__status-msg">
                        <c:choose>
                            <c:when test="${profileIncomplete}">Hồ sơ còn thiếu thông tin. Bấm &ldquo;Bổ sung hồ sơ&rdquo; để điền các mục còn trống.</c:when>
                            <c:when test="${not empty documentSummary}">${documentSummary.overallMessage}</c:when>
                            <c:when test="${not empty registrantName}">Thông tin hồ sơ đã được tải từ hệ thống.</c:when>
                            <c:otherwise>Chưa có dữ liệu hồ sơ. Vui lòng bổ sung thông tin cá nhân.</c:otherwise>
                        </c:choose>
                    </p>
                </div>

                <div class="profile-checklist-card profile-checklist-card--${not empty documentSummary ? documentSummary.overallStatusClass : 'complete'}">
                    <h2 class="profile-checklist-title">Tài liệu đính kèm</h2>
                    <p class="profile-checklist-desc">
                        <c:if test="${not empty profileRegistrationStatusLabel}">
                            <span class="status-badge status-badge--mini status-badge--${profileRegistrationStatusClass eq 'success' ? 'approved' : (profileRegistrationStatusClass eq 'danger' ? 'rejected' : (profileRegistrationStatusClass eq 'pending' ? 'pending' : 'info'))}" style="margin-bottom:0.5rem;display:inline-block;">
                                Hồ sơ: ${profileRegistrationStatusLabel}
                            </span>
                            <br>
                        </c:if>
                        <c:choose>
                            <c:when test="${not empty documentSummary}">
                                ${documentSummary.requiredUploaded}/${documentSummary.requiredTotal} giấy tờ bắt buộc
                                <c:if test="${documentSummary.otherCount > 0}"> · ${documentSummary.otherCount} hồ sơ khác</c:if>
                                · ${documentSummary.overallStatusLabel}
                            </c:when>
                            <c:otherwise>
                                Quản lý và tải lên giấy tờ, ảnh chân dung và các tài liệu bổ sung cho hồ sơ thi.
                            </c:otherwise>
                        </c:choose>
                    </p>
                    <c:if test="${not empty documentSummary and not empty documentSummary.checklistItems}">
                        <ul class="profile-checklist-list" aria-label="Danh sách giấy tờ bắt buộc">
                            <c:forEach var="item" items="${documentSummary.checklistItems}">
                                <li class="profile-checklist-item">
                                    <span class="profile-checklist-label-wrap">
                                        <span class="profile-checklist-dot profile-checklist-dot--${item.uploaded ? 'checked' : 'pending'}" aria-hidden="true">
                                            <c:if test="${item.uploaded}">✓</c:if>
                                        </span>
                                        ${item.label}
                                    </span>
                                    <span class="status-badge status-badge--mini status-badge--${item.statusClass eq 'success' ? 'approved' : (item.statusClass eq 'danger' ? 'rejected' : (item.statusClass eq 'warning' ? 'pending' : 'gray'))}">${item.statusLabel}</span>
                                </li>
                            </c:forEach>
                        </ul>
                    </c:if>
                    <a href="${pageContext.request.contextPath}/registrant/upload-documents" class="profile-checklist-action">
                        <c:choose>
                            <c:when test="${not empty documentSummary and documentSummary.requiredUploaded lt documentSummary.requiredTotal}">Tải lên giấy tờ còn thiếu</c:when>
                            <c:when test="${not empty documentSummary and documentSummary.awaitingSubmitCount gt 0}">Gửi duyệt hồ sơ</c:when>
                            <c:otherwise>Tải lên / quản lý hồ sơ</c:otherwise>
                        </c:choose>
                        <svg width="14" height="14" viewBox="0 0 24 24" fill="none" aria-hidden="true">
                            <path d="M5 12h14M13 6l6 6-6 6" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                        </svg>
                    </a>
                </div>
            </div>
        </div>

    </main>

    <jsp:include page="/views/layout/footer.jsp" />
</div>

<%-- Modal chỉnh sửa hồ sơ --%>
<div id="profile-edit-modal" class="modal-backdrop" role="dialog" aria-modal="true" aria-labelledby="profile-edit-modal-title" hidden>
    <div class="score-modal-content profile-edit-modal">
        <div class="modal-header">
            <h3 id="profile-edit-modal-title">
                <c:choose>
                    <c:when test="${profileIncomplete}">Bổ sung hồ sơ cá nhân</c:when>
                    <c:otherwise>Chỉnh sửa hồ sơ cá nhân</c:otherwise>
                </c:choose>
            </h3>
            <button type="button" class="btn-close-modal" id="btn-close-profile-modal" aria-label="Đóng">&times;</button>
        </div>
        <form method="post" action="${pageContext.request.contextPath}/registrant/profile" id="profile-edit-form">
            <div class="modal-body">
                <p id="profile-edit-modal-error" class="profile-edit-modal__error" hidden></p>

                <div class="p-form-grid">
                    <div class="p-input-group">
                        <label class="p-input-label" for="edit-fullName">Họ và tên thí sinh <span class="profile-edit-required">*</span></label>
                        <input type="text" class="p-input-field p-input-field--no-icon" id="edit-fullName" name="fullName"
                               value="${not empty param.fullName ? param.fullName : registrantName}"
                               placeholder="Nhập họ và tên" required>
                    </div>
                    <div class="p-input-group">
                        <label class="p-input-label" for="edit-dob">Ngày sinh</label>
                        <input type="date" class="p-input-field p-input-field--no-icon" id="edit-dob" name="dob"
                               value="${not empty param.dob ? param.dob : birthday}">
                    </div>
                    <div class="p-input-group">
                        <label class="p-input-label" for="edit-gender">Giới tính</label>
                        <c:set var="editGender" value="${not empty param.gender ? param.gender : gender}" />
                        <select class="p-input-field p-input-field--select p-input-field--no-icon" id="edit-gender" name="gender">
                            <option value="Nam" ${editGender eq 'Nam' ? 'selected' : ''}>Nam</option>
                            <option value="Nữ" ${editGender eq 'Nữ' ? 'selected' : ''}>Nữ</option>
                        </select>
                    </div>
                    <div class="p-input-group">
                        <label class="p-input-label" for="edit-phone">Số điện thoại</label>
                        <input type="tel" class="p-input-field p-input-field--no-icon" id="edit-phone" name="phone"
                               value="${not empty param.phone ? param.phone : phone}"
                               placeholder="Nhập số điện thoại">
                    </div>
                    <div class="p-input-group">
                        <label class="p-input-label" for="edit-email">Địa chỉ Email</label>
                        <input type="email" class="p-input-field p-input-field--no-icon" id="edit-email" value="${email}" readonly>
                        <span class="profile-edit-hint">Email tài khoản không đổi qua form này.</span>
                    </div>
                    <div class="p-input-group">
                        <label class="p-input-label" for="edit-address">Địa chỉ thường trú</label>
                        <input type="text" class="p-input-field p-input-field--no-icon" id="edit-address" name="address"
                               value="${not empty param.address ? param.address : address}"
                               placeholder="Nhập địa chỉ thường trú">
                    </div>
                    <div class="p-input-group">
                        <label class="p-input-label" for="edit-idCard">Số căn cước / CMND</label>
                        <input type="text" class="p-input-field p-input-field--no-icon" id="edit-idCard" name="idCard"
                               value="${not empty param.idCard ? param.idCard : idCardNumber}"
                               placeholder="Nhập 12 số căn cước hoặc 9 số CMND"
                               inputmode="numeric" maxlength="12" autocomplete="off"
                               ${not idCardEditable ? 'readonly' : ''}>
                        <c:choose>
                            <c:when test="${not idCardEditable}">
                                <span class="profile-edit-hint">Không thể đổi số căn cước sau khi hồ sơ đã được duyệt.</span>
                            </c:when>
                            <c:otherwise>
                                <span class="profile-edit-hint">Phải khớp số in trên thẻ. Ngày cấp / nơi cấp xem trên ảnh căn cước đã tải.</span>
                            </c:otherwise>
                        </c:choose>
                    </div>
                </div>
            </div>
            <div class="modal-footer">
                <button type="button" class="welcome-banner__btn welcome-banner__btn--outline" id="btn-cancel-profile-modal">Hủy</button>
                <button type="submit" class="welcome-banner__btn welcome-banner__btn--primary">Lưu thay đổi</button>
            </div>
        </form>
    </div>
</div>

<input type="hidden" id="profile-edit-open-flag" value="${openEditModal ? '1' : ''}"
       data-error-message="${fn:escapeXml(error)}"
       data-id-card-editable="${idCardEditable ? '1' : '0'}">

<script src="${pageContext.request.contextPath}/assets/js/registrant/profile-edit-modal.js" charset="UTF-8"></script>

</body>
</html>

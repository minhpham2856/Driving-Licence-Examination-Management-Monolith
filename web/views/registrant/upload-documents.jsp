<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>

<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Upload Hồ Sơ Bổ Sung - Lái Vui</title>
    <meta name="description" content="Tải lên và cập nhật hồ sơ, ảnh chân dung, CCCD và giấy khám sức khỏe để đăng ký thi sát hạch lái xe tại Lái Vui.">

    <link rel="preconnect" href="https://fonts.googleapis.com">
    <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
    <link href="https://fonts.googleapis.com/css2?family=Be+Vietnam+Pro:wght@400;500;600;700;800&family=Inter:wght@400;500;600;700;800&display=swap" rel="stylesheet">

    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/style.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/layout.css">
</head>
<body class="has-side-nav-bar">

<jsp:include page="/views/layout/sidebar-registrant.jsp">
    <jsp:param name="activeSidebar" value="upload-documents" />
</jsp:include>

<div class="dashboard-shell">
    <main class="main-content" id="main-content">

        <nav class="breadcrumbs" aria-label="Breadcrumb">
            <a href="${pageContext.request.contextPath}/registrant/dashboard">Trang chủ</a>
            <span class="breadcrumbs__separator" aria-hidden="true">/</span>
            <a href="${pageContext.request.contextPath}/registrant/profile">Hồ sơ cá nhân</a>
            <span class="breadcrumbs__separator" aria-hidden="true">/</span>
            <span class="breadcrumbs__current" aria-current="page">Upload hồ sơ bổ sung</span>
        </nav>

        <header class="page-header">
            <div class="page-title-wrap">
                <h1 class="page-title">Upload hồ sơ bổ sung</h1>
                <p class="page-subtitle">Tải lên tài liệu đính kèm để hoàn tất thủ tục xét duyệt hồ sơ sát hạch</p>
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
                <div class="p-alert-banner__content">
                    <span class="p-alert-banner__title">Không thể xử lý</span>
                    <span>${error}</span>
                </div>
            </section>
        </c:if>

        <section class="p-alert-banner" aria-label="Hướng dẫn upload">
            <svg width="20" height="20" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                <circle cx="12" cy="12" r="10" stroke="currentColor" stroke-width="2"></circle>
                <path d="M12 16v-4M12 8h.01" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"></path>
            </svg>
            <div class="p-alert-banner__content">
                <span class="p-alert-banner__title">Yêu cầu định dạng tệp tin</span>
                <span>
                    Chấp nhận các định dạng ảnh: <strong>PNG, JPG, JPEG</strong>. Dung lượng tối đa: <strong>5MB mỗi tệp</strong>.
                </span>
            </div>
        </section>

        <c:choose>
            <c:when test="${not empty hasProfile and hasProfile}">
                <form action="${pageContext.request.contextPath}/registrant/upload-documents" method="post"
                      enctype="multipart/form-data" id="upload-documents-form">

                    <div class="upload-grid">
                        <c:forEach var="slot" items="${uploadSlots}">
                            <div class="upload-card ${slot.cardClass}">
                                <div class="upload-card__header">
                                    <h2 class="upload-card__title">${slot.title}</h2>
                                    <span class="r-stat-card__badge r-stat-card__badge--${slot.statusClass}">${slot.statusLabel}</span>
                                </div>

                                <div class="upload-card__preview-box">
                                    <c:if test="${slot.showPreview}">
                                        <c:choose>
                                            <c:when test="${slot.imagePreview}">
                                                <c:set var="previewUrl" value="${slot.documentUrl}" />
                                                <c:if test="${fn:startsWith(slot.documentUrl, '/')}">
                                                    <c:set var="previewUrl" value="${pageContext.request.contextPath}${slot.documentUrl}" />
                                                </c:if>
                                                <img src="${previewUrl}" alt="${slot.title}" class="upload-card__preview-img"
                                                     onerror="this.src='${pageContext.request.contextPath}/assets/imgs/LOGO.png'">
                                            </c:when>
                                            <c:otherwise>
                                                <c:set var="previewUrl" value="${slot.documentUrl}" />
                                                <c:if test="${fn:startsWith(slot.documentUrl, '/')}">
                                                    <c:set var="previewUrl" value="${pageContext.request.contextPath}${slot.documentUrl}" />
                                                </c:if>
                                                <img src="${previewUrl}" alt="${slot.title}" class="upload-card__preview-img"
                                                     onerror="this.style.display='none'">
                                                <span class="upload-card__approved-label">Tài liệu đã tải lên</span>
                                            </c:otherwise>
                                        </c:choose>
                                    </c:if>

                                    <c:if test="${slot.showUpload}">
                                        <label class="upload-card__dropzone" for="file-${slot.slotKey}">
                                            <svg width="40" height="40" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                                                <path d="M12 16V8m0 8-3-3m3 3 3-3" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"></path>
                                                <path d="M20 16.58A5 5 0 0 0 18 7h-1.26A8 8 0 1 0 4 15.25" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"></path>
                                            </svg>
                                            <span class="upload-card__dropzone-text">Kéo thả hoặc nhấp để chọn tệp tải lên</span>
                                            <span class="upload-card__dropzone-sub">PNG, JPG, JPEG &lt; 5MB</span>
                                            <input type="file" id="file-${slot.slotKey}" name="${slot.fileInputName}"
                                                   accept="image/png,image/jpeg,image/jpg" class="upload-card__file-input"
                                                   style="display:none;">
                                        </label>
                                    </c:if>
                                </div>

                                <c:if test="${slot.canDelete}">
                                    <form action="${pageContext.request.contextPath}/registrant/upload-documents" method="post"
                                          style="margin-top:0.75rem;" onsubmit="return confirm('Xóa tài liệu này?');">
                                        <input type="hidden" name="action" value="delete">
                                        <input type="hidden" name="slotKey" value="${slot.slotKey}">
                                        <button type="submit" class="welcome-banner__btn welcome-banner__btn--outline"
                                                style="font-size:0.8rem;padding:0.35rem 0.75rem;">
                                            Xóa tài liệu
                                        </button>
                                    </form>
                                </c:if>

                                <c:if test="${slot.showFeedback}">
                                    <div class="upload-card__feedback upload-card__feedback--error">
                                        <svg width="14" height="14" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                                            <circle cx="12" cy="12" r="10" stroke="currentColor" stroke-width="2"></circle>
                                            <path d="M12 8v5M12 16h.01" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"></path>
                                        </svg>
                                        <span><strong>Lý do từ chối:</strong> ${slot.feedbackMessage}</span>
                                    </div>
                                </c:if>
                            </div>
                        </c:forEach>
                    </div>

                    <div class="upload-action-bar">
                        <div class="upload-action-bar__info">
                            <span class="upload-action-bar__title">Hoàn tất tải lên?</span>
                            <span class="upload-action-bar__subtitle">Hồ sơ sẽ chuyển sang trạng thái chờ duyệt sau khi bạn gửi.</span>
                        </div>
                        <div class="upload-action-bar__buttons">
                            <a href="${pageContext.request.contextPath}/registrant/profile" class="welcome-banner__btn welcome-banner__btn--outline upload-action-bar__btn-outline">
                                Quay lại hồ sơ
                            </a>
                            <button type="submit" name="submitForReview" value="true"
                                    class="welcome-banner__btn welcome-banner__btn--primary upload-action-bar__btn-primary">
                                Gửi duyệt hồ sơ bổ sung
                            </button>
                        </div>
                    </div>
                </form>
            </c:when>
            <c:otherwise>
                <section class="p-alert-banner" aria-label="Thiếu hồ sơ">
                    <div class="p-alert-banner__content">
                        <span class="p-alert-banner__title">Chưa có hồ sơ cá nhân</span>
                        <span>${missingProfileMessage}</span>
                    </div>
                </section>
                <a href="${pageContext.request.contextPath}/registrant/profile" class="welcome-banner__btn welcome-banner__btn--primary">
                    Tạo hồ sơ cá nhân
                </a>
            </c:otherwise>
        </c:choose>

    </main>

    <jsp:include page="/views/layout/footer.jsp" />
</div>

<script>
(function () {
    document.querySelectorAll('.upload-card__file-input').forEach(function (input) {
        input.addEventListener('change', function () {
            var label = this.closest('.upload-card__dropzone');
            if (!label || !this.files || !this.files.length) {
                return;
            }
            var text = label.querySelector('.upload-card__dropzone-text');
            if (text) {
                text.textContent = 'Đã chọn: ' + this.files[0].name;
            }
        });
    });
})();
</script>

</body>
</html>

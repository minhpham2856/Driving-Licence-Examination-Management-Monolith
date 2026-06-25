<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>

<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Upload Hồ Sơ Bổ Sung - Lái Vui</title>
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

        <header class="page-header page-header--compact">
            <div class="page-title-wrap">
                <h1 class="page-title">Upload hồ sơ bổ sung</h1>
                <p class="page-subtitle">Tải lên tài liệu và gửi yêu cầu duyệt cho ban quản lý</p>
            </div>
        </header>

        <c:if test="${not empty profileRegistrationStatusLabel}">
            <section class="upload-registration-status" aria-label="Trạng thái hồ sơ trên hệ thống">
                <div class="upload-registration-status__inner">
                    <span class="upload-registration-status__label">Trạng thái hồ sơ:</span>
                    <span class="r-stat-card__badge r-stat-card__badge--${profileRegistrationStatusClass}">
                        ${profileRegistrationStatusLabel}
                    </span>
                </div>
            </section>
        </c:if>

        <c:if test="${not empty registrationUserNotice}">
            <section class="p-alert-banner p-alert-banner--compact" aria-label="Thông báo đồng bộ">
                <div class="p-alert-banner__content">
                    <span>${registrationUserNotice}</span>
                </div>
            </section>
        </c:if>

        <c:if test="${param.success eq 'upload'}">
            <section class="p-alert-banner p-alert-banner--compact" aria-label="Thông báo thành công">
                <div class="p-alert-banner__content">
                    <span class="p-alert-banner__title">Tải lên thành công</span>
                    <span>
                        <c:choose>
                            <c:when test="${profileApproved}">
                                Hồ sơ bổ sung đã được lưu. Trạng thái hồ sơ vẫn là «Đã duyệt» cho đến khi bạn bấm «Gửi yêu cầu duyệt».
                            </c:when>
                            <c:otherwise>
                                Tài liệu đã được lưu. Bấm «Gửi yêu cầu duyệt» khi đã đủ hồ sơ.
                            </c:otherwise>
                        </c:choose>
                    </span>
                </div>
            </section>
        </c:if>

        <c:if test="${param.success eq 'delete'}">
            <section class="p-alert-banner p-alert-banner--compact" aria-label="Thông báo thành công">
                <div class="p-alert-banner__content">
                    <span class="p-alert-banner__title">Đã xóa tài liệu</span>
                    <span>Tệp đã được gỡ khỏi hồ sơ.</span>
                </div>
            </section>
        </c:if>

        <c:if test="${param.success eq 'request'}">
            <section class="p-alert-banner p-alert-banner--compact" aria-label="Thông báo thành công">
                <div class="p-alert-banner__content">
                    <span class="p-alert-banner__title">Đã gửi yêu cầu duyệt</span>
                    <span>Ban quản lý sẽ kiểm tra và phản hồi trạng thái hồ sơ của bạn.</span>
                </div>
            </section>
        </c:if>

        <c:if test="${not empty error}">
            <section class="p-alert-banner p-alert-banner--compact" aria-label="Thông báo lỗi">
                <div class="p-alert-banner__content">
                    <span class="p-alert-banner__title">Không thể xử lý</span>
                    <span>${error}</span>
                </div>
            </section>
        </c:if>

        <c:choose>
            <c:when test="${empty documentsByType}">
                <p class="upload-empty-msg">Chưa có hồ sơ cá nhân. Vui lòng hoàn tất đăng ký tài khoản trước khi upload tài liệu.</p>
            </c:when>
            <c:otherwise>
                <c:set var="mandatoryLocked" value="${profileApproved or hasPendingReview}" />
                <c:if test="${profileApproved}">
                    <section class="p-alert-banner register-exam-notice register-exam-notice--approved" style="margin-bottom:1.25rem;" aria-label="Hướng dẫn bổ sung hồ sơ">
                        <div class="p-alert-banner__content">
                            <span class="p-alert-banner__title">Hồ sơ đã được duyệt</span>
                            <span>Để bổ sung giấy tờ, hãy dùng mục <strong>Hồ sơ khác</strong> bên dưới.</span>
                        </div>
                    </section>
                </c:if>
                <div class="upload-page-layout">
                    <%-- 4 loại giấy tờ chuẩn --%>
                    <div class="upload-grid upload-grid--compact">
                        <c:forTokens var="docType" items="Portrait,IdFront,IdBack,HealthCertificate" delims=",">
                            <c:set var="doc" value="${documentsByType[docType]}" />
                            <c:set var="inputId" value="file-${docType}" />
                            <c:set var="docTitle" value="${docType eq 'Portrait' ? '1. Ảnh chân dung 3x4' : docType eq 'IdFront' ? '2. Mặt trước CCCD / CMND' : docType eq 'IdBack' ? '3. Mặt sau CCCD / CMND' : '4. Giấy khám sức khỏe lái xe'}" />

                            <div class="upload-card upload-card--${doc.statusClass}">
                                <div class="upload-card__header">
                                    <h2 class="upload-card__title">${docTitle}</h2>
                                    <span class="r-stat-card__badge r-stat-card__badge--${doc.statusClass}">${doc.statusLabel}</span>
                                </div>

                                <c:if test="${mandatoryLocked and not empty doc.documentUrl}">
                                    <p class="upload-card__locked-hint">Giấy tờ đã duyệt — không thể thay thế trực tiếp. Bổ sung qua mục «Hồ sơ khác».</p>
                                </c:if>

                                <c:if test="${not empty doc.notes and doc.statusClass eq 'danger'}">
                                    <div class="upload-card__feedback upload-card__feedback--error">
                                        <span><strong>Ghi chú:</strong> ${fn:substringBefore(doc.notes, ' · ')}</span>
                                    </div>
                                </c:if>

                                <form method="post" enctype="multipart/form-data"
                                      action="${pageContext.request.contextPath}/registrant/upload-documents"
                                      class="upload-card__form" data-upload-form>
                                    <input type="hidden" name="documentType" value="${docType}">

                                    <div class="upload-slot-wrap">
                                        <c:if test="${not empty doc.documentUrl and doc.documentId gt 0 and not mandatoryLocked}">
                                            <button type="submit"
                                                    form="delete-doc-${docType}"
                                                    class="upload-slot__delete"
                                                    title="Xóa tệp đã tải"
                                                    aria-label="Xóa tệp ${docTitle}"
                                                    onclick="return confirm('Xóa tệp đã tải cho ${docTitle}?');">×</button>
                                        </c:if>

                                    <label class="upload-slot" for="${inputId}" data-upload-slot data-has-file="${not empty doc.documentUrl ? '1' : '0'}">
                                        <c:choose>
                                            <c:when test="${not empty doc.documentUrl}">
                                                <div class="upload-slot__face upload-slot__face--ready" data-slot-ready>
                                                    <img src="${doc.documentUrl}" alt="" class="upload-slot__preview"
                                                         onerror="this.hidden=true;this.nextElementSibling.hidden=false;">
                                                    <p class="upload-slot__message upload-slot__message--missing" hidden>
                                                        <strong>Tệp không còn trên máy chủ</strong> (thường do redeploy). Vui lòng tải lên lại.
                                                    </p>
                                                    <p class="upload-slot__message">
                                                        <strong>Đã tải lên</strong><c:if test="${not empty doc.fileSizeLabel}"> · ${doc.fileSizeLabel}</c:if> · Bấm để thay tệp
                                                    </p>
                                                </div>
                                            </c:when>
                                            <c:otherwise>
                                                <div class="upload-slot__face upload-slot__face--empty" data-slot-empty>
                                                    <p class="upload-slot__message"><strong>Chọn tệp để tải lên</strong> · Tối đa 5MB · PNG, JPG, JPEG</p>
                                                </div>
                                            </c:otherwise>
                                        </c:choose>
                                        <div class="upload-slot__face upload-slot__face--picked" data-slot-picked hidden>
                                            <p class="upload-slot__message" data-picked-message></p>
                                        </div>
                                        <input type="file" id="${inputId}" name="documentFile" accept="image/png,image/jpeg"
                                               class="upload-slot__input" data-file-input
                                               ${mandatoryLocked ? 'disabled' : ''}>
                                    </label>
                                    </div>

                                    <div class="upload-card__actions">
                                        <button type="submit" class="welcome-banner__btn welcome-banner__btn--primary upload-card__submit"
                                                ${mandatoryLocked ? 'disabled' : ''}>Tải lên</button>
                                    </div>
                                </form>
                                <c:if test="${not empty doc.documentUrl and doc.documentId gt 0 and not mandatoryLocked}">
                                    <form method="post" id="delete-doc-${docType}"
                                          action="${pageContext.request.contextPath}/registrant/upload-documents"
                                          class="upload-card__delete-form">
                                        <input type="hidden" name="action" value="deleteDocument">
                                        <input type="hidden" name="documentId" value="${doc.documentId}">
                                    </form>
                                </c:if>
                            </div>
                        </c:forTokens>
                    </div>

                    <%-- Hồ sơ khác — upload nhiều tệp --%>
                    <section class="upload-other-section">
                        <div class="upload-other-section__header">
                            <div>
                                <h2 class="upload-other-section__title">5. Hồ sơ khác</h2>
                                <p class="upload-other-section__desc">Nộp nhiều giấy tờ bổ sung (cam kết, xác nhận cư trú, v.v.). Mỗi tệp cần ghi rõ lý do.</p>
                            </div>
                            <span class="upload-other-section__count">${otherDocumentCount} tệp đã nộp</span>
                        </div>

                        <c:if test="${not empty otherDocuments}">
                            <div class="upload-other-list">
                                <c:forEach var="other" items="${otherDocuments}" varStatus="st">
                                    <article class="upload-other-item upload-other-item--${other.statusClass}">
                                        <div class="upload-other-item__preview">
                                            <c:choose>
                                                <c:when test="${fn:contains(fn:toLowerCase(other.documentUrl), '.pdf')}">
                                                    <span class="upload-other-item__pdf">PDF</span>
                                                </c:when>
                                                <c:otherwise>
                                                    <img src="${other.documentUrl}" alt="">
                                                </c:otherwise>
                                            </c:choose>
                                        </div>
                                        <div class="upload-other-item__body">
                                            <div class="upload-other-item__meta">
                                                <span class="r-stat-card__badge r-stat-card__badge--${other.statusClass}">${other.statusLabel}</span>
                                                <span class="upload-other-item__size">
                                                    <c:choose>
                                                        <c:when test="${not empty other.fileSizeLabel}">${other.fileSizeLabel}</c:when>
                                                        <c:otherwise>—</c:otherwise>
                                                    </c:choose>
                                                    · Tối đa 5MB
                                                </span>
                                            </div>
                                            <p class="upload-other-item__note">${fn:substringBefore(other.notes, ' · ')}</p>
                                            <div class="upload-other-item__actions">
                                                <a href="${other.documentUrl}" target="_blank" rel="noopener" class="upload-other-item__link">Xem tệp</a>
                                                <c:if test="${other.documentId gt 0 and not hasPendingReview and (not profileApproved or other.statusLabel eq 'Chưa gửi duyệt')}">
                                                    <form method="post" action="${pageContext.request.contextPath}/registrant/upload-documents" class="upload-other-item__delete-form">
                                                        <input type="hidden" name="action" value="deleteDocument">
                                                        <input type="hidden" name="documentId" value="${other.documentId}">
                                                        <button type="submit" class="upload-other-item__delete" title="Xóa tệp"
                                                                onclick="return confirm('Xóa hồ sơ bổ sung này?');">×</button>
                                                    </form>
                                                </c:if>
                                            </div>
                                        </div>
                                    </article>
                                </c:forEach>
                            </div>
                        </c:if>

                        <form method="post" enctype="multipart/form-data"
                              action="${pageContext.request.contextPath}/registrant/upload-documents"
                              class="upload-other-form" data-upload-form>
                            <input type="hidden" name="documentType" value="Other">

                            <label class="p-input-label" for="other-reason">Lý do / ghi chú <span class="profile-edit-required">*</span></label>
                            <textarea id="other-reason" name="reasonNote" class="upload-card__reason" rows="2"
                                      placeholder="Ví dụ: Giấy cam kết bổ sung hồ sơ, giấy xác nhận cư trú..." required></textarea>

                            <label class="upload-slot upload-slot--inline" for="file-other" data-upload-slot data-multiple="1">
                                <div class="upload-slot__face upload-slot__face--empty" data-slot-empty>
                                    <p class="upload-slot__message"><strong>Chọn một hoặc nhiều tệp</strong> · Tối đa 5MB/tệp · PNG, JPG, JPEG, PDF</p>
                                </div>
                                <div class="upload-slot__face upload-slot__face--picked" data-slot-picked hidden>
                                    <p class="upload-slot__message" data-picked-message></p>
                                </div>
                                <input type="file" id="file-other" name="documentFile" accept="image/png,image/jpeg,application/pdf"
                                       class="upload-slot__input" data-file-input multiple>
                            </label>

                            <div class="upload-card__actions">
                                <button type="submit" class="welcome-banner__btn welcome-banner__btn--primary upload-card__submit">Thêm hồ sơ</button>
                            </div>
                        </form>
                    </section>

                    <section class="upload-request-card upload-request-card--compact">
                        <div class="upload-request-card__content">
                            <h2 class="upload-request-card__title">Gửi yêu cầu duyệt hồ sơ</h2>
                            <p class="upload-request-card__desc">
                                <c:choose>
                                    <c:when test="${profileApproved}">
                                        Chỉ gửi khi đã thêm hồ sơ bổ sung tại mục «Hồ sơ khác». Bấm gửi sẽ chuyển trạng thái hồ sơ sang «Chờ duyệt».
                                    </c:when>
                                    <c:otherwise>
                                        Sau khi tải đủ tài liệu, gửi yêu cầu để ban quản lý kiểm tra và phê duyệt.
                                    </c:otherwise>
                                </c:choose>
                            </p>
                            <c:if test="${hasPendingReview}">
                                <p class="upload-request-card__hint">Hồ sơ của bạn đang trong trạng thái chờ duyệt.</p>
                            </c:if>
                        </div>
                        <form method="post" action="${pageContext.request.contextPath}/registrant/upload-documents" class="upload-request-card__form">
                            <input type="hidden" name="action" value="requestApproval">
                            <label class="p-input-label" for="request-note">Ghi chú gửi ban quản lý (tùy chọn)</label>
                            <textarea id="request-note" name="requestNote" class="upload-card__reason" rows="2"
                                      placeholder="Ví dụ: Đã bổ sung giấy khám sức khỏe mới, nhờ ban quản lý xem xét."></textarea>
                            <button type="submit" class="welcome-banner__btn welcome-banner__btn--primary"
                                    ${canRequestApproval ? '' : 'disabled'}>
                                Gửi yêu cầu duyệt
                            </button>
                        </form>
                    </section>
                </div>
            </c:otherwise>
        </c:choose>

        <div class="upload-action-bar upload-action-bar--compact">
            <div class="upload-action-bar__info">
                <span class="upload-action-bar__title">Quay lại hồ sơ</span>
                <span class="upload-action-bar__subtitle">Kiểm tra thông tin cá nhân và trạng thái xét duyệt.</span>
            </div>
            <a href="${pageContext.request.contextPath}/registrant/profile" class="welcome-banner__btn welcome-banner__btn--outline upload-action-bar__btn-outline">
                Quay lại hồ sơ
            </a>
        </div>

    </main>

    <jsp:include page="/views/layout/footer.jsp" />
</div>

<script src="${pageContext.request.contextPath}/assets/js/registrant/upload-documents.js" charset="UTF-8"></script>
</body>
</html>

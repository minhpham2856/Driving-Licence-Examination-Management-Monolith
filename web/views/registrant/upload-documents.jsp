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

                <c:if test="${param.success eq 'upload'}">
                    <section class="p-alert-banner p-alert-banner--compact" aria-label="Thông báo thành công">
                        <div class="p-alert-banner__content">
                            <span class="p-alert-banner__title">Tải lên thành công</span>
                            <span>
                                <c:choose>
                                    <c:when test="${profileApproved}">
                                        Hồ sơ bổ sung đã được lưu. Trạng thái hồ sơ vẫn là <strong>Đã duyệt</strong> cho đến khi bạn bấm <strong>Gửi yêu cầu duyệt</strong>.
                                    </c:when>
                                    <c:otherwise>
                                        Tài liệu đã được lưu. Bấm <strong>Gửi yêu cầu duyệt</strong> khi đã đủ hồ sơ.
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
                            <c:choose>
                                <c:when test="${profileApproved}">
                                    <span class="p-alert-banner__title">Đã gửi duyệt hồ sơ bổ sung</span>
                                    <span>Chỉ các tệp tại mục <strong>Hồ sơ khác</strong> được gửi ban quản lý. Trạng thái hồ sơ chính vẫn là <strong>Đã duyệt</strong>.</span>
                                </c:when>
                                <c:otherwise>
                                    <span class="p-alert-banner__title">Đã gửi yêu cầu duyệt</span>
                                    <span>Ban quản lý sẽ kiểm tra và phản hồi trạng thái hồ sơ của bạn.</span>
                                </c:otherwise>
                            </c:choose>
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
                        <c:set var="mandatoryReviewLock" value="${primaryPendingReview}" />
                        <c:if test="${profileApproved}">
                            <section class="p-alert-banner register-exam-notice register-exam-notice--approved" style="margin-bottom:1.25rem;" aria-label="Hướng dẫn bổ sung hồ sơ">
                                <div class="p-alert-banner__content">
                                    <span class="p-alert-banner__title">Hồ sơ đã được duyệt</span>
                                    <span>
                                        <c:choose>
                                            <c:when test="${hasSupplementPendingReview}">
                                                Hồ sơ bổ sung đang chờ ban quản lý xem xét. Trạng thái hồ sơ chính vẫn là <strong>Đã duyệt</strong> - bạn vẫn có thể đăng ký thi hạng A1/A2.
                                            </c:when>
                                            <c:otherwise>
                                                Để bổ sung giấy tờ, hãy dùng mục <strong>Hồ sơ khác</strong> bên dưới.
                                            </c:otherwise>
                                        </c:choose>
                                    </span>
                                </div>
                            </section>
                        </c:if>
                        <c:if test="${profileRejected}">
                            <section class="p-alert-banner" style="margin-bottom:1.25rem;background:#fff7ed;border-color:#fdba74;" aria-label="Hồ sơ bị từ chối">
                                <div class="p-alert-banner__content">
                                    <span class="p-alert-banner__title">Hồ sơ bị từ chối</span>
                                    <span>Vui lòng sửa lại giấy tờ tại 4 mục bên dưới (tải lên thay thế) rồi bấm <strong>Gửi yêu cầu duyệt</strong> lại.</span>
                                </div>
                            </section>
                        </c:if>
                        <div class="upload-page-layout">
                            <%-- 4 loại giấy tờ chuẩn --%>
                            <div class="upload-grid upload-grid--compact">
                                <c:forTokens var="docType" items="Portrait,IdFront,IdBack,HealthCertificate" delims=",">
                                    <c:set var="doc" value="${documentsByType[docType]}" />
                                    <c:set var="inputId" value="file-${docType}" />
                                    <c:set var="docTitle" value="${docType eq 'Portrait' ? '1. Ảnh chân dung 3x4' : docType eq 'IdFront' ? '2. Mặt trước căn cước / CMND' : docType eq 'IdBack' ? '3. Mặt sau căn cước / CMND' : '4. Giấy khám sức khỏe lái xe'}" />

                                    <c:set var="mandatoryApproved" value="${profileApproved and not empty doc.documentUrl}" />
                                    <c:set var="mandatoryReplaceBlocked" value="${mandatoryApproved or mandatoryReviewLock}" />

                                    <div class="upload-card upload-card--${mandatoryApproved ? 'success' : doc.statusClass}${mandatoryApproved ? ' upload-card--mandatory-approved' : ''}">
                                        <div class="upload-card__header">
                                            <h2 class="upload-card__title">${docTitle}</h2>
                                            <span class="r-stat-card__badge r-stat-card__badge--${mandatoryApproved ? 'success' : doc.statusClass}">
                                                ${mandatoryApproved ? 'Đã duyệt' : doc.statusLabel}
                                            </span>
                                        </div>

                                        <c:if test="${mandatoryApproved}">
                                            <p class="upload-card__locked-hint upload-card__locked-hint--approved">Giấy tờ đã duyệt - không thể thay thế trực tiếp. Bổ sung qua mục <strong>Hồ sơ khác</strong>.</p>
                                        </c:if>
                                        <c:if test="${mandatoryReviewLock and not empty doc.documentUrl}">
                                            <p class="upload-card__locked-hint">Hồ sơ đang chờ duyệt - không thể thay đổi giấy tờ lúc này.</p>
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
                                                <c:if test="${not empty doc.documentUrl and doc.documentId gt 0 and not mandatoryReplaceBlocked}">
                                                    <button type="submit"
                                                            form="delete-doc-${docType}"
                                                            class="upload-slot__delete"
                                                            title="Xóa tệp đã tải"
                                                            aria-label="Xóa tệp ${docTitle}"
                                                            onclick="return confirm('Xóa tệp đã tải cho ${docTitle}?');">×</button>
                                                </c:if>

                                                <label class="upload-slot${mandatoryReplaceBlocked ? ' upload-slot--locked' : ''}${mandatoryApproved ? ' upload-slot--locked-approved' : ''}" for="${mandatoryReplaceBlocked ? '' : inputId}" data-upload-slot data-has-file="${not empty doc.documentUrl ? '1' : '0'}">
                                                    <c:choose>
                                                        <c:when test="${not empty doc.documentUrl}">
                                                            <div class="upload-slot__face upload-slot__face--ready" data-slot-ready>
                                                                <img src="${doc.documentUrl}" alt="" class="upload-slot__preview"
                                                                     onerror="this.hidden=true;this.nextElementSibling.hidden=false;">
                                                                <p class="upload-slot__message upload-slot__message--missing" hidden>
                                                                    <strong>Tệp không còn trên máy chủ</strong> (thường do redeploy). Vui lòng tải lên lại.
                                                                </p>
                                                                <p class="upload-slot__message">
                                                                    <c:choose>
                                                                        <c:when test="${mandatoryApproved}">
                                                                            <strong>Đã được phê duyệt</strong><c:if test="${not empty doc.fileSizeLabel}"> · ${doc.fileSizeLabel}</c:if>
                                                                        </c:when>
                                                                        <c:when test="${mandatoryReviewLock}">
                                                                            <strong>Đã tải lên</strong><c:if test="${not empty doc.fileSizeLabel}"> · ${doc.fileSizeLabel}</c:if> · Chờ ban quản lý duyệt
                                                                        </c:when>
                                                                        <c:otherwise>
                                                                            <strong>Đã tải lên</strong><c:if test="${not empty doc.fileSizeLabel}"> · ${doc.fileSizeLabel}</c:if> · Bấm để thay tệp
                                                                        </c:otherwise>
                                                                    </c:choose>
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
                                                           ${mandatoryReplaceBlocked ? 'disabled' : ''}>
                                                </label>
                                            </div>

                                            <c:if test="${not mandatoryReplaceBlocked}">
                                                <div class="upload-card__actions">
                                                    <button type="submit" class="welcome-banner__btn welcome-banner__btn--primary upload-card__submit">Tải lên</button>
                                                </div>
                                            </c:if>
                                        </form>
                                        <c:if test="${not empty doc.documentUrl and doc.documentId gt 0 and not mandatoryReplaceBlocked}">
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

                            <%-- Hồ sơ khác - upload nhiều tệp --%>
                            <section class="upload-other-section">
                                <div class="upload-other-section__header">
                                    <div>
                                        <h2 class="upload-other-section__title">5. Hồ sơ khác</h2>
                                        <p class="upload-other-section__desc">Nộp giấy tờ bổ sung theo từng hạng GPLX (cam kết, xác nhận cư trú, v.v.). Mỗi tệp cần chọn hạng bằng và ghi rõ lý do.</p>
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
                                                        <c:if test="${not empty other.supplementLicenceCode}">
                                                            <span class="upload-other-item__licence">Bổ sung hạng ${other.supplementLicenceCode}</span>
                                                        </c:if>
                                                        <span class="upload-other-item__size">
                                                            <c:choose>
                                                                <c:when test="${not empty other.fileSizeLabel}">${other.fileSizeLabel}</c:when>
                                                                <c:otherwise>-</c:otherwise>
                                                            </c:choose>
                                                            · Tối đa 5MB
                                                        </span>
                                                    </div>
                                                    <p class="upload-other-item__note">
                                                        <c:choose>
                                                            <c:when test="${not empty other.reasonSummary}">${other.reasonSummary}</c:when>
                                                            <c:otherwise>${fn:substringBefore(other.notes, ' · ')}</c:otherwise>
                                                        </c:choose>
                                                    </p>
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

                                    <p class="upload-licence-picker__hint" style="margin-top:0;">
                                        Hồ sơ khác là tùy chọn. Hạng A1 / A / B1 chỉ cần 4 giấy tờ bắt buộc —
                                        chọn hạng khi <strong>Gửi yêu cầu duyệt</strong> bên dưới. Hồ sơ đã duyệt được tái sử dụng khi thi hạng khác nếu không cần đổi.
                                    </p>

                                    <label class="upload-other-form__label" for="other-reason">Lý do / ghi chú <span class="profile-edit-required">*</span></label>
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
                                    <h2 class="upload-request-card__title">
                                        <c:choose>
                                            <c:when test="${profileApproved}">Gửi duyệt hồ sơ bổ sung</c:when>
                                            <c:otherwise>Gửi yêu cầu duyệt hồ sơ</c:otherwise>
                                        </c:choose>
                                    </h2>
                                    <p class="upload-request-card__desc">
                                        <c:choose>
                                            <c:when test="${profileApproved}">
                                                Chọn hạng bằng. Bốn giấy tờ bắt buộc giữ nguyên nếu đã duyệt.
                                                Hồ sơ khác chỉ gửi khi bạn đã thêm tệp mới cần xét.
                                            </c:when>
                                            <c:otherwise>
                                                Chọn hạng bằng bạn gửi duyệt (A1 / A / B1 đều chỉ cần 4 giấy tờ bắt buộc), rồi gửi yêu cầu để ban quản lý phê duyệt.
                                            </c:otherwise>
                                        </c:choose>
                                    </p>
                                    <c:if test="${hasPendingReview}">
                                        <p class="upload-request-card__hint">
                                            <c:choose>
                                                <c:when test="${profileApproved and hasSupplementPendingReview}">
                                                    Hồ sơ bổ sung đang chờ ban quản lý xem xét.
                                                </c:when>
                                                <c:otherwise>
                                                    Hồ sơ của bạn đang trong trạng thái chờ duyệt.
                                                </c:otherwise>
                                            </c:choose>
                                        </p>
                                    </c:if>
                                </div>
                                <form method="post" action="${pageContext.request.contextPath}/registrant/upload-documents" class="upload-request-card__form">
                                    <input type="hidden" name="action" value="requestApproval">

                                    <div class="upload-licence-picker">
                                        <div class="upload-licence-picker__head">
                                            <span class="upload-licence-picker__badge" aria-hidden="true">
                                                <svg width="18" height="18" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                                                <rect x="3" y="5" width="18" height="14" rx="2" stroke="currentColor" stroke-width="2"/>
                                                <path d="M7 9h4M7 13h10" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
                                                </svg>
                                            </span>
                                            <label class="upload-licence-picker__label" for="approval-licence">
                                                Hạng bằng gửi duyệt <span class="profile-edit-required">*</span>
                                            </label>
                                        </div>
                                        <div class="p-input-wrapper upload-licence-picker__field">
                                            <span class="p-input-icon" aria-hidden="true">
                                                <svg width="16" height="16" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                                                <path d="M12 2C8.13 2 5 5.13 5 9c0 5.25 7 13 7 13s7-7.75 7-13c0-3.87-3.13-7-7-7z" stroke="currentColor" stroke-width="2" stroke-linejoin="round"/>
                                                <circle cx="12" cy="9" r="2.5" stroke="currentColor" stroke-width="2"/>
                                                </svg>
                                            </span>
                                            <select id="approval-licence" name="approvalLicenceCode"
                                                    class="p-input-field p-input-field--select upload-licence-picker__select" required>
                                                <option value="" disabled selected>Chọn hạng GPLX...</option>
                                                <c:forEach var="licence" items="${approvalLicenceOptions}">
                                                    <option value="${licence.code}" title="${licence.name}">
                                                        Hạng ${licence.code}<c:if test="${not empty licence.name}"> — ${licence.name}</c:if>
                                                    </option>
                                                </c:forEach>
                                            </select>
                                        </div>
                                        <c:if test="${empty approvalLicenceOptions}">
                                            <p class="upload-licence-picker__hint">
                                                Hiện chưa có hạng bằng để chọn. Vui lòng thử lại sau hoặc liên hệ trung tâm hỗ trợ.
                                            </p>
                                        </c:if>
                                        <p class="upload-licence-picker__hint">Hạng A1 / A / B1 đều chỉ cần 4 giấy tờ bắt buộc. Hồ sơ khác là tùy chọn; hồ sơ đã duyệt có thể tái sử dụng khi thi hạng khác.</p>
                                    </div>

                                    <label class="p-input-label" for="request-note">Ghi chú gửi ban quản lý (tùy chọn)</label>
                                    <textarea id="request-note" name="requestNote" class="upload-card__reason" rows="2"
                                              placeholder="${profileApproved ? 'Ví dụ: Đã bổ sung hồ sơ cho hạng B1, nhờ ban quản lý xem xét.' : 'Ví dụ: Đã đủ giấy tờ hạng A1, nhờ ban quản lý xem xét.'}"></textarea>
                                    <button type="submit" class="welcome-banner__btn welcome-banner__btn--primary"
                                            ${canRequestApproval ? '' : 'disabled'}>
                                        <c:choose>
                                            <c:when test="${profileApproved}">Gửi duyệt hồ sơ bổ sung</c:when>
                                            <c:otherwise>Gửi yêu cầu duyệt</c:otherwise>
                                        </c:choose>
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


        </div>

        <script src="${pageContext.request.contextPath}/assets/js/registrant/upload-documents.js" charset="UTF-8"></script>
    </body>
</html>

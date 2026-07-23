<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<c:set var="ctx" value="${pageContext.request.contextPath}" />
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Duyệt hồ sơ - Lái Vui</title>
    <link rel="stylesheet" href="${ctx}/assets/css/style.css">
    <link rel="stylesheet" href="${ctx}/assets/css/layout.css">
</head>
<body class="has-side-nav-bar dossier-review-page">
<jsp:include page="/views/layout/sidebar-managingstaff.jsp">
    <jsp:param name="activeSidebar" value="duyet-ho-so" />
</jsp:include>
<div class="dashboard-shell">
<main class="main-content">
    <nav class="breadcrumbs">
        <a href="${ctx}/manager/dashboard">Dashboard</a>
        <span class="breadcrumbs__separator">/</span>
        <span class="breadcrumbs__current">Duyệt hồ sơ</span>
    </nav>
    <c:if test="${not empty sessionScope.reviewSuccess}">
        <div class="p-alert-banner" style="border-color:#10b981;color:#047857">${sessionScope.reviewSuccess}</div>
        <c:remove var="reviewSuccess" scope="session" />
    </c:if>
    <c:if test="${not empty sessionScope.reviewError}">
        <div class="p-alert-banner" style="border-color:#ef4444;color:#991b1b">${sessionScope.reviewError}</div>
        <c:remove var="reviewError" scope="session" />
    </c:if>

    <c:choose>
        <c:when test="${not empty dossier}">
            <header class="page-header">
                <div class="page-title-wrap">
                    <h1 class="page-title"><c:out value="${dossier.profile.fullName}" /></h1>
                    <p class="page-subtitle">CCCD: <c:out value="${dossier.profile.govIdNo}" /> · Hạng GPLX: <strong><c:out value="${empty dossier.licenceDisplayClass ? 'Chưa xác định' : dossier.licenceDisplayClass}" /></strong></p>
                </div>
                <div class="page-actions" style="display:flex;gap:.75rem">
                    <a class="btn-export" href="${ctx}/manager/dossier-detail?registrationId=${dossier.registrationId}"
                       style="display:inline-flex;text-decoration:none">Xem chi tiết</a>
                    <a class="btn-export" href="${ctx}/manager/dossiers?page=${currentPage}"
                       style="display:inline-flex;text-decoration:none">Quay lại danh sách</a>
                </div>
            </header>
            <c:set var="defaultDocument" value="${dossier.documents['PORTRAIT']}" />
            <c:set var="defaultDocumentLabel" value="Ảnh chân dung 3x4" />
            <c:if test="${empty defaultDocument}">
                <c:set var="defaultDocument" value="${dossier.documents['ID_FRONT']}" />
                <c:set var="defaultDocumentLabel" value="CCCD mặt trước" />
            </c:if>
            <c:if test="${empty defaultDocument}">
                <c:set var="defaultDocument" value="${dossier.documents['ID_BACK']}" />
                <c:set var="defaultDocumentLabel" value="CCCD mặt sau" />
            </c:if>
            <c:if test="${empty defaultDocument}">
                <c:set var="defaultDocument" value="${dossier.documents['HEALTH_CERTIFICATE']}" />
                <c:set var="defaultDocumentLabel" value="Giấy khám sức khỏe" />
            </c:if>
            <div class="profile-grid dossier-review-grid">
                <section class="profile-main-content">
                    <div class="log-card dossier-documents-card">
                        <div class="log-card-header"><h2 class="log-card-title">Tài liệu đã nộp (${dossier.documentCount}/${dossier.requiredDocumentTotal})</h2></div>
                        <div class="report-grid dossier-document-grid">
                            <c:set var="documentTypes" value="PORTRAIT,ID_FRONT,ID_BACK,HEALTH_CERTIFICATE" />
                            <c:forTokens var="type" items="${documentTypes}" delims=",">
                                <c:set var="document" value="${dossier.documents[type]}" />
                                <c:set var="documentLabel">
                                    <c:choose>
                                        <c:when test="${type eq 'PORTRAIT'}">Ảnh chân dung 3x4</c:when>
                                        <c:when test="${type eq 'ID_FRONT'}">CCCD mặt trước</c:when>
                                        <c:when test="${type eq 'ID_BACK'}">CCCD mặt sau</c:when>
                                        <c:otherwise>Giấy khám sức khỏe</c:otherwise>
                                    </c:choose>
                                </c:set>
                                <div class="profile-score-card dossier-document-card">
                                    <strong><c:out value="${documentLabel}" /></strong>
                                    <c:choose>
                                        <c:when test="${not empty document}">
                                            <span class="action-badge action-badge--success">Đã tải lên</span>
                                            <a class="btn-export js-document-preview"
                                               href="${ctx}/manager/document-view?id=${document.documentId}"
                                               data-document-label="${documentLabel}"
                                               style="display:inline-flex;text-decoration:none;margin-top:auto">Xem bên phải</a>
                                        </c:when>
                                        <c:otherwise>
                                            <span class="action-badge action-badge--warning">Còn thiếu</span>
                                        </c:otherwise>
                                    </c:choose>
                                </div>
                            </c:forTokens>
                            <c:if test="${dossier.graduationCertificateRequired}">
                                <c:set var="document" value="${dossier.documents['GRADUATION_CERTIFICATE']}" />
                                <div class="profile-score-card dossier-document-card">
                                    <strong>Giấy tốt nghiệp / chứng chỉ đào tạo</strong>
                                    <c:choose>
                                        <c:when test="${not empty document}">
                                            <span class="action-badge action-badge--success">Đã tải lên</span>
                                            <a class="btn-export js-document-preview"
                                               href="${ctx}/manager/document-view?id=${document.documentId}"
                                               data-document-label="Giấy tốt nghiệp / chứng chỉ đào tạo"
                                               style="display:inline-flex;text-decoration:none;margin-top:auto">Xem bên phải</a>
                                        </c:when>
                                        <c:otherwise>
                                            <span class="action-badge action-badge--warning">Còn thiếu</span>
                                        </c:otherwise>
                                    </c:choose>
                                </div>
                            </c:if>
                        </div>
                    </div>
                    <section class="log-card dossier-decision-card dossier-decision-panel" aria-labelledby="dossierDecisionTitle">
                        <div class="log-card-header"><h2 class="log-card-title" id="dossierDecisionTitle">Duyệt hồ sơ</h2></div>
                        <div class="dossier-decision-card__body">
                            <div class="report-pane dossier-licence-summary">
                                <span>Hạng GPLX đề nghị duyệt</span>
                                <strong><c:out value="${empty dossier.licenceDisplayClass ? 'Chưa xác định' : dossier.licenceDisplayClass}" /></strong>
                            </div>
                            <c:if test="${not dossier.complete}">
                                <div class="p-alert-banner dossier-review-warning">
                                    Hồ sơ cần đủ ${dossier.requiredDocumentTotal} giấy tờ mới có thể duyệt.
                                </div>
                            </c:if>
                            <form action="${ctx}/manager/dossiers" method="post" class="dossier-decision-form">
                                <input type="hidden" name="id" value="${dossier.registrationId}">
                                <input type="hidden" name="returnPage" value="${currentPage}">
                                <div class="dossier-decision-choices">
                                    <label class="dossier-decision-option">
                                        <input type="radio" name="decision" value="approve"
                                               ${dossier.complete and dossier.motorcycleLicence ? 'checked' : 'disabled'}>
                                        <span><strong>Chấp nhận</strong><small>Duyệt hồ sơ và hạng GPLX</small></span>
                                    </label>
                                    <label class="dossier-decision-option dossier-decision-option--reject">
                                        <input type="radio" name="decision" value="reject"
                                               ${not dossier.complete ? 'checked' : ''}>
                                        <span><strong>Từ chối</strong><small>Yêu cầu thí sinh hoàn thiện lại</small></span>
                                    </label>
                                </div>
                                <label class="input-label" for="reason">Lý do/Ghi chú</label>
                                <textarea class="input-field" id="reason" name="reason" rows="2"
                                          placeholder="Bắt buộc khi từ chối hồ sơ; nội dung này sẽ được gửi qua email."></textarea>
                                <button class="btn-filter" type="submit">Xác nhận</button>
                            </form>
                        </div>
                    </section>
                </section>
                <aside class="profile-sidebar dossier-review-workspace">
                    <div class="profile-sidebar-card dossier-review-workspace__card">
                        <section class="dossier-preview-panel" aria-labelledby="documentPreviewLabel">
                            <h3 id="documentPreviewLabel"><c:out value="${empty defaultDocument ? 'Xem trước tài liệu' : defaultDocumentLabel}" /></h3>
                            <c:choose>
                                <c:when test="${not empty defaultDocument}">
                                    <iframe id="dossierDocumentPreview"
                                            src="${ctx}/manager/document-view?id=${defaultDocument.documentId}"
                                            title="Xem trước tài liệu hồ sơ"></iframe>
                                </c:when>
                                <c:otherwise>
                                    <p class="dossier-preview-empty">Hồ sơ chưa có tài liệu để xem trước.</p>
                                </c:otherwise>
                            </c:choose>
                        </section>
                    </div>
                </aside>
            </div>
        </c:when>
        <c:otherwise>
            <header class="page-header">
                <div class="page-title-wrap">
                    <h1 class="page-title">Hồ sơ chờ duyệt</h1>
                </div>
            </header>
            <section class="log-card">
                <div class="table-responsive">
                    <table class="audit-table">
                        <thead><tr><th>Mã</th><th>Họ tên</th><th>CCCD</th><th>Hạng GPLX</th><th>Tài liệu</th><th>Trạng thái</th><th></th></tr></thead>
                        <tbody>
                        <c:forEach var="item" items="${dossiers}">
                            <tr>
                                <td>#${item.registrationId}</td>
                                <td><strong><c:out value="${item.profile.fullName}" /></strong></td>
                                <td><c:out value="${item.profile.govIdNo}" /></td>
                                <td><strong><c:out value="${empty item.licenceDisplayClass ? 'Chưa xác định' : item.licenceDisplayClass}" /></strong></td>
                                <td>${item.documentCount}/${item.requiredDocumentTotal}</td>
                                <td><span class="action-badge action-badge--${item.statusKey}">${item.statusLabel}</span></td>
                                <td>
                                    <div style="display:flex;gap:.5rem;justify-content:flex-end">
                                        <a class="btn-export" href="${ctx}/manager/dossier-detail?registrationId=${item.registrationId}"
                                           style="display:inline-flex;text-decoration:none">Chi tiết</a>
                                        <a class="btn-filter" href="${ctx}/manager/dossiers?id=${item.registrationId}&amp;page=${currentPage}"
                                           style="display:inline-flex;text-decoration:none">Thẩm định</a>
                                    </div>
                                </td>
                            </tr>
                        </c:forEach>
                        <c:if test="${empty dossiers}">
                            <tr><td colspan="7" style="text-align:center;padding:2rem">Không có hồ sơ chờ duyệt.</td></tr>
                        </c:if>
                        </tbody>
                    </table>
                </div>
                <footer class="pagination-footer">
                    <nav class="pagination-nav" aria-label="Phân trang duyệt hồ sơ">
                        <c:choose>
                            <c:when test="${currentPage gt 1}">
                                <a class="page-btn page-btn--wide" href="${ctx}/manager/dossiers?page=${currentPage - 1}">Trước</a>
                            </c:when>
                            <c:otherwise><span class="page-btn page-btn--wide disabled">Trước</span></c:otherwise>
                        </c:choose>
                        <c:forEach var="pageNumber" begin="${pageStart}" end="${pageEnd}">
                            <a class="page-btn ${pageNumber eq currentPage ? 'active' : ''}"
                               href="${ctx}/manager/dossiers?page=${pageNumber}">${pageNumber}</a>
                        </c:forEach>
                        <c:choose>
                            <c:when test="${currentPage lt totalPages}">
                                <a class="page-btn page-btn--wide" href="${ctx}/manager/dossiers?page=${currentPage + 1}">Sau</a>
                            </c:when>
                            <c:otherwise><span class="page-btn page-btn--wide disabled">Sau</span></c:otherwise>
                        </c:choose>
                    </nav>
                </footer>
            </section>
        </c:otherwise>
    </c:choose>
</main>
<jsp:include page="/views/layout/footer.jsp" />
</div>
<script>
    document.querySelectorAll('.js-document-preview').forEach(function (link) {
        link.addEventListener('click', function (event) {
            const preview = document.getElementById('dossierDocumentPreview');
            if (!preview) return;
            event.preventDefault();
            preview.src = link.href;
            document.getElementById('documentPreviewLabel').textContent =
                    link.dataset.documentLabel || 'Xem trước tài liệu';
        });
    });
</script>
</body>
</html>


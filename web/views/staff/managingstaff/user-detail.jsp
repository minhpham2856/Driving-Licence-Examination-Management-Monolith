<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<c:set var="ctx" value="${pageContext.request.contextPath}" />
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Chi tiết hồ sơ học viên - Lái Vui</title>
    <link rel="stylesheet" href="${ctx}/assets/css/style.css">
    <link rel="stylesheet" href="${ctx}/assets/css/layout.css">
    <style>
        .dossier-status-nav {
            display: grid;
            grid-template-columns: repeat(7, minmax(0, 1fr));
            gap: .65rem;
            margin: 0 0 1.25rem;
        }
        .dossier-status-nav a {
            justify-content: center;
            min-width: 0;
            padding-inline: .6rem;
            text-decoration: none;
            white-space: nowrap;
        }
        .dossier-detail-workspace {
            display: grid;
            grid-template-columns: minmax(230px, .65fr) minmax(275px, .8fr) minmax(420px, 1.55fr);
            gap: 1rem;
            align-items: start;
            margin-top: 1rem;
        }
        .dossier-detail-card {
            min-width: 0;
            background: #fff;
            border: 1px solid #dbe3ef;
            border-radius: 14px;
            padding: 1rem;
        }
        .dossier-detail-summary {
            text-align: center;
        }
        .dossier-detail-summary .profile-avatar-large {
            width: 64px;
            height: 64px;
            margin: 0 auto .65rem;
            font-size: 1.5rem;
        }
        .dossier-detail-summary .profile-name {
            margin-bottom: .45rem;
            font-size: 1.1rem;
        }
        .dossier-detail-summary .profile-quick-info {
            margin-top: 1rem;
        }
        .dossier-detail-summary .quick-info-item {
            gap: .6rem;
            padding: .48rem 0;
        }
        .dossier-detail-summary .quick-info-value {
            max-width: 62%;
            overflow-wrap: anywhere;
            text-align: right;
        }
        .dossier-detail-result {
            margin-top: .85rem;
            padding-top: .85rem;
            border-top: 1px solid #e2e8f0;
            text-align: left;
        }
        .dossier-detail-result strong,
        .dossier-detail-result span {
            display: block;
        }
        .dossier-detail-result span {
            margin-top: .25rem;
            color: #64748b;
            font-size: .85rem;
            line-height: 1.4;
        }
        .dossier-document-list {
            display: grid;
            gap: .65rem;
            margin-top: .85rem;
        }
        .dossier-document-item {
            display: grid;
            grid-template-columns: minmax(0, 1fr) auto;
            gap: .65rem;
            align-items: center;
            padding: .72rem;
            border: 1px solid #e2e8f0;
            border-radius: 10px;
            background: #f8fafc;
        }
        .dossier-document-item__name {
            display: block;
            color: #0f172a;
            font-size: .88rem;
            line-height: 1.3;
        }
        .dossier-document-item .action-badge {
            margin-top: .3rem;
        }
        .dossier-document-link {
            min-width: 74px;
            justify-content: center;
            padding: .45rem .6rem;
            text-decoration: none;
        }
        .dossier-document-link.is-active {
            border-color: #075fd8;
            background: #075fd8;
            color: #fff;
            box-shadow: 0 0 0 3px rgba(7, 95, 216, .12);
        }
        .dossier-preview-heading {
            display: flex;
            justify-content: space-between;
            gap: 1rem;
            align-items: center;
            margin-bottom: .75rem;
        }
        .dossier-preview-heading h2 {
            margin: 0;
        }
        .dossier-preview-frame {
            display: block;
            width: 100%;
            height: min(61vh, 570px);
            min-height: 390px;
            border: 1px solid #cbd5e1;
            border-radius: 10px;
            background: #f8fafc;
        }
        .dossier-preview-empty {
            display: grid;
            min-height: 390px;
            place-items: center;
            border: 1px dashed #cbd5e1;
            border-radius: 10px;
            color: #64748b;
            text-align: center;
        }
        @media (max-width: 1100px) {
            .dossier-status-nav { grid-template-columns: repeat(4, minmax(0, 1fr)); }
            .dossier-detail-workspace { grid-template-columns: minmax(230px, .7fr) minmax(0, 1.3fr); }
            .dossier-detail-preview { grid-column: 1 / -1; }
        }
        @media (max-width: 680px) {
            .dossier-status-nav { grid-template-columns: repeat(2, minmax(0, 1fr)); }
            .dossier-detail-workspace { grid-template-columns: 1fr; }
            .dossier-detail-preview { grid-column: auto; }
            .dossier-preview-frame { height: 430px; min-height: 0; }
        }
    </style>
</head>
<body class="has-side-nav-bar">
<jsp:include page="/views/layout/sidebar-managingstaff.jsp">
    <jsp:param name="activeSidebar" value="chi-tiet-hoc-vien" />
</jsp:include>

<div class="dashboard-shell">
<main class="main-content">
    <nav class="breadcrumbs">
        <a href="${ctx}/manager/dashboard">Dashboard</a>
        <span class="breadcrumbs__separator">/</span>
        <a href="${ctx}/manager/registrants">Danh sách học viên</a>
        <span class="breadcrumbs__separator">/</span>
        <span class="breadcrumbs__current">Chi tiết hồ sơ</span>
    </nav>

    <c:if test="${not empty sessionScope.reminderSuccess}">
        <div class="p-alert-banner" style="border-color:#10b981;color:#047857">
            <c:out value="${sessionScope.reminderSuccess}" />
        </div>
        <c:remove var="reminderSuccess" scope="session" />
    </c:if>
    <c:if test="${not empty sessionScope.reminderError}">
        <div class="p-alert-banner" style="border-color:#ef4444;color:#991b1b">
            <c:out value="${sessionScope.reminderError}" />
        </div>
        <c:remove var="reminderError" scope="session" />
    </c:if>

    <c:choose>
        <c:when test="${listMode}">
            <header class="page-header">
                <div class="page-title-wrap">
                    <h1 class="page-title">Quản lý hồ sơ thí sinh</h1>
                    <p class="page-subtitle">Dữ liệu được phân trang trực tiếp tại database, 15 thí sinh mỗi trang.</p>
                </div>
                <span class="action-badge action-badge--info" style="font-weight:700">
                    ${totalFiltered} thí sinh
                </span>
            </header>

            <nav class="dossier-status-nav" aria-label="Lọc trạng thái hồ sơ">
                <a class="${statusFilter eq 'all' ? 'btn-filter' : 'btn-export'}"
                   href="${ctx}/manager/dossier-detail?status=all" style="text-decoration:none">Tất cả (${statusCounts.all})</a>
                <a class="${statusFilter eq 'pending' ? 'btn-filter' : 'btn-export'}"
                   href="${ctx}/manager/dossier-detail?status=pending" style="text-decoration:none">Chờ duyệt (${statusCounts.pending})</a>
                <a class="${statusFilter eq 'approved' ? 'btn-filter' : 'btn-export'}"
                   href="${ctx}/manager/dossier-detail?status=approved" style="text-decoration:none">Đã duyệt (${statusCounts.approved})</a>
                <a class="${statusFilter eq 'rejected' ? 'btn-filter' : 'btn-export'}"
                   href="${ctx}/manager/dossier-detail?status=rejected" style="text-decoration:none">Đã từ chối (${statusCounts.rejected})</a>
                <a class="${statusFilter eq 'present' ? 'btn-filter' : 'btn-export'}"
                   href="${ctx}/manager/dossier-detail?status=present" style="text-decoration:none">Đang thi (${statusCounts.present})</a>
                <a class="${statusFilter eq 'completed' ? 'btn-filter' : 'btn-export'}"
                   href="${ctx}/manager/dossier-detail?status=completed" style="text-decoration:none">Đã thi xong (${statusCounts.completed})</a>
            </nav>

            <section class="log-card">
                <div class="table-responsive">
                    <table class="audit-table">
                        <thead>
                            <tr>
                                <th>Mã tài khoản</th>
                                <th>Họ và tên</th>
                                <th>CCCD</th>
                                <th>Liên hệ</th>
                                <th>Giấy tờ</th>
                                <th>Trạng thái hồ sơ</th>
                                <th style="text-align:center;min-width:292px">Thao tác</th>
                            </tr>
                        </thead>
                        <tbody>
                            <c:forEach var="item" items="${dossiers}">
                                <tr>
                                    <td>#${item.user.id}<br><small>@<c:out value="${item.user.username}" /></small></td>
                                    <td><strong><c:out value="${item.profile.fullName}" /></strong></td>
                                    <td><c:out value="${item.profile.govIdNo}" /></td>
                                    <td>
                                        <c:out value="${item.profile.phoneNo}" /><br>
                                        <small><c:out value="${item.user.email}" /></small>
                                    </td>
                                    <td>${item.documentCount}/${item.requiredDocumentTotal}</td>
                                    <td>
                                        <span class="action-badge action-badge--${item.statusKey}">
                                            ${item.statusLabel}
                                        </span>
                                    </td>
                                    <td style="text-align:center;vertical-align:middle;white-space:nowrap">
                                        <div style="display:grid;grid-template-columns:1fr 1fr 1fr;align-items:center;justify-items:center;gap:.25rem;width:292px;margin:0 auto">
                                            <a class="btn-export"
                                               href="${ctx}/manager/dossier-detail?id=${item.user.id}"
                                               style="grid-column:1;justify-self:start;padding:.4rem .5rem;text-decoration:none">Xem chi tiết</a>
                                            <c:if test="${item.pendingReview}">
                                                <a class="btn-filter"
                                                   href="${ctx}/manager/dossiers?id=${item.registrationId}&amp;page=${currentPage}"
                                                   style="grid-column:2;padding:.4rem .5rem;text-decoration:none">Duyệt hồ sơ</a>
                                            </c:if>
                                            <c:if test="${item.reminderEligible}">
                                                <form action="${ctx}/manager/dossiers/remind" method="post" style="grid-column:3;justify-self:end;margin:0"
                                                      onsubmit="return confirm('Gửi email nhắc hoàn thiện hồ sơ này?');">
                                                    <input type="hidden" name="id" value="${item.registrationId}">
                                                    <input type="hidden" name="returnTo" value="list">
                                                    <input type="hidden" name="returnStatus" value="${statusFilter}">
                                                    <input type="hidden" name="returnPage" value="${currentPage}">
                                                    <button class="btn-export" type="submit" style="padding:.4rem .5rem">Gửi email nhắc</button>
                                                </form>
                                            </c:if>
                                        </div>
                                    </td>
                                </tr>
                            </c:forEach>
                            <c:if test="${empty dossiers}">
                                <tr>
                                    <td colspan="7" style="padding:3rem;text-align:center;color:#64748b">
                                        Chưa có tài khoản Registrant trong database.
                                    </td>
                                </tr>
                            </c:if>
                        </tbody>
                    </table>
                </div>
                <footer class="pagination-footer">
                    <div class="pagination-info">
                        Hiển thị ${firstItem} - ${lastItem} trong tổng số ${totalFiltered} hồ sơ · 15 người/trang
                    </div>
                    <nav class="pagination-nav" aria-label="Phân trang hồ sơ thí sinh">
                        <c:choose>
                            <c:when test="${currentPage gt 1}">
                                <a class="page-btn page-btn--wide"
                                   href="${ctx}/manager/dossier-detail?status=${statusFilter}&amp;page=${currentPage - 1}">Trước</a>
                            </c:when>
                            <c:otherwise><span class="page-btn page-btn--wide disabled">Trước</span></c:otherwise>
                        </c:choose>
                        <c:forEach var="pageNumber" begin="${pageStart}" end="${pageEnd}">
                            <a class="page-btn ${pageNumber eq currentPage ? 'active' : ''}"
                               href="${ctx}/manager/dossier-detail?status=${statusFilter}&amp;page=${pageNumber}">${pageNumber}</a>
                        </c:forEach>
                        <c:choose>
                            <c:when test="${currentPage lt totalPages}">
                                <a class="page-btn page-btn--wide"
                                   href="${ctx}/manager/dossier-detail?status=${statusFilter}&amp;page=${currentPage + 1}">Sau</a>
                            </c:when>
                            <c:otherwise><span class="page-btn page-btn--wide disabled">Sau</span></c:otherwise>
                        </c:choose>
                    </nav>
                </footer>
            </section>
        </c:when>
        <c:when test="${not empty dossier}">
            <header class="page-header">
                <div class="page-title-wrap">
                    <h1 class="page-title"><c:out value="${dossier.profile.fullName}" /></h1>
                    <p class="page-subtitle">
                        Tài khoản @<c:out value="${dossier.user.username}" />
                        · Hồ sơ #${dossier.registrationId}
                    </p>
                </div>
                <div class="page-actions" style="display:flex;gap:.75rem">
                    <a class="btn-export" href="${ctx}/manager/dossier-detail"
                       style="display:inline-flex;text-decoration:none">Quay lại</a>
                    <c:if test="${dossier.pendingReview}">
                        <a class="btn-filter" href="${ctx}/manager/dossiers?id=${dossier.registrationId}"
                           style="display:inline-flex;text-decoration:none">Duyệt hồ sơ</a>
                    </c:if>
                    <c:if test="${dossier.reminderEligible}">
                        <form action="${ctx}/manager/dossiers/remind" method="post" style="margin:0"
                              onsubmit="return confirm('Gửi email nhắc hoàn thiện hồ sơ này?');">
                            <input type="hidden" name="id" value="${dossier.registrationId}">
                            <input type="hidden" name="returnTo" value="detail">
                            <button class="btn-export" type="submit">Gửi email nhắc</button>
                        </form>
                    </c:if>
                </div>
            </header>

            <c:set var="defaultDocument" value="${dossier.documents['PORTRAIT']}" />
            <c:set var="defaultDocumentLabel" value="Ảnh chân dung 3x4" />
            <c:if test="${empty defaultDocument}"><c:set var="defaultDocument" value="${dossier.documents['ID_FRONT']}" /><c:set var="defaultDocumentLabel" value="CCCD mặt trước" /></c:if>
            <c:if test="${empty defaultDocument}"><c:set var="defaultDocument" value="${dossier.documents['ID_BACK']}" /><c:set var="defaultDocumentLabel" value="CCCD mặt sau" /></c:if>
            <c:if test="${empty defaultDocument}"><c:set var="defaultDocument" value="${dossier.documents['HEALTH_CERTIFICATE']}" /><c:set var="defaultDocumentLabel" value="Giấy khám sức khỏe" /></c:if>
            <c:if test="${empty defaultDocument}"><c:set var="defaultDocument" value="${dossier.documents['GRADUATION_CERTIFICATE']}" /><c:set var="defaultDocumentLabel" value="Giấy tốt nghiệp / chứng chỉ đào tạo" /></c:if>

            <div class="dossier-detail-workspace">
                <aside class="dossier-detail-card dossier-detail-summary">
                    <div class="profile-avatar-large profile-avatar--blue">${fn:substring(dossier.profile.fullName, 0, 1)}</div>
                    <h2 class="profile-name"><c:out value="${dossier.profile.fullName}" /></h2>
                    <span class="action-badge action-badge--${dossier.statusKey}">${dossier.statusLabel}</span>
                    <div class="profile-quick-info">
                        <div class="quick-info-item"><span class="quick-info-label">CCCD</span><span class="quick-info-value"><c:out value="${dossier.profile.govIdNo}" /></span></div>
                        <div class="quick-info-item"><span class="quick-info-label">Ngày sinh</span><span class="quick-info-value"><fmt:formatDate value="${dossier.profile.dateOfBirth}" pattern="dd/MM/yyyy" /></span></div>
                        <div class="quick-info-item"><span class="quick-info-label">Giới tính</span><span class="quick-info-value"><c:out value="${dossier.profile.sex}" /></span></div>
                        <div class="quick-info-item"><span class="quick-info-label">Điện thoại</span><span class="quick-info-value"><c:out value="${dossier.profile.phoneNo}" /></span></div>
                        <div class="quick-info-item"><span class="quick-info-label">Email</span><span class="quick-info-value"><c:out value="${dossier.user.email}" /></span></div>
                        <div class="quick-info-item"><span class="quick-info-label">Địa chỉ</span><span class="quick-info-value"><c:out value="${dossier.profile.address}" /></span></div>
                        <div class="quick-info-item"><span class="quick-info-label">Tài khoản</span><span class="quick-info-value">${dossier.user.active ? 'Hoạt động' : 'Đã khóa'}</span></div>
                    </div>
                    <div class="dossier-detail-result">
                        <strong>Kết quả xử lý</strong>
                        <span><c:out value="${empty dossier.reviewMessage ? 'Chưa có ghi chú' : dossier.reviewMessage}" /></span>
                    </div>
                </aside>

                <section class="dossier-detail-card">
                    <div style="display:flex;justify-content:space-between;gap:.75rem;align-items:center">
                        <h2 class="log-card-title">Giấy tờ (${dossier.documentCount}/${dossier.requiredDocumentTotal})</h2>
                        <span class="action-badge action-badge--${dossier.complete ? 'success' : 'warning'}">${dossier.complete ? 'Đã đủ' : 'Còn thiếu'}</span>
                    </div>
                    <div class="dossier-document-list">
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
                            <div class="dossier-document-item">
                                <div><strong class="dossier-document-item__name"><c:out value="${documentLabel}" /></strong><span class="action-badge action-badge--${not empty document ? 'success' : 'warning'}">${not empty document ? 'Đã tải lên' : 'Còn thiếu'}</span></div>
                                <c:if test="${not empty document}"><a class="btn-export dossier-document-link js-dossier-document-link ${defaultDocument.documentId eq document.documentId ? 'is-active' : ''}" href="${ctx}/manager/document-view?id=${document.documentId}" data-document-label="${fn:escapeXml(documentLabel)}">Xem</a></c:if>
                            </div>
                        </c:forTokens>
                        <c:if test="${dossier.graduationCertificateRequired}">
                            <c:set var="document" value="${dossier.documents['GRADUATION_CERTIFICATE']}" />
                            <div class="dossier-document-item">
                                <div><strong class="dossier-document-item__name">Giấy tốt nghiệp / chứng chỉ đào tạo</strong><span class="action-badge action-badge--${not empty document ? 'success' : 'warning'}">${not empty document ? 'Đã tải lên' : 'Còn thiếu'}</span></div>
                                <c:if test="${not empty document}"><a class="btn-export dossier-document-link js-dossier-document-link ${defaultDocument.documentId eq document.documentId ? 'is-active' : ''}" href="${ctx}/manager/document-view?id=${document.documentId}" data-document-label="Giấy tốt nghiệp / chứng chỉ đào tạo">Xem</a></c:if>
                            </div>
                        </c:if>
                    </div>
                </section>

                <section class="dossier-detail-card dossier-detail-preview">
                    <div class="dossier-preview-heading">
                        <h2 id="dossier-preview-title" class="log-card-title"><c:out value="${empty defaultDocument ? 'Xem tài liệu' : defaultDocumentLabel}" /></h2>
                        <a id="dossier-preview-open" class="btn-export" target="_blank" rel="noopener" href="${ctx}/manager/document-view?id=${defaultDocument.documentId}" ${empty defaultDocument ? 'hidden' : ''} style="text-decoration:none">Mở riêng</a>
                    </div>
                    <c:choose>
                        <c:when test="${not empty defaultDocument}"><iframe id="dossier-document-frame" class="dossier-preview-frame" src="${ctx}/manager/document-view?id=${defaultDocument.documentId}" title="${fn:escapeXml(defaultDocumentLabel)}"></iframe></c:when>
                        <c:otherwise><div class="dossier-preview-empty">Hồ sơ chưa có tài liệu để xem.</div></c:otherwise>
                    </c:choose>
                </section>
            </div>
        </c:when>
        <c:otherwise>
            <div class="report-pane" style="padding:4rem 1.5rem;text-align:center;margin-top:1.5rem">
                Không tìm thấy hồ sơ thí sinh.
            </div>
        </c:otherwise>
    </c:choose>
</main>
<jsp:include page="/views/layout/footer.jsp">
    <jsp:param name="standalone" value="false" />
</jsp:include>
</div>
<script>
    document.addEventListener('DOMContentLoaded', function () {
        const links = Array.from(document.querySelectorAll('.js-dossier-document-link'));
        const frame = document.getElementById('dossier-document-frame');
        const title = document.getElementById('dossier-preview-title');
        const openLink = document.getElementById('dossier-preview-open');
        if (!frame || !title || !openLink) return;
        links.forEach(function (link) {
            link.addEventListener('click', function (event) {
                event.preventDefault();
                links.forEach(function (item) { item.classList.remove('is-active'); });
                link.classList.add('is-active');
                const label = link.dataset.documentLabel || 'Xem tài liệu';
                frame.src = link.href;
                frame.title = label;
                title.textContent = label;
                openLink.href = link.href;
                openLink.hidden = false;
            });
        });
    });
</script>
</body>
</html>


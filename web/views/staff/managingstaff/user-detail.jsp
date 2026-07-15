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
        @media (max-width: 1100px) {
            .dossier-status-nav { grid-template-columns: repeat(4, minmax(0, 1fr)); }
        }
        @media (max-width: 680px) {
            .dossier-status-nav { grid-template-columns: repeat(2, minmax(0, 1fr)); }
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
                <a class="${statusFilter eq 'supplement' ? 'btn-filter' : 'btn-export'}"
                   href="${ctx}/manager/dossier-detail?status=supplement" style="text-decoration:none">Cần bổ sung (${statusCounts.supplement})</a>
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
                                <th>Hạng GPLX</th>
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
                                    <td><c:out value="${empty item.licenceDisplayClass ? 'Chưa chọn' : item.licenceDisplayClass}" /></td>
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
                                    <td colspan="8" style="padding:3rem;text-align:center;color:#64748b">
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
                        · Hạng <c:out value="${empty dossier.licenceDisplayClass ? 'Chưa chọn' : dossier.licenceDisplayClass}" />
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

            <div class="profile-grid">
                <aside class="profile-sidebar">
                    <div class="profile-sidebar-card">
                        <div class="profile-avatar-large profile-avatar--blue"
                             style="margin:auto;background:linear-gradient(135deg,#0052cc,#6366f1)">
                            ${fn:substring(dossier.profile.fullName, 0, 1)}
                        </div>
                        <h2 class="profile-name"><c:out value="${dossier.profile.fullName}" /></h2>
                        <span class="action-badge action-badge--${dossier.statusKey}">${dossier.statusLabel}</span>
                        <div class="profile-quick-info" style="margin-top:1.5rem">
                            <div class="quick-info-item"><span class="quick-info-label">CCCD</span><span class="quick-info-value"><c:out value="${dossier.profile.govIdNo}" /></span></div>
                            <div class="quick-info-item"><span class="quick-info-label">Ngày sinh</span><span class="quick-info-value"><fmt:formatDate value="${dossier.profile.dateOfBirth}" pattern="dd/MM/yyyy" /></span></div>
                            <div class="quick-info-item"><span class="quick-info-label">Giới tính</span><span class="quick-info-value"><c:out value="${dossier.profile.sex}" /></span></div>
                            <div class="quick-info-item"><span class="quick-info-label">Điện thoại</span><span class="quick-info-value"><c:out value="${dossier.profile.phoneNo}" /></span></div>
                            <div class="quick-info-item"><span class="quick-info-label">Email</span><span class="quick-info-value"><c:out value="${dossier.user.email}" /></span></div>
                            <div class="quick-info-item"><span class="quick-info-label">Địa chỉ</span><span class="quick-info-value" style="text-align:right"><c:out value="${dossier.profile.address}" /></span></div>
                            <div class="quick-info-item"><span class="quick-info-label">Tài khoản</span><span class="quick-info-value">${dossier.user.active ? 'Đang hoạt động' : 'Đã khóa'}</span></div>
                        </div>
                    </div>
                </aside>

                <section class="profile-main-content" style="gap:1.5rem">
                    <div class="log-card">
                        <div class="log-card-header">
                            <h2 class="log-card-title">Giấy tờ hồ sơ (${dossier.documentCount}/${dossier.requiredDocumentTotal})</h2>
                            <span class="action-badge action-badge--${dossier.complete ? 'success' : 'warning'}">
                                ${dossier.complete ? 'ĐÃ ĐỦ HỒ SƠ' : 'CHƯA ĐỦ HỒ SƠ'}
                            </span>
                        </div>
                        <div class="report-grid" style="grid-template-columns:repeat(2,minmax(0,1fr));padding:1.5rem;gap:1rem">
                            <c:set var="documentTypes" value="PORTRAIT,ID_FRONT,ID_BACK,HEALTH_CERTIFICATE" />
                            <c:forTokens var="type" items="${documentTypes}" delims=",">
                                <c:set var="document" value="${dossier.documents[type]}" />
                                <div class="profile-score-card" style="align-items:flex-start;min-height:120px">
                                    <strong>
                                        <c:choose>
                                            <c:when test="${type eq 'PORTRAIT'}">Ảnh chân dung 3x4</c:when>
                                            <c:when test="${type eq 'ID_FRONT'}">CCCD mặt trước</c:when>
                                            <c:when test="${type eq 'ID_BACK'}">CCCD mặt sau</c:when>
                                            <c:otherwise>Giấy khám sức khỏe</c:otherwise>
                                        </c:choose>
                                    </strong>
                                    <c:choose>
                                        <c:when test="${not empty document}">
                                            <span class="action-badge action-badge--success">Đã tải lên</span>
                                            <a class="btn-export" target="_blank" rel="noopener"
                                               href="${ctx}/manager/document-view?id=${document.documentId}"
                                               style="display:inline-flex;text-decoration:none;margin-top:auto">Mở tài liệu</a>
                                        </c:when>
                                        <c:otherwise>
                                            <span class="action-badge action-badge--warning">Còn thiếu</span>
                                        </c:otherwise>
                                    </c:choose>
                                </div>
                            </c:forTokens>
                            <c:if test="${dossier.graduationCertificateRequired}">
                                <c:set var="document" value="${dossier.documents['GRADUATION_CERTIFICATE']}" />
                                <div class="profile-score-card" style="align-items:flex-start;min-height:120px">
                                    <strong>Giấy tốt nghiệp / chứng chỉ đào tạo</strong>
                                    <c:choose>
                                        <c:when test="${not empty document}">
                                            <span class="action-badge action-badge--success">Đã tải lên</span>
                                            <a class="btn-export" target="_blank" rel="noopener"
                                               href="${ctx}/manager/document-view?id=${document.documentId}"
                                               style="display:inline-flex;text-decoration:none;margin-top:auto">Mở tài liệu</a>
                                        </c:when>
                                        <c:otherwise>
                                            <span class="action-badge action-badge--warning">Còn thiếu</span>
                                        </c:otherwise>
                                    </c:choose>
                                </div>
                            </c:if>
                        </div>
                    </div>

                    <c:if test="${not empty dossier.documents['APPROVED_DOSSIER_PDF']}">
                        <div class="report-pane" style="padding:1.5rem;border-color:#10b981">
                            <h2 class="log-card-title">PDF hồ sơ đã duyệt</h2>
                            <p>Biểu mẫu hồ sơ, ảnh chân dung và tài liệu căn cước đã được tổng hợp.</p>
                            <a class="btn-filter" target="_blank" rel="noopener"
                               href="${fn:startsWith(dossier.documents['APPROVED_DOSSIER_PDF'].documentUrl, 'http://') or fn:startsWith(dossier.documents['APPROVED_DOSSIER_PDF'].documentUrl, 'https://') ? dossier.documents['APPROVED_DOSSIER_PDF'].documentUrl : ctx}${fn:startsWith(dossier.documents['APPROVED_DOSSIER_PDF'].documentUrl, 'http://') or fn:startsWith(dossier.documents['APPROVED_DOSSIER_PDF'].documentUrl, 'https://') ? '' : dossier.documents['APPROVED_DOSSIER_PDF'].documentUrl}"
                               style="display:inline-flex;text-decoration:none">Mở PDF hồ sơ</a>
                        </div>
                    </c:if>

                    <div class="report-pane" style="padding:1.5rem">
                        <h2 class="log-card-title">Kết quả xử lý hồ sơ</h2>
                        <div style="display:grid;grid-template-columns:180px 1fr;gap:.75rem;margin-top:1rem">
                            <strong>Trạng thái hiện tại</strong><span>${dossier.statusLabel}</span>
                            <strong>Hạng GPLX</strong><span><c:out value="${empty dossier.licenceDisplayClass ? 'Chưa chọn' : dossier.licenceDisplayClass}" /></span>
                            <strong>Ghi chú gần nhất</strong><span><c:out value="${empty dossier.reviewMessage ? 'Chưa có ghi chú' : dossier.reviewMessage}" /></span>
                        </div>
                    </div>
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
</body>
</html>


<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>

<c:set var="ctx" value="${pageContext.request.contextPath}" />

<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Quản lý Lệ phí thi - Lái Vui</title>
    <link rel="preconnect" href="https://fonts.googleapis.com">
    <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
    <link href="https://fonts.googleapis.com/css2?family=Be+Vietnam+Pro:wght@400;500;600;700;800&family=Inter:wght@400;500;600;700;800&display=swap" rel="stylesheet">
    <link rel="stylesheet" href="${ctx}/assets/css/style.css">
    <link rel="stylesheet" href="${ctx}/assets/css/layout.css">
</head>
<body class="has-side-nav-bar">

<jsp:include page="/views/layout/sidebar-admin.jsp">
    <jsp:param name="activeSidebar" value="le-phi" />
</jsp:include>

<div class="dashboard-shell">
    <main class="main-content">

        <nav class="breadcrumbs" aria-label="Breadcrumb">
            <a href="${ctx}/views/public/home.jsp">Trang chủ</a>
            <span class="breadcrumbs__separator" aria-hidden="true">/</span>
            <a href="${ctx}/admin/dashboard">Quản trị</a>
            <span class="breadcrumbs__separator" aria-hidden="true">/</span>
            <span class="breadcrumbs__current" aria-current="page">Lệ phí thi</span>
        </nav>

        <c:if test="${not empty sessionScope.flashMessage}">
            <div style="margin-bottom: 1.25rem; padding: 0.85rem 1.1rem; border-radius: 10px; font-weight: 600; font-size: 0.9rem; display: flex; align-items: center; gap: 10px;
                        background: ${sessionScope.flashType eq 'success' ? 'rgba(16,185,129,0.08)' : 'rgba(239,68,68,0.08)'};
                        border: 1px solid ${sessionScope.flashType eq 'success' ? 'rgba(16,185,129,0.25)' : 'rgba(239,68,68,0.25)'};
                        color: ${sessionScope.flashType eq 'success' ? '#047857' : '#b91c1c'};">
                ${sessionScope.flashMessage}
            </div>
            <c:remove var="flashMessage" scope="session" />
            <c:remove var="flashType" scope="session" />
        </c:if>

        <header class="page-header">
            <div class="page-title-wrap">
                <h1 class="page-title">Quản lý Lệ phí thi</h1>
                <p class="page-subtitle">Thiết lập đơn giá lệ phí sát hạch lý thuyết, thực hành sa hình, thuê xe chip và lệ phí cấp bằng theo từng hạng GPLX.</p>
            </div>
            <div class="page-actions" style="display: flex; gap: 10px;">
                <button type="button" class="btn-filter" id="btn-add-fee" onclick="openFeeModal()" style="height: 42px; padding: 0 1.25rem; font-size: 0.9rem; border-radius: 8px; flex: none; cursor:pointer; display:inline-flex; align-items:center; gap:6px;">
                    <svg width="16" height="16" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                        <path d="M12 5v14M5 12h14" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round"/>
                    </svg>
                    Thêm cấu hình phí
                </button>
            </div>
        </header>

        <section class="metrics-row" aria-label="Thống kê lệ phí">
            <div class="stat-card">
                <div class="stat-icon stat-icon--blue">
                    <svg width="24" height="24" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                        <rect x="2" y="4" width="20" height="14" rx="2" stroke="currentColor" stroke-width="2"/>
                        <path d="M12 11h.01M17 11h.01M7 11h.01M12 15h.01M7 15h.01M17 15h.01" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
                    </svg>
                </div>
                <div class="stat-info">
                    <span class="stat-number">${empty totalFees ? 0 : totalFees}</span>
                    <span class="stat-label">Tổng số biểu phí</span>
                    <span class="stat-trend stat-trend--up">Toàn hệ thống</span>
                </div>
            </div>
            <div class="stat-card">
                <div class="stat-icon stat-icon--blue" style="background-color: rgba(6,182,212,0.08); color: #06b6d4;">
                    <svg width="24" height="24" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                        <path d="M12 2v20M17 5H9.5a3.5 3.5 0 0 0 0 7h5a3.5 3.5 0 0 1 0 7H6" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                    </svg>
                </div>
                <div class="stat-info">
                    <span class="stat-number">${empty theoryFees ? 0 : theoryFees}</span>
                    <span class="stat-label">Phí Lý Thuyết</span>
                    <span class="stat-trend stat-trend--up" style="color: #06b6d4;">Sát hạch lý thuyết</span>
                </div>
            </div>
            <div class="stat-card">
                <div class="stat-icon stat-icon--amber">
                    <svg width="24" height="24" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                        <path d="M19 17h2c.6 0 1-.4 1-1v-3c0-.9-.7-1.7-1.5-1.9C18.7 10.6 16 10 16 10s-1.3-1.4-2.2-2.3c-.5-.4-1.1-.7-1.8-.7H5c-1.1 0-2 .9-2 2v7h2" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
                        <circle cx="7" cy="17" r="2" stroke="currentColor" stroke-width="2"/>
                        <circle cx="17" cy="17" r="2" stroke="currentColor" stroke-width="2"/>
                    </svg>
                </div>
                <div class="stat-info">
                    <span class="stat-number">${empty practicalFees ? 0 : practicalFees}</span>
                    <span class="stat-label">Phí Thực Hành / Xe Chip</span>
                    <span class="stat-trend stat-trend--up">Sa hình & Đường trường</span>
                </div>
            </div>
            <div class="stat-card">
                <div class="stat-icon stat-icon--green">
                    <svg width="24" height="24" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                        <circle cx="12" cy="12" r="10" stroke="currentColor" stroke-width="2"/>
                        <path d="M8 12.5l3 3 5-6" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                    </svg>
                </div>
                <div class="stat-info">
                    <span class="stat-number">${empty certFees ? 0 : certFees}</span>
                    <span class="stat-label">Lệ phí cấp bằng / Khác</span>
                    <span class="stat-trend stat-trend--up">Cấp bằng & Administrative</span>
                </div>
            </div>
        </section>

        <section class="filter-panel" aria-label="Bộ lọc lệ phí">
            <h2 class="filter-title">
                <svg width="18" height="18" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                    <path d="M22 3H2l8 9.46V19l4 2v-8.54L22 3z" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                </svg>
                Bộ lọc tìm kiếm
            </h2>
            <form action="${ctx}/admin/exam-fee" method="GET">
                <div class="filter-grid" style="grid-template-columns: 2fr 1.25fr 1.1fr 1.1fr 1.75fr;">
                    <div class="input-group">
                        <label for="searchKeyword" class="input-label">Tìm cấu hình lệ phí</label>
                        <input type="text" id="searchKeyword" name="searchKeyword" class="input-field"
                               placeholder="Nhập tên biểu phí..." value="${param.searchKeyword}">
                    </div>
                    <div class="input-group">
                        <label for="filterClass" class="input-label">Hạng GPLX</label>
                        <select id="filterClass" name="filterClass" class="input-field">
                            <option value="">Tất cả hạng</option>
                            <c:forEach var="cl" items="${licenceClassesList}">
                                <option value="${cl.licenceId}" ${param.filterClass eq cl.licenceId ? 'selected' : ''}>Hạng ${cl.licenceClass}</option>
                            </c:forEach>
                        </select>
                    </div>
                    <div class="input-group">
                        <label for="filterCategory" class="input-label">Phân loại phí</label>
                        <select id="filterCategory" name="filterCategory" class="input-field">
                            <option value="">Tất cả phân loại</option>
                            <option value="theory" ${param.filterCategory eq 'theory' ? 'selected' : ''}>Sát hạch lý thuyết</option>
                            <option value="practical" ${param.filterCategory eq 'practical' ? 'selected' : ''}>Sát hạch sa hình</option>
                            <option value="rent" ${param.filterCategory eq 'rent' ? 'selected' : ''}>Thuê xe chip</option>
                            <option value="license" ${param.filterCategory eq 'license' ? 'selected' : ''}>Lệ phí cấp bằng</option>
                        </select>
                    </div>
                    <div class="input-group">
                        <label for="filterStatus" class="input-label">Trạng thái</label>
                        <select id="filterStatus" name="filterStatus" class="input-field">
                            <option value="">Tất cả trạng thái</option>
                            <option value="active" ${param.filterStatus eq 'active' ? 'selected' : ''}>Đang hoạt động</option>
                            <option value="inactive" ${param.filterStatus eq 'inactive' ? 'selected' : ''}>Tạm dừng</option>
                        </select>
                    </div>
                    <div class="input-group filter-grid__btn-col">
                        <div class="btn-group">
                            <button type="submit" class="btn-filter">
                                <svg width="16" height="16" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                                    <path d="M21 21l-6-6m2-5a7 7 0 11-14 0 7 7 0 0114 0z" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round"/>
                                </svg>
                                Lọc
                            </button>
                            <a href="${ctx}/admin/exam-fee" class="btn-reset">Đặt lại</a>
                        </div>
                    </div>
                </div>
            </form>
        </section>

        <section class="log-card" aria-label="Danh sách lệ phí thi">
            <header class="log-card-header">
                <h2 class="log-card-title">
                    <svg width="20" height="20" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg" style="color: #0052cc;">
                        <rect x="2" y="5" width="20" height="14" rx="2" stroke="currentColor" stroke-width="2"/>
                        <path d="M2 10H22" stroke="currentColor" stroke-width="2"/>
                        <path d="M6 15H8M10 15H12" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
                    </svg>
                    Bảng cấu hình biểu phí sát hạch
                    <c:if test="${not empty examFees}">
                        <span style="font-size: 0.78rem; font-weight: 600; background: rgba(0,82,204,0.08); color: #0052cc; padding: 2px 10px; border-radius: 9999px; margin-left: 6px;">
                            ${fn:length(examFees)} biểu phí
                        </span>
                    </c:if>
                </h2>
            </header>

            <div class="table-responsive">
                <table class="audit-table">
                    <thead>
                        <tr>
                            <th scope="col" class="col-id">STT</th>
                            <th scope="col" style="width: 130px;">Mã biểu phí</th>
                            <th scope="col">Tên gọi biểu phí</th>
                            <th scope="col" style="width: 120px; text-align: center;">Hạng GPLX</th>
                            <th scope="col" style="width: 160px; text-align: center;">Phân loại lệ phí</th>
                            <th scope="col" style="width: 160px; text-align: right;">Mức thu (Đơn giá)</th>
                            <th scope="col" style="width: 130px; text-align: center;">Trạng thái</th>
                            <th scope="col" style="width: 140px; text-align: center;">Cập nhật</th>
                            <th scope="col" style="text-align: center; width: 160px;">Thao tác</th>
                        </tr>
                    </thead>
                    <tbody>
                        <c:choose>
                            <c:when test="${not empty examFees}">
                                <c:forEach var="fee" items="${examFees}" varStatus="status">
                                    <tr>
                                        <td class="col-id">${status.index + 1}</td>
                                        <td style="font-weight: 700; color: #0052cc; font-family: monospace; font-size: 0.9rem;">${fee.code}</td>
                                        <td>
                                            <div class="user-info" style="white-space: normal;">
                                                <span class="user-name" style="font-size: 0.92rem; font-weight: 600; color: #0f172a; white-space: normal;">${fee.name}</span>
                                            </div>
                                        </td>
                                        <td style="text-align: center;">
                                            <span class="role-badge role-badge--admin" style="font-weight: 800; padding: 3px 10px; border-radius: 5px;">
                                                ${empty fee.licenceClass ? 'Tất cả' : fee.licenceClass}
                                            </span>
                                        </td>
                                        <td style="text-align: center;">
                                            <c:choose>
                                                <c:when test="${fee.category eq 'theory'}"><span class="role-badge role-badge--coi" style="color:#06b6d4; background-color:rgba(6,182,212,0.06); border-color:rgba(6,182,212,0.15);">Thi lý thuyết</span></c:when>
                                                <c:when test="${fee.category eq 'practical'}"><span class="role-badge role-badge--other" style="color:#b45309; background-color:rgba(245,158,11,0.06); border-color:rgba(245,158,11,0.15);">Thi sa hình</span></c:when>
                                                <c:when test="${fee.category eq 'rent'}"><span class="role-badge role-badge--coi" style="color:#7c3aed; background-color:rgba(124,58,237,0.06); border-color:rgba(124,58,237,0.15);">Thuê xe chip</span></c:when>
                                                <c:otherwise><span class="role-badge role-badge--other" style="color:#475569; background-color:rgba(100,116,139,0.06); border-color:rgba(100,116,139,0.15);">Lệ phí cấp bằng</span></c:otherwise>
                                            </c:choose>
                                        </td>
                                        <td style="text-align: right; font-size: 1.05rem; font-weight: 800; color: #059669; font-family: 'Inter', sans-serif;">
                                            <fmt:formatNumber value="${fee.amount}" type="currency" currencySymbol="đ" maxFractionDigits="0"/>
                                        </td>
                                        <td style="text-align: center;">
                                            <c:choose>
                                                <c:when test="${fee.status eq 'active'}"><span class="action-badge action-badge--success">Hoạt động</span></c:when>
                                                <c:otherwise><span class="action-badge action-badge--danger">Tạm dừng</span></c:otherwise>
                                            </c:choose>
                                        </td>
                                        <td style="text-align: center; font-size: 0.8rem; color: #64748b; font-weight: 500;">
                                            <fmt:formatDate value="${fee.lastUpdated}" pattern="dd/MM/yyyy HH:mm"/>
                                        </td>
                                        <td>
                                            <div style="display: flex; gap: 6px; justify-content: center;">
                                                <button type="button" class="btn-export"
                                                        style="padding: 4px 10px; font-size: 0.8rem; border-radius: 6px; border-color: rgba(245,158,11,0.25); color: #d97706; cursor:pointer;"
                                                        data-id="${fee.id}" data-name="${fn:escapeXml(fee.name)}" data-type="${fee.category}"
                                                        data-amount="${fee.amount}" data-licence="${empty fee.licenceId ? '' : fee.licenceId}" data-status="${fee.status}"
                                                        onclick="openFeeModalEdit(this)">
                                                    Sửa
                                                </button>
                                                <button type="button" class="btn-export"
                                                        style="padding: 4px 10px; font-size: 0.8rem; border-radius: 6px; border-color: rgba(239,68,68,0.25); color: #dc2626; cursor:pointer;"
                                                        onclick="deleteFee('${fee.id}', '${fee.code}')">
                                                    Xóa
                                                </button>
                                            </div>
                                        </td>
                                    </tr>
                                </c:forEach>
                            </c:when>
                            <c:otherwise>
                                <tr>
                                    <td colspan="9" style="text-align: center; padding: 5rem 1.5rem; color: #64748b; font-weight: 500;">
                                        <svg width="64" height="64" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg" style="margin: 0 auto 1.5rem; display: block; opacity: 0.25; color: #64748b;">
                                            <rect x="2" y="5" width="20" height="14" rx="2" stroke="currentColor" stroke-width="2"/>
                                            <path d="M2 10H22" stroke="currentColor" stroke-width="2"/>
                                            <path d="M6 15H8M10 15H12" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
                                        </svg>
                                        Chưa có cấu hình đơn giá lệ phí nào trong hệ thống.
                                        <p style="font-size: 0.82rem; font-weight: 400; color: #94a3b8; margin-top: 0.5rem; max-width: 450px; margin-left: auto; margin-right: auto;">
                                            Nhấn nút <strong>Thêm cấu hình phí</strong> để khai báo đơn giá đầu tiên.
                                        </p>
                                    </td>
                                </tr>
                            </c:otherwise>
                        </c:choose>
                    </tbody>
                </table>
            </div>

            <footer class="pagination-footer">
                <div class="pagination-info">
                    Hiển thị <c:choose><c:when test="${not empty examFees}">1 - ${fn:length(examFees)}</c:when><c:otherwise>0</c:otherwise></c:choose>
                    trong tổng số ${empty totalFees ? 0 : totalFees} biểu phí
                </div>
                <div class="pagination-nav">
                    <button class="page-btn page-btn--wide disabled" disabled>Trước</button>
                    <button class="page-btn active">1</button>
                    <button class="page-btn page-btn--wide disabled" disabled>Sau</button>
                </div>
            </footer>
        </section>

    </main>

    <jsp:include page="/views/layout/footer.jsp">
        <jsp:param name="standalone" value="false" />
    </jsp:include>
</div>

<form id="delFeeForm" action="${ctx}/admin/exam-fee" method="POST" style="display:none;">
    <input type="hidden" name="action" value="delete">
    <input type="hidden" name="id" id="delFeeId">
</form>

<style>
    .modal-overlay { display:none; position:fixed; inset:0; z-index:1000; background:rgba(15,23,42,0.45); align-items:flex-start; justify-content:center; padding:4vh 1rem; overflow-y:auto; }
    .modal-overlay.is-open { display:flex; }
    .modal-card { width:100%; max-width:620px; background:#fff; border-radius:16px; box-shadow:0 20px 60px rgba(15,23,42,0.25); font-family:'Be Vietnam Pro','Inter',sans-serif; }
    .modal-head { display:flex; align-items:center; justify-content:space-between; padding:1.25rem 1.5rem; border-bottom:1px solid #e2e8f0; }
    .modal-head h3 { margin:0; font-size:1.1rem; font-weight:800; color:#0f172a; }
    .modal-close { border:none; background:transparent; font-size:1.5rem; line-height:1; color:#94a3b8; cursor:pointer; padding:0 4px; }
    .modal-body { padding:1.5rem; }
    .modal-foot { display:flex; gap:12px; justify-content:flex-end; padding:1rem 1.5rem; border-top:1px solid #e2e8f0; }
</style>

<div id="feeModal" class="modal-overlay" onclick="if(event.target===this)closeFeeModal()">
    <div class="modal-card" role="dialog" aria-modal="true">
        <form action="${ctx}/admin/exam-fee?action=save" method="POST">
            <div class="modal-head">
                <h3 id="feeModalTitle">Thêm cấu hình phí</h3>
                <button type="button" class="modal-close" onclick="closeFeeModal()">&times;</button>
            </div>
            <div class="modal-body">
                <input type="hidden" name="feeId" id="f_id" value="">
                <div class="input-group" style="margin-bottom:1.25rem;">
                    <label for="f_name" class="input-label">Tên biểu phí <span style="color:#dc2626;">*</span></label>
                    <input type="text" id="f_name" name="feeName" class="input-field" placeholder="VD: Lệ phí sát hạch lý thuyết hạng B2" required>
                </div>
                <div class="filter-grid" style="grid-template-columns:1fr 1fr; gap:1.25rem; margin-bottom:1.25rem;">
                    <div class="input-group">
                        <label for="f_type" class="input-label">Phân loại phí <span style="color:#dc2626;">*</span></label>
                        <select id="f_type" name="feeType" class="input-field" required>
                            <option value="">-- Chọn phân loại --</option>
                            <option value="theory">Sát hạch lý thuyết</option>
                            <option value="practical">Sát hạch sa hình</option>
                            <option value="rent">Thuê xe chip</option>
                            <option value="license">Lệ phí cấp bằng</option>
                        </select>
                    </div>
                    <div class="input-group">
                        <label for="f_class" class="input-label">Hạng GPLX</label>
                        <select id="f_class" name="licenceId" class="input-field">
                            <option value="">Tất cả hạng</option>
                            <c:forEach var="cl" items="${licenceClassesList}">
                                <option value="${cl.licenceId}">Hạng ${cl.licenceClass}</option>
                            </c:forEach>
                        </select>
                    </div>
                </div>
                <div class="filter-grid" style="grid-template-columns:1fr 1fr; gap:1.25rem;">
                    <div class="input-group">
                        <label for="f_amount" class="input-label">Mức thu (đồng) <span style="color:#dc2626;">*</span></label>
                        <input type="number" id="f_amount" name="amount" class="input-field" min="0" step="1000" placeholder="VD: 90000" required>
                    </div>
                    <div class="input-group">
                        <label for="f_status" class="input-label">Trạng thái</label>
                        <select id="f_status" name="status" class="input-field">
                            <option value="active">Hoạt động</option>
                            <option value="inactive">Tạm dừng</option>
                        </select>
                    </div>
                </div>
            </div>
            <div class="modal-foot">
                <button type="button" class="btn-reset" onclick="closeFeeModal()" style="height:44px; padding:0 1.5rem; display:inline-flex; align-items:center;">Hủy bỏ</button>
                <button type="submit" class="btn-filter" style="height:44px; padding:0 1.5rem;">Lưu biểu phí</button>
            </div>
        </form>
    </div>
</div>

<script>
    function openFeeModal() {
        document.getElementById('feeModalTitle').textContent = 'Thêm cấu hình phí';
        ['f_id','f_name','f_type','f_class','f_amount'].forEach(function(k){document.getElementById(k).value='';});
        document.getElementById('f_status').value = 'active';
        document.getElementById('feeModal').classList.add('is-open');
    }
    function openFeeModalEdit(b) {
        document.getElementById('feeModalTitle').textContent = 'Chỉnh sửa biểu phí';
        document.getElementById('f_id').value = b.dataset.id;
        document.getElementById('f_name').value = b.dataset.name;
        document.getElementById('f_type').value = b.dataset.type;
        document.getElementById('f_class').value = b.dataset.licence || '';
        document.getElementById('f_amount').value = b.dataset.amount;
        document.getElementById('f_status').value = b.dataset.status;
        document.getElementById('feeModal').classList.add('is-open');
    }
    function closeFeeModal() { document.getElementById('feeModal').classList.remove('is-open'); }
    function deleteFee(id, code) {
        if (confirm('Xóa biểu phí "' + code + '" khỏi hệ thống?\nHành động này không thể hoàn tác.')) {
            document.getElementById('delFeeId').value = id;
            document.getElementById('delFeeForm').submit();
        }
    }
    document.addEventListener('keydown', function(e){ if(e.key==='Escape') closeFeeModal(); });
</script>

</body>
</html>

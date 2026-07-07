<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>

<c:set var="ctx" value="${pageContext.request.contextPath}" />

<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Quản lý Máy thi - Lái Vui</title>
    <link rel="preconnect" href="https://fonts.googleapis.com">
    <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
    <link href="https://fonts.googleapis.com/css2?family=Be+Vietnam+Pro:wght@400;500;600;700;800&family=Inter:wght@400;500;600;700;800&display=swap" rel="stylesheet">
    <jsp:include page="/views/admin/components/admin-styles.jsp" />
</head>
<body class="has-side-nav-bar">

<jsp:include page="/views/admin/components/sidebar.jsp">
    <jsp:param name="activeSidebar" value="may-thi" />
</jsp:include>

<div class="dashboard-shell">
    <main class="main-content">

        <nav class="breadcrumbs" aria-label="Breadcrumb">
            <a href="${ctx}/views/public/home.jsp">Trang chủ</a>
            <span class="breadcrumbs__separator" aria-hidden="true">/</span>
            <a href="${ctx}/admin/dashboard">Quản trị</a>
            <span class="breadcrumbs__separator" aria-hidden="true">/</span>
            <span class="breadcrumbs__current" aria-current="page">Máy thi</span>
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
                <h1 class="page-title">Quản lý Máy thi</h1>
                <p class="page-subtitle">Quản lý danh sách thiết bị/máy thi theo từng khu vực thi: loại thiết bị, tình trạng hoạt động và khu vực trực thuộc.</p>
            </div>
            <div class="page-actions" style="display: flex; gap: 10px;">
                <button type="button" class="btn-filter" id="btn-add-computer" onclick="openDevModal()" style="height: 42px; padding: 0 1.25rem; font-size: 0.9rem; border-radius: 8px; flex: none; cursor:pointer; display:inline-flex; align-items:center; gap:6px;">
                    <svg width="16" height="16" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                        <path d="M12 5v14M5 12h14" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round"/>
                    </svg>
                    Thêm máy thi
                </button>
            </div>
        </header>

        <section class="metrics-row" aria-label="Thống kê máy thi">
            <div class="stat-card">
                <div class="stat-icon stat-icon--blue">
                    <svg width="24" height="24" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                        <rect x="2" y="4" width="20" height="12" rx="2" stroke="currentColor" stroke-width="2"/>
                        <path d="M8 20h8M12 16v4" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
                    </svg>
                </div>
                <div class="stat-info">
                    <span class="stat-number">${empty totalDevices ? 0 : totalDevices}</span>
                    <span class="stat-label">Tổng số máy</span>
                    <span class="stat-trend stat-trend--up">Toàn hệ thống</span>
                </div>
            </div>
            <div class="stat-card">
                <div class="stat-icon stat-icon--green">
                    <svg width="24" height="24" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                        <path d="M22 11.08V12a10 10 0 1 1-5.93-9.14" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
                        <path d="M22 4L12 14.01l-3-3" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                    </svg>
                </div>
                <div class="stat-info">
                    <span class="stat-number">${empty activeDevices ? 0 : activeDevices}</span>
                    <span class="stat-label">Đang hoạt động</span>
                    <span class="stat-trend stat-trend--up">Sẵn sàng sử dụng</span>
                </div>
            </div>
            <div class="stat-card">
                <div class="stat-icon stat-icon--amber">
                    <svg width="24" height="24" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                        <circle cx="12" cy="12" r="10" stroke="currentColor" stroke-width="2"/>
                        <path d="M12 8v4l3 3" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
                    </svg>
                </div>
                <div class="stat-info">
                    <span class="stat-number">${empty maintenanceDevices ? 0 : maintenanceDevices}</span>
                    <span class="stat-label">Đang bảo trì</span>
                    <span class="stat-trend stat-trend--up">Tạm ngưng phục vụ</span>
                </div>
            </div>
            <div class="stat-card">
                <div class="stat-icon stat-icon--red">
                    <svg width="24" height="24" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                        <rect x="2" y="4" width="20" height="12" rx="2" stroke="currentColor" stroke-width="2" style="opacity:0.4;"/>
                        <path d="M8 20h8M12 16v4" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
                        <line x1="5" y1="7" x2="19" y2="13" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
                    </svg>
                </div>
                <div class="stat-info">
                    <span class="stat-number">${empty brokenDevices ? 0 : brokenDevices}</span>
                    <span class="stat-label font-bold">Hỏng / Khóa</span>
                    <span class="stat-trend stat-trend--down">Cần xử lý</span>
                </div>
            </div>
        </section>

        <section class="filter-panel" aria-label="Bộ lọc máy thi">
            <h2 class="filter-title">
                <svg width="18" height="18" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                    <path d="M22 3H2l8 9.46V19l4 2v-8.54L22 3z" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                </svg>
                Bộ lọc tìm kiếm
            </h2>
            <form action="${ctx}/admin/exam-computer" method="GET">
                <div class="filter-grid" style="grid-template-columns: 2fr 1.25fr 1.75fr;">
                    <div class="input-group">
                        <label for="searchKeyword" class="input-label">Tìm máy thi</label>
                        <input type="text" id="searchKeyword" name="searchKeyword" class="input-field"
                               placeholder="Nhập tên máy/thiết bị..." value="${param.searchKeyword}">
                    </div>
                    <div class="input-group">
                        <label for="filterStatus" class="input-label">Tình trạng máy</label>
                        <select id="filterStatus" name="filterStatus" class="input-field">
                            <option value="">Tất cả tình trạng</option>
                            <option value="Hoạt động" ${param.filterStatus eq 'Hoạt động' ? 'selected' : ''}>Đang hoạt động</option>
                            <option value="Bảo trì" ${param.filterStatus eq 'Bảo trì' ? 'selected' : ''}>Đang bảo trì</option>
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
                            <a href="${ctx}/admin/exam-computer" class="btn-reset">Đặt lại</a>
                        </div>
                    </div>
                </div>
            </form>
        </section>

        <section class="log-card" aria-label="Danh sách máy thi">
            <header class="log-card-header">
                <h2 class="log-card-title">
                    <svg width="20" height="20" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg" style="color: #0052cc;">
                        <rect x="2" y="4" width="20" height="12" rx="2" stroke="currentColor" stroke-width="2"/>
                        <path d="M8 20h8M12 16v4" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
                    </svg>
                    Danh sách máy thi
                    <c:if test="${not empty examDevices}">
                        <span style="font-size: 0.78rem; font-weight: 600; background: rgba(0,82,204,0.08); color: #0052cc; padding: 2px 10px; border-radius: 9999px; margin-left: 6px;">
                            ${fn:length(examDevices)} máy
                        </span>
                    </c:if>
                </h2>
            </header>

            <div class="table-responsive">
                <table class="audit-table">
                    <thead>
                        <tr>
                            <th scope="col" class="col-id">STT</th>
                            <th scope="col" style="width: 120px;">Mã máy</th>
                            <th scope="col">Tên thiết bị</th>
                            <th scope="col" style="width: 170px;">Loại thiết bị</th>
                            <th scope="col">Khu vực thi</th>
                            <th scope="col" style="width: 140px; text-align: center;">Tình trạng máy</th>
                            <th scope="col" style="text-align: center; width: 220px;">Thao tác</th>
                        </tr>
                    </thead>
                    <tbody>
                        <c:choose>
                            <c:when test="${not empty examDevices}">
                                <c:forEach var="dev" items="${examDevices}" varStatus="status">
                                    <tr>
                                        <td class="col-id">${status.index + 1}</td>
                                        <td style="font-weight: 700; color: #0052cc; font-family: monospace; font-size: 0.9rem;">${dev.code}</td>
                                        <td>
                                            <div class="user-cell">
                                                <div class="user-avatar" style="background: linear-gradient(135deg,#0052cc,#003d9b); border-radius: 8px;">
                                                    <svg width="14" height="14" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                                                        <rect x="2" y="4" width="20" height="12" rx="2" stroke="currentColor" stroke-width="2"/>
                                                        <path d="M8 20h8M12 16v4" stroke="currentColor" stroke-width="2"/>
                                                    </svg>
                                                </div>
                                                <div class="user-info"><span class="user-name">${dev.deviceName}</span></div>
                                            </div>
                                        </td>
                                        <td style="font-size: 0.88rem; color: #475569; font-weight: 500;">${empty dev.deviceType ? '—' : dev.deviceType}</td>
                                        <td>
                                            <div class="user-info">
                                                <span class="user-name" style="font-size: 0.88rem;">${empty dev.areaName ? '—' : dev.areaName}</span>
                                            </div>
                                        </td>
                                        <td style="text-align: center;">
                                            <c:choose>
                                                <c:when test="${dev.status eq 'Hoạt động'}"><span class="action-badge action-badge--success">Hoạt động</span></c:when>
                                                <c:when test="${dev.status eq 'Bảo trì'}"><span class="action-badge action-badge--warning">Bảo trì</span></c:when>
                                                <c:otherwise><span class="action-badge action-badge--danger">Khóa / Hỏng</span></c:otherwise>
                                            </c:choose>
                                        </td>
                                        <td>
                                            <div style="display: flex; gap: 5px; justify-content: center; flex-wrap: wrap;">
                                                <button type="button" class="btn-export"
                                                        style="padding: 4px 10px; font-size: 0.8rem; border-radius: 6px; border-color: rgba(2,132,199,0.25); color: #0284c7; cursor:pointer;"
                                                        data-code="${dev.code}" data-name="${fn:escapeXml(dev.deviceName)}"
                                                        data-type="${fn:escapeXml(dev.deviceType)}" data-areaName="${fn:escapeXml(dev.areaName)}" data-status="${dev.status}"
                                                        onclick="openDevDetail(this)">
                                                    Chi tiết
                                                </button>
                                                <button type="button" class="btn-export"
                                                        style="padding: 4px 10px; font-size: 0.8rem; border-radius: 6px; border-color: rgba(245,158,11,0.25); color: #d97706; cursor:pointer;"
                                                        data-id="${dev.id}" data-name="${fn:escapeXml(dev.deviceName)}"
                                                        data-type="${fn:escapeXml(dev.deviceType)}" data-status="${dev.status}" data-area="${dev.examAreaId}"
                                                        onclick="openDevModalEdit(this)">
                                                    Sửa
                                                </button>
                                                <button type="button" class="btn-export"
                                                        style="padding: 4px 10px; font-size: 0.8rem; border-radius: 6px; border-color: rgba(239,68,68,0.25); color: #dc2626; cursor:pointer;"
                                                        onclick="deleteDev('${dev.id}', '${fn:escapeXml(dev.deviceName)}')">
                                                    Xóa
                                                </button>
                                            </div>
                                        </td>
                                    </tr>
                                </c:forEach>
                            </c:when>
                            <c:otherwise>
                                <tr>
                                    <td colspan="7" style="text-align: center; padding: 5rem 1.5rem; color: #64748b; font-weight: 500;">
                                        <svg width="64" height="64" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg" style="margin: 0 auto 1.5rem; display: block; opacity: 0.25; color: #64748b;">
                                            <rect x="2" y="4" width="20" height="12" rx="2" stroke="currentColor" stroke-width="2"/>
                                            <path d="M8 20h8M12 16v4" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
                                        </svg>
                                        Chưa có máy thi nào trong hệ thống.
                                        <p style="font-size: 0.82rem; font-weight: 400; color: #94a3b8; margin-top: 0.5rem; max-width: 450px; margin-left: auto; margin-right: auto;">
                                            Nhấn nút <strong>Thêm máy thi</strong> để thêm thiết bị đầu tiên, hoặc chỉnh bộ lọc tìm kiếm.
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
                    Hiển thị <c:choose><c:when test="${not empty examDevices}">1 - ${fn:length(examDevices)}</c:when><c:otherwise>0</c:otherwise></c:choose>
                    trong tổng số ${empty totalDevices ? 0 : totalDevices} máy thi
                </div>
                <div class="pagination-nav">
                    <button class="page-btn page-btn--wide disabled" disabled>Trước</button>
                    <button class="page-btn active">1</button>
                    <button class="page-btn page-btn--wide disabled" disabled>Sau</button>
                </div>
            </footer>
        </section>

    </main>
</div>

<form id="deleteDevForm" action="${ctx}/admin/exam-computer" method="POST" style="display:none;">
    <input type="hidden" name="action" value="delete">
    <input type="hidden" name="id" id="deleteDevId">
</form>

<style>
    .modal-overlay { display:none; position:fixed; inset:0; z-index:1000; background:rgba(15,23,42,0.45); align-items:flex-start; justify-content:center; padding:4vh 1rem; overflow-y:auto; }
    .modal-overlay.is-open { display:flex; }
    .modal-card { width:100%; max-width:600px; background:#fff; border-radius:16px; box-shadow:0 20px 60px rgba(15,23,42,0.25); font-family:'Be Vietnam Pro','Inter',sans-serif; animation:modalIn .18s ease-out; }
    @keyframes modalIn { from{opacity:0;transform:translateY(-12px);} to{opacity:1;transform:none;} }
    .modal-head { display:flex; align-items:center; justify-content:space-between; padding:1.25rem 1.5rem; border-bottom:1px solid #e2e8f0; }
    .modal-head h3 { margin:0; font-size:1.1rem; font-weight:800; color:#0f172a; }
    .modal-close { border:none; background:transparent; font-size:1.5rem; line-height:1; color:#94a3b8; cursor:pointer; padding:0 4px; }
    .modal-body { padding:1.5rem; }
    .modal-foot { display:flex; gap:12px; justify-content:flex-end; padding:1rem 1.5rem; border-top:1px solid #e2e8f0; }
    .detail-row { display:flex; justify-content:space-between; gap:1rem; padding:0.6rem 0; border-bottom:1px dashed #e2e8f0; font-size:0.9rem; }
    .detail-row span:first-child { color:#64748b; }
    .detail-row span:last-child { font-weight:600; color:#0f172a; text-align:right; }
</style>

<%-- Add/Edit modal --%>
<div id="devModal" class="modal-overlay" onclick="if(event.target===this)closeDevModal()">
    <div class="modal-card" role="dialog" aria-modal="true">
        <form action="${ctx}/admin/exam-computer?action=save" method="POST">
            <div class="modal-head">
                <h3 id="devModalTitle">Thêm máy thi</h3>
                <button type="button" class="modal-close" onclick="closeDevModal()">&times;</button>
            </div>
            <div class="modal-body">
                <input type="hidden" name="examDeviceId" id="m_id" value="">
                <div class="input-group" style="margin-bottom:1.25rem;">
                    <label for="m_name" class="input-label">Tên máy / thiết bị <span style="color:#dc2626;">*</span></label>
                    <input type="text" id="m_name" name="deviceName" class="input-field" placeholder="VD: Máy trạm thi số 01" required>
                </div>
                <div class="filter-grid" style="grid-template-columns:1fr 1fr; gap:1.25rem; margin-bottom:1.25rem;">
                    <div class="input-group">
                        <label for="m_type" class="input-label">Loại thiết bị <span style="color:#dc2626;">*</span></label>
                        <input type="text" id="m_type" name="deviceType" class="input-field" list="deviceTypeList"
                               placeholder="VD: Xe gắn máy, Xe ô tô..." required>
                        <datalist id="deviceTypeList">
                            <option value="Xe gắn máy"></option>
                            <option value="Xe ô tô"></option>
                            <option value="Máy trạm thi"></option>
                            <option value="Camera giám sát"></option>
                            <option value="Thiết bị khác"></option>
                        </datalist>
                    </div>
                    <div class="input-group">
                        <label for="m_status" class="input-label">Tình trạng máy <span style="color:#dc2626;">*</span></label>
                        <select id="m_status" name="status" class="input-field" required>
                            <option value="">-- Chọn tình trạng --</option>
                            <option value="Hoạt động">Đang hoạt động</option>
                            <option value="Bảo trì">Đang bảo trì</option>
                        </select>
                    </div>
                </div>
                <div class="input-group">
                    <label for="m_area" class="input-label">Khu vực thi <span style="color:#dc2626;">*</span></label>
                    <select id="m_area" name="examAreaId" class="input-field" required>
                        <option value="">-- Chọn khu vực thi --</option>
                        <c:forEach var="area" items="${areas}">
                            <option value="${area.id}">${area.name}</option>
                        </c:forEach>
                    </select>
                </div>
            </div>
            <div class="modal-foot">
                <button type="button" class="btn-reset" onclick="closeDevModal()" style="height:44px; padding:0 1.5rem; display:inline-flex; align-items:center;">Hủy bỏ</button>
                <button type="submit" class="btn-filter" style="height:44px; padding:0 1.5rem;">Lưu máy thi</button>
            </div>
        </form>
    </div>
</div>

<%-- Detail modal --%>
<div id="devDetail" class="modal-overlay" onclick="if(event.target===this)closeDevDetail()">
    <div class="modal-card" role="dialog" aria-modal="true" style="max-width:520px;">
        <div class="modal-head">
            <h3>Chi tiết máy thi</h3>
            <button type="button" class="modal-close" onclick="closeDevDetail()">&times;</button>
        </div>
        <div class="modal-body">
            <div class="detail-row"><span>Mã máy</span><span id="dd_code"></span></div>
            <div class="detail-row"><span>Tên thiết bị</span><span id="dd_name"></span></div>
            <div class="detail-row"><span>Loại thiết bị</span><span id="dd_type"></span></div>
            <div class="detail-row"><span>Khu vực thi</span><span id="dd_room"></span></div>
            <div class="detail-row" style="border-bottom:none;"><span>Tình trạng</span><span id="dd_status"></span></div>
        </div>
        <div class="modal-foot">
            <button type="button" class="btn-filter" onclick="closeDevDetail()" style="height:44px; padding:0 1.5rem;">Đóng</button>
        </div>
    </div>
</div>

<script>
    function openDevModal() {
        document.getElementById('devModalTitle').textContent = 'Thêm máy thi';
        ['m_id','m_name','m_type','m_status','m_area'].forEach(function(k){document.getElementById(k).value='';});
        document.getElementById('devModal').classList.add('is-open');
    }
    function openDevModalEdit(b) {
        document.getElementById('devModalTitle').textContent = 'Chỉnh sửa máy thi';
        document.getElementById('m_id').value = b.dataset.id;
        document.getElementById('m_name').value = b.dataset.name;
        document.getElementById('m_type').value = b.dataset.type;
        document.getElementById('m_status').value = b.dataset.status;
        document.getElementById('m_area').value = b.dataset.area;
        document.getElementById('devModal').classList.add('is-open');
    }
    function closeDevModal() { document.getElementById('devModal').classList.remove('is-open'); }

    function openDevDetail(b) {
        document.getElementById('dd_code').textContent = b.dataset.code;
        document.getElementById('dd_name').textContent = b.dataset.name;
        document.getElementById('dd_type').textContent = b.dataset.type && b.dataset.type !== '' ? b.dataset.type : '—';
        document.getElementById('dd_room').textContent = b.dataset.areaname && b.dataset.areaname !== '' ? b.dataset.areaname : '—';
        var st = b.dataset.status;
        var stLabel = (st === 'Hoạt động') ? 'Đang hoạt động'
                    : (st === 'Bảo trì' ? 'Đang bảo trì' : 'Hỏng / Khóa');
        document.getElementById('dd_status').textContent = stLabel;
        document.getElementById('devDetail').classList.add('is-open');
    }
    function closeDevDetail() { document.getElementById('devDetail').classList.remove('is-open'); }

    function deleteDev(id, name) {
        if (confirm('Bạn có chắc chắn muốn xóa máy thi "' + name + '"?\nHành động này không thể hoàn tác.')) {
            document.getElementById('deleteDevId').value = id;
            document.getElementById('deleteDevForm').submit();
        }
    }
    document.addEventListener('keydown', function(e){ if(e.key==='Escape'){closeDevModal();closeDevDetail();} });
</script>

</body>
</html>

<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<c:set var="ctx" value="${pageContext.request.contextPath}" />
<!DOCTYPE html>
<html lang="vi">
    <head>
        <meta charset="UTF-8">
        <meta name="viewport" content="width=device-width, initial-scale=1.0">
        <title>Quản lý Máy thi - Lái Vui</title>
        <link href="https://fonts.googleapis.com/css2?family=Be+Vietnam+Pro:wght@400;500;600;700;800&family=Inter:wght@400;500;600;700;800&display=swap" rel="stylesheet">
        <link rel="stylesheet" href="${ctx}/assets/css/style.css">
        <link rel="stylesheet" href="${ctx}/assets/css/layout.css">
    </head>
    <body class="has-side-nav-bar">
        <jsp:include page="/views/layout/sidebar-admin.jsp">
            <jsp:param name="activeSidebar" value="may-thi" />
        </jsp:include>
        
        <div class="dashboard-shell">
            <main class="main-content">
                <nav class="breadcrumbs">
                    <a href="${ctx}/views/public/home.jsp">Trang chủ</a><span class="breadcrumbs__separator">/</span>
                    <a href="${ctx}/admin/dashboard">Quản trị</a><span class="breadcrumbs__separator">/</span>
                    <span class="breadcrumbs__current" aria-current="page">Máy thi</span>
                </nav>

                <c:if test="${not empty sessionScope.flashMessage and sessionScope.reopenModal == null}">
                    <div style="margin-bottom:1.25rem;padding:.85rem 1.1rem;border-radius:10px;font-weight:600;font-size:.9rem;
                                background:${sessionScope.flashType eq 'success' ? 'rgba(16,185,129,.08)' : 'rgba(239,68,68,.08)'};
                                border:1px solid ${sessionScope.flashType eq 'success' ? 'rgba(16,185,129,.25)' : 'rgba(239,68,68,.25)'};
                                color:${sessionScope.flashType eq 'success' ? '#047857' : '#b91c1c'};">${sessionScope.flashMessage}</div>
                    <c:remove var="flashMessage" scope="session" />
                    <c:remove var="flashType" scope="session" />
                </c:if>

                <header class="page-header">
                    <div class="page-title-wrap">
                        <h1 class="page-title">Quản lý Máy thi &amp; Thiết bị</h1>
                        <p class="page-subtitle">Máy tính (thi lý thuyết) và mô tô (thi thực hành) đặt trong từng phòng/sân. Chọn khu vực rồi phòng/sân trước khi khai báo.</p>
                    </div>
                    <div class="page-actions" style="display:flex;gap:10px;flex-wrap:wrap;">
                        <a href="${ctx}/admin/exam-computer?action=template" class="btn-export" style="height:42px;padding:0 1.25rem;border-radius:8px;display:inline-flex;align-items:center;text-decoration:none;">Tải biểu mẫu Excel</a>
                        <button type="button" class="btn-export" onclick="openImportModal()" style="height:42px;padding:0 1.25rem;border-radius:8px;cursor:pointer;">Import máy/thiết bị</button>
                        <a href="${ctx}/admin/exam-computer?action=export&amp;searchKeyword=${fn:escapeXml(param.searchKeyword)}&amp;filterType=${fn:escapeXml(param.filterType)}&amp;filterZone=${fn:escapeXml(param.filterZone)}&amp;filterArea=${fn:escapeXml(param.filterArea)}"
                           class="btn-export" style="height:42px;padding:0 1.25rem;border-radius:8px;display:inline-flex;align-items:center;text-decoration:none;">Xuất Excel</a>
                        <button type="button" class="btn-filter" onclick="openDevModal()" style="height:42px;padding:0 1.25rem;border-radius:8px;cursor:pointer;">+ Thêm máy/thiết bị</button>
                    </div>
                </header>

                <section class="filter-panel">
                    <h2 class="filter-title">Bộ lọc tìm kiếm</h2>
                    <form action="${ctx}/admin/exam-computer" method="GET">
                        <div class="filter-grid" style="grid-template-columns:2fr 1.3fr 1.3fr 1.4fr;">
                            <div class="input-group">
                                <label class="input-label">Tìm máy/thiết bị</label>
                                <input type="text" name="searchKeyword" class="input-field" placeholder="Tên máy/thiết bị..." value="${param.searchKeyword}">
                            </div>
                            <div class="input-group">
                                <label class="input-label">Khu vực</label>
                                <select name="filterZone" class="input-field">
                                    <option value="">Tất cả khu vực</option>
                                    <c:forEach var="z" items="${zones}">
                                        <option value="${z.zoneId}" ${param.filterZone eq z.zoneId ? 'selected' : ''}>${z.zoneName}</option>
                                    </c:forEach>
                                </select>
                            </div>
                            <div class="input-group">
                                <label class="input-label">Loại thiết bị</label>
                                <select name="filterType" class="input-field">
                                    <option value="">Tất cả loại</option>
                                    <option value="Máy tính" ${param.filterType eq 'Máy tính' ? 'selected' : ''}>Máy tính</option>
                                    <option value="Mô tô" ${param.filterType eq 'Mô tô' ? 'selected' : ''}>Mô tô</option>
                                    <option value="Mô tô ba bánh" ${param.filterType eq 'Mô tô ba bánh' ? 'selected' : ''}>Mô tô ba bánh</option>
                                </select>
                            </div>
                            <div class="input-group filter-grid__btn-col">
                                <div class="btn-group">
                                    <button type="submit" class="btn-filter">Lọc</button>
                                    <a href="${ctx}/admin/exam-computer" class="btn-reset">Đặt lại</a>
                                </div>
                            </div>
                        </div>
                    </form>
                </section>

                <section class="log-card">
                    <header class="log-card-header">
                        <h2 class="log-card-title">Danh sách máy thi / thiết bị
                            <c:if test="${not empty devices}">
                                <span style="font-size:.78rem;font-weight:600;background:rgba(0,82,204,.08);color:#0052cc;padding:2px 10px;border-radius:9999px;margin-left:6px;">${fn:length(devices)}</span>
                            </c:if>
                        </h2>
                        <div class="log-card-actions" style="display:flex;gap:10px;align-items:center;">
                            <button type="button" id="bulkDelBtn" class="btn-export" onclick="bulkDeleteSelected()" disabled
                                    style="cursor:pointer;border-color:rgba(239,68,68,.35);color:#dc2626;">Xóa mục đã chọn (<span id="selCount">0</span>)</button>
                            <c:if test="${not empty devices}">
                                <button type="button" class="btn-export" onclick="bulkDeleteFiltered()" style="cursor:pointer;border-color:rgba(239,68,68,.35);color:#dc2626;">Xóa toàn bộ kết quả lọc</button>
                            </c:if>
                        </div>
                    </header>
                    <div class="table-responsive">
                        <table class="audit-table">
                            <thead>
                                <tr>
                                    <th style="width:42px;text-align:center;"><input type="checkbox" id="checkAll" onclick="toggleAll(this)" title="Chọn tất cả"></th>
                                    <th class="col-id">STT</th>
                                    <th style="width:100px;">Mã</th>
                                    <th>Tên máy/thiết bị</th>
                                    <th style="width:140px;text-align:center;">Loại</th>
                                    <th>Phòng/sân</th>
                                    <th>Khu vực</th>
                                    <th style="width:120px;text-align:center;">Trạng thái</th>
                                    <th style="width:200px;text-align:center;">Thao tác</th>
                                </tr>
                            </thead>
                            <tbody>
                                <c:choose>
                                    <c:when test="${not empty devices}">
                                        <c:forEach var="d" items="${devices}" varStatus="st">
                                            <tr>
                                                <td style="text-align:center;"><input type="checkbox" class="rowChk" value="${d.deviceId}" data-name="${fn:escapeXml(d.deviceName)}" onclick="updateSel()"></td>
                                                <td class="col-id">${st.index+1}</td>
                                                <td style="font-weight:700;color:#0052cc;font-family:monospace;">${d.code}</td>
                                                <td><span class="user-name" style="font-weight:600;color:#0f172a;">${d.deviceName}</span></td>
                                                <td style="text-align:center;"><span class="role-badge role-badge--cham">${d.deviceType}</span></td>
                                                <td>${d.areaName}</td>
                                                <td>${d.zoneName}</td>
                                                <td style="text-align:center;">
                                                    <c:choose>
                                                        <c:when test="${d.active}"><span class="action-badge action-badge--success">Hoạt động</span></c:when>
                                                        <c:otherwise><span class="action-badge action-badge--warning">Bảo trì</span></c:otherwise>
                                                    </c:choose>
                                                </td>
                                                <td>
                                                    <div style="display:flex;gap:6px;justify-content:center;flex-wrap:wrap;">
                                                        <c:choose>
                                                            <c:when test="${d.active}">
                                                                <button type="button" class="btn-export" style="padding:4px 10px;font-size:.8rem;border-color:rgba(245,158,11,.25);color:#d97706;cursor:pointer;" onclick="toggleDev('${d.deviceId}', false)">Bảo trì</button>
                                                            </c:when>
                                                            <c:otherwise>
                                                                <button type="button" class="btn-export" style="padding:4px 10px;font-size:.8rem;border-color:rgba(16,185,129,.25);color:#059669;cursor:pointer;" onclick="toggleDev('${d.deviceId}', true)">Kích hoạt</button>
                                                            </c:otherwise>
                                                        </c:choose>
                                                        <button type="button" class="btn-export" style="padding:4px 10px;font-size:.8rem;border-color:rgba(245,158,11,.25);color:#d97706;cursor:pointer;"
                                                                data-id="${d.deviceId}" data-zone="${d.zoneId}" data-area="${d.areaId}" data-name="${fn:escapeXml(d.deviceName)}"
                                                                data-type="${fn:escapeXml(d.deviceType)}" data-active="${d.active}" onclick="editDev(this)">Sửa</button>
                                                        <button type="button" class="btn-export" style="padding:4px 10px;font-size:.8rem;border-color:rgba(239,68,68,.25);color:#dc2626;cursor:pointer;"
                                                                onclick="delDev('${d.deviceId}', '${fn:escapeXml(d.deviceName)}')">Xóa</button>
                                                    </div>
                                                </td>
                                            </tr>
                                        </c:forEach>
                                    </c:when>
                                    <c:otherwise>
                                        <tr>
                                            <td colspan="9" style="text-align:center;padding:4rem 1.5rem;color:#64748b;">Chưa có máy thi/thiết bị nào. Nhấn <b>Thêm máy/thiết bị</b> để bắt đầu.</td>
                                        </tr>
                                    </c:otherwise>
                                </c:choose>
                            </tbody>
                        </table>
                    </div>
                </section>
            </main>
            <jsp:include page="/views/layout/footer.jsp">
                <jsp:param name="standalone" value="false" />
            </jsp:include>
        </div>

        <form id="delForm" action="${ctx}/admin/exam-computer" method="POST" style="display:none;">
            <input type="hidden" name="action" value="delete">
            <input type="hidden" name="id" id="delId">
        </form>
        <form id="toggleForm" action="${ctx}/admin/exam-computer" method="POST" style="display:none;">
            <input type="hidden" name="action" value="toggle">
            <input type="hidden" name="id" id="tgId">
            <input type="hidden" name="active" id="tgActive">
        </form>

        <form id="bulkForm" action="${ctx}/admin/exam-computer" method="POST" style="display:none;">
            <input type="hidden" name="action" value="bulkDelete">
            <input type="hidden" name="scope" id="bulkScope" value="">
            <input type="hidden" name="searchKeyword" value="${fn:escapeXml(param.searchKeyword)}">
            <input type="hidden" name="filterType" value="${fn:escapeXml(param.filterType)}">
            <input type="hidden" name="filterZone" value="${fn:escapeXml(param.filterZone)}">
            <input type="hidden" name="filterArea" value="${fn:escapeXml(param.filterArea)}">
            <div id="bulkIds"></div>
        </form>

        <style>
            .modal-overlay{
                display:none;
                position:fixed;
                inset:0;
                z-index:1000;
                background:rgba(15,23,42,.45);
                align-items:flex-start;
                justify-content:center;
                padding:4vh 1rem;
                overflow-y:auto;
            }
            .modal-overlay.is-open{
                display:flex;
            }
            .modal-card{
                width:100%;
                max-width:620px;
                background:#fff;
                border-radius:16px;
                box-shadow:0 20px 60px rgba(15,23,42,.25);
            }
            .modal-head{
                display:flex;
                align-items:center;
                justify-content:space-between;
                padding:1.25rem 1.5rem;
                border-bottom:1px solid #e2e8f0;
            }
            .modal-head h3{
                margin:0;
                font-size:1.1rem;
                font-weight:800;
                color:#0f172a;
            }
            .modal-close{
                border:none;
                background:transparent;
                font-size:1.5rem;
                color:#94a3b8;
                cursor:pointer;
            }
            .modal-body{
                padding:1.5rem;
            }
            .modal-foot{
                display:flex;
                gap:12px;
                justify-content:flex-end;
                padding:1rem 1.5rem;
                border-top:1px solid #e2e8f0;
            }
            .modal-err{
                display:none;
                margin-bottom:1rem;
                padding:.7rem .9rem;
                border-radius:8px;
                background:rgba(239,68,68,.08);
                border:1px solid rgba(239,68,68,.25);
                color:#b91c1c;
                font-size:.85rem;
                font-weight:600;
            }
            .modal-err.show{
                display:block;
            }
            #devFields{
                display:none;
            }
            #devFields.show{
                display:block;
            }
        </style>

        <div id="devModal" class="modal-overlay" onclick="if (event.target === this) closeDevModal()">
            <div class="modal-card">
                <form action="${ctx}/admin/exam-computer" method="POST">
                    <div class="modal-head">
                        <h3 id="devTitle">Thêm máy/thiết bị</h3>
                        <button type="button" class="modal-close" onclick="closeDevModal()">&times;</button>
                    </div>
                    <div class="modal-body">
                        <div id="devErr" class="modal-err"></div>
                        <input type="hidden" name="deviceId" id="d_id" value="">
                        <div class="filter-grid" style="grid-template-columns:1fr 1fr;gap:1.25rem;">
                            <div class="input-group">
                                <label class="input-label">Bước 1 &mdash; Khu vực <span style="color:#dc2626;">*</span></label>
                                <select id="d_zone" name="zoneId" class="input-field" onchange="onDevZoneChange()" required>
                                    <option value="">-- Chọn khu vực --</option>
                                    <c:forEach var="z" items="${zones}">
                                        <option value="${z.zoneId}">${z.zoneName}</option>
                                    </c:forEach>
                                </select>
                            </div>
                            <div class="input-group">
                                <label class="input-label">Bước 2 &mdash; Phòng/sân <span style="color:#dc2626;">*</span></label>
                                <select id="d_area" name="areaId" class="input-field" onchange="onDevAreaChange()" required disabled>
                                    <option value="">-- Chọn khu vực trước --</option>
                                </select>
                            </div>
                        </div>
                        <div id="devFields">
                            <div class="input-group" style="margin:1.25rem 0;">
                                <label class="input-label">Bước 3 &mdash; Tên máy/thiết bị <span style="color:#dc2626;">*</span></label>
                                <input type="text" id="d_name" name="deviceName" class="input-field" maxlength="100" placeholder="VD: Máy tính 01 / Xe mô tô B2-05">
                            </div>
                            <div class="filter-grid" style="grid-template-columns:1fr 1fr;gap:1.25rem;">
                                <div class="input-group">
                                    <label class="input-label">Loại thiết bị <span style="color:#dc2626;">*</span></label>
                                    <select id="d_type" name="deviceType" class="input-field">
                                        <option value="">-- Chọn loại --</option>
                                        <option value="Máy tính">Máy tính</option>
                                        <option value="Mô tô">Mô tô</option>
                                        <option value="Mô tô ba bánh">Mô tô ba bánh</option>
                                    </select>
                                </div>
                                <div class="input-group">
                                    <label class="input-label">Trạng thái</label>
                                    <select id="d_status" name="status" class="input-field">
                                        <option value="active">Hoạt động</option>
                                        <option value="inactive">Bảo trì</option>
                                    </select>
                                </div>
                            </div>
                        </div>
                    </div>
                    <div class="modal-foot">
                        <button type="button" class="btn-reset" onclick="closeDevModal()" style="height:44px;padding:0 1.5rem;display:inline-flex;align-items:center;">Hủy</button>
                        <button type="submit" class="btn-filter" style="height:44px;padding:0 1.5rem;">Lưu máy/thiết bị</button>
                    </div>
                </form>
            </div>
        </div>

        <div id="importModal" class="modal-overlay" onclick="if (event.target === this) closeImportModal()">
            <div class="modal-card" style="max-width:560px;">
                <form action="${ctx}/admin/exam-computer" method="POST" enctype="multipart/form-data">
                    <input type="hidden" name="action" value="import">
                    <div class="modal-head">
                        <h3>Import máy / thiết bị thi từ Excel</h3>
                        <button type="button" class="modal-close" onclick="closeImportModal()">&times;</button>
                    </div>
                    <div class="modal-body">
                        <div style="margin-bottom:1.1rem;padding:.7rem .9rem;border-radius:8px;background:#eff6ff;border:1px solid #bfdbfe;color:#1e40af;font-size:.82rem;line-height:1.6;">
                            Dùng đúng <b>biểu mẫu Excel (.xlsx)</b> tải từ nút <b>Tải biểu mẫu Excel</b>. Cột <b>Khu vực thi</b> và
                            <b>Phòng/sân thi</b> phải trùng tên đã có (biểu mẫu kèm sheet <b>"Phòng sân hiện có"</b> để đối chiếu).
                            Dòng sai dữ liệu sẽ bị bỏ qua và báo lại chi tiết.
                        </div>
                        <div class="input-group">
                            <label class="input-label">Chọn file Excel <span style="color:#dc2626;">*</span></label>
                            <input type="file" id="importFile" name="file" class="input-field" accept=".xlsx" required style="padding:8px;">
                        </div>
                    </div>
                    <div class="modal-foot">
                        <a href="${ctx}/admin/exam-computer?action=template" class="btn-reset" style="height:44px;padding:0 1.25rem;display:inline-flex;align-items:center;text-decoration:none;">Tải biểu mẫu</a>
                        <button type="button" class="btn-reset" onclick="closeImportModal()" style="height:44px;padding:0 1.5rem;display:inline-flex;align-items:center;">Hủy</button>
                        <button type="submit" class="btn-filter" style="height:44px;padding:0 1.5rem;">Bắt đầu import</button>
                    </div>
                </form>
            </div>
        </div>

        <script>
            function openImportModal() {
                document.getElementById('importFile').value = '';
                document.getElementById('importModal').classList.add('is-open');
            }
            function closeImportModal() {
                document.getElementById('importModal').classList.remove('is-open');
            }
            
            var AREAS_BY_ZONE = {};
            <c:forEach var="a" items="${areas}">
            AREAS_BY_ZONE[${a.zoneId}] = AREAS_BY_ZONE[${a.zoneId}] || [];
            AREAS_BY_ZONE[${a.zoneId}].push({id:${a.areaId}, name: "${fn:escapeXml(a.areaName)} (${fn:escapeXml(a.areaType)})"});
            </c:forEach>

            function fillAreas(zid, selectedAreaId) {
                var sel = document.getElementById('d_area');
                sel.innerHTML = '';
                if (!zid) {
                    sel.disabled = true;
                    sel.innerHTML = '<option value="">-- Chọn khu vực trước --</option>';
                    return;
                }
                var list = AREAS_BY_ZONE[zid] || [];
                if (!list.length) {
                    sel.disabled = true;
                    sel.innerHTML = '<option value="">(Khu vực chưa có phòng/sân)</option>';
                    return;
                }
                sel.disabled = false;
                sel.innerHTML = '<option value="">-- Chọn phòng/sân --</option>';
                list.forEach(function (a) {
                    var o = document.createElement('option');
                    o.value = a.id;
                    o.textContent = a.name;
                    if (String(a.id) === String(selectedAreaId))
                        o.selected = true;
                    sel.appendChild(o);
                });
            }
            function onDevZoneChange() {
                var zid = document.getElementById('d_zone').value;
                fillAreas(zid, '');
                document.getElementById('devFields').classList.remove('show');
            }
            function onDevAreaChange() {
                var aid = document.getElementById('d_area').value;
                document.getElementById('devFields').classList.toggle('show', !!aid);
            }
            function openDevModal() {
                document.getElementById('devTitle').textContent = 'Thêm máy/thiết bị';
                document.getElementById('d_id').value = '';
                document.getElementById('d_zone').value = '';
                document.getElementById('d_name').value = '';
                document.getElementById('d_type').value = '';
                document.getElementById('d_status').value = 'active';
                fillAreas('', '');
                document.getElementById('devErr').classList.remove('show');
                document.getElementById('devFields').classList.remove('show');
                document.getElementById('devModal').classList.add('is-open');
            }
            function editDev(b) {
                document.getElementById('devTitle').textContent = 'Chỉnh sửa máy/thiết bị';
                document.getElementById('d_id').value = b.dataset.id;
                document.getElementById('d_zone').value = b.dataset.zone;
                fillAreas(b.dataset.zone, b.dataset.area);
                document.getElementById('d_name').value = b.dataset.name;
                document.getElementById('d_type').value = b.dataset.type;
                document.getElementById('d_status').value = (b.dataset.active === 'true') ? 'active' : 'inactive';
                document.getElementById('devFields').classList.add('show');
                document.getElementById('devErr').classList.remove('show');
                document.getElementById('devModal').classList.add('is-open');
            }
            function closeDevModal() {
                document.getElementById('devModal').classList.remove('is-open');
            }
            function delDev(id, name) {
                if (confirm('Xóa "' + name + '"?\nKhông thể hoàn tác.')) {
                    document.getElementById('delId').value = id;
                    document.getElementById('delForm').submit();
                }
            }
            function toggleDev(id, toActive) {
                document.getElementById('tgId').value = id;
                document.getElementById('tgActive').value = toActive ? 'true' : 'false';
                document.getElementById('toggleForm').submit();
            }
            document.addEventListener('keydown', e => {
                if (e.key === 'Escape') {
                    closeDevModal();
                    closeImportModal();
                }
            });

            /* ---------------- Xóa hàng loạt ---------------- */
            function selectedRows() {
                return Array.prototype.slice.call(document.querySelectorAll('.rowChk:checked'));
            }
            function updateSel() {
                var n = selectedRows().length;
                document.getElementById('selCount').textContent = n;
                document.getElementById('bulkDelBtn').disabled = (n === 0);
                var all = document.querySelectorAll('.rowChk');
                var head = document.getElementById('checkAll');
                if (head) {
                    head.checked = (all.length > 0 && n === all.length);
                }
            }
            function toggleAll(box) {
                document.querySelectorAll('.rowChk').forEach(function (c) {
                    c.checked = box.checked;
                });
                updateSel();
            }
            function submitBulk(scope, ids) {
                document.getElementById('bulkScope').value = scope;
                var box = document.getElementById('bulkIds');
                box.innerHTML = '';
                (ids || []).forEach(function (v) {
                    var i = document.createElement('input');
                    i.type = 'hidden';
                    i.name = 'ids';
                    i.value = v;
                    box.appendChild(i);
                });
                document.getElementById('bulkForm').submit();
            }
            function bulkDeleteSelected() {
                var rows = selectedRows();
                if (rows.length === 0) {
                    alert('Bạn chưa chọn máy/thiết bị nào.');
                    return;
                }
                var names = rows.slice(0, 5).map(function (c) {
                    return '• ' + c.getAttribute('data-name');
                }).join('\n');
                if (rows.length > 5) {
                    names += '\n• ... và ' + (rows.length - 5) + ' máy/thiết bị khác';
                }
                var msg = 'CẢNH BÁO - XÓA VĨNH VIỄN ' + rows.length + ' MÁY/THIẾT BỊ THI\n\n' + names +
                        '\n\nMáy đang gắn với kỳ thi sẽ được tự động bỏ qua.\n' +
                        'Hành động này KHÔNG THỂ hoàn tác. Bạn có chắc chắn muốn xóa?';
                if (confirm(msg)) {
                    submitBulk('', rows.map(function (c) {
                        return c.value;
                    }));
                }
            }
            function bulkDeleteFiltered() {
                var total = document.querySelectorAll('.rowChk').length;
                var f = [];
                var kw = document.querySelector('input[name="searchKeyword"]');
                var tp = document.querySelector('select[name="filterType"]');
                var zn = document.querySelector('select[name="filterZone"]');
                var ar = document.querySelector('select[name="filterArea"]');
                if (kw && kw.value.trim()) {
                    f.push('Từ khóa: "' + kw.value.trim() + '"');
                }
                if (tp && tp.value) {
                    f.push('Loại: ' + tp.options[tp.selectedIndex].text);
                }
                if (zn && zn.value) {
                    f.push('Khu vực: ' + zn.options[zn.selectedIndex].text);
                }
                if (ar && ar.value) {
                    f.push('Phòng/sân: ' + ar.options[ar.selectedIndex].text);
                }
                var scope = f.length ? ('theo bộ lọc — ' + f.join(' | ')) : 'TOÀN BỘ danh sách (không áp bộ lọc nào)';
                var msg = 'CẢNH BÁO - XÓA VĨNH VIỄN TẤT CẢ MÁY/THIẾT BỊ ĐANG HIỂN THỊ\n\n' +
                        'Phạm vi: ' + scope + '\nSố máy/thiết bị sẽ bị xóa: khoảng ' + total +
                        '\n\nMáy đang gắn với kỳ thi sẽ được tự động bỏ qua.\n' +
                        'Hành động này KHÔNG THỂ hoàn tác. Bạn có chắc chắn muốn xóa?';
                if (confirm(msg)) {
                    submitBulk('filtered', null);
                }
            }
            updateSel();
        </script>

        <c:if test="${sessionScope.reopenModal eq 'device'}">
            <script>
                document.getElementById('devTitle').textContent = '${sessionScope.f_mode eq "edit" ? "Chỉnh sửa máy/thiết bị" : "Thêm máy/thiết bị"}';
                document.getElementById('d_id').value = '${sessionScope.f_deviceId}';
                document.getElementById('d_zone').value = '${sessionScope.f_zoneId}';
                fillAreas('${sessionScope.f_zoneId}', '${sessionScope.f_areaId}');
                document.getElementById('d_name').value = "${fn:escapeXml(sessionScope.f_deviceName)}";
                document.getElementById('d_type').value = "${fn:escapeXml(sessionScope.f_deviceType)}";
                document.getElementById('d_status').value = '${sessionScope.f_active ? "active" : "inactive"}';
                document.getElementById('devFields').classList.add('show');
                var e = document.getElementById('devErr');
                e.textContent = "${fn:escapeXml(sessionScope.flashMessage)}";
                e.classList.add('show');
                document.getElementById('devModal').classList.add('is-open');
            </script>
            <c:remove var="reopenModal" scope="session" />
            <c:remove var="flashMessage" scope="session" />
            <c:remove var="flashType" scope="session" />
        </c:if>
    </body>
</html>
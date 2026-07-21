<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<c:set var="ctx" value="${pageContext.request.contextPath}" />
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Quản lý Khu vực thi - Lái Vui</title>
    <link href="https://fonts.googleapis.com/css2?family=Be+Vietnam+Pro:wght@400;500;600;700;800&family=Inter:wght@400;500;600;700;800&display=swap" rel="stylesheet">
    <link rel="stylesheet" href="${ctx}/assets/css/style.css">
    <link rel="stylesheet" href="${ctx}/assets/css/layout.css">
</head>
<body class="has-side-nav-bar">
<jsp:include page="/views/layout/sidebar-admin.jsp"><jsp:param name="activeSidebar" value="khu-vuc" /></jsp:include>
<div class="dashboard-shell">
    <main class="main-content">
        <nav class="breadcrumbs" aria-label="Breadcrumb">
            <a href="${ctx}/views/public/home.jsp">Trang chủ</a><span class="breadcrumbs__separator">/</span>
            <a href="${ctx}/admin/dashboard">Quản trị</a><span class="breadcrumbs__separator">/</span>
            <span class="breadcrumbs__current" aria-current="page">Khu vực thi</span>
        </nav>

<%-- Chỉ hiển thị thông báo này ngoài màn hình nếu ĐÂY KHÔNG PHẢI là lỗi từ form modal --%>
<c:if test="${not empty sessionScope.flashMessage && sessionScope.reopenModal ne 'zone'}">
    <div style="margin-bottom:1.25rem;padding:.85rem 1.1rem;border-radius:10px;font-weight:600;font-size:.9rem;
                background:${sessionScope.flashType eq 'success' ? 'rgba(16,185,129,.08)' : 'rgba(239,68,68,.08)'};
                border:1px solid ${sessionScope.flashType eq 'success' ? 'rgba(16,185,129,.25)' : 'rgba(239,68,68,.25)'};
                color:${sessionScope.flashType eq 'success' ? '#047857' : '#b91c1c'};">${sessionScope.flashMessage}</div>
    <c:remove var="flashMessage" scope="session" /><c:remove var="flashType" scope="session" />
</c:if>

        <header class="page-header">
            <div class="page-title-wrap">
                <h1 class="page-title">Quản lý Khu vực thi</h1>
                <p class="page-subtitle">Khu vực thi (khuôn viên / địa điểm) chứa các phòng thi, sân thi bên trong.</p>
            </div>
            <div class="page-actions" style="display:flex;gap:10px;">
                <button type="button" class="btn-filter" onclick="openZoneModal()" style="height:42px;padding:0 1.25rem;border-radius:8px;cursor:pointer;">+ Thêm khu vực</button>
            </div>
        </header>

        <section class="filter-panel">
            <h2 class="filter-title">Bộ lọc tìm kiếm</h2>
            <form action="${ctx}/admin/exam-area" method="GET">
                <div class="filter-grid" style="grid-template-columns:2.5fr 1.5fr 1.5fr;">
                    <div class="input-group"><label class="input-label">Tìm khu vực</label>
                        <input type="text" name="searchKeyword" class="input-field" placeholder="Tên khu vực hoặc địa điểm..." value="${param.searchKeyword}"></div>
                    <div class="input-group"><label class="input-label">Trạng thái</label>
                        <select name="filterStatus" class="input-field">
                            <option value="">Tất cả</option>
                            <option value="active" ${param.filterStatus eq 'active' ? 'selected' : ''}>Đang hoạt động</option>
                            <option value="inactive" ${param.filterStatus eq 'inactive' ? 'selected' : ''}>Ngừng</option>
                        </select></div>
                    <div class="input-group filter-grid__btn-col"><div class="btn-group">
                        <button type="submit" class="btn-filter">Lọc</button>
                        <a href="${ctx}/admin/exam-area" class="btn-reset">Đặt lại</a>
                    </div></div>
                </div>
            </form>
        </section>

        <section class="log-card">
            <header class="log-card-header"><h2 class="log-card-title">Danh sách khu vực thi
                <c:if test="${not empty zones}"><span style="font-size:.78rem;font-weight:600;background:rgba(0,82,204,.08);color:#0052cc;padding:2px 10px;border-radius:9999px;margin-left:6px;">${fn:length(zones)} khu vực</span></c:if>
            </h2></header>
            <div class="table-responsive"><table class="audit-table">
                <thead><tr>
                    <th class="col-id">STT</th><th style="width:110px;">Mã</th><th>Tên khu vực</th>
                    <th>Địa điểm</th><th style="width:110px;text-align:center;">Số phòng/sân</th>
                    <th style="width:120px;text-align:center;">Trạng thái</th><th style="width:160px;text-align:center;">Thao tác</th>
                </tr></thead>
                <tbody>
                <c:choose>
                    <c:when test="${not empty zones}">
                        <c:forEach var="z" items="${zones}" varStatus="st">
                            <tr>
                                <td class="col-id">${st.index+1}</td>
                                <td style="font-weight:700;color:#0052cc;font-family:monospace;">${z.code}</td>
                                <td><span class="user-name" style="font-weight:600;color:#0f172a;">${z.zoneName}</span></td>
                                <td class="details-cell">${z.location}</td>
                                <td style="text-align:center;font-weight:700;">${z.areaCount}</td>
                                <td style="text-align:center;">
                                    <c:choose><c:when test="${z.active}"><span class="action-badge action-badge--success">Hoạt động</span></c:when>
                                    <c:otherwise><span class="action-badge action-badge--danger">Ngừng</span></c:otherwise></c:choose>
                                </td>
                                <td><div style="display:flex;gap:6px;justify-content:center;">
                                    <button type="button" class="btn-export" style="padding:4px 10px;font-size:.8rem;border-color:rgba(245,158,11,.25);color:#d97706;cursor:pointer;"
                                        data-id="${z.zoneId}" data-name="${fn:escapeXml(z.zoneName)}" data-location="${fn:escapeXml(z.location)}" data-active="${z.active}"
                                        onclick="editZone(this)">Sửa</button>
                                    <button type="button" class="btn-export" style="padding:4px 10px;font-size:.8rem;border-color:rgba(239,68,68,.25);color:#dc2626;cursor:pointer;"
                                        onclick="delZone('${z.zoneId}','${fn:escapeXml(z.zoneName)}')">Xóa</button>
                                </div></td>
                            </tr>
                        </c:forEach>
                    </c:when>
                    <c:otherwise><tr><td colspan="7" style="text-align:center;padding:4rem 1.5rem;color:#64748b;">Chưa có khu vực thi nào. Nhấn <b>Thêm khu vực</b> để bắt đầu.</td></tr></c:otherwise>
                </c:choose>
                </tbody>
            </table></div>
        </section>
    </main>
    <jsp:include page="/views/layout/footer.jsp"><jsp:param name="standalone" value="false" /></jsp:include>
</div>

<form id="delForm" action="${ctx}/admin/exam-area" method="POST" style="display:none;">
    <input type="hidden" name="action" value="delete"><input type="hidden" name="id" id="delId"></form>

<style>
.modal-overlay{display:none;position:fixed;inset:0;z-index:1000;background:rgba(15,23,42,.45);align-items:flex-start;justify-content:center;padding:4vh 1rem;overflow-y:auto;}
.modal-overlay.is-open{display:flex;}
.modal-card{width:100%;max-width:560px;background:#fff;border-radius:16px;box-shadow:0 20px 60px rgba(15,23,42,.25);}
.modal-head{display:flex;align-items:center;justify-content:space-between;padding:1.25rem 1.5rem;border-bottom:1px solid #e2e8f0;}
.modal-head h3{margin:0;font-size:1.1rem;font-weight:800;color:#0f172a;}
.modal-close{border:none;background:transparent;font-size:1.5rem;color:#94a3b8;cursor:pointer;}
.modal-body{padding:1.5rem;}
.modal-foot{display:flex;gap:12px;justify-content:flex-end;padding:1rem 1.5rem;border-top:1px solid #e2e8f0;}
.modal-err{display:none;margin-bottom:1rem;padding:.7rem .9rem;border-radius:8px;background:rgba(239,68,68,.08);border:1px solid rgba(239,68,68,.25);color:#b91c1c;font-size:.85rem;font-weight:600;}
.modal-err.show{display:block;}
</style>

<div id="zoneModal" class="modal-overlay" onclick="if(event.target===this)closeZoneModal()">
    <div class="modal-card">
        <form action="${ctx}/admin/exam-area" method="POST">
            <div class="modal-head"><h3 id="zoneTitle">Thêm khu vực thi</h3><button type="button" class="modal-close" onclick="closeZoneModal()">&times;</button></div>
            <div class="modal-body">
                <div id="zoneErr" class="modal-err"></div>
                <input type="hidden" name="zoneId" id="z_id" value="">
                <div class="input-group" style="margin-bottom:1.25rem;"><label class="input-label">Tên khu vực <span style="color:#dc2626;">*</span></label>
                    <input type="text" id="z_name" name="zoneName" class="input-field" maxlength="100" placeholder="VD: Trung tâm sát hạch Hà Nội" required></div>
                <div class="input-group" style="margin-bottom:1.25rem;"><label class="input-label">Địa điểm <span style="color:#dc2626;">*</span></label>
                    <input type="text" id="z_location" name="location" class="input-field" maxlength="255" placeholder="VD: Số 1 Lê Lợi, Hoàn Kiếm, Hà Nội" required></div>
                <div class="input-group"><label class="input-label">Trạng thái</label>
                    <select id="z_status" name="status" class="input-field"><option value="active">Hoạt động</option><option value="inactive">Ngừng</option></select></div>
            </div>
            <div class="modal-foot"><button type="button" class="btn-reset" onclick="closeZoneModal()" style="height:44px;padding:0 1.5rem;display:inline-flex;align-items:center;">Hủy</button>
                <button type="submit" class="btn-filter" style="height:44px;padding:0 1.5rem;">Lưu khu vực</button></div>
        </form>
    </div>
</div>

<script>
function openZoneModal(){document.getElementById('zoneTitle').textContent='Thêm khu vực thi';
    ['z_id','z_name','z_location'].forEach(k=>document.getElementById(k).value='');
    document.getElementById('z_status').value='active';document.getElementById('zoneErr').classList.remove('show');
    document.getElementById('zoneModal').classList.add('is-open');}
function editZone(b){document.getElementById('zoneTitle').textContent='Chỉnh sửa khu vực thi';
    document.getElementById('z_id').value=b.dataset.id;document.getElementById('z_name').value=b.dataset.name;
    document.getElementById('z_location').value=b.dataset.location;document.getElementById('z_status').value=(b.dataset.active==='true')?'active':'inactive';
    document.getElementById('zoneErr').classList.remove('show');document.getElementById('zoneModal').classList.add('is-open');}
function closeZoneModal(){document.getElementById('zoneModal').classList.remove('is-open');}
function delZone(id,name){if(confirm('Xóa khu vực "'+name+'"?\nKhông thể hoàn tác.')){document.getElementById('delId').value=id;document.getElementById('delForm').submit();}}
document.addEventListener('keydown',e=>{if(e.key==='Escape')closeZoneModal();});
</script>

<c:if test="${sessionScope.reopenModal eq 'zone'}">
<script>
    // Đợi DOM tải xong để gán giá trị chính xác
    document.addEventListener("DOMContentLoaded", function() {
        document.getElementById('zoneTitle').textContent = '${sessionScope.f_mode eq "edit" ? "Chỉnh sửa khu vực thi" : "Thêm khu vực thi"}';
        document.getElementById('z_id').value = '${sessionScope.f_zoneId}';
        document.getElementById('z_name').value = "${fn:escapeXml(sessionScope.f_zoneName)}";
        document.getElementById('z_location').value = "${fn:escapeXml(sessionScope.f_location)}";
        document.getElementById('z_status').value = '${sessionScope.f_active ? "active" : "inactive"}';
        
        var errorDiv = document.getElementById('zoneErr');
        if (errorDiv) {
            errorDiv.textContent = "${fn:escapeXml(sessionScope.flashMessage)}";
            errorDiv.classList.add('show');
        }
        document.getElementById('zoneModal').classList.add('is-open');
    });
</script>
<%-- Xóa các session flag để không bị lặp lại ở lần tải trang sau --%>
<c:remove var="reopenModal" scope="session" />
<c:remove var="flashMessage" scope="session" />
<c:remove var="flashType" scope="session" />
</c:if>

</body></html>

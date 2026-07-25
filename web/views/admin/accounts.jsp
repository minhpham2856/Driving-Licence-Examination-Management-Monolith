<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<c:set var="ctx" value="${pageContext.request.contextPath}" />
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8"><meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Quản lý Tài khoản - Lái Vui</title>
    <link href="https://fonts.googleapis.com/css2?family=Be+Vietnam+Pro:wght@400;500;600;700;800&family=Inter:wght@400;500;600;700;800&display=swap" rel="stylesheet">
    <link rel="stylesheet" href="${ctx}/assets/css/style.css"><link rel="stylesheet" href="${ctx}/assets/css/layout.css">
</head>
<body class="has-side-nav-bar">
<jsp:include page="/views/layout/sidebar-admin.jsp"><jsp:param name="activeSidebar" value="tai-khoan" /></jsp:include>
<div class="dashboard-shell"><main class="main-content">
    <nav class="breadcrumbs"><a href="${ctx}/views/public/home.jsp">Trang chủ</a><span class="breadcrumbs__separator">/</span>
        <a href="${ctx}/admin/dashboard">Quản trị</a><span class="breadcrumbs__separator">/</span>
        <span class="breadcrumbs__current" aria-current="page">Tài khoản</span></nav>

    <c:if test="${not empty sessionScope.flashMessage}">
        <c:choose>
            <c:when test="${sessionScope.flashType eq 'success'}"><c:set var="flashBg" value="rgba(16,185,129,.08)"/><c:set var="flashBd" value="rgba(16,185,129,.25)"/><c:set var="flashFg" value="#047857"/></c:when>
            <c:when test="${sessionScope.flashType eq 'warning'}"><c:set var="flashBg" value="#fffbeb"/><c:set var="flashBd" value="#fcd34d"/><c:set var="flashFg" value="#92400e"/></c:when>
            <c:otherwise><c:set var="flashBg" value="rgba(239,68,68,.08)"/><c:set var="flashBd" value="rgba(239,68,68,.25)"/><c:set var="flashFg" value="#b91c1c"/></c:otherwise>
        </c:choose>
        <div style="margin-bottom:1.25rem;padding:.85rem 1.1rem;border-radius:10px;font-weight:600;font-size:.9rem;
            background:${flashBg};border:1px solid ${flashBd};color:${flashFg};">${sessionScope.flashMessage}</div>
        <c:remove var="flashMessage" scope="session" /><c:remove var="flashType" scope="session" /></c:if>

    <header class="page-header"><div class="page-title-wrap">
        <h1 class="page-title">Quản lý Tài khoản hệ thống</h1></div>
        <div class="page-actions" style="display:flex;gap:10px;flex-wrap:wrap;">
            <a href="${ctx}/admin/accounts?action=template" class="btn-export" style="height:42px;padding:0 1.25rem;border-radius:8px;display:inline-flex;align-items:center;text-decoration:none;">Tải biểu mẫu Excel</a>
            <button type="button" class="btn-export" onclick="openImportModal()" style="height:42px;padding:0 1.25rem;border-radius:8px;cursor:pointer;">Import tài khoản</button>
            <button type="button" class="btn-filter" onclick="openAccModal()" style="height:42px;padding:0 1.25rem;border-radius:8px;cursor:pointer;">+ Tạo tài khoản mới</button></div>
    </header>

    <section class="metrics-row">
        <div class="stat-card"><div class="stat-icon stat-icon--blue"><svg width="24" height="24" viewBox="0 0 24 24" fill="none"><path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2M9 11a4 4 0 1 0 0-8 4 4 0 0 0 0 8z" stroke="currentColor" stroke-width="2"/></svg></div>
            <div class="stat-info"><span class="stat-number">${empty totalAccounts ? 0 : totalAccounts}</span><span class="stat-label">Tổng số tài khoản</span><span class="stat-trend stat-trend--up">Toàn hệ thống</span></div></div>
        <div class="stat-card"><div class="stat-icon stat-icon--blue" style="background:rgba(0,82,204,.08);color:#0052cc;"><svg width="24" height="24" viewBox="0 0 24 24" fill="none"><path d="M12 22s8-4 8-10V5l-8-3-8 3v7c0 6 8 10 8 10z" stroke="currentColor" stroke-width="2"/></svg></div>
            <div class="stat-info"><span class="stat-number">${empty adminCount ? 0 : adminCount}</span><span class="stat-label">Admin hệ thống</span><span class="stat-trend stat-trend--up" style="color:#0052cc;">Quản trị tối cao</span></div></div>
        <div class="stat-card"><div class="stat-icon stat-icon--blue" style="background:rgba(13,148,136,.08);color:#0d9488;"><svg width="24" height="24" viewBox="0 0 24 24" fill="none"><path d="M2 22s2-4 10-4 10 4 10 4M12 11a4 4 0 1 0 0-8 4 4 0 0 0 0 8z" stroke="currentColor" stroke-width="2"/></svg></div>
            <div class="stat-info"><span class="stat-number">${empty coiThiCount ? 0 : coiThiCount}</span><span class="stat-label">Cán bộ coi thi</span><span class="stat-trend stat-trend--up" style="color:#0d9488;">Giám sát phòng thi</span></div></div>
        <div class="stat-card"><div class="stat-icon stat-icon--blue" style="background:rgba(124,58,237,.08);color:#7c3aed;"><svg width="24" height="24" viewBox="0 0 24 24" fill="none"><path d="M12 20h9M3 20v-8a2 2 0 0 1 2-2h6a2 2 0 0 1 2 2v8" stroke="currentColor" stroke-width="2"/></svg></div>
            <div class="stat-info"><span class="stat-number">${empty chamThiCount ? 0 : chamThiCount}</span><span class="stat-label">Giám khảo chấm thi</span><span class="stat-trend stat-trend--up" style="color:#7c3aed;">Đánh giá sát hạch</span></div></div>
    </section>

    <section class="filter-panel"><h2 class="filter-title">Bộ lọc tìm kiếm</h2>
        <form action="${ctx}/admin/accounts" method="GET"><div class="filter-grid" style="grid-template-columns:2fr 1.25fr 1.25fr 1.5fr;">
            <div class="input-group"><label class="input-label">Tìm kiếm tài khoản</label>
                <input type="text" name="searchKeyword" class="input-field" placeholder="Tên đăng nhập, họ tên, email, sđt..." value="${param.searchKeyword}"></div>
            <div class="input-group"><label class="input-label">Vai trò</label>
                <select name="filterRole" class="input-field"><option value="">Tất cả vai trò</option>
                    <c:forEach var="r" items="${roles}"><option value="${r.roleId}" ${param.filterRole eq r.roleId ? 'selected' : ''}>${r.roleName}</option></c:forEach>
                </select></div>
            <div class="input-group"><label class="input-label">Trạng thái</label>
                <select name="filterStatus" class="input-field"><option value="">Tất cả</option>
                    <option value="active" ${param.filterStatus eq 'active' ? 'selected' : ''}>Đang hoạt động</option>
                    <option value="inactive" ${param.filterStatus eq 'inactive' ? 'selected' : ''}>Khóa / Vô hiệu</option></select></div>
            <div class="input-group filter-grid__btn-col"><div class="btn-group">
                <button type="submit" class="btn-filter">Lọc</button><a href="${ctx}/admin/accounts" class="btn-reset">Đặt lại</a></div></div>
        </div></form>
    </section>

    <section class="log-card"><header class="log-card-header"><h2 class="log-card-title">Danh sách tài khoản hệ thống
        <c:if test="${not empty accounts}"><span style="font-size:.78rem;font-weight:600;background:rgba(0,82,204,.08);color:#0052cc;padding:2px 10px;border-radius:9999px;margin-left:6px;">${fn:length(accounts)} tài khoản</span></c:if>
    </h2><div class="log-card-actions" style="display:flex;gap:10px;align-items:center;">
        <button type="button" id="bulkDelBtn" class="btn-export" onclick="bulkDeleteSelected()" disabled
                style="cursor:pointer;border-color:rgba(239,68,68,.35);color:#dc2626;">Xóa mục đã chọn (<span id="selCount">0</span>)</button>
        <c:if test="${not empty accounts}">
            <button type="button" class="btn-export" onclick="bulkDeleteFiltered()" style="cursor:pointer;border-color:rgba(239,68,68,.35);color:#dc2626;">Xóa toàn bộ kết quả lọc</button>
        </c:if>
        <a href="${ctx}/admin/accounts?action=export&amp;searchKeyword=${fn:escapeXml(param.searchKeyword)}&amp;filterRole=${fn:escapeXml(param.filterRole)}&amp;filterStatus=${fn:escapeXml(param.filterStatus)}"
           class="btn-export" style="display:inline-flex;align-items:center;text-decoration:none;">Xuất Excel danh sách</a>
    </div></header>
    <div class="table-responsive"><table class="audit-table">
        <thead><tr>
            <th style="width:42px;text-align:center;"><input type="checkbox" id="checkAll" onclick="toggleAll(this)" title="Chọn tất cả (trừ tài khoản quản trị)"></th>
            <th class="col-id">STT</th><th style="min-width:200px;">Tên tài khoản</th><th style="min-width:180px;">Email &amp; SĐT</th>
            <th style="width:150px;text-align:center;">Vai trò</th><th style="min-width:150px;">Trung tâm / Đơn vị</th>
            <th style="width:120px;text-align:center;">Trạng thái</th><th style="text-align:center;width:260px;">Thao tác</th></tr></thead>
        <tbody>
        <c:choose><c:when test="${not empty accounts}">
            <c:forEach var="acc" items="${accounts}" varStatus="st"><tr>
                <td style="text-align:center;">
                    <c:choose>
                        <c:when test="${acc.roleCode eq 'admin'}"><span title="Không thể xóa tài khoản quản trị" style="color:#cbd5e1;">&mdash;</span></c:when>
                        <c:otherwise><input type="checkbox" class="rowChk" value="${acc.id}" data-name="${fn:escapeXml(acc.fullName)}" onclick="updateSel()"></c:otherwise>
                    </c:choose>
                </td>
                <td class="col-id">${st.index+1}</td>
                <td><div class="user-cell">
                    <c:choose>
                        <c:when test="${acc.roleCode eq 'admin'}"><div class="user-avatar">${fn:substring(acc.fullName,0,1)}</div></c:when>
                        <c:when test="${acc.roleCode eq 'coi_thi'}"><div class="user-avatar user-avatar--teal">${fn:substring(acc.fullName,0,1)}</div></c:when>
                        <c:when test="${acc.roleCode eq 'cham_thi'}"><div class="user-avatar user-avatar--purple">${fn:substring(acc.fullName,0,1)}</div></c:when>
                        <c:otherwise><div class="user-avatar user-avatar--orange">${fn:substring(acc.fullName,0,1)}</div></c:otherwise>
                    </c:choose>
                    <div class="user-info"><span class="user-name" style="font-weight:600;color:#0f172a;">${acc.fullName}</span><span class="user-username">@${acc.username}</span></div>
                </div></td>
                <td><div style="font-weight:500;color:#334155;font-size:.88rem;">${acc.email}</div><div style="font-size:.75rem;color:#64748b;margin-top:2px;">${acc.phone}</div></td>
                <td style="text-align:center;">
                    <c:choose>
                        <c:when test="${acc.roleCode eq 'admin'}"><span class="role-badge role-badge--admin">${acc.role}</span></c:when>
                        <c:when test="${acc.roleCode eq 'coi_thi'}"><span class="role-badge role-badge--coi">${acc.role}</span></c:when>
                        <c:when test="${acc.roleCode eq 'cham_thi'}"><span class="role-badge role-badge--cham">${acc.role}</span></c:when>
                        <c:otherwise><span class="role-badge role-badge--other">${acc.role}</span></c:otherwise>
                    </c:choose>
                </td>
                <td><span style="font-weight:500;color:#475569;font-size:.88rem;">${acc.department}</span></td>
                <td style="text-align:center;"><c:choose>
                    <c:when test="${acc.status eq 'active'}"><span class="action-badge action-badge--success">Hoạt động</span></c:when>
                    <c:otherwise><span class="action-badge action-badge--danger">Khóa / Vô hiệu</span></c:otherwise></c:choose></td>
                <td><div style="display:flex;gap:6px;justify-content:center;flex-wrap:wrap;">
                    <%-- Không cho khóa/xóa tài khoản Quản trị viên (kể cả chính mình) --%>
                    <c:choose>
                        <c:when test="${acc.roleCode eq 'admin'}">
                            <button type="button" class="btn-export" style="padding:4px 10px;font-size:.8rem;border-color:rgba(2,132,199,.25);color:#0284c7;cursor:pointer;" onclick="resetAccount('${acc.id}','${fn:escapeXml(acc.username)}')">Cấp lại MK</button>
                            <span style="font-size:.75rem;color:#94a3b8;align-self:center;">Tài khoản quản trị</span>
                        </c:when>
                        <c:otherwise>
                            <c:choose>
                                <c:when test="${acc.status eq 'active'}"><button type="button" class="btn-export" style="padding:4px 10px;font-size:.8rem;border-color:rgba(239,68,68,.25);color:#dc2626;cursor:pointer;" onclick="lockAccount('${acc.id}','${fn:escapeXml(acc.fullName)}',true)">Khóa</button></c:when>
                                <c:otherwise><button type="button" class="btn-export" style="padding:4px 10px;font-size:.8rem;border-color:rgba(16,185,129,.25);color:#059669;cursor:pointer;" onclick="lockAccount('${acc.id}','${fn:escapeXml(acc.fullName)}',false)">Mở khóa</button></c:otherwise>
                            </c:choose>
                            <button type="button" class="btn-export" style="padding:4px 10px;font-size:.8rem;border-color:rgba(2,132,199,.25);color:#0284c7;cursor:pointer;" onclick="resetAccount('${acc.id}','${fn:escapeXml(acc.username)}')">Cấp lại MK</button>
                            <button type="button" class="btn-export" style="padding:4px 10px;font-size:.8rem;border-color:rgba(100,116,139,.25);color:#475569;cursor:pointer;" onclick="deleteAccount('${acc.id}','${fn:escapeXml(acc.fullName)}')">Xóa</button>
                        </c:otherwise>
                    </c:choose>
                </div></td>
            </tr></c:forEach>
        </c:when><c:otherwise><tr><td colspan="8" style="text-align:center;padding:5rem 1.5rem;color:#64748b;">Chưa có tài khoản nào. Nhấn <b>Tạo tài khoản mới</b>.</td></tr></c:otherwise></c:choose>
        </tbody>
    </table></div>
    <footer class="pagination-footer"><div class="pagination-info">Hiển thị <c:choose><c:when test="${not empty accounts}">1 - ${fn:length(accounts)}</c:when><c:otherwise>0</c:otherwise></c:choose> trong tổng số ${empty totalAccounts ? 0 : totalAccounts} tài khoản</div>
        <div class="pagination-nav"><button class="page-btn page-btn--wide disabled" disabled>Trước</button><button class="page-btn active">1</button><button class="page-btn page-btn--wide disabled" disabled>Sau</button></div></footer>
    </section>
</main>
<jsp:include page="/views/layout/footer.jsp"><jsp:param name="standalone" value="false" /></jsp:include></div>

<form id="lockForm" action="${ctx}/admin/accounts" method="POST" style="display:none;"><input type="hidden" name="action" value="lock"><input type="hidden" name="id" id="lockId"><input type="hidden" name="lock" id="lockVal"></form>
<form id="resetForm" action="${ctx}/admin/accounts" method="POST" style="display:none;"><input type="hidden" name="action" value="reset"><input type="hidden" name="id" id="resetId"></form>
<form id="delAccForm" action="${ctx}/admin/accounts" method="POST" style="display:none;"><input type="hidden" name="action" value="delete"><input type="hidden" name="id" id="delAccId"></form>

<%-- Xóa hàng loạt: gửi danh sách id đã tick, hoặc scope=filtered kèm bộ lọc hiện tại --%>
<form id="bulkForm" action="${ctx}/admin/accounts" method="POST" style="display:none;">
    <input type="hidden" name="action" value="bulkDelete">
    <input type="hidden" name="scope" id="bulkScope" value="">
    <input type="hidden" name="searchKeyword" value="${fn:escapeXml(param.searchKeyword)}">
    <input type="hidden" name="filterRole" value="${fn:escapeXml(param.filterRole)}">
    <input type="hidden" name="filterStatus" value="${fn:escapeXml(param.filterStatus)}">
    <div id="bulkIds"></div>
</form>

<style>
.modal-overlay{display:none;position:fixed;inset:0;z-index:1000;background:rgba(15,23,42,.45);align-items:flex-start;justify-content:center;padding:4vh 1rem;overflow-y:auto;}
.modal-overlay.is-open{display:flex;}.modal-card{width:100%;max-width:680px;background:#fff;border-radius:16px;box-shadow:0 20px 60px rgba(15,23,42,.25);}
.modal-head{display:flex;align-items:center;justify-content:space-between;padding:1.25rem 1.5rem;border-bottom:1px solid #e2e8f0;}.modal-head h3{margin:0;font-size:1.1rem;font-weight:800;color:#0f172a;}
.modal-close{border:none;background:transparent;font-size:1.5rem;color:#94a3b8;cursor:pointer;}.modal-body{padding:1.5rem;}
.modal-foot{display:flex;gap:12px;justify-content:flex-end;padding:1rem 1.5rem;border-top:1px solid #e2e8f0;}
</style>

<div id="accModal" class="modal-overlay" onclick="if(event.target===this)closeAccModal()"><div class="modal-card">
    <form action="${ctx}/admin/accounts" method="POST">
        <div class="modal-head"><h3>Tạo tài khoản mới</h3><button type="button" class="modal-close" onclick="closeAccModal()">&times;</button></div>
        <div class="modal-body">
            <div style="margin-bottom:1.1rem;padding:.6rem .9rem;border-radius:8px;background:#eff6ff;border:1px solid #bfdbfe;color:#1e40af;font-size:.82rem;">Mật khẩu tạm <b>6 số</b> do hệ thống sinh và <b>gửi thẳng về email</b> của tài khoản. Người dùng bắt buộc đổi ở lần đăng nhập đầu.</div>
            <div class="filter-grid" style="grid-template-columns:1fr 1fr;gap:1.25rem;margin-bottom:1.25rem;">
                <div class="input-group"><label class="input-label">Tên đăng nhập <span style="color:#dc2626;">*</span></label><input type="text" id="a_username" name="username" class="input-field" maxlength="50" required></div>
                <div class="input-group"><label class="input-label">Vai trò <span style="color:#dc2626;">*</span></label>
                    <select id="a_role" name="role" class="input-field" required><option value="">-- Chọn vai trò --</option>
                        <c:forEach var="r" items="${roles}"><option value="${r.roleId}">${r.roleName}</option></c:forEach>
                    </select></div>
            </div>
            <div class="input-group" style="margin-bottom:1.25rem;"><label class="input-label">Email <span style="color:#dc2626;">*</span></label><input type="email" id="a_email" name="email" class="input-field" maxlength="255" required></div>
            <div class="filter-grid" style="grid-template-columns:1fr 1fr;gap:1.25rem;margin-bottom:1.25rem;">
                <div class="input-group"><label class="input-label">Họ và tên <span style="color:#dc2626;">*</span></label><input type="text" id="a_fullName" name="fullName" class="input-field" maxlength="255" required></div>
                <div class="input-group"><label class="input-label">Số điện thoại <span style="color:#dc2626;">*</span></label><input type="text" id="a_phone" name="phone" class="input-field" maxlength="10" required></div>
            </div>
            <div class="filter-grid" style="grid-template-columns:1fr 1fr 1fr;gap:1.25rem;margin-bottom:1.25rem;">
                <div class="input-group"><label class="input-label">Ngày sinh <span style="color:#dc2626;">*</span></label><input type="date" id="a_dob" name="dateOfBirth" class="input-field" required></div>
                <div class="input-group"><label class="input-label">Giới tính <span style="color:#dc2626;">*</span></label><select id="a_sex" name="sex" class="input-field" required><option value="">--</option><option value="Nam">Nam</option><option value="Nữ">Nữ</option></select></div>
                <div class="input-group"><label class="input-label">Trạng thái</label><select id="a_status" name="status" class="input-field"><option value="active">Hoạt động</option><option value="inactive">Khóa / Vô hiệu</option></select></div>
            </div>
            <div class="filter-grid" style="grid-template-columns:1fr 1.4fr;gap:1.25rem;">
                <div class="input-group"><label class="input-label">Số CCCD/CMND <span style="color:#dc2626;">*</span></label><input type="text" id="a_govId" name="govId" class="input-field" maxlength="12" required></div>
                <div class="input-group"><label class="input-label">Địa chỉ</label><input type="text" id="a_address" name="address" class="input-field" maxlength="500"></div>
            </div>
        </div>
        <div class="modal-foot"><button type="button" class="btn-reset" onclick="closeAccModal()" style="height:44px;padding:0 1.5rem;display:inline-flex;align-items:center;">Hủy bỏ</button>
            <button type="submit" class="btn-filter" style="height:44px;padding:0 1.5rem;">Tạo tài khoản</button></div>
    </form>
</div></div>

<div id="importModal" class="modal-overlay" onclick="if(event.target===this)closeImportModal()"><div class="modal-card" style="max-width:560px;">
    <form action="${ctx}/admin/accounts" method="POST" enctype="multipart/form-data">
        <input type="hidden" name="action" value="import">
        <div class="modal-head"><h3>Import tài khoản từ Excel</h3><button type="button" class="modal-close" onclick="closeImportModal()">&times;</button></div>
        <div class="modal-body">
            <div style="margin-bottom:1.1rem;padding:.7rem .9rem;border-radius:8px;background:#eff6ff;border:1px solid #bfdbfe;color:#1e40af;font-size:.82rem;line-height:1.6;">
                Dùng đúng <b>biểu mẫu Excel (.xlsx)</b> tải từ nút <b>Tải biểu mẫu Excel</b>. Mỗi tài khoản được sinh
                <b>mật khẩu tạm 6 số</b> và gửi về email tương ứng. Dòng có dữ liệu sai sẽ bị bỏ qua và báo lại chi tiết.
            </div>
            <div class="input-group">
                <label class="input-label">Chọn file Excel <span style="color:#dc2626;">*</span></label>
                <input type="file" id="importFile" name="file" class="input-field" accept=".xlsx" required style="padding:8px;">
            </div>
        </div>
        <div class="modal-foot">
            <a href="${ctx}/admin/accounts?action=template" class="btn-reset" style="height:44px;padding:0 1.25rem;display:inline-flex;align-items:center;text-decoration:none;">Tải biểu mẫu</a>
            <button type="button" class="btn-reset" onclick="closeImportModal()" style="height:44px;padding:0 1.5rem;display:inline-flex;align-items:center;">Hủy bỏ</button>
            <button type="submit" class="btn-filter" style="height:44px;padding:0 1.5rem;">Bắt đầu import</button>
        </div>
    </form>
</div></div>

<script>
function openAccModal(){['a_username','a_email','a_role','a_fullName','a_phone','a_dob','a_sex','a_govId','a_address'].forEach(k=>document.getElementById(k).value='');
    document.getElementById('a_status').value='active';document.getElementById('accModal').classList.add('is-open');}
function closeAccModal(){document.getElementById('accModal').classList.remove('is-open');}
function openImportModal(){document.getElementById('importFile').value='';document.getElementById('importModal').classList.add('is-open');}
function closeImportModal(){document.getElementById('importModal').classList.remove('is-open');}
function lockAccount(id,name,lock){var m=lock?('Khóa tài khoản của "'+name+'"?'):('Mở khóa tài khoản của "'+name+'"?');
    if(confirm(m)){document.getElementById('lockId').value=id;document.getElementById('lockVal').value=lock?'true':'false';document.getElementById('lockForm').submit();}}
function resetAccount(id,u){if(confirm('Cấp lại mật khẩu 6 số cho "'+u+'" và gửi về email của tài khoản này?')){document.getElementById('resetId').value=id;document.getElementById('resetForm').submit();}}
function deleteAccount(id,name){if(confirm('Xóa vĩnh viễn tài khoản của "'+name+'"?')){document.getElementById('delAccId').value=id;document.getElementById('delAccForm').submit();}}
document.addEventListener('keydown',e=>{if(e.key==='Escape'){closeAccModal();closeImportModal();}});

/* ---------------- Xóa hàng loạt ---------------- */
function selectedRows(){return Array.prototype.slice.call(document.querySelectorAll('.rowChk:checked'));}
function updateSel(){
    var n=selectedRows().length;
    document.getElementById('selCount').textContent=n;
    document.getElementById('bulkDelBtn').disabled=(n===0);
    var all=document.querySelectorAll('.rowChk');
    var head=document.getElementById('checkAll');
    if(head){head.checked=(all.length>0&&n===all.length);}
}
function toggleAll(box){
    document.querySelectorAll('.rowChk').forEach(function(c){c.checked=box.checked;});
    updateSel();
}
function submitBulk(scope,ids){
    document.getElementById('bulkScope').value=scope;
    var box=document.getElementById('bulkIds');
    box.innerHTML='';
    (ids||[]).forEach(function(v){
        var i=document.createElement('input');i.type='hidden';i.name='ids';i.value=v;box.appendChild(i);
    });
    document.getElementById('bulkForm').submit();
}
function bulkDeleteSelected(){
    var rows=selectedRows();
    if(rows.length===0){alert('Bạn chưa chọn tài khoản nào.');return;}
    var names=rows.slice(0,5).map(function(c){return '• '+c.getAttribute('data-name');}).join('\n');
    if(rows.length>5){names+='\n• ... và '+(rows.length-5)+' tài khoản khác';}
    var msg='CẢNH BÁO - XÓA VĨNH VIỄN '+rows.length+' TÀI KHOẢN\n\n'+names+
            '\n\nHành động này KHÔNG THỂ hoàn tác. Bạn có chắc chắn muốn xóa?';
    if(confirm(msg)){submitBulk('',rows.map(function(c){return c.value;}));}
}
function bulkDeleteFiltered(){
    var total=document.querySelectorAll('.rowChk').length;
    var f=[];
    var kw=document.querySelector('input[name="searchKeyword"]');
    var role=document.querySelector('select[name="filterRole"]');
    var st=document.querySelector('select[name="filterStatus"]');
    if(kw&&kw.value.trim()){f.push('Từ khóa: "'+kw.value.trim()+'"');}
    if(role&&role.value){f.push('Vai trò: '+role.options[role.selectedIndex].text);}
    if(st&&st.value){f.push('Trạng thái: '+st.options[st.selectedIndex].text);}
    var scope=f.length?('theo bộ lọc — '+f.join(' | ')):'TOÀN BỘ danh sách (không áp bộ lọc nào)';
    var msg='CẢNH BÁO - XÓA VĨNH VIỄN TẤT CẢ TÀI KHOẢN ĐANG HIỂN THỊ\n\n'+
            'Phạm vi: '+scope+'\nSố tài khoản sẽ bị xóa: khoảng '+total+
            '\n\nTài khoản Quản trị viên sẽ được tự động bỏ qua.\n'+
            'Hành động này KHÔNG THỂ hoàn tác. Bạn có chắc chắn muốn xóa?';
    if(confirm(msg)){submitBulk('filtered',null);}
}
updateSel();
</script>
</body></html>

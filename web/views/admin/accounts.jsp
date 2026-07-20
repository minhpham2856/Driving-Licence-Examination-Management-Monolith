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
        <div style="margin-bottom:1.25rem;padding:.85rem 1.1rem;border-radius:10px;font-weight:600;font-size:.9rem;
            background:${sessionScope.flashType eq 'success' ? 'rgba(16,185,129,.08)' : 'rgba(239,68,68,.08)'};
            border:1px solid ${sessionScope.flashType eq 'success' ? 'rgba(16,185,129,.25)' : 'rgba(239,68,68,.25)'};
            color:${sessionScope.flashType eq 'success' ? '#047857' : '#b91c1c'};">${sessionScope.flashMessage}</div>
        <c:remove var="flashMessage" scope="session" /><c:remove var="flashType" scope="session" /></c:if>

    <c:if test="${not empty sessionScope.newAccPassword}">
        <div style="margin-bottom:1.25rem;padding:1rem 1.2rem;border-radius:12px;background:#fffbeb;border:1px solid #fcd34d;">
            <div style="font-weight:700;color:#92400e;margin-bottom:6px;">🔑 Mật khẩu tạm cho tài khoản: ${sessionScope.newAccUsername}</div>
            <div style="display:flex;align-items:center;gap:10px;">
                <code id="tmpPw" style="font-size:1.1rem;font-weight:800;letter-spacing:1px;background:#fff;padding:6px 14px;border-radius:8px;border:1px dashed #d97706;color:#b45309;">${sessionScope.newAccPassword}</code>
                <button type="button" onclick="copyTmp()" class="btn-export" style="cursor:pointer;padding:6px 12px;">Sao chép</button></div>
            <div style="font-size:.8rem;color:#92400e;margin-top:8px;">Gửi mật khẩu này cho người dùng. Họ <b>bắt buộc đổi mật khẩu lần đăng nhập đầu</b>. Chỉ hiển thị <b>một lần</b>.</div>
        </div>
        <c:remove var="newAccPassword" scope="session" /><c:remove var="newAccUsername" scope="session" /></c:if>

    <header class="page-header"><div class="page-title-wrap">
        <h1 class="page-title">Quản lý Tài khoản hệ thống</h1>
        <p class="page-subtitle">Cấp phát tài khoản, phân quyền, kiểm soát trạng thái. Mật khẩu do hệ thống sinh; người dùng tự đổi khi đăng nhập lần đầu.</p></div>
        <div class="page-actions" style="display:flex;gap:10px;">
            <button class="btn-export" style="height:42px;padding:0 1.25rem;border-radius:8px;">Xuất Excel</button>
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
    </h2><div class="log-card-actions"><button class="btn-export">In danh sách</button></div></header>
    <div class="table-responsive"><table class="audit-table">
        <thead><tr><th class="col-id">STT</th><th style="min-width:200px;">Tên tài khoản</th><th style="min-width:180px;">Email &amp; SĐT</th>
            <th style="width:150px;text-align:center;">Vai trò</th><th style="min-width:150px;">Trung tâm / Đơn vị</th>
            <th style="width:120px;text-align:center;">Trạng thái</th><th style="text-align:center;width:260px;">Thao tác</th></tr></thead>
        <tbody>
        <c:choose><c:when test="${not empty accounts}">
            <c:forEach var="acc" items="${accounts}" varStatus="st"><tr>
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
                    <c:choose>
                        <c:when test="${acc.status eq 'active'}"><button type="button" class="btn-export" style="padding:4px 10px;font-size:.8rem;border-color:rgba(239,68,68,.25);color:#dc2626;cursor:pointer;" onclick="lockAccount('${acc.id}','${fn:escapeXml(acc.fullName)}',true)">Khóa</button></c:when>
                        <c:otherwise><button type="button" class="btn-export" style="padding:4px 10px;font-size:.8rem;border-color:rgba(16,185,129,.25);color:#059669;cursor:pointer;" onclick="lockAccount('${acc.id}','${fn:escapeXml(acc.fullName)}',false)">Mở khóa</button></c:otherwise>
                    </c:choose>
                    <button type="button" class="btn-export" style="padding:4px 10px;font-size:.8rem;border-color:rgba(2,132,199,.25);color:#0284c7;cursor:pointer;" onclick="resetAccount('${acc.id}','${fn:escapeXml(acc.username)}')">Cấp lại MK</button>
                    <button type="button" class="btn-export" style="padding:4px 10px;font-size:.8rem;border-color:rgba(100,116,139,.25);color:#475569;cursor:pointer;" onclick="deleteAccount('${acc.id}','${fn:escapeXml(acc.fullName)}')">Xóa</button>
                </div></td>
            </tr></c:forEach>
        </c:when><c:otherwise><tr><td colspan="7" style="text-align:center;padding:5rem 1.5rem;color:#64748b;">Chưa có tài khoản nào. Nhấn <b>Tạo tài khoản mới</b>.</td></tr></c:otherwise></c:choose>
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
            <div style="margin-bottom:1.1rem;padding:.6rem .9rem;border-radius:8px;background:#eff6ff;border:1px solid #bfdbfe;color:#1e40af;font-size:.82rem;">Mật khẩu do <b>hệ thống sinh</b>, hiện cho bạn <b>một lần</b> sau khi tạo. Người dùng đổi ở lần đăng nhập đầu.</div>
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

<script>
function openAccModal(){['a_username','a_email','a_role','a_fullName','a_phone','a_dob','a_sex','a_govId','a_address'].forEach(k=>document.getElementById(k).value='');
    document.getElementById('a_status').value='active';document.getElementById('accModal').classList.add('is-open');}
function closeAccModal(){document.getElementById('accModal').classList.remove('is-open');}
function lockAccount(id,name,lock){var m=lock?('Khóa tài khoản của "'+name+'"?'):('Mở khóa tài khoản của "'+name+'"?');
    if(confirm(m)){document.getElementById('lockId').value=id;document.getElementById('lockVal').value=lock?'true':'false';document.getElementById('lockForm').submit();}}
function resetAccount(id,u){if(confirm('Cấp lại mật khẩu cho "'+u+'"?')){document.getElementById('resetId').value=id;document.getElementById('resetForm').submit();}}
function deleteAccount(id,name){if(confirm('Xóa vĩnh viễn tài khoản của "'+name+'"?')){document.getElementById('delAccId').value=id;document.getElementById('delAccForm').submit();}}
function copyTmp(){var el=document.getElementById('tmpPw');if(!el)return;navigator.clipboard.writeText(el.textContent).then(function(){alert('Đã sao chép.');});}
document.addEventListener('keydown',e=>{if(e.key==='Escape')closeAccModal();});
</script>
</body></html>

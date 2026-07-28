<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<c:set var="ctx" value="${pageContext.request.contextPath}" />
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8"><meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Quản lý Hạng GPLX - Lái Vui</title>
    <link href="https://fonts.googleapis.com/css2?family=Be+Vietnam+Pro:wght@400;500;600;700;800&family=Inter:wght@400;500;600;700;800&display=swap" rel="stylesheet">
    <link rel="stylesheet" href="${ctx}/assets/css/style.css"><link rel="stylesheet" href="${ctx}/assets/css/layout.css">
</head>
<body class="has-side-nav-bar">
<jsp:include page="/views/layout/sidebar-admin.jsp"><jsp:param name="activeSidebar" value="hang-gplx" /></jsp:include>
<div class="dashboard-shell"><main class="main-content">
    <nav class="breadcrumbs"><a href="${ctx}/views/public/home.jsp">Trang chủ</a><span class="breadcrumbs__separator">/</span>
        <a href="${ctx}/admin/dashboard">Quản trị</a><span class="breadcrumbs__separator">/</span>
        <span class="breadcrumbs__current" aria-current="page">Hạng GPLX</span></nav>

    <c:if test="${not empty sessionScope.flashMessage and sessionScope.reopenModal == null}">
        <div style="margin-bottom:1.25rem;padding:.85rem 1.1rem;border-radius:10px;font-weight:600;font-size:.9rem;
            background:${sessionScope.flashType eq 'success' ? 'rgba(16,185,129,.08)' : 'rgba(239,68,68,.08)'};
            border:1px solid ${sessionScope.flashType eq 'success' ? 'rgba(16,185,129,.25)' : 'rgba(239,68,68,.25)'};
            color:${sessionScope.flashType eq 'success' ? '#047857' : '#b91c1c'};">${sessionScope.flashMessage}</div>
        <c:remove var="flashMessage" scope="session" /><c:remove var="flashType" scope="session" /></c:if>

    <header class="page-header"><div class="page-title-wrap">
        <h1 class="page-title">Quản lý Hạng GPLX</h1>
        <p class="page-subtitle">Danh mục hạng giấy phép lái xe: độ tuổi tối thiểu, thời hạn hiệu lực.</p></div>
        <div class="page-actions"><button type="button" class="btn-filter" onclick="openLicModal()" style="height:42px;padding:0 1.25rem;border-radius:8px;cursor:pointer;">+ Thêm hạng GPLX</button></div>
    </header>

    <section class="filter-panel"><h2 class="filter-title">Bộ lọc</h2>
        <form action="${ctx}/admin/licence-class" method="GET"><div class="filter-grid" style="grid-template-columns:3fr 1.5fr;">
            <div class="input-group"><label class="input-label">Tìm hạng</label>
                <input type="text" name="searchKeyword" class="input-field" placeholder="Mã hạng hoặc mô tả..." value="${param.searchKeyword}"></div>
            <div class="input-group filter-grid__btn-col"><div class="btn-group">
                <button type="submit" class="btn-filter">Lọc</button><a href="${ctx}/admin/licence-class" class="btn-reset">Đặt lại</a></div></div>
        </div></form>
    </section>

    <section class="log-card"><header class="log-card-header"><h2 class="log-card-title">Danh sách hạng GPLX
        <c:if test="${not empty licences}"><span style="font-size:.78rem;font-weight:600;background:rgba(0,82,204,.08);color:#0052cc;padding:2px 10px;border-radius:9999px;margin-left:6px;">${fn:length(licences)}</span></c:if>
    </h2></header>
    <div class="table-responsive"><table class="audit-table">
        <thead><tr><th class="col-id">STT</th><th style="width:120px;">Mã hạng</th><th>Mô tả</th>
            <th style="width:130px;text-align:center;">Tuổi tối thiểu</th><th style="width:130px;text-align:center;">Hiệu lực (năm)</th>
            <th style="width:100px;text-align:center;">Biểu phí</th><th style="width:160px;text-align:center;">Thao tác</th></tr></thead>
        <tbody>
        <c:choose><c:when test="${not empty licences}">
            <c:forEach var="l" items="${licences}" varStatus="st"><tr>
                <td class="col-id">${st.index+1}</td>
                <td><span class="role-badge role-badge--admin" style="font-family:monospace;font-weight:700;">${l.licenceClass}</span></td>
                <td class="details-cell">${empty l.description ? '-' : l.description}</td>
                <td style="text-align:center;font-weight:600;">${l.minimumAge}</td>
                <td style="text-align:center;font-weight:600;">${l.validForYears}</td>
                <td style="text-align:center;">${l.feeCount}</td>
                <td><div style="display:flex;gap:6px;justify-content:center;">
                    <button type="button" class="btn-export" style="padding:4px 10px;font-size:.8rem;border-color:rgba(245,158,11,.25);color:#d97706;cursor:pointer;"
                        data-id="${l.licenceId}" data-class="${fn:escapeXml(l.licenceClass)}" data-desc="${fn:escapeXml(l.description)}"
                        data-age="${l.minimumAge}" data-valid="${l.validForYears}" onclick="editLic(this)">Sửa</button>
                    <button type="button" class="btn-export" style="padding:4px 10px;font-size:.8rem;border-color:rgba(239,68,68,.25);color:#dc2626;cursor:pointer;"
                        onclick="delLic('${l.licenceId}','${fn:escapeXml(l.licenceClass)}')">Xóa</button>
                </div></td>
            </tr></c:forEach>
        </c:when><c:otherwise><tr><td colspan="7" style="text-align:center;padding:4rem 1.5rem;color:#64748b;">Chưa có hạng GPLX nào.</td></tr></c:otherwise></c:choose>
        </tbody>
    </table></div></section>
</main>
</div>

<form id="delForm" action="${ctx}/admin/licence-class" method="POST" style="display:none;"><input type="hidden" name="action" value="delete"><input type="hidden" name="id" id="delId"></form>

<style>
.modal-overlay{display:none;position:fixed;inset:0;z-index:1000;background:rgba(15,23,42,.45);align-items:flex-start;justify-content:center;padding:4vh 1rem;overflow-y:auto;}
.modal-overlay.is-open{display:flex;}.modal-card{width:100%;max-width:560px;background:#fff;border-radius:16px;box-shadow:0 20px 60px rgba(15,23,42,.25);}
.modal-head{display:flex;align-items:center;justify-content:space-between;padding:1.25rem 1.5rem;border-bottom:1px solid #e2e8f0;}
.modal-head h3{margin:0;font-size:1.1rem;font-weight:800;color:#0f172a;}.modal-close{border:none;background:transparent;font-size:1.5rem;color:#94a3b8;cursor:pointer;}
.modal-body{padding:1.5rem;}.modal-foot{display:flex;gap:12px;justify-content:flex-end;padding:1rem 1.5rem;border-top:1px solid #e2e8f0;}
.modal-err{display:none;margin-bottom:1rem;padding:.7rem .9rem;border-radius:8px;background:rgba(239,68,68,.08);border:1px solid rgba(239,68,68,.25);color:#b91c1c;font-size:.85rem;font-weight:600;}.modal-err.show{display:block;}
</style>

<div id="licModal" class="modal-overlay" onclick="if(event.target===this)closeLicModal()"><div class="modal-card">
    <form action="${ctx}/admin/licence-class" method="POST">
        <div class="modal-head"><h3 id="licTitle">Thêm hạng GPLX</h3><button type="button" class="modal-close" onclick="closeLicModal()">&times;</button></div>
        <div class="modal-body"><div id="licErr" class="modal-err"></div>
            <input type="hidden" name="licenceId" id="l_id" value="">
            <div class="input-group" style="margin-bottom:1.25rem;"><label class="input-label">Mã hạng <span style="color:#dc2626;">*</span></label>
                <input type="text" id="l_class" name="licenceClass" class="input-field" maxlength="10" placeholder="VD: A1, B2, C" required style="text-transform:uppercase;"></div>
            <div class="input-group" style="margin-bottom:1.25rem;"><label class="input-label">Mô tả</label>
                <input type="text" id="l_desc" name="description" class="input-field" maxlength="500" placeholder="VD: Xe mô tô hai bánh đến 175cm3"></div>
            <div class="filter-grid" style="grid-template-columns:1fr 1fr;gap:1.25rem;">
                <div class="input-group"><label class="input-label">Độ tuổi tối thiểu <span style="color:#dc2626;">*</span></label>
                    <input type="number" id="l_age" name="minimumAge" class="input-field" min="16" max="100" placeholder="VD: 18" required></div>
                <div class="input-group"><label class="input-label">Hiệu lực (năm) <span style="color:#dc2626;">*</span></label>
                    <input type="number" id="l_valid" name="validForYears" class="input-field" min="1" max="50" placeholder="VD: 10" required></div>
            </div>
        </div>
        <div class="modal-foot"><button type="button" class="btn-reset" onclick="closeLicModal()" style="height:44px;padding:0 1.5rem;display:inline-flex;align-items:center;">Hủy</button>
            <button type="submit" class="btn-filter" style="height:44px;padding:0 1.5rem;">Lưu hạng</button></div>
    </form>
</div></div>

<script>
function openLicModal(){document.getElementById('licTitle').textContent='Thêm hạng GPLX';
    ['l_id','l_class','l_desc','l_age','l_valid'].forEach(k=>document.getElementById(k).value='');
    document.getElementById('licErr').classList.remove('show');document.getElementById('licModal').classList.add('is-open');}
function editLic(b){document.getElementById('licTitle').textContent='Chỉnh sửa hạng GPLX';
    document.getElementById('l_id').value=b.dataset.id;document.getElementById('l_class').value=b.dataset.class;
    document.getElementById('l_desc').value=b.dataset.desc;document.getElementById('l_age').value=b.dataset.age;document.getElementById('l_valid').value=b.dataset.valid;
    document.getElementById('licErr').classList.remove('show');document.getElementById('licModal').classList.add('is-open');}
function closeLicModal(){document.getElementById('licModal').classList.remove('is-open');}
function delLic(id,name){if(confirm('Xóa hạng "'+name+'"?\nKhông thể hoàn tác.')){document.getElementById('delId').value=id;document.getElementById('delForm').submit();}}
document.addEventListener('keydown',e=>{if(e.key==='Escape')closeLicModal();});
</script>

<c:if test="${sessionScope.reopenModal eq 'licence'}">
<script>
document.getElementById('licTitle').textContent='${sessionScope.f_mode eq "edit" ? "Chỉnh sửa hạng GPLX" : "Thêm hạng GPLX"}';
document.getElementById('l_id').value='${sessionScope.f_licenceId}';
document.getElementById('l_class').value="${fn:escapeXml(sessionScope.f_licenceClass)}";
document.getElementById('l_desc').value="${fn:escapeXml(sessionScope.f_description)}";
document.getElementById('l_age').value="${fn:escapeXml(sessionScope.f_minimumAge)}";
document.getElementById('l_valid').value="${fn:escapeXml(sessionScope.f_validForYears)}";
var e=document.getElementById('licErr');e.textContent="${fn:escapeXml(sessionScope.flashMessage)}";e.classList.add('show');
document.getElementById('licModal').classList.add('is-open');
</script>
<c:remove var="reopenModal" scope="session" /><c:remove var="flashMessage" scope="session" /><c:remove var="flashType" scope="session" /></c:if>

</body></html>

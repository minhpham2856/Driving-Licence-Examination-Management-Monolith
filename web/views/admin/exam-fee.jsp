<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<c:set var="ctx" value="${pageContext.request.contextPath}" />
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8"><meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Quản lý Lệ phí thi - Lái Vui</title>
    <link href="https://fonts.googleapis.com/css2?family=Be+Vietnam+Pro:wght@400;500;600;700;800&family=Inter:wght@400;500;600;700;800&display=swap" rel="stylesheet">
    <link rel="stylesheet" href="${ctx}/assets/css/style.css"><link rel="stylesheet" href="${ctx}/assets/css/layout.css">
</head>
<body class="has-side-nav-bar">
<jsp:include page="/views/layout/sidebar-admin.jsp"><jsp:param name="activeSidebar" value="le-phi" /></jsp:include>
<div class="dashboard-shell"><main class="main-content">
    <nav class="breadcrumbs"><a href="${ctx}/views/public/home.jsp">Trang chủ</a><span class="breadcrumbs__separator">/</span>
        <a href="${ctx}/admin/dashboard">Quản trị</a><span class="breadcrumbs__separator">/</span>
        <span class="breadcrumbs__current" aria-current="page">Lệ phí thi</span></nav>

    <c:if test="${not empty sessionScope.flashMessage and sessionScope.reopenModal == null}">
        <div style="margin-bottom:1.25rem;padding:.85rem 1.1rem;border-radius:10px;font-weight:600;font-size:.9rem;
            background:${sessionScope.flashType eq 'success' ? 'rgba(16,185,129,.08)' : 'rgba(239,68,68,.08)'};
            border:1px solid ${sessionScope.flashType eq 'success' ? 'rgba(16,185,129,.25)' : 'rgba(239,68,68,.25)'};
            color:${sessionScope.flashType eq 'success' ? '#047857' : '#b91c1c'};">${sessionScope.flashMessage}</div>
        <c:remove var="flashMessage" scope="session" /><c:remove var="flashType" scope="session" /></c:if>

    <header class="page-header"><div class="page-title-wrap">
        <h1 class="page-title">Quản lý Lệ phí thi</h1>
        <p class="page-subtitle">Biểu phí = mức tiền của từng loại phí theo hạng GPLX. "Danh mục phí" là các loại phí chung dùng để lập biểu phí và thanh toán.</p></div>
        <div class="page-actions" style="display:flex;gap:10px;">
            <button type="button" class="btn-export" onclick="openFeeModal()" style="height:42px;padding:0 1.25rem;border-radius:8px;cursor:pointer;">Danh mục phí</button>
            <button type="button" class="btn-filter" onclick="openLFModal()" style="height:42px;padding:0 1.25rem;border-radius:8px;cursor:pointer;">+ Thêm mức phí</button>
        </div>
    </header>

    <%-- ===== BIỂU PHÍ THEO HẠNG (Licence_Fee) ===== --%>
    <section class="filter-panel"><h2 class="filter-title">Bộ lọc biểu phí</h2>
        <form action="${ctx}/admin/exam-fee" method="GET"><div class="filter-grid" style="grid-template-columns:1.5fr 1.5fr 1fr;">
            <div class="input-group"><label class="input-label">Hạng GPLX</label>
                <select name="filterLicence" class="input-field"><option value="">Tất cả hạng</option>
                    <c:forEach var="l" items="${licences}"><option value="${l.licenceId}" ${param.filterLicence eq l.licenceId ? 'selected' : ''}>${l.licenceClass}</option></c:forEach>
                </select></div>
            <div class="input-group"><label class="input-label">Loại phí</label>
                <select name="filterFee" class="input-field"><option value="">Tất cả loại phí</option>
                    <c:forEach var="f" items="${fees}"><option value="${f.feeId}" ${param.filterFee eq f.feeId ? 'selected' : ''}>${f.feeName}</option></c:forEach>
                </select></div>
            <div class="input-group filter-grid__btn-col"><div class="btn-group">
                <button type="submit" class="btn-filter">Lọc</button><a href="${ctx}/admin/exam-fee" class="btn-reset">Đặt lại</a></div></div>
        </div></form>
    </section>

    <section class="log-card"><header class="log-card-header"><h2 class="log-card-title">Biểu phí theo hạng
        <c:if test="${not empty licenceFees}"><span style="font-size:.78rem;font-weight:600;background:rgba(0,82,204,.08);color:#0052cc;padding:2px 10px;border-radius:9999px;margin-left:6px;">${fn:length(licenceFees)}</span></c:if>
    </h2></header>
    <div class="table-responsive"><table class="audit-table">
        <thead><tr><th class="col-id">STT</th><th style="width:140px;">Hạng GPLX</th><th>Loại phí</th>
            <th style="width:150px;">Nhóm phí</th><th style="width:160px;text-align:right;">Mức phí (đ)</th><th style="width:160px;text-align:center;">Thao tác</th></tr></thead>
        <tbody>
        <c:choose><c:when test="${not empty licenceFees}">
            <c:forEach var="lf" items="${licenceFees}" varStatus="st"><tr>
                <td class="col-id">${st.index+1}</td>
                <td><c:choose><c:when test="${empty lf.licenceId}"><span class="role-badge role-badge--other">Áp dụng chung</span></c:when>
                    <c:otherwise><span class="role-badge role-badge--admin" style="font-family:monospace;font-weight:700;">${lf.licenceClass}</span></c:otherwise></c:choose></td>
                <td style="font-weight:600;color:#0f172a;">${lf.feeName}</td>
                <td><span class="user-username">${lf.feeType}</span></td>
                <td style="text-align:right;font-weight:700;color:#0f172a;"><fmt:formatNumber value="${lf.amount}" type="number" maxFractionDigits="0"/></td>
                <td><div style="display:flex;gap:6px;justify-content:center;">
                    <button type="button" class="btn-export" style="padding:4px 10px;font-size:.8rem;border-color:rgba(245,158,11,.25);color:#d97706;cursor:pointer;"
                        data-id="${lf.licenceFeeId}" data-licence="${empty lf.licenceId ? '' : lf.licenceId}" data-fee="${lf.feeId}" data-amount="${lf.amount}" onclick="editLF(this)">Sửa</button>
                    <button type="button" class="btn-export" style="padding:4px 10px;font-size:.8rem;border-color:rgba(239,68,68,.25);color:#dc2626;cursor:pointer;"
                        onclick="delLF('${lf.licenceFeeId}')">Xóa</button>
                </div></td>
            </tr></c:forEach>
        </c:when><c:otherwise><tr><td colspan="6" style="text-align:center;padding:4rem 1.5rem;color:#64748b;">Chưa có mức phí nào. Nhấn <b>Thêm mức phí</b> (cần có loại phí trong Danh mục phí trước).</td></tr></c:otherwise></c:choose>
        </tbody>
    </table></div></section>

    <%-- ===== DANH MỤC PHÍ (Fee) ===== --%>
    <section class="log-card" style="margin-top:1.5rem;"><header class="log-card-header"><h2 class="log-card-title">Danh mục phí (loại phí chung)
        <c:if test="${not empty fees}"><span style="font-size:.78rem;font-weight:600;background:rgba(0,82,204,.08);color:#0052cc;padding:2px 10px;border-radius:9999px;margin-left:6px;">${fn:length(fees)}</span></c:if>
    </h2><div class="log-card-actions"><button type="button" class="btn-export" onclick="openFeeModal()">+ Thêm loại phí</button></div></header>
    <div class="table-responsive"><table class="audit-table">
        <thead><tr><th class="col-id">STT</th><th>Tên loại phí</th><th style="width:180px;">Nhóm phí</th>
            <th style="width:120px;text-align:center;">Trạng thái</th><th style="width:220px;text-align:center;">Thao tác</th></tr></thead>
        <tbody>
        <c:choose><c:when test="${not empty fees}">
            <c:forEach var="f" items="${fees}" varStatus="st"><tr>
                <td class="col-id">${st.index+1}</td>
                <td style="font-weight:600;color:#0f172a;">${f.feeName}</td>
                <td><span class="user-username">${f.feeType}</span></td>
                <td style="text-align:center;"><c:choose><c:when test="${f.active}"><span class="action-badge action-badge--success">Đang dùng</span></c:when>
                    <c:otherwise><span class="action-badge action-badge--danger">Tắt</span></c:otherwise></c:choose></td>
                <td><div style="display:flex;gap:6px;justify-content:center;flex-wrap:wrap;">
                    <c:choose><c:when test="${f.active}"><button type="button" class="btn-export" style="padding:4px 10px;font-size:.8rem;border-color:rgba(245,158,11,.25);color:#d97706;cursor:pointer;" onclick="toggleFee('${f.feeId}',false)">Tắt</button></c:when>
                        <c:otherwise><button type="button" class="btn-export" style="padding:4px 10px;font-size:.8rem;border-color:rgba(16,185,129,.25);color:#059669;cursor:pointer;" onclick="toggleFee('${f.feeId}',true)">Bật</button></c:otherwise></c:choose>
                    <button type="button" class="btn-export" style="padding:4px 10px;font-size:.8rem;border-color:rgba(245,158,11,.25);color:#d97706;cursor:pointer;"
                        data-id="${f.feeId}" data-name="${fn:escapeXml(f.feeName)}" data-type="${fn:escapeXml(f.feeType)}" data-active="${f.active}" onclick="editFee(this)">Sửa</button>
                    <button type="button" class="btn-export" style="padding:4px 10px;font-size:.8rem;border-color:rgba(239,68,68,.25);color:#dc2626;cursor:pointer;" onclick="delFee('${f.feeId}','${fn:escapeXml(f.feeName)}')">Xóa</button>
                </div></td>
            </tr></c:forEach>
        </c:when><c:otherwise><tr><td colspan="5" style="text-align:center;padding:3rem;color:#64748b;">Chưa có loại phí nào. Nhấn <b>Thêm loại phí</b>.</td></tr></c:otherwise></c:choose>
        </tbody>
    </table></div></section>
</main>
</div>

<form id="delLFForm" action="${ctx}/admin/exam-fee" method="POST" style="display:none;"><input type="hidden" name="action" value="deleteLF"><input type="hidden" name="id" id="delLFId"></form>
<form id="delFeeForm" action="${ctx}/admin/exam-fee" method="POST" style="display:none;"><input type="hidden" name="action" value="deleteFee"><input type="hidden" name="id" id="delFeeId"></form>
<form id="toggleFeeForm" action="${ctx}/admin/exam-fee" method="POST" style="display:none;"><input type="hidden" name="action" value="toggleFee"><input type="hidden" name="id" id="tgFeeId"><input type="hidden" name="active" id="tgFeeActive"></form>

<style>
.modal-overlay{display:none;position:fixed;inset:0;z-index:1000;background:rgba(15,23,42,.45);align-items:flex-start;justify-content:center;padding:4vh 1rem;overflow-y:auto;}
.modal-overlay.is-open{display:flex;}.modal-card{width:100%;max-width:580px;background:#fff;border-radius:16px;box-shadow:0 20px 60px rgba(15,23,42,.25);}
.modal-head{display:flex;align-items:center;justify-content:space-between;padding:1.25rem 1.5rem;border-bottom:1px solid #e2e8f0;}
.modal-head h3{margin:0;font-size:1.1rem;font-weight:800;color:#0f172a;}.modal-close{border:none;background:transparent;font-size:1.5rem;color:#94a3b8;cursor:pointer;}
.modal-body{padding:1.5rem;}.modal-foot{display:flex;gap:12px;justify-content:flex-end;padding:1rem 1.5rem;border-top:1px solid #e2e8f0;}
.modal-err{display:none;margin-bottom:1rem;padding:.7rem .9rem;border-radius:8px;background:rgba(239,68,68,.08);border:1px solid rgba(239,68,68,.25);color:#b91c1c;font-size:.85rem;font-weight:600;}.modal-err.show{display:block;}
</style>

<%-- Modal: Mức phí (Licence_Fee) --%>
<div id="lfModal" class="modal-overlay" onclick="if(event.target===this)closeLFModal()"><div class="modal-card">
    <form action="${ctx}/admin/exam-fee" method="POST">
        <div class="modal-head"><h3 id="lfTitle">Thêm mức phí</h3><button type="button" class="modal-close" onclick="closeLFModal()">&times;</button></div>
        <div class="modal-body"><div id="lfErr" class="modal-err"></div>
            <input type="hidden" name="licenceFeeId" id="lf_id" value="">
            <div class="input-group" style="margin-bottom:1.25rem;"><label class="input-label">Hạng GPLX</label>
                <select id="lf_licence" name="licenceId" class="input-field"><option value="">Áp dụng chung (mọi hạng)</option>
                    <c:forEach var="l" items="${licences}"><option value="${l.licenceId}">${l.licenceClass} &mdash; ${l.description}</option></c:forEach>
                </select></div>
            <div class="input-group" style="margin-bottom:1.25rem;"><label class="input-label">Loại phí <span style="color:#dc2626;">*</span></label>
                <select id="lf_fee" name="feeId" class="input-field" required><option value="">-- Chọn loại phí --</option>
                    <c:forEach var="f" items="${activeFees}"><option value="${f.feeId}">${f.feeName} (${f.feeType})</option></c:forEach>
                </select>
                <c:if test="${empty activeFees}"><div style="font-size:.8rem;color:#b45309;margin-top:6px;">Chưa có loại phí nào đang bật. Hãy thêm trong "Danh mục phí".</div></c:if></div>
            <div class="input-group"><label class="input-label">Mức phí (đồng) <span style="color:#dc2626;">*</span></label>
                <input type="number" id="lf_amount" name="amount" class="input-field" min="0" step="1000" placeholder="VD: 100000" required></div>
        </div>
        <div class="modal-foot"><button type="button" class="btn-reset" onclick="closeLFModal()" style="height:44px;padding:0 1.5rem;display:inline-flex;align-items:center;">Hủy</button>
            <button type="submit" class="btn-filter" style="height:44px;padding:0 1.5rem;">Lưu mức phí</button></div>
    </form>
</div></div>

<%-- Modal: Loại phí (Fee) --%>
<div id="feeModal" class="modal-overlay" onclick="if(event.target===this)closeFeeModal()"><div class="modal-card">
    <form action="${ctx}/admin/exam-fee" method="POST">
        <input type="hidden" name="action" value="saveFee">
        <div class="modal-head"><h3 id="feeTitle">Thêm loại phí</h3><button type="button" class="modal-close" onclick="closeFeeModal()">&times;</button></div>
        <div class="modal-body"><div id="feeErr" class="modal-err"></div>
            <input type="hidden" name="feeId" id="fee_id" value="">
            <div class="input-group" style="margin-bottom:1.25rem;"><label class="input-label">Tên loại phí <span style="color:#dc2626;">*</span></label>
                <input type="text" id="fee_name" name="feeName" class="input-field" maxlength="100" placeholder="VD: Lệ phí sát hạch lý thuyết" required></div>
            <div class="filter-grid" style="grid-template-columns:1.5fr 1fr;gap:1.25rem;">
                <div class="input-group"><label class="input-label">Nhóm phí <span style="color:#dc2626;">*</span></label>
                    <input type="text" id="fee_type" name="feeType" class="input-field" maxlength="50" placeholder="VD: Lý thuyết / Thực hành / Hồ sơ" required></div>
                <div class="input-group"><label class="input-label">Trạng thái</label>
                    <select id="fee_status" name="status" class="input-field"><option value="active">Đang dùng</option><option value="inactive">Tắt</option></select></div>
            </div>
        </div>
        <div class="modal-foot"><button type="button" class="btn-reset" onclick="closeFeeModal()" style="height:44px;padding:0 1.5rem;display:inline-flex;align-items:center;">Hủy</button>
            <button type="submit" class="btn-filter" style="height:44px;padding:0 1.5rem;">Lưu loại phí</button></div>
    </form>
</div></div>

<script>
// Licence_Fee modal
function openLFModal(){document.getElementById('lfTitle').textContent='Thêm mức phí';
    document.getElementById('lf_id').value='';document.getElementById('lf_licence').value='';document.getElementById('lf_fee').value='';document.getElementById('lf_amount').value='';
    document.getElementById('lfErr').classList.remove('show');document.getElementById('lfModal').classList.add('is-open');}
function editLF(b){document.getElementById('lfTitle').textContent='Chỉnh sửa mức phí';
    document.getElementById('lf_id').value=b.dataset.id;document.getElementById('lf_licence').value=b.dataset.licence;
    document.getElementById('lf_fee').value=b.dataset.fee;document.getElementById('lf_amount').value=Math.round(parseFloat(b.dataset.amount||'0'));
    document.getElementById('lfErr').classList.remove('show');document.getElementById('lfModal').classList.add('is-open');}
function closeLFModal(){document.getElementById('lfModal').classList.remove('is-open');}
function delLF(id){if(confirm('Xóa mức phí này?')){document.getElementById('delLFId').value=id;document.getElementById('delLFForm').submit();}}
// Fee modal
function openFeeModal(){document.getElementById('feeTitle').textContent='Thêm loại phí';
    document.getElementById('fee_id').value='';document.getElementById('fee_name').value='';document.getElementById('fee_type').value='';document.getElementById('fee_status').value='active';
    document.getElementById('feeErr').classList.remove('show');document.getElementById('feeModal').classList.add('is-open');}
function editFee(b){document.getElementById('feeTitle').textContent='Chỉnh sửa loại phí';
    document.getElementById('fee_id').value=b.dataset.id;document.getElementById('fee_name').value=b.dataset.name;document.getElementById('fee_type').value=b.dataset.type;
    document.getElementById('fee_status').value=(b.dataset.active==='true')?'active':'inactive';
    document.getElementById('feeErr').classList.remove('show');document.getElementById('feeModal').classList.add('is-open');}
function closeFeeModal(){document.getElementById('feeModal').classList.remove('is-open');}
function delFee(id,name){if(confirm('Xóa loại phí "'+name+'"?')){document.getElementById('delFeeId').value=id;document.getElementById('delFeeForm').submit();}}
function toggleFee(id,on){document.getElementById('tgFeeId').value=id;document.getElementById('tgFeeActive').value=on?'true':'false';document.getElementById('toggleFeeForm').submit();}
document.addEventListener('keydown',e=>{if(e.key==='Escape'){closeLFModal();closeFeeModal();}});
</script>

<c:if test="${sessionScope.reopenModal eq 'lf'}">
<script>
document.getElementById('lfTitle').textContent='${sessionScope.f_mode eq "edit" ? "Chỉnh sửa mức phí" : "Thêm mức phí"}';
document.getElementById('lf_id').value='${sessionScope.f_lfId}';
document.getElementById('lf_licence').value="${sessionScope.f_licenceId}";
document.getElementById('lf_fee').value='${sessionScope.f_feeId}';
document.getElementById('lf_amount').value="${fn:escapeXml(sessionScope.f_amount)}";
var e=document.getElementById('lfErr');e.textContent="${fn:escapeXml(sessionScope.flashMessage)}";e.classList.add('show');
document.getElementById('lfModal').classList.add('is-open');
</script>
<c:remove var="reopenModal" scope="session" /><c:remove var="flashMessage" scope="session" /><c:remove var="flashType" scope="session" /></c:if>

<c:if test="${sessionScope.reopenModal eq 'fee'}">
<script>
document.getElementById('feeTitle').textContent='${sessionScope.f_mode eq "edit" ? "Chỉnh sửa loại phí" : "Thêm loại phí"}';
document.getElementById('fee_id').value='${sessionScope.f_feeId}';
document.getElementById('fee_name').value="${fn:escapeXml(sessionScope.f_feeName)}";
document.getElementById('fee_type').value="${fn:escapeXml(sessionScope.f_feeType)}";
document.getElementById('fee_status').value='${sessionScope.f_active ? "active" : "inactive"}';
var e=document.getElementById('feeErr');e.textContent="${fn:escapeXml(sessionScope.flashMessage)}";e.classList.add('show');
document.getElementById('feeModal').classList.add('is-open');
</script>
<c:remove var="reopenModal" scope="session" /><c:remove var="flashMessage" scope="session" /><c:remove var="flashType" scope="session" /></c:if>

</body></html>

<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<c:set var="ctx" value="${pageContext.request.contextPath}" />
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8"><meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Quản lý Phòng thi - Lái Vui</title>
    <link href="https://fonts.googleapis.com/css2?family=Be+Vietnam+Pro:wght@400;500;600;700;800&family=Inter:wght@400;500;600;700;800&display=swap" rel="stylesheet">
    <link rel="stylesheet" href="${ctx}/assets/css/style.css">
    <link rel="stylesheet" href="${ctx}/assets/css/layout.css">
</head>
<body class="has-side-nav-bar">
<jsp:include page="/views/layout/sidebar-admin.jsp"><jsp:param name="activeSidebar" value="phong-thi" /></jsp:include>
<div class="dashboard-shell">
    <main class="main-content">
        <nav class="breadcrumbs">
            <a href="${ctx}/views/public/home.jsp">Trang chủ</a><span class="breadcrumbs__separator">/</span>
            <a href="${ctx}/admin/dashboard">Quản trị</a><span class="breadcrumbs__separator">/</span>
            <span class="breadcrumbs__current" aria-current="page">Phòng thi</span>
        </nav>

        <c:if test="${not empty sessionScope.flashMessage and sessionScope.reopenModal == null}">
            <div style="margin-bottom:1.25rem;padding:.85rem 1.1rem;border-radius:10px;font-weight:600;font-size:.9rem;
                        background:${sessionScope.flashType eq 'success' ? 'rgba(16,185,129,.08)' : 'rgba(239,68,68,.08)'};
                        border:1px solid ${sessionScope.flashType eq 'success' ? 'rgba(16,185,129,.25)' : 'rgba(239,68,68,.25)'};
                        color:${sessionScope.flashType eq 'success' ? '#047857' : '#b91c1c'};">${sessionScope.flashMessage}</div>
            <c:remove var="flashMessage" scope="session" /><c:remove var="flashType" scope="session" />
        </c:if>

        <header class="page-header">
            <div class="page-title-wrap">
                <h1 class="page-title">Quản lý Phòng thi &amp; Sân thi</h1>
                <p class="page-subtitle">Mỗi phòng/sân thuộc một khu vực thi. Chọn khu vực trước khi khai báo phòng, sân.</p>
            </div>
            <div class="page-actions" style="display:flex;gap:10px;">
                <button type="button" class="btn-filter" onclick="openRoomModal()" style="height:42px;padding:0 1.25rem;border-radius:8px;cursor:pointer;">+ Thêm phòng/sân</button>
            </div>
        </header>

        <section class="filter-panel">
            <h2 class="filter-title">Bộ lọc tìm kiếm</h2>
            <form action="${ctx}/admin/exam-room" method="GET">
                <div class="filter-grid" style="grid-template-columns:2fr 1.3fr 1.3fr 1.4fr;">
                    <div class="input-group"><label class="input-label">Tìm phòng/sân</label>
                        <input type="text" name="searchKeyword" class="input-field" placeholder="Tên hoặc địa điểm..." value="${param.searchKeyword}"></div>
                    <div class="input-group"><label class="input-label">Khu vực</label>
                        <select name="filterZone" class="input-field"><option value="">Tất cả khu vực</option>
                            <c:forEach var="z" items="${zones}"><option value="${z.zoneId}" ${param.filterZone eq z.zoneId ? 'selected' : ''}>${z.zoneName}</option></c:forEach>
                        </select></div>
                    <div class="input-group"><label class="input-label">Loại</label>
                        <select name="filterType" class="input-field"><option value="">Tất cả loại</option>
                            <option value="Phòng thủ tục" ${param.filterType eq 'Phòng thủ tục' ? 'selected' : ''}>Phòng thủ tục</option>
                            <option value="Phòng thi" ${param.filterType eq 'Phòng thi' ? 'selected' : ''}>Phòng thi</option>
                            <option value="Sân thi" ${param.filterType eq 'Sân thi' ? 'selected' : ''}>Sân thi</option>
                        </select></div>
                    <div class="input-group filter-grid__btn-col"><div class="btn-group">
                        <button type="submit" class="btn-filter">Lọc</button>
                        <a href="${ctx}/admin/exam-room" class="btn-reset">Đặt lại</a>
                    </div></div>
                </div>
            </form>
        </section>

        <section class="log-card">
            <header class="log-card-header"><h2 class="log-card-title">Danh sách phòng/sân thi
                <c:if test="${not empty areas}"><span style="font-size:.78rem;font-weight:600;background:rgba(0,82,204,.08);color:#0052cc;padding:2px 10px;border-radius:9999px;margin-left:6px;">${fn:length(areas)}</span></c:if>
            </h2></header>
            <div class="table-responsive"><table class="audit-table">
                <thead><tr>
                    <th class="col-id">STT</th><th style="width:100px;">Mã</th><th>Tên phòng/sân</th>
                    <th style="width:130px;text-align:center;">Loại</th><th>Khu vực</th>
                    <th style="width:100px;text-align:center;">Sức chứa</th><th style="width:90px;text-align:center;">Máy thi</th>
                    <th style="width:160px;text-align:center;">Thao tác</th>
                </tr></thead>
                <tbody>
                <c:choose>
                    <c:when test="${not empty areas}">
                        <c:forEach var="a" items="${areas}" varStatus="st">
                            <tr>
                                <td class="col-id">${st.index+1}</td>
                                <td style="font-weight:700;color:#0052cc;font-family:monospace;">${a.code}</td>
                                <td><span class="user-name" style="font-weight:600;color:#0f172a;">${a.areaName}</span>
                                    <div style="font-size:.75rem;color:#94a3b8;">${a.location}</div></td>
                                <td style="text-align:center;"><span class="role-badge role-badge--coi">${a.areaType}</span></td>
                                <td>${a.zoneName}</td>
                                <td style="text-align:center;font-weight:700;">${a.capacityText}</td>
                                <td style="text-align:center;">${a.deviceCount}</td>
                                <td><div style="display:flex;gap:6px;justify-content:center;">
                                    <button type="button" class="btn-export" style="padding:4px 10px;font-size:.8rem;border-color:rgba(245,158,11,.25);color:#d97706;cursor:pointer;"
                                        data-id="${a.areaId}" data-zone="${a.zoneId}" data-name="${fn:escapeXml(a.areaName)}"
                                        data-type="${fn:escapeXml(a.areaType)}" data-capacity="${a.capacity}" data-location="${fn:escapeXml(a.location)}"
                                        onclick="editRoom(this)">Sửa</button>
                                    <button type="button" class="btn-export" style="padding:4px 10px;font-size:.8rem;border-color:rgba(239,68,68,.25);color:#dc2626;cursor:pointer;"
                                        onclick="delRoom('${a.areaId}','${fn:escapeXml(a.areaName)}')">Xóa</button>
                                </div></td>
                            </tr>
                        </c:forEach>
                    </c:when>
                    <c:otherwise><tr><td colspan="8" style="text-align:center;padding:4rem 1.5rem;color:#64748b;">Chưa có phòng/sân thi nào. Nhấn <b>Thêm phòng/sân</b> để bắt đầu.</td></tr></c:otherwise>
                </c:choose>
                </tbody>
            </table></div>
        </section>
    </main>
    <jsp:include page="/views/layout/footer.jsp"><jsp:param name="standalone" value="false" /></jsp:include>
</div>

<form id="delForm" action="${ctx}/admin/exam-room" method="POST" style="display:none;">
    <input type="hidden" name="action" value="delete"><input type="hidden" name="id" id="delId"></form>

<style>
.modal-overlay{display:none;position:fixed;inset:0;z-index:1000;background:rgba(15,23,42,.45);align-items:flex-start;justify-content:center;padding:4vh 1rem;overflow-y:auto;}
.modal-overlay.is-open{display:flex;}
.modal-card{width:100%;max-width:620px;background:#fff;border-radius:16px;box-shadow:0 20px 60px rgba(15,23,42,.25);}
.modal-head{display:flex;align-items:center;justify-content:space-between;padding:1.25rem 1.5rem;border-bottom:1px solid #e2e8f0;}
.modal-head h3{margin:0;font-size:1.1rem;font-weight:800;color:#0f172a;}
.modal-close{border:none;background:transparent;font-size:1.5rem;color:#94a3b8;cursor:pointer;}
.modal-body{padding:1.5rem;}
.modal-foot{display:flex;gap:12px;justify-content:flex-end;padding:1rem 1.5rem;border-top:1px solid #e2e8f0;}
.modal-err{display:none;margin-bottom:1rem;padding:.7rem .9rem;border-radius:8px;background:rgba(239,68,68,.08);border:1px solid rgba(239,68,68,.25);color:#b91c1c;font-size:.85rem;font-weight:600;}
.modal-err.show{display:block;}
.zone-hint{margin:.75rem 0 0;padding:.6rem .8rem;border-radius:8px;background:#f8fafc;border:1px solid #e2e8f0;font-size:.8rem;color:#475569;}
#roomFields{display:none;}
#roomFields.show{display:block;}
</style>

<div id="roomModal" class="modal-overlay" onclick="if(event.target===this)closeRoomModal()">
    <div class="modal-card">
        <form action="${ctx}/admin/exam-room" method="POST">
            <div class="modal-head"><h3 id="roomTitle">Thêm phòng/sân thi</h3><button type="button" class="modal-close" onclick="closeRoomModal()">&times;</button></div>
            <div class="modal-body">
                <div id="roomErr" class="modal-err"></div>
                <input type="hidden" name="areaId" id="r_id" value="">

                <div class="input-group"><label class="input-label">Bước 1 &mdash; Chọn khu vực thi <span style="color:#dc2626;">*</span></label>
                    <select id="r_zone" name="zoneId" class="input-field" onchange="onZoneChange()" required>
                        <option value="">-- Chọn khu vực --</option>
                        <c:forEach var="z" items="${zones}"><option value="${z.zoneId}">${z.zoneName}</option></c:forEach>
                    </select>
                    <div id="r_zoneHint" class="zone-hint" style="display:none;"></div>
                </div>

                <div id="roomFields">
                    <div class="input-group" style="margin:1.25rem 0;"><label class="input-label">Bước 2 &mdash; Tên phòng/sân <span style="color:#dc2626;">*</span></label>
                        <input type="text" id="r_name" name="areaName" class="input-field" maxlength="100" placeholder="VD: Phòng thi lý thuyết 01"></div>
                    <div class="filter-grid" style="grid-template-columns:1fr 1fr;gap:1.25rem;margin-bottom:1.25rem;">
                        <div class="input-group"><label class="input-label">Loại <span style="color:#dc2626;">*</span></label>
                            <select id="r_type" name="areaType" class="input-field">
                                <option value="">-- Chọn loại --</option>
                                <option value="Phòng thủ tục">Phòng thủ tục</option>
                                <option value="Phòng thi">Phòng thi</option>
                                <option value="Sân thi">Sân thi</option>
                            </select></div>
                        <div class="input-group"><label class="input-label">Sức chứa (người)</label>
                            <input type="number" id="r_capacity" name="capacity" class="input-field" min="1" placeholder="VD: 50 (có thể bỏ trống)"></div>
                    </div>
                    <div class="input-group"><label class="input-label">Địa điểm chi tiết <span style="color:#dc2626;">*</span></label>
                        <input type="text" id="r_location" name="location" class="input-field" maxlength="255" placeholder="VD: Tầng 2, tòa nhà A"></div>
                </div>
            </div>
            <div class="modal-foot"><button type="button" class="btn-reset" onclick="closeRoomModal()" style="height:44px;padding:0 1.5rem;display:inline-flex;align-items:center;">Hủy</button>
                <button type="submit" class="btn-filter" style="height:44px;padding:0 1.5rem;">Lưu phòng/sân</button></div>
        </form>
    </div>
</div>

<script>
// Danh sách phòng/sân theo khu vực (để xem khu vực đã có gì)
var AREAS_BY_ZONE = {};
<c:forEach var="a" items="${allAreas}">
AREAS_BY_ZONE[${a.zoneId}] = AREAS_BY_ZONE[${a.zoneId}] || [];
AREAS_BY_ZONE[${a.zoneId}].push({name:"${fn:escapeXml(a.areaName)}",type:"${fn:escapeXml(a.areaType)}"});
</c:forEach>

function onZoneChange(){
    var zid=document.getElementById('r_zone').value;
    var hint=document.getElementById('r_zoneHint');
    var fields=document.getElementById('roomFields');
    if(!zid){hint.style.display='none';fields.classList.remove('show');return;}
    var list=AREAS_BY_ZONE[zid]||[];
    if(list.length){hint.innerHTML='<b>Khu vực này đã có '+list.length+' phòng/sân:</b> '+list.map(function(x){return x.name+' ('+x.type+')';}).join(', ');}
    else{hint.innerHTML='Khu vực này <b>chưa có</b> phòng/sân nào.';}
    hint.style.display='block';
    fields.classList.add('show');
}
function openRoomModal(){document.getElementById('roomTitle').textContent='Thêm phòng/sân thi';
    ['r_id','r_name','r_capacity','r_location'].forEach(k=>document.getElementById(k).value='');
    document.getElementById('r_zone').value='';document.getElementById('r_type').value='';
    document.getElementById('roomErr').classList.remove('show');document.getElementById('r_zoneHint').style.display='none';
    document.getElementById('roomFields').classList.remove('show');
    document.getElementById('roomModal').classList.add('is-open');}
function editRoom(b){document.getElementById('roomTitle').textContent='Chỉnh sửa phòng/sân thi';
    document.getElementById('r_id').value=b.dataset.id;document.getElementById('r_zone').value=b.dataset.zone;
    document.getElementById('r_name').value=b.dataset.name;document.getElementById('r_type').value=b.dataset.type;
    document.getElementById('r_capacity').value=(b.dataset.capacity==='0'||b.dataset.capacity==='')?'':b.dataset.capacity;
    document.getElementById('r_location').value=b.dataset.location;
    document.getElementById('roomErr').classList.remove('show');onZoneChange();
    document.getElementById('roomModal').classList.add('is-open');}
function closeRoomModal(){document.getElementById('roomModal').classList.remove('is-open');}
function delRoom(id,name){if(confirm('Xóa "'+name+'"?\nKhông thể hoàn tác.')){document.getElementById('delId').value=id;document.getElementById('delForm').submit();}}
document.addEventListener('keydown',e=>{if(e.key==='Escape')closeRoomModal();});
</script>

<c:if test="${sessionScope.reopenModal eq 'room'}">
<script>
document.getElementById('roomTitle').textContent='${sessionScope.f_mode eq "edit" ? "Chỉnh sửa phòng/sân thi" : "Thêm phòng/sân thi"}';
document.getElementById('r_id').value='${sessionScope.f_areaId}';
document.getElementById('r_zone').value='${sessionScope.f_zoneId}';
document.getElementById('r_name').value="${fn:escapeXml(sessionScope.f_areaName)}";
document.getElementById('r_type').value="${fn:escapeXml(sessionScope.f_areaType)}";
document.getElementById('r_capacity').value="${fn:escapeXml(sessionScope.f_capacity)}";
document.getElementById('r_location').value="${fn:escapeXml(sessionScope.f_location)}";
onZoneChange();
var e=document.getElementById('roomErr');e.textContent="${fn:escapeXml(sessionScope.flashMessage)}";e.classList.add('show');
document.getElementById('roomModal').classList.add('is-open');
</script>
<c:remove var="reopenModal" scope="session" /><c:remove var="flashMessage" scope="session" /><c:remove var="flashType" scope="session" />
</c:if>

</body></html>

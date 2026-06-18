<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>

<c:set var="ctx" value="${pageContext.request.contextPath}" />

<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Quản lý Phòng thi - Lái Vui</title>
    <link rel="preconnect" href="https://fonts.googleapis.com">
    <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
    <link href="https://fonts.googleapis.com/css2?family=Be+Vietnam+Pro:wght@400;500;600;700;800&family=Inter:wght@400;500;600;700;800&display=swap" rel="stylesheet">
    <link rel="stylesheet" href="${ctx}/assets/css/style.css">
    <link rel="stylesheet" href="${ctx}/assets/css/layout.css">
</head>
<body class="has-side-nav-bar">

<jsp:include page="/views/layout/sidebar-admin.jsp">
    <jsp:param name="activeSidebar" value="phong-thi" />
</jsp:include>

<div class="dashboard-shell">
    <main class="main-content">

        <nav class="breadcrumbs" aria-label="Breadcrumb">
            <a href="${ctx}/views/public/home.jsp">Trang chủ</a>
            <span class="breadcrumbs__separator" aria-hidden="true">/</span>
            <a href="${ctx}/admin/dashboard">Quản trị</a>
            <span class="breadcrumbs__separator" aria-hidden="true">/</span>
            <span class="breadcrumbs__current" aria-current="page">Phòng thi</span>
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
                <h1 class="page-title">Quản lý Phòng thi</h1>
                <p class="page-subtitle">Cấu hình phòng thi lý thuyết và thực hành, thiết lập sức chứa tối đa và trạng thái hoạt động toàn hệ thống.</p>
            </div>
            <div class="page-actions" style="display: flex; gap: 10px;">
                <button type="button" class="btn-filter" id="btn-add-room" onclick="openRoomModal()" style="height: 42px; padding: 0 1.25rem; font-size: 0.9rem; border-radius: 8px; flex: none; cursor: pointer; display:inline-flex; align-items:center; gap:6px;">
                    <svg width="16" height="16" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                        <path d="M12 5v14M5 12h14" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round"/>
                    </svg>
                    Thêm phòng thi
                </button>
            </div>
        </header>

        <section class="metrics-row" aria-label="Thống kê phòng thi">
            <div class="stat-card">
                <div class="stat-icon stat-icon--blue">
                    <svg width="24" height="24" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                        <path d="M3 21V7L12 3L21 7V21" stroke="currentColor" stroke-width="2" stroke-linejoin="round"/>
                        <path d="M9 21V15H15V21" stroke="currentColor" stroke-width="2" stroke-linejoin="round"/>
                    </svg>
                </div>
                <div class="stat-info">
                    <span class="stat-number">${empty totalRooms ? 0 : totalRooms}</span>
                    <span class="stat-label">Tổng phòng thi</span>
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
                    <span class="stat-number">${empty activeRooms ? 0 : activeRooms}</span>
                    <span class="stat-label">Đang hoạt động</span>
                    <span class="stat-trend stat-trend--up">Sẵn sàng sử dụng</span>
                </div>
            </div>
            <div class="stat-card">
                <div class="stat-icon stat-icon--blue">
                    <svg width="24" height="24" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                        <rect x="2" y="3" width="20" height="14" rx="2" stroke="currentColor" stroke-width="2"/>
                        <path d="M8 21h8M12 17v4" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
                    </svg>
                </div>
                <div class="stat-info">
                    <span class="stat-number">${empty theoryRooms ? 0 : theoryRooms}</span>
                    <span class="stat-label">Phòng lý thuyết</span>
                    <span class="stat-trend stat-trend--up">Thi trắc nghiệm</span>
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
                    <span class="stat-number">${empty practicalRooms ? 0 : practicalRooms}</span>
                    <span class="stat-label">Phòng thực hành</span>
                    <span class="stat-trend stat-trend--up">Sân thi sa hình</span>
                </div>
            </div>
        </section>

        <section class="filter-panel" aria-label="Bộ lọc phòng thi">
            <h2 class="filter-title">
                <svg width="18" height="18" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                    <path d="M22 3H2l8 9.46V19l4 2v-8.54L22 3z" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                </svg>
                Bộ lọc tìm kiếm
            </h2>
            <form action="${ctx}/admin/exam-room" method="GET">
                <div class="filter-grid" style="grid-template-columns: 2fr 1.25fr 1.1fr 1.1fr 1.75fr;">
                    <div class="input-group">
                        <label for="searchKeyword" class="input-label">Tìm phòng thi</label>
                        <input type="text" id="searchKeyword" name="searchKeyword" class="input-field"
                               placeholder="Nhập tên phòng thi..." value="${param.searchKeyword}">
                    </div>
                    <div class="input-group">
                        <label for="filterArea" class="input-label">Khu vực thi</label>
                        <select id="filterArea" name="filterArea" class="input-field">
                            <option value="">Tất cả khu vực</option>
                            <c:forEach var="area" items="${examAreas}">
                                <option value="${area.examAreaId}" ${param.filterArea eq area.examAreaId ? 'selected' : ''}>${area.areaName}</option>
                            </c:forEach>
                        </select>
                    </div>
                    <div class="input-group">
                        <label for="filterType" class="input-label">Loại phòng</label>
                        <select id="filterType" name="filterType" class="input-field">
                            <option value="">Tất cả loại</option>
                            <option value="theory" ${param.filterType eq 'theory' ? 'selected' : ''}>Lý thuyết</option>
                            <option value="practical" ${param.filterType eq 'practical' ? 'selected' : ''}>Thực hành</option>
                        </select>
                    </div>
                    <div class="input-group">
                        <label for="filterStatus" class="input-label">Trạng thái</label>
                        <select id="filterStatus" name="filterStatus" class="input-field">
                            <option value="">Tất cả trạng thái</option>
                            <option value="active" ${param.filterStatus eq 'active' ? 'selected' : ''}>Hoạt động</option>
                            <option value="maintenance" ${param.filterStatus eq 'maintenance' ? 'selected' : ''}>Bảo trì</option>
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
                            <a href="${ctx}/admin/exam-room" class="btn-reset">Đặt lại</a>
                        </div>
                    </div>
                </div>
            </form>
        </section>

        <section class="log-card" aria-label="Danh sách phòng thi">
            <header class="log-card-header">
                <h2 class="log-card-title">
                    <svg width="20" height="20" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg" style="color: #0052cc;">
                        <path d="M3 21V7L12 3L21 7V21" stroke="currentColor" stroke-width="2" stroke-linejoin="round"/>
                        <path d="M9 21V15H15V21" stroke="currentColor" stroke-width="2" stroke-linejoin="round"/>
                    </svg>
                    Danh sách phòng thi
                    <c:if test="${not empty examRooms}">
                        <span style="font-size: 0.78rem; font-weight: 600; background: rgba(0,82,204,0.08); color: #0052cc; padding: 2px 10px; border-radius: 9999px; margin-left: 6px;">
                            ${fn:length(examRooms)} phòng
                        </span>
                    </c:if>
                </h2>
            </header>

            <div class="table-responsive">
                <table class="audit-table">
                    <thead>
                        <tr>
                            <th scope="col" class="col-id">STT</th>
                            <th scope="col" style="width: 120px;">Mã phòng</th>
                            <th scope="col">Tên phòng thi</th>
                            <th scope="col">Khu vực</th>
                            <th scope="col" style="width: 130px; text-align: center;">Loại phòng</th>
                            <th scope="col" style="width: 100px; text-align: center;">Sức chứa</th>
                            <th scope="col" style="width: 110px; text-align: center;">Số máy thi</th>
                            <th scope="col" style="width: 130px; text-align: center;">Trạng thái</th>
                            <th scope="col" style="text-align: center; width: 250px;">Thao tác</th>
                        </tr>
                    </thead>
                    <tbody>
                        <c:choose>
                            <c:when test="${not empty examRooms}">
                                <c:forEach var="room" items="${examRooms}" varStatus="status">
                                    <tr>
                                        <td class="col-id">${status.index + 1}</td>
                                        <td style="font-weight: 700; color: #0052cc; font-family: monospace; font-size: 0.9rem;">${room.code}</td>
                                        <td>
                                            <div class="user-cell">
                                                <div class="user-avatar" style="background: ${room.type eq 'theory' ? 'linear-gradient(135deg,#0052cc,#003d9b)' : 'linear-gradient(135deg,#10b981,#059669)'}; border-radius: 8px;">
                                                    <svg width="14" height="14" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                                                        <path d="M3 21V7L12 3L21 7V21" stroke="currentColor" stroke-width="2"/>
                                                    </svg>
                                                </div>
                                                <div class="user-info">
                                                    <span class="user-name">${room.name}</span>
                                                    <span class="user-username">Tầng ${empty room.floor ? '-' : room.floor}</span>
                                                </div>
                                            </div>
                                        </td>
                                        <td>
                                            <div class="user-info">
                                                <span class="user-name" style="font-size: 0.88rem;">${room.areaName}</span>
                                                <span class="user-username">${room.areaCode}</span>
                                            </div>
                                        </td>
                                        <td style="text-align: center;">
                                            <c:choose>
                                                <c:when test="${room.type eq 'theory'}"><span class="role-badge role-badge--admin">Lý thuyết</span></c:when>
                                                <c:otherwise><span class="role-badge role-badge--coi">Thực hành</span></c:otherwise>
                                            </c:choose>
                                        </td>
                                        <td style="text-align: center;">
                                            <span style="font-size: 1rem; font-weight: 700; color: #0f172a;">${empty room.capacity ? '-' : room.capacity}</span>
                                            <span style="font-size: 0.75rem; color: #64748b; display: block;">người</span>
                                        </td>
                                        <td style="text-align: center;">
                                            <c:choose>
                                                <c:when test="${room.type eq 'theory'}">
                                                    <span style="font-size: 1rem; font-weight: 700; color: #0f172a;">${room.computerCount}</span>
                                                    <span style="font-size: 0.75rem; color: #64748b; display: block;">máy thi</span>
                                                </c:when>
                                                <c:otherwise><span style="color: #94a3b8; font-size: 0.85rem;">—</span></c:otherwise>
                                            </c:choose>
                                        </td>
                                        <td style="text-align: center;">
                                            <c:choose>
                                                <c:when test="${room.status eq 'active'}"><span class="action-badge action-badge--success">Hoạt động</span></c:when>
                                                <c:when test="${room.status eq 'maintenance'}"><span class="action-badge action-badge--warning">Bảo trì</span></c:when>
                                                <c:otherwise><span class="action-badge action-badge--danger">Tạm dừng</span></c:otherwise>
                                            </c:choose>
                                        </td>
                                        <td>
                                            <div style="display: flex; gap: 5px; justify-content: center; flex-wrap: wrap;">
                                                <c:if test="${room.type eq 'theory'}">
                                                    <a href="${ctx}/admin/exam-computer?filterRoom=${room.id}"
                                                       class="btn-export"
                                                       style="padding: 4px 8px; font-size: 0.78rem; border-radius: 6px; border-color: rgba(139,92,246,0.25); color: #7c3aed; text-decoration: none;">
                                                        Máy thi
                                                    </a>
                                                </c:if>
                                                <button type="button" class="btn-export"
                                                        style="padding: 4px 10px; font-size: 0.8rem; border-radius: 6px; border-color: rgba(2,132,199,0.25); color: #0284c7; cursor:pointer;"
                                                        data-id="${room.id}" data-code="${room.code}" data-name="${fn:escapeXml(room.name)}"
                                                        data-type="${room.type}" data-typelabel="${room.type eq 'theory' ? 'Lý thuyết' : 'Thực hành'}"
                                                        data-capacity="${room.capacity}" data-floor="${fn:escapeXml(room.floor)}"
                                                        data-area="${fn:escapeXml(room.areaName)}" data-areacode="${room.areaCode}"
                                                        data-count="${room.computerCount}" data-status="${room.status}"
                                                        onclick="openRoomDetail(this)">
                                                    Chi tiết
                                                </button>
                                                <button type="button" class="btn-export"
                                                        style="padding: 4px 10px; font-size: 0.8rem; border-radius: 6px; border-color: rgba(245,158,11,0.25); color: #d97706; cursor:pointer;"
                                                        data-id="${room.id}" data-name="${fn:escapeXml(room.name)}" data-type="${room.type}"
                                                        data-capacity="${room.capacity}" data-floor="${fn:escapeXml(room.floor)}"
                                                        data-status="${room.status}" data-area="${room.examAreaId}"
                                                        onclick="openRoomModalEdit(this)">
                                                    Sửa
                                                </button>
                                                <button type="button" class="btn-export"
                                                        style="padding: 4px 10px; font-size: 0.8rem; border-radius: 6px; border-color: rgba(239,68,68,0.25); color: #dc2626; cursor:pointer;"
                                                        onclick="deleteRoom('${room.id}', '${fn:escapeXml(room.name)}')">
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
                                            <path d="M3 21V7L12 3L21 7V21" stroke="currentColor" stroke-width="2" stroke-linejoin="round"/>
                                            <path d="M9 21V15H15V21" stroke="currentColor" stroke-width="2" stroke-linejoin="round"/>
                                        </svg>
                                        Chưa có phòng thi nào trong hệ thống.
                                        <p style="font-size: 0.82rem; font-weight: 400; color: #94a3b8; margin-top: 0.5rem; max-width: 420px; margin-left: auto; margin-right: auto;">
                                            Nhấn <strong>Thêm phòng thi</strong> để cấu hình phòng thi đầu tiên, hoặc điều chỉnh bộ lọc để tìm phòng phù hợp.
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
                    Hiển thị <c:choose><c:when test="${not empty examRooms}">1 - ${fn:length(examRooms)}</c:when><c:otherwise>0</c:otherwise></c:choose>
                    trong tổng số ${empty totalRooms ? 0 : totalRooms} phòng thi
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

<%-- delete form --%>
<form id="deleteRoomForm" action="${ctx}/admin/exam-room" method="POST" style="display:none;">
    <input type="hidden" name="action" value="delete">
    <input type="hidden" name="id" id="deleteRoomId">
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
<div id="roomModal" class="modal-overlay" onclick="if(event.target===this)closeRoomModal()">
    <div class="modal-card" role="dialog" aria-modal="true">
        <form action="${ctx}/admin/exam-room?action=save" method="POST">
            <div class="modal-head">
                <h3 id="roomModalTitle">Thêm phòng thi</h3>
                <button type="button" class="modal-close" onclick="closeRoomModal()">&times;</button>
            </div>
            <div class="modal-body">
                <input type="hidden" name="examRoomId" id="r_id" value="">
                <div class="input-group" style="margin-bottom:1.25rem;">
                    <label for="r_name" class="input-label">Tên phòng thi <span style="color:#dc2626;">*</span></label>
                    <input type="text" id="r_name" name="roomName" class="input-field" placeholder="VD: Phòng thi lý thuyết A01" required>
                </div>
                <div class="filter-grid" style="grid-template-columns:1fr 1fr; gap:1.25rem; margin-bottom:1.25rem;">
                    <div class="input-group">
                        <label for="r_type" class="input-label">Loại phòng <span style="color:#dc2626;">*</span></label>
                        <select id="r_type" name="roomType" class="input-field" required>
                            <option value="">-- Chọn loại --</option>
                            <option value="theory">Lý thuyết</option>
                            <option value="practical">Thực hành</option>
                        </select>
                    </div>
                    <div class="input-group">
                        <label for="r_status" class="input-label">Trạng thái <span style="color:#dc2626;">*</span></label>
                        <select id="r_status" name="status" class="input-field" required>
                            <option value="">-- Chọn trạng thái --</option>
                            <option value="active">Hoạt động</option>
                            <option value="maintenance">Bảo trì</option>
                            <option value="inactive">Tạm dừng</option>
                        </select>
                    </div>
                </div>
                <div class="filter-grid" style="grid-template-columns:1fr 1fr; gap:1.25rem; margin-bottom:1.25rem;">
                    <div class="input-group">
                        <label for="r_capacity" class="input-label">Sức chứa (người)</label>
                        <input type="number" id="r_capacity" name="capacity" class="input-field" min="0" placeholder="VD: 40">
                    </div>
                    <div class="input-group">
                        <label for="r_floor" class="input-label">Tầng</label>
                        <input type="text" id="r_floor" name="floor" class="input-field" placeholder="VD: 1, Trệt...">
                    </div>
                </div>
                <div class="input-group">
                    <label for="r_area" class="input-label">Khu vực thi <span style="color:#dc2626;">*</span></label>
                    <select id="r_area" name="examAreaId" class="input-field" required>
                        <option value="">-- Chọn khu vực --</option>
                        <c:forEach var="area" items="${examAreas}">
                            <option value="${area.examAreaId}">${area.areaName}</option>
                        </c:forEach>
                    </select>
                </div>
            </div>
            <div class="modal-foot">
                <button type="button" class="btn-reset" onclick="closeRoomModal()" style="height:44px; padding:0 1.5rem; display:inline-flex; align-items:center;">Hủy bỏ</button>
                <button type="submit" class="btn-filter" style="height:44px; padding:0 1.5rem;">Lưu phòng thi</button>
            </div>
        </form>
    </div>
</div>

<%-- Detail modal (read-only) --%>
<div id="roomDetail" class="modal-overlay" onclick="if(event.target===this)closeRoomDetail()">
    <div class="modal-card" role="dialog" aria-modal="true" style="max-width:520px;">
        <div class="modal-head">
            <h3>Chi tiết phòng thi</h3>
            <button type="button" class="modal-close" onclick="closeRoomDetail()">&times;</button>
        </div>
        <div class="modal-body">
            <div class="detail-row"><span>Mã phòng</span><span id="d_code"></span></div>
            <div class="detail-row"><span>Tên phòng thi</span><span id="d_name"></span></div>
            <div class="detail-row"><span>Loại phòng</span><span id="d_type"></span></div>
            <div class="detail-row"><span>Sức chứa</span><span id="d_capacity"></span></div>
            <div class="detail-row"><span>Tầng</span><span id="d_floor"></span></div>
            <div class="detail-row"><span>Khu vực</span><span id="d_area"></span></div>
            <div class="detail-row"><span>Số máy thi</span><span id="d_count"></span></div>
            <div class="detail-row" style="border-bottom:none;"><span>Trạng thái</span><span id="d_status"></span></div>
        </div>
        <div class="modal-foot">
            <button type="button" class="btn-filter" onclick="closeRoomDetail()" style="height:44px; padding:0 1.5rem;">Đóng</button>
        </div>
    </div>
</div>

<script>
    function openRoomModal() {
        document.getElementById('roomModalTitle').textContent = 'Thêm phòng thi';
        ['r_id','r_name','r_type','r_status','r_capacity','r_floor','r_area'].forEach(function(k){document.getElementById(k).value='';});
        document.getElementById('roomModal').classList.add('is-open');
    }
    function openRoomModalEdit(b) {
        document.getElementById('roomModalTitle').textContent = 'Chỉnh sửa phòng thi';
        document.getElementById('r_id').value = b.dataset.id;
        document.getElementById('r_name').value = b.dataset.name;
        document.getElementById('r_type').value = b.dataset.type;
        document.getElementById('r_status').value = b.dataset.status;
        document.getElementById('r_capacity').value = (b.dataset.capacity && b.dataset.capacity !== '') ? b.dataset.capacity : '';
        document.getElementById('r_floor').value = b.dataset.floor || '';
        document.getElementById('r_area').value = b.dataset.area;
        document.getElementById('roomModal').classList.add('is-open');
    }
    function closeRoomModal() { document.getElementById('roomModal').classList.remove('is-open'); }

    function openRoomDetail(b) {
        document.getElementById('d_code').textContent = b.dataset.code;
        document.getElementById('d_name').textContent = b.dataset.name;
        document.getElementById('d_type').textContent = b.dataset.typelabel;
        document.getElementById('d_capacity').textContent = (b.dataset.capacity && b.dataset.capacity !== '') ? b.dataset.capacity + ' người' : '—';
        document.getElementById('d_floor').textContent = b.dataset.floor && b.dataset.floor !== '' ? b.dataset.floor : '—';
        document.getElementById('d_area').textContent = b.dataset.area + ' (' + b.dataset.areacode + ')';
        document.getElementById('d_count').textContent = b.dataset.type === 'theory' ? (b.dataset.count + ' máy') : '—';
        var st = b.dataset.status;
        document.getElementById('d_status').textContent = st === 'active' ? 'Hoạt động' : (st === 'maintenance' ? 'Bảo trì' : 'Tạm dừng');
        document.getElementById('roomDetail').classList.add('is-open');
    }
    function closeRoomDetail() { document.getElementById('roomDetail').classList.remove('is-open'); }

    function deleteRoom(id, name) {
        if (confirm('Bạn có chắc chắn muốn xóa phòng thi "' + name + '"?\nThao tác này không thể hoàn tác.')) {
            document.getElementById('deleteRoomId').value = id;
            document.getElementById('deleteRoomForm').submit();
        }
    }
    document.addEventListener('keydown', function(e){ if(e.key==='Escape'){closeRoomModal();closeRoomDetail();} });
</script>

</body>
</html>

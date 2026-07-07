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

    <link rel="preconnect" href="https://fonts.googleapis.com">
    <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
    <link href="https://fonts.googleapis.com/css2?family=Be+Vietnam+Pro:wght@400;500;600;700;800&family=Inter:wght@400;500;600;700;800&display=swap" rel="stylesheet">

    <jsp:include page="/views/admin/components/admin-styles.jsp" />
</head>
<body class="has-side-nav-bar">

<jsp:include page="/views/admin/components/sidebar.jsp">
    <jsp:param name="activeSidebar" value="khu-vuc" />
</jsp:include>

<div class="dashboard-shell">
    <main class="main-content">

        <nav class="breadcrumbs" aria-label="Breadcrumb">
            <a href="${ctx}/views/public/home.jsp">Trang chủ</a>
            <span class="breadcrumbs__separator" aria-hidden="true">/</span>
            <a href="${ctx}/admin/dashboard">Quản trị</a>
            <span class="breadcrumbs__separator" aria-hidden="true">/</span>
            <span class="breadcrumbs__current" aria-current="page">Khu vực thi</span>
        </nav>

        <c:if test="${not empty sessionScope.flashMessage}">
            <div style="margin-bottom: 1.25rem; padding: 0.85rem 1.1rem; border-radius: 10px; font-weight: 600; font-size: 0.9rem; display: flex; align-items: center; gap: 10px;
                        background: ${sessionScope.flashType eq 'success' ? 'rgba(16,185,129,0.08)' : 'rgba(239,68,68,0.08)'};
                        border: 1px solid ${sessionScope.flashType eq 'success' ? 'rgba(16,185,129,0.25)' : 'rgba(239,68,68,0.25)'};
                        color: ${sessionScope.flashType eq 'success' ? '#047857' : '#b91c1c'};">
                <svg width="18" height="18" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                    <circle cx="12" cy="12" r="10" stroke="currentColor" stroke-width="2"/>
                    <path d="M8 12.5l3 3 5-6" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                </svg>
                ${sessionScope.flashMessage}
            </div>
            <c:remove var="flashMessage" scope="session" />
            <c:remove var="flashType" scope="session" />
        </c:if>

        <header class="page-header">
            <div class="page-title-wrap">
                <h1 class="page-title">Quản lý Khu vực thi</h1>
                <p class="page-subtitle">Quản lý danh sách khu vực sát hạch, loại khu vực, sức chứa và địa điểm chi tiết.</p>
            </div>
            <div class="page-actions" style="display: flex; gap: 10px;">
                <button type="button" class="btn-filter" onclick="openAreaModal()" style="height: 42px; padding: 0 1.25rem; font-size: 0.9rem; border-radius: 8px; flex: none; display: inline-flex; align-items: center; gap: 6px; cursor: pointer;">
                    <svg width="16" height="16" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                        <path d="M12 5v14M5 12h14" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round"/>
                    </svg>
                    Thêm khu vực
                </button>
            </div>
        </header>

        <section class="filter-panel" aria-label="Bộ lọc tìm kiếm">
            <h2 class="filter-title">
                <svg width="18" height="18" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                    <path d="M22 3H2l8 9.46V19l4 2v-8.54L22 3z" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                </svg>
                Bộ lọc tìm kiếm
            </h2>
            <form action="${ctx}/admin/exam-area" method="GET">
                <div class="filter-grid" style="grid-template-columns: 2.5fr 1.5fr 1.5fr;">
                    <div class="input-group">
                        <label for="searchKeyword" class="input-label">Tìm khu vực</label>
                        <input type="text" id="searchKeyword" name="searchKeyword" class="input-field"
                               placeholder="Nhập tên khu vực hoặc địa điểm..." value="${param.searchKeyword}">
                    </div>
                    <div class="input-group">
                        <label for="filterType" class="input-label">Loại khu vực</label>
                        <select id="filterType" name="filterType" class="input-field">
                            <option value="">Tất cả loại</option>
                            <option value="Lý thuyết" ${param.filterType eq 'Lý thuyết' ? 'selected' : ''}>Lý thuyết</option>
                            <option value="Thực hành" ${param.filterType eq 'Thực hành' ? 'selected' : ''}>Thực hành</option>
                            <option value="Hỗn hợp" ${param.filterType eq 'Hỗn hợp' ? 'selected' : ''}>Hỗn hợp</option>
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
                            <a href="${ctx}/admin/exam-area" class="btn-reset">Đặt lại</a>
                        </div>
                    </div>
                </div>
            </form>
        </section>

        <section class="log-card" aria-label="Danh sách khu vực thi">
            <header class="log-card-header">
                <h2 class="log-card-title">
                    <svg width="20" height="20" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg" style="color: #0052cc;">
                        <path d="M12 2C8.13 2 5 5.13 5 9C5 14.25 12 22 12 22C12 22 19 14.25 19 9C19 5.13 15.87 2 12 2Z" stroke="currentColor" stroke-width="2" stroke-linejoin="round"/>
                        <circle cx="12" cy="9" r="2.5" stroke="currentColor" stroke-width="2"/>
                    </svg>
                    Danh sách khu vực thi
                    <c:if test="${not empty examAreas}">
                        <span style="font-size: 0.78rem; font-weight: 600; background: rgba(0,82,204,0.08); color: #0052cc; padding: 2px 10px; border-radius: 9999px; margin-left: 6px;">
                            ${fn:length(examAreas)} khu vực
                        </span>
                    </c:if>
                </h2>
            </header>

            <div class="table-responsive">
                <table class="audit-table">
                    <thead>
                        <tr>
                            <th scope="col" class="col-id">STT</th>
                            <th scope="col" style="width: 120px;">Mã khu vực</th>
                            <th scope="col">Tên khu vực thi</th>
                            <th scope="col" style="width: 140px; text-align: center;">Loại</th>
                            <th scope="col" style="width: 120px; text-align: center;">Sức chứa</th>
                            <th scope="col">Địa điểm</th>
                            <th scope="col" style="text-align: center; width: 160px;">Thao tác</th>
                        </tr>
                    </thead>
                    <tbody>
                        <c:choose>
                            <c:when test="${not empty examAreas}">
                                <c:forEach var="area" items="${examAreas}" varStatus="status">
                                    <tr>
                                        <td class="col-id">${status.index + 1}</td>
                                        <td style="font-weight: 700; color: #0052cc; font-family: monospace; font-size: 0.9rem;">${area.code}</td>
                                        <td>
                                            <div class="user-cell">
                                                <div class="user-avatar" style="background: linear-gradient(135deg, #0052cc, #003d9b); border-radius: 8px;">
                                                    <svg width="14" height="14" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                                                        <path d="M12 2C8.13 2 5 5.13 5 9C5 14.25 12 22 12 22C12 22 19 14.25 19 9C19 5.13 15.87 2 12 2Z" stroke="currentColor" stroke-width="2"/>
                                                        <circle cx="12" cy="9" r="2.5" stroke="currentColor" stroke-width="2"/>
                                                    </svg>
                                                </div>
                                                <div class="user-info">
                                                    <span class="user-name">${area.areaName}</span>
                                                </div>
                                            </div>
                                        </td>
                                        <td style="text-align: center;">
                                            <span class="role-badge role-badge--coi">${area.areaType}</span>
                                        </td>
                                        <td style="text-align: center;">
                                            <span style="font-size: 1.1rem; font-weight: 700; color: #0f172a;">${area.capacity}</span>
                                            <span style="font-size: 0.8rem; color: #64748b; display: block;">người</span>
                                        </td>
                                        <td class="details-cell">${area.location}</td>
                                        <td>
                                            <div style="display: flex; gap: 6px; justify-content: center;">
                                                <button type="button"
                                                   class="btn-export"
                                                   style="padding: 4px 10px; font-size: 0.8rem; border-radius: 6px; border-color: rgba(245,158,11,0.25); color: #d97706; cursor: pointer;"
                                                   data-id="${area.examAreaId}"
                                                   data-name="${fn:escapeXml(area.areaName)}"
                                                   data-type="${fn:escapeXml(area.areaType)}"
                                                   data-capacity="${area.capacity}"
                                                   data-location="${fn:escapeXml(area.location)}"
                                                   onclick="openAreaModalEdit(this)">
                                                    Sửa
                                                </button>
                                                <button class="btn-export"
                                                        style="padding: 4px 10px; font-size: 0.8rem; border-radius: 6px; border-color: rgba(239,68,68,0.25); color: #dc2626;"
                                                        onclick="deleteArea('${area.examAreaId}', '${fn:escapeXml(area.areaName)}')">
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
                                        <svg width="64" height="64" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg"
                                             style="margin: 0 auto 1.5rem; display: block; opacity: 0.25; color: #64748b;">
                                            <path d="M12 2C8.13 2 5 5.13 5 9C5 14.25 12 22 12 22C12 22 19 14.25 19 9C19 5.13 15.87 2 12 2Z" stroke="currentColor" stroke-width="2" stroke-linejoin="round"/>
                                            <circle cx="12" cy="9" r="2.5" stroke="currentColor" stroke-width="2"/>
                                        </svg>
                                        Chưa có khu vực thi nào trong hệ thống.
                                        <p style="font-size: 0.82rem; font-weight: 400; color: #94a3b8; margin-top: 0.5rem; max-width: 400px; margin-left: auto; margin-right: auto;">
                                            Nhấn <strong>Thêm khu vực</strong> phía trên để bắt đầu cấu hình khu vực thi đầu tiên cho hệ thống.
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
                    Hiển thị
                    <c:choose>
                        <c:when test="${not empty examAreas}">1 - ${fn:length(examAreas)}</c:when>
                        <c:otherwise>0</c:otherwise>
                    </c:choose>
                    trong tổng số ${empty totalAreas ? 0 : totalAreas} khu vực
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

<%-- Hidden form for delete (POST) --%>
<form id="deleteForm" action="${ctx}/admin/exam-area" method="POST" style="display:none;">
    <input type="hidden" name="action" value="delete">
    <input type="hidden" name="id" id="deleteId">
</form>

<%-- ===== In-page modal: Thêm / Sửa khu vực thi ===== --%>
<style>
    .modal-overlay {
        display: none; position: fixed; inset: 0; z-index: 1000;
        background: rgba(15, 23, 42, 0.45);
        align-items: flex-start; justify-content: center;
        padding: 4vh 1rem; overflow-y: auto;
    }
    .modal-overlay.is-open { display: flex; }
    .modal-card {
        width: 100%; max-width: 600px; background: #fff; border-radius: 16px;
        box-shadow: 0 20px 60px rgba(15, 23, 42, 0.25);
        font-family: 'Be Vietnam Pro', 'Inter', sans-serif;
        animation: modalIn .18s ease-out;
    }
    @keyframes modalIn { from { opacity: 0; transform: translateY(-12px); } to { opacity: 1; transform: none; } }
    .modal-head {
        display: flex; align-items: center; justify-content: space-between;
        padding: 1.25rem 1.5rem; border-bottom: 1px solid #e2e8f0;
    }
    .modal-head h3 { margin: 0; font-size: 1.1rem; font-weight: 800; color: #0f172a; }
    .modal-close {
        border: none; background: transparent; font-size: 1.5rem; line-height: 1;
        color: #94a3b8; cursor: pointer; padding: 0 4px;
    }
    .modal-close:hover { color: #0f172a; }
    .modal-body { padding: 1.5rem; }
    .modal-foot {
        display: flex; gap: 12px; justify-content: flex-end;
        padding: 1rem 1.5rem; border-top: 1px solid #e2e8f0;
    }
</style>

<div id="areaModal" class="modal-overlay" onclick="if(event.target===this)closeAreaModal()">
    <div class="modal-card" role="dialog" aria-modal="true" aria-labelledby="areaModalTitle">
        <form action="${ctx}/admin/exam-area?action=save" method="POST">
            <div class="modal-head">
                <h3 id="areaModalTitle">Thêm khu vực thi</h3>
                <button type="button" class="modal-close" onclick="closeAreaModal()" aria-label="Đóng">&times;</button>
            </div>
            <div class="modal-body">
                <input type="hidden" name="examAreaId" id="m_examAreaId" value="">

                <div class="input-group" style="margin-bottom: 1.25rem;">
                    <label for="m_areaName" class="input-label">Tên khu vực thi <span style="color:#dc2626;">*</span></label>
                    <input type="text" id="m_areaName" name="areaName" class="input-field"
                           placeholder="VD: Khu vực sát hạch trung tâm Hà Nội" required>
                </div>

                <div class="filter-grid" style="grid-template-columns: 1fr 1fr; gap: 1.25rem; margin-bottom: 1.25rem;">
                    <div class="input-group">
                        <label for="m_areaType" class="input-label">Loại khu vực <span style="color:#dc2626;">*</span></label>
                        <select id="m_areaType" name="areaType" class="input-field" required>
                            <option value="">-- Chọn loại --</option>
                            <option value="Lý thuyết">Lý thuyết</option>
                            <option value="Thực hành">Thực hành</option>
                            <option value="Hỗn hợp">Hỗn hợp</option>
                        </select>
                    </div>
                    <div class="input-group">
                        <label for="m_capacity" class="input-label">Sức chứa (người) <span style="color:#dc2626;">*</span></label>
                        <input type="number" id="m_capacity" name="capacity" class="input-field" min="1"
                               placeholder="VD: 50" required>
                    </div>
                </div>

                <div class="input-group">
                    <label for="m_location" class="input-label">Địa điểm <span style="color:#dc2626;">*</span></label>
                    <input type="text" id="m_location" name="location" class="input-field"
                           placeholder="VD: Số 1 Đường Lê Lợi, Quận 1, TP.HCM" required>
                </div>
            </div>
            <div class="modal-foot">
                <button type="button" class="btn-reset" onclick="closeAreaModal()" style="height: 44px; padding: 0 1.5rem; display: inline-flex; align-items: center;">Hủy bỏ</button>
                <button type="submit" class="btn-filter" style="height: 44px; padding: 0 1.5rem;">Lưu khu vực</button>
            </div>
        </form>
    </div>
</div>

<script>
    function deleteArea(areaId, areaName) {
        if (confirm('Bạn có chắc chắn muốn xóa khu vực "' + areaName + '"?\nThao tác này không thể hoàn tác.')) {
            document.getElementById('deleteId').value = areaId;
            document.getElementById('deleteForm').submit();
        }
    }

    function openAreaModal() {
        document.getElementById('areaModalTitle').textContent = 'Thêm khu vực thi';
        document.getElementById('m_examAreaId').value = '';
        document.getElementById('m_areaName').value = '';
        document.getElementById('m_areaType').value = '';
        document.getElementById('m_capacity').value = '';
        document.getElementById('m_location').value = '';
        document.getElementById('areaModal').classList.add('is-open');
    }

    function openAreaModalEdit(btn) {
        document.getElementById('areaModalTitle').textContent = 'Chỉnh sửa khu vực thi';
        document.getElementById('m_examAreaId').value = btn.dataset.id;
        document.getElementById('m_areaName').value = btn.dataset.name;
        document.getElementById('m_areaType').value = btn.dataset.type;
        document.getElementById('m_capacity').value = btn.dataset.capacity;
        document.getElementById('m_location').value = btn.dataset.location;
        document.getElementById('areaModal').classList.add('is-open');
    }

    function closeAreaModal() {
        document.getElementById('areaModal').classList.remove('is-open');
    }

    document.addEventListener('keydown', function (e) {
        if (e.key === 'Escape') closeAreaModal();
    });
</script>

</body>
</html>

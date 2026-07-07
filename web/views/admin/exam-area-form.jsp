<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>

<c:set var="ctx" value="${pageContext.request.contextPath}" />
<c:set var="isEdit" value="${mode eq 'edit'}" />

<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>${isEdit ? 'Sửa' : 'Thêm'} Khu vực thi - Lái Vui</title>

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
            <a href="${ctx}/admin/exam-area">Khu vực thi</a>
            <span class="breadcrumbs__separator" aria-hidden="true">/</span>
            <span class="breadcrumbs__current" aria-current="page">${isEdit ? 'Chỉnh sửa' : 'Thêm mới'}</span>
        </nav>

        <header class="page-header">
            <div class="page-title-wrap">
                <h1 class="page-title">${isEdit ? 'Chỉnh sửa Khu vực thi' : 'Thêm Khu vực thi mới'}</h1>
                <p class="page-subtitle">${isEdit ? 'Cập nhật thông tin khu vực sát hạch hiện có.' : 'Khai báo một khu vực sát hạch mới cho hệ thống.'}</p>
            </div>
        </header>

        <c:if test="${not empty error}">
            <div style="margin-bottom: 1.25rem; padding: 0.85rem 1.1rem; border-radius: 10px; font-weight: 600; font-size: 0.9rem; display: flex; align-items: center; gap: 10px;
                        background: rgba(239,68,68,0.08); border: 1px solid rgba(239,68,68,0.25); color: #b91c1c;">
                <svg width="18" height="18" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                    <circle cx="12" cy="12" r="10" stroke="currentColor" stroke-width="2"/>
                    <path d="M12 8v4M12 16h.01" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
                </svg>
                ${error}
            </div>
        </c:if>

        <section class="filter-panel" aria-label="Biểu mẫu khu vực thi" style="max-width: 760px;">
            <form action="${ctx}/admin/exam-area?action=save" method="POST">
                <input type="hidden" name="examAreaId" value="${area.examAreaId}">

                <div class="input-group" style="margin-bottom: 1.25rem;">
                    <label for="areaName" class="input-label">Tên khu vực thi <span style="color:#dc2626;">*</span></label>
                    <input type="text" id="areaName" name="areaName" class="input-field"
                           placeholder="VD: Khu vực sát hạch trung tâm Hà Nội" value="${area.areaName}" required>
                </div>

                <div class="filter-grid" style="grid-template-columns: 1fr 1fr; gap: 1.25rem; margin-bottom: 1.25rem;">
                    <div class="input-group">
                        <label for="areaType" class="input-label">Loại khu vực <span style="color:#dc2626;">*</span></label>
                        <select id="areaType" name="areaType" class="input-field" required>
                            <option value="">-- Chọn loại --</option>
                            <option value="Lý thuyết" ${area.areaType eq 'Lý thuyết' ? 'selected' : ''}>Lý thuyết</option>
                            <option value="Thực hành" ${area.areaType eq 'Thực hành' ? 'selected' : ''}>Thực hành</option>
                            <option value="Hỗn hợp" ${area.areaType eq 'Hỗn hợp' ? 'selected' : ''}>Hỗn hợp</option>
                        </select>
                    </div>
                    <div class="input-group">
                        <label for="capacity" class="input-label">Sức chứa (người) <span style="color:#dc2626;">*</span></label>
                        <input type="number" id="capacity" name="capacity" class="input-field" min="1"
                               placeholder="VD: 50" value="${area.capacity > 0 ? area.capacity : ''}" required>
                    </div>
                </div>

                <div class="input-group" style="margin-bottom: 1.5rem;">
                    <label for="location" class="input-label">Địa điểm <span style="color:#dc2626;">*</span></label>
                    <input type="text" id="location" name="location" class="input-field"
                           placeholder="VD: Số 1 Đường Lê Lợi, Quận 1, TP.HCM" value="${area.location}" required>
                </div>

                <div style="display: flex; gap: 12px;">
                    <button type="submit" class="btn-filter" style="height: 46px; padding: 0 1.75rem;">
                        <svg width="16" height="16" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                            <path d="M19 21H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h11l5 5v11a2 2 0 0 1-2 2z" stroke="currentColor" stroke-width="2" stroke-linejoin="round"/>
                            <path d="M17 21v-8H7v8M7 3v5h8" stroke="currentColor" stroke-width="2" stroke-linejoin="round"/>
                        </svg>
                        ${isEdit ? 'Cập nhật' : 'Lưu khu vực'}
                    </button>
                    <a href="${ctx}/admin/exam-area" class="btn-reset" style="height: 46px; line-height: 46px; padding: 0 1.75rem; display: inline-flex; align-items: center;">Hủy bỏ</a>
                </div>
            </form>
        </section>

    </main>
</div>

</body>
</html>

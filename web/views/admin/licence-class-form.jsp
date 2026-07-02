<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>

<c:set var="ctx" value="${pageContext.request.contextPath}" />
<c:set var="isEdit" value="${mode eq 'edit'}" />

<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>${isEdit ? 'Sửa' : 'Thêm'} Hạng GPLX - Lái Vui</title>

    <link rel="preconnect" href="https://fonts.googleapis.com">
    <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
    <link href="https://fonts.googleapis.com/css2?family=Be+Vietnam+Pro:wght@400;500;600;700;800&family=Inter:wght@400;500;600;700;800&display=swap" rel="stylesheet">

    <link rel="stylesheet" href="${ctx}/assets/css/style.css">
    <link rel="stylesheet" href="${ctx}/assets/css/layout.css">
</head>
<body class="has-side-nav-bar">

<jsp:include page="/views/layout/sidebar-admin.jsp">
    <jsp:param name="activeSidebar" value="hang-gplx" />
</jsp:include>

<div class="dashboard-shell">
    <main class="main-content">

        <nav class="breadcrumbs" aria-label="Breadcrumb">
            <a href="${ctx}/views/public/home.jsp">Trang chủ</a>
            <span class="breadcrumbs__separator" aria-hidden="true">/</span>
            <a href="${ctx}/admin/dashboard">Quản trị</a>
            <span class="breadcrumbs__separator" aria-hidden="true">/</span>
            <a href="${ctx}/admin/licence-class">Hạng GPLX</a>
            <span class="breadcrumbs__separator" aria-hidden="true">/</span>
            <span class="breadcrumbs__current" aria-current="page">${isEdit ? 'Chỉnh sửa' : 'Thêm mới'}</span>
        </nav>

        <header class="page-header">
            <div class="page-title-wrap">
                <h1 class="page-title">${isEdit ? 'Chỉnh sửa Hạng GPLX' : 'Thêm Hạng GPLX mới'}</h1>
                <p class="page-subtitle">${isEdit ? 'Cập nhật thông tin hạng giấy phép lái xe hiện có.' : 'Khai báo một hạng giấy phép lái xe mới cho hệ thống.'}</p>
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

        <section class="filter-panel" aria-label="Biểu mẫu hạng GPLX" style="max-width: 760px;">
            <form action="${ctx}/admin/licence-class?action=save" method="POST">
                <input type="hidden" name="licenceId" value="${licence.licenceId}">

                <div class="filter-grid" style="grid-template-columns: 1fr 1fr; gap: 1.25rem; margin-bottom: 1.25rem;">
                    <div class="input-group">
                        <label for="licenceClass" class="input-label">Mã hạng <span style="color:#dc2626;">*</span></label>
                        <input type="text" id="licenceClass" name="licenceClass" class="input-field"
                               placeholder="VD: A1, B2, C..." value="${licence.licenceClass}" required>
                    </div>
                    <div class="input-group">
                        <label for="upgradeFromLicenceId" class="input-label">Nâng hạng từ (không bắt buộc)</label>
                        <select id="upgradeFromLicenceId" name="upgradeFromLicenceId" class="input-field">
                            <option value="">-- Không nâng hạng --</option>
                            <c:forEach var="l" items="${licences}">
                                <c:if test="${l.licenceId ne licence.licenceId}">
                                    <option value="${l.licenceId}" ${licence.upgradeFromLicenceId eq l.licenceId ? 'selected' : ''}>
                                        Hạng ${l.licenceClass}
                                    </option>
                                </c:if>
                            </c:forEach>
                        </select>
                    </div>
                </div>

                <div class="filter-grid" style="grid-template-columns: 1fr 1fr; gap: 1.25rem; margin-bottom: 1.25rem;">
                    <div class="input-group">
                        <label for="minimumAge" class="input-label">Độ tuổi tối thiểu <span style="color:#dc2626;">*</span></label>
                        <input type="number" id="minimumAge" name="minimumAge" class="input-field" min="1"
                               placeholder="VD: 18" value="${licence.minimumAge > 0 ? licence.minimumAge : ''}" required>
                    </div>
                    <div class="input-group">
                        <label for="validForYears" class="input-label">Thời hạn (năm) <span style="color:#dc2626;">*</span></label>
                        <input type="number" id="validForYears" name="validForYears" class="input-field" min="1"
                               placeholder="VD: 10" value="${licence.validForYears > 0 ? licence.validForYears : ''}" required>
                    </div>
                </div>

                <div class="input-group" style="margin-bottom: 1.5rem;">
                    <label for="description" class="input-label">Mô tả & Phạm vi điều khiển</label>
                    <textarea id="description" name="description" class="input-field" rows="3"
                              style="resize: vertical; min-height: 90px; padding: 0.65rem 0.9rem;"
                              placeholder="VD: Xe mô tô hai bánh có dung tích xi-lanh từ 50 cm³ đến dưới 175 cm³...">${licence.description}</textarea>
                </div>

                <div style="display: flex; gap: 12px;">
                    <button type="submit" class="btn-filter" style="height: 46px; padding: 0 1.75rem;">
                        <svg width="16" height="16" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                            <path d="M19 21H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h11l5 5v11a2 2 0 0 1-2 2z" stroke="currentColor" stroke-width="2" stroke-linejoin="round"/>
                            <path d="M17 21v-8H7v8M7 3v5h8" stroke="currentColor" stroke-width="2" stroke-linejoin="round"/>
                        </svg>
                        ${isEdit ? 'Cập nhật' : 'Lưu hạng GPLX'}
                    </button>
                    <a href="${ctx}/admin/licence-class" class="btn-reset" style="height: 46px; line-height: 46px; padding: 0 1.75rem; display: inline-flex; align-items: center;">Hủy bỏ</a>
                </div>
            </form>
        </section>

    </main>
</div>

</body>
</html>

<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>

<c:set var="ctx" value="${pageContext.request.contextPath}" />

<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Quản lý Hạng GPLX - Lái Vui</title>

    <link rel="preconnect" href="https://fonts.googleapis.com">
    <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
    <link href="https://fonts.googleapis.com/css2?family=Be+Vietnam+Pro:wght@400;500;600;700;800&family=Inter:wght@400;500;600;700;800&display=swap" rel="stylesheet">

    <jsp:include page="/views/admin/components/admin-styles.jsp" />
</head>
<body class="has-side-nav-bar">

<jsp:include page="/views/admin/components/sidebar.jsp">
    <jsp:param name="activeSidebar" value="hang-gplx" />
</jsp:include>

<div class="dashboard-shell">
    <main class="main-content">

        <nav class="breadcrumbs" aria-label="Breadcrumb">
            <a href="${ctx}/views/public/home.jsp">Trang chủ</a>
            <span class="breadcrumbs__separator" aria-hidden="true">/</span>
            <a href="${ctx}/admin/dashboard">Quản trị</a>
            <span class="breadcrumbs__separator" aria-hidden="true">/</span>
            <span class="breadcrumbs__current" aria-current="page">Hạng GPLX</span>
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
                <h1 class="page-title">Quản lý Hạng GPLX</h1>
                <p class="page-subtitle">Cấu hình danh mục hạng giấy phép lái xe: độ tuổi tối thiểu, thời hạn và điều kiện nâng hạng.</p>
            </div>
            <div class="page-actions" style="display: flex; gap: 10px;">
                <button type="button" class="btn-filter" onclick="openLicenceModal()" style="height: 42px; padding: 0 1.25rem; font-size: 0.9rem; border-radius: 8px; flex: none; display: inline-flex; align-items: center; gap: 6px; cursor: pointer;">
                    <svg width="16" height="16" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                        <path d="M12 5v14M5 12h14" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round"/>
                    </svg>
                    Thêm hạng GPLX
                </button>
            </div>
        </header>

        <section class="filter-panel" aria-label="Bộ lọc hạng GPLX">
            <h2 class="filter-title">
                <svg width="18" height="18" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                    <path d="M22 3H2l8 9.46V19l4 2v-8.54L22 3z" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                </svg>
                Bộ lọc tìm kiếm
            </h2>
            <form action="${ctx}/admin/licence-class" method="GET">
                <div class="filter-grid" style="grid-template-columns: 3fr 1.5fr;">
                    <div class="input-group">
                        <label for="searchKeyword" class="input-label">Tìm hạng giấy phép</label>
                        <input type="text" id="searchKeyword" name="searchKeyword" class="input-field"
                               placeholder="Nhập mã hạng hoặc mô tả..." value="${param.searchKeyword}">
                    </div>
                    <div class="input-group filter-grid__btn-col">
                        <div class="btn-group">
                            <button type="submit" class="btn-filter">
                                <svg width="16" height="16" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                                    <path d="M21 21l-6-6m2-5a7 7 0 11-14 0 7 7 0 0114 0z" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round"/>
                                </svg>
                                Lọc
                            </button>
                            <a href="${ctx}/admin/licence-class" class="btn-reset">Đặt lại</a>
                        </div>
                    </div>
                </div>
            </form>
        </section>

        <section class="log-card" aria-label="Danh sách hạng GPLX">
            <header class="log-card-header">
                <h2 class="log-card-title">
                    <svg width="20" height="20" viewBox="0 0 20 20" fill="none" xmlns="http://www.w3.org/2000/svg" style="color: #0052cc;">
                        <rect x="1" y="4" width="18" height="12" rx="2" stroke="currentColor" stroke-width="2"/>
                        <circle cx="6" cy="10" r="2" stroke="currentColor" stroke-width="2"/>
                        <path d="M10 7.5H16M10 10H14M10 12.5H15" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
                    </svg>
                    Danh mục Hạng Giấy phép lái xe
                    <c:if test="${not empty licenceClasses}">
                        <span style="font-size: 0.78rem; font-weight: 600; background: rgba(0,82,204,0.08); color: #0052cc; padding: 2px 10px; border-radius: 9999px; margin-left: 6px;">
                            ${fn:length(licenceClasses)} hạng
                        </span>
                    </c:if>
                </h2>
            </header>

            <div class="table-responsive">
                <table class="audit-table">
                    <thead>
                        <tr>
                            <th scope="col" class="col-id">STT</th>
                            <th scope="col" style="width: 110px; text-align: center;">Mã hạng</th>
                            <th scope="col">Mô tả & Phạm vi điều khiển</th>
                            <th scope="col" style="width: 130px; text-align: center;">Độ tuổi tối thiểu</th>
                            <th scope="col" style="width: 120px; text-align: center;">Thời hạn (năm)</th>
                            <th scope="col" style="width: 140px; text-align: center;">Nâng hạng từ</th>
                            <th scope="col" style="text-align: center; width: 110px;">Thao tác</th>
                        </tr>
                    </thead>
                    <tbody>
                        <c:choose>
                            <c:when test="${not empty licenceClasses}">
                                <c:forEach var="grade" items="${licenceClasses}" varStatus="status">
                                    <tr>
                                        <td class="col-id">${status.index + 1}</td>
                                        <td style="text-align: center;">
                                            <span class="role-badge role-badge--admin"
                                                  style="font-size: 0.95rem; font-weight: 800; font-family: 'Inter', sans-serif; padding: 4px 14px; border-radius: 6px; background: rgba(0,82,204,0.06); color: #0052cc; border-color: rgba(0,82,204,0.18);">
                                                ${grade.licenceClass}
                                            </span>
                                        </td>
                                        <td>
                                            <div class="user-info" style="white-space: normal;">
                                                <span class="user-name" style="font-size: 0.92rem; font-weight: 600; color: #0f172a; white-space: normal;">
                                                    Hạng ${grade.licenceClass}
                                                </span>
                                                <span class="user-username" style="font-family: var(--font-body); font-size: 0.78rem; color: #64748b; margin-top: 3px; line-height: 1.45;">
                                                    ${empty grade.description ? 'Phạm vi sát hạch quốc gia theo quy định của Bộ Giao thông Vận tải.' : grade.description}
                                                </span>
                                            </div>
                                        </td>
                                        <td style="text-align: center;">
                                            <span style="font-size: 1.05rem; font-weight: 700; color: #0f172a;">${grade.minimumAge}</span>
                                            <span style="font-size: 0.72rem; color: #64748b; display: block;">tuổi</span>
                                        </td>
                                        <td style="text-align: center;">
                                            <span style="font-size: 1.05rem; font-weight: 700; color: #0f172a;">${grade.validForYears}</span>
                                            <span style="font-size: 0.72rem; color: #64748b; display: block;">năm</span>
                                        </td>
                                        <td style="text-align: center;">
                                            <c:choose>
                                                <c:when test="${not empty grade.upgradeFromLicenceId and not empty licenceClassById[grade.upgradeFromLicenceId]}">
                                                    <span class="role-badge role-badge--coi" style="font-weight: 700;">${licenceClassById[grade.upgradeFromLicenceId]}</span>
                                                </c:when>
                                                <c:otherwise>
                                                    <span style="color: #94a3b8; font-size: 0.85rem;">—</span>
                                                </c:otherwise>
                                            </c:choose>
                                        </td>
                                        <td>
                                            <div style="display: flex; gap: 6px; justify-content: center;">
                                                <button type="button"
                                                   class="btn-export"
                                                   style="padding: 4px 14px; font-size: 0.8rem; border-radius: 6px; border-color: rgba(245,158,11,0.25); color: #d97706; cursor: pointer;"
                                                   data-id="${grade.licenceId}"
                                                   data-class="${fn:escapeXml(grade.licenceClass)}"
                                                   data-desc="${fn:escapeXml(grade.description)}"
                                                   data-age="${grade.minimumAge}"
                                                   data-years="${grade.validForYears}"
                                                   data-upgrade="${grade.upgradeFromLicenceId}"
                                                   onclick="openLicenceModalEdit(this)">
                                                    Sửa
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
                                            <path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z" stroke="currentColor" stroke-width="2" stroke-linejoin="round"/>
                                            <path d="M14 2v6h6M16 13H8M16 17H8M10 9H8" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
                                        </svg>
                                        Chưa có hạng giấy phép lái xe nào trong hệ thống.
                                        <p style="font-size: 0.82rem; font-weight: 400; color: #94a3b8; margin-top: 0.5rem; max-width: 440px; margin-left: auto; margin-right: auto;">
                                            Nhấn nút <strong>Thêm hạng GPLX</strong> để bắt đầu khai báo danh mục sát hạch giấy phép lái xe cho hệ thống.
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
                        <c:when test="${not empty licenceClasses}">1 - ${fn:length(licenceClasses)}</c:when>
                        <c:otherwise>0</c:otherwise>
                    </c:choose>
                    trong tổng số ${empty totalClasses ? 0 : totalClasses} hạng GPLX
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

<%-- ===== In-page modal: Thêm / Sửa hạng GPLX ===== --%>
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

<div id="licenceModal" class="modal-overlay" onclick="if(event.target===this)closeLicenceModal()">
    <div class="modal-card" role="dialog" aria-modal="true" aria-labelledby="licenceModalTitle">
        <form action="${ctx}/admin/licence-class?action=save" method="POST">
            <div class="modal-head">
                <h3 id="licenceModalTitle">Thêm hạng GPLX</h3>
                <button type="button" class="modal-close" onclick="closeLicenceModal()" aria-label="Đóng">&times;</button>
            </div>
            <div class="modal-body">
                <input type="hidden" name="licenceId" id="m_licenceId" value="">

                <div class="filter-grid" style="grid-template-columns: 1fr 1fr; gap: 1.25rem; margin-bottom: 1.25rem;">
                    <div class="input-group">
                        <label for="m_licenceClass" class="input-label">Mã hạng <span style="color:#dc2626;">*</span></label>
                        <input type="text" id="m_licenceClass" name="licenceClass" class="input-field"
                               placeholder="VD: A1, B2, C..." required>
                    </div>
                    <div class="input-group">
                        <label for="m_upgradeFrom" class="input-label">Nâng hạng từ (không bắt buộc)</label>
                        <select id="m_upgradeFrom" name="upgradeFromLicenceId" class="input-field">
                            <option value="">-- Không nâng hạng --</option>
                            <c:forEach var="l" items="${licenceClasses}">
                                <option value="${l.licenceId}">Hạng ${l.licenceClass}</option>
                            </c:forEach>
                        </select>
                    </div>
                </div>

                <div class="filter-grid" style="grid-template-columns: 1fr 1fr; gap: 1.25rem; margin-bottom: 1.25rem;">
                    <div class="input-group">
                        <label for="m_minimumAge" class="input-label">Độ tuổi tối thiểu <span style="color:#dc2626;">*</span></label>
                        <input type="number" id="m_minimumAge" name="minimumAge" class="input-field" min="1"
                               placeholder="VD: 18" required>
                    </div>
                    <div class="input-group">
                        <label for="m_validForYears" class="input-label">Thời hạn (năm) <span style="color:#dc2626;">*</span></label>
                        <input type="number" id="m_validForYears" name="validForYears" class="input-field" min="1"
                               placeholder="VD: 10" required>
                    </div>
                </div>

                <div class="input-group">
                    <label for="m_description" class="input-label">Mô tả & Phạm vi điều khiển</label>
                    <textarea id="m_description" name="description" class="input-field" rows="3"
                              style="resize: vertical; min-height: 90px; padding: 0.65rem 0.9rem;"
                              placeholder="VD: Xe mô tô hai bánh có dung tích xi-lanh từ 50 cm³ đến dưới 175 cm³..."></textarea>
                </div>
            </div>
            <div class="modal-foot">
                <button type="button" class="btn-reset" onclick="closeLicenceModal()" style="height: 44px; padding: 0 1.5rem; display: inline-flex; align-items: center;">Hủy bỏ</button>
                <button type="submit" class="btn-filter" style="height: 44px; padding: 0 1.5rem;">Lưu hạng GPLX</button>
            </div>
        </form>
    </div>
</div>

<script>
    function openLicenceModal() {
        document.getElementById('licenceModalTitle').textContent = 'Thêm hạng GPLX';
        document.getElementById('m_licenceId').value = '';
        document.getElementById('m_licenceClass').value = '';
        document.getElementById('m_description').value = '';
        document.getElementById('m_minimumAge').value = '';
        document.getElementById('m_validForYears').value = '';
        setUpgradeOptionsState('');
        document.getElementById('m_upgradeFrom').value = '';
        document.getElementById('licenceModal').classList.add('is-open');
    }

    function openLicenceModalEdit(btn) {
        document.getElementById('licenceModalTitle').textContent = 'Chỉnh sửa hạng GPLX';
        document.getElementById('m_licenceId').value = btn.dataset.id;
        document.getElementById('m_licenceClass').value = btn.dataset.class;
        document.getElementById('m_description').value = btn.dataset.desc;
        document.getElementById('m_minimumAge').value = btn.dataset.age;
        document.getElementById('m_validForYears').value = btn.dataset.years;
        // ẩn chính nó khỏi danh sách "nâng hạng từ" và chọn giá trị hiện tại
        setUpgradeOptionsState(btn.dataset.id);
        var up = btn.dataset.upgrade;
        document.getElementById('m_upgradeFrom').value = (up && up !== '' ) ? up : '';
        document.getElementById('licenceModal').classList.add('is-open');
    }

    // ẩn option trùng với hạng đang sửa (không thể nâng hạng từ chính nó)
    function setUpgradeOptionsState(selfId) {
        var sel = document.getElementById('m_upgradeFrom');
        for (var i = 0; i < sel.options.length; i++) {
            var opt = sel.options[i];
            if (opt.value === '') continue;
            opt.hidden = (selfId !== '' && opt.value === String(selfId));
        }
    }

    function closeLicenceModal() {
        document.getElementById('licenceModal').classList.remove('is-open');
    }

    document.addEventListener('keydown', function (e) {
        if (e.key === 'Escape') closeLicenceModal();
    });
</script>

</body>
</html>

<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>

<c:set var="ctx" value="${pageContext.request.contextPath}" />

<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Quản lý Tài khoản - Lái Vui</title>
    <link rel="preconnect" href="https://fonts.googleapis.com">
    <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
    <link href="https://fonts.googleapis.com/css2?family=Be+Vietnam+Pro:wght@400;500;600;700;800&family=Inter:wght@400;500;600;700;800&display=swap" rel="stylesheet">
    <link rel="stylesheet" href="${ctx}/assets/css/style.css">
    <link rel="stylesheet" href="${ctx}/assets/css/layout.css">
</head>
<body class="has-side-nav-bar">

<jsp:include page="/views/layout/sidebar-admin.jsp">
    <jsp:param name="activeSidebar" value="tai-khoan" />
</jsp:include>

<div class="dashboard-shell">
    <main class="main-content">

        <nav class="breadcrumbs" aria-label="Breadcrumb">
            <a href="${ctx}/views/public/home.jsp">Trang chủ</a>
            <span class="breadcrumbs__separator" aria-hidden="true">/</span>
            <a href="${ctx}/admin/dashboard">Quản trị</a>
            <span class="breadcrumbs__separator" aria-hidden="true">/</span>
            <span class="breadcrumbs__current" aria-current="page">Tài khoản</span>
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
                <h1 class="page-title">Quản lý Tài khoản hệ thống</h1>
                <p class="page-subtitle">Cấp phát tài khoản mới, quản lý thông tin cá nhân, phân quyền truy cập và kiểm soát trạng thái hoạt động của người dùng.</p>
            </div>
            <div class="page-actions" style="display: flex; gap: 10px;">
                <button type="button" class="btn-filter" id="btn-add-account" onclick="openAccModal()" style="height: 42px; padding: 0 1.25rem; font-size: 0.9rem; border-radius: 8px; flex: none; cursor:pointer; display:inline-flex; align-items:center; gap:6px;">
                    <svg width="16" height="16" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                        <path d="M12 5v14M5 12h14" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round"/>
                    </svg>
                    Tạo tài khoản mới
                </button>
            </div>
        </header>

        <section class="metrics-row" aria-label="Thống kê tài khoản">
            <div class="stat-card">
                <div class="stat-icon stat-icon--blue">
                    <svg width="24" height="24" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                        <path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2M9 11a4 4 0 1 0 0-8 4 4 0 0 0 0 8zm14 10v-2a4 4 0 0 0-3-3.87M16 3.13a4 4 0 0 1 0 7.75" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                    </svg>
                </div>
                <div class="stat-info">
                    <span class="stat-number">${empty totalAccounts ? 0 : totalAccounts}</span>
                    <span class="stat-label">Tổng số tài khoản</span>
                    <span class="stat-trend stat-trend--up">Toàn hệ thống</span>
                </div>
            </div>
            <div class="stat-card">
                <div class="stat-icon stat-icon--blue" style="background-color: rgba(0, 82, 204, 0.08); color: #0052cc;">
                    <svg width="24" height="24" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                        <path d="M12 22s8-4 8-10V5l-8-3-8 3v7c0 6 8 10 8 10z" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                    </svg>
                </div>
                <div class="stat-info">
                    <span class="stat-number">${empty adminCount ? 0 : adminCount}</span>
                    <span class="stat-label">Admin hệ thống</span>
                    <span class="stat-trend stat-trend--up" style="color: #0052cc;">Quản trị tối cao</span>
                </div>
            </div>
            <div class="stat-card">
                <div class="stat-icon stat-icon--blue" style="background-color: rgba(13, 148, 136, 0.08); color: #0d9488;">
                    <svg width="24" height="24" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                        <path d="M2 22s2-4 10-4 10 4 10 4M12 11a4 4 0 1 0 0-8 4 4 0 0 0 0 8z" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                    </svg>
                </div>
                <div class="stat-info">
                    <span class="stat-number">${empty coiThiCount ? 0 : coiThiCount}</span>
                    <span class="stat-label">Cán bộ coi thi</span>
                    <span class="stat-trend stat-trend--up" style="color: #0d9488;">Giám sát phòng thi</span>
                </div>
            </div>
            <div class="stat-card">
                <div class="stat-icon stat-icon--blue" style="background-color: rgba(124, 58, 237, 0.08); color: #7c3aed;">
                    <svg width="24" height="24" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                        <path d="M12 20h9M3 20v-8a2 2 0 0 1 2-2h6a2 2 0 0 1 2 2v8M3 10V6a2 2 0 0 1 2-2h6a2 2 0 0 1 2 2v4" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                    </svg>
                </div>
                <div class="stat-info">
                    <span class="stat-number">${empty chamThiCount ? 0 : chamThiCount}</span>
                    <span class="stat-label">Giám khảo chấm thi</span>
                    <span class="stat-trend stat-trend--up" style="color: #7c3aed;">Đánh giá sát hạch</span>
                </div>
            </div>
        </section>

        <section class="filter-panel" aria-label="Bộ lọc tài khoản">
            <h2 class="filter-title">
                <svg width="18" height="18" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                    <path d="M22 3H2l8 9.46V19l4 2v-8.54L22 3z" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                </svg>
                Bộ lọc tìm kiếm
            </h2>
            <form action="${ctx}/admin/accounts" method="GET">
                <div class="filter-grid" style="grid-template-columns: 2fr 1.25fr 1.25fr 1.5fr;">
                    <div class="input-group">
                        <label for="searchKeyword" class="input-label">Tìm kiếm tài khoản</label>
                        <input type="text" id="searchKeyword" name="searchKeyword" class="input-field"
                               placeholder="Tên đăng nhập, họ tên, email, sđt..." value="${param.searchKeyword}">
                    </div>
                    <div class="input-group">
                        <label for="filterRole" class="input-label">Vai trò phân quyền</label>
                        <select id="filterRole" name="filterRole" class="input-field">
                            <option value="">Tất cả vai trò</option>
                            <option value="admin" ${param.filterRole eq 'admin' ? 'selected' : ''}>Quản trị viên (Admin)</option>
                            <option value="coi_thi" ${param.filterRole eq 'coi_thi' ? 'selected' : ''}>Cán bộ coi thi</option>
                            <option value="cham_thi" ${param.filterRole eq 'cham_thi' ? 'selected' : ''}>Giám khảo chấm thi</option>
                            <option value="managing" ${param.filterRole eq 'managing' ? 'selected' : ''}>Cán bộ quản lý</option>
                            <option value="candidate" ${param.filterRole eq 'candidate' ? 'selected' : ''}>Thí sinh</option>
                        </select>
                    </div>
                    <div class="input-group">
                        <label for="filterStatus" class="input-label">Trạng thái tài khoản</label>
                        <select id="filterStatus" name="filterStatus" class="input-field">
                            <option value="">Tất cả trạng thái</option>
                            <option value="active" ${param.filterStatus eq 'active' ? 'selected' : ''}>Đang hoạt động</option>
                            <option value="inactive" ${param.filterStatus eq 'inactive' ? 'selected' : ''}>Khóa / Vô hiệu</option>
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
                            <a href="${ctx}/admin/accounts" class="btn-reset">Đặt lại</a>
                        </div>
                    </div>
                </div>
            </form>
        </section>

        <section class="log-card" aria-label="Danh sách tài khoản">
            <header class="log-card-header">
                <h2 class="log-card-title">
                    <svg width="20" height="17" viewBox="0 0 20 17" fill="none" xmlns="http://www.w3.org/2000/svg" style="color: #0052cc;">
                        <circle cx="8" cy="5" r="3.5" stroke="currentColor" stroke-width="2"/>
                        <path d="M1 16C1 12.69 4.13 10 8 10C11.87 10 15 12.69 15 16" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
                    </svg>
                    Danh sách tài khoản hệ thống
                    <c:if test="${not empty accounts}">
                        <span style="font-size: 0.78rem; font-weight: 600; background: rgba(0,82,204,0.08); color: #0052cc; padding: 2px 10px; border-radius: 9999px; margin-left: 6px;">
                            ${fn:length(accounts)} tài khoản
                        </span>
                    </c:if>
                </h2>
            </header>

            <div class="table-responsive">
                <table class="audit-table">
                    <thead>
                        <tr>
                            <th scope="col" class="col-id">STT</th>
                            <th scope="col" style="min-width: 200px;">Tên tài khoản</th>
                            <th scope="col" style="min-width: 180px;">Email & SĐT</th>
                            <th scope="col" style="width: 150px; text-align: center;">Vai trò</th>
                            <th scope="col" style="width: 140px; text-align: center;">Ngày tạo</th>
                            <th scope="col" style="width: 130px; text-align: center;">Trạng thái</th>
                            <th scope="col" style="text-align: center; width: 220px;">Thao tác</th>
                        </tr>
                    </thead>
                    <tbody>
                        <c:choose>
                            <c:when test="${not empty accounts}">
                                <c:forEach var="acc" items="${accounts}" varStatus="status">
                                    <tr>
                                        <td class="col-id">${status.index + 1}</td>
                                        <td>
                                            <div class="user-cell">
                                                <c:choose>
                                                    <c:when test="${acc.roleCode eq 'admin'}"><div class="user-avatar" title="Quản trị viên">${fn:substring(acc.fullName, 0, 1)}</div></c:when>
                                                    <c:when test="${acc.roleCode eq 'coi_thi'}"><div class="user-avatar user-avatar--teal" title="Cán bộ coi thi">${fn:substring(acc.fullName, 0, 1)}</div></c:when>
                                                    <c:when test="${acc.roleCode eq 'cham_thi'}"><div class="user-avatar user-avatar--purple" title="Giám khảo chấm thi">${fn:substring(acc.fullName, 0, 1)}</div></c:when>
                                                    <c:otherwise><div class="user-avatar user-avatar--orange" title="Người dùng">${fn:substring(acc.fullName, 0, 1)}</div></c:otherwise>
                                                </c:choose>
                                                <div class="user-info">
                                                    <span class="user-name" style="font-weight: 600; color: #0f172a;">${acc.fullName}</span>
                                                    <span class="user-username">@${acc.username}</span>
                                                </div>
                                            </div>
                                        </td>
                                        <td>
                                            <div style="font-weight: 500; color: #334155; font-size: 0.88rem;">${acc.email}</div>
                                            <div style="font-size: 0.75rem; color: #64748b; margin-top: 2px;">${acc.phone}</div>
                                        </td>
                                        <td style="text-align: center;">
                                            <c:choose>
                                                <c:when test="${acc.roleCode eq 'admin'}"><span class="role-badge role-badge--admin">Admin</span></c:when>
                                                <c:when test="${acc.roleCode eq 'coi_thi'}"><span class="role-badge role-badge--coi">Cán bộ coi thi</span></c:when>
                                                <c:when test="${acc.roleCode eq 'cham_thi'}"><span class="role-badge role-badge--cham">Giám khảo</span></c:when>
                                                <c:when test="${acc.roleCode eq 'managing'}"><span class="role-badge role-badge--admin">Cán bộ quản lý</span></c:when>
                                                <c:otherwise><span class="role-badge role-badge--other">Thí sinh</span></c:otherwise>
                                            </c:choose>
                                        </td>
                                        <td style="text-align: center; font-size: 0.82rem; color: #64748b; font-weight: 500;">
                                            <fmt:formatDate value="${acc.createdAt}" pattern="dd/MM/yyyy"/>
                                        </td>
                                        <td style="text-align: center;">
                                            <c:choose>
                                                <c:when test="${acc.status eq 'active'}"><span class="action-badge action-badge--success">Hoạt động</span></c:when>
                                                <c:otherwise><span class="action-badge action-badge--danger">Khóa / Vô hiệu</span></c:otherwise>
                                            </c:choose>
                                        </td>
                                        <td>
                                            <div style="display: flex; gap: 6px; justify-content: center; flex-wrap: wrap;">
                                                <button type="button" class="btn-export"
                                                        style="padding: 4px 10px; font-size: 0.8rem; border-radius: 6px; border-color: rgba(245,158,11,0.25); color: #d97706; cursor:pointer;"
                                                        data-id="${acc.id}" data-username="${fn:escapeXml(acc.username)}" data-email="${fn:escapeXml(acc.email)}"
                                                        data-role="${acc.role}" data-fullname="${fn:escapeXml(acc.fullName)}" data-phone="${fn:escapeXml(acc.phone)}"
                                                        data-sex="${fn:escapeXml(acc.sex)}" data-govid="${fn:escapeXml(acc.govId)}" data-address="${fn:escapeXml(acc.address)}"
                                                        data-dob="<fmt:formatDate value='${acc.dateOfBirth}' pattern='yyyy-MM-dd'/>" data-status="${acc.status}"
                                                        onclick="openAccModalEdit(this)">
                                                    Sửa
                                                </button>
                                                <c:choose>
                                                    <c:when test="${acc.status eq 'active'}">
                                                        <button type="button" class="btn-export"
                                                                style="padding: 4px 10px; font-size: 0.8rem; border-radius: 6px; border-color: rgba(239,68,68,0.25); color: #dc2626; cursor:pointer;"
                                                                onclick="lockAccount('${acc.id}', '${fn:escapeXml(acc.fullName)}', true)">
                                                            Khóa
                                                        </button>
                                                    </c:when>
                                                    <c:otherwise>
                                                        <button type="button" class="btn-export"
                                                                style="padding: 4px 10px; font-size: 0.8rem; border-radius: 6px; border-color: rgba(16,185,129,0.25); color: #059669; cursor:pointer;"
                                                                onclick="lockAccount('${acc.id}', '${fn:escapeXml(acc.fullName)}', false)">
                                                            Mở khóa
                                                        </button>
                                                    </c:otherwise>
                                                </c:choose>
                                                <button type="button" class="btn-export"
                                                        style="padding: 4px 10px; font-size: 0.8rem; border-radius: 6px; border-color: rgba(100,116,139,0.25); color: #475569; cursor:pointer;"
                                                        onclick="deleteAccount('${acc.id}', '${fn:escapeXml(acc.fullName)}')">
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
                                        <svg width="64" height="64" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg" style="margin: 0 auto 1.5rem; display: block; opacity: 0.25; color: #64748b;">
                                            <circle cx="8" cy="5" r="3.5" stroke="currentColor" stroke-width="2"/>
                                            <path d="M1 16C1 12.69 4.13 10 8 10C11.87 10 15 12.69 15 16" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
                                        </svg>
                                        Chưa có tài khoản nào trong hệ thống.
                                        <p style="font-size: 0.82rem; font-weight: 400; color: #94a3b8; margin-top: 0.5rem; max-width: 440px; margin-left: auto; margin-right: auto;">
                                            Nhấn <strong>Tạo tài khoản mới</strong> để cấp phát thông tin đăng nhập và phân quyền cho người dùng.
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
                    Hiển thị <c:choose><c:when test="${not empty accounts}">1 - ${fn:length(accounts)}</c:when><c:otherwise>0</c:otherwise></c:choose>
                    trong tổng số ${empty totalAccounts ? 0 : totalAccounts} tài khoản
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

<form id="lockForm" action="${ctx}/admin/accounts" method="POST" style="display:none;">
    <input type="hidden" name="action" value="lock">
    <input type="hidden" name="id" id="lockId">
    <input type="hidden" name="lock" id="lockVal">
</form>
<form id="delAccForm" action="${ctx}/admin/accounts" method="POST" style="display:none;">
    <input type="hidden" name="action" value="delete">
    <input type="hidden" name="id" id="delAccId">
</form>

<style>
    .modal-overlay { display:none; position:fixed; inset:0; z-index:1000; background:rgba(15,23,42,0.45); align-items:flex-start; justify-content:center; padding:4vh 1rem; overflow-y:auto; }
    .modal-overlay.is-open { display:flex; }
    .modal-card { width:100%; max-width:680px; background:#fff; border-radius:16px; box-shadow:0 20px 60px rgba(15,23,42,0.25); font-family:'Be Vietnam Pro','Inter',sans-serif; }
    .modal-head { display:flex; align-items:center; justify-content:space-between; padding:1.25rem 1.5rem; border-bottom:1px solid #e2e8f0; }
    .modal-head h3 { margin:0; font-size:1.1rem; font-weight:800; color:#0f172a; }
    .modal-close { border:none; background:transparent; font-size:1.5rem; line-height:1; color:#94a3b8; cursor:pointer; padding:0 4px; }
    .modal-body { padding:1.5rem; }
    .modal-foot { display:flex; gap:12px; justify-content:flex-end; padding:1rem 1.5rem; border-top:1px solid #e2e8f0; }
</style>

<div id="accModal" class="modal-overlay" onclick="if(event.target===this)closeAccModal()">
    <div class="modal-card" role="dialog" aria-modal="true">
        <form action="${ctx}/admin/accounts?action=save" method="POST">
            <div class="modal-head">
                <h3 id="accModalTitle">Tạo tài khoản mới</h3>
                <button type="button" class="modal-close" onclick="closeAccModal()">&times;</button>
            </div>
            <div class="modal-body">
                <input type="hidden" name="userId" id="a_id" value="">
                <div class="filter-grid" style="grid-template-columns:1fr 1fr; gap:1.25rem; margin-bottom:1.25rem;">
                    <div class="input-group">
                        <label for="a_username" class="input-label">Tên đăng nhập <span style="color:#dc2626;">*</span></label>
                        <input type="text" id="a_username" name="username" class="input-field" required>
                    </div>
                    <div class="input-group">
                        <label for="a_role" class="input-label">Vai trò <span style="color:#dc2626;">*</span></label>
                        <select id="a_role" name="role" class="input-field" required>
                            <option value="">-- Chọn vai trò --</option>
                            <option value="Admin">Quản trị viên (Admin)</option>
                            <option value="ExamStaff">Cán bộ coi thi</option>
                            <option value="Examiner">Giám khảo chấm thi</option>
                            <option value="ManagingStaff">Cán bộ quản lý</option>
                            <option value="Registrant">Thí sinh</option>
                        </select>
                    </div>
                </div>
                <div class="filter-grid" style="grid-template-columns:1fr 1fr; gap:1.25rem; margin-bottom:1.25rem;">
                    <div class="input-group">
                        <label for="a_email" class="input-label">Email <span style="color:#dc2626;">*</span></label>
                        <input type="email" id="a_email" name="email" class="input-field" required>
                    </div>
                    <div class="input-group">
                        <label for="a_password" class="input-label">Mật khẩu <span id="a_pw_req" style="color:#dc2626;">*</span></label>
                        <input type="text" id="a_password" name="password" class="input-field" placeholder="Tối thiểu 6 ký tự">
                        <small id="a_pw_hint" style="display:none; color:#94a3b8; font-size:0.72rem;">Để trống nếu không đổi mật khẩu</small>
                    </div>
                </div>
                <div class="filter-grid" style="grid-template-columns:1fr 1fr; gap:1.25rem; margin-bottom:1.25rem;">
                    <div class="input-group">
                        <label for="a_fullName" class="input-label">Họ và tên <span style="color:#dc2626;">*</span></label>
                        <input type="text" id="a_fullName" name="fullName" class="input-field" required>
                    </div>
                    <div class="input-group">
                        <label for="a_phone" class="input-label">Số điện thoại <span style="color:#dc2626;">*</span></label>
                        <input type="text" id="a_phone" name="phone" class="input-field" required>
                    </div>
                </div>
                <div class="filter-grid" style="grid-template-columns:1fr 1fr 1fr; gap:1.25rem; margin-bottom:1.25rem;">
                    <div class="input-group">
                        <label for="a_dob" class="input-label">Ngày sinh <span style="color:#dc2626;">*</span></label>
                        <input type="date" id="a_dob" name="dateOfBirth" class="input-field" required>
                    </div>
                    <div class="input-group">
                        <label for="a_sex" class="input-label">Giới tính <span style="color:#dc2626;">*</span></label>
                        <select id="a_sex" name="sex" class="input-field" required>
                            <option value="">--</option>
                            <option value="Nam">Nam</option>
                            <option value="Nữ">Nữ</option>
                            <option value="Khác">Khác</option>
                        </select>
                    </div>
                    <div class="input-group">
                        <label for="a_status" class="input-label">Trạng thái</label>
                        <select id="a_status" name="status" class="input-field">
                            <option value="active">Hoạt động</option>
                            <option value="inactive">Khóa / Vô hiệu</option>
                        </select>
                    </div>
                </div>
                <div class="filter-grid" style="grid-template-columns:1fr 1.4fr; gap:1.25rem;">
                    <div class="input-group">
                        <label for="a_govId" class="input-label">Số CCCD/CMND <span style="color:#dc2626;">*</span></label>
                        <input type="text" id="a_govId" name="govId" class="input-field" required>
                    </div>
                    <div class="input-group">
                        <label for="a_address" class="input-label">Địa chỉ</label>
                        <input type="text" id="a_address" name="address" class="input-field">
                    </div>
                </div>
            </div>
            <div class="modal-foot">
                <button type="button" class="btn-reset" onclick="closeAccModal()" style="height:44px; padding:0 1.5rem; display:inline-flex; align-items:center;">Hủy bỏ</button>
                <button type="submit" class="btn-filter" style="height:44px; padding:0 1.5rem;">Lưu tài khoản</button>
            </div>
        </form>
    </div>
</div>

<script>
    function openAccModal() {
        document.getElementById('accModalTitle').textContent = 'Tạo tài khoản mới';
        ['a_id','a_username','a_email','a_password','a_role','a_fullName','a_phone','a_dob','a_sex','a_govId','a_address'].forEach(function(k){document.getElementById(k).value='';});
        document.getElementById('a_status').value = 'active';
        document.getElementById('a_username').readOnly = false;
        document.getElementById('a_pw_req').style.display = 'inline';
        document.getElementById('a_pw_hint').style.display = 'none';
        document.getElementById('accModal').classList.add('is-open');
    }
    function openAccModalEdit(b) {
        document.getElementById('accModalTitle').textContent = 'Chỉnh sửa tài khoản';
        document.getElementById('a_id').value = b.dataset.id;
        document.getElementById('a_username').value = b.dataset.username;
        document.getElementById('a_username').readOnly = true;
        document.getElementById('a_email').value = b.dataset.email;
        document.getElementById('a_password').value = '';
        document.getElementById('a_role').value = b.dataset.role;
        document.getElementById('a_fullName').value = b.dataset.fullname;
        document.getElementById('a_phone').value = b.dataset.phone;
        document.getElementById('a_dob').value = b.dataset.dob;
        document.getElementById('a_sex').value = b.dataset.sex;
        document.getElementById('a_govId').value = b.dataset.govid;
        document.getElementById('a_address').value = b.dataset.address || '';
        document.getElementById('a_status').value = b.dataset.status;
        document.getElementById('a_pw_req').style.display = 'none';
        document.getElementById('a_pw_hint').style.display = 'block';
        document.getElementById('accModal').classList.add('is-open');
    }
    function closeAccModal() { document.getElementById('accModal').classList.remove('is-open'); }

    function lockAccount(id, name, lock) {
        var msg = lock ? ('Khóa tài khoản "' + name + '"? Người dùng sẽ không đăng nhập được.')
                       : ('Mở khóa tài khoản "' + name + '"?');
        if (confirm(msg)) {
            document.getElementById('lockId').value = id;
            document.getElementById('lockVal').value = lock ? 'true' : 'false';
            document.getElementById('lockForm').submit();
        }
    }
    function deleteAccount(id, name) {
        if (confirm('Xóa vĩnh viễn tài khoản "' + name + '"?\nNếu tài khoản đã phát sinh dữ liệu, hệ thống sẽ không cho xóa — hãy dùng Khóa.')) {
            document.getElementById('delAccId').value = id;
            document.getElementById('delAccForm').submit();
        }
    }
    document.addEventListener('keydown', function(e){ if(e.key==='Escape') closeAccModal(); });
</script>

</body>
</html>

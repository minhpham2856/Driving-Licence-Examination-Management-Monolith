<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<c:set var="ctx" value="${pageContext.request.contextPath}" />
<c:if test="${empty requestScope.totalRegistrants}">
    <c:redirect url="/manager/registrants" />
</c:if>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Quản lý thí sinh - Lái Vui</title>
    <link rel="stylesheet" href="${ctx}/assets/css/style.css">
    <link rel="stylesheet" href="${ctx}/assets/css/layout.css">
</head>
<body class="has-side-nav-bar">
<jsp:include page="/views/layout/sidebar-managingstaff.jsp">
    <jsp:param name="activeSidebar" value="hoc-vien" />
</jsp:include>
<div class="dashboard-shell">
<main class="main-content">
    <nav class="breadcrumbs">
        <a href="${ctx}/manager/dashboard">Dashboard</a>
        <span class="breadcrumbs__separator">/</span>
        <span class="breadcrumbs__current">Quản lý thí sinh</span>
    </nav>
    <header class="page-header">
        <div class="page-title-wrap">
            <h1 class="page-title">Quản Lý Thí Sinh</h1>
            <p class="page-subtitle">Quản lý tài khoản, hồ sơ và trạng thái đăng ký của toàn bộ Registrant.</p>
        </div>
        <div class="page-actions" style="display:flex;gap:.75rem">
            <a class="btn-export"
               href="${ctx}/manager/registrants?export=csv&amp;keyword=${fn:escapeXml(param.keyword)}&amp;licence=${fn:escapeXml(param.licence)}&amp;dossierStatus=${fn:escapeXml(param.dossierStatus)}&amp;accountStatus=${fn:escapeXml(param.accountStatus)}"
               style="display:inline-flex;text-decoration:none">Xuất danh sách Excel</a>
            <a class="btn-filter" href="${ctx}/manager/create-user"
               style="display:inline-flex;text-decoration:none">Tạo tài khoản &amp; hồ sơ</a>
        </div>
    </header>

    <c:if test="${not empty sessionScope.registrantSuccess}">
        <div class="p-alert-banner" style="border-color:#10b981;color:#047857"><c:out value="${sessionScope.registrantSuccess}" /></div>
        <c:remove var="registrantSuccess" scope="session" />
    </c:if>
    <c:if test="${not empty sessionScope.registrantError}">
        <div class="p-alert-banner" style="border-color:#ef4444;color:#991b1b"><c:out value="${sessionScope.registrantError}" /></div>
        <c:remove var="registrantError" scope="session" />
    </c:if>

    <div class="report-grid" style="grid-template-columns:repeat(4,minmax(0,1fr));gap:1rem;margin:1.25rem 0">
        <div class="profile-score-card"><span class="score-card-part">TỔNG THÍ SINH</span><strong style="font-size:1.7rem">${totalRegistrants}</strong></div>
        <div class="profile-score-card"><span class="score-card-part">HỒ SƠ ĐÃ DUYỆT</span><strong style="font-size:1.7rem;color:#059669">${approvedCount}</strong></div>
        <div class="profile-score-card"><span class="score-card-part">CẦN XỬ LÝ</span><strong style="font-size:1.7rem;color:#d97706">${pendingCount}</strong></div>
        <div class="profile-score-card"><span class="score-card-part">TÀI KHOẢN ĐÃ KHÓA</span><strong style="font-size:1.7rem;color:#dc2626">${lockedCount}</strong></div>
    </div>

    <section class="filter-panel">
        <h2 class="filter-title">Tìm kiếm và lọc dữ liệu</h2>
        <form action="${ctx}/manager/registrants" method="get">
            <div class="filter-grid" style="grid-template-columns:2fr 1fr 1.25fr 1fr 1.5fr">
                <div class="input-group">
                    <label class="input-label" for="keyword">Tên, CCCD, email, SĐT hoặc username</label>
                    <input class="input-field" id="keyword" name="keyword" value="<c:out value='${param.keyword}' />" placeholder="Nhập từ khóa">
                </div>
                <div class="input-group">
                    <label class="input-label" for="licence">Hạng GPLX</label>
                    <select class="input-field" id="licence" name="licence">
                        <option value="">Tất cả</option>
                        <option value="A1" ${param.licence eq 'A1' ? 'selected' : ''}>Hạng A1</option>
                        <option value="A" ${param.licence eq 'A' ? 'selected' : ''}>Hạng A</option>
                        <option value="B1" ${param.licence eq 'B1' ? 'selected' : ''}>Hạng B1</option>
                        <option value="B" ${param.licence eq 'B' ? 'selected' : ''}>Hạng B</option>
                        <option value="C" ${param.licence eq 'C' ? 'selected' : ''}>Hạng C</option>
                        <option value="C1" ${param.licence eq 'C1' ? 'selected' : ''}>Hạng C1</option>
                    </select>
                </div>
                <div class="input-group">
                    <label class="input-label" for="dossierStatus">Trạng thái hồ sơ</label>
                    <select class="input-field" id="dossierStatus" name="dossierStatus">
                        <option value="">Tất cả</option>
                        <option value="Draft" ${param.dossierStatus eq 'Draft' ? 'selected' : ''}>Bản nháp</option>
                        <option value="Pending" ${param.dossierStatus eq 'Pending' ? 'selected' : ''}>Chờ duyệt</option>
                        <option value="Submitted" ${param.dossierStatus eq 'Submitted' ? 'selected' : ''}>Đã gửi duyệt</option>
                        <option value="NeedSupplement" ${param.dossierStatus eq 'NeedSupplement' ? 'selected' : ''}>Cần bổ sung</option>
                        <option value="Approved" ${param.dossierStatus eq 'Approved' ? 'selected' : ''}>Đã duyệt</option>
                        <option value="Rejected" ${param.dossierStatus eq 'Rejected' ? 'selected' : ''}>Đã từ chối</option>
                        <option value="Present" ${param.dossierStatus eq 'Present' ? 'selected' : ''}>Đang tham gia thi</option>
                    </select>
                </div>
                <div class="input-group">
                    <label class="input-label" for="accountStatus">Tài khoản</label>
                    <select class="input-field" id="accountStatus" name="accountStatus">
                        <option value="">Tất cả</option>
                        <option value="active" ${param.accountStatus eq 'active' ? 'selected' : ''}>Hoạt động</option>
                        <option value="locked" ${param.accountStatus eq 'locked' ? 'selected' : ''}>Đã khóa</option>
                    </select>
                </div>
                <div class="input-group filter-grid__btn-col">
                    <div class="btn-group">
                        <button class="btn-filter" type="submit">Áp dụng</button>
                        <a class="btn-reset" href="${ctx}/manager/registrants">Đặt lại</a>
                    </div>
                </div>
            </div>
        </form>
    </section>

    <section class="log-card">
        <header class="log-card-header">
            <h2 class="log-card-title">Danh sách thí sinh từ database</h2>
            <span class="action-badge action-badge--info">${fn:length(registrants)} kết quả</span>
        </header>
        <div class="table-responsive">
            <table class="audit-table">
                <thead><tr><th>Mã</th><th>Thí sinh</th><th>CCCD / Liên hệ</th><th>Hạng</th><th>Nguồn hồ sơ</th><th>Giấy tờ</th><th>Hồ sơ</th><th>Tài khoản</th><th style="text-align:center">Thao tác</th></tr></thead>
                <tbody>
                    <c:forEach var="item" items="${registrants}">
                        <tr>
                            <td>#${item.user.id}</td>
                            <td><strong><c:out value="${item.profile.fullName}" /></strong><br><small>@<c:out value="${item.user.username}" /> · <c:out value="${item.user.email}" /></small></td>
                            <td><c:out value="${item.profile.govIdNo}" /><br><small><c:out value="${item.profile.phoneNo}" /></small></td>
                            <td><c:out value="${empty item.licenceClass ? '—' : item.licenceClass}" /></td>
                            <td><c:out value="${item.sourceLabel}" /></td>
                            <td>${item.documentCount}/4</td>
                            <td><span class="action-badge action-badge--${item.statusKey}">${item.statusLabel}</span></td>
                            <td><span class="action-badge action-badge--${item.user.active ? 'success' : 'danger'}">${item.user.active ? 'Hoạt động' : 'Đã khóa'}</span></td>
                            <td>
                                <div style="display:flex;flex-wrap:wrap;gap:.4rem;justify-content:center">
                                    <a class="btn-export" href="${ctx}/manager/dossier-detail?id=${item.user.id}" style="padding:.35rem .6rem;text-decoration:none">Chi tiết</a>
                                    <c:if test="${item.reviewable}">
                                        <a class="btn-export" href="${ctx}/manager/dossiers?id=${item.registrationId}" style="padding:.35rem .6rem;text-decoration:none;color:#d97706">Duyệt</a>
                                    </c:if>
                                    <form action="${ctx}/manager/registrants" method="post" style="margin:0" onsubmit="return confirm('${item.user.active ? 'Khóa' : 'Mở khóa'} tài khoản này?');">
                                        <input type="hidden" name="id" value="${item.user.id}">
                                        <input type="hidden" name="action" value="${item.user.active ? 'lock' : 'activate'}">
                                        <button class="btn-export" type="submit" style="padding:.35rem .6rem;color:${item.user.active ? '#dc2626' : '#059669'}">${item.user.active ? 'Khóa' : 'Mở khóa'}</button>
                                    </form>
                                </div>
                            </td>
                        </tr>
                    </c:forEach>
                    <c:if test="${empty registrants}">
                        <tr><td colspan="9" style="padding:3rem;text-align:center;color:#64748b">Không tìm thấy thí sinh phù hợp.</td></tr>
                    </c:if>
                </tbody>
            </table>
        </div>
    </section>
</main>
<jsp:include page="/views/layout/footer.jsp"><jsp:param name="standalone" value="false" /></jsp:include>
</div>
</body>
</html>

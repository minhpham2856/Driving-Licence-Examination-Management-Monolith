<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib prefix = "fn" uri = "http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix = "fmt" uri = "http://java.sun.com/jsp/jstl/fmt" %>

<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Duyệt hồ sơ học viên - Lái Vui</title>
    <link rel="preconnect" href="https://fonts.googleapis.com">
    <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
    <link href="https://fonts.googleapis.com/css2?family=Be+Vietnam+Pro:wght@400;500;600;700;800&family=Inter:wght@400;500;600;700;800&display=swap" rel="stylesheet">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/style.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/layout.css">
</head>
<body class="has-side-nav-bar">

<jsp:include page="/views/layout/sidebar-managingstaff.jsp">
    <jsp:param name="activeSidebar" value="duyet-ho-so" />
</jsp:include>

<div class="dashboard-shell">
    <main class="main-content">
        
        <nav class="breadcrumbs">
            <a href="${pageContext.request.contextPath}/views/public/home.jsp">Trang chủ</a>
            <span class="breadcrumbs__separator">/</span>
            <a href="${pageContext.request.contextPath}/views/staff/managingstaff/dashboard.jsp">Dashboard quản lý</a>
            <span class="breadcrumbs__separator">/</span>
            <span class="breadcrumbs__current">Duyệt hồ sơ giấy tờ</span>
        </nav>
        
        <c:choose>
            <c:when test="${not empty param.id}">
                <c:choose>
                    <c:when test="${not empty user}">

                <header class="page-header">
                    <div class="page-title-wrap">
                        <h1 class="page-title">Thẩm Định Hồ Sơ: ${user.fullName}</h1>
                        <p class="page-subtitle">Kiểm tra thông tin đối chiếu với giấy tờ đã tải lên để phê duyệt hoặc từ chối.</p>
                    </div>
                    
                    <div class="page-actions">
                        <a href="approve.jsp" class="btn-export" style="height: 42px; padding: 0 1.25rem; font-size: 0.9rem; border-radius: 8px; text-decoration: none; display: inline-flex; align-items: center; gap: 6px; background-color: #ffffff; color: #475569;">
                            <svg width="16" height="16" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                                <path d="M19 12H5M12 19l-7-7 7-7" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                            </svg>
                            Quay lại danh sách chờ duyệt
                        </a>
                    </div>
                </header>

                <div class="profile-grid">
                    
                    <div style="display: flex; flex-direction: column; gap: 1.5rem;">
                        
                        <div class="report-pane" style="padding: 1.5rem;">
                            <div class="grading-pane__header" style="border-bottom: none; margin-bottom: 1.25rem; padding-bottom: 0;">
                                <h2 class="grading-pane__title" style="font-size: 1.05rem; display: flex; align-items: center; gap: 6px;">
                                    <svg width="18" height="18" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg" style="color: #0052cc;">
                                        <circle cx="12" cy="12" r="10" stroke="currentColor" stroke-width="2"/>
                                        <path d="M12 8v4M12 16h.01" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
                                    </svg>
                                    Đối chiếu thông tin cá nhân
                                </h2>
                            </div>

                            <div style="display: grid; grid-template-columns: 1fr 1fr; gap: 1rem 1.5rem;">
                                <div class="quick-info-item" style="flex-direction: column; align-items: flex-start; gap: 4px;">
                                    <span class="quick-info-label">Họ và tên học viên:</span>
                                    <span class="quick-info-value" style="font-size: 1rem; color: #0f172a;">${user.fullName}</span>
                                </div>
                                <div class="quick-info-item" style="flex-direction: column; align-items: flex-start; gap: 4px;">
                                    <span class="quick-info-label">Mã số hồ sơ / SBD:</span>
                                    <span class="quick-info-value" style="font-size: 1rem; color: #0052cc;">${user.code}</span>
                                </div>
                                <div class="quick-info-item" style="flex-direction: column; align-items: flex-start; gap: 4px;">
                                    <span class="quick-info-label">Số Căn cước công dân:</span>
                                    <span class="quick-info-value" style="font-size: 1rem; color: #0f172a; font-family: monospace;">${user.cccd}</span>
                                </div>
                                <div class="quick-info-item" style="flex-direction: column; align-items: flex-start; gap: 4px;">
                                    <span class="quick-info-label">Ngày sinh:</span>
                                    <span class="quick-info-value" style="font-size: 1rem; color: #0f172a;">${user.dob}</span>
                                </div>
                                <div class="quick-info-item" style="flex-direction: column; align-items: flex-start; gap: 4px;">
                                    <span class="quick-info-label">Giới tính / Số điện thoại:</span>
                                    <span class="quick-info-value" style="font-size: 1rem; color: #0f172a;">${user.gender} | ${user.phone}</span>
                                </div>
                                <div class="quick-info-item" style="flex-direction: column; align-items: flex-start; gap: 4px;">
                                    <span class="quick-info-label">Hạng GPLX đăng ký / Đợt nộp:</span>
                                    <span class="quick-info-value" style="font-size: 1rem; color: #d97706; font-weight: 800;">Hạng ${user.licenseClass} (${user.typeName})</span>
                                </div>
                            </div>
                        </div>

                        <div class="report-pane" style="padding: 1.5rem;">
                            <div class="grading-pane__header" style="border-bottom: none; margin-bottom: 1.25rem; padding-bottom: 0;">
                                <h2 class="grading-pane__title" style="font-size: 1.05rem;">Giấy tờ đính kèm đối chiếu</h2>
                            </div>

                            <div style="display: flex; flex-direction: column; gap: 1.25rem;">
                                <div>
                                    <span class="quick-info-label" style="display: block; margin-bottom: 6px;">1. Ảnh thẻ chân dung 3x4:</span>
                                    <div class="face-photo-placeholder" style="width: 100px; height: 133px; border-style: solid; border-color: #cbd5e1; background-color: #f8fafc; color: #64748b; display: flex; align-items: center; justify-content: center; border-radius: 6px;">
                                        <img src="${pageContext.request.contextPath}/assets/imgs/avatar-placeholder.svg" alt="Ảnh chân dung" style="width: 40px; height: 40px; opacity: 0.4;">
                                    </div>
                                </div>

                                <div>
                                    <span class="quick-info-label" style="display: block; margin-bottom: 6px;">2. Căn cước công dân (Mặt trước):</span>
                                    <div class="face-photo-placeholder" style="width: 100%; aspect-ratio: 1.6; border-style: solid; border-color: #cbd5e1; background-color: #f8fafc; border-radius: 8px; display: flex; flex-direction: column; align-items: center; justify-content: center; color: #0052cc; font-weight: 700; gap: 8px;">
                                        <svg width="40" height="40" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                                            <rect x="2" y="5" width="20" height="14" rx="2" stroke="currentColor" stroke-width="2"/>
                                            <circle cx="8" cy="12" r="2.5" stroke="currentColor" stroke-width="2"/>
                                            <path d="M14 9h4M14 12h4M14 15h2" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
                                        </svg>
                                        [ẢNH CCCD MẶT TRƯỚC HỌC VIÊN]
                                        <span style="font-size: 0.72rem; color: #64748b; font-weight: 400;">Bấm để xem ảnh phóng to</span>
                                    </div>
                                </div>
                            </div>
                        </div>

                    </div>

                    <aside class="profile-sidebar" style="position: sticky; top: 1.5rem;">
                        <div class="profile-sidebar-card" style="align-items: flex-start;">
                            <div class="sidebar-card-header" style="padding-bottom: 0.5rem; margin-bottom: 1.25rem;">
                                <h3 class="sidebar-card-title" style="color: #d97706; font-size: 1rem;">
                                    Phê duyệt hồ sơ này
                                </h3>
                            </div>
                            
                            <form action="${pageContext.request.contextPath}/manager/approve" method="POST" style="width: 100%; display: flex; flex-direction: column; gap: 1.25rem;">
                                <input type="hidden" name="id" value="${user.id}">
                                
                                <div class="input-group">
                                    <label class="input-label" style="margin-bottom: 8px;">Quyết định duyệt:</label>
                                    <div style="display: flex; flex-direction: column; gap: 10px;">
                                        <label style="display: inline-flex; align-items: center; gap: 8px; font-size: 0.9rem; font-weight: 700; color: #10b981; cursor: pointer;">
                                            <input type="radio" name="decision" value="approve" checked style="width: 16px; height: 16px; accent-color: #10b981;">
                                            Đồng ý duyệt (Hợp lệ)
                                        </label>
                                        <label style="display: inline-flex; align-items: center; gap: 8px; font-size: 0.9rem; font-weight: 700; color: #ef4444; cursor: pointer;">
                                            <input type="radio" name="decision" value="reject" style="width: 16px; height: 16px; accent-color: #ef4444;">
                                            Từ chối duyệt (Không hợp lệ)
                                        </label>
                                    </div>
                                </div>

                                <div class="input-group">
                                    <label for="rejectionReason" class="input-label">Lý do từ chối (nếu từ chối):</label>
                                    <textarea id="rejectionReason" name="rejectionReason" class="input-field" rows="4" placeholder="Nhập lý do chi tiết (ví dụ: ảnh CCCD bị mờ nét, giấy khám sức khỏe quá hạn 6 tháng...)" style="height: auto; resize: vertical; padding: 10px; font-size: 0.85rem;"></textarea>
                                </div>

                                <hr style="border: 0; border-top: 1px solid #f1f5f9; width: 100%; margin: 4px 0;">

                                <div style="display: flex; flex-direction: column; gap: 8px; width: 100%;">
                                    <button type="submit" class="btn-filter" style="width: 100%; height: 42px; border-radius: 8px; background-color: #0052cc; border-color: #0052cc; justify-content: center; font-weight: 700;">Xác nhận quyết định</button>
                                    <a href="approve.jsp" class="btn-reset" style="text-decoration: none; display: inline-flex; align-items: center; justify-content: center; border: 1px solid #cbd5e1; border-radius: 8px; height: 42px; font-size: 0.9rem; font-weight: 600; color: #475569; background-color: #ffffff;">Hủy bỏ</a>
                                </div>
                            </form>
                        </div>
                    </aside>
                </div>
                    </c:when>
                    <c:otherwise>
                        <div class="report-pane" style="padding: 5rem 1.5rem; text-align: center; color: #64748b; font-weight: 500; margin-top: 1.5rem; width: 100%; box-sizing: border-box;">
                            <svg width="64" height="64" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg" style="margin: 0 auto 1.5rem; display: block; opacity: 0.25; color: #64748b;">
                                <circle cx="12" cy="12" r="10" stroke="currentColor" stroke-width="2"/>
                                <path d="M12 8v4M12 16h.01" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
                            </svg>
                            Không tìm thấy hồ sơ học viên chi tiết để thẩm định.
                            <p style="font-size: 0.85rem; font-weight: 400; color: #94a3b8; margin-top: 0.5rem; max-width: 400px; margin-left: auto; margin-right: auto;">
                                Vui lòng quay trở lại danh sách hồ sơ chờ duyệt và chọn học viên khác.
                            </p>
                            <a href="approve.jsp" class="btn-export" style="text-decoration: none; display: inline-flex; align-items: center; justify-content: center; border: 1px solid #cbd5e1; border-radius: 8px; height: 42px; padding: 0 1.5rem; font-size: 0.9rem; font-weight: 600; color: #475569; background-color: #ffffff; margin-top: 1.5rem;">Quay lại danh sách</a>
                        </div>
                    </c:otherwise>
                </c:choose>
            </c:when>
            
            <c:otherwise>
                <header class="page-header">
                    <div class="page-title-wrap">
                        <h1 class="page-title">Hồ Sơ Chờ Phê Duyệt</h1>
                        <p class="page-subtitle">Danh sách hồ sơ học viên mới gửi lên và thí sinh tự do chờ ban quản lý kiểm duyệt.</p>
                    </div>
                </header>

                <section class="log-card">
                    <header class="log-card-header">
                        <h2 class="log-card-title">
                            <svg width="20" height="20" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg" style="color: #ea580c;">
                                <path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z" stroke="currentColor" stroke-width="2" stroke-linejoin="round"/>
                                <path d="M14 2v6h6" stroke="currentColor" stroke-width="2"/>
                            </svg>
                            Danh sách hồ sơ chờ thẩm định
                        </h2>
                        
                        <span class="action-badge action-badge--warning" style="font-weight: 700;">
                            ${empty pendingApprovalsCount ? 0 : pendingApprovalsCount} hồ sơ chờ duyệt
                        </span>
                    </header>

                    <div class="table-responsive">
                        <table class="audit-table">
                            <thead>
                                <tr>
                                    <th scope="col" style="width: 90px; text-align: center;">Mã học viên</th>
                                    <th scope="col">Họ và tên</th>
                                    <th scope="col" style="width: 140px;">Số CCCD</th>
                                    <th scope="col" style="width: 120px; text-align: center;">Hạng GPLX</th>
                                    <th scope="col" style="width: 150px; text-align: center;">Loại hồ sơ</th>
                                    <th scope="col" style="width: 160px;">Ngày gửi hồ sơ</th>
                                    <th scope="col" style="width: 120px; text-align: center;">Trạng thái</th>
                                    <th scope="col" style="width: 140px; text-align: center;">Thao tác</th>
                                </tr>
                            </thead>
                            <tbody>
                                <c:choose>
                                    <c:when test="${not empty pendingUsersList}">
                                        <c:forEach var="user" items="${pendingUsersList}">
                                            <tr>
                                                <td style="text-align: center; font-weight: 700; color: #64748b;">${user.code}</td>
                                                <td style="font-weight: 700; color: #0f172a;">${user.fullName}</td>
                                                <td style="font-family: monospace; font-size: 0.9rem;">${user.cccd}</td>
                                                <td style="text-align: center;">
                                                    <span class="role-badge role-badge--coi" style="padding: 2px 8px; font-size: 0.75rem; font-weight: 700;">Hạng ${user.licenseClass}</span>
                                                </td>
                                                <td style="text-align: center; font-weight: 600; color: #475569;">
                                                    ${user.type eq 'student' ? 'Học viên chính khóa' : 'Thí sinh tự do'}
                                                </td>
                                                <td style="color: #64748b; font-size: 0.85rem;">${user.registerDate}</td>
                                                <td style="text-align: center;">
                                                    <span class="action-badge action-badge--warning">Chờ duyệt</span>
                                                </td>
                                                <td style="text-align: center;">
                                                    <a href="approve.jsp?id=${user.id}" class="btn-filter" style="padding: 4px 12px; font-size: 0.8rem; border-radius: 6px; text-decoration: none; display: inline-flex; align-items: center; background-color: #d97706; border-color: #d97706;">Xem & Duyệt</a>
                                                </td>
                                            </tr>
                                        </c:forEach>
                                    </c:when>
                                    <c:otherwise>
                                        <tr>
                                            <td colspan="8" style="text-align: center; padding: 4rem 1.5rem; color: #64748b; font-weight: 500;">
                                                Không có hồ sơ nào đang chờ duyệt.
                                            </td>
                                        </tr>
                                    </c:otherwise>
                                </c:choose>
                            </tbody>
                        </table>
                    </div>
                </section>
            </c:otherwise>
        </c:choose>

    </main>

    <jsp:include page="/views/layout/footer.jsp">
        <jsp:param name="standalone" value="false" />
    </jsp:include>
</div>

</body>
</html>

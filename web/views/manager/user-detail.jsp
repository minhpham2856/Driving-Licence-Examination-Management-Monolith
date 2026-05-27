<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib prefix = "fn" uri = "http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix = "fmt" uri = "http://java.sun.com/jsp/jstl/fmt" %>

<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Chi tiết hồ sơ học viên - Lái Vui</title>
    <link rel="preconnect" href="https://fonts.googleapis.com">
    <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
    <link href="https://fonts.googleapis.com/css2?family=Be+Vietnam+Pro:wght@400;500;600;700;800&family=Inter:wght@400;500;600;700;800&display=swap" rel="stylesheet">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/style.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/layout.css">
</head>
<body class="has-side-nav-bar">

<jsp:include page="/views/layout/sidebar-manager.jsp">
    <jsp:param name="activeSidebar" value="hoc-vien" />
</jsp:include>

<div class="dashboard-shell">
    <main class="main-content">
        
        <!-- Breadcrumbs -->
        <nav class="breadcrumbs" aria-label="Breadcrumb">
            <a href="${pageContext.request.contextPath}/views/public/home.jsp">Trang chủ</a>
            <span class="breadcrumbs__separator" aria-hidden="true">/</span>
            <a href="${pageContext.request.contextPath}/views/manager/dashboard.jsp">Dashboard quản lý</a>
            <span class="breadcrumbs__separator" aria-hidden="true">/</span>
            <a href="${pageContext.request.contextPath}/views/manager/users.jsp">Danh sách học viên</a>
            <span class="breadcrumbs__separator" aria-hidden="true">/</span>
            <span class="breadcrumbs__current">
                <c:choose>
                    <c:when test="${not empty userDetail}">${userDetail.fullName}</c:when>
                    <c:otherwise>Chi tiết hồ sơ</c:otherwise>
                </c:choose>
            </span>
        </nav>
        
        <c:set var="user" value="${userDetail}" />
        <c:choose>
            <c:when test="${not empty user}">

        <!-- Header -->
        <header class="page-header">
            <div class="page-title-wrap">
                <h1 class="page-title">${user.fullName}</h1>
                <p class="page-subtitle">Học viên mã ${user.code} đăng ký thi sát hạch hạng bằng ${user.licenseClass}.</p>
            </div>
            
            <div class="page-actions" style="display: flex; gap: 10px;">
                <a href="${pageContext.request.contextPath}/views/manager/users.jsp" class="btn-export" style="height: 42px; padding: 0 1.25rem; font-size: 0.9rem; border-radius: 8px; text-decoration: none; display: inline-flex; align-items: center; gap: 6px; background-color: #ffffff; color: #475569;">
                    <svg width="16" height="16" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                        <path d="M19 12H5M12 19l-7-7 7-7" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                    </svg>
                    Quay lại danh sách
                </a>
                
                <c:if test="${user.statusKey eq 'warning'}">
                    <a href="${pageContext.request.contextPath}/views/manager/approve.jsp?id=${user.id}" class="btn-filter" style="height: 42px; padding: 0 1.25rem; font-size: 0.9rem; border-radius: 8px; text-decoration: none; display: inline-flex; align-items: center; gap: 6px; background-color: #d97706; border-color: #d97706;">
                        <svg width="16" height="16" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                            <path d="M9 11L12 14L22 4" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                            <path d="M21 12v7a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h11" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                        </svg>
                        Duyệt hồ sơ học viên
                    </a>
                </c:if>
            </div>
        </header>

        <div class="profile-grid">
            <!-- Sidebar: User profile stats card -->
            <aside class="profile-sidebar">
                <div class="profile-sidebar-card">
                    <div class="profile-avatar-wrap">
                        <div class="profile-avatar-large profile-avatar--blue" style="background: linear-gradient(135deg, #0052cc 0%, #6366f1 100%);">
                            ${fn:substring(user.fullName, 0, 1)}
                        </div>
                        <span class="profile-class-badge" style="background-color: #0052cc;">Hạng ${user.licenseClass}</span>
                    </div>
                    
                    <h2 class="profile-name">${user.fullName}</h2>
                    <p class="profile-sbd" style="color: #64748b; font-weight: 500;">Mã số: ${user.code}</p>
                    
                    <div class="profile-status-wrapper" style="margin-bottom: 1.5rem;">
                        <span class="action-badge action-badge--${user.statusKey}">${user.status}</span>
                    </div>

                    <hr style="border: 0; border-top: 1px solid #f1f5f9; width: 100%; margin: 0 0 1.5rem 0;">
                    
                    <div class="profile-quick-info">
                        <div class="quick-info-item">
                            <span class="quick-info-label">Số CCCD:</span>
                            <span class="quick-info-value">${user.cccd}</span>
                        </div>
                        <div class="quick-info-item">
                            <span class="quick-info-label">Ngày sinh:</span>
                            <span class="quick-info-value">${user.dob}</span>
                        </div>
                        <div class="quick-info-item">
                            <span class="quick-info-label">Giới tính:</span>
                            <span class="quick-info-value">${user.gender}</span>
                        </div>
                        <div class="quick-info-item">
                            <span class="quick-info-label">Điện thoại:</span>
                            <span class="quick-info-value">${user.phone}</span>
                        </div>
                        <div class="quick-info-item">
                            <span class="quick-info-label">Địa chỉ email:</span>
                            <span class="quick-info-value" style="font-size: 0.78rem;">${user.email}</span>
                        </div>
                        <div class="quick-info-item">
                            <span class="quick-info-label">Quê quán:</span>
                            <span class="quick-info-value" style="text-align: right;">${user.address}</span>
                        </div>
                        <div class="quick-info-item" style="border-top: 1px dashed #e2e8f0; padding-top: 8px; margin-top: 4px;">
                            <span class="quick-info-label">Loại hồ sơ:</span>
                            <span class="quick-info-value" style="color: #0052cc;">${user.typeName}</span>
                        </div>
                    </div>
                </div>
            </aside>

            <!-- Main Content: Uploaded Documents and Exam Registration History -->
            <section class="profile-main-content" style="gap: 1.5rem;">
                
                <!-- Uploaded Legal Documents Card -->
                <div class="log-card">
                    <div class="log-card-header" style="border-bottom: none; padding-bottom: 0.5rem;">
                        <h2 class="log-card-title" style="display: flex; align-items: center; gap: 8px;">
                            <svg width="20" height="20" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg" style="color: #0052cc;">
                                <path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z" stroke="currentColor" stroke-width="2" stroke-linejoin="round"/>
                                <path d="M14 2v6h6M16 13H8M16 17H8M10 9H8" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                            </svg>
                            Hồ sơ giấy tờ pháp lý đã nộp
                        </h2>
                        
                        <span class="action-badge action-badge--info" style="font-weight: 700;">3 GIẤY TỜ ĐÃ TẢI LÊN</span>
                    </div>

                    <div style="padding: 0 1.5rem 1.5rem 1.5rem;">
                        <p style="font-size: 0.85rem; color: #64748b; margin-bottom: 1.25rem;">Học viên tự chịu trách nhiệm về tính pháp lý và chính xác của giấy tờ đã nộp.</p>
                        
                        <div class="report-grid" style="grid-template-columns: repeat(3, 1fr); gap: 1rem; width: 100%;">
                            
                            <!-- Card 1: CCCD Photo -->
                            <div class="profile-score-card" style="padding: 1rem; min-height: 180px; align-items: center; justify-content: space-between;">
                                <div style="text-align: center; width: 100%;">
                                    <span class="score-card-part" style="display: block; margin-bottom: 0.5rem;">GIẤY TỜ 1</span>
                                    <h4 class="score-card-title" style="font-size: 0.85rem; margin-bottom: 0.75rem;">Ảnh Căn cước công dân</h4>
                                </div>
                                <div class="face-photo-placeholder" style="width: 100%; aspect-ratio: 16/10; border-style: solid; border-color: #cbd5e1; background-color: #f8fafc; color: #0052cc; display: flex; align-items: center; justify-content: center; font-size: 0.8rem; font-weight: 700; flex-direction: column; gap: 4px;">
                                    <svg width="24" height="24" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                                        <rect x="2" y="5" width="20" height="14" rx="2" stroke="currentColor" stroke-width="2"/>
                                        <circle cx="8" cy="12" r="2.5" stroke="currentColor" stroke-width="2"/>
                                        <path d="M14 9h4M14 12h4M14 15h2" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
                                    </svg>
                                    CCCD_MAT_TRUOC.png
                                </div>
                                <span class="action-badge action-badge--success" style="font-size: 0.7rem; padding: 2px 8px; margin-top: 0.75rem;">Hợp lệ</span>
                            </div>

                            <!-- Card 2: Health Certificate -->
                            <div class="profile-score-card" style="padding: 1rem; min-height: 180px; align-items: center; justify-content: space-between;">
                                <div style="text-align: center; width: 100%;">
                                    <span class="score-card-part" style="display: block; margin-bottom: 0.5rem;">GIẤY TỜ 2</span>
                                    <h4 class="score-card-title" style="font-size: 0.85rem; margin-bottom: 0.75rem;">Giấy khám sức khỏe</h4>
                                </div>
                                <div class="face-photo-placeholder" style="width: 100%; aspect-ratio: 16/10; border-style: solid; border-color: #cbd5e1; background-color: #f8fafc; color: #10b981; display: flex; align-items: center; justify-content: center; font-size: 0.8rem; font-weight: 700; flex-direction: column; gap: 4px;">
                                    <svg width="24" height="24" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                                        <path d="M22 12h-4l-3 9L9 3l-3 9H2" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                                    </svg>
                                    GKSK_PHUONG.pdf
                                </div>
                                <span class="action-badge action-badge--success" style="font-size: 0.7rem; padding: 2px 8px; margin-top: 0.75rem;">Hợp lệ</span>
                            </div>

                            <!-- Card 3: 3x4 Portrait Photo -->
                            <div class="profile-score-card" style="padding: 1rem; min-height: 180px; align-items: center; justify-content: space-between;">
                                <div style="text-align: center; width: 100%;">
                                    <span class="score-card-part" style="display: block; margin-bottom: 0.5rem;">GIẤY TỜ 3</span>
                                    <h4 class="score-card-title" style="font-size: 0.85rem; margin-bottom: 0.75rem;">Ảnh chân dung 3x4</h4>
                                </div>
                                <div class="face-photo-placeholder" style="width: 80px; height: 106px; border-style: solid; border-color: #cbd5e1; background-color: #f8fafc; color: #64748b; display: flex; align-items: center; justify-content: center;">
                                    <img src="${pageContext.request.contextPath}/assets/imgs/avatar-placeholder.svg" alt="Ảnh chân dung" style="width: 32px; height: 32px; opacity: 0.4;">
                                </div>
                                <span class="action-badge action-badge--success" style="font-size: 0.7rem; padding: 2px 8px; margin-top: 0.75rem;">Hợp lệ</span>
                            </div>

                        </div>
                    </div>
                </div>

                <!-- Exam Registration History Card -->
                <div class="log-card">
                    <div class="log-card-header" style="border-bottom: none; padding-bottom: 0.5rem;">
                        <h2 class="log-card-title" style="display: flex; align-items: center; gap: 8px;">
                            <svg width="20" height="20" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg" style="color: #7c3aed;">
                                <rect x="3" y="4" width="18" height="18" rx="2" stroke="currentColor" stroke-width="2" stroke-linejoin="round"/>
                                <path d="M16 2v4M8 2v4M3 10h18" stroke="currentColor" stroke-width="2"/>
                            </svg>
                            Lịch sử đăng ký và Kết quả sát hạch
                        </h2>
                    </div>

                    <div style="padding: 0 1.5rem 1.5rem 1.5rem;">
                        <div class="table-responsive">
                            <table class="audit-table" style="font-size: 0.85rem;">
                                <thead>
                                    <tr>
                                        <th scope="col" style="width: 140px;">Ngày đăng ký</th>
                                        <th scope="col" style="width: 150px;">Kỳ thi / Khóa thi</th>
                                        <th scope="col" style="width: 100px; text-align: center;">Hạng GPLX</th>
                                        <th scope="col" style="text-align: center;">Điểm thi (LT | SH | ĐT)</th>
                                        <th scope="col" style="width: 120px; text-align: center;">Kết quả chung</th>
                                    </tr>
                                </thead>
                                <tbody>
                                    <c:choose>
                                        <c:when test="${not empty examHistory}">
                                            <c:forEach var="hist" items="${examHistory}">
                                                <tr>
                                                    <td style="color: #64748b; font-weight: 500;">${hist.registerDate}</td>
                                                    <td style="font-weight: 700; color: #0052cc;">${hist.sessionName}</td>
                                                    <td style="text-align: center;">
                                                        <span class="role-badge role-badge--coi">Hạng ${hist.licenseClass}</span>
                                                    </td>
                                                    <td style="text-align: center; font-weight: 600;">
                                                        ${hist.theoryScore} | ${hist.practiceScore} | ${hist.roadScore}
                                                    </td>
                                                    <td style="text-align: center;">
                                                        <span class="action-badge action-badge--${hist.statusKey}">${hist.status}</span>
                                                    </td>
                                                </tr>
                                            </c:forEach>
                                        </c:when>
                                        <c:otherwise>
                                            <tr>
                                                <td colspan="5" style="text-align: center; padding: 2rem; color: #64748b;">
                                                    Chưa ghi nhận lịch sử thi sát hạch.
                                                </td>
                                            </tr>
                                        </c:otherwise>
                                    </c:choose>
                                </tbody>
                            </table>
                        </div>
                    </div>
                </div>

            </section>
        </div>
            </c:when>
            <c:otherwise>
                <div class="report-pane" style="padding: 5rem 1.5rem; text-align: center; color: #64748b; font-weight: 500; margin-top: 1.5rem;">
                    <svg width="64" height="64" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg" style="margin: 0 auto 1.5rem; display: block; opacity: 0.25; color: #64748b;">
                        <circle cx="12" cy="12" r="10" stroke="currentColor" stroke-width="2"/>
                        <path d="M12 8v4M12 16h.01" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
                    </svg>
                    Không tìm thấy hồ sơ học viên chi tiết.
                    <p style="font-size: 0.85rem; font-weight: 400; color: #94a3b8; margin-top: 0.5rem; max-width: 400px; margin-left: auto; margin-right: auto;">
                        Vui lòng quay trở lại danh sách học viên và thử chọn lại.
                    </p>
                    <a href="${pageContext.request.contextPath}/views/manager/users.jsp" class="btn-export" style="text-decoration: none; display: inline-flex; align-items: center; justify-content: center; border: 1px solid #cbd5e1; border-radius: 8px; height: 42px; padding: 0 1.5rem; font-size: 0.9rem; font-weight: 600; color: #475569; background-color: #ffffff; margin-top: 1.5rem;">Quay lại danh sách</a>
                </div>
            </c:otherwise>
        </c:choose>

    </main>

    <jsp:include page="/views/layout/footer.jsp">
        <jsp:param name="standalone" value="false" />
    </jsp:include>
</div>

</body>
</html>

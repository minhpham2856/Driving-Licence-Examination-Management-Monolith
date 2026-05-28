<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib prefix = "fn" uri = "http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix = "fmt" uri = "http://java.sun.com/jsp/jstl/fmt" %>

<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Chi tiết hồ sơ thí sinh - Lái Vui</title>
    <link rel="preconnect" href="https://fonts.googleapis.com">
    <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
    <link href="https://fonts.googleapis.com/css2?family=Be+Vietnam+Pro:wght@400;500;600;700;800&family=Inter:wght@400;500;600;700;800&display=swap" rel="stylesheet">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/style.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/layout.css">
</head>
<body class="has-side-nav-bar">

<jsp:include page="/views/layout/sidebar-examiner.jsp">
    <jsp:param name="activeSidebar" value="ds-thi-sinh" />
</jsp:include>

<div class="dashboard-shell">
    <main class="main-content">
        
        <nav class="breadcrumbs" aria-label="Breadcrumb">
            <a href="${pageContext.request.contextPath}/views/public/home.jsp">Trang chủ</a>
            <span class="breadcrumbs__separator" aria-hidden="true">/</span>
            <a href="${pageContext.request.contextPath}/views/examiner/dashboard.jsp">Danh sách thí sinh</a>
            <span class="breadcrumbs__separator" aria-hidden="true">/</span>
            <span class="breadcrumbs__current">
                <c:choose>
                    <c:when test="${not empty candidateDetail}">${candidateDetail.fullName}</c:when>
                    <c:otherwise>Chi tiết thí sinh</c:otherwise>
                </c:choose>
            </span>
        </nav>
        
        <c:choose>
            <c:when test="${not empty candidateDetail}">
                <header class="page-header">
                    <div class="page-title-wrap">
                        <h1 class="page-title">${candidateDetail.fullName}</h1>
                        <p class="page-subtitle">Hồ sơ số báo danh ${candidateDetail.sbd} đăng ký sát hạch lái xe khóa thi ${candidateDetail.sessionName}.</p>
                    </div>
                    
                    <div class="page-actions" style="display: flex; gap: 10px;">
                        <a href="${pageContext.request.contextPath}/views/examiner/dashboard.jsp" class="btn-export" style="height: 42px; padding: 0 1.25rem; font-size: 0.9rem; border-radius: 8px; text-decoration: none; display: inline-flex; align-items: center; gap: 6px; background-color: #ffffff; color: #475569;">
                            <svg width="16" height="16" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                                <path d="M19 12H5M12 19l-7-7 7-7" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                            </svg>
                            Quay lại danh sách
                        </a>
                        
                        <c:if test="${candidateDetail.statusKey eq 'warning'}">
                            <a href="${pageContext.request.contextPath}/views/examiner/candidatecall.jsp?sbd=${candidateDetail.sbd}" class="btn-filter" style="height: 42px; padding: 0 1.25rem; font-size: 0.9rem; border-radius: 8px; text-decoration: none; display: inline-flex; align-items: center; gap: 6px; background-color: #059669; border-color: #059669;">
                                <svg width="16" height="16" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                                    <circle cx="9" cy="7" r="4" stroke="currentColor" stroke-width="2"/>
                                    <path d="M3 21v-2a7 7 0 0 1 14 0v2M19 8v6M16 11h6" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                                </svg>
                                Bắt đầu gọi thi
                            </a>
                        </c:if>

                        <c:if test="${candidateDetail.statusKey eq 'success' or candidateDetail.statusKey eq 'danger'}">
                            <a href="${pageContext.request.contextPath}/views/examiner/editscore.jsp" class="btn-filter" style="height: 42px; padding: 0 1.25rem; font-size: 0.9rem; border-radius: 8px; text-decoration: none; display: inline-flex; align-items: center; gap: 6px; background-color: #0052cc; border-color: #0052cc;">
                                <svg width="16" height="16" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                                    <path d="M11 4H4a2 2 0 0 0-2 2v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2v-7M18.5 2.5a2.121 2.121 0 1 1 3 3L12 15l-4 1 1-4 9.5-9.5z" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                                </svg>
                                Điều chỉnh điểm
                            </a>
                        </c:if>
                    </div>
                </header>

                <div class="profile-grid">
                    <aside class="profile-sidebar">
                        <div class="profile-sidebar-card">
                            <div class="profile-avatar-wrap">
                                <div class="profile-avatar-large profile-avatar--${empty candidateDetail.avatarClass ? 'blue' : candidateDetail.avatarClass}">
                                    ${fn:substring(candidateDetail.fullName, 0, 1)}
                                </div>
                                <span class="profile-class-badge">Hạng ${candidateDetail.licenseClass}</span>
                            </div>
                            
                            <h2 class="profile-name">${candidateDetail.fullName}</h2>
                            <p class="profile-sbd">SBD: ${candidateDetail.sbd}</p>
                            
                            <div class="profile-status-wrapper">
                                <span class="action-badge action-badge--${candidateDetail.statusKey}">${candidateDetail.status}</span>
                            </div>

                            <hr style="border: 0; border-top: 1px solid #f1f5f9; width: 100%; margin: 1.5rem 0;">
                            
                            <div class="profile-quick-info">
                                <div class="quick-info-item">
                                    <span class="quick-info-label">Số CCCD:</span>
                                    <span class="quick-info-value">${candidateDetail.cccd}</span>
                                </div>
                                <div class="quick-info-item">
                                    <span class="quick-info-label">Ngày sinh:</span>
                                    <span class="quick-info-value">${candidateDetail.dob}</span>
                                </div>
                                <div class="quick-info-item">
                                    <span class="quick-info-label">Giới tính:</span>
                                    <span class="quick-info-value">${candidateDetail.gender}</span>
                                </div>
                                <div class="quick-info-item">
                                    <span class="quick-info-label">Điện thoại:</span>
                                    <span class="quick-info-value">${candidateDetail.phone}</span>
                                </div>
                                <div class="quick-info-item">
                                    <span class="quick-info-label">Quê quán:</span>
                                    <span class="quick-info-value" style="text-align: right;">${candidateDetail.address}</span>
                                </div>
                            </div>
                        </div>

                        <div class="profile-sidebar-card" style="margin-top: 1.5rem;">
                            <div class="sidebar-card-header">
                                <h3 class="sidebar-card-title">
                                    <svg width="18" height="18" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg" style="color: #0052cc;">
                                        <path d="M23 19a2 2 0 0 1-2 2H3a2 2 0 0 1-2-2V8a2 2 0 0 1 2-2h4l2-3h6l2 3h4a2 2 0 0 1 2 2z" stroke="currentColor" stroke-width="2" stroke-linejoin="round"/>
                                        <circle cx="12" cy="13" r="4" stroke="currentColor" stroke-width="2" stroke-linejoin="round"/>
                                    </svg>
                                    Đối chiếu nhận dạng FaceID
                                </h3>
                            </div>
                            
                            <div class="faceid-match-banner" style="background-color: ${candidateDetail.statusKey eq 'warning' ? 'rgba(245, 158, 11, 0.08)' : 'rgba(16, 185, 129, 0.08)'};">
                                <span style="font-weight: 800; font-size: 1.15rem; color: ${candidateDetail.statusKey eq 'warning' ? '#d97706' : '#10b981'};">
                                    ${empty candidateDetail.faceMatchRate ? '-- %' : candidateDetail.faceMatchRate}
                                </span>
                                <span style="font-size: 0.72rem; color: #64748b; font-weight: 600; text-transform: uppercase; margin-top: 2px;">Trùng khớp sinh trắc</span>
                            </div>

                            <div class="faceid-comparison-box">
                                <div class="face-photo-slot">
                                    <div class="face-photo-placeholder">
                                        <img src="${pageContext.request.contextPath}/assets/imgs/avatar-placeholder.svg" alt="Ảnh chân dung trống" style="width: 24px; height: 24px; opacity: 0.4;">
                                    </div>
                                    <span class="face-photo-label">Ảnh hồ sơ gốc</span>
                                </div>
                                <div class="face-photo-slot">
                                    <div class="face-photo-placeholder" style="border-color: ${candidateDetail.statusKey eq 'warning' ? '#cbd5e1' : '#10b981'};">
                                        <c:if test="${candidateDetail.statusKey eq 'info'}">
                                            <div class="live-indicator">LIVE</div>
                                        </c:if>
                                        <img src="${pageContext.request.contextPath}/assets/imgs/camera-placeholder.svg" alt="Khung chụp ảnh live" style="width: 24px; height: 24px; opacity: 0.4;">
                                    </div>
                                    <span class="face-photo-label">Chụp tại cabin</span>
                                </div>
                            </div>
                            
                            <div class="faceid-verdict-banner faceid-verdict--${candidateDetail.statusKey eq 'warning' ? 'warning' : 'success'}">
                                <c:choose>
                                    <c:when test="${candidateDetail.statusKey eq 'warning'}">
                                        Chưa xác thực sinh trắc khuôn mặt.
                                    </c:when>
                                    <c:otherwise>
                                        Nhận dạng trùng khớp. Đủ điều kiện thi.
                                    </c:otherwise>
                                </c:choose>
                            </div>
                        </div>
                    </aside>

                    <section class="profile-main-content">
                        <div class="profile-section-title">
                            <svg width="20" height="20" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg" style="color: #0052cc;">
                                <circle cx="12" cy="12" r="10" stroke="currentColor" stroke-width="2"/>
                                <path d="M12 16v-4M12 8h.01" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                            </svg>
                            Kết quả các phần thi sát hạch
                        </div>
                        
                        <div class="profile-score-grid">
                            <div class="profile-score-card profile-score-card--${candidateDetail.theoryStatusKey}">
                                <div class="score-card-header">
                                    <span class="score-card-part">PHẦN THI 1</span>
                                    <span class="score-card-badge score-card-badge--${candidateDetail.theoryStatusKey}">${candidateDetail.theoryStatus}</span>
                                </div>
                                <h4 class="score-card-title">Lý thuyết luật GT</h4>
                                <div class="score-card-value">${candidateDetail.theoryScore}</div>
                                <div class="score-card-footer">Yêu cầu đạt chuẩn hạng bằng</div>
                            </div>

                            <div class="profile-score-card profile-score-card--${candidateDetail.practiceStatusKey}">
                                <div class="score-card-header">
                                    <span class="score-card-part">PHẦN THI 2</span>
                                    <span class="score-card-badge score-card-badge--${candidateDetail.practiceStatusKey}">${candidateDetail.practiceStatus}</span>
                                </div>
                                <h4 class="score-card-title">Thực hành sa hình</h4>
                                <div class="score-card-value">${candidateDetail.practiceScore}</div>
                                <div class="score-card-footer">Yêu cầu tối thiểu 80/100</div>
                            </div>

                            <div class="profile-score-card profile-score-card--${candidateDetail.roadStatusKey}">
                                <div class="score-card-header">
                                    <span class="score-card-part">PHẦN THI 3</span>
                                    <span class="score-card-badge score-card-badge--${candidateDetail.roadStatusKey}">${candidateDetail.roadStatus}</span>
                                </div>
                                <h4 class="score-card-title">Sát hạch đường trường</h4>
                                <div class="score-card-value">${candidateDetail.roadScore}</div>
                                <div class="score-card-footer">Yêu cầu tối thiểu 80/100</div>
                            </div>
                        </div>

                        <div class="log-card" style="margin-top: 1.5rem;">
                            <div class="log-card-header" style="border-bottom: none; padding-bottom: 0.5rem;">
                                <h2 class="log-card-title">
                                    <svg width="20" height="20" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg" style="color: #ef4444;">
                                        <path d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-3L13.732 4c-.77-1.333-2.694-1.333-3.464 0L3.34 16c-.77 1.333.192 3 1.732 3z" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                                    </svg>
                                    Nhật ký sa hình & Vi phạm điểm trừ
                                </h2>
                            </div>
                            
                            <div style="padding: 0 1.5rem 1.5rem 1.5rem;">
                                <p style="font-size: 0.85rem; color: #64748b; margin-bottom: 1.25rem;">Nhật ký tự động ghi nhận điểm số còn lại qua từng bài thi sa hình và các lỗi vi phạm.</p>
                                
                                <div class="practice-timeline">
                                    <c:choose>
                                        <c:when test="${not empty candidateDetail.practiceTimeline}">
                                            <c:forEach var="item" items="${candidateDetail.practiceTimeline}">
                                                <div class="timeline-item">
                                                    <div class="timeline-dot ${item.penalty < 0 ? 'timeline-dot--danger' : 'timeline-dot--success'}"></div>
                                                    <div class="timeline-content">
                                                        <div class="timeline-header">
                                                            <span class="timeline-step-title">${item.step}</span>
                                                            <span class="timeline-time">${item.time}</span>
                                                        </div>
                                                        <p class="timeline-note">${item.note}</p>
                                                        <div class="timeline-footer">
                                                            <span class="timeline-score-rem" style="color: ${item.currentScore >= 80 ? '#10b981' : '#ef4444'}; font-weight: 700;">Điểm còn lại: ${item.currentScore}</span>
                                                            <c:if test="${item.penalty < 0}">
                                                                <span class="timeline-penalty-badge">${item.penalty} điểm</span>
                                                            </c:if>
                                                        </div>
                                                    </div>
                                                </div>
                                            </c:forEach>
                                        </c:when>
                                        <c:otherwise>
                                            <div style="text-align: center; color: #94a3b8; font-style: italic; padding: 2rem 0; font-size: 0.88rem;">
                                                Không có lỗi vi phạm hoặc tiến trình thi sa hình nào được ghi nhận.
                                            </div>
                                        </c:otherwise>
                                    </c:choose>
                                </div>
                            </div>
                        </div>

                        <div class="log-card" style="margin-top: 1.5rem;">
                            <div class="log-card-header">
                                <h2 class="log-card-title">
                                    <svg width="20" height="20" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg" style="color: #0052cc;">
                                        <path d="M12 22c5.523 0 10-4.477 10-10S17.523 2 12 2 2 6.477 2 12s4.477 10 10 10z" stroke="currentColor" stroke-width="2"/>
                                        <path d="M12 6v6l4 2" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                                    </svg>
                                    Nhật ký thao tác và Tiến trình hệ thống
                                </h2>
                                <span class="action-badge action-badge--info" style="font-weight: 700;">AUDIT TRAIL SECURED</span>
                            </div>
                            
                            <div class="table-responsive">
                                <table class="audit-table" style="font-size: 0.85rem;">
                                    <thead>
                                        <tr>
                                            <th scope="col" style="width: 130px;">Thời gian</th>
                                            <th scope="col" style="width: 180px;">Sự kiện</th>
                                            <th scope="col">Mô tả chi tiết sự kiện</th>
                                            <th scope="col" style="width: 140px; text-align: center;">Người thực hiện</th>
                                        </tr>
                                    </thead>
                                    <tbody>
                                        <c:choose>
                                            <c:when test="${not empty candidateDetail.auditLogs}">
                                                <c:forEach var="log" items="${candidateDetail.auditLogs}">
                                                    <tr>
                                                        <td style="color: #64748b; font-weight: 600;">${log.time}</td>
                                                        <td>
                                                            <span class="action-badge action-badge--info" style="font-size: 0.72rem; padding: 2px 6px;">${log.event}</span>
                                                        </td>
                                                        <td style="font-weight: 500; color: #334155;">${log.desc}</td>
                                                        <td style="text-align: center; color: #0052cc; font-weight: 700;">@${log.user}</td>
                                                    </tr>
                                                </c:forEach>
                                            </c:when>
                                            <c:otherwise>
                                                <tr>
                                                    <td colspan="4" style="text-align: center; color: #94a3b8; font-style: italic; padding: 2rem 0;">
                                                        Chưa có hồ sơ kiểm toán ghi nhận.
                                                    </td>
                                                </tr>
                                            </c:otherwise>
                                        </c:choose>
                                    </tbody>
                                </table>
                            </div>
                        </div>
                    </section>
                </div>
            </c:when>
            <c:otherwise>
                <div class="log-card" style="text-align: center; padding: 8rem 2rem;">
                    <img src="${pageContext.request.contextPath}/assets/imgs/empty-candidates.svg" alt="Không tìm thấy hồ sơ" style="width: 64px; height: 64px; margin: 0 auto 1.5rem; display: block; opacity: 0.25;">
                    <h2 style="font-size: 1.25rem; font-weight: 700; color: #0f172a; margin-bottom: 0.5rem;">Không tìm thấy thông tin chi tiết thí sinh</h2>
                    <p style="color: #64748b; font-size: 0.9rem; max-width: 400px; margin: 0 auto 1.5rem;">
                        Dữ liệu chi tiết thí sinh chưa được truyền từ máy chủ. Vui lòng quay lại danh sách thí sinh và thử lại.
                    </p>
                    <a href="${pageContext.request.contextPath}/views/examiner/dashboard.jsp" class="btn-filter" style="height: 40px; padding: 0 1.5rem; font-size: 0.9rem; border-radius: 8px; text-decoration: none; display: inline-flex; align-items: center; background-color: #0052cc; border-color: #0052cc;">
                        Quay lại danh sách
                    </a>
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

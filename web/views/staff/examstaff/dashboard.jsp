<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib prefix = "fn" uri = "http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix = "fmt" uri = "http://java.sun.com/jsp/jstl/fmt" %>

<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Dashboard Giám khảo - Lái Vui</title>
    
    <!-- Google Fonts: Inter & Be Vietnam Pro -->
    <link rel="preconnect" href="https://fonts.googleapis.com">
    <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
    <link href="https://fonts.googleapis.com/css2?family=Be+Vietnam+Pro:wght@400;500;600;700;800&family=Inter:wght@400;500;600;700;800&display=swap" rel="stylesheet">
    
    <!-- External Layout Stylesheets (Matching layout standard) -->
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/style.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/layout.css">
</head>
<body class="has-side-nav-bar">

<jsp:include page="/views/layout/sidebar-examstaff.jsp">
    <jsp:param name="activeSidebar" value="dashboard" />
</jsp:include>

<div class="dashboard-shell">

    <main class="main-content">
        
        <!-- Breadcrumbs Navigation -->
        <nav class="breadcrumbs" aria-label="Breadcrumb">
            <a href="${pageContext.request.contextPath}/views/public/home.jsp">Trang chủ</a>
            <span class="breadcrumbs__separator" aria-hidden="true">/</span>
            <span class="breadcrumbs__current">Quản lý thi</span>
            <span class="breadcrumbs__separator" aria-hidden="true">/</span>
            <span class="breadcrumbs__current" aria-current="page">Dashboard</span>
        </nav>
        
        <!-- Page Header Section -->
        <header class="page-header">
            <div class="page-title-wrap">
                <h1 class="page-title">Dashboard Giám khảo</h1>
                <p class="page-subtitle">Tổng quan số liệu ca thi sát hạch lái xe, giám sát trực tiếp phòng thi lý thuyết và sa hình.</p>
            </div>
            
            <div class="page-actions" style="display: flex; gap: 10px;">
                <a href="candidatecall.jsp" class="btn-filter" style="height: 42px; padding: 0 1.25rem; font-size: 0.9rem; border-radius: 8px; flex: none; text-decoration: none; background-color: #0052cc; border-color: #0052cc; display: inline-flex; align-items: center; gap: 6px;">
                    <svg width="16" height="16" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                        <circle cx="9" cy="7" r="4" stroke="currentColor" stroke-width="2"/>
                        <path d="M3 21v-2a7 7 0 0 1 14 0v2M19 8v6M16 11h6" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                    </svg>
                    Điều hành gọi thi
                </a>
            </div>
        </header>

        <!-- KPI Metrics Row -->
        <section class="metrics-row" aria-label="Chỉ số ca thi">
            <!-- Card 1: Active Exam Session -->
            <div class="stat-card">
                <div class="stat-icon stat-icon--blue">
                    <svg width="24" height="24" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                        <path d="M8 7V3m8 4V3m-9 8h10M5 21h14a2 2 0 0 0 2-2V7a2 2 0 0 0-2-2H5a2 2 0 0 0-2 2v12a2 2 0 0 0 2 2z" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                    </svg>
                </div>
                <div class="stat-info">
                    <span class="stat-number" style="font-size: 1.15rem; font-weight: 800; color: #0f172a; margin-bottom: 0.15rem;">${empty activeSessionName ? 'Chưa mở ca thi' : activeSessionName}</span>
                    <span class="stat-label">Đợt thi sát hạch</span>
                    <span class="stat-trend ${empty activeSessionName ? 'stat-trend--down' : 'stat-trend--up'}">${empty activeSessionStatus ? 'Không hoạt động' : activeSessionStatus}</span>
                </div>
            </div>
            
            <!-- Card 2: Total Candidates in Session -->
            <div class="stat-card">
                <div class="stat-icon stat-icon--amber">
                    <svg width="24" height="24" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                        <path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2M9 11a4 4 0 1 0 0-8 4 4 0 0 0 0 8zm14 10v-2a4 4 0 0 0-3-3.87M16 3.13a4 4 0 0 1 0 7.75" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                    </svg>
                </div>
                <div class="stat-info">
                    <span class="stat-number">${empty totalExaminees ? 0 : totalExaminees}</span>
                    <span class="stat-label">Tổng thí sinh ca thi</span>
                    <span class="stat-trend stat-trend--up">${empty sessionCandidatesTrend ? '0 đăng ký mới' : sessionCandidatesTrend}</span>
                </div>
            </div>
            
            <!-- Card 3: Active Candidates (Testing) -->
            <div class="stat-card">
                <div class="stat-icon stat-icon--blue">
                    <svg width="24" height="24" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                        <circle cx="12" cy="12" r="10" stroke="currentColor" stroke-width="2"/>
                        <path d="M12 6v6l4 2" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                    </svg>
                </div>
                <div class="stat-info">
                    <span class="stat-number" style="color: #0052cc;">${empty activeExaminees ? 0 : activeExaminees}</span>
                    <span class="stat-label">Đang thi trực tiếp</span>
                    <span class="stat-trend stat-trend--up">Trong phòng thi / cabin</span>
                </div>
            </div>
            
            <!-- Card 4: Completed Candidates -->
            <div class="stat-card">
                <div class="stat-icon stat-icon--green">
                    <svg width="24" height="24" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                        <path d="M22 11.08V12a10 10 0 1 1-5.93-9.14" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                        <path d="M22 4L12 14.01l-3-3" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                    </svg>
                </div>
                <div class="stat-info">
                    <span class="stat-number" style="color: #10b981;">${empty passedExaminees ? 0 : passedExaminees}</span>
                    <span class="stat-label">Đạt sát hạch</span>
                    <span class="stat-trend stat-trend--up">${empty successRate ? "0%" : successRate} tỷ lệ đạt ca này</span>
                </div>
            </div>
        </section>

        <!-- Grid layout for Live Monitor and Quick Actions -->
        <div class="report-grid" style="grid-template-columns: 1.8fr 1fr; margin-top: 1.5rem;">
            
            <!-- Left Pane: Live Room Monitor -->
            <div class="report-pane">
                <div class="grading-pane__header" style="border-bottom: 1px solid #f1f5f9; padding-bottom: 0.75rem; margin-bottom: 1.25rem;">
                    <h2 class="grading-pane__title" style="font-size: 1.05rem; display: inline-flex; align-items: center; gap: 8px;">
                        <svg width="20" height="20" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg" style="color: #0052cc;">
                            <rect x="2" y="3" width="20" height="14" rx="2" stroke="currentColor" stroke-width="2"/>
                            <path d="M8 21h8M12 17v4" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
                        </svg>
                        Giám sát Trực Tiếp Phòng Máy (Live Monitor)
                    </h2>
                    <span class="action-badge action-badge--success" style="font-size: 0.75rem; font-weight: 700; padding: 2px 8px;">Đang kết nối</span>
                </div>

                <!-- Live Computers Layout Grid -->
                <div style="display: grid; grid-template-columns: repeat(3, 1fr); gap: 1rem;">
                    <c:choose>
                        <c:when test="${not empty liveComputers}">
                            <c:forEach var="comp" items="${liveComputers}">
                                <c:choose>
                                    <c:when test="${comp.status eq 'testing'}">
                                        <div style="border: 1px solid #0052cc; background-color: rgba(0, 82, 204, 0.02); border-radius: 12px; padding: 1rem; position: relative;">
                                            <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 0.75rem;">
                                                <span style="font-size: 0.8rem; font-weight: 700; color: #475569; font-family: monospace;">${comp.computerName}</span>
                                                <span class="action-badge action-badge--info" style="font-size: 0.7rem; padding: 1px 6px;">Đang thi</span>
                                            </div>
                                            <div style="font-size: 0.9rem; font-weight: 700; color: #0f172a; margin-bottom: 0.25rem;">${comp.candidateName}</div>
                                            <div style="font-size: 0.75rem; color: #64748b; margin-bottom: 0.75rem;">SBD: ${comp.sbd} | Hạng ${comp.licenseClass}</div>
                                            <div style="background-color: #f1f5f9; border-radius: 6px; padding: 6px 10px; display: flex; justify-content: space-between; align-items: center;">
                                                <span style="font-size: 0.75rem; color: #475569;">Thời gian còn lại:</span>
                                                <span style="font-size: 0.8rem; font-weight: 700; color: #0f172a; font-family: monospace;">${comp.extraInfo}</span>
                                            </div>
                                        </div>
                                    </c:when>
                                    
                                    <c:when test="${comp.status eq 'passed'}">
                                        <div style="border: 1px solid #10b981; background-color: rgba(16, 185, 129, 0.01); border-radius: 12px; padding: 1rem; position: relative;">
                                            <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 0.75rem;">
                                                <span style="font-size: 0.8rem; font-weight: 700; color: #475569; font-family: monospace;">${comp.computerName}</span>
                                                <span class="action-badge action-badge--success" style="font-size: 0.7rem; padding: 1px 6px;">Đạt sát hạch</span>
                                            </div>
                                            <div style="font-size: 0.9rem; font-weight: 700; color: #0f172a; margin-bottom: 0.25rem;">${comp.candidateName}</div>
                                            <div style="font-size: 0.75rem; color: #64748b; margin-bottom: 0.75rem;">SBD: ${comp.sbd} | Hạng ${comp.licenseClass}</div>
                                            <div style="background-color: #ecfdf5; border-radius: 6px; padding: 6px 10px; display: flex; justify-content: space-between; align-items: center;">
                                                <span style="font-size: 0.75rem; color: #047857; font-weight: 600;">Điểm thi đạt:</span>
                                                <span style="font-size: 0.82rem; font-weight: 800; color: #047857; font-family: monospace;">${comp.extraInfo}</span>
                                            </div>
                                        </div>
                                    </c:when>
 
                                    <c:when test="${comp.status eq 'failed'}">
                                        <div style="border: 1px solid #ef4444; background-color: rgba(239, 68, 68, 0.01); border-radius: 12px; padding: 1rem; position: relative;">
                                            <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 0.75rem;">
                                                <span style="font-size: 0.8rem; font-weight: 700; color: #475569; font-family: monospace;">${comp.computerName}</span>
                                                <span class="action-badge action-badge--danger" style="font-size: 0.7rem; padding: 1px 6px;">Chưa đạt</span>
                                            </div>
                                            <div style="font-size: 0.9rem; font-weight: 700; color: #0f172a; margin-bottom: 0.25rem;">${comp.candidateName}</div>
                                            <div style="font-size: 0.75rem; color: #64748b; margin-bottom: 0.75rem;">SBD: ${comp.sbd} | Hạng ${comp.licenseClass}</div>
                                            <div style="background-color: #fef2f2; border-radius: 6px; padding: 6px 10px; display: flex; justify-content: space-between; align-items: center;">
                                                <span style="font-size: 0.75rem; color: #b91c1c; font-weight: 600;">Điểm thi trượt:</span>
                                                <span style="font-size: 0.82rem; font-weight: 800; color: #b91c1c; font-family: monospace;">${comp.extraInfo}</span>
                                            </div>
                                        </div>
                                    </c:when>
 
                                    <c:otherwise>
                                        <div style="border: 1px dashed #cbd5e1; border-radius: 12px; padding: 1rem; display: flex; flex-direction: column; justify-content: center; align-items: center; min-height: 124px;">
                                            <span style="font-size: 0.8rem; font-weight: 700; color: #64748b; font-family: monospace; margin-bottom: 0.5rem;">${comp.computerName}</span>
                                            <div style="font-size: 0.82rem; font-weight: 600; color: #94a3b8; display: inline-flex; align-items: center; gap: 4px;">
                                                <svg width="12" height="12" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                                                    <circle cx="12" cy="12" r="10" stroke="currentColor" stroke-width="2"/>
                                                    <path d="M12 8v8M8 12h8" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
                                                </svg>
                                                Sẵn sàng
                                            </div>
                                        </div>
                                    </c:otherwise>
                                </c:choose>
                            </c:forEach>
                        </c:when>
                        
                        <c:otherwise>
                            <!-- EMPTY STATE KHI KHÔNG CÓ MÁY THI HOẠT ĐỘNG -->
                            <div style="grid-column: span 3; text-align: center; padding: 3.5rem 1rem; color: #64748b;">
                                <img src="${pageContext.request.contextPath}/assets/imgs/empty-grading.svg" alt="Không có hoạt động" style="width: 54px; height: 54px; margin: 0 auto 1rem; display: block; opacity: 0.3;">
                                <div style="font-size: 0.9rem; font-weight: 600; color: #475569;">Phòng thi hiện đang trống</div>
                                <p style="font-size: 0.8rem; color: #94a3b8; margin-top: 0.25rem; max-width: 320px; margin-left: auto; margin-right: auto;">Không ghi nhận máy thi nào đang vận hành trong ca thi này. Nhấn gọi thí sinh để bắt đầu ca sát hạch.</p>
                            </div>
                        </c:otherwise>
                    </c:choose>
                </div>
            </div>
            
            <!-- Right Pane: Quick Actions & Sessions -->
            <div class="report-pane" style="display: flex; flex-direction: column; justify-content: space-between;">
                <div>
                    <div class="grading-pane__header" style="border-bottom: 1px solid #f1f5f9; padding-bottom: 0.75rem; margin-bottom: 1.25rem;">
                        <h2 class="grading-pane__title" style="font-size: 1.05rem;">Phím tắt nghiệp vụ</h2>
                    </div>
                    
                    <div style="display: flex; flex-direction: column; gap: 0.75rem;">
                        <a href="upload.jsp" style="text-decoration: none; display: flex; align-items: center; gap: 10px; padding: 12px 16px; border: 1px solid #e2e8f0; border-radius: 8px; color: #334155; background: #ffffff; font-weight: 600; font-size: 0.88rem; transition: all 0.2s;">
                            <span style="color: #0052cc; display: inline-flex; align-items: center;">
                                <svg width="18" height="18" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                                    <path d="M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4M7 10l5 5 5-5M12 15V3" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                                </svg>
                            </span>
                            Tải danh sách Excel thí sinh
                        </a>
                        
                        <a href="candidatecall.jsp" style="text-decoration: none; display: flex; align-items: center; gap: 10px; padding: 12px 16px; border: 1px solid #e2e8f0; border-radius: 8px; color: #334155; background: #ffffff; font-weight: 600; font-size: 0.88rem; transition: all 0.2s;">
                            <span style="color: #059669; display: inline-flex; align-items: center;">
                                <svg width="18" height="18" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                                    <circle cx="9" cy="7" r="4" stroke="currentColor" stroke-width="2"/>
                                    <path d="M3 21v-2a7 7 0 0 1 14 0v2M19 8v6M16 11h6" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                                </svg>
                            </span>
                            Gọi thí sinh sát hạch sa hình
                        </a>

                        <a href="candidatelist.jsp" style="text-decoration: none; display: flex; align-items: center; gap: 10px; padding: 12px 16px; border: 1px solid #e2e8f0; border-radius: 8px; color: #334155; background: #ffffff; font-weight: 600; font-size: 0.88rem; transition: all 0.2s;">
                            <span style="color: #4f46e5; display: inline-flex; align-items: center;">
                                <svg width="18" height="18" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                                    <path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2" stroke="currentColor" stroke-width="1.5" stroke-linecap="round"/>
                                    <circle cx="9" cy="7" r="4" stroke="currentColor" stroke-width="1.5"/>
                                </svg>
                            </span>
                            Xem Danh sách thí sinh & Sửa điểm
                        </a>

                        <a href="report.jsp" style="text-decoration: none; display: flex; align-items: center; gap: 10px; padding: 12px 16px; border: 1px solid #e2e8f0; border-radius: 8px; color: #334155; background: #ffffff; font-weight: 600; font-size: 0.88rem; transition: all 0.2s;">
                            <span style="color: #ea580c; display: inline-flex; align-items: center;">
                                <svg width="18" height="18" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                                    <rect x="3" y="3" width="18" height="18" rx="2" stroke="currentColor" stroke-width="2"/>
                                    <path d="M9 17v-5M15 17v-3M12 17v-8" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
                                </svg>
                            </span>
                            Báo cáo thống kê ca thi
                        </a>
                    </div>
                </div>

                <div style="margin-top: 1.5rem; border-top: 1px solid #f1f5f9; padding-top: 1.25rem;">
                    <div style="font-size: 0.82rem; font-weight: 700; color: #64748b; text-transform: uppercase; margin-bottom: 0.75rem; letter-spacing: 0.5px;">Trạng thái đợt sát hạch</div>
                    <div style="display: flex; flex-direction: column; gap: 8px;">
                        <div style="display: flex; justify-content: space-between; font-size: 0.82rem;">
                            <span style="color: #475569;">Trưởng ban giám khảo:</span>
                            <span style="font-weight: 700; color: #0f172a;">${empty headExaminerName ? 'Chưa phân công' : headExaminerName}</span>
                        </div>
                        <div style="display: flex; justify-content: space-between; font-size: 0.82rem;">
                            <span style="color: #475569;">Phòng thi lý thuyết:</span>
                            <span style="font-weight: 700; color: #059669;">${empty theoryRoomStatus ? 'Không hoạt động' : theoryRoomStatus}</span>
                        </div>
                        <div style="display: flex; justify-content: space-between; font-size: 0.82rem;">
                            <span style="color: #475569;">Sân sa hình hạng A1/B2:</span>
                            <span style="font-weight: 700; color: #059669;">${empty practiceFieldStatus ? 'Không hoạt động' : practiceFieldStatus}</span>
                        </div>
                    </div>
                </div>
            </div>

        </div>

    </main>

    <%-- Inject the footer template --%>
    <jsp:include page="/views/layout/footer.jsp">
        <jsp:param name="standalone" value="false" />
    </jsp:include>
</div>

</body>
</html>

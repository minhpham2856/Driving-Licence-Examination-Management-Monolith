<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib prefix = "fn" uri = "http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix = "fmt" uri = "http://java.sun.com/jsp/jstl/fmt" %>

<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Tổng Quan Ca Thi - Ban Sát Hạch</title>
    
    <!-- Google Fonts: Inter & Be Vietnam Pro -->
    <link rel="preconnect" href="https://fonts.googleapis.com">
    <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
    <link href="https://fonts.googleapis.com/css2?family=Be+Vietnam+Pro:wght@400;500;600;700;800&family=Inter:wght@400;500;600;700;800&display=swap" rel="stylesheet">
    
    <!-- External Layout Stylesheets -->
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/style.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/layout.css">
    
    <style>
        .progress-indicator-bar {
            display: flex;
            height: 12px;
            border-radius: 999px;
            overflow: hidden;
            background-color: #e2e8f0;
            margin: 1rem 0;
            box-shadow: inset 0 2px 4px rgba(0,0,0,0.05);
        }
        .progress-indicator-segment {
            height: 100%;
            transition: width 0.4s ease;
        }
        .progress-indicator-segment--success { background: linear-gradient(90deg, #10b981, #059669); }
        .progress-indicator-segment--info { background: linear-gradient(90deg, #3b82f6, #1d4ed8); }
        .progress-indicator-segment--pending { background: linear-gradient(90deg, #f59e0b, #d97706); }
        .progress-indicator-segment--empty { background-color: #cbd5e1; }
        
        .progress-legend {
            display: flex;
            gap: 1.5rem;
            flex-wrap: wrap;
            margin-bottom: 1.5rem;
            font-size: 0.85rem;
        }
        .progress-legend-item {
            display: flex;
            align-items: center;
            gap: 6px;
            font-weight: 500;
            color: #475569;
        }
        .progress-legend-dot {
            width: 10px;
            height: 10px;
            border-radius: 50%;
        }
        
        .room-monitor-grid {
            display: grid;
            grid-template-columns: repeat(3, 1fr);
            gap: 1.5rem;
            margin-top: 1.5rem;
        }
        
        .room-monitor-card {
            background: rgba(255, 255, 255, 0.7);
            backdrop-filter: blur(16px);
            -webkit-backdrop-filter: blur(16px);
            border: 1px solid rgba(226, 232, 240, 0.8);
            border-radius: 16px;
            box-shadow: 0 4px 20px -2px rgba(15, 23, 42, 0.04);
            padding: 1.25rem;
            display: flex;
            flex-direction: column;
            gap: 1rem;
            transition: transform 0.2s ease, box-shadow 0.2s ease;
        }
        .room-monitor-card:hover {
            transform: translateY(-2px);
            box-shadow: 0 10px 25px -5px rgba(15, 23, 42, 0.08);
        }
        
        .room-header {
            display: flex;
            justify-content: space-between;
            align-items: flex-start;
            border-bottom: 1px solid #f1f5f9;
            padding-bottom: 0.75rem;
        }
        .room-title {
            font-size: 1rem;
            font-weight: 700;
            color: #0f172a;
            display: flex;
            align-items: center;
            gap: 8px;
        }
        .room-badge {
            font-size: 0.72rem;
            font-weight: 700;
            padding: 2px 8px;
            border-radius: 6px;
        }
        .room-badge--green { background-color: rgba(16, 185, 129, 0.1); color: #059669; }
        .room-badge--blue { background-color: rgba(59, 130, 246, 0.1); color: #1d4ed8; }
        .room-badge--orange { background-color: rgba(245, 158, 11, 0.1); color: #b45309; }
        
        .room-candidate-list {
            display: flex;
            flex-direction: column;
            gap: 0.75rem;
            min-height: 240px;
        }
        
        .room-candidate-item {
            background: #ffffff;
            border: 1px solid #f1f5f9;
            border-radius: 12px;
            padding: 0.85rem;
            display: flex;
            flex-direction: column;
            gap: 0.5rem;
            transition: all 0.2s ease;
        }
        .room-candidate-item:hover {
            border-color: #cbd5e1;
            box-shadow: 0 2px 8px rgba(0, 0, 0, 0.02);
        }
        
        .candidate-meta {
            display: flex;
            justify-content: space-between;
            align-items: center;
            font-size: 0.75rem;
            color: #64748b;
        }
        .candidate-name {
            font-size: 0.88rem;
            font-weight: 700;
            color: #0f172a;
        }
        .candidate-sbd {
            font-family: monospace;
            font-weight: 800;
            color: #0052cc;
            font-size: 0.88rem;
        }
        .candidate-step {
            display: inline-flex;
            align-items: center;
            gap: 4px;
            font-size: 0.75rem;
            font-weight: 600;
            padding: 2px 6px;
            border-radius: 4px;
        }
        .candidate-step--verify { background-color: #eff6ff; color: #1d4ed8; }
        .candidate-step--photo { background-color: #faf5ff; color: #7e22ce; }
        .candidate-step--payment { background-color: #fef3c7; color: #b45309; }
        .candidate-step--ready { background-color: #ecfdf5; color: #047857; }
        .candidate-step--waiting { background-color: #f8fafc; color: #475569; }
        
        .empty-room-state {
            display: flex;
            flex-direction: column;
            align-items: center;
            justify-content: center;
            flex: 1;
            color: #94a3b8;
            font-size: 0.8rem;
            text-align: center;
            padding: 2rem 1rem;
        }
    </style>
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
            <span class="breadcrumbs__current">Ban Sát Hạch</span>
            <span class="breadcrumbs__separator" aria-hidden="true">/</span>
            <span class="breadcrumbs__current" aria-current="page">Tổng quan ca thi</span>
        </nav>
        
        <!-- Page Header Section -->
        <header class="page-header">
            <div class="page-title-wrap">
                <h1 class="page-title">Tổng quan ca thi sát hạch</h1>
                <p class="page-subtitle">Giám sát trực quan tiến độ đón tiếp, phân bổ, làm thủ tục hồ sơ và trạng thái thi của thí sinh trong ngày.</p>
            </div>
            
            <div class="page-actions" style="display: flex; gap: 10px;">
                <a href="allocation.jsp" class="btn-export" style="height: 42px; padding: 0 1.25rem; font-size: 0.9rem; border-radius: 8px; background-color: #ffffff; color: #475569; border-color: #e2e8f0; text-decoration: none; display: inline-flex; align-items: center; gap: 6px;">
                    <svg width="18" height="18" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                        <rect x="3" y="3" width="7" height="9" rx="1.5" stroke="currentColor" stroke-width="1.5"/>
                        <rect x="14" y="3" width="7" height="5" rx="1.5" stroke="currentColor" stroke-width="1.5"/>
                        <rect x="14" y="12" width="7" height="9" rx="1.5" stroke="currentColor" stroke-width="1.5"/>
                        <rect x="3" y="16" width="7" height="5" rx="1.5" stroke="currentColor" stroke-width="1.5"/>
                    </svg>
                    Phân bổ khu vực
                </a>
                <a href="procedure.jsp" class="btn-filter" style="height: 42px; padding: 0 1.25rem; font-size: 0.9rem; border-radius: 8px; flex: none; text-decoration: none; background-color: #0052cc; border-color: #0052cc; display: inline-flex; align-items: center; gap: 6px;">
                    <svg width="18" height="18" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                        <rect x="2" y="3" width="20" height="18" rx="2.5" stroke="currentColor" stroke-width="1.5"/>
                        <circle cx="7.5" cy="10" r="3" stroke="currentColor" stroke-width="1.5"/>
                        <path d="M3.5 18c0-2 2-3.5 4-3.5s4 1.5 4 3.5" stroke="currentColor" stroke-width="1.5" stroke-linecap="round"/>
                        <line x1="14" y1="8" x2="19" y2="8" stroke="currentColor" stroke-width="1.5" stroke-linecap="round"/>
                        <line x1="14" y1="12" x2="19" y2="12" stroke="currentColor" stroke-width="1.5" stroke-linecap="round"/>
                        <line x1="14" y1="16" x2="17" y2="16" stroke="currentColor" stroke-width="1.5" stroke-linecap="round"/>
                    </svg>
                    Mở Bàn thủ tục (3 Bước)
                </a>
            </div>
        </header>

        <!-- Dynamic parameters or standard values simulation using JSTL -->
        <c:set var="totalCandidatesCount" value="120" />
        <c:set var="completedCount" value="48" />
        <c:set var="processingCount" value="18" />
        <c:set var="pendingCount" value="54" />
        
        <c:set var="completedPercent" value="${(completedCount * 100) / totalCandidatesCount}" />
        <c:set var="processingPercent" value="${(processingCount * 100) / totalCandidatesCount}" />
        <c:set var="pendingPercent" value="${(pendingCount * 100) / totalCandidatesCount}" />

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
                    <span class="stat-number" style="font-size: 1.1rem; font-weight: 800; color: #0f172a; margin-bottom: 0.15rem;">Ca Sáng 24/05</span>
                    <span class="stat-label">Đợt thi sát hạch</span>
                    <span class="stat-trend stat-trend--up">Đang diễn ra</span>
                </div>
            </div>
            
            <!-- Card 2: Total Candidates in Session -->
            <div class="stat-card">
                <div class="stat-icon stat-icon--blue" style="background-color: rgba(59, 130, 246, 0.06); color: #2563eb;">
                    <svg width="24" height="24" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                        <path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2M9 11a4 4 0 1 0 0-8 4 4 0 0 0 0 8z" stroke="currentColor" stroke-width="2"/>
                    </svg>
                </div>
                <div class="stat-info">
                    <span class="stat-number">${totalCandidatesCount}</span>
                    <span class="stat-label">Tổng thí sinh ca thi</span>
                    <span class="stat-trend stat-trend--up">100% hồ sơ hợp lệ</span>
                </div>
            </div>
            
            <!-- Card 3: Completed Procedures -->
            <div class="stat-card">
                <div class="stat-icon stat-icon--green">
                    <svg width="24" height="24" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                        <path d="M22 11.08V12a10 10 0 1 1-5.93-9.14" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                        <path d="M22 4L12 14.01l-3-3" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                    </svg>
                </div>
                <div class="stat-info">
                    <span class="stat-number" style="color: #10b981;">${completedCount}</span>
                    <span class="stat-label">Đã xong thủ tục</span>
                    <span class="stat-trend stat-trend--up"><fmt:formatNumber value="${completedPercent}" maxFractionDigits="1"/>% hoàn thành</span>
                </div>
            </div>
            
            <!-- Card 4: Undergoing Procedure -->
            <div class="stat-card">
                <div class="stat-icon stat-icon--amber">
                    <svg width="24" height="24" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                        <circle cx="12" cy="12" r="10" stroke="currentColor" stroke-width="2"/>
                        <path d="M12 6v6l4 2" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                    </svg>
                </div>
                <div class="stat-info">
                    <span class="stat-number" style="color: #d97706;">${processingCount}</span>
                    <span class="stat-label">Đang làm thủ tục</span>
                    <span class="stat-trend stat-trend--up">Tại quầy / bàn chờ</span>
                </div>
            </div>
        </section>

        <!-- Procedure Progress Visualization Section -->
        <div class="report-pane" style="margin-top: 1.5rem;">
            <div class="grading-pane__header" style="border-bottom: none; padding-bottom: 0; margin-bottom: 0.5rem;">
                <h2 class="grading-pane__title" style="font-size: 1.05rem; display: inline-flex; align-items: center; gap: 8px;">
                    <svg width="20" height="20" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg" style="color: #0052cc;">
                        <path d="M12 20h9M3 20v-8a2 2 0 0 1 2-2h4a2 2 0 0 1 2 2v8M13 20V4a2 2 0 0 1 2-2h4a2 2 0 0 1 2 2v16" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                    </svg>
                    Tiến độ hoàn thiện thủ tục trong ngày
                </h2>
            </div>
            
            <div class="progress-indicator-bar">
                <div class="progress-indicator-segment progress-indicator-segment--success" style="width: ${completedPercent}%" title="Đã xong thủ tục: ${completedCount}"></div>
                <div class="progress-indicator-segment progress-indicator-segment--info" style="width: ${processingPercent}%" title="Đang làm thủ tục: ${processingCount}"></div>
                <div class="progress-indicator-segment progress-indicator-segment--pending" style="width: ${pendingPercent}%" title="Chưa đến / Đang chờ: ${pendingCount}"></div>
            </div>
            
            <div class="progress-legend">
                <div class="progress-legend-item">
                    <span class="progress-legend-dot" style="background-color: #10b981;"></span>
                    <span>Đã hoàn thành: <strong>${completedCount}</strong> học viên (${fn:substring(completedPercent, 0, 4)}%)</span>
                </div>
                <div class="progress-legend-item">
                    <span class="progress-legend-dot" style="background-color: #3b82f6;"></span>
                    <span>Đang làm hồ sơ / Đối chiếu: <strong>${processingCount}</strong> học viên (${fn:substring(processingPercent, 0, 4)}%)</span>
                </div>
                <div class="progress-legend-item">
                    <span class="progress-legend-dot" style="background-color: #f59e0b;"></span>
                    <span>Chưa đến / Chờ gọi: <strong>${pendingCount}</strong> học viên (${fn:substring(pendingPercent, 0, 4)}%)</span>
                </div>
            </div>
        </div>

        <!-- Room Monitoring Dashboard (Replaces Machine Grid) -->
        <div class="room-monitor-grid">
            
            <!-- Column 1: Waiting Room (Phòng Chờ) -->
            <div class="room-monitor-card">
                <div class="room-header">
                    <h3 class="room-title">
                        <svg width="20" height="20" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg" style="color: #ea580c;">
                            <path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2M9 11a4 4 0 1 0 0-8 4 4 0 0 0 0 8z" stroke="currentColor" stroke-width="2"/>
                            <path d="M23 21v-2a4 4 0 0 0-3-3.87M16 3.13a4 4 0 0 1 0 7.75" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
                        </svg>
                        Phòng chờ chính
                    </h3>
                    <span class="room-badge room-badge--orange">Chờ gọi</span>
                </div>
                
                <div class="room-candidate-list">
                    <!-- Simulate Candidates in Waiting Room using JSTL -->
                    <div class="room-candidate-item">
                        <div class="candidate-meta">
                            <span class="candidate-sbd">SBD: B2-0239</span>
                            <span class="candidate-step candidate-step--waiting">Hạng B2</span>
                        </div>
                        <div class="candidate-name">Phạm Minh Hoàng</div>
                        <div style="font-size: 0.72rem; color: #64748b; display: flex; justify-content: space-between; align-items: center; margin-top: 2px;">
                            <span>Trạng thái: Đang chờ</span>
                            <span style="font-weight: 600; color: #475569;">Thời gian chờ: 15p</span>
                        </div>
                    </div>
                    
                    <div class="room-candidate-item">
                        <div class="candidate-meta">
                            <span class="candidate-sbd">SBD: A1-0182</span>
                            <span class="candidate-step candidate-step--waiting">Hạng A1</span>
                        </div>
                        <div class="candidate-name">Lê Thị Thanh Huyền</div>
                        <div style="font-size: 0.72rem; color: #64748b; display: flex; justify-content: space-between; align-items: center; margin-top: 2px;">
                            <span>Trạng thái: Đang chờ</span>
                            <span style="font-weight: 600; color: #475569;">Thời gian chờ: 12p</span>
                        </div>
                    </div>
                    
                    <div class="room-candidate-item">
                        <div class="candidate-meta">
                            <span class="candidate-sbd">SBD: B2-0199</span>
                            <span class="candidate-step candidate-step--waiting">Hạng B2</span>
                        </div>
                        <div class="candidate-name">Đặng Văn Lâm</div>
                        <div style="font-size: 0.72rem; color: #64748b; display: flex; justify-content: space-between; align-items: center; margin-top: 2px;">
                            <span>Trạng thái: Đang chờ</span>
                            <span style="font-weight: 600; color: #475569;">Thời gian chờ: 8p</span>
                        </div>
                    </div>

                    <div class="room-candidate-item">
                        <div class="candidate-meta">
                            <span class="candidate-sbd">SBD: A1-0185</span>
                            <span class="candidate-step candidate-step--waiting">Hạng A1</span>
                        </div>
                        <div class="candidate-name">Nguyễn Hoàng Nam</div>
                        <div style="font-size: 0.72rem; color: #64748b; display: flex; justify-content: space-between; align-items: center; margin-top: 2px;">
                            <span>Trạng thái: Đang chờ</span>
                            <span style="font-weight: 600; color: #475569;">Thời gian chờ: 5p</span>
                        </div>
                    </div>
                </div>
                
                <a href="candidatecall.jsp" style="text-decoration: none; text-align: center; font-size: 0.8rem; font-weight: 700; color: #0052cc; padding: 6px; border: 1px dashed rgba(0, 82, 204, 0.4); border-radius: 8px; background: rgba(0, 82, 204, 0.02); transition: all 0.2s;" class="hover-elevate">
                    Xem phòng điều hành gọi thi &rarr;
                </a>
            </div>
            
            <!-- Column 2: Procedure Room (Phòng Làm Thủ Tục) -->
            <div class="room-monitor-card">
                <div class="room-header">
                    <h3 class="room-title">
                        <svg width="20" height="20" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg" style="color: #2563eb;">
                            <rect x="3" y="4" width="18" height="12" rx="2" stroke="currentColor" stroke-width="2"/>
                            <path d="M12 20h.01M16 20h.01M8 20h.01M12 16v4" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
                        </svg>
                        Bàn thủ tục khép kín
                    </h3>
                    <span class="room-badge room-badge--blue">Đang xử lý</span>
                </div>
                
                <div class="room-candidate-list">
                    <!-- Simulate Desks Process -->
                    <div class="room-candidate-item" style="border-left: 3px solid #2563eb;">
                        <div class="candidate-meta">
                            <span style="font-weight: 700; color: #1e293b;">BÀN SỐ 01</span>
                            <span class="candidate-step candidate-step--verify">Bước 1: Xác minh</span>
                        </div>
                        <div style="display: flex; justify-content: space-between; align-items: center; margin-top: 4px;">
                            <span class="candidate-name">Nguyễn Anh Tuấn</span>
                            <span class="candidate-sbd">SBD: A1-0024</span>
                        </div>
                        <div style="font-size: 0.72rem; color: #64748b; margin-top: 2px;">
                            Đang đối chiếu CCCD và sửa đổi thông tin GPLX cũ.
                        </div>
                    </div>
                    
                    <div class="room-candidate-item" style="border-left: 3px solid #7e22ce;">
                        <div class="candidate-meta">
                            <span style="font-weight: 700; color: #1e293b;">BÀN SỐ 02</span>
                            <span class="candidate-step candidate-step--photo">Bước 2: Chụp ảnh</span>
                        </div>
                        <div style="display: flex; justify-content: space-between; align-items: center; margin-top: 4px;">
                            <span class="candidate-name">Trần Thị Mai</span>
                            <span class="candidate-sbd">SBD: B2-0145</span>
                        </div>
                        <div style="font-size: 0.72rem; color: #64748b; margin-top: 2px;">
                            Đang điều chỉnh camera và chụp ảnh chân dung trực tiếp.
                        </div>
                    </div>
                    
                    <div class="room-candidate-item" style="border-left: 3px solid #b45309;">
                        <div class="candidate-meta">
                            <span style="font-weight: 700; color: #1e293b;">BÀN SỐ 03</span>
                            <span class="candidate-step candidate-step--payment">Bước 3: Lệ phí</span>
                        </div>
                        <div style="display: flex; justify-content: space-between; align-items: center; margin-top: 4px;">
                            <span class="candidate-name">Vũ Huy Hoàng</span>
                            <span class="candidate-sbd">SBD: B2-0112</span>
                        </div>
                        <div style="font-size: 0.72rem; color: #64748b; margin-top: 2px;">
                            Học viên đang quét mã QR chuyển khoản lệ phí thi sát hạch.
                        </div>
                    </div>
                </div>
                
                <a href="procedure.jsp" style="text-decoration: none; text-align: center; font-size: 0.8rem; font-weight: 700; color: #0052cc; padding: 6px; border: 1px dashed rgba(0, 82, 204, 0.4); border-radius: 8px; background: rgba(0, 82, 204, 0.02); transition: all 0.2s;" class="hover-elevate">
                    Vào quầy làm thủ tục &rarr;
                </a>
            </div>
            
            <!-- Column 3: Test Field (Sân Sát Hạch) -->
            <div class="room-monitor-card">
                <div class="room-header">
                    <h3 class="room-title">
                        <svg width="20" height="20" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg" style="color: #10b981;">
                            <path d="M12 22c5.523 0 10-4.477 10-10S17.523 2 12 2 2 6.477 2 12s4.477 10 10 10z" stroke="currentColor" stroke-width="2"/>
                            <path d="M12 6v6l4 2" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                        </svg>
                        Sân sát hạch thực hành
                    </h3>
                    <span class="room-badge room-badge--green">Đang thi</span>
                </div>
                
                <div class="room-candidate-list">
                    <!-- Simulate Candidates on Field -->
                    <div class="room-candidate-item">
                        <div class="candidate-meta">
                            <span class="candidate-sbd" style="color: #10b981;">Xe số 05 (A1)</span>
                            <span class="candidate-step candidate-step--ready">Đang thực hiện</span>
                        </div>
                        <div class="candidate-name">Phan Thanh Tùng</div>
                        <div style="font-size: 0.72rem; color: #64748b; display: flex; justify-content: space-between; align-items: center; margin-top: 2px;">
                            <span>Bài: Đường vòng số 8</span>
                            <span style="font-weight: 800; color: #10b981;">95 điểm</span>
                        </div>
                    </div>
                    
                    <div class="room-candidate-item">
                        <div class="candidate-meta">
                            <span class="candidate-sbd" style="color: #10b981;">Xe số 08 (B2)</span>
                            <span class="candidate-step candidate-step--ready">Đang thực hiện</span>
                        </div>
                        <div class="candidate-name">Nguyễn Thị Thu Hà</div>
                        <div style="font-size: 0.72rem; color: #64748b; display: flex; justify-content: space-between; align-items: center; margin-top: 2px;">
                            <span>Bài: Dừng & khởi hành ngang dốc</span>
                            <span style="font-weight: 800; color: #10b981;">100 điểm</span>
                        </div>
                    </div>
                    
                    <div class="room-candidate-item">
                        <div class="candidate-meta">
                            <span class="candidate-sbd" style="color: #10b981;">Xe số 12 (B2)</span>
                            <span class="candidate-step candidate-step--ready">Đang thực hiện</span>
                        </div>
                        <div class="candidate-name">Trần Đình Trọng</div>
                        <div style="font-size: 0.72rem; color: #64748b; display: flex; justify-content: space-between; align-items: center; margin-top: 2px;">
                            <span>Bài: Ghép xe dọc vào nơi đỗ</span>
                            <span style="font-weight: 800; color: #b45309;">90 điểm</span>
                        </div>
                    </div>

                    <div class="room-candidate-item">
                        <div class="candidate-meta">
                            <span class="candidate-sbd" style="color: #10b981;">Xe số 02 (A1)</span>
                            <span class="candidate-step candidate-step--ready">Đang thực hiện</span>
                        </div>
                        <div class="candidate-name">Bùi Tiến Dũng</div>
                        <div style="font-size: 0.72rem; color: #64748b; display: flex; justify-content: space-between; align-items: center; margin-top: 2px;">
                            <span>Bài: Đường gồ ghề</span>
                            <span style="font-weight: 800; color: #10b981;">100 điểm</span>
                        </div>
                    </div>
                </div>
                
                <a href="report.jsp" style="text-decoration: none; text-align: center; font-size: 0.8rem; font-weight: 700; color: #0052cc; padding: 6px; border: 1px dashed rgba(0, 82, 204, 0.4); border-radius: 8px; background: rgba(0, 82, 204, 0.02); transition: all 0.2s;" class="hover-elevate">
                    Xem thống kê kết quả thi sát hạch &rarr;
                </a>
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

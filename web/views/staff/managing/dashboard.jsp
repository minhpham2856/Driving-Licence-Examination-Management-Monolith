<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib prefix = "fn" uri = "http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix = "fmt" uri = "http://java.sun.com/jsp/jstl/fmt" %>

<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Dashboard quản lý - Lái Vui</title>
    <link rel="preconnect" href="https://fonts.googleapis.com">
    <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
    <link href="https://fonts.googleapis.com/css2?family=Be+Vietnam+Pro:wght@400;500;600;700;800&family=Inter:wght@400;500;600;700;800&display=swap" rel="stylesheet">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/style.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/layout.css">
</head>
<body class="has-side-nav-bar">

<jsp:include page="/views/layout/sidebar-managingstaff.jsp">
    <jsp:param name="activeSidebar" value="dashboard" />
</jsp:include>

<div class="dashboard-shell">
    <main class="main-content">
        
        <nav class="breadcrumbs">
            <a href="${pageContext.request.contextPath}/views/public/home.jsp">Trang chủ</a>
            <span class="breadcrumbs__separator">/</span>
            <span class="breadcrumbs__current">Dashboard quản lý</span>
        </nav>
        
        <header class="page-header">
            <div class="page-title-wrap">
                <h1 class="page-title">Dashboard Quản Lý</h1>
                <p class="page-subtitle">Tổng quan thông số học viên, hồ sơ giấy tờ và tiến độ duyệt khóa học.</p>
            </div>
            
            <div class="page-actions" style="display: flex; gap: 10px;">
                <a href="${pageContext.request.contextPath}/manager/create-user" class="btn-filter" style="height: 42px; padding: 0 1.25rem; font-size: 0.9rem; border-radius: 8px; text-decoration: none; display: inline-flex; align-items: center; gap: 6px; background-color: #0052cc; border-color: #0052cc;">
                    <svg width="16" height="16" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                        <path d="M16 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
                        <circle cx="8" cy="7" r="4" stroke="currentColor" stroke-width="2"/>
                        <path d="M20 8v6M17 11h6" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                    </svg>
                    Thêm học viên mới
                </a>
                
                <a href="${pageContext.request.contextPath}/views/staff/managing/approve.jsp" class="btn-export" style="height: 42px; padding: 0 1.25rem; font-size: 0.9rem; border-radius: 8px; text-decoration: none; display: inline-flex; align-items: center; gap: 6px; background-color: #ffffff; color: #475569;">
                    <svg width="16" height="16" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                        <path d="M9 11L12 14L22 4" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                        <path d="M21 12v7a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h11" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                    </svg>
                    Duyệt hồ sơ (${empty pendingApprovalsCount ? 0 : pendingApprovalsCount})
                </a>
            </div>
        </header>

        <section class="metrics-row">
            <div class="stat-card">
                <div class="stat-icon stat-icon--blue">
                    <svg width="24" height="24" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                        <path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
                        <circle cx="9" cy="7" r="4" stroke="currentColor" stroke-width="2"/>
                        <path d="M23 21v-2a4 4 0 0 0-3-3.87M16 3.13a4 4 0 0 1 0 7.75" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
                    </svg>
                </div>
                <div class="stat-info">
                    <span class="stat-number">${empty totalStudents ? 0 : totalStudents}</span>
                    <span class="stat-label">Học viên hệ thống</span>
                    <span class="stat-trend stat-trend--up">
                        Chỉ số đào tạo
                    </span>
                </div>
            </div>

            <div class="stat-card">
                <div class="stat-icon stat-icon--amber">
                    <svg width="24" height="24" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                        <path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z" stroke="currentColor" stroke-width="2" stroke-linejoin="round"/>
                        <path d="M14 2v6h6M16 13H8M16 17H8M10 9H8" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                    </svg>
                </div>
                <div class="stat-info">
                    <span class="stat-number" style="color: #d97706;">${empty pendingApprovalsCount ? 0 : pendingApprovalsCount}</span>
                    <span class="stat-label">Hồ sơ chờ duyệt</span>
                    <span class="stat-trend stat-trend--down" style="color: #d97706; background-color: rgba(245, 158, 11, 0.1);">
                        Cần thẩm định
                    </span>
                </div>
            </div>

            <div class="stat-card">
                <div class="stat-icon stat-icon--purple">
                    <svg width="24" height="24" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                        <rect x="3" y="4" width="18" height="18" rx="2" stroke="currentColor" stroke-width="2" stroke-linejoin="round"/>
                        <path d="M16 2v4M8 2v4M3 10h18" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                    </svg>
                </div>
                <div class="stat-info">
                    <span class="stat-number" style="color: #7c3aed;">${empty upcomingExamsCount ? 0 : upcomingExamsCount}</span>
                    <span class="stat-label">Kỳ thi sắp tới</span>
                    <span class="stat-trend stat-trend--up" style="color: #7c3aed; background-color: rgba(139, 92, 246, 0.1);">
                        Lịch thi sát hạch
                    </span>
                </div>
            </div>

            <div class="stat-card">
                <div class="stat-icon stat-icon--green">
                    <svg width="24" height="24" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                        <circle cx="12" cy="12" r="10" stroke="currentColor" stroke-width="2"/>
                        <path d="M22 12A10 10 0 0 1 12 22M2 12A10 10 0 0 1 12 2M2 12A10 10 0 0 1 12 2" stroke="currentColor" stroke-width="2" stroke-dasharray="4 4"/>
                        <path d="M9 12l2 2 4-4" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                    </svg>
                </div>
                <div class="stat-info">
                    <span class="stat-number" style="color: #10b981;">${empty activeStudentsCount ? 0 : activeStudentsCount}</span>
                    <span class="stat-label">Hồ sơ đã duyệt</span>
                    <span class="stat-trend stat-trend--up" style="color: #10b981; background-color: rgba(16, 185, 129, 0.1);">
                        Hồ sơ hợp lệ
                    </span>
                </div>
            </div>
        </section>

        <div class="report-grid" style="grid-template-columns: 1.6fr 1fr; gap: 1.5rem; margin-top: 1.5rem;">
            
            <div style="display: flex; flex-direction: column; gap: 1.5rem;">
                
                <div class="report-pane" style="padding: 1.5rem;">
                    <div class="grading-pane__header" style="border-bottom: none; margin-bottom: 1rem; padding-bottom: 0;">
                        <h2 class="grading-pane__title" style="font-size: 1.1rem; display: flex; align-items: center; gap: 8px;">
                            <svg width="20" height="20" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg" style="color: #0052cc;">
                                <rect x="3" y="4" width="18" height="18" rx="2" stroke="currentColor" stroke-width="2" stroke-linejoin="round"/>
                                <path d="M16 2v4M8 2v4M3 10h18" stroke="currentColor" stroke-width="2"/>
                            </svg>
                            Các kỳ thi sát hạch sắp tới
                        </h2>
                    </div>
                    
                    <div class="table-responsive">
                        <table class="audit-table" style="font-size: 0.88rem;">
                            <thead>
                                <tr>
                                    <th scope="col" style="width: 140px;">Tên đợt thi</th>
                                    <th scope="col" style="width: 100px; text-align: center;">Hạng xe</th>
                                    <th scope="col">Ngày thi</th>
                                    <th scope="col" style="text-align: center;">Hồ sơ tham dự</th>
                                    <th scope="col" style="width: 130px; text-align: center;">Trạng thái</th>
                                </tr>
                            </thead>
                            <tbody>
                                <c:choose>
                                    <c:when test="${not empty upcomingExams}">
                                        <c:forEach var="exam" items="${upcomingExams}">
                                            <tr>
                                                <td style="font-weight: 700; color: #0052cc;">${exam.examName}</td>
                                                <td style="text-align: center;">
                                                    <span class="role-badge role-badge--admin" style="padding: 2px 8px; font-size: 0.75rem;">Hạng ${exam.licenseClass}</span>
                                                </td>
                                                <td style="font-weight: 500; color: #475569;">${exam.examDate}</td>
                                                <td style="text-align: center; font-weight: 700; color: #0f172a;">${exam.registeredCount} học viên</td>
                                                <td style="text-align: center;">
                                                    <span class="action-badge action-badge--${exam.statusClass}" style="font-size: 0.75rem;">${exam.status}</span>
                                                </td>
                                            </tr>
                                        </c:forEach>
                                    </c:when>
                                    <c:otherwise>
                                        <tr>
                                            <td colspan="5" style="text-align: center; padding: 2rem 1rem; color: #64748b;">
                                                Không có kỳ thi sát hạch sắp tới.
                                            </td>
                                        </tr>
                                    </c:otherwise>
                                </c:choose>
                            </tbody>
                        </table>
                    </div>
                </div>

                <div class="report-pane" style="padding: 1.5rem;">
                    <div class="grading-pane__header" style="border-bottom: none; margin-bottom: 1rem; padding-bottom: 0; display: flex; justify-content: space-between; align-items: center;">
                        <h2 class="grading-pane__title" style="font-size: 1.1rem; display: flex; align-items: center; gap: 8px;">
                            <svg width="20" height="20" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg" style="color: #ea580c;">
                                <path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z" stroke="currentColor" stroke-width="2" stroke-linejoin="round"/>
                                <path d="M14 2v6h6" stroke="currentColor" stroke-width="2" stroke-linejoin="round"/>
                                <path d="M12 18v-6M9 15h6" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                            </svg>
                            Hồ sơ mới gửi chờ duyệt
                        </h2>
                        
                        <a href="${pageContext.request.contextPath}/views/staff/managing/approve.jsp" style="font-size: 0.8rem; font-weight: 700; color: #0052cc; text-decoration: none; display: inline-flex; align-items: center; gap: 4px;">
                            Xem tất cả hồ sơ chờ duyệt
                            <svg width="12" height="12" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                                <path d="M5 12h14M12 5l7 7-7 7" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                            </svg>
                        </a>
                    </div>

                    <div class="table-responsive">
                        <table class="audit-table" style="font-size: 0.88rem;">
                            <thead>
                                <tr>
                                    <th scope="col">Họ và tên</th>
                                    <th scope="col" style="width: 130px;">Số CCCD</th>
                                    <th scope="col" style="width: 90px; text-align: center;">Hạng đăng ký</th>
                                    <th scope="col" style="width: 120px;">Ngày nộp hồ sơ</th>
                                    <th scope="col" style="width: 100px; text-align: center;">Thao tác</th>
                                </tr>
                            </thead>
                            <tbody>
                                <c:choose>
                                    <c:when test="${not empty recentSubmissions}">
                                        <c:forEach var="sub" items="${recentSubmissions}">
                                            <tr>
                                                <td style="font-weight: 600; color: #0f172a;">${sub.fullName}</td>
                                                <td style="font-family: monospace;">${sub.cccd}</td>
                                                <td style="text-align: center;">
                                                    <span class="role-badge role-badge--coi">Hạng ${sub.licenseClass}</span>
                                                </td>
                                                <td style="color: #64748b;">${sub.submitDate}</td>
                                                <td style="text-align: center;">
                                                    <a href="${pageContext.request.contextPath}/views/staff/managing/approve.jsp?id=${sub.id}" class="btn-export" style="padding: 4px 10px; font-size: 0.78rem; border-radius: 6px; border-color: rgba(217, 119, 6, 0.25); color: #d97706; font-weight: 700; text-decoration: none;">Xem & Duyệt</a>
                                                </td>
                                            </tr>
                                        </c:forEach>
                                    </c:when>
                                    <c:otherwise>
                                        <tr>
                                            <td colspan="5" style="text-align: center; padding: 2rem 1rem; color: #64748b;">
                                                Không có hồ sơ mới gửi chờ duyệt.
                                            </td>
                                        </tr>
                                    </c:otherwise>
                                </c:choose>
                            </tbody>
                        </table>
                    </div>
                </div>

            </div>

            <div style="display: flex; flex-direction: column; gap: 1.5rem;">
                
                <div class="report-pane" style="padding: 1.5rem;">
                    <div class="grading-pane__header" style="border-bottom: none; margin-bottom: 1.25rem; padding-bottom: 0;">
                        <h2 class="grading-pane__title" style="font-size: 1.1rem; display: flex; align-items: center; gap: 8px;">
                            <svg width="20" height="20" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg" style="color: #0052cc;">
                                <circle cx="12" cy="12" r="10" stroke="currentColor" stroke-width="2"/>
                                <path d="M12 6v6l4 2" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
                            </svg>
                            Phân loại học viên theo hạng xe
                        </h2>
                    </div>

                    <div style="display: flex; flex-direction: column; gap: 1.25rem;">
                        
                        <div>
                            <div style="display: flex; justify-content: space-between; font-size: 0.85rem; margin-bottom: 4px;">
                                <span style="font-weight: 700; color: #0d9488;">Hạng A1 (Mô tô &lt; 175cc)</span>
                                <span style="font-weight: 700; color: #0f172a;">${empty classA1Count ? 0 : classA1Count} học viên</span>
                            </div>
                            <div class="progress-bar-container" style="height: 8px; background-color: #f1f5f9; border-radius: 999px; overflow: hidden; width: 100%;">
                                <div class="progress-bar-fill" style="width: ${empty classA1Percent ? 0 : classA1Percent}%; background: #0d9488; height: 100%; border-radius: 999px;"></div>
                            </div>
                        </div>

                        <div>
                            <div style="display: flex; justify-content: space-between; font-size: 0.85rem; margin-bottom: 4px;">
                                <span style="font-weight: 700; color: #8b5cf6;">Hạng A2 (Mô tô &gt;= 175cc)</span>
                                <span style="font-weight: 700; color: #0f172a;">${empty classA2Count ? 0 : classA2Count} học viên</span>
                            </div>
                            <div class="progress-bar-container" style="height: 8px; background-color: #f1f5f9; border-radius: 999px; overflow: hidden; width: 100%;">
                                <div class="progress-bar-fill" style="width: ${empty classA2Percent ? 0 : classA2Percent}%; background: #8b5cf6; height: 100%; border-radius: 999px;"></div>
                            </div>
                        </div>

                        <div>
                            <div style="display: flex; justify-content: space-between; font-size: 0.85rem; margin-bottom: 4px;">
                                <span style="font-weight: 700; color: #0052cc;">Hạng B1 (Ô tô tự động)</span>
                                <span style="font-weight: 700; color: #0f172a;">${empty classB1Count ? 0 : classB1Count} học viên</span>
                            </div>
                            <div class="progress-bar-container" style="height: 8px; background-color: #f1f5f9; border-radius: 999px; overflow: hidden; width: 100%;">
                                <div class="progress-bar-fill" style="width: ${empty classB1Percent ? 0 : classB1Percent}%; background: #0052cc; height: 100%; border-radius: 999px;"></div>
                            </div>
                        </div>

                        <div>
                            <div style="display: flex; justify-content: space-between; font-size: 0.85rem; margin-bottom: 4px;">
                                <span style="font-weight: 700; color: #f59e0b;">Hạng B2 (Ô tô số sàn)</span>
                                <span style="font-weight: 700; color: #0f172a;">${empty classB2Count ? 0 : classB2Count} học viên</span>
                            </div>
                            <div class="progress-bar-container" style="height: 8px; background-color: #f1f5f9; border-radius: 999px; overflow: hidden; width: 100%;">
                                <div class="progress-bar-fill" style="width: ${empty classB2Percent ? 0 : classB2Percent}%; background: #f59e0b; height: 100%; border-radius: 999px;"></div>
                            </div>
                        </div>

                        <div>
                            <div style="display: flex; justify-content: space-between; font-size: 0.85rem; margin-bottom: 4px;">
                                <span style="font-weight: 700; color: #ef4444;">Hạng C (Ô tô tải lớn)</span>
                                <span style="font-weight: 700; color: #0f172a;">${empty classCCount ? 0 : classCCount} học viên</span>
                            </div>
                            <div class="progress-bar-container" style="height: 8px; background-color: #f1f5f9; border-radius: 999px; overflow: hidden; width: 100%;">
                                <div class="progress-bar-fill" style="width: ${empty classCPercent ? 0 : classCPercent}%; background: #ef4444; height: 100%; border-radius: 999px;"></div>
                            </div>
                        </div>

                    </div>
                </div>

                <div class="report-pane" style="padding: 1.5rem;">
                    <div class="grading-pane__header" style="border-bottom: none; margin-bottom: 1.25rem; padding-bottom: 0;">
                        <h2 class="grading-pane__title" style="font-size: 1.1rem; display: flex; align-items: center; gap: 8px;">
                            <svg width="20" height="20" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg" style="color: #0052cc;">
                                <path d="M12 2L2 22h20L12 2z" stroke="currentColor" stroke-width="2" stroke-linejoin="round"/>
                            </svg>
                            Các lối tắt chức năng nhanh
                        </h2>
                    </div>

                    <div style="display: grid; grid-template-columns: 1fr 1fr; gap: 0.75rem;">
                        <a href="${pageContext.request.contextPath}/views/staff/managing/users.jsp" class="btn-export" style="text-decoration: none; padding: 1rem; border-radius: 8px; display: flex; flex-direction: column; align-items: center; text-align: center; gap: 6px; box-sizing: border-box; width: 100%;">
                            <svg width="24" height="24" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg" style="color: #0052cc;">
                                <circle cx="9" cy="7" r="4" stroke="currentColor" stroke-width="1.8"/>
                                <path d="M3 21v-2a7 7 0 0 1 14 0v2M19 8v6M16 11h6" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round"/>
                            </svg>
                            <span style="font-size: 0.8rem; font-weight: 700; color: #334155;">Học viên mới</span>
                        </a>

                        <a href="${pageContext.request.contextPath}/views/staff/managing/approve.jsp" class="btn-export" style="text-decoration: none; padding: 1rem; border-radius: 8px; display: flex; flex-direction: column; align-items: center; text-align: center; gap: 6px; box-sizing: border-box; width: 100%;">
                            <svg width="24" height="24" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg" style="color: #d97706;">
                                <path d="M9 11L12 14L22 4" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round"/>
                                <path d="M21 12v7a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h11" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round"/>
                            </svg>
                            <span style="font-size: 0.8rem; font-weight: 700; color: #334155;">Duyệt hồ sơ</span>
                        </a>

                        <a href="${pageContext.request.contextPath}/views/staff/managing/users.jsp?filterStatus=active" class="btn-export" style="text-decoration: none; padding: 1rem; border-radius: 8px; display: flex; flex-direction: column; align-items: center; text-align: center; gap: 6px; box-sizing: border-box; width: 100%;">
                            <svg width="24" height="24" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg" style="color: #10b981;">
                                <path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2" stroke="currentColor" stroke-width="1.8" stroke-linecap="round"/>
                                <circle cx="9" cy="7" r="4" stroke="currentColor" stroke-width="1.8"/>
                            </svg>
                            <span style="font-size: 0.8rem; font-weight: 700; color: #334155;">Xem học viên</span>
                        </a>

                        <a href="${pageContext.request.contextPath}/manager/create-user" class="btn-export" style="text-decoration: none; padding: 1rem; border-radius: 8px; display: flex; flex-direction: column; align-items: center; text-align: center; gap: 6px; box-sizing: border-box; width: 100%;">
                            <svg width="24" height="24" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg" style="color: #7c3aed;">
                                <path d="M16 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2" stroke="currentColor" stroke-width="1.8" stroke-linecap="round"/>
                                <circle cx="8" cy="7" r="4" stroke="currentColor" stroke-width="1.8"/>
                                <path d="M20 8v6M17 11h6" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round"/>
                            </svg>
                            <span style="font-size: 0.8rem; font-weight: 700; color: #334155;">Thêm tài khoản</span>
                        </a>
                    </div>
                </div>

            </div>

        </div>

    </main>
</div>

</body>
</html>

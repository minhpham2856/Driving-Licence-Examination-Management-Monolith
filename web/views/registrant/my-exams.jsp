<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>

<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Lịch Thi & Kết Quả Sát Hạch - Lái Vui</title>
    <meta name="description" content="Xem lịch thi sắp tới, thẻ dự thi, số báo danh và tra cứu kết quả thi lý thuyết, thực hành tại Lái Vui.">

    <!-- Google Fonts -->
    <link rel="preconnect" href="https://fonts.googleapis.com">
    <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
    <link href="https://fonts.googleapis.com/css2?family=Be+Vietnam+Pro:wght@400;500;600;700;800&family=Inter:wght@400;500;600;700;800&display=swap" rel="stylesheet">

    <!-- Layout Stylesheets -->
    <jsp:include page="/views/registrant/components/registrant-styles.jsp" />
</head>
<body class="has-side-nav-bar">

<%-- Inject registrant sidebar --%>
<jsp:include page="/views/registrant/components/sidebar.jsp">
    <jsp:param name="activeSidebar" value="exam-schedule" />
</jsp:include>

<div class="dashboard-shell">
    <main class="main-content" id="main-content">

        <%-- Breadcrumbs --%>
        <nav class="breadcrumbs" aria-label="Breadcrumb">
            <a href="${pageContext.request.contextPath}/views/registrant/dashboard.jsp">Trang chủ</a>
            <span class="breadcrumbs__separator" aria-hidden="true">/</span>
            <span class="breadcrumbs__current" aria-current="page">Lịch thi & kết quả</span>
        </nav>

        <%-- Page Header --%>
        <header class="page-header">
            <div class="page-title-wrap">
                <h1 class="page-title">Lịch thi & KQ quả</h1>
                <p class="page-subtitle">Quản lý các kỳ thi đã đăng ký và theo dõi điểm số chi tiết của bạn.</p>
            </div>
            <div class="page-header-actions">
                <a href="#" class="btn-header-outline" id="btn-download-cert">
                    <svg width="16" height="16" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                        <path d="M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4M7 10l5 5 5-5M12 15V3" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                    </svg>
                    Tải chứng nhận
                </a>
                <a href="${pageContext.request.contextPath}/views/registrant/register-exam.jsp" class="btn-header-primary" id="btn-register-new-exam">
                    <svg width="16" height="16" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                        <line x1="12" y1="5" x2="12" y2="19" stroke="currentColor" stroke-width="2.5" stroke-linecap="round"/>
                        <line x1="5" y1="12" x2="19" y2="12" stroke="currentColor" stroke-width="2.5" stroke-linecap="round"/>
                    </svg>
                    Đăng ký thi mới
                </a>
            </div>
        </header>

        <%-- =========================================================
             3 Stat Cards Row
             ========================================================= --%>
        <section class="my-exams-stats-row" aria-label="Thống kê kỳ thi">
            
            <%-- Stat 1: Tổng số kỳ thi --%>
            <div class="my-exams-stat-card">
                <div class="my-exams-stat-card__icon-wrap my-exams-stat-card__icon-wrap--blue" aria-hidden="true">
                    <svg width="22" height="22" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                        <path d="M12 2L2 7l10 5 10-5-10-5z" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                        <path d="M2 17l10 5 10-5M2 12l10 5 10-5" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                    </svg>
                </div>
                <div class="my-exams-stat-card__details">
                    <span class="my-exams-stat-card__label">Tổng số kỳ thi</span>
                    <span class="my-exams-stat-card__value">03</span>
                </div>
            </div>

            <%-- Stat 2: Số kỳ thi đạt --%>
            <div class="my-exams-stat-card">
                <div class="my-exams-stat-card__icon-wrap my-exams-stat-card__icon-wrap--green" aria-hidden="true">
                    <svg width="22" height="22" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                        <circle cx="12" cy="12" r="10" stroke="currentColor" stroke-width="2"/>
                        <path d="M8 12l3 3 5-5" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                    </svg>
                </div>
                <div class="my-exams-stat-card__details">
                    <span class="my-exams-stat-card__label">Số kỳ thi đạt</span>
                    <span class="my-exams-stat-card__value">02</span>
                </div>
            </div>

            <%-- Stat 3: Sắp diễn ra --%>
            <div class="my-exams-stat-card">
                <div class="my-exams-stat-card__icon-wrap my-exams-stat-card__icon-wrap--orange" aria-hidden="true">
                    <svg width="22" height="22" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                        <rect x="3" y="4" width="18" height="16" rx="2" stroke="currentColor" stroke-width="2"/>
                        <path d="M16 2v4M8 2v4M3 10h18" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
                    </svg>
                </div>
                <div class="my-exams-stat-card__details">
                    <span class="my-exams-stat-card__label">Sắp diễn ra</span>
                    <span class="my-exams-stat-card__value">01</span>
                </div>
            </div>

        </section>

        <c:set var="selectedExamId" value="${not empty param.examId ? param.examId : '240892'}" />

        <%-- =========================================================
             Exams Table List Panel
             ========================================================= --%>
        <section class="my-exams-panel" id="exams-table-anchor" aria-label="Danh sách kỳ thi của tôi">
            <table class="my-exams-table" role="table">
                <thead>
                    <tr>
                        <th scope="col">Kỳ thi</th>
                        <th scope="col">Hạng bằng</th>
                        <th scope="col">SBD</th>
                        <th scope="col">Phòng thi</th>
                        <th scope="col">Trạng thái</th>
                        <th scope="col">Thao tác</th>
                    </tr>
                </thead>
                <tbody>
                    
                    <%-- Row 1: Sát hạch Khóa 12/2023 --%>
                    <tr class="${selectedExamId == '240892' ? 'my-exams-table__row--selected' : ''}">
                        <td>
                            <span class="my-exams-table__title">Sát hạch Khóa 12/2023</span>
                            <span class="my-exams-table__subtitle">25/12/2023</span>
                        </td>
                        <td>
                            <span class="my-exams-table__licence-badge">B2</span>
                        </td>
                        <td>
                            <span class="my-exams-table__sbd">240892</span>
                        </td>
                        <td>Phòng 402 - Tầng 4</td>
                        <td>
                            <span class="status-badge status-badge--approved">Đạt</span>
                        </td>
                        <td>
                            <c:choose>
                                <c:when test="${selectedExamId == '240892'}">
                                    <a href="?#exams-table-anchor" class="my-exams-table__action-btn my-exams-table__action-btn--active" aria-label="Đóng chi tiết kỳ thi Khóa 12/2023">
                                        <svg width="16" height="16" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                                            <path d="M18 15l-6-6-6 6" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                                        </svg>
                                    </a>
                                </c:when>
                                <c:otherwise>
                                    <a href="?examId=240892#exam-details" class="my-exams-table__action-btn" aria-label="Xem chi tiết kỳ thi Khóa 12/2023">
                                        <svg width="16" height="16" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                                            <path d="M6 9l6 6 6-6" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                                        </svg>
                                    </a>
                                </c:otherwise>
                            </c:choose>
                        </td>
                    </tr>

                    <%-- Row 2: Thi thử tháng 11 --%>
                    <tr class="${selectedExamId == '240112' ? 'my-exams-table__row--selected' : ''}">
                        <td>
                            <span class="my-exams-table__title">Thi thử tháng 11</span>
                            <span class="my-exams-table__subtitle">15/11/2023</span>
                        </td>
                        <td>
                            <span class="my-exams-table__licence-badge">B2</span>
                        </td>
                        <td>
                            <span class="my-exams-table__sbd">240112</span>
                        </td>
                        <td>Sân tập A - Nhà A1</td>
                        <td>
                            <span class="status-badge status-badge--rejected">Không đạt</span>
                        </td>
                        <td>
                            <c:choose>
                                <c:when test="${selectedExamId == '240112'}">
                                    <a href="?#exams-table-anchor" class="my-exams-table__action-btn my-exams-table__action-btn--active" aria-label="Đóng chi tiết thi thử tháng 11">
                                        <svg width="16" height="16" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                                            <path d="M18 15l-6-6-6 6" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                                        </svg>
                                    </a>
                                </c:when>
                                <c:otherwise>
                                    <a href="?examId=240112#exam-details" class="my-exams-table__action-btn" aria-label="Xem chi tiết thi thử tháng 11">
                                        <svg width="16" height="16" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                                            <path d="M6 9l6 6 6-6" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                                        </svg>
                                    </a>
                                </c:otherwise>
                            </c:choose>
                        </td>
                    </tr>

                    <%-- Row 3: Kiểm tra định kỳ đợt 2 --%>
                    <tr class="${selectedExamId == '240045' ? 'my-exams-table__row--selected' : ''}">
                        <td>
                            <span class="my-exams-table__title">Kiểm tra định kỳ đợt 2</span>
                            <span class="my-exams-table__subtitle">02/11/2023</span>
                        </td>
                        <td>
                            <span class="my-exams-table__licence-badge">B2</span>
                        </td>
                        <td>
                            <span class="my-exams-table__sbd">240045</span>
                        </td>
                        <td>Hội trường B</td>
                        <td>
                            <span class="status-badge status-badge--gray">Vắng thi</span>
                        </td>
                        <td>
                            <c:choose>
                                <c:when test="${selectedExamId == '240045'}">
                                    <a href="?#exams-table-anchor" class="my-exams-table__action-btn my-exams-table__action-btn--active" aria-label="Đóng chi tiết kiểm tra định kỳ đợt 2">
                                        <svg width="16" height="16" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                                            <path d="M18 15l-6-6-6 6" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                                        </svg>
                                    </a>
                                </c:when>
                                <c:otherwise>
                                    <a href="?examId=240045#exam-details" class="my-exams-table__action-btn" aria-label="Xem chi tiết kiểm tra định kỳ đợt 2">
                                        <svg width="16" height="16" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                                            <path d="M6 9l6 6 6-6" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                                        </svg>
                                    </a>
                                </c:otherwise>
                            </c:choose>
                        </td>
                    </tr>

                    <%-- Row 4: Tốt nghiệp K12 (Dự kiến) --%>
                    <tr class="my-exams-table__row--pending ${selectedExamId == 'TNP-B2-0224' ? 'my-exams-table__row--selected' : ''}">
                        <td>
                            <span class="my-exams-table__title">Tốt nghiệp K12 (Dự kiến)</span>
                            <span class="my-exams-table__subtitle">15/02/2024</span>
                        </td>
                        <td>
                            <span class="my-exams-table__licence-badge my-exams-table__licence-badge--orange">B2</span>
                        </td>
                        <td>
                            <span class="my-exams-table__sbd">--</span>
                        </td>
                        <td>Đang cập nhật</td>
                        <td>
                            <span class="status-badge status-badge--pending">Chờ thi</span>
                        </td>
                        <td>
                            <c:choose>
                                <c:when test="${selectedExamId == 'TNP-B2-0224'}">
                                    <a href="?#exams-table-anchor" class="my-exams-table__action-btn my-exams-table__action-btn--active" aria-label="Đóng chi tiết kỳ thi Tốt nghiệp K12">
                                        <svg width="16" height="16" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                                            <path d="M18 15l-6-6-6 6" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                                        </svg>
                                    </a>
                                </c:when>
                                <c:otherwise>
                                    <a href="?examId=TNP-B2-0224#exam-details" class="my-exams-table__action-btn" aria-label="Xem chi tiết kỳ thi Tốt nghiệp K12">
                                        <svg width="16" height="16" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                                            <path d="M18 8A6 6 0 0 0 6 8c0 7-3 9-3 9h18s-3-2-3-9M13.73 21a2 2 0 0 1-3.46 0" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                                        </svg>
                                    </a>
                                </c:otherwise>
                            </c:choose>
                        </td>
                    </tr>

                </tbody>
            </table>
        </section>

        <%-- =========================================================
             Selected Exam Detail Section (SC-629-1368 & SC-629-978 Detail)
             ========================================================= --%>
        <section id="exam-details" class="exam-details-section" aria-label="Chi tiết kỳ thi sát hạch">
            <h2 class="section-title-premium">
                <svg width="20" height="20" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg" class="section-title-premium__icon" aria-hidden="true">
                    <circle cx="12" cy="12" r="10" stroke="currentColor" stroke-width="2"/>
                    <path d="M12 16v-4M12 8h.01" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                </svg>
                Chi tiết kỳ thi & kết quả sát hạch
            </h2>
            
            <div class="exam-details-grid">
                
                <%-- ==========================================
                     LEFT COLUMN: Chi tiết đợt thi & phòng thi
                     ========================================== --%>
                <div class="exam-details-card exam-details-card--left">
                    <div class="exam-details-card__header">
                        <svg width="20" height="20" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg" class="exam-details-card__header-icon" aria-hidden="true">
                            <rect x="3" y="4" width="18" height="16" rx="2" stroke="currentColor" stroke-width="2"/>
                            <path d="M16 2v4M8 2v4M3 10h18" stroke="currentColor" stroke-width="2"/>
                        </svg>
                        <h3 class="exam-details-card__title">Chi tiết đợt thi & phòng thi</h3>
                    </div>
                    
                    <div class="exam-details-card__body">
                        <div class="exam-details-ticket">
                            <div class="exam-details-ticket__brand">
                                <span class="ticket-brand__main">Trung tâm sát hạch Lái Vui</span>
                                <span class="ticket-brand__sub">Thẻ dự thi sát hạch</span>
                            </div>
                            
                            <div class="exam-details-ticket__meta">
                                <c:choose>
                                    <c:when test="${selectedExamId == '240892'}">
                                        <div class="ticket-meta-grid">
                                            <div class="ticket-meta-item">
                                                <span class="ticket-meta-label">Hạng GPLX</span>
                                                <span class="ticket-meta-value ticket-meta-value--badge">Hạng B2</span>
                                            </div>
                                            <div class="ticket-meta-item">
                                                <span class="ticket-meta-label">Mã đợt thi</span>
                                                <span class="ticket-meta-value">SH-2023-12-A</span>
                                            </div>
                                            <div class="ticket-meta-item">
                                                <span class="ticket-meta-label">Ngày thi</span>
                                                <span class="ticket-meta-value">25/12/2023</span>
                                            </div>
                                            <div class="ticket-meta-item">
                                                <span class="ticket-meta-label">Giờ tập trung</span>
                                                <span class="ticket-meta-value">07:30 sáng</span>
                                            </div>
                                            <div class="ticket-meta-item">
                                                <span class="ticket-meta-label">Phòng thi</span>
                                                <span class="ticket-meta-value">Phòng 402 - Tầng 4</span>
                                            </div>
                                            <div class="ticket-meta-item">
                                                <span class="ticket-meta-label">Máy thi</span>
                                                <span class="ticket-meta-value">MT-08</span>
                                            </div>
                                        </div>
                                        <div class="ticket-sbd-block">
                                            <span class="ticket-sbd-label">Số Báo Danh</span>
                                            <span class="ticket-sbd-value">240892</span>
                                        </div>
                                    </c:when>
                                    <c:when test="${selectedExamId == '240112'}">
                                        <div class="ticket-meta-grid">
                                            <div class="ticket-meta-item">
                                                <span class="ticket-meta-label">Hạng GPLX</span>
                                                <span class="ticket-meta-value ticket-meta-value--badge">Hạng B2</span>
                                            </div>
                                            <div class="ticket-meta-item">
                                                <span class="ticket-meta-label">Mã đợt thi</span>
                                                <span class="ticket-meta-value">SH-2023-11-T</span>
                                            </div>
                                            <div class="ticket-meta-item">
                                                <span class="ticket-meta-label">Ngày thi</span>
                                                <span class="ticket-meta-value">15/11/2023</span>
                                            </div>
                                            <div class="ticket-meta-item">
                                                <span class="ticket-meta-label">Giờ tập trung</span>
                                                <span class="ticket-meta-value">08:00 sáng</span>
                                            </div>
                                            <div class="ticket-meta-item">
                                                <span class="ticket-meta-label">Phòng thi</span>
                                                <span class="ticket-meta-value">Sân tập A - Nhà A1</span>
                                            </div>
                                            <div class="ticket-meta-item">
                                                <span class="ticket-meta-label">Máy thi</span>
                                                <span class="ticket-meta-value">MT-02</span>
                                            </div>
                                        </div>
                                        <div class="ticket-sbd-block">
                                            <span class="ticket-sbd-label">Số Báo Danh</span>
                                            <span class="ticket-sbd-value">240112</span>
                                        </div>
                                    </c:when>
                                    <c:when test="${selectedExamId == '240045'}">
                                        <div class="ticket-meta-grid">
                                            <div class="ticket-meta-item">
                                                <span class="ticket-meta-label">Hạng GPLX</span>
                                                <span class="ticket-meta-value ticket-meta-value--badge">Hạng B2</span>
                                            </div>
                                            <div class="ticket-meta-item">
                                                <span class="ticket-meta-label">Mã đợt thi</span>
                                                <span class="ticket-meta-value">SH-2023-11-K</span>
                                            </div>
                                            <div class="ticket-meta-item">
                                                <span class="ticket-meta-label">Ngày thi</span>
                                                <span class="ticket-meta-value">02/11/2023</span>
                                            </div>
                                            <div class="ticket-meta-item">
                                                <span class="ticket-meta-label">Giờ tập trung</span>
                                                <span class="ticket-meta-value">08:30 sáng</span>
                                            </div>
                                            <div class="ticket-meta-item">
                                                <span class="ticket-meta-label">Phòng thi</span>
                                                <span class="ticket-meta-value">Hội trường B</span>
                                            </div>
                                            <div class="ticket-meta-item">
                                                <span class="ticket-meta-label">Máy thi</span>
                                                <span class="ticket-meta-value">--</span>
                                            </div>
                                        </div>
                                        <div class="ticket-sbd-block">
                                            <span class="ticket-sbd-label">Số Báo Danh</span>
                                            <span class="ticket-sbd-value">240045</span>
                                        </div>
                                    </c:when>
                                    <c:otherwise>
                                        <div class="ticket-meta-grid">
                                            <div class="ticket-meta-item">
                                                <span class="ticket-meta-label">Hạng GPLX</span>
                                                <span class="ticket-meta-value ticket-meta-value--badge ticket-meta-value--badge-orange">Hạng B2</span>
                                            </div>
                                            <div class="ticket-meta-item">
                                                <span class="ticket-meta-label">Mã đợt thi</span>
                                                <span class="ticket-meta-value">SH-2024-02-G</span>
                                            </div>
                                            <div class="ticket-meta-item">
                                                <span class="ticket-meta-label">Ngày thi</span>
                                                <span class="ticket-meta-value">15/02/2024</span>
                                            </div>
                                            <div class="ticket-meta-item">
                                                <span class="ticket-meta-label">Giờ tập trung</span>
                                                <span class="ticket-meta-value">08:00 sáng</span>
                                            </div>
                                            <div class="ticket-meta-item">
                                                <span class="ticket-meta-label">Phòng thi</span>
                                                <span class="ticket-meta-value text-muted">Đang cập nhật</span>
                                            </div>
                                            <div class="ticket-meta-item">
                                                <span class="ticket-meta-label">Máy thi</span>
                                                <span class="ticket-meta-value text-muted">Đang cập nhật</span>
                                            </div>
                                        </div>
                                        <div class="ticket-sbd-block">
                                            <span class="ticket-sbd-label">Số Báo Danh</span>
                                            <span class="ticket-sbd-value text-muted">Đang cập nhật</span>
                                        </div>
                                    </c:otherwise>
                                </c:choose>
                            </div>
                            
                            <div class="exam-details-ticket__footer">
                                <div class="ticket-qr-section">
                                    <div class="ticket-qr-code">
                                        <c:choose>
                                            <c:when test="${selectedExamId == '240892' || selectedExamId == '240112' || selectedExamId == '240045'}">
                                                <svg width="72" height="72" viewBox="0 0 100 100" fill="none" xmlns="http://www.w3.org/2000/svg" aria-label="QR check-in">
                                                    <!-- Outer Border and Position Blocks -->
                                                    <rect width="100" height="100" rx="8" fill="#ffffff"/>
                                                    <!-- Top-left Finder Pattern -->
                                                    <rect x="10" y="10" width="25" height="25" fill="#0f172a" rx="2"/>
                                                    <rect x="15" y="15" width="15" height="15" fill="#ffffff" rx="1"/>
                                                    <rect x="18" y="18" width="9" height="9" fill="#0f172a" rx="1"/>
                                                    <!-- Top-right Finder Pattern -->
                                                    <rect x="65" y="10" width="25" height="25" fill="#0f172a" rx="2"/>
                                                    <rect x="70" y="15" width="15" height="15" fill="#ffffff" rx="1"/>
                                                    <rect x="73" y="18" width="9" height="9" fill="#0f172a" rx="1"/>
                                                    <!-- Bottom-left Finder Pattern -->
                                                    <rect x="10" y="65" width="25" height="25" fill="#0f172a" rx="2"/>
                                                    <rect x="15" y="70" width="15" height="15" fill="#ffffff" rx="1"/>
                                                    <rect x="18" y="73" width="9" height="9" fill="#0f172a" rx="1"/>
                                                    <!-- Dummy QR Code Blocks -->
                                                    <rect x="42" y="12" width="6" height="6" fill="#0f172a" rx="1"/>
                                                    <rect x="52" y="18" width="6" height="12" fill="#0f172a" rx="1"/>
                                                    <rect x="42" y="32" width="12" height="6" fill="#0f172a" rx="1"/>
                                                    <rect x="12" y="42" width="6" height="12" fill="#0f172a" rx="1"/>
                                                    <rect x="32" y="42" width="18" height="6" fill="#0f172a" rx="1"/>
                                                    <rect x="56" y="42" width="6" height="6" fill="#0f172a" rx="1"/>
                                                    <rect x="68" y="42" width="12" height="6" fill="#0f172a" rx="1"/>
                                                    <rect x="82" y="42" width="6" height="12" fill="#0f172a" rx="1"/>
                                                    <rect x="42" y="54" width="6" height="6" fill="#0f172a" rx="1"/>
                                                    <rect x="52" y="54" width="12" height="6" fill="#0f172a" rx="1"/>
                                                    <rect x="42" y="66" width="6" height="12" fill="#0f172a" rx="1"/>
                                                    <rect x="54" y="66" width="6" height="6" fill="#0f172a" rx="1"/>
                                                    <rect x="68" y="56" width="6" height="18" fill="#0f172a" rx="1"/>
                                                    <rect x="80" y="68" width="8" height="8" fill="#0f172a" rx="1"/>
                                                </svg>
                                            </c:when>
                                            <c:otherwise>
                                                <!-- Disabled/Pending QR code -->
                                                <svg width="72" height="72" viewBox="0 0 100 100" fill="none" xmlns="http://www.w3.org/2000/svg" style="opacity: 0.25;" aria-label="QR Chưa sẵn sàng">
                                                    <rect width="100" height="100" rx="8" fill="#e2e8f0"/>
                                                    <rect x="10" y="10" width="25" height="25" fill="#64748b" rx="2"/>
                                                    <rect x="15" y="15" width="15" height="15" fill="#e2e8f0" rx="1"/>
                                                    <rect x="65" y="10" width="25" height="25" fill="#64748b" rx="2"/>
                                                    <rect x="70" y="15" width="15" height="15" fill="#e2e8f0" rx="1"/>
                                                    <rect x="10" y="65" width="25" height="25" fill="#64748b" rx="2"/>
                                                    <rect x="15" y="70" width="15" height="15" fill="#e2e8f0" rx="1"/>
                                                </svg>
                                            </c:otherwise>
                                        </c:choose>
                                    </div>
                                    <div class="ticket-qr-text">
                                        <c:choose>
                                            <c:when test="${selectedExamId == '240892' || selectedExamId == '240112' || selectedExamId == '240045'}">
                                                <span class="qr-text-primary">Quét mã check-in cổng kiểm soát</span>
                                                <span class="qr-text-secondary">Vui lòng xuất trình mã này khi tới trung tâm sát hạch</span>
                                            </c:when>
                                            <c:otherwise>
                                                <span class="qr-text-primary text-muted">Mã check-in chưa khả dụng</span>
                                                <span class="qr-text-secondary">Sẽ tự động hiển thị trước giờ thi 24 tiếng</span>
                                            </c:otherwise>
                                        </c:choose>
                                    </div>
                                </div>
                            </div>
                        </div>
                        
                        <div class="exam-details-rules-banner">
                            <div class="rules-banner__icon">
                                <svg width="18" height="18" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg" aria-hidden="true">
                                    <circle cx="12" cy="12" r="10" stroke="currentColor" stroke-width="2"/>
                                    <line x1="12" y1="16" x2="12" y2="12" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
                                    <circle cx="12" cy="8" r="1.25" fill="currentColor"/>
                                </svg>
                            </div>
                            <div class="rules-banner__content">
                                <span class="rules-banner__title">Quy chế phòng sát hạch</span>
                                <p class="rules-banner__desc">
                                    Thí sinh xuất trình <strong>Số căn cước bản gốc</strong> và <strong>Thẻ dự thi này</strong> (bản in hoặc trên app) khi vào cửa. Vui lòng tập trung trước giờ thi tối thiểu 30 phút để nghe phổ biến nội quy phòng thi.
                                </p>
                            </div>
                        </div>
                    </div>
                </div>
                
                <%-- ==========================================
                     RIGHT COLUMN: Chi tiết kết quả & cấp bằng
                     ========================================== --%>
                <div class="exam-details-cards-wrap">
                    
                    <%-- Card 1: Sát hạch Lý thuyết --%>
                    <div class="exam-details-card exam-details-card--right">
                        <div class="exam-details-card__header">
                            <svg width="20" height="20" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg" class="exam-details-card__header-icon exam-details-card__header-icon--blue" aria-hidden="true">
                                <path d="M12 6.253v13m0-13C10.832 5.477 9.246 5 7.5 5S4.168 5.477 3 6.253v13C4.168 18.477 5.754 18 7.5 18s3.332.477 4.5 1.253m0-13C13.168 5.477 14.754 5 16.5 5c1.747 0 3.332.477 4.5 1.253v13c-1.168-.776-2.754-1.253-4.5-1.253-1.746 0-3.332.477-4.5 1.253" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                            </svg>
                            <h3 class="exam-details-card__title">Sát hạch Lý thuyết</h3>
                            
                            <c:choose>
                                <c:when test="${selectedExamId == '240892'}">
                                    <span class="details-badge details-badge--success">Đạt</span>
                                </c:when>
                                <c:when test="${selectedExamId == '240112'}">
                                    <span class="details-badge details-badge--danger">Không đạt</span>
                                </c:when>
                                <c:when test="${selectedExamId == '240045'}">
                                    <span class="details-badge details-badge--gray">Vắng thi</span>
                                </c:when>
                                <c:otherwise>
                                    <span class="details-badge details-badge--warning">Chờ thi</span>
                                </c:otherwise>
                            </c:choose>
                        </div>
                        
                        <div class="exam-details-card__body">
                            <c:choose>
                                <c:when test="${selectedExamId == '240892'}">
                                    <div class="score-display">
                                        <span class="score-display__value score-display__value--green">34</span>
                                        <span class="score-display__label">Đạt</span>
                                    </div>
                                    
                                    <table class="scorecard-table" role="table">
                                        <thead>
                                            <tr>
                                                <th scope="col">Tiêu chí sát hạch</th>
                                                <th scope="col">Yêu cầu đạt</th>
                                                <th scope="col">Kết quả đạt được</th>
                                                <th scope="col">Đánh giá</th>
                                            </tr>
                                        </thead>
                                        <tbody>
                                            <tr>
                                                <td class="font-medium">Số câu trả lời đúng</td>
                                                <td>&ge; 32 câu</td>
                                                <td class="font-semibold text-green">34 câu</td>
                                                <td><span class="table-badge table-badge--success">Đạt</span></td>
                                            </tr>
                                            <tr>
                                                <td class="font-medium">Số câu trả lời sai</td>
                                                <td>&le; 3 câu</td>
                                                <td class="font-semibold text-green">1 câu</td>
                                                <td><span class="table-badge table-badge--success">Hợp lệ</span></td>
                                            </tr>
                                            <tr>
                                                <td class="font-medium">Lỗi sai câu điểm liệt</td>
                                                <td>Không phạm lỗi</td>
                                                <td class="font-semibold text-green">Không</td>
                                                <td><span class="table-badge table-badge--success">Hợp lệ</span></td>
                                            </tr>
                                        </tbody>
                                    </table>
                                </c:when>
                                <c:when test="${selectedExamId == '240112'}">
                                    <div class="score-display">
                                        <span class="score-display__value score-display__value--red">28</span>
                                        <span class="score-display__label">Không đạt</span>
                                    </div>
                                    
                                    <table class="scorecard-table" role="table">
                                        <thead>
                                            <tr>
                                                <th scope="col">Tiêu chí sát hạch</th>
                                                <th scope="col">Yêu cầu đạt</th>
                                                <th scope="col">Kết quả đạt được</th>
                                                <th scope="col">Đánh giá</th>
                                            </tr>
                                        </thead>
                                        <tbody>
                                            <tr>
                                                <td class="font-medium">Số câu trả lời đúng</td>
                                                <td>&ge; 32 câu</td>
                                                <td class="font-semibold text-red">28 câu</td>
                                                <td><span class="table-badge table-badge--danger">Không đạt</span></td>
                                            </tr>
                                            <tr>
                                                <td class="font-medium">Số câu trả lời sai</td>
                                                <td>&le; 3 câu</td>
                                                <td class="font-semibold text-red">7 câu</td>
                                                <td><span class="table-badge table-badge--danger">Không hợp lệ</span></td>
                                            </tr>
                                            <tr>
                                                <td class="font-medium">Lỗi sai câu điểm liệt</td>
                                                <td>Không phạm lỗi</td>
                                                <td class="font-semibold text-green">Không</td>
                                                <td><span class="table-badge table-badge--success">Hợp lệ</span></td>
                                            </tr>
                                        </tbody>
                                    </table>
                                </c:when>
                                <c:when test="${selectedExamId == '240045'}">
                                    <div class="scorecard-placeholder">
                                        <span class="placeholder-icon">&empty;</span>
                                        <p class="placeholder-text">Học viên vắng mặt trong buổi sát hạch lý thuyết.</p>
                                    </div>
                                </c:when>
                                <c:otherwise>
                                    <div class="scorecard-placeholder">
                                        <span class="placeholder-icon placeholder-icon--spin">⚙</span>
                                        <p class="placeholder-text">Kỳ thi chưa diễn ra. Điểm số sẽ được cập nhật sau khi hoàn thành bài thi lý thuyết.</p>
                                    </div>
                                </c:otherwise>
                            </c:choose>
                        </div>
                    </div>
                    
                    <%-- Card 2: Sát hạch Thực hành --%>
                    <div class="exam-details-card exam-details-card--right">
                        <div class="exam-details-card__header">
                            <svg width="20" height="20" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg" class="exam-details-card__header-icon exam-details-card__header-icon--orange" aria-hidden="true">
                                <rect x="3" y="3" width="18" height="18" rx="2" stroke="currentColor" stroke-width="2" stroke-linejoin="round"/>
                                <path d="M9 3v18M15 3v18M3 9h18M3 15h18" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                            </svg>
                            <h3 class="exam-details-card__title">Sát hạch Thực hành</h3>
                            
                            <c:choose>
                                <c:when test="${selectedExamId == '240892'}">
                                    <span class="details-badge details-badge--success">Đạt</span>
                                </c:when>
                                <c:when test="${selectedExamId == '240112'}">
                                    <span class="details-badge details-badge--gray">Chưa đủ điều kiện</span>
                                </c:when>
                                <c:when test="${selectedExamId == '240045'}">
                                    <span class="details-badge details-badge--gray">Vắng thi</span>
                                </c:when>
                                <c:otherwise>
                                    <span class="details-badge details-badge--warning">Chờ thi</span>
                                </c:otherwise>
                            </c:choose>
                        </div>
                        
                        <div class="exam-details-card__body">
                            <c:choose>
                                <c:when test="${selectedExamId == '240892'}">
                                    <div class="score-display">
                                        <span class="score-display__value score-display__value--green">95</span>
                                        <span class="score-display__label">Đạt</span>
                                    </div>
                                    
                                    <table class="scorecard-table" role="table">
                                        <thead>
                                            <tr>
                                                <th scope="col">Nội dung thi sa hình</th>
                                                <th scope="col">Yêu cầu đạt</th>
                                                <th scope="col">Kết quả đạt được</th>
                                                <th scope="col">Đánh giá chung</th>
                                            </tr>
                                        </thead>
                                        <tbody>
                                            <tr>
                                                <td class="font-medium">Điểm thi sa hình thực tế</td>
                                                <td>&ge; 80 / 100 điểm</td>
                                                <td class="font-semibold text-green">95 / 100 điểm</td>
                                                <td><span class="table-badge table-badge--success">Đạt</span></td>
                                            </tr>
                                            <tr>
                                                <td class="font-medium">Tổng điểm trừ phạt sa hình</td>
                                                <td>-</td>
                                                <td class="font-semibold text-red">-5 điểm</td>
                                                <td><span class="table-badge table-badge--danger">Bị trừ điểm</span></td>
                                            </tr>
                                            <tr>
                                                <td class="font-medium">Số lỗi sa hình ghi nhận</td>
                                                <td>-</td>
                                                <td class="font-semibold text-green">1 lỗi</td>
                                                <td><span class="table-badge table-badge--success">Hợp lệ</span></td>
                                            </tr>
                                        </tbody>
                                    </table>
                                    
                                    <div class="practical-deduction-comment">
                                        <svg width="14" height="14" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg" class="comment-icon" aria-hidden="true">
                                            <path d="M4 15s1-1 4-1 5 2 8 2 4-1 4-1V3s-1 1-4 1-5-2-8-2-4 1-4 1zM4 22v-7" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                                        </svg>
                                        <span>Trạm 2: Đè vạch xuất phát hình thi B2 -5 điểm</span>
                                    </div>
                                </c:when>
                                <c:when test="${selectedExamId == '240112'}">
                                    <div class="scorecard-placeholder">
                                        <svg width="24" height="24" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg" class="placeholder-svg-icon" aria-hidden="true">
                                            <circle cx="12" cy="12" r="10" stroke="currentColor" stroke-width="2"/>
                                            <line x1="15" y1="9" x2="9" y2="15" stroke="currentColor" stroke-width="2"/>
                                        </svg>
                                        <p class="placeholder-text">Học viên không đủ điều kiện tham gia phần thi thực hành do chưa đạt phần thi lý thuyết.</p>
                                    </div>
                                </c:when>
                                <c:when test="${selectedExamId == '240045'}">
                                    <div class="scorecard-placeholder">
                                        <span class="placeholder-icon">&empty;</span>
                                        <p class="placeholder-text">Học viên vắng mặt trong buổi sát hạch thực hành.</p>
                                    </div>
                                </c:when>
                                <c:otherwise>
                                    <div class="scorecard-placeholder">
                                        <span class="placeholder-icon placeholder-icon--spin">⚙</span>
                                        <p class="placeholder-text">Kỳ thi chưa diễn ra. Điểm số sẽ được cập nhật sau khi hoàn thành bài thi thực hành.</p>
                                    </div>
                                </c:otherwise>
                            </c:choose>
                        </div>
                    </div>
                    
                    <%-- Card 3: Đăng ký dịch vụ cấp phát GPLX --%>
                    <div class="exam-details-card exam-details-card--right">
                        <div class="exam-details-card__header">
                            <svg width="20" height="20" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg" class="exam-details-card__header-icon exam-details-card__header-icon--green" aria-hidden="true">
                                <rect x="1" y="3" width="15" height="13" rx="2" stroke="currentColor" stroke-width="2"/>
                                <polygon points="16 8 20 8 23 11 23 16 16 16 16 8" stroke="currentColor" stroke-width="2" stroke-linejoin="round"/>
                                <circle cx="5.5" cy="18.5" r="2.5" stroke="currentColor" stroke-width="2"/>
                                <circle cx="18.5" cy="18.5" r="2.5" stroke="currentColor" stroke-width="2"/>
                            </svg>
                            <h3 class="exam-details-card__title">Đăng ký dịch vụ cấp phát GPLX</h3>
                        </div>
                        
                        <div class="exam-details-card__body">
                            <c:choose>
                                <c:when test="${selectedExamId == '240892'}">
                                    <table class="shipping-details-table" role="table">
                                        <tbody>
                                            <tr>
                                                <td class="shipping-details-label">Dịch vụ nhận bằng</td>
                                                <td class="shipping-details-value">Nhận bằng tại nhà (VNPost)</td>
                                            </tr>
                                            <tr>
                                                <td class="shipping-details-label">Trạng thái phôi bằng</td>
                                                <td class="shipping-details-value"><span class="table-badge table-badge--success">Đang in phôi bằng lái B2</span></td>
                                            </tr>
                                            <tr>
                                                <td class="shipping-details-label">Dự kiến bàn giao</td>
                                                <td class="shipping-details-value font-semibold">05/01/2024</td>
                                            </tr>
                                        </tbody>
                                    </table>
                                </c:when>
                                <c:otherwise>
                                    <div class="scorecard-placeholder">
                                        <svg width="24" height="24" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg" class="placeholder-svg-icon text-muted" aria-hidden="true">
                                            <rect x="1" y="3" width="15" height="13" rx="2" stroke="currentColor" stroke-width="2"/>
                                            <polygon points="16 8 20 8 23 11 23 16 16 16 16 8" stroke="currentColor" stroke-width="2" stroke-linejoin="round"/>
                                            <circle cx="5.5" cy="18.5" r="2.5" stroke="currentColor" stroke-width="2"/>
                                            <circle cx="18.5" cy="18.5" r="2.5" stroke="currentColor" stroke-width="2"/>
                                        </svg>
                                        <p class="placeholder-text">Dịch vụ cấp phát GPLX sẽ mở đăng ký khi học viên hoàn thành và Đạt cả 2 phần thi Lý thuyết & Thực hành.</p>
                                    </div>
                                </c:otherwise>
                            </c:choose>
                        </div>
                    </div>
                    
                </div>
                
            </div>
        </section>

        <%-- =========================================================
             Bottom Split Card Grid Section
             ========================================================= --%>
        <div class="my-exams-bottom-grid">
            
            <%-- Left Side: Upcoming Exam Dark Card --%>
            <div class="next-exam-card">
                
                <div class="next-exam-card__header">
                    <span class="next-exam-card__badge">Kỳ thi tiếp theo</span>
                    <span class="next-exam-card__code">TNP-B2-0224</span>
                </div>

                <div>
                    <h2 class="next-exam-card__title">Sát hạch Tốt nghiệp</h2>
                    
                    <div class="next-exam-card__info-list">
                        <div class="next-exam-card__info-item">
                            <svg width="16" height="16" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg" class="next-exam-card__info-icon">
                                <rect x="3" y="4" width="18" height="16" rx="2" stroke="currentColor" stroke-width="2"/>
                                <line x1="16" y1="2" x2="16" y2="6" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
                                <line x1="8" y1="2" x2="8" y2="6" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
                                <line x1="3" y1="10" x2="21" y2="10" stroke="currentColor" stroke-width="2"/>
                            </svg>
                            <span>15 tháng 02, 2024</span>
                        </div>
                        <div class="next-exam-card__info-item">
                            <svg width="16" height="16" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg" class="next-exam-card__info-icon">
                                <circle cx="12" cy="12" r="10" stroke="currentColor" stroke-width="2"/>
                                <polyline points="12 6 12 12 16 14" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                            </svg>
                            <span>08:00 AM</span>
                        </div>
                        <div class="next-exam-card__info-item">
                            <svg width="16" height="16" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg" class="next-exam-card__info-icon">
                                <path d="M21 10c0 7-9 13-9 13s-9-6-9-13a9 9 0 0 1 18 0z" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                                <circle cx="12" cy="10" r="3" stroke="currentColor" stroke-width="2"/>
                            </svg>
                            <span>Sân sát hạch số 1 - TP.HCM</span>
                        </div>
                    </div>
                </div>

                <a href="#" class="next-exam-card__btn">Xem thông báo chi tiết</a>

            </div>

        </div>

    </main>

    <%-- Footer --%>
    <jsp:include page="/views/landing/components/footer.jsp" />
</div>

</body>
</html>

<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>

<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Dashboard Cá Nhân - Lái Vui</title>
    <meta name="description" content="Theo dõi hồ sơ, đợt thi, kết quả sát hạch và chứng chỉ lái xe của bạn tại Lái Vui.">

    <!-- Google Fonts -->
    <link rel="preconnect" href="https://fonts.googleapis.com">
    <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
    <link href="https://fonts.googleapis.com/css2?family=Be+Vietnam+Pro:wght@400;500;600;700;800&family=Inter:wght@400;500;600;700;800&display=swap" rel="stylesheet">

    <!-- Layout Stylesheets -->
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/style.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/layout.css">
</head>
<body class="has-side-nav-bar">

<%-- Inject registrant sidebar --%>
<jsp:include page="/views/layout/sidebar-registrant.jsp">
    <jsp:param name="activeSidebar" value="dashboard" />
</jsp:include>

<div class="dashboard-shell">
    <main class="main-content" id="main-content">

        <%-- Breadcrumbs --%>
        <nav class="breadcrumbs" aria-label="Breadcrumb">
            <a href="${pageContext.request.contextPath}/views/public/home.jsp">Trang chủ</a>
            <span class="breadcrumbs__separator" aria-hidden="true">/</span>
            <span class="breadcrumbs__current" aria-current="page">Dashboard cá nhân</span>
        </nav>

        <%-- =========================================================
             Welcome Banner
             ========================================================= --%>
        <section class="welcome-banner" aria-label="Chào mừng">
            <div class="welcome-banner__avatar" aria-hidden="true">
                <svg width="40" height="40" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                    <circle cx="12" cy="8" r="4" stroke="currentColor" stroke-width="1.8"></circle>
                    <path d="M4 20C4 16.686 7.582 14 12 14C16.418 14 20 16.686 20 20" stroke="currentColor" stroke-width="1.8" stroke-linecap="round"></path>
                </svg>
            </div>
            <div class="welcome-banner__text">
                <p class="welcome-banner__greeting">Xin chào, thí sinh</p>
                <h1 class="welcome-banner__name">${empty registrantName ? 'Nguyễn Văn A' : registrantName}</h1>
                <p class="welcome-banner__sub">
                    Chào mừng bạn trở lại! Theo dõi quá trình đăng ký và sát hạch lái xe của bạn ngay bên dưới.
                </p>
            </div>
            <div class="welcome-banner__actions">
                <a href="${pageContext.request.contextPath}/views/registrant/register-exam.jsp" class="welcome-banner__btn welcome-banner__btn--primary" id="btn-register-exam">
                    <svg width="16" height="16" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                        <rect x="3" y="4" width="18" height="16" rx="2" stroke="currentColor" stroke-width="2"></rect>
                        <path d="M16 2v4M8 2v4M3 10h18M12 14v4M10 16h4" stroke="currentColor" stroke-width="2" stroke-linecap="round"></path>
                    </svg>
                    Đăng ký đợt thi
                </a>
                <a href="${pageContext.request.contextPath}/views/registrant/profile.jsp" class="welcome-banner__btn welcome-banner__btn--outline" id="btn-view-profile">
                    <svg width="16" height="16" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                        <circle cx="12" cy="8" r="4" stroke="currentColor" stroke-width="2"></circle>
                        <path d="M4 20C4 16.686 7.582 14 12 14C16.418 14 20 16.686 20 20" stroke="currentColor" stroke-width="2" stroke-linecap="round"></path>
                    </svg>
                    Hồ sơ cá nhân
                </a>
            </div>
        </section>

        <%-- =========================================================
             KPI Stat Cards
             ========================================================= --%>
        <section class="stat-cards-row" aria-label="Thống kê cá nhân">

            <%-- Card 1: Hồ sơ đăng ký --%>
            <div class="r-stat-card" id="stat-profile">
                <div class="r-stat-card__top">
                    <div class="r-stat-card__icon r-stat-card__icon--blue" aria-hidden="true">
                        <svg width="22" height="22" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                            <circle cx="12" cy="8" r="4" stroke="currentColor" stroke-width="1.8"></circle>
                            <path d="M4 20C4 16.686 7.582 14 12 14C16.418 14 20 16.686 20 20" stroke="currentColor" stroke-width="1.8" stroke-linecap="round"></path>
                        </svg>
                    </div>
                    <span class="r-stat-card__badge r-stat-card__badge--success">Đã xác nhận</span>
                </div>
                <div>
                    <p class="r-stat-card__value">${empty profileStatus ? '1' : profileStatus}</p>
                    <p class="r-stat-card__label">Hồ sơ đăng ký</p>
                </div>
            </div>

            <%-- Card 2: Đợt thi đã đăng ký --%>
            <div class="r-stat-card" id="stat-exams">
                <div class="r-stat-card__top">
                    <div class="r-stat-card__icon r-stat-card__icon--green" aria-hidden="true">
                        <svg width="22" height="22" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                            <rect x="3" y="4" width="18" height="16" rx="2" stroke="currentColor" stroke-width="1.8"></rect>
                            <path d="M16 2v4M8 2v4M3 10h18" stroke="currentColor" stroke-width="1.8" stroke-linecap="round"></path>
                            <path d="M8 15l2 2 4-4" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round"></path>
                        </svg>
                    </div>
                    <span class="r-stat-card__badge r-stat-card__badge--info">Sắp thi</span>
                </div>
                <div>
                    <p class="r-stat-card__value">${empty registeredExams ? '2' : registeredExams}</p>
                    <p class="r-stat-card__label">Đợt thi đã đăng ký</p>
                </div>
            </div>

            <%-- Card 3: Kết quả sát hạch --%>
            <div class="r-stat-card" id="stat-results">
                <div class="r-stat-card__top">
                    <div class="r-stat-card__icon r-stat-card__icon--amber" aria-hidden="true">
                        <svg width="22" height="22" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                            <rect x="4" y="2" width="16" height="20" rx="2" stroke="currentColor" stroke-width="1.8"></rect>
                            <path d="M8 7h8M8 11h8M8 15h5" stroke="currentColor" stroke-width="1.8" stroke-linecap="round"></path>
                        </svg>
                    </div>
                    <span class="r-stat-card__badge r-stat-card__badge--pending">Chờ kết quả</span>
                </div>
                <div>
                    <p class="r-stat-card__value">${empty examResults ? '1' : examResults}</p>
                    <p class="r-stat-card__label">Kết quả đã có</p>
                </div>
            </div>

            <%-- Card 4: Lệ phí --%>
            <div class="r-stat-card" id="stat-payment">
                <div class="r-stat-card__top">
                    <div class="r-stat-card__icon r-stat-card__icon--red" aria-hidden="true">
                        <svg width="22" height="22" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                            <rect x="2" y="5" width="20" height="14" rx="2" stroke="currentColor" stroke-width="1.8"></rect>
                            <path d="M2 10h20" stroke="currentColor" stroke-width="1.8"></path>
                            <circle cx="6" cy="15" r="1.5" fill="currentColor"></circle>
                        </svg>
                    </div>
                    <span class="r-stat-card__badge r-stat-card__badge--neutral">VNĐ</span>
                </div>
                <div>
                    <p class="r-stat-card__value r-stat-card__value--compact">
                        <fmt:formatNumber value="${empty totalFee ? 1200000 : totalFee}" type="number"/>
                    </p>
                    <p class="r-stat-card__label">Tổng lệ phí đã nộp</p>
                </div>
            </div>
        </section>

        <%-- =========================================================
             Main Content: Exam List + Right Sidebar
             ========================================================= --%>
        <div class="content-grid">

            <%-- LEFT: Đợt thi đã đăng ký --%>
            <div class="r-panel" id="panel-registered-exams">
                <div class="r-panel__header">
                    <h2 class="r-panel__title">
                        <svg width="18" height="18" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                            <rect x="3" y="4" width="18" height="16" rx="2" stroke="currentColor" stroke-width="2"></rect>
                            <path d="M16 2v4M8 2v4M3 10h18" stroke="currentColor" stroke-width="2" stroke-linecap="round"></path>
                        </svg>
                        Đợt thi đã đăng ký
                    </h2>
                    <a href="${pageContext.request.contextPath}/views/registrant/my-exams.jsp" class="r-panel__link" id="link-all-exams">
                        Xem tất cả
                        <svg width="14" height="14" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                            <path d="M5 12h14M12 5l7 7-7 7" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round"></path>
                        </svg>
                    </a>
                </div>
                <div class="r-panel__body--noPad">
                    <table class="reg-table" role="table" aria-label="Danh sách đợt thi đã đăng ký">
                        <thead>
                            <tr>
                                <th scope="col">Đợt thi</th>
                                <th scope="col">Hạng bằng</th>
                                <th scope="col">Ngày thi</th>
                                <th scope="col">Địa điểm</th>
                                <th scope="col">Trạng thái</th>
                                <th scope="col"></th>
                            </tr>
                        </thead>
                        <tbody>
                            <c:if test="${not empty registeredExamList}">
                                <c:forEach var="exam" items="${registeredExamList}">
                                    <tr>
                                        <td>
                                            <span class="reg-table__title">${exam.examName}</span><br>
                                            <span class="reg-table__subtitle">${exam.examCode}</span>
                                        </td>
                                        <td>${exam.licenceClass}</td>
                                        <td>
                                            <fmt:formatDate value="${exam.examDate}" pattern="dd/MM/yyyy"/>
                                        </td>
                                        <td>${exam.location}</td>
                                        <td>
                                            <span class="status-badge status-badge--${exam.statusClass}">${exam.statusLabel}</span>
                                        </td>
                                        <td>
                                            <a href="${pageContext.request.contextPath}/views/registrant/my-exams.jsp?examId=${exam.id}"
                                               class="reg-table__link">Chi tiết</a>
                                        </td>
                                    </tr>
                                </c:forEach>
                            </c:if>
                            <c:if test="${empty registeredExamList}">
                                <%-- Fallback demo rows when no backend data --%>
                                <tr>
                                    <td>
                                        <span class="reg-table__title">Đợt thi tháng 06/2025</span><br>
                                        <span class="reg-table__subtitle">SH-2025-06-A</span>
                                    </td>
                                    <td>Hạng B2</td>
                                    <td>15/06/2025</td>
                                    <td>Hà Nội</td>
                                    <td><span class="status-badge status-badge--info">Đã đăng ký</span></td>
                                    <td><a href="#" class="reg-table__link">Chi tiết</a></td>
                                </tr>
                                <tr>
                                    <td>
                                        <span class="reg-table__title">Đợt thi tháng 04/2025</span><br>
                                        <span class="reg-table__subtitle">SH-2025-04-C</span>
                                    </td>
                                    <td>Hạng A2</td>
                                    <td>20/04/2025</td>
                                    <td>TP. Hồ Chí Minh</td>
                                    <td><span class="status-badge status-badge--approved">Đã hoàn thành</span></td>
                                    <td><a href="#" class="reg-table__link">Chi tiết</a></td>
                                </tr>
                                <tr>
                                    <td>
                                        <span class="reg-table__title">Đợt thi tháng 02/2025</span><br>
                                        <span class="reg-table__subtitle">SH-2025-02-B</span>
                                    </td>
                                    <td>Hạng B1</td>
                                    <td>10/02/2025</td>
                                    <td>Đà Nẵng</td>
                                    <td><span class="status-badge status-badge--pending">Chờ thanh toán</span></td>
                                    <td><a href="#" class="reg-table__link">Chi tiết</a></td>
                                </tr>
                            </c:if>
                        </tbody>
                    </table>
                </div>
            </div>

            <%-- RIGHT COLUMN --%>
            <div class="dashboard-sidebar-column">

                <%-- Upcoming Exam Countdown --%>
                <div class="r-panel" id="panel-upcoming">
                    <div class="r-panel__header">
                        <h2 class="r-panel__title">
                            <svg width="18" height="18" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                                <circle cx="12" cy="12" r="9" stroke="currentColor" stroke-width="2"></circle>
                                <path d="M12 7v5l3 3" stroke="currentColor" stroke-width="2" stroke-linecap="round"></path>
                            </svg>
                            Đợt thi sắp tới
                        </h2>
                    </div>
                    <div class="r-panel__body">
                        <div class="upcoming-exam-card">
                            <div class="upcoming-exam-card__header">
                                <div>
                                    <p class="upcoming-exam-card__label">Hạng B2 — Lý thuyết</p>
                                    <p class="upcoming-exam-card__name">${empty upcomingExamName ? 'Đợt thi tháng 06/2025' : upcomingExamName}</p>
                                </div>
                                <div class="upcoming-exam-card__countdown" id="countdown-block">
                                    <div class="countdown-number">${empty upcomingExamDays ? 18 : upcomingExamDays}</div>
                                    <div class="countdown-label">ngày nữa</div>
                                </div>
                            </div>
                            <div class="upcoming-exam-card__detail">
                                <div class="upcoming-exam-detail-row">
                                    <svg width="15" height="15" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                                        <rect x="3" y="4" width="18" height="16" rx="2" stroke="currentColor" stroke-width="2"></rect>
                                        <path d="M16 2v4M8 2v4M3 10h18" stroke="currentColor" stroke-width="2" stroke-linecap="round"></path>
                                    </svg>
                                    <c:if test="${not empty upcomingExamDate}">
                                        <fmt:formatDate value="${upcomingExamDate}" pattern="EEEE, dd/MM/yyyy" />
                                    </c:if>
                                    <c:if test="${empty upcomingExamDate}">Chủ Nhật, 15/06/2025</c:if>
                                </div>
                                <div class="upcoming-exam-detail-row">
                                    <svg width="15" height="15" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                                        <circle cx="12" cy="12" r="9" stroke="currentColor" stroke-width="2"></circle>
                                        <path d="M12 7v5l3 3" stroke="currentColor" stroke-width="2" stroke-linecap="round"></path>
                                    </svg>
                                    ${empty upcomingExamTime ? '08:00 — 10:00' : upcomingExamTime}
                                </div>
                                <div class="upcoming-exam-detail-row">
                                    <svg width="15" height="15" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                                        <path d="M12 2C8.13 2 5 5.13 5 9C5 14.25 12 22 12 22C12 22 19 14.25 19 9C19 5.13 15.87 2 12 2Z" stroke="currentColor" stroke-width="2" stroke-linejoin="round"></path>
                                        <circle cx="12" cy="9" r="2.5" stroke="currentColor" stroke-width="2"></circle>
                                    </svg>
                                    ${empty upcomingExamLocation ? 'Trung tâm sát hạch Hà Nội' : upcomingExamLocation}
                                </div>
                            </div>
                            <a href="${pageContext.request.contextPath}/views/registrant/my-exams.jsp" class="upcoming-exam-card__btn" id="btn-view-exam-detail">
                                <svg width="15" height="15" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                                    <path d="M1 12S5 4 12 4s11 8 11 8-4 8-11 8S1 12 1 12z" stroke="currentColor" stroke-width="2"></path>
                                    <circle cx="12" cy="12" r="3" stroke="currentColor" stroke-width="2"></circle>
                                </svg>
                                Xem chi tiết
                            </a>
                        </div>
                    </div>
                </div>

                <%-- Quick Links --%>
                <div class="r-panel" id="panel-quick-links">
                    <div class="r-panel__header">
                        <h2 class="r-panel__title">
                            <svg width="18" height="18" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                                <path d="M13 2L3 14h9l-1 8 10-12h-9l1-8z" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"></path>
                            </svg>
                            Truy cập nhanh
                        </h2>
                    </div>
                    <div class="r-panel__body">
                        <div class="quick-links-grid">
                            <a href="${pageContext.request.contextPath}/views/registrant/profile.jsp" class="quick-link-card" id="ql-profile">
                                <div class="quick-link-card__icon" aria-hidden="true">
                                    <svg width="18" height="18" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                                        <circle cx="12" cy="8" r="4" stroke="currentColor" stroke-width="1.8"></circle>
                                        <path d="M4 20C4 16.686 7.582 14 12 14C16.418 14 20 16.686 20 20" stroke="currentColor" stroke-width="1.8" stroke-linecap="round"></path>
                                    </svg>
                                </div>
                                <span class="quick-link-card__label">Hồ sơ cá nhân</span>
                                <span class="quick-link-card__sub">Thông tin cá nhân</span>
                            </a>
                            <a href="${pageContext.request.contextPath}/views/registrant/register-exam.jsp" class="quick-link-card" id="ql-register">
                                <div class="quick-link-card__icon" aria-hidden="true">
                                    <svg width="18" height="18" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                                        <rect x="3" y="4" width="18" height="16" rx="2" stroke="currentColor" stroke-width="1.8"></rect>
                                        <path d="M16 2v4M8 2v4M3 10h18" stroke="currentColor" stroke-width="1.8" stroke-linecap="round"></path>
                                    </svg>
                                </div>
                                <span class="quick-link-card__label">Đăng ký thi</span>
                                <span class="quick-link-card__sub">Đăng ký đợt thi mới</span>
                            </a>
                            <a href="${pageContext.request.contextPath}/views/registrant/my-exams.jsp" class="quick-link-card" id="ql-my-exams">
                                <div class="quick-link-card__icon" aria-hidden="true">
                                    <svg width="18" height="18" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                                        <rect x="5" y="3" width="14" height="18" rx="2" stroke="currentColor" stroke-width="1.8"></rect>
                                        <path d="M9 8h6M9 12h6M9 16h4" stroke="currentColor" stroke-width="1.8" stroke-linecap="round"></path>
                                    </svg>
                                </div>
                                <span class="quick-link-card__label">Đợt thi của tôi</span>
                                <span class="quick-link-card__sub">Lịch thi & phòng thi</span>
                            </a>
                            <a href="${pageContext.request.contextPath}/views/registrant/my-exams.jsp" class="quick-link-card" id="ql-scores">
                                <div class="quick-link-card__icon" aria-hidden="true">
                                    <svg width="18" height="18" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                                        <rect x="4" y="2" width="16" height="20" rx="2" stroke="currentColor" stroke-width="1.8"></rect>
                                        <path d="M8 7h8M8 11h8M8 15h5" stroke="currentColor" stroke-width="1.8" stroke-linecap="round"></path>
                                    </svg>
                                </div>
                                <span class="quick-link-card__label">Kết quả & Điểm</span>
                                <span class="quick-link-card__sub">Tra cứu kết quả thi</span>
                            </a>
                        </div>
                    </div>
                </div>
            </div>
        </div>

        <%-- =========================================================
             Recent Activity Timeline
             ========================================================= --%>
        <div class="r-panel r-panel--activity" id="panel-activity">
            <div class="r-panel__header">
                <h2 class="r-panel__title">
                    <svg width="18" height="18" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                        <circle cx="12" cy="12" r="9" stroke="currentColor" stroke-width="2"></circle>
                        <path d="M12 7v5l3 3" stroke="currentColor" stroke-width="2" stroke-linecap="round"></path>
                    </svg>
                    Hoạt động gần đây
                </h2>
            </div>
            <div class="r-panel__body">
                <div class="timeline" role="list">
                    <c:if test="${not empty activityList}">
                        <c:forEach var="act" items="${activityList}">
                            <div class="timeline-item" role="listitem">
                                <div class="timeline-item__dot timeline-item__dot--${act.colorClass}" aria-hidden="true">
                                    <svg width="14" height="14" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                                        <path d="${act.iconPath}" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"></path>
                                    </svg>
                                </div>
                                <div class="timeline-item__content">
                                    <p class="timeline-item__title">${act.title}</p>
                                    <p class="timeline-item__desc">${act.desc}</p>
                                    <span class="timeline-item__time">${act.time}</span>
                                </div>
                            </div>
                        </c:forEach>
                    </c:if>
                    <c:if test="${empty activityList}">
                        <%-- Fallback demo activities --%>
                        <div class="timeline-item" role="listitem">
                            <div class="timeline-item__dot timeline-item__dot--green" aria-hidden="true">
                                <svg width="14" height="14" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                                    <path d="M20 6L9 17l-5-5" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round"></path>
                                </svg>
                            </div>
                            <div class="timeline-item__content">
                                <p class="timeline-item__title">Đăng ký đợt thi thành công</p>
                                <p class="timeline-item__desc">Đã đăng ký tham gia Đợt thi tháng 06/2025 — Hạng B2</p>
                                <span class="timeline-item__time">Hôm nay, 09:45</span>
                            </div>
                        </div>
                        <div class="timeline-item" role="listitem">
                            <div class="timeline-item__dot timeline-item__dot--blue" aria-hidden="true">
                                <svg width="14" height="14" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                                    <rect x="2" y="5" width="20" height="14" rx="2" stroke="currentColor" stroke-width="2"></rect>
                                    <path d="M2 10h20" stroke="currentColor" stroke-width="2"></path>
                                </svg>
                            </div>
                            <div class="timeline-item__content">
                                <p class="timeline-item__title">Thanh toán lệ phí thành công</p>
                                <p class="timeline-item__desc">Lệ phí thi Hạng B2 — 600.000 VNĐ đã được xử lý</p>
                                <span class="timeline-item__time">Hôm qua, 14:22</span>
                            </div>
                        </div>
                        <div class="timeline-item" role="listitem">
                            <div class="timeline-item__dot timeline-item__dot--amber" aria-hidden="true">
                                <svg width="14" height="14" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                                    <rect x="4" y="2" width="16" height="20" rx="2" stroke="currentColor" stroke-width="2"></rect>
                                    <path d="M8 7h8M8 11h8M8 15h5" stroke="currentColor" stroke-width="2" stroke-linecap="round"></path>
                                </svg>
                            </div>
                            <div class="timeline-item__content">
                                <p class="timeline-item__title">Kết quả thi được cập nhật</p>
                                <p class="timeline-item__desc">Đợt thi tháng 04/2025 — Hạng A2: Đạt 42/50 câu</p>
                                <span class="timeline-item__time">22/04/2025, 08:00</span>
                            </div>
                        </div>
                    </c:if>
                </div>
            </div>
        </div>

    </main>

    <%-- Footer --%>
    <jsp:include page="/views/layout/footer.jsp" />
</div>

</body>
</html>

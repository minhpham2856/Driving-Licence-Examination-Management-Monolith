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
            <a href="${pageContext.request.contextPath}/home">Trang chủ</a>
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
                <h1 class="welcome-banner__name">${registrantName}</h1>
                <p class="welcome-banner__sub">
                    Chào mừng bạn trở lại! Theo dõi quá trình đăng ký và sát hạch lái xe của bạn ngay bên dưới.
                </p>
            </div>
            <div class="welcome-banner__actions">
                <a href="${pageContext.request.contextPath}/registrant/register-exam" class="welcome-banner__btn welcome-banner__btn--primary" id="btn-register-exam">
                    <svg width="16" height="16" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                        <rect x="3" y="4" width="18" height="16" rx="2" stroke="currentColor" stroke-width="2"></rect>
                        <path d="M16 2v4M8 2v4M3 10h18M12 14v4M10 16h4" stroke="currentColor" stroke-width="2" stroke-linecap="round"></path>
                    </svg>
                    Đăng ký đợt thi
                </a>
                <a href="${pageContext.request.contextPath}/registrant/profile" class="welcome-banner__btn welcome-banner__btn--outline" id="btn-view-profile">
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
                    <span class="r-stat-card__badge r-stat-card__badge--${profileStatusBadgeClass}">${profileStatusBadge}</span>
                </div>
                <div>
                    <p class="r-stat-card__value">${profileDocumentCount}</p>
                    <p class="r-stat-card__label">Giấy tờ đã tải lên</p>
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
                    <span class="r-stat-card__badge r-stat-card__badge--${examStatBadgeClass}">${examStatBadge}</span>
                </div>
                <div>
                    <p class="r-stat-card__value">${registeredExams}</p>
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
                    <span class="r-stat-card__badge r-stat-card__badge--${resultStatBadgeClass}">${resultStatBadge}</span>
                </div>
                <div>
                    <p class="r-stat-card__value">${examResults}</p>
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
                        <fmt:formatNumber value="${totalFee}" type="number"/>
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
                    <a href="${pageContext.request.contextPath}/registrant/my-exams" class="r-panel__link" id="link-all-exams">
                        Xem tất cả
                        <svg width="14" height="14" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                            <path d="M5 12h14M12 5l7 7-7 7" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round"></path>
                        </svg>
                    </a>
                </div>
                <form method="get" action="${pageContext.request.contextPath}/registrant/dashboard"
                      class="r-panel__body" style="padding:1rem 1.25rem;border-bottom:1px solid #e2e8f0;" aria-label="Lọc đợt thi">
                    <div style="display:flex;flex-wrap:wrap;gap:0.75rem;align-items:flex-end;">
                        <div>
                            <label for="dashFilterStatus" style="display:block;font-size:0.8rem;margin-bottom:0.25rem;">Trạng thái</label>
                            <select id="dashFilterStatus" name="status" class="input-field">
                                <option value="all" ${filterStatus eq 'all' ? 'selected' : ''}>Tất cả</option>
                                <option value="upcoming" ${filterStatus eq 'upcoming' ? 'selected' : ''}>Sắp thi</option>
                                <option value="pending_payment" ${filterStatus eq 'pending_payment' ? 'selected' : ''}>Chờ thanh toán</option>
                                <option value="passed" ${filterStatus eq 'passed' ? 'selected' : ''}>Đã đạt</option>
                                <option value="cancelled" ${filterStatus eq 'cancelled' ? 'selected' : ''}>Đã hủy</option>
                            </select>
                        </div>
                        <div style="flex:1;min-width:180px;">
                            <label for="dashFilterQuery" style="display:block;font-size:0.8rem;margin-bottom:0.25rem;">Tìm đợt thi</label>
                            <input type="search" id="dashFilterQuery" name="q" class="input-field" placeholder="Tên đợt, hạng..."
                                   value="${filterQuery}">
                        </div>
                        <button type="submit" class="welcome-banner__btn welcome-banner__btn--primary" style="height:42px;">Lọc</button>
                    </div>
                </form>
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
                            <c:choose>
                                <c:when test="${not empty registeredExamList}">
                                    <c:forEach var="exam" items="${registeredExamList}">
                                        <tr>
                                            <td>
                                                <span class="reg-table__title">${exam.title}</span><br>
                                                <span class="reg-table__subtitle">
                                                    <c:choose>
                                                        <c:when test="${not empty exam.sbd}">SBD: ${exam.sbd}</c:when>
                                                        <c:otherwise>
                                                            <fmt:formatDate value="${exam.examDate}" pattern="dd/MM/yyyy"/>
                                                        </c:otherwise>
                                                    </c:choose>
                                                </span>
                                            </td>
                                            <td>Hạng ${exam.licenceCode}</td>
                                            <td>
                                                <fmt:formatDate value="${exam.examDate}" pattern="dd/MM/yyyy"/>
                                            </td>
                                            <td>${exam.roomLabel}</td>
                                            <td>
                                                <span class="status-badge status-badge--${exam.statusClass}">${exam.statusLabel}</span>
                                            </td>
                                            <td>
                                                <a href="${pageContext.request.contextPath}/registrant/my-exams/detail?examId=${exam.registrationId}"
                                                   class="reg-table__link">Chi tiết</a>
                                            </td>
                                        </tr>
                                    </c:forEach>
                                </c:when>
                                <c:otherwise>
                                    <tr>
                                        <td colspan="6" style="text-align:center;padding:2rem;color:#64748b;">
                                            Bạn chưa đăng ký đợt thi nào.
                                            <a href="${pageContext.request.contextPath}/registrant/register-exam">Đăng ký ngay</a>
                                        </td>
                                    </tr>
                                </c:otherwise>
                            </c:choose>
                        </tbody>
                    </table>
                    <c:if test="${examListPage.totalPages > 1}">
                        <div style="padding:0.75rem 1.25rem;display:flex;justify-content:space-between;align-items:center;font-size:0.875rem;">
                            <span>Trang ${examListPage.page}/${examListPage.totalPages} — ${examListPage.totalItems} đợt</span>
                            <div style="display:flex;gap:0.5rem;">
                                <c:if test="${examListPage.hasPrevious}">
                                    <a href="${pageContext.request.contextPath}/registrant/dashboard?status=${filterStatus}&amp;q=${filterQuery}&amp;page=${examListPage.page - 1}">Trước</a>
                                </c:if>
                                <c:if test="${examListPage.hasNext}">
                                    <a href="${pageContext.request.contextPath}/registrant/dashboard?status=${filterStatus}&amp;q=${filterQuery}&amp;page=${examListPage.page + 1}">Sau</a>
                                </c:if>
                            </div>
                        </div>
                    </c:if>
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
                        <c:choose>
                            <c:when test="${not empty upcomingExamName}">
                                <div class="upcoming-exam-card">
                                    <div class="upcoming-exam-card__header">
                                        <div>
                                            <p class="upcoming-exam-card__label">${upcomingExamLabel}</p>
                                            <p class="upcoming-exam-card__name">${upcomingExamName}</p>
                                        </div>
                                        <div class="upcoming-exam-card__countdown" id="countdown-block">
                                            <div class="countdown-number">${upcomingExamDays}</div>
                                            <div class="countdown-label">ngày nữa</div>
                                        </div>
                                    </div>
                                    <div class="upcoming-exam-card__detail">
                                        <div class="upcoming-exam-detail-row">
                                            <svg width="15" height="15" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                                                <rect x="3" y="4" width="18" height="16" rx="2" stroke="currentColor" stroke-width="2"></rect>
                                                <path d="M16 2v4M8 2v4M3 10h18" stroke="currentColor" stroke-width="2" stroke-linecap="round"></path>
                                            </svg>
                                            <fmt:formatDate value="${upcomingExamDate}" pattern="EEEE, dd/MM/yyyy" />
                                        </div>
                                        <div class="upcoming-exam-detail-row">
                                            <svg width="15" height="15" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                                                <circle cx="12" cy="12" r="9" stroke="currentColor" stroke-width="2"></circle>
                                                <path d="M12 7v5l3 3" stroke="currentColor" stroke-width="2" stroke-linecap="round"></path>
                                            </svg>
                                            ${upcomingExamTime}
                                        </div>
                                        <div class="upcoming-exam-detail-row">
                                            <svg width="15" height="15" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                                                <path d="M12 2C8.13 2 5 5.13 5 9C5 14.25 12 22 12 22C12 22 19 14.25 19 9C19 5.13 15.87 2 12 2Z" stroke="currentColor" stroke-width="2" stroke-linejoin="round"></path>
                                                <circle cx="12" cy="9" r="2.5" stroke="currentColor" stroke-width="2"></circle>
                                            </svg>
                                            ${upcomingExamLocation}
                                        </div>
                                    </div>
                                    <a href="${pageContext.request.contextPath}/registrant/my-exams/detail?examId=${upcomingExamId}" class="upcoming-exam-card__btn" id="btn-view-exam-detail">
                                        <svg width="15" height="15" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                                            <path d="M1 12S5 4 12 4s11 8 11 8-4 8-11 8S1 12 1 12z" stroke="currentColor" stroke-width="2"></path>
                                            <circle cx="12" cy="12" r="3" stroke="currentColor" stroke-width="2"></circle>
                                        </svg>
                                        Xem chi tiết
                                    </a>
                                </div>
                            </c:when>
                            <c:otherwise>
                                <p style="color:#64748b;text-align:center;padding:1.5rem 0;">
                                    Không có đợt thi sắp tới.
                                </p>
                            </c:otherwise>
                        </c:choose>
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
                            <a href="${pageContext.request.contextPath}/registrant/profile" class="quick-link-card" id="ql-profile">
                                <div class="quick-link-card__icon" aria-hidden="true">
                                    <svg width="18" height="18" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                                        <circle cx="12" cy="8" r="4" stroke="currentColor" stroke-width="1.8"></circle>
                                        <path d="M4 20C4 16.686 7.582 14 12 14C16.418 14 20 16.686 20 20" stroke="currentColor" stroke-width="1.8" stroke-linecap="round"></path>
                                    </svg>
                                </div>
                                <span class="quick-link-card__label">Hồ sơ cá nhân</span>
                                <span class="quick-link-card__sub">Thông tin cá nhân</span>
                            </a>
                            <a href="${pageContext.request.contextPath}/registrant/register-exam" class="quick-link-card" id="ql-register">
                                <div class="quick-link-card__icon" aria-hidden="true">
                                    <svg width="18" height="18" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                                        <rect x="3" y="4" width="18" height="16" rx="2" stroke="currentColor" stroke-width="1.8"></rect>
                                        <path d="M16 2v4M8 2v4M3 10h18" stroke="currentColor" stroke-width="1.8" stroke-linecap="round"></path>
                                    </svg>
                                </div>
                                <span class="quick-link-card__label">Đăng ký thi</span>
                                <span class="quick-link-card__sub">Đăng ký đợt thi mới</span>
                            </a>
                            <a href="${pageContext.request.contextPath}/registrant/my-exams" class="quick-link-card" id="ql-my-exams">
                                <div class="quick-link-card__icon" aria-hidden="true">
                                    <svg width="18" height="18" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                                        <rect x="5" y="3" width="14" height="18" rx="2" stroke="currentColor" stroke-width="1.8"></rect>
                                        <path d="M9 8h6M9 12h6M9 16h4" stroke="currentColor" stroke-width="1.8" stroke-linecap="round"></path>
                                    </svg>
                                </div>
                                <span class="quick-link-card__label">Đợt thi của tôi</span>
                                <span class="quick-link-card__sub">Lịch thi & phòng thi</span>
                            </a>
                            <a href="${pageContext.request.contextPath}/registrant/my-exams" class="quick-link-card" id="ql-scores">
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
                    <c:choose>
                        <c:when test="${not empty activityList}">
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
                        </c:when>
                        <c:otherwise>
                            <p style="color:#64748b;text-align:center;padding:1rem 0;">
                                Chưa có hoạt động nào gần đây.
                            </p>
                        </c:otherwise>
                    </c:choose>
                </div>
            </div>
        </div>

    </main>

    <%-- Footer --%>
    <jsp:include page="/views/layout/footer.jsp" />
</div>

</body>
</html>

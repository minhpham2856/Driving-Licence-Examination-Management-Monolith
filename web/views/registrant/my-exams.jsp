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
        <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/style.css?v=<%= System.currentTimeMillis() %>">
        <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/layout.css?v=<%= System.currentTimeMillis() %>">
    </head>
    <body class="has-side-nav-bar">

        <%-- Inject registrant sidebar --%>
        <jsp:include page="/views/layout/sidebar-registrant.jsp">
            <jsp:param name="activeSidebar" value="exam-schedule" />
        </jsp:include>

        <div class="dashboard-shell">
            <main class="main-content" id="main-content">

                <%-- Breadcrumbs --%>
                <nav class="breadcrumbs" aria-label="Breadcrumb">
                    <a href="${pageContext.request.contextPath}/registrant/dashboard">Trang chủ</a>
                    <span class="breadcrumbs__separator" aria-hidden="true">/</span>
                    <span class="breadcrumbs__current" aria-current="page">Lịch thi & kết quả</span>
                </nav>

                <%-- Page Header --%>
                <header class="page-header">
                    <div class="page-title-wrap">
                        <h1 class="page-title">Lịch thi & Kết quả</h1>
                        <p class="page-subtitle">Quản lý các kỳ thi đã đăng ký và theo dõi điểm số chi tiết của bạn.</p>
                    </div>
                    <div class="page-header-actions">
                        <a href="${pageContext.request.contextPath}/registrant/register-exam" class="btn-header-primary" id="btn-register-new-exam">
                            <svg width="16" height="16" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                            <line x1="12" y1="5" x2="12" y2="19" stroke="currentColor" stroke-width="2.5" stroke-linecap="round"/>
                            <line x1="5" y1="12" x2="19" y2="12" stroke="currentColor" stroke-width="2.5" stroke-linecap="round"/>
                            </svg>
                            Đăng ký thi mới
                        </a>
                    </div>
                </header>

                <c:if test="${not empty successMessage}">
                    <section class="p-alert-banner" aria-label="Thông báo thành công" style="margin-bottom:1.25rem;background:#f0fdf4;border-color:#86efac;color:#166534;">
                        <svg width="20" height="20" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                        <path d="M22 11.08V12a10 10 0 1 1-5.93-9.14" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                        <polyline points="22 4 12 14.01 9 11.01" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                        </svg>
                        <div class="p-alert-banner__content">
                            <span class="p-alert-banner__title">Đăng ký thành công</span>
                            <span><c:out value="${successMessage}"/></span>
                        </div>
                    </section>
                </c:if>

                <c:if test="${not empty errorMessage}">
                    <section class="p-alert-banner" aria-label="Thông báo lỗi" style="margin-bottom:1.25rem;background:#fef2f2;border-color:#fecaca;color:#991b1b;">
                        <div class="p-alert-banner__content">
                            <span class="p-alert-banner__title">Không thể xử lý</span>
                            <span><c:out value="${errorMessage}"/></span>
                        </div>
                    </section>
                </c:if>

                <section class="my-exams-stats-row" aria-label="Thống kê kỳ thi">
                    <div class="my-exams-stat-card">
                        <div class="my-exams-stat-card__icon-wrap my-exams-stat-card__icon-wrap--blue" aria-hidden="true">
                            <svg width="22" height="22" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                            <path d="M12 2L2 7l10 5 10-5-10-5z" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                            <path d="M2 17l10 5 10-5M2 12l10 5 10-5" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                            </svg>
                        </div>
                        <div class="my-exams-stat-card__details">
                            <span class="my-exams-stat-card__label">Tổng số kỳ thi</span>
                            <span class="my-exams-stat-card__value">${totalExamCount != null ? totalExamCount : 0}</span>
                        </div>
                    </div>
                    <div class="my-exams-stat-card">
                        <div class="my-exams-stat-card__icon-wrap my-exams-stat-card__icon-wrap--green" aria-hidden="true">
                            <svg width="22" height="22" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                            <circle cx="12" cy="12" r="10" stroke="currentColor" stroke-width="2"/>
                            <path d="M8 12l3 3 5-5" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                            </svg>
                        </div>
                        <div class="my-exams-stat-card__details">
                            <span class="my-exams-stat-card__label">Số kỳ thi đạt</span>
                            <span class="my-exams-stat-card__value">${passedExamCount != null ? passedExamCount : 0}</span>
                        </div>
                    </div>
                    <div class="my-exams-stat-card">
                        <div class="my-exams-stat-card__icon-wrap my-exams-stat-card__icon-wrap--orange" aria-hidden="true">
                            <svg width="22" height="22" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                            <rect x="3" y="4" width="18" height="16" rx="2" stroke="currentColor" stroke-width="2"/>
                            <path d="M16 2v4M8 2v4M3 10h18" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
                            </svg>
                        </div>
                        <div class="my-exams-stat-card__details">
                            <span class="my-exams-stat-card__label">Sắp diễn ra</span>
                            <span class="my-exams-stat-card__value">${upcomingExamCount != null ? upcomingExamCount : 0}</span>
                        </div>
                    </div>
                </section>

                <c:set var="selectedExamId" value="${empty selectedExamId ? '' : selectedExamId}" />

                <jsp:include page="/views/registrant/partials/exam-list-filter-form.jsp">
                    <jsp:param name="filterAction" value="${pageContext.request.contextPath}/registrant/my-exams"/>
                    <jsp:param name="clearUrl" value="${pageContext.request.contextPath}/registrant/my-exams"/>
                </jsp:include>

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
                            <c:if test="${not empty myExamList}">
                                <c:forEach var="exam" items="${myExamList}">
                                    <tr class="${not empty selectedExam and selectedExam.candidateId eq exam.candidateId ? 'my-exams-table__row--selected' : ''}${exam.sbdPending ? ' my-exams-table__row--pending' : ''}">
                                        <td>
                                            <span class="my-exams-table__title">${exam.examTitle}</span>
                                            <span class="my-exams-table__subtitle">
                                                <fmt:formatDate value="${exam.examDate}" pattern="dd/MM/yyyy"/>
                                                <c:if test="${exam.sessionTimePublished}">
                                                    · ${not empty exam.sessionTimeDisplay ? exam.sessionTimeDisplay : ''}
                                                    <c:if test="${empty exam.sessionTimeDisplay}">
                                                        <fmt:formatDate value="${exam.sessionStart}" pattern="HH:mm"/>
                                                    </c:if>
                                                </c:if>
                                            </span>
                                        </td>
                                        <td><span class="my-exams-table__licence-badge">${exam.licenceClass}</span></td>
                                        <td><span class="my-exams-table__sbd${exam.sbdPending ? ' my-exams-table__sbd--pending' : ''}">${exam.sbdDisplay}</span></td>
                                        <td>${exam.roomName}</td>
                                        <td><span class="status-badge status-badge--${exam.statusClass}">${exam.statusLabel}</span></td>
                                        <td>
                                            <c:choose>
                                                <c:when test="${not empty selectedExam and selectedExam.candidateId eq exam.candidateId}">
                                                    <a href="${pageContext.request.contextPath}/registrant/my-exams"
                                                       class="my-exams-table__action-btn my-exams-table__action-btn--active"
                                                       aria-expanded="true">Thu gọn</a>
                                                </c:when>
                                                <c:otherwise>
                                                    <a href="${pageContext.request.contextPath}/registrant/my-exams?examId=${exam.candidateId}<c:if test='${not empty searchQuery}'>&amp;q=${fn:escapeXml(searchQuery)}</c:if><c:if test='${statusFilter ne "all"}'>&amp;status=${fn:escapeXml(statusFilter)}</c:if><c:if test='${licenceFilter ne "all"}'>&amp;licence=${fn:escapeXml(licenceFilter)}</c:if>#exam-details"
                                                       class="my-exams-table__action-btn"
                                                       aria-expanded="false">Chi tiết</a>
                                                </c:otherwise>
                                            </c:choose>
                                        </td>
                                    </tr>
                                </c:forEach>
                            </c:if>
                            <c:if test="${empty myExamList}">
                                <tr>
                                    <td colspan="6" style="text-align:center;padding:2rem;color:#64748b;">
                                        <c:choose>
                                            <c:when test="${searchActive}">Không có kỳ thi phù hợp với bộ lọc.</c:when>
                                            <c:otherwise>
                                                Chưa có nguyện vọng hoặc kỳ thi chính thức nào.
                                                <a href="${pageContext.request.contextPath}/registrant/register-exam">Đăng ký nguyện vọng</a>
                                            </c:otherwise>
                                        </c:choose>
                                    </td>
                                </tr>
                            </c:if>
                        </tbody>
                    </table>
                </section>

                <section id="exam-details" class="exam-details-section" aria-label="Chi tiết kỳ thi sát hạch">
                    <h2 class="section-title-premium">
                        <svg width="20" height="20" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg" class="section-title-premium__icon" aria-hidden="true">
                        <circle cx="12" cy="12" r="10" stroke="currentColor" stroke-width="2"/>
                        <path d="M12 16v-4M12 8h.01" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                        </svg>
                        Chi tiết kỳ thi & kết quả sát hạch
                    </h2>

                    <c:choose>
                        <c:when test="${not empty selectedExam}">
                            <div class="exam-details-grid">
                                <div class="exam-details-card exam-details-card--left">
                                    <div class="exam-details-card__header">
                                        <h3 class="exam-details-card__title">Thông tin kỳ thi</h3>
                                        <span class="status-badge status-badge--${selectedExam.statusClass}">${selectedExam.statusLabel}</span>
                                    </div>
                                    <div class="exam-details-card__body">
                                        <div class="ticket-meta-grid">
                                            <div class="ticket-meta-item">
                                                <span class="ticket-meta-label">Tên kỳ thi</span>
                                                <span class="ticket-meta-value">${selectedExam.examTitle}</span>
                                            </div>
                                            <div class="ticket-meta-item">
                                                <span class="ticket-meta-label">Hạng GPLX</span>
                                                <span class="ticket-meta-value ticket-meta-value--badge">Hạng ${selectedExam.licenceClass}</span>
                                            </div>
                                            <div class="ticket-meta-item">
                                                <span class="ticket-meta-label">
                                                    <c:choose>
                                                        <c:when test="${selectedExam.preferredDate}">Ngày nguyện vọng</c:when>
                                                        <c:otherwise>Ngày thi</c:otherwise>
                                                    </c:choose>
                                                </span>
                                                <span class="ticket-meta-value">
                                                    <fmt:formatDate value="${selectedExam.examDate}" pattern="dd/MM/yyyy"/>
                                                </span>
                                            </div>
                                            <div class="ticket-meta-item">
                                                <span class="ticket-meta-label">Giờ kỳ thi</span>
                                                <span class="ticket-meta-value">
                                                    <c:choose>
                                                        <c:when test="${selectedExam.preferredDate}">
                                                            Chờ trung tâm công bố lịch chính thức
                                                        </c:when>
                                                        <c:when test="${selectedExam.sessionTimePublished and not empty selectedExam.sessionTimeDisplay}">
                                                            ${selectedExam.sessionTimeDisplay}
                                                        </c:when>
                                                        <c:when test="${selectedExam.sessionTimePublished}">
                                                            <fmt:formatDate value="${selectedExam.sessionStart}" pattern="HH:mm"/>
                                                        </c:when>
                                                        <c:otherwise>Chờ Ban sát hạch mở kỳ thi</c:otherwise>
                                                    </c:choose>
                                                </span>
                                            </div>
                                            <div class="ticket-meta-item">
                                                <span class="ticket-meta-label">Số báo danh</span>
                                                <span class="ticket-meta-value${selectedExam.sbdPending ? ' ticket-meta-value--sbd-pending' : ''}">${selectedExam.sbdDisplay}</span>
                                            </div>
                                            <div class="ticket-meta-item">
                                                <span class="ticket-meta-label">Phần thi</span>
                                                <span class="ticket-meta-value">${not empty selectedExam.examSectionName ? selectedExam.examSectionName : '-'}</span>
                                            </div>
                                            <div class="ticket-meta-item">
                                                <span class="ticket-meta-label">Phòng thi</span>
                                                <span class="ticket-meta-value">${selectedExam.roomName}</span>
                                            </div>
                                            <div class="ticket-meta-item">
                                                <span class="ticket-meta-label">Kết quả chung</span>
                                                <span class="ticket-meta-value">${not empty selectedExam.overallResultLabel ? selectedExam.overallResultLabel : '-'}</span>
                                            </div>
                                        </div>
                                    </div>
                                </div>

                                <c:if test="${selectedExam.preferredDate}">
                                    <div class="exam-details-card" style="margin-top:1rem;">
                                        <div class="exam-details-card__body">
                                            <c:choose>
                                                <c:when test="${selectedExam.preferredCancelled}">
                                                    <p class="placeholder-text" style="margin:0;">
                                                        Ngày thi nguyện vọng này <strong>đã bị hủy</strong>.
                                                        <c:choose>
                                                            <c:when test="${not empty selectedExam.cancelReason}">
                                                                Lý do: <strong><c:out value="${selectedExam.cancelReason}"/></strong>.
                                                            </c:when>
                                                            <c:otherwise>
                                                                Không có lý do chi tiết trên hệ thống.
                                                            </c:otherwise>
                                                        </c:choose>
                                                        Bạn có thể chọn ngày thi dự kiến khác đang mở tại
                                                        <a href="${pageContext.request.contextPath}/registrant/register-exam">Đăng ký thi</a>.
                                                    </p>
                                                </c:when>
                                                <c:otherwise>
                                                    <p class="placeholder-text" style="margin:0;">
                                                        Đây là <strong>ngày thi nguyện vọng</strong> bạn đã đăng ký.
                                                        Lịch thi chính thức, số báo danh và kết quả sát hạch sẽ được cập nhật sau khi
                                                        trung tâm công bố. Trạng thái hiện tại:
                                                        <strong><c:out value="${selectedExam.statusLabel}"/></strong>.
                                                    </p>
                                                </c:otherwise>
                                            </c:choose>
                                        </div>
                                    </div>
                                </c:if>

                                <c:if test="${not selectedExam.preferredDate}">
                                <div class="exam-details-cards-wrap">
                                    <div class="exam-details-card exam-details-card--right">
                                        <div class="exam-details-card__header">
                                            <h3 class="exam-details-card__title">Sát hạch Lý thuyết</h3>
                                            <c:if test="${not empty selectedExam.theoryResultLabel}">
                                                <span class="registrant-score-badge registrant-score-badge--${selectedExam.theoryPassBadgeClass}">${selectedExam.theoryResultLabel}</span>
                                            </c:if>
                                        </div>
                                        <div class="exam-details-card__body">
                                            <c:choose>
                                                <c:when test="${selectedExam.theoryScore != null}">
                                                    <div class="registrant-score-card registrant-score-card--${selectedExam.theoryPassBadgeClass}">
                                                        <div class="registrant-score-card__hero">
                                                            <span class="registrant-score-card__value">${selectedExam.theoryScoreDisplay}</span>
                                                        </div>
                                                        <p class="registrant-score-card__detail">${selectedExam.theoryScoreDetail}</p>
                                                        <ul class="registrant-score-card__stats registrant-score-card__stats--detailed" aria-label="Chi tiết điểm lý thuyết">
                                                            <li>
                                                                <span class="registrant-score-card__stat-label">Câu đúng</span>
                                                                <strong class="registrant-score-card__stat-value">${selectedExam.theoryScoreDisplay}</strong>
                                                            </li>
                                                            <li>
                                                                <span class="registrant-score-card__stat-label">Câu sai</span>
                                                                <strong class="registrant-score-card__stat-value">${selectedExam.theoryWrongCount}</strong>
                                                            </li>
                                                            <li>
                                                                <span class="registrant-score-card__stat-label">Điểm chuẩn</span>
                                                                <strong class="registrant-score-card__stat-value">21/25</strong>
                                                            </li>
                                                            <li>
                                                                <span class="registrant-score-card__stat-label">Kết quả</span>
                                                                <strong class="registrant-score-card__stat-value registrant-score-card__stat-value--${selectedExam.theoryPassBadgeClass}">${selectedExam.theoryResultLabel}</strong>
                                                            </li>
                                                        </ul>
                                                    </div>
                                                </c:when>
                                                <c:otherwise>
                                                    <p class="placeholder-text" style="margin:0;">Chưa có kết quả lý thuyết.</p>
                                                </c:otherwise>
                                            </c:choose>
                                        </div>
                                    </div>

                                    <div class="exam-details-card exam-details-card--right">
                                        <div class="exam-details-card__header">
                                            <h3 class="exam-details-card__title">Sát hạch Thực hành</h3>
                                            <c:if test="${not empty selectedExam.practicalResultLabel}">
                                                <span class="registrant-score-badge registrant-score-badge--${selectedExam.practicalPassBadgeClass}">${selectedExam.practicalResultLabel}</span>
                                            </c:if>
                                        </div>
                                        <div class="exam-details-card__body">
                                            <c:choose>
                                                <c:when test="${selectedExam.practicalScore != null}">
                                                    <div class="registrant-score-card registrant-score-card--${selectedExam.practicalPassBadgeClass}">
                                                        <div class="registrant-score-card__hero">
                                                            <span class="registrant-score-card__value">${selectedExam.practicalScoreDisplay}</span>
                                                        </div>
                                                        <p class="registrant-score-card__detail">${selectedExam.practicalScoreDetail}</p>
                                                        <ul class="registrant-score-card__stats registrant-score-card__stats--detailed" aria-label="Chi tiết điểm thực hành">
                                                            <li>
                                                                <span class="registrant-score-card__stat-label">Điểm còn lại</span>
                                                                <strong class="registrant-score-card__stat-value">${selectedExam.practicalScoreDisplay}</strong>
                                                            </li>
                                                            <li>
                                                                <span class="registrant-score-card__stat-label">Điểm chuẩn</span>
                                                                <strong class="registrant-score-card__stat-value">80/100</strong>
                                                            </li>
                                                            <li>
                                                                <span class="registrant-score-card__stat-label">Kết quả</span>
                                                                <strong class="registrant-score-card__stat-value registrant-score-card__stat-value--${selectedExam.practicalPassBadgeClass}">${selectedExam.practicalResultLabel}</strong>
                                                            </li>
                                                        </ul>
                                                    </div>
                                                </c:when>
                                                <c:when test="${selectedExam.theoryResultLabel eq 'Trượt'}">
                                                    <p class="placeholder-text" style="margin:0;">
                                                        Không đủ điều kiện thi thực hành vì chưa đạt phần lý thuyết.
                                                    </p>
                                                </c:when>
                                                <c:otherwise>
                                                    <p class="placeholder-text" style="margin:0;">Chưa có kết quả thực hành.</p>
                                                </c:otherwise>
                                            </c:choose>
                                        </div>
                                    </div>

                                    <c:if test="${selectedExam.roadScore != null}">
                                        <div class="exam-details-card exam-details-card--right">
                                            <div class="exam-details-card__header">
                                                <h3 class="exam-details-card__title">Sát hạch Đường trường</h3>
                                                <c:if test="${not empty selectedExam.roadPassBadgeClass}">
                                                    <span class="registrant-score-badge registrant-score-badge--${selectedExam.roadPassBadgeClass}">${selectedExam.roadPassBadgeClass eq 'passed' ? 'Đạt' : 'Trượt'}</span>
                                                </c:if>
                                            </div>
                                            <div class="exam-details-card__body">
                                                <div class="registrant-score-card registrant-score-card--${selectedExam.roadPassBadgeClass}">
                                                    <div class="registrant-score-card__hero">
                                                        <span class="registrant-score-card__value">${selectedExam.roadScoreDisplay}</span>
                                                    </div>
                                                    <p class="registrant-score-card__detail">${selectedExam.roadScoreDetail}</p>
                                                    <ul class="registrant-score-card__stats registrant-score-card__stats--detailed">
                                                        <li>
                                                            <span class="registrant-score-card__stat-label">Điểm còn lại</span>
                                                            <strong class="registrant-score-card__stat-value">${selectedExam.roadScoreDisplay}</strong>
                                                        </li>
                                                        <li>
                                                            <span class="registrant-score-card__stat-label">Điểm chuẩn</span>
                                                            <strong class="registrant-score-card__stat-value">80/100</strong>
                                                        </li>
                                                        <li>
                                                            <span class="registrant-score-card__stat-label">Kết quả</span>
                                                            <strong class="registrant-score-card__stat-value registrant-score-card__stat-value--${selectedExam.roadPassBadgeClass}">${selectedExam.roadPassBadgeClass eq 'passed' ? 'Đạt' : 'Trượt'}</strong>
                                                        </li>
                                                    </ul>
                                                </div>
                                            </div>
                                        </div>
                                    </c:if>
                                </div>
                                </c:if>

                            </div>
                        </c:when>
                        <c:when test="${not empty myExamList}">
                            <p style="color:#64748b;margin:0;">Chọn một kỳ thi trong bảng trên để xem chi tiết.</p>
                        </c:when>
                        <c:otherwise>
                            <p style="color:#64748b;margin:0;">Chưa có kỳ thi để hiển thị chi tiết.</p>
                        </c:otherwise>
                    </c:choose>
                </section>

            </main>
        </div>

    </body>
</html>

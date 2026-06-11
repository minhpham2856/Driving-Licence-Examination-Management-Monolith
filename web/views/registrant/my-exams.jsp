<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>

<c:set var="ctx" value="${pageContext.request.contextPath}" />

<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Lịch Thi & Kết Quả Sát Hạch - Lái Vui</title>
    <link rel="stylesheet" href="${ctx}/assets/css/style.css">
    <link rel="stylesheet" href="${ctx}/assets/css/layout.css">
</head>
<body class="has-side-nav-bar">

<jsp:include page="/views/layout/sidebar-registrant.jsp">
    <jsp:param name="activeSidebar" value="exam-schedule" />
</jsp:include>

<div class="dashboard-shell">
    <main class="main-content" id="main-content">

        <nav class="breadcrumbs" aria-label="Breadcrumb">
            <a href="${ctx}/registrant/dashboard">Trang chủ</a>
            <span class="breadcrumbs__separator" aria-hidden="true">/</span>
            <span class="breadcrumbs__current" aria-current="page">Lịch thi & kết quả</span>
        </nav>

        <header class="page-header">
            <div class="page-title-wrap">
                <h1 class="page-title">Lịch thi & Kết quả</h1>
                <p class="page-subtitle">Quản lý các kỳ thi đã đăng ký và theo dõi điểm số chi tiết của bạn.</p>
            </div>
            <div class="page-header-actions">
                <a href="${ctx}/registrant/register-exam" class="btn-header-primary">Đăng ký thi mới</a>
            </div>
        </header>

        <c:if test="${not empty success}">
            <section class="p-alert-banner" aria-label="Thông báo">
                <div class="p-alert-banner__content">
                    <span class="p-alert-banner__title">Thành công</span>
                    <span>${success}</span>
                </div>
            </section>
        </c:if>
        <c:if test="${not empty errorMessage}">
            <section class="p-alert-banner" aria-label="Thông báo lỗi">
                <div class="p-alert-banner__content">
                    <span class="p-alert-banner__title">Thông báo</span>
                    <span>${errorMessage}</span>
                </div>
            </section>
        </c:if>

        <section class="my-exams-stats-row" aria-label="Thống kê kỳ thi">
            <div class="my-exams-stat-card">
                <div class="my-exams-stat-card__details">
                    <span class="my-exams-stat-card__label">Tổng số kỳ thi</span>
                    <span class="my-exams-stat-card__value">${totalExamsCount}</span>
                </div>
            </div>
            <div class="my-exams-stat-card">
                <div class="my-exams-stat-card__details">
                    <span class="my-exams-stat-card__label">Sắp diễn ra</span>
                    <span class="my-exams-stat-card__value">${upcomingExamsCount}</span>
                </div>
            </div>
            <div class="my-exams-stat-card">
                <div class="my-exams-stat-card__details">
                    <span class="my-exams-stat-card__label">Đã đạt</span>
                    <span class="my-exams-stat-card__value">${passedExamsCount}</span>
                </div>
            </div>
        </section>

        <form method="get" action="${ctx}/registrant/my-exams" class="p-form-card" style="margin-bottom:1rem;padding:1rem;" aria-label="Lọc lịch thi">
            <div style="display:flex;flex-wrap:wrap;gap:0.75rem;align-items:flex-end;">
                <div>
                    <label for="filterStatus" style="display:block;font-size:0.8rem;margin-bottom:0.25rem;">Trạng thái</label>
                    <select id="filterStatus" name="status" class="input-field">
                        <option value="all" ${filterStatus eq 'all' ? 'selected' : ''}>Tất cả</option>
                        <option value="upcoming" ${filterStatus eq 'upcoming' ? 'selected' : ''}>Sắp thi</option>
                        <option value="pending_payment" ${filterStatus eq 'pending_payment' ? 'selected' : ''}>Chờ thanh toán</option>
                        <option value="passed" ${filterStatus eq 'passed' ? 'selected' : ''}>Đã đạt</option>
                        <option value="cancelled" ${filterStatus eq 'cancelled' ? 'selected' : ''}>Đã hủy</option>
                    </select>
                </div>
                <div style="flex:1;min-width:200px;">
                    <label for="filterQuery" style="display:block;font-size:0.8rem;margin-bottom:0.25rem;">Tìm đợt thi</label>
                    <input type="search" id="filterQuery" name="q" class="input-field" placeholder="Tên đợt, hạng, SBD..."
                           value="${filterQuery}">
                </div>
                <button type="submit" class="btn-header-primary">Lọc</button>
                <a href="${ctx}/registrant/my-exams" class="welcome-banner__btn welcome-banner__btn--outline">Xóa lọc</a>
            </div>
        </form>

        <section id="exams-table-anchor" class="my-exams-table-section" aria-label="Danh sách kỳ thi">
            <table class="my-exams-table" role="table">
                <thead>
                    <tr>
                        <th scope="col">Kỳ thi</th>
                        <th scope="col">Hạng</th>
                        <th scope="col">SBD</th>
                        <th scope="col">Phòng / Khu vực</th>
                        <th scope="col">Trạng thái</th>
                        <th scope="col">Chi tiết</th>
                    </tr>
                </thead>
                <tbody>
                    <c:forEach var="exam" items="${examRows}">
                        <tr class="${exam.pendingRow ? ' my-exams-table__row--pending' : ''}">
                            <td>
                                <span class="my-exams-table__title">${exam.title}</span>
                                <span class="my-exams-table__subtitle">
                                    <fmt:formatDate value="${exam.examDate}" pattern="dd/MM/yyyy"/>
                                </span>
                            </td>
                            <td><span class="my-exams-table__licence-badge">${exam.licenceCode}</span></td>
                            <td>
                                <span class="my-exams-table__sbd">
                                    <c:choose>
                                        <c:when test="${not empty exam.sbd}">${exam.sbd}</c:when>
                                        <c:otherwise><span class="text-muted">Chưa cấp</span></c:otherwise>
                                    </c:choose>
                                </span>
                            </td>
                            <td>${exam.roomLabel}</td>
                            <td>
                                <span class="status-badge status-badge--${exam.statusClass}">${exam.statusLabel}</span>
                            </td>
                            <td>
                                <a href="${ctx}/registrant/my-exams/detail?examId=${exam.registrationId}"
                                   class="my-exams-table__action-btn">Xem</a>
                            </td>
                        </tr>
                    </c:forEach>
                    <c:if test="${empty examRows}">
                        <tr>
                            <td colspan="6">
                                <c:choose>
                                    <c:when test="${not empty filterQuery or (filterStatus ne 'all')}">
                                        Không có kỳ thi khớp bộ lọc.
                                        <a href="${ctx}/registrant/my-exams">Xem tất cả</a>
                                    </c:when>
                                    <c:otherwise>
                                        Bạn chưa đăng ký kỳ thi nào.
                                        <a href="${ctx}/registrant/register-exam">Đăng ký ngay</a>
                                    </c:otherwise>
                                </c:choose>
                            </td>
                        </tr>
                    </c:if>
                </tbody>
            </table>

            <c:if test="${examListPage.totalPages > 1}">
                <nav class="pagination-bar" style="display:flex;gap:0.5rem;justify-content:flex-end;margin-top:1rem;" aria-label="Phân trang">
                    <c:if test="${examListPage.hasPrevious}">
                        <a href="${ctx}/registrant/my-exams?status=${filterStatus}&amp;q=${filterQuery}&amp;page=${examListPage.page - 1}">Trước</a>
                    </c:if>
                    <span>Trang ${examListPage.page} / ${examListPage.totalPages} (${examListPage.totalItems} kỳ)</span>
                    <c:if test="${examListPage.hasNext}">
                        <a href="${ctx}/registrant/my-exams?status=${filterStatus}&amp;q=${filterQuery}&amp;page=${examListPage.page + 1}">Sau</a>
                    </c:if>
                </nav>
            </c:if>
        </section>

    </main>
    <jsp:include page="/views/layout/footer.jsp" />
</div>
</body>
</html>

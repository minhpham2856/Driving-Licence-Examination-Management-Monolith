<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>

<c:set var="ctx" value="${pageContext.request.contextPath}" />

<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Chi tiết kỳ thi - Lái Vui</title>
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
            <a href="${ctx}/registrant/my-exams">Lịch thi &amp; kết quả</a>
            <span class="breadcrumbs__separator" aria-hidden="true">/</span>
            <span class="breadcrumbs__current" aria-current="page">Chi tiết kỳ thi</span>
        </nav>

        <header class="page-header">
            <div class="page-title-wrap">
                <h1 class="page-title">Chi tiết kỳ thi &amp; kết quả</h1>
                <p class="page-subtitle">Thông tin phòng thi, SBD và điểm sát hạch theo đăng ký #${selectedExamId}.</p>
            </div>
            <div class="page-header-actions">
                <a href="${ctx}/registrant/my-exams" class="btn-header-primary">Quay lại danh sách</a>
            </div>
        </header>

        <c:if test="${not empty detailError}">
            <section class="p-alert-banner" aria-label="Lỗi">
                <div class="p-alert-banner__content">
                    <span class="p-alert-banner__title">Không thể hiển thị</span>
                    <span>${detailError}</span>
                </div>
            </section>
        </c:if>

        <c:if test="${not empty examDetail}">
            <c:if test="${examDetail.paymentPending && not examDetail.cancelled}">
                <section class="p-alert-banner" style="margin-bottom:1rem;" aria-label="Chờ thanh toán">
                    <div class="p-alert-banner__content">
                        <span class="p-alert-banner__title">Chờ thanh toán tại quầy</span>
                        <span>Vui lòng đến quầy thu ngân để nộp lệ phí và hoàn tất thủ tục.</span>
                    </div>
                </section>
            </c:if>
            <c:if test="${examDetail.cancelled}">
                <section class="p-alert-banner" style="margin-bottom:1rem;" aria-label="Đã hủy">
                    <div class="p-alert-banner__content">
                        <span class="p-alert-banner__title">Đăng ký đã hủy</span>
                        <span>Đơn đăng ký không còn hiệu lực. Bạn có thể <a href="${ctx}/registrant/register-exam">đăng ký lại</a>.</span>
                    </div>
                </section>
            </c:if>

            <section class="exam-details-section" aria-label="Chi tiết kỳ thi">
                <div class="exam-details-grid">
                    <div class="exam-details-card exam-details-card--left">
                        <div class="exam-details-card__header">
                            <h3 class="exam-details-card__title">Chi tiết đợt thi &amp; phòng thi</h3>
                        </div>
                        <div class="exam-details-card__body">
                            <div class="exam-details-ticket">
                                <div class="exam-details-ticket__meta">
                                    <div class="ticket-meta-grid">
                                        <div class="ticket-meta-item">
                                            <span class="ticket-meta-label">Hạng GPLX</span>
                                            <span class="ticket-meta-value ticket-meta-value--badge">${examDetail.licenceLabel}</span>
                                        </div>
                                        <div class="ticket-meta-item">
                                            <span class="ticket-meta-label">Mã đợt thi</span>
                                            <span class="ticket-meta-value">${examDetail.sessionCode}</span>
                                        </div>
                                        <div class="ticket-meta-item">
                                            <span class="ticket-meta-label">Ngày thi</span>
                                            <span class="ticket-meta-value"><fmt:formatDate value="${examDetail.examDate}" pattern="dd/MM/yyyy"/></span>
                                        </div>
                                        <div class="ticket-meta-item">
                                            <span class="ticket-meta-label">Giờ tập trung</span>
                                            <span class="ticket-meta-value">${examDetail.gatherTimeLabel}</span>
                                        </div>
                                        <div class="ticket-meta-item">
                                            <span class="ticket-meta-label">Phòng thi</span>
                                            <span class="ticket-meta-value">${examDetail.roomLabel}</span>
                                        </div>
                                        <div class="ticket-meta-item">
                                            <span class="ticket-meta-label">Máy thi</span>
                                            <span class="ticket-meta-value">${examDetail.machineLabel}</span>
                                        </div>
                                    </div>
                                    <c:choose>
                                        <c:when test="${not empty examDetail.sbd}">
                                            <div class="ticket-sbd-block">
                                                <span class="ticket-sbd-label">Số Báo Danh</span>
                                                <span class="ticket-sbd-value">${examDetail.sbd}</span>
                                            </div>
                                        </c:when>
                                        <c:otherwise>
                                            <p class="text-muted" style="margin-top:0.75rem;">SBD chưa được cấp — staff sẽ import danh sách Công an.</p>
                                        </c:otherwise>
                                    </c:choose>
                                </div>
                                <div class="exam-details-ticket__footer">
                                    <div class="ticket-qr-text">
                                        <c:choose>
                                            <c:when test="${examDetail.qrAvailable}">
                                                <span class="qr-text-primary">Mã check-in sẵn sàng sau khi thanh toán tại quầy</span>
                                            </c:when>
                                            <c:otherwise>
                                                <span class="qr-text-primary text-muted">Mã check-in chưa khả dụng</span>
                                            </c:otherwise>
                                        </c:choose>
                                    </div>
                                </div>
                            </div>
                        </div>
                    </div>

                    <div class="exam-details-cards-wrap">
                        <c:forEach var="section" items="${examDetail.scoreSections}">
                            <div class="exam-details-card exam-details-card--right">
                                <div class="exam-details-card__header">
                                    <h3 class="exam-details-card__title">${section.sectionTitle}</h3>
                                    <span class="details-badge details-badge--${section.badgeClass}">${section.statusLabel}</span>
                                </div>
                                <div class="exam-details-card__body">
                                    <c:choose>
                                        <c:when test="${section.showPlaceholder}">
                                            <div class="scorecard-placeholder">
                                                <p class="placeholder-text">${section.placeholderText}</p>
                                            </div>
                                        </c:when>
                                        <c:otherwise>
                                            <div class="score-display">
                                                <span class="score-display__value score-display__value--${section.statusClass}">${section.finalScore}</span>
                                                <span class="score-display__label">${section.statusLabel}</span>
                                            </div>
                                        </c:otherwise>
                                    </c:choose>
                                </div>
                            </div>
                        </c:forEach>
                        <c:if test="${empty examDetail.scoreSections}">
                            <div class="exam-details-card exam-details-card--right">
                                <div class="exam-details-card__body">
                                    <p class="placeholder-text">Chưa có kết quả thi cho đợt đăng ký này.</p>
                                </div>
                            </div>
                        </c:if>
                    </div>
                </div>
            </section>
        </c:if>

    </main>
    <jsp:include page="/views/layout/footer.jsp" />
</div>
</body>
</html>

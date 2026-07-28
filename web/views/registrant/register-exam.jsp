<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>

<c:set var="selectedClassCode" value="${not empty param.licenceSelect ? param.licenceSelect : selectedLicenceCode}" />
<c:set var="selectedSessionCode" value="${not empty param.sessionSelect ? param.sessionSelect : selectedSessionCode}" />
<c:set var="ctx" value="${pageContext.request.contextPath}" />

<!DOCTYPE html>
<html lang="vi">
    <head>
        <meta charset="UTF-8">
        <meta name="viewport" content="width=device-width, initial-scale=1.0">
        <title>Đăng Ký Đợt Thi Mới - Lái Vui</title>
        <meta name="description" content="Lựa chọn hạng bằng lái, chọn lịch thi khả dụng và hoàn tất thủ tục đăng ký thi sát hạch lái xe tại Lái Vui.">

        <!-- Google Fonts -->
        <link rel="preconnect" href="https://fonts.googleapis.com">
        <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
        <link href="https://fonts.googleapis.com/css2?family=Be+Vietnam+Pro:wght@400;500;600;700;800&family=Inter:wght@400;500;600;700;800&display=swap" rel="stylesheet">

        <!-- Layout Stylesheets -->
        <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/style.css">
        <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/layout.css?v=<%= System.currentTimeMillis() %>">
    </head>
    <body class="has-side-nav-bar">

        <%-- Inject registrant sidebar --%>
        <jsp:include page="/views/layout/sidebar-registrant.jsp">
            <jsp:param name="activeSidebar" value="register-exam" />
        </jsp:include>

        <div class="dashboard-shell">
            <main class="main-content" id="main-content">

                <%-- Breadcrumbs --%>
                <nav class="breadcrumbs" aria-label="Breadcrumb">
                    <a href="${pageContext.request.contextPath}/registrant/dashboard">Trang chủ</a>
                    <span class="breadcrumbs__separator" aria-hidden="true">/</span>
                    <span class="breadcrumbs__current" aria-current="page">Đăng ký thi</span>
                </nav>

                <%-- Page Header --%>
                <header class="page-header">
                    <div class="page-title-wrap">
                        <h1 class="page-title">Đăng ký ngày thi dự kiến</h1>
                        <p class="page-subtitle">Chọn hạng bằng đã được duyệt và ngày thi dự kiến do trung tâm công bố</p>
                    </div>
                </header>

                <%-- Step Wizard Indicator --%>
                <section class="step-wizard" aria-label="Tiến trình đăng ký">
                    <div class="step-item step-item--active">
                        <div class="step-number">1</div>
                        <span>Chọn hạng & Lịch thi</span>
                    </div>
                    <div class="step-line"></div>
                    <div class="step-item">
                        <div class="step-number">2</div>
                        <span>Xác nhận đăng ký</span>
                    </div>
                </section>

                <c:if test="${not empty error}">
                    <section class="p-alert-banner" aria-label="Thông báo lỗi" style="margin-bottom:1.25rem;">
                        <svg width="20" height="20" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                        <circle cx="12" cy="12" r="10" stroke="currentColor" stroke-width="2"></circle>
                        <path d="M12 8v5M12 16h.01" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"></path>
                        </svg>
                        <div class="p-alert-banner__content">
                            <span class="p-alert-banner__title">Không thể đăng ký</span>
                            <span><c:out value="${error}"/></span>
                        </div>
                    </section>
                </c:if>

                <c:if test="${not empty documentGateMessage}">
                    <section class="p-alert-banner" aria-label="Yêu cầu duyệt hồ sơ" style="margin-bottom:1.25rem;">
                        <svg width="20" height="20" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                        <circle cx="12" cy="12" r="10" stroke="currentColor" stroke-width="2"></circle>
                        <path d="M12 8v5M12 16h.01" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"></path>
                        </svg>
                        <div class="p-alert-banner__content">
                            <span class="p-alert-banner__title">Chưa đủ điều kiện đăng ký thi</span>
                            <span>
                                <c:out value="${documentGateMessage}"/>
                                <a href="${pageContext.request.contextPath}/registrant/upload-documents" class="profile-checklist-link"> Quản lý tài liệu →</a>
                            </span>
                        </div>
                    </section>
                </c:if>

                <c:if test="${showProfileApprovedNotice}">
                    <section class="p-alert-banner register-exam-notice register-exam-notice--approved" aria-label="Hồ sơ đã duyệt" style="margin-bottom:1.25rem;">
                        <svg width="20" height="20" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg" aria-hidden="true">
                        <path d="M22 11.08V12a10 10 0 1 1-5.93-9.14" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                        <polyline points="22 4 12 14.01 9 11.01" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                        </svg>
                        <div class="p-alert-banner__content">
                            <span class="p-alert-banner__title">Hồ sơ tài liệu đã được duyệt</span>
                            <span>
                                Bạn đã nộp đủ giấy tờ bắt buộc và được ban quản lý phê duyệt
                                (<c:out value="${profileRegistrationStatusLabel}"/>).
                                <c:choose>
                                    <c:when test="${not hasOtherDocuments}">
                                        Hồ sơ đã duyệt chỉ mở đăng ký cho <strong>hạng đã chọn khi gửi yêu cầu duyệt</strong>.
                                        Muốn thi hạng khác: vào
                                        <a href="${pageContext.request.contextPath}/registrant/upload-documents" class="profile-checklist-link">Quản lý tài liệu</a>,
                                        chọn hạng mới khi gửi duyệt (có thể tái sử dụng 4 giấy đã có nếu không cần đổi).
                                    </c:when>
                                    <c:otherwise>
                                        Chỉ các hạng đã được ban quản lý duyệt kèm hồ sơ mới chọn được.
                                        Chọn hạng đã duyệt và ngày thi dự kiến, rồi xác nhận đăng ký bên dưới.
                                    </c:otherwise>
                                </c:choose>
                            </span>
                        </div>
                    </section>
                </c:if>

                <c:if test="${not empty licenceGateMessage}">
                    <section id="register-exam-licence-notice" class="p-alert-banner register-exam-notice register-exam-notice--licence" aria-label="Hạn chế hạng GPLX" style="margin-bottom:1.25rem;">
                        <svg width="20" height="20" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                        <circle cx="12" cy="12" r="10" stroke="currentColor" stroke-width="2"></circle>
                        <path d="M12 8v5M12 16h.01" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"></path>
                        </svg>
                        <div class="p-alert-banner__content">
                            <span class="p-alert-banner__title">Không đủ điều kiện cho hạng đã chọn</span>
                            <span>
                                <c:out value="${licenceGateMessage}"/>
                                <a href="${pageContext.request.contextPath}/registrant/upload-documents" class="profile-checklist-link"> Quản lý tài liệu →</a>
                            </span>
                        </div>
                    </section>
                </c:if>

                <%-- Chọn hạng / ca: GET (không lồng form POST). Xác nhận: form POST riêng bên phải. --%>
                <div class="profile-layout-grid">

                    <%-- Left Column: Licence Selector + Available Sessions --%>
                    <div class="dashboard-sidebar-column dashboard-sidebar-column--wide">

                        <%-- Section 1: Select Licence --%>
                        <section class="p-form-card" id="register-exam-licence" aria-label="Chọn hạng GPLX">
                            <div class="p-form-header">
                                <h2 class="p-form-title">
                                    <svg width="20" height="20" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                                    <circle cx="12" cy="12" r="10" stroke="currentColor" stroke-width="2"></circle>
                                    <path d="M12 8v8M8 12h8" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"></path>
                                    </svg>
                                    1. Chọn hạng bằng lái đăng ký
                                </h2>
                            </div>
                            <div class="p-form-body p-form-body--compact">
                                <div class="licence-selector-grid">

                                    <c:if test="${not empty licenceClassesList}">
                                        <c:forEach var="licence" items="${licenceClassesList}">
                                            <c:set var="licenceDocsAllowed" value="${licenceDocumentAllowed[licence.code]}" />
                                            <%-- Hiện card luôn; khóa + hint nếu chưa duyệt hồ sơ kèm hạng này --%>
                                            <c:set var="licencePickBlocked" value="${licenceDocsAllowed ne true}" />
                                            <c:url var="licencePickUrl" value="/registrant/register-exam">
                                                <c:param name="licenceSelect" value="${licence.code}"/>
                                                <c:if test="${not empty searchQuery}"><c:param name="q" value="${searchQuery}"/></c:if>
                                                <c:if test="${locationFilter ne 'all'}"><c:param name="location" value="${locationFilter}"/></c:if>
                                                <c:if test="${not empty fromDateIso}"><c:param name="fromDate" value="${fromDateIso}"/></c:if>
                                                <c:if test="${not empty toDateIso}"><c:param name="toDate" value="${toDateIso}"/></c:if>
                                            </c:url>
                                            <c:set var="licencePickFragment" value="${licencePickBlocked ? '#register-exam-licence-notice' : '#register-exam-session'}" />
                                            <%-- Không gắn --active khi bị khóa: 3 thẻ restricted phải cùng style (không highlight vàng) --%>
                                            <c:set var="licenceCardClasses" value="licence-card ${selectedClassCode eq licence.code and not licencePickBlocked ? 'licence-card--active' : ''}${licencePickBlocked ? ' licence-card--restricted' : ''}" />
                                            <c:choose>
                                                <c:when test="${licencePickBlocked}">
                                                    <div class="licence-card-link licence-card-link--blocked"
                                                         title="${not empty licenceDocumentBlockMessages[licence.code] ? licenceDocumentBlockMessages[licence.code] : documentGateMessage}"
                                                         aria-disabled="true">
                                                        <div class="${licenceCardClasses}">
                                                            <div class="licence-card__icon">
                                                                <c:choose>
                                                                    <c:when test="${fn:contains(fn:toLowerCase(licence.vehicleType), 'moto')}">
                                                                        <svg width="20" height="20" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                                                                        <circle cx="6" cy="17" r="3" stroke="currentColor" stroke-width="2"></circle>
                                                                        <circle cx="18" cy="17" r="3" stroke="currentColor" stroke-width="2"></circle>
                                                                        <path d="M8.5 10.5 12 6h4l2.5 4.5" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"></path>
                                                                        <path d="M12 6v11" stroke="currentColor" stroke-width="2"></path>
                                                                        </svg>
                                                                    </c:when>
                                                                    <c:when test="${fn:contains(fn:toLowerCase(licence.vehicleType), 'bus')}">
                                                                        <svg width="20" height="20" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                                                                        <rect x="3" y="5" width="18" height="13" rx="2" stroke="currentColor" stroke-width="2"></rect>
                                                                        <path d="M3 12h18M7 18v2M17 18v2" stroke="currentColor" stroke-width="2" stroke-linecap="round"></path>
                                                                        </svg>
                                                                    </c:when>
                                                                    <c:otherwise>
                                                                        <svg width="20" height="20" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                                                                        <rect x="2" y="6" width="20" height="12" rx="2" stroke="currentColor" stroke-width="2"></rect>
                                                                        <circle cx="6" cy="12" r="2.5" stroke="currentColor" stroke-width="2"></circle>
                                                                        <circle cx="18" cy="12" r="2.5" stroke="currentColor" stroke-width="2"></circle>
                                                                        <path d="M10 12h4" stroke="currentColor" stroke-width="2" stroke-linecap="round"></path>
                                                                        </svg>
                                                                    </c:otherwise>
                                                                </c:choose>
                                                            </div>
                                                            <span class="licence-card__code">Hạng ${licence.code}</span>
                                                            <span class="licence-card__name">${licence.name}</span>
                                                            <span class="licence-card__hint">
                                                                <c:choose>
                                                                    <c:when test="${not canRegisterExam}">Chưa đủ giấy tờ / chưa được duyệt</c:when>
                                                                    <c:otherwise>Chưa được duyệt hồ sơ cho hạng này</c:otherwise>
                                                                </c:choose>
                                                            </span>
                                                        </div>
                                                    </div>
                                                </c:when>
                                                <c:otherwise>
                                                    <a href="${licencePickUrl}${licencePickFragment}" class="licence-card-link">
                                                        <div class="${licenceCardClasses}">
                                                            <div class="licence-card__icon">
                                                                <c:choose>
                                                                    <c:when test="${fn:contains(fn:toLowerCase(licence.vehicleType), 'moto')}">
                                                                        <svg width="20" height="20" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                                                                        <circle cx="6" cy="17" r="3" stroke="currentColor" stroke-width="2"></circle>
                                                                        <circle cx="18" cy="17" r="3" stroke="currentColor" stroke-width="2"></circle>
                                                                        <path d="M8.5 10.5 12 6h4l2.5 4.5" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"></path>
                                                                        <path d="M12 6v11" stroke="currentColor" stroke-width="2"></path>
                                                                        </svg>
                                                                    </c:when>
                                                                    <c:when test="${fn:contains(fn:toLowerCase(licence.vehicleType), 'bus')}">
                                                                        <svg width="20" height="20" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                                                                        <rect x="3" y="5" width="18" height="13" rx="2" stroke="currentColor" stroke-width="2"></rect>
                                                                        <path d="M3 12h18M7 18v2M17 18v2" stroke="currentColor" stroke-width="2" stroke-linecap="round"></path>
                                                                        </svg>
                                                                    </c:when>
                                                                    <c:otherwise>
                                                                        <svg width="20" height="20" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                                                                        <rect x="2" y="6" width="20" height="12" rx="2" stroke="currentColor" stroke-width="2"></rect>
                                                                        <circle cx="6" cy="12" r="2.5" stroke="currentColor" stroke-width="2"></circle>
                                                                        <circle cx="18" cy="12" r="2.5" stroke="currentColor" stroke-width="2"></circle>
                                                                        <path d="M10 12h4" stroke="currentColor" stroke-width="2" stroke-linecap="round"></path>
                                                                        </svg>
                                                                    </c:otherwise>
                                                                </c:choose>
                                                            </div>
                                                            <span class="licence-card__code">Hạng ${licence.code}</span>
                                                            <span class="licence-card__name">${licence.name}</span>
                                                        </div>
                                                    </a>
                                                </c:otherwise>
                                            </c:choose>
                                        </c:forEach>
                                    </c:if>

                                    <c:if test="${empty licenceClassesList}">
                                        <p style="color:#64748b;margin:0;">
                                            Hiện chưa có hạng bằng để đăng ký. Vui lòng thử lại sau hoặc liên hệ trung tâm hỗ trợ.
                                        </p>
                                    </c:if>

                                </div>
                            </div>
                        </section>

                        <%-- Section 2: Select Exam Session --%>
                        <section class="p-form-card" id="register-exam-session" aria-label="Chọn ngày thi dự kiến">
                            <div class="p-form-header">
                                <h2 class="p-form-title">
                                    <svg width="20" height="20" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                                    <rect x="3" y="4" width="18" height="16" rx="2" stroke="currentColor" stroke-width="2"></rect>
                                    <path d="M16 2v4M8 2v4M3 10h18" stroke="currentColor" stroke-width="2" stroke-linecap="round"></path>
                                    </svg>
                                    2. Ngày thi dự kiến
                                </h2>
                            </div>
                            <jsp:include page="/views/registrant/partials/session-list-filter-form.jsp"/>
                            <div class="p-form-body p-form-body--compact">
                                <div class="session-selector-list">

                                    <c:if test="${not empty examSessionsList and (not canRegisterExam or selectedLicenceDocumentAllowed)}">
                                        <%-- Dynamic sessions loop from database --%>
                                        <c:forEach var="session" items="${examSessionsList}">
                                            <c:url var="sessionPickUrl" value="/registrant/register-exam">
                                                <c:param name="licenceSelect" value="${selectedClassCode}"/>
                                                <c:param name="sessionSelect" value="${session.id}"/>
                                                <c:if test="${not empty searchQuery}"><c:param name="q" value="${searchQuery}"/></c:if>
                                                <c:if test="${locationFilter ne 'all'}"><c:param name="location" value="${locationFilter}"/></c:if>
                                                <c:if test="${not empty fromDateIso}"><c:param name="fromDate" value="${fromDateIso}"/></c:if>
                                                <c:if test="${not empty toDateIso}"><c:param name="toDate" value="${toDateIso}"/></c:if>
                                            </c:url>
                                            <a href="${sessionPickUrl}#register-exam-summary" class="licence-card-link">
                                                <div class="session-card ${sessionChosen and selectedSessionCode eq session.id ? 'session-card--active' : ''}">                                                <div class="session-card__title-wrap">
                                                        <span class="session-card__title">${session.examName}</span>
                                                        <span class="session-card__subtitle">Hạng ${session.licenceClass}</span>
                                                    </div>
                                                    <div class="session-card__info-item session-card__hide-sm">
                                                        <svg width="15" height="15" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                                                        <rect x="3" y="4" width="18" height="16" rx="2" stroke="currentColor" stroke-width="2"></rect>
                                                        <path d="M16 2v4M8 2v4M3 10h18" stroke="currentColor" stroke-width="2" stroke-linecap="round"></path>
                                                        </svg>
                                                        <span><fmt:formatDate value="${session.examDate}" pattern="dd/MM/yyyy"/></span>
                                                    </div>
                                                    <div class="session-card__info-item session-card__hide-md">
                                                        <svg width="15" height="15" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                                                        <path d="M12 2C8.13 2 5 5.13 5 9C5 14.25 12 22 12 22C12 22 19 14.25 19 9C19 5.13 15.87 2 12 2Z" stroke="currentColor" stroke-width="2" stroke-linejoin="round"></path>
                                                        <circle cx="12" cy="9" r="2.5" stroke="currentColor" stroke-width="2"></circle>
                                                        </svg>
                                                        <span>${session.location}</span>
                                                    </div>
                                                    <div class="session-card__info-item">
                                                        <span class="r-stat-card__badge r-stat-card__badge--success">Nguyện vọng đăng ký</span>
                                                    </div>
                                                    <div class="session-card__radio-indicator"></div>
                                                </div>
                                            </a>
                                        </c:forEach>
                                    </c:if>

                                    <c:if test="${empty examSessionsList or (canRegisterExam and not selectedLicenceDocumentAllowed and not empty selectedClassCode)}">
                                        <p class="session-selector-list__empty">
                                            <c:choose>
                                                <c:when test="${canRegisterExam and not selectedLicenceDocumentAllowed and not empty selectedClassCode}">
                                                    Hạng ${selectedClassCode} chưa được duyệt kèm hồ sơ.
                                                    Vào Quản lý tài liệu → chọn hạng này khi Gửi yêu cầu duyệt
                                                    (có thể tái sử dụng 4 giấy đã có nếu không cần đổi).
                                                </c:when>
                                                <c:when test="${searchActive}">Không có ngày thi phù hợp với bộ lọc.</c:when>
                                                <c:otherwise>
                                                    Chưa có ngày thi dự kiến cho hạng ${not empty selectedClassCode ? selectedClassCode : 'đã chọn'}.
                                                    Vui lòng quay lại sau khi trung tâm công bố lịch, hoặc thử bỏ bộ lọc ngày/địa điểm.
                                                </c:otherwise>
                                            </c:choose>
                                        </p>
                                    </c:if>

                                </div>
                            </div>
                        </section>

                    </div>

                    <%-- Right: tóm tắt + POST confirmRegistration=1 → MERGE RegistrationDates (không thu SePay) --%>
                    <div class="dashboard-sidebar-column" id="register-exam-summary">

                        <form action="${ctx}/registrant/register-exam" method="post" id="registrationForm" class="register-exam-confirm-form">
                            <input type="hidden" name="licenceSelect" value="${selectedClassCode}">
                            <input type="hidden" name="sessionSelect" value="${selectedSessionCode}">
                            <input type="hidden" name="confirmRegistration" value="1">
                            <c:if test="${not empty searchQuery}"><input type="hidden" name="q" value="${searchQuery}"></c:if>
                            <c:if test="${locationFilter ne 'all'}"><input type="hidden" name="location" value="${locationFilter}"></c:if>
                            <c:if test="${not empty fromDateIso}"><input type="hidden" name="fromDate" value="${fromDateIso}"></c:if>
                            <c:if test="${not empty toDateIso}"><input type="hidden" name="toDate" value="${toDateIso}"></c:if>

                                <div class="payment-summary-card">
                                    <h3 class="payment-summary-title">Tóm tắt đăng ký</h3>

                                    <div class="payment-summary-list">
                                        <div class="payment-summary-item">
                                            <span>Hạng đăng ký</span>
                                            <span>
                                            <c:choose>
                                                <c:when test="${not empty selectedLicence}">Hạng ${selectedLicence.code}</c:when>
                                                <c:otherwise>—</c:otherwise>
                                            </c:choose>
                                        </span>
                                    </div>
                                    <div class="payment-summary-item">
                                        <span>Ngày dự kiến</span>
                                        <span>
                                            <c:choose>
                                                <c:when test="${sessionChosen and not empty selectedSession}">
                                                    <fmt:formatDate value="${selectedSession.examDate}" pattern="dd/MM/yyyy"/>
                                                </c:when>
                                                <c:otherwise>Chưa chọn — bấm một ngày bên trái</c:otherwise>
                                            </c:choose>
                                        </span>
                                    </div>
                                    <c:if test="${sessionChosen and not empty selectedSession}">
                                        <div class="payment-summary-item">
                                            <span>Ghi chú</span>
                                            <span>${selectedSession.examName}</span>
                                        </div>
                                        <div class="payment-summary-item">
                                            <span>Địa điểm</span>
                                            <span>${selectedSession.location}</span>
                                        </div>
                                    </c:if>
                                    <div class="payment-summary-item">
                                        <span>Số báo danh (SBD)</span>
                                        <span class="register-exam-sbd-pending">${sbdPendingDisplay}</span>
                                    </div>
                                </div>

                                <div class="payment-footer-text register-exam-date-hint" style="margin-top:12px;">
                                    Bước này chỉ ghi <strong>nguyện vọng ngày thi</strong>.
                                    Lịch thi chính thức, giờ ca và kết quả do trung tâm cập nhật sau — vui lòng chờ thông báo từ phía trung tâm.
                                </div>

                                <div class="payment-footer-text register-exam-sbd-hint" style="margin-top:12px;">
                                    Khi có lịch chính thức, bạn theo dõi tại
                                    <a href="${pageContext.request.contextPath}/registrant/my-exams" class="profile-checklist-link">Lịch thi &amp; kết quả</a>.
                                </div>

                                <div class="payment-footer-text" style="margin-top:12px;">
                                    Mỗi hạng GPLX chỉ được đăng ký <strong>một lần</strong> ngày thi dự kiến.
                                    Sau khi xác nhận, không thể đổi sang ngày khác — vui lòng theo dõi lịch chính thức tại
                                    <a href="${pageContext.request.contextPath}/registrant/my-exams" class="profile-checklist-link">Lịch thi &amp; kết quả</a>.
                                </div>

                                <c:if test="${not empty licenceGateMessage}">
                                    <div class="p-alert-banner" role="alert" style="margin-top:12px;background:#fff7ed;border-color:#fdba74;color:#9a3412;padding:0.75rem 1rem;border-radius:8px;font-size:0.875rem;">
                                        <c:out value="${licenceGateMessage}"/>
                                    </div>
                                </c:if>

                                <c:if test="${not empty registrationConflictMessage}">
                                    <div class="p-alert-banner" role="alert" style="margin-top:12px;background:#fff7ed;border-color:#fdba74;color:#9a3412;padding:0.75rem 1rem;border-radius:8px;font-size:0.875rem;">
                                        <c:out value="${registrationConflictMessage}"/>
                                    </div>
                                </c:if>

                                <c:if test="${not empty preferredDateCancelledNotice}">
                                    <div class="p-alert-banner" role="status" style="margin-top:12px;background:#eff6ff;border:1px solid #93c5fd;color:#1e3a8a;padding:0.75rem 1rem;border-radius:8px;font-size:0.875rem;">
                                        <c:out value="${preferredDateCancelledNotice}"/>
                                    </div>
                                </c:if>

                                <button type="submit" class="payment-submit-btn" id="btn-submit-registration"
                                        ${not sessionChosen or not canRegisterExam or not selectedLicenceDocumentAllowed or empty canConfirmRegistration or not canConfirmRegistration ? 'disabled' : ''}>
                                    Xác nhận đăng ký
                                </button>

                                <div class="payment-footer-text">
                                    Bằng cách nhấn nút, bạn xác nhận thông tin đăng ký dự thi là chính xác.
                                </div>
                            </div>

                        </form>

                    </div>

                </div>
            </main>

        </div>

    </body>
</html>

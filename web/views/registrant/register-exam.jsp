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
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/layout.css">
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
                <h1 class="page-title">Đăng ký đợt thi mới</h1>
                <p class="page-subtitle">Chọn hạng bằng lái đăng ký dự thi và lựa chọn lịch thi phù hợp</p>
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
                            Với 4 giấy tờ bắt buộc, bạn chỉ có thể đăng ký thi hạng A1 hoặc A2.
                            Để đăng ký hạng khác, vui lòng bổ sung hồ sơ khác đúng hạng tại
                            <a href="${pageContext.request.contextPath}/registrant/upload-documents" class="profile-checklist-link">Quản lý tài liệu</a>.
                        </c:when>
                        <c:otherwise>
                            Vui lòng chọn hạng GPLX và đợt thi, sau đó xác nhận đăng ký bên dưới.
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
                                        <c:url var="licencePickUrl" value="/registrant/register-exam">
                                            <c:param name="licenceSelect" value="${licence.code}"/>
                                            <c:if test="${not empty searchQuery}"><c:param name="q" value="${searchQuery}"/></c:if>
                                            <c:if test="${locationFilter ne 'all'}"><c:param name="location" value="${locationFilter}"/></c:if>
                                            <c:if test="${not empty fromDateIso}"><c:param name="fromDate" value="${fromDateIso}"/></c:if>
                                            <c:if test="${not empty toDateIso}"><c:param name="toDate" value="${toDateIso}"/></c:if>
                                        </c:url>
                                        <c:set var="licencePickFragment" value="${canRegisterExam and licenceDocsAllowed eq false ? '#register-exam-licence-notice' : '#register-exam-session'}" />
                                        <c:set var="licenceCardClasses" value="licence-card ${selectedClassCode eq licence.code ? 'licence-card--active' : ''}${canRegisterExam and licenceDocsAllowed eq false ? ' licence-card--restricted' : ''}" />
                                        <a href="${licencePickUrl}${licencePickFragment}"
                                           class="licence-card-link${canRegisterExam and licenceDocsAllowed eq false ? ' licence-card-link--blocked' : ''}"
                                           <c:if test="${canRegisterExam and licenceDocsAllowed eq false}">title="${licenceDocumentBlockMessages[licence.code]}"</c:if>>
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
                                                <span class="licence-card__fee">
                                                    <fmt:formatNumber value="${licence.examFee != null ? licence.examFee : 0}" type="number"/> đ
                                                </span>
                                                <c:if test="${canRegisterExam and licenceDocsAllowed eq false}">
                                                <span class="licence-card__hint">Chưa có hồ sơ bổ sung cho hạng này</span>
                                                </c:if>
                                            </div>
                                        </a>
                                    </c:forEach>
                                </c:if>
                                
                                <c:if test="${empty licenceClassesList}">
                                    <p style="color:#64748b;margin:0;">Không có hạng GPLX khả dụng trong hệ thống.</p>
                                </c:if>

                            </div>
                        </div>
                    </section>

                    <%-- Section 2: Select Exam Session --%>
                    <section class="p-form-card" id="register-exam-session" aria-label="Chọn đợt thi">                        <div class="p-form-header">
                            <h2 class="p-form-title">
                                <svg width="20" height="20" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                                    <rect x="3" y="4" width="18" height="16" rx="2" stroke="currentColor" stroke-width="2"></rect>
                                    <path d="M16 2v4M8 2v4M3 10h18" stroke="currentColor" stroke-width="2" stroke-linecap="round"></path>
                                </svg>
                                2. Lịch thi khả dụng
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
                                                    <span class="session-card__subtitle">Mã: ${session.examCode} — Hạng ${session.licenceClass}</span>
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
                                                    <span class="r-stat-card__badge r-stat-card__badge--success">Còn ${session.slotsRemaining} chỗ</span>
                                                </div>
                                                <div class="session-card__radio-indicator"></div>
                                            </div>
                                        </a>
                                    </c:forEach>
                                </c:if>
                                
                                <c:if test="${empty examSessionsList or (canRegisterExam and not selectedLicenceDocumentAllowed and not empty selectedClassCode)}">
                                    <p style="color:#64748b;margin:0;">
                                        <c:choose>
                                            <c:when test="${canRegisterExam and not selectedLicenceDocumentAllowed and not empty selectedClassCode}">
                                                Không thể chọn đợt thi — vui lòng bổ sung hồ sơ khác cho hạng ${selectedClassCode} hoặc chọn hạng A1/A2.
                                            </c:when>
                                            <c:when test="${searchActive}">Không có đợt thi phù hợp với bộ lọc.</c:when>
                                            <c:otherwise>
                                                Không có đợt thi mở cho hạng ${not empty selectedClassCode ? selectedClassCode : 'đã chọn'}.
                                            </c:otherwise>
                                        </c:choose>
                                    </p>
                                </c:if>

                            </div>
                        </div>
                    </section>

                </div>

                <%-- Right Column: Registration Summary + POST xác nhận --%>
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
                                <span>Đợt thi</span>
                                <span>
                                    <c:choose>
                                        <c:when test="${sessionChosen and not empty selectedSession}">${selectedSession.examName}</c:when>
                                        <c:otherwise>Chưa chọn — bấm một ca thi bên trái</c:otherwise>
                                    </c:choose>
                                </span>
                            </div>
                            <c:if test="${sessionChosen and not empty selectedSession}">
                            <div class="payment-summary-item">
                                <span>Ngày thi</span>
                                <span><fmt:formatDate value="${selectedSession.examDate}" pattern="dd/MM/yyyy"/></span>
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
                            <c:if test="${not empty selectedLicence}">
                            <div class="payment-summary-item payment-summary-item--total">
                                <span>Lệ phí tham khảo</span>
                                <span class="payment-summary-value--total">
                                    <fmt:formatNumber value="${selectedLicence.examFee}" type="number"/> đ
                                </span>
                            </div>
                            </c:if>
                        </div>

                        <div class="payment-footer-text register-exam-sbd-hint" style="margin-top:12px;">
                            Số báo danh (SBD) được cấp sau khi bạn xác nhận đăng ký đợt thi.
                            Ban sát hạch sẽ cập nhật SBD chính thức khi nhập danh sách thí sinh — bạn có thể theo dõi tại
                            <a href="${pageContext.request.contextPath}/registrant/my-exams" class="profile-checklist-link">Lịch thi &amp; kết quả</a>.
                        </div>

                        <div class="payment-footer-text register-exam-date-hint" style="margin-top:12px;">
                            Khi đăng ký chỉ xác định <strong>ngày thi</strong>. Giờ ca thi do Ban sát hạch mở và bố trí vào đúng ngày thi.
                        </div>

                        <div class="payment-footer-text" style="margin-top:12px;">
                            Lệ phí thi sát hạch sẽ thu tại trung tâm khi làm bài thực hành.
                        </div>

                        <div class="payment-footer-text" style="margin-top:12px;">
                            Mỗi hạng GPLX chỉ được một đăng ký đang xử lý cho từng phần thi (Lý thuyết, Sa hình, …).
                            Chỉ đăng ký lại khi đăng ký trước bị từ chối hoặc đã được hủy.
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

    <%-- Footer --%>
    <jsp:include page="/views/layout/footer.jsp" />
</div>

</body>
</html>

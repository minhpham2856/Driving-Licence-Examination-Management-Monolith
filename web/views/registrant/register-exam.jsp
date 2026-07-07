<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>

<c:set var="selectedClassCode" value="${param.licenceSelect}" />
<c:if test="${empty selectedClassCode}">
    <c:set var="selectedClassCode" value="B2" />
</c:if>

<c:set var="selectedSessionCode" value="${param.sessionSelect}" />
<c:if test="${empty selectedSessionCode}">
    <c:set var="selectedSessionCode" value="SH-2025-06-B2" />
</c:if>

<c:choose>
    <c:when test="${selectedClassCode eq 'A1'}">
        <c:set var="feeSathachName" value="LS phí thi sát hạch A1" />
        <c:set var="feeSathachValue" value="65,000đ" />
        <c:set var="feeTotal" value="250,000đ" />
    </c:when>
    <c:when test="${selectedClassCode eq 'A2'}">
        <c:set var="feeSathachName" value="LS phí thi sát hạch A2" />
        <c:set var="feeSathachValue" value="165,000đ" />
        <c:set var="feeTotal" value="350,000đ" />
    </c:when>
    <c:when test="${selectedClassCode eq 'B1'}">
        <c:set var="feeSathachName" value="LS phí thi sát hạch B1" />
        <c:set var="feeSathachValue" value="615,000đ" />
        <c:set var="feeTotal" value="800,000đ" />
    </c:when>
    <c:otherwise>
        <c:set var="feeSathachName" value="LS phí thi sát hạch B2" />
        <c:set var="feeSathachValue" value="1,015,000đ" />
        <c:set var="feeTotal" value="1,200,000đ" />
    </c:otherwise>
</c:choose>

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
    <jsp:include page="/views/registrant/components/registrant-styles.jsp" />
</head>
<body class="has-side-nav-bar">

<%-- Inject registrant sidebar --%>
<jsp:include page="/views/registrant/components/sidebar.jsp">
    <jsp:param name="activeSidebar" value="register-exam" />
</jsp:include>

<div class="dashboard-shell">
    <main class="main-content" id="main-content">

        <%-- Breadcrumbs --%>
        <nav class="breadcrumbs" aria-label="Breadcrumb">
            <a href="${pageContext.request.contextPath}/views/registrant/dashboard.jsp">Trang chủ</a>
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
                <span>Xác nhận thông tin</span>
            </div>
            <div class="step-line"></div>
            <div class="step-item">
                <div class="step-number">3</div>
                <span>Lệ phí & Thanh toán</span>
            </div>
        </section>

        <%-- Selection Form wrapping entire grid and sidebar to post values to servlet --%>
        <form action="${pageContext.request.contextPath}/registrant/register-exam" method="post" id="registrationForm">
            
            <%-- Hidden selection parameters to be submitted cleanly by the form POST --%>
            <input type="hidden" name="licenceSelect" value="${selectedClassCode}">
            <input type="hidden" name="sessionSelect" value="${selectedSessionCode}">

            <%-- Selection Layout Grid --%>
            <div class="profile-layout-grid">
                
                <%-- Left Column: Licence Selector + Available Sessions --%>
                <div class="dashboard-sidebar-column dashboard-sidebar-column--wide">
                    
                    <%-- Section 1: Select Licence --%>
                    <section class="p-form-card" aria-label="Chọn hạng GPLX">
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
                                    <%-- Dynamic licence list loop from database --%>
                                    <c:forEach var="licence" items="${licenceClassesList}">
                                        <a href="?licenceSelect=${licence.code}&sessionSelect=${selectedSessionCode}" class="licence-card-link">
                                            <div class="licence-card ${selectedClassCode eq licence.code ? 'licence-card--active' : ''}">
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
                                                    <fmt:formatNumber value="${empty licence.examFee ? 1200000 : licence.examFee}" type="number"/> đ
                                                </span>
                                            </div>
                                        </a>
                                    </c:forEach>
                                </c:if>
                                
                                <c:if test="${empty licenceClassesList}">
                                    <%-- Fallback high-fidelity selections for standalone preview --%>
                                    <%-- Hạng A1 --%>
                                    <a href="?licenceSelect=A1&sessionSelect=${selectedSessionCode}" class="licence-card-link">
                                        <div class="licence-card ${selectedClassCode eq 'A1' ? 'licence-card--active' : ''}">
                                            <div class="licence-card__icon">
                                                <svg width="20" height="20" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                                                    <circle cx="6" cy="17" r="3" stroke="currentColor" stroke-width="2"></circle>
                                                    <circle cx="18" cy="17" r="3" stroke="currentColor" stroke-width="2"></circle>
                                                    <path d="M8.5 10.5 12 6h4l2.5 4.5" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"></path>
                                                    <path d="M12 6v11" stroke="currentColor" stroke-width="2"></path>
                                                </svg>
                                            </div>
                                            <span class="licence-card__code">Hạng A1</span>
                                            <span class="licence-card__name">Mô tô 2 bánh dưới 175cc</span>
                                            <span class="licence-card__fee">250.000 đ</span>
                                        </div>
                                    </a>

                                    <%-- Hạng A2 --%>
                                    <a href="?licenceSelect=A2&sessionSelect=${selectedSessionCode}" class="licence-card-link">
                                        <div class="licence-card ${selectedClassCode eq 'A2' ? 'licence-card--active' : ''}">
                                            <div class="licence-card__icon">
                                                <svg width="20" height="20" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                                                    <circle cx="6" cy="17" r="3" stroke="currentColor" stroke-width="2"></circle>
                                                    <circle cx="18" cy="17" r="3" stroke="currentColor" stroke-width="2"></circle>
                                                    <path d="M8.5 10.5 12 6h4l2.5 4.5" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"></path>
                                                    <path d="M6 14h12" stroke="currentColor" stroke-width="2" stroke-linecap="round"></path>
                                                </svg>
                                            </div>
                                            <span class="licence-card__code">Hạng A2</span>
                                            <span class="licence-card__name">Mô tô 2 bánh trên 175cc</span>
                                            <span class="licence-card__fee">350.000 đ</span>
                                        </div>
                                    </a>

                                    <%-- Hạng B1 --%>
                                    <a href="?licenceSelect=B1&sessionSelect=${selectedSessionCode}" class="licence-card-link">
                                        <div class="licence-card ${selectedClassCode eq 'B1' ? 'licence-card--active' : ''}">
                                            <div class="licence-card__icon">
                                                <svg width="20" height="20" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                                                    <rect x="2" y="6" width="20" height="12" rx="2" stroke="currentColor" stroke-width="2"></rect>
                                                    <circle cx="7" cy="12" r="2" stroke="currentColor" stroke-width="2"></circle>
                                                    <circle cx="17" cy="12" r="2" stroke="currentColor" stroke-width="2"></circle>
                                                </svg>
                                            </div>
                                            <span class="licence-card__code">Hạng B1</span>
                                            <span class="licence-card__name">Ô tô số tự động gia đình</span>
                                            <span class="licence-card__fee">800.000 đ</span>
                                        </div>
                                    </a>

                                    <%-- Hạng B2 --%>
                                    <a href="?licenceSelect=B2&sessionSelect=${selectedSessionCode}" class="licence-card-link">
                                        <div class="licence-card ${selectedClassCode eq 'B2' ? 'licence-card--active' : ''}">
                                            <div class="licence-card__icon">
                                                <svg width="20" height="20" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                                                    <rect x="2" y="6" width="20" height="12" rx="2" stroke="currentColor" stroke-width="2"></rect>
                                                    <circle cx="6" cy="12" r="2.5" stroke="currentColor" stroke-width="2"></circle>
                                                    <circle cx="18" cy="12" r="2.5" stroke="currentColor" stroke-width="2"></circle>
                                                    <path d="M10 12h4" stroke="currentColor" stroke-width="2" stroke-linecap="round"></path>
                                                </svg>
                                            </div>
                                            <span class="licence-card__code">Hạng B2</span>
                                            <span class="licence-card__name">Ô tô số sàn kinh doanh</span>
                                            <span class="licence-card__fee">1.200.000 đ</span>
                                        </div>
                                    </a>
                                </c:if>

                            </div>
                        </div>
                    </section>

                    <%-- Section 2: Select Exam Session --%>
                    <section class="p-form-card" aria-label="Chọn đợt thi">
                        <div class="p-form-header">
                            <h2 class="p-form-title">
                                <svg width="20" height="20" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                                    <rect x="3" y="4" width="18" height="16" rx="2" stroke="currentColor" stroke-width="2"></rect>
                                    <path d="M16 2v4M8 2v4M3 10h18" stroke="currentColor" stroke-width="2" stroke-linecap="round"></path>
                                </svg>
                                2. Lịch thi khả dụng
                            </h2>
                        </div>
                        <div class="p-form-body p-form-body--compact">
                            <div class="session-selector-list">
                                
                                <c:if test="${not empty examSessionsList}">
                                    <%-- Dynamic sessions loop from database --%>
                                    <c:forEach var="session" items="${examSessionsList}">
                                        <a href="?licenceSelect=${selectedClassCode}&sessionSelect=${session.id}" class="licence-card-link">
                                            <div class="session-card ${selectedSessionCode eq session.id ? 'session-card--active' : ''}">
                                                <div class="session-card__title-wrap">
                                                    <span class="session-card__title">${session.examName}</span>
                                                    <span class="session-card__subtitle">Mã: ${session.examCode} - Hạng ${session.licenceClass}</span>
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
                                
                                <c:if test="${empty examSessionsList}">
                                    <%-- Fallback standard rows for standalone preview --%>
                                    <%-- Session 1 --%>
                                    <a href="?licenceSelect=${selectedClassCode}&sessionSelect=SH-2025-06-B2" class="licence-card-link">
                                        <div class="session-card ${selectedSessionCode eq 'SH-2025-06-B2' ? 'session-card--active' : ''}">
                                            <div class="session-card__title-wrap">
                                                <span class="session-card__title">Đợt thi tháng 06/2025</span>
                                                <span class="session-card__subtitle">Mã: SH-2025-06-B2 - Hạng B2</span>
                                            </div>
                                            <div class="session-card__info-item session-card__hide-sm">
                                                <svg width="15" height="15" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                                                    <rect x="3" y="4" width="18" height="16" rx="2" stroke="currentColor" stroke-width="2"></rect>
                                                    <path d="M16 2v4M8 2v4M3 10h18" stroke="currentColor" stroke-width="2" stroke-linecap="round"></path>
                                                </svg>
                                                <span>15/06/2025</span>
                                            </div>
                                            <div class="session-card__info-item session-card__hide-md">
                                                <svg width="15" height="15" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                                                    <path d="M12 2C8.13 2 5 5.13 5 9C5 14.25 12 22 12 22C12 22 19 14.25 19 9C19 5.13 15.87 2 12 2Z" stroke="currentColor" stroke-width="2" stroke-linejoin="round"></path>
                                                    <circle cx="12" cy="9" r="2.5" stroke="currentColor" stroke-width="2"></circle>
                                                </svg>
                                                <span>Sân sát hạch HN</span>
                                            </div>
                                            <div class="session-card__info-item">
                                                <span class="r-stat-card__badge r-stat-card__badge--success">Còn 12 chỗ</span>
                                            </div>
                                            <div class="session-card__radio-indicator"></div>
                                        </div>
                                    </a>

                                    <%-- Session 2 --%>
                                    <a href="?licenceSelect=${selectedClassCode}&sessionSelect=SH-2025-07-B2" class="licence-card-link">
                                        <div class="session-card ${selectedSessionCode eq 'SH-2025-07-B2' ? 'session-card--active' : ''}">
                                            <div class="session-card__title-wrap">
                                                <span class="session-card__title">Đợt thi tháng 07/2025</span>
                                                <span class="session-card__subtitle">Mã: SH-2025-07-B2 - Hạng B2</span>
                                            </div>
                                            <div class="session-card__info-item session-card__hide-sm">
                                                <svg width="15" height="15" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                                                    <rect x="3" y="4" width="18" height="16" rx="2" stroke="currentColor" stroke-width="2"></rect>
                                                    <path d="M16 2v4M8 2v4M3 10h18" stroke="currentColor" stroke-width="2" stroke-linecap="round"></path>
                                                </svg>
                                                <span>13/07/2025</span>
                                            </div>
                                            <div class="session-card__info-item session-card__hide-md">
                                                <svg width="15" height="15" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                                                    <path d="M12 2C8.13 2 5 5.13 5 9C5 14.25 12 22 12 22C12 22 19 14.25 19 9C19 5.13 15.87 2 12 2Z" stroke="currentColor" stroke-width="2" stroke-linejoin="round"></path>
                                                    <circle cx="12" cy="9" r="2.5" stroke="currentColor" stroke-width="2"></circle>
                                                </svg>
                                                <span>Sân sát hạch HN</span>
                                            </div>
                                            <div class="session-card__info-item">
                                                <span class="r-stat-card__badge r-stat-card__badge--info">Còn 56 chỗ</span>
                                            </div>
                                            <div class="session-card__radio-indicator"></div>
                                        </div>
                                    </a>
                                </c:if>

                            </div>
                        </div>
                    </section>

                </div>

                <%-- Right Column: Fee Breakdown & Payment Summary Card --%>
                <div class="dashboard-sidebar-column">
                    
                    <div class="payment-summary-card">
                        <h3 class="payment-summary-title">Tóm tắt chi phí</h3>
                        
                        <div class="payment-summary-list">
                            <div class="payment-summary-item">
                                <span>${feeSathachName}</span>
                                <span>${feeSathachValue}</span>
                            </div>
                            <div class="payment-summary-item">
                                <span>Phí cấp bằng PET</span>
                                <span>135,000đ</span>
                            </div>
                            <div class="payment-summary-item">
                                <span>Phí quản lý hồ sơ</span>
                                <span>50,000đ</span>
                            </div>
                            
                            <div class="payment-summary-item payment-summary-item--total">
                                <span>Tổng cộng</span>
                                <span class="payment-summary-value--total">${feeTotal}</span>
                            </div>
                        </div>

                        <%-- Payment Methods --%>
                        <div class="payment-method-section">
                            <span class="payment-method-header">Phương thức thanh toán</span>
                            <div class="payment-methods-grid">
                                
                                <%-- Option 1: QR Code (Checked by Default) --%>
                                <label>
                                    <input type="radio" name="paymentMethodSelect" class="pm-radio" value="QR" checked>
                                    <div class="pm-card">
                                        <span class="pm-card__icon">
                                            <svg width="24" height="24" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                                                <rect x="3" y="3" width="7" height="7" rx="1" stroke="currentColor" stroke-width="2"></rect>
                                                <rect x="14" y="3" width="7" height="7" rx="1" stroke="currentColor" stroke-width="2"></rect>
                                                <rect x="3" y="14" width="7" height="7" rx="1" stroke="currentColor" stroke-width="2"></rect>
                                                <path d="M14 14h3v3h-3zm3 3h4v4h-4zm-3 4h3v-2h-3zm5-4h2v-3h-2z" stroke="currentColor" stroke-width="2" stroke-linejoin="round"></path>
                                            </svg>
                                        </span>
                                        <span class="pm-card__label">Mã QR<br>(MoMo/VNPAY)</span>
                                    </div>
                                </label>

                                <%-- Option 2: Bank Transfer --%>
                                <label>
                                    <input type="radio" name="paymentMethodSelect" class="pm-radio" value="BANK">
                                    <div class="pm-card">
                                        <span class="pm-card__icon">
                                            <svg width="24" height="24" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                                                <path d="M3 21h18M3 10h18M5 10v7M10 10v7M14 10v7M19 10v7M2 10l10-7 10 7" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"></path>
                                            </svg>
                                        </span>
                                        <span class="pm-card__label">Chuyển khoản<br>Ngân hàng</span>
                                    </div>
                                </label>

                            </div>
                        </div>

                        <%-- Action Button --%>
                        <button type="submit" class="payment-submit-btn" id="btn-submit-registration">
                            Đăng ký ngay
                        </button>

                        <%-- Footer Notice --%>
                        <div class="payment-footer-text">
                            Bằng cách nhấn nút, bạn đồng ý với Điều khoản của Lái Vui.
                        </div>
                    </div>

                </div>

            </div>
        </form>

    </main>

    <%-- Footer --%>
    <jsp:include page="/views/landing/components/footer.jsp" />
</div>

</body>
</html>

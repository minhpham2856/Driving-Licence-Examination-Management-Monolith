<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>

<c:set var="ctx" value="${pageContext.request.contextPath}" />
<c:set var="logoUrl" value="${ctx}/assets/imgs/LOGO.png" />

<c:set var="activeSidebar" value="${param.activeSidebar}" />
<c:if test="${empty activeSidebar}">
    <c:choose>
        <c:when test="${fn:contains(pageContext.request.requestURI, 'dashboard')}">
            <c:set var="activeSidebar" value="dashboard" />
        </c:when>
        <c:when test="${fn:contains(pageContext.request.requestURI, 'candidate-call') or fn:contains(pageContext.request.requestURI, 'goi-thi-sinh')}">
            <c:set var="activeSidebar" value="goi-thi-sinh" />
        </c:when>
        <c:when test="${fn:contains(pageContext.request.requestURI, 'candidate-detail') or fn:contains(pageContext.request.requestURI, 'sua-thong-tin')}">
            <c:set var="activeSidebar" value="sua-thong-tin" />
        </c:when>
        <c:when test="${fn:contains(pageContext.request.requestURI, 'edit-score') or fn:contains(pageContext.request.requestURI, 'sua-ket-qua')}">
            <c:set var="activeSidebar" value="sua-ket-qua" />
        </c:when>
        <c:when test="${fn:contains(pageContext.request.requestURI, 'export') or fn:contains(pageContext.request.requestURI, 'xuat-file')}">
            <c:set var="activeSidebar" value="xuat-file" />
        </c:when>
        <c:when test="${fn:contains(pageContext.request.requestURI, 'audit') or fn:contains(pageContext.request.requestURI, 'nhat-ky')}">
            <c:set var="activeSidebar" value="nhat-ky" />
        </c:when>
        <c:otherwise>
            <c:set var="activeSidebar" value="dashboard" />
        </c:otherwise>
    </c:choose>
</c:if>

<aside class="side-nav-bar side-nav-bar--examiner">
    <div class="side-nav-bar__brand">
        <div class="side-nav-bar__brand-inner">
            <a href="${ctx}/views/examiner/dashboard.jsp" class="side-nav-bar__logo-link">
                <img src="${logoUrl}" width="40" height="40" class="side-nav-bar__logo-img">
            </a>
            <div class="side-nav-bar__brand-title-wrap">
                <h1 class="side-nav-bar__brand-title">Sát hạch viên</h1>
                <p class="side-nav-bar__brand-subtitle">
                    <c:choose>
                        <c:when test="${not empty sessionScope.user.person.fullName}">${sessionScope.user.person.fullName}</c:when>
                        <c:otherwise>Nguyễn Văn Tùng</c:otherwise>
                    </c:choose>
                </p>
            </div>
        </div>
    </div>

    <nav class="side-nav-bar__menu">
        <a href="${ctx}/views/examiner/dashboard.jsp"
           class="side-nav-bar__link${activeSidebar eq 'dashboard' ? ' is-active' : ''}">
            <span class="side-nav-bar__icon side-nav-bar__icon--18">
                <svg viewBox="0 0 18 18" fill="none" xmlns="http://www.w3.org/2000/svg">
                    <path d="M10 6V0H18V6H10V6M0 10V0H8V10H0V10M10 18V8H18V18H10V18M0 18V12H8V18H0V18M2 8H6V2H2V8M12 16H16V10H12V16M12 4H16V2H12V4M2 16H6V14H2V16" fill="currentColor"/>
                </svg>
            </span>
            <span class="side-nav-bar__label">Bảng điều khiển</span>
        </a>

        <a href="${ctx}/views/examiner/candidate-call.jsp"
           class="side-nav-bar__link${activeSidebar eq 'goi-thi-sinh' ? ' is-active' : ''}">
            <span class="side-nav-bar__icon side-nav-bar__icon--call">
                <svg viewBox="0 0 20.5 19.5" fill="none" xmlns="http://www.w3.org/2000/svg">
                    <path d="M8 8C6.9 8 5.95833 7.60833 5.175 6.825C4.39167 6.04167 4 5.1 4 4C4 2.9 4.39167 1.95833 5.175 1.175C5.95833 0.391667 6.9 0 8 0C9.1 0 10.0417 0.391667 10.825 1.175C11.6083 1.95833 12 2.9 12 4C12 5.1 11.6083 6.04167 10.825 6.825C10.0417 7.60833 9.1 8 8 8M8 6C8.55 6 9.02083 5.80417 9.4125 5.4125C9.80417 5.02083 10 4.55 10 4C10 3.45 9.80417 2.97917 9.4125 2.5875C9.02083 2.19583 8.55 2 8 2C7.45 2 6.97917 2.19583 6.5875 2.5875C6.19583 2.97917 6 3.45 6 4C6 4.55 6.19583 5.02083 6.5875 5.4125C6.97917 5.80417 7.45 6 8 6M19.1 19.5L15.9 16.3C15.55 16.5 15.175 16.6667 14.775 16.8C14.375 16.9333 13.95 17 13.5 17C12.25 17 11.1875 16.5625 10.3125 15.6875C9.4375 14.8125 9 13.75 9 12.5C9 11.25 9.4375 10.1875 10.3125 9.3125C11.1875 8.4375 12.25 8 13.5 8C14.75 8 15.8125 8.4375 16.6875 9.3125C17.5625 10.1875 18 11.25 18 12.5C18 12.95 17.9333 13.375 17.8 13.775C17.6667 14.175 17.5 14.55 17.3 14.9L20.5 18.1L19.1 19.5M13.5 15C14.2 15 14.7917 14.7583 15.275 14.275C15.7583 13.7917 16 13.2 16 12.5C16 11.8 15.7583 11.2083 15.275 10.725C14.7917 10.2417 14.2 10 13.5 10C12.8 10 12.2083 10.2417 11.725 10.725C11.2417 11.2083 11 11.8 11 12.5C11 13.2 11.2417 13.7917 11.725 14.275C12.2083 14.7583 12.8 15 13.5 15M0 16V13.225C0 12.6583 0.141667 12.1333 0.425 11.65C0.708333 11.1667 1.1 10.8 1.6 10.55C2.45 10.1167 3.40833 9.75 4.475 9.45C5.54167 9.15 6.725 9 8.025 9C7.825 9.3 7.65417 9.62083 7.5125 9.9625C7.37083 10.3042 7.25833 10.6583 7.175 11.025C6.175 11.1083 5.28333 11.2792 4.5 11.5375C3.71667 11.7958 3.05833 12.0667 2.525 12.35C2.35833 12.4333 2.22917 12.5542 2.1375 12.7125C2.04583 12.8708 2 13.0417 2 13.225V14H7.175C7.25833 14.3667 7.37083 14.7167 7.5125 15.05C7.65417 15.3833 7.825 15.7 8.025 16H0V16" fill="currentColor"/>
                </svg>
            </span>
            <span class="side-nav-bar__label">Gọi thí sinh</span>
        </a>

        <a href="${ctx}/views/examiner/candidate-details.jsp"
           class="side-nav-bar__link${activeSidebar eq 'sua-thong-tin' ? ' is-active' : ''}">
            <span class="side-nav-bar__icon side-nav-bar__icon--20">
                <svg viewBox="0 0 20 20.025" fill="none" xmlns="http://www.w3.org/2000/svg">
                    <path d="M2 20.025C1.45 20.025 0.979167 19.8292 0.5875 19.4375C0.195833 19.0458 0 18.575 0 18.025V4.025C0 3.475 0.195833 3.00417 0.5875 2.6125C0.979167 2.22083 1.45 2.025 2 2.025H10.925L8.925 4.025H2V18.025H16V11.075L18 9.075V18.025C18 18.575 17.8042 19.0458 17.4125 19.4375C17.0208 19.8292 16.55 20.025 16 20.025H2M6 14.025V9.775L15.175 0.6C15.375 0.4 15.6 0.25 15.85 0.15C16.1 0.05 16.35 0 16.6 0C16.8667 0 17.1208 0.05 17.3625 0.15C17.6042 0.25 17.825 0.4 18.025 0.6L19.425 2.025C19.6083 2.225 19.75 2.44583 19.85 2.6875C19.95 2.92917 20 3.175 20 3.425C20 3.675 19.9542 3.92083 19.8625 4.1625C19.7708 4.40417 19.625 4.625 19.425 4.825L10.25 14.025H6M18.025 3.425L16.625 2.025L18.025 3.425M8 12.025H9.4L15.2 6.225L14.5 5.525L13.775 4.825L8 10.6V12.025" fill="currentColor"/>
                </svg>
            </span>
            <span class="side-nav-bar__label">Sửa thông tin</span>
        </a>

        <a href="${ctx}/views/examiner/result-details.jsp"
           class="side-nav-bar__link${activeSidebar eq 'sua-ket-qua' ? ' is-active' : ''}">
            <span class="side-nav-bar__icon side-nav-bar__icon--result">
                <svg viewBox="0 0 20 18" fill="none" xmlns="http://www.w3.org/2000/svg">
                    <path d="M2 18C1.45 18 0.979167 17.8042 0.5875 17.4125C0.195833 17.0208 0 16.55 0 16V2C0 1.45 0.195833 0.979167 0.5875 0.5875C0.979167 0.195833 1.45 0 2 0H18C18.55 0 19.0208 0.195833 19.4125 0.5875C19.8042 0.979167 20 1.45 20 2V16C20 16.55 19.8042 17.0208 19.4125 17.4125C19.0208 17.8042 18.55 18 18 18H2M2 16H18V2H2V16M3 14H8V12H3V14M12.55 12L17.5 7.05L16.075 5.625L12.55 9.175L11.125 7.75L9.725 9.175L12.55 12M3 10H8V8H3V10M3 6H8V4H3V6" fill="currentColor"/>
                </svg>
            </span>
            <span class="side-nav-bar__label">Sửa kết quả</span>
        </a>

        <a href="${ctx}/views/examiner/export.jsp"
           class="side-nav-bar__link${activeSidebar eq 'xuat-file' ? ' is-active' : ''}">
            <span class="side-nav-bar__icon side-nav-bar__icon--16">
                <svg viewBox="0 0 16 16" fill="none" xmlns="http://www.w3.org/2000/svg">
                    <path d="M8 12L3 7L4.4 5.55L7 8.15V0H9V8.15L11.6 5.55L13 7L8 12M2 16C1.45 16 0.979167 15.8042 0.5875 15.4125C0.195833 15.0208 0 14.55 0 14V11H2V14H14V11H16V14C16 14.55 15.8042 15.0208 15.4125 15.4125C15.0208 15.8042 14.55 16 14 16H2" fill="currentColor"/>
                </svg>
            </span>
            <span class="side-nav-bar__label">Xuất file</span>
        </a>

        <a href="${ctx}/views/examiner/audit.jsp"
           class="side-nav-bar__link${activeSidebar eq 'nhat-ky' ? ' is-active' : ''}">
            <span class="side-nav-bar__icon side-nav-bar__icon--18">
                <svg viewBox="0 0 18 18" fill="none" xmlns="http://www.w3.org/2000/svg">
                    <path d="M9 18C6.7 18 4.69583 17.2375 2.9875 15.7125C1.27917 14.1875 0.3 12.2833 0.05 10H2.1C2.33333 11.7333 3.10417 13.1667 4.4125 14.3C5.72083 15.4333 7.25 16 9 16C10.95 16 12.6042 15.3208 13.9625 13.9625C15.3208 12.6042 16 10.95 16 9C16 7.05 15.3208 5.39583 13.9625 4.0375C12.6042 2.67917 10.95 2 9 2C7.85 2 6.775 2.26667 5.775 2.8C4.775 3.33333 3.93333 4.06667 3.25 5H6V7H0V1H2V3.35C2.85 2.28333 3.8875 1.45833 5.1125 0.875C6.3375 0.291667 7.63333 0 9 0C10.25 0 11.4208 0.2375 12.5125 0.7125C13.6042 1.1875 14.5542 1.82917 15.3625 2.6375C16.1708 3.44583 16.8125 4.39583 17.2875 5.4875C17.7625 6.57917 18 7.75 18 9C18 10.25 17.7625 11.4208 17.2875 12.5125C16.8125 13.6042 16.1708 14.5542 15.3625 15.3625C14.5542 16.1708 13.6042 16.8125 12.5125 17.2875C11.4208 17.7625 10.25 18 9 18M11.8 13.2L8 9.4V4H10V8.6L13.2 11.8L11.8 13.2" fill="currentColor"/>
                </svg>
            </span>
            <span class="side-nav-bar__label">Nhật Ký</span>
        </a>
    </nav>

    <div class="side-nav-bar__footer">
        <a href="${ctx}/logout" class="side-nav-bar__logout">
            <span class="side-nav-bar__icon side-nav-bar__icon--18">
                <svg viewBox="0 0 18 18" fill="none" xmlns="http://www.w3.org/2000/svg">
                    <path d="M2 18C1.45 18 0.979167 17.8042 0.5875 17.4125C0.195833 17.0208 0 16.55 0 16V2C0 1.45 0.195833 0.979167 0.5875 0.5875C0.979167 0.195833 1.45 0 2 0H9V2H2V16H9V18H2M13 14L11.625 12.55L14.175 10H6V8H14.175L11.625 5.45L13 4L18 9L13 14" fill="currentColor"/>
                </svg>
            </span>
            <span class="side-nav-bar__logout-label">Logout</span>
        </a>
    </div>
</aside>

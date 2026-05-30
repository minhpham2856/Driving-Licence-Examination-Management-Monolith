<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib prefix = "fn" uri = "http://java.sun.com/jsp/jstl/functions" %>

<c:set var="ctx" value="${pageContext.request.contextPath}" />
<c:set var="logoUrl" value="${ctx}/assets/imgs/LOGO.png" />

<c:set var="activeSidebar" value="${param.activeSidebar}" />
<c:if test="${empty activeSidebar}">
    <c:choose>
        <c:when test="${fn:contains(pageContext.request.requestURI, 'dashboard')}">
            <c:set var="activeSidebar" value="dashboard" />
        </c:when>
        <c:when test="${fn:contains(pageContext.request.requestURI, 'profile') or fn:contains(pageContext.request.requestURI, 'ho-so-ca-nhan')}">
            <c:set var="activeSidebar" value="profile" />
        </c:when>
        <c:when test="${fn:contains(pageContext.request.requestURI, 'upload') or fn:contains(pageContext.request.requestURI, 'ho-so-bo-sung')}">
            <c:set var="activeSidebar" value="upload-documents" />
        </c:when>
        <c:when test="${fn:contains(pageContext.request.requestURI, 'register') or fn:contains(pageContext.request.requestURI, 'dang-ky-thi')}">
            <c:set var="activeSidebar" value="register-exam" />
        </c:when>
        <c:when test="${fn:contains(pageContext.request.requestURI, 'track') or fn:contains(pageContext.request.requestURI, 'tien-trinh')}">
            <c:set var="activeSidebar" value="track-profile" />
        </c:when>
        <c:when test="${fn:contains(pageContext.request.requestURI, 'schedule') or fn:contains(pageContext.request.requestURI, 'lich-thi') or fn:contains(pageContext.request.requestURI, 'result') or fn:contains(pageContext.request.requestURI, 'ket-qua')}">
            <c:set var="activeSidebar" value="exam-schedule" />
        </c:when>
        <c:when test="${fn:contains(pageContext.request.requestURI, 'settings') or fn:contains(pageContext.request.requestURI, 'cai-dat')}">
            <c:set var="activeSidebar" value="settings" />
        </c:when>
        <c:otherwise>
            <c:set var="activeSidebar" value="dashboard" />
        </c:otherwise>
    </c:choose>
</c:if>

<aside class="side-nav-bar" role="navigation" aria-label="Cổng thí sinh đăng ký">
    <div class="side-nav-bar__brand">
        <div class="side-nav-bar__brand-inner">
            <a href="${ctx}/views/registrant/dashboard.jsp" class="side-nav-bar__logo-link" aria-label="Đăng ký sát hạch">
                <img src="${logoUrl}" alt="Lái Vui" width="63" height="63" class="side-nav-bar__logo-img">
            </a>
            <div class="side-nav-bar__brand-title-wrap">
                <h1 class="side-nav-bar__brand-title">Đăng ký thi</h1>
            </div>
        </div>
    </div>

    <nav class="side-nav-bar__menu">
        
        <%-- 1. Dashboard cá nhân --%>
        <a href="${ctx}/views/registrant/dashboard.jsp"
           class="side-nav-bar__link${activeSidebar eq 'dashboard' ? ' is-active' : ''}"
           <c:if test="${activeSidebar eq 'dashboard'}">aria-current="page"</c:if>>
            <span class="side-nav-bar__icon side-nav-bar__icon--md" aria-hidden="true">
                <svg width="24" height="24" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                    <rect x="3" y="3" width="7" height="7" rx="1.5" stroke="currentColor" stroke-width="1.5" stroke-linejoin="round"/>
                    <rect x="14" y="3" width="7" height="7" rx="1.5" stroke="currentColor" stroke-width="1.5" stroke-linejoin="round"/>
                    <rect x="3" y="14" width="7" height="7" rx="1.5" stroke="currentColor" stroke-width="1.5" stroke-linejoin="round"/>
                    <rect x="14" y="14" width="7" height="7" rx="1.5" stroke="currentColor" stroke-width="1.5" stroke-linejoin="round"/>
                </svg>
            </span>
            <span class="side-nav-bar__label">Dashboard cá nhân</span>
        </a>

        <%-- 2. Hồ sơ cá nhân (Nhóm menu với Submenu) --%>
        <div class="side-nav-bar__menu-group">
            <a href="${ctx}/views/registrant/profile.jsp"
               class="side-nav-bar__link${(activeSidebar eq 'profile' or activeSidebar eq 'upload-documents') ? ' is-active' : ''}"
               <c:if test="${activeSidebar eq 'profile' or activeSidebar eq 'upload-documents'}">aria-current="page"</c:if>>
                <span class="side-nav-bar__icon side-nav-bar__icon--user" aria-hidden="true">
                    <svg width="24" height="24" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                        <circle cx="12" cy="8" r="4" stroke="currentColor" stroke-width="1.5"></circle>
                        <path d="M4 20C4 16.686 7.582 14 12 14C16.418 14 20 16.686 20 20" stroke="currentColor" stroke-width="1.5" stroke-linecap="round"></path>
                    </svg>
                </span>
                <span class="side-nav-bar__label">Hồ sơ cá nhân</span>
            </a>
            <div class="side-nav-bar__submenu">
                <a href="${ctx}/views/registrant/profile.jsp"
                   class="side-nav-bar__submenu-link${activeSidebar eq 'profile' ? ' is-active' : ''}">
                    <span class="submenu-dot"></span>
                    <span class="side-nav-bar__label">Thông tin cá nhân</span>
                </a>
                <a href="${ctx}/views/registrant/upload-documents.jsp"
                   class="side-nav-bar__submenu-link${activeSidebar eq 'upload-documents' ? ' is-active' : ''}">
                    <span class="submenu-dot"></span>
                    <span class="side-nav-bar__label">Upload hồ sơ bổ sung</span>
                </a>
            </div>
        </div>

        <%-- 4. Đăng ký thi --%>
        <a href="${ctx}/views/registrant/register-exam.jsp"
           class="side-nav-bar__link${activeSidebar eq 'register-exam' ? ' is-active' : ''}"
           <c:if test="${activeSidebar eq 'register-exam'}">aria-current="page"</c:if>>
            <span class="side-nav-bar__icon side-nav-bar__icon--md" aria-hidden="true">
                <svg width="24" height="24" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                    <rect x="3" y="4" width="18" height="16" rx="2" stroke="currentColor" stroke-width="1.5"/>
                    <line x1="16" y1="2" x2="16" y2="6" stroke="currentColor" stroke-width="1.5" stroke-linecap="round"/>
                    <line x1="8" y1="2" x2="8" y2="6" stroke="currentColor" stroke-width="1.5" stroke-linecap="round"/>
                    <line x1="3" y1="10" x2="21" y2="10" stroke="currentColor" stroke-width="1.5"/>
                    <circle cx="12" cy="15" r="1.5" fill="currentColor"/>
                </svg>
            </span>
            <span class="side-nav-bar__label">Đăng ký thi</span>
        </a>

        <%-- 5. Theo dõi tiến trình hồ sơ --%>
        <a href="${ctx}/views/registrant/track-profile.jsp"
           class="side-nav-bar__link${activeSidebar eq 'track-profile' ? ' is-active' : ''}"
           <c:if test="${activeSidebar eq 'track-profile'}">aria-current="page"</c:if>>
            <span class="side-nav-bar__icon side-nav-bar__icon--md" aria-hidden="true">
                <svg width="24" height="24" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                    <path d="M9 5H7C5.89543 5 5 5.89543 5 7V19C5 20.1046 5.89543 21 7 21H17C18.1046 21 19 20.1046 19 19V7C19 5.89543 18.1046 5 17 5H15" stroke="currentColor" stroke-width="1.5" stroke-linecap="round"/>
                    <rect x="9" y="3" width="6" height="4" rx="1" stroke="currentColor" stroke-width="1.5" fill="white"/>
                    <path d="M9 12L11 14L15 10" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"/>
                    <line x1="9" y1="17" x2="15" y2="17" stroke="currentColor" stroke-width="1.5" stroke-linecap="round"/>
                </svg>
            </span>
            <span class="side-nav-bar__label">Theo dõi tiến trình hồ sơ</span>
        </a>

        <%-- 6. Lịch thi & kết quả --%>
        <a href="${ctx}/views/registrant/my-exams.jsp"
           class="side-nav-bar__link${activeSidebar eq 'exam-schedule' ? ' is-active' : ''}"
           <c:if test="${activeSidebar eq 'exam-schedule'}">aria-current="page"</c:if>>
            <span class="side-nav-bar__icon side-nav-bar__icon--md" aria-hidden="true">
                <svg width="24" height="24" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                    <rect x="3" y="4" width="18" height="16" rx="2" stroke="currentColor" stroke-width="1.5"/>
                    <line x1="16" y1="2" x2="16" y2="6" stroke="currentColor" stroke-width="1.5" stroke-linecap="round"/>
                    <line x1="8" y1="2" x2="8" y2="6" stroke="currentColor" stroke-width="1.5" stroke-linecap="round"/>
                    <line x1="3" y1="10" x2="21" y2="10" stroke="currentColor" stroke-width="1.5"/>
                    <path d="M12 13L13.5 15.5L16 15.5L14 17L15 19.5L12 18L9 19.5L10 17L8 15.5L10.5 15.5L12 13Z" stroke="currentColor" stroke-width="1.5" stroke-linejoin="round"/>
                </svg>
            </span>
            <span class="side-nav-bar__label">Lịch thi & kết quả</span>
        </a>

        <%-- 7. Cài đặt --%>
        <a href="${ctx}/views/registrant/settings.jsp"
           class="side-nav-bar__link${activeSidebar eq 'settings' ? ' is-active' : ''}"
           <c:if test="${activeSidebar eq 'settings'}">aria-current="page"</c:if>>
            <span class="side-nav-bar__icon side-nav-bar__icon--md" aria-hidden="true">
                <svg width="24" height="24" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                    <circle cx="12" cy="12" r="3" stroke="currentColor" stroke-width="1.5"/>
                    <path d="M19.4 15a1.65 1.65 0 0 0 .33 1.82l.06.06a2 2 0 1 1-2.83 2.83l-.06-.06a1.65 1.65 0 0 0-1.82-.33 1.65 1.65 0 0 0-1 1.51V21a2 2 0 0 1-4 0v-.09A1.65 1.65 0 0 0 9 19.4a1.65 1.65 0 0 0-1.82.33l-.06.06a2 2 0 1 1-2.83-2.83l.06-.06a1.65 1.65 0 0 0 .33-1.82 1.65 1.65 0 0 0-1.51-1H3a2 2 0 0 1 0-4h.09A1.65 1.65 0 0 0 4.6 9a1.65 1.65 0 0 0-.33-1.82l-.06-.06a2 2 0 1 1 2.83-2.83l.06.06a1.65 1.65 0 0 0 1.82.33H9a1.65 1.65 0 0 0 1-1.51V3a2 2 0 0 1 4 0v.09a1.65 1.65 0 0 0 1 1.51 1.65 1.65 0 0 0 1.82-.33l.06-.06a2 2 0 1 1 2.83 2.83l-.06.06a1.65 1.65 0 0 0-.33 1.82V9a1.65 1.65 0 0 0 1.51 1H21a2 2 0 0 1 0 4h-.09a1.65 1.65 0 0 0-1.51 1z" stroke="currentColor" stroke-width="1.5" stroke-linejoin="round"/>
                </svg>
            </span>
            <span class="side-nav-bar__label">Cài đặt</span>
        </a>

    </nav>

    <div class="side-nav-bar__footer">
        <a href="${ctx}/logout" class="side-nav-bar__logout">
            <span class="side-nav-bar__icon side-nav-bar__icon--xs" aria-hidden="true">
                <svg width="18" height="18" viewBox="0 0 18 18" fill="none" xmlns="http://www.w3.org/2000/svg">
                    <path d="M6.5 16H3.5C2.67 16 2 15.33 2 14.5V3.5C2 2.67 2.67 2 3.5 2H6.5" stroke="currentColor" stroke-width="1.5" stroke-linecap="round"/>
                    <path d="M12 12.5L16 9L12 5.5" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"/>
                    <path d="M16 9H7" stroke="currentColor" stroke-width="1.5" stroke-linecap="round"/>
                </svg>
            </span>
            <span class="side-nav-bar__logout-label">Đăng xuất</span>
        </a>
    </div>
</aside>

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
        <c:when test="${fn:contains(pageContext.request.requestURI, 'exam-area')}">
            <c:set var="activeSidebar" value="khu-vuc" />
        </c:when>
        <c:when test="${fn:contains(pageContext.request.requestURI, 'exam-room')}">
            <c:set var="activeSidebar" value="phong-thi" />
        </c:when>
        <c:when test="${fn:contains(pageContext.request.requestURI, 'exam-computer')}">
            <c:set var="activeSidebar" value="may-thi" />
        </c:when>
        <c:when test="${fn:contains(pageContext.request.requestURI, 'licence-class')}">
            <c:set var="activeSidebar" value="hang-gplx" />
        </c:when>
        <c:when test="${fn:contains(pageContext.request.requestURI, 'exam-fee')}">
            <c:set var="activeSidebar" value="le-phi" />
        </c:when>
        <c:when test="${fn:contains(pageContext.request.requestURI, 'accounts')}">
            <c:set var="activeSidebar" value="tai-khoan" />
        </c:when>
        <c:when test="${fn:contains(pageContext.request.requestURI, 'audit')}">
            <c:set var="activeSidebar" value="audit" />
        </c:when>
        <c:otherwise>
            <c:set var="activeSidebar" value="dashboard" />
        </c:otherwise>
    </c:choose>
</c:if>

<aside class="side-nav-bar" role="navigation" aria-label="Quản trị hệ thống" data-node-id="admin:1">
    <div class="side-nav-bar__brand" data-node-id="admin:2">
        <div class="side-nav-bar__brand-inner" data-node-id="admin:3">
            <a href="${ctx}/admin/dashboard" class="side-nav-bar__logo-link" aria-label="Quản trị hệ thống">
                <img src="${logoUrl}" alt="Lái Vui" width="63" height="63" class="side-nav-bar__logo-img" data-node-id="admin:4">
            </a>
            <div class="side-nav-bar__brand-title-wrap" data-node-id="admin:5">
                <h1 class="side-nav-bar__brand-title" data-node-id="admin:6">Quản trị</h1>
            </div>
        </div>
    </div>

    <nav class="side-nav-bar__menu" data-node-id="admin:7">

        <%-- SC-070: Dashboard Admin --%>
        <a href="${ctx}/admin/dashboard"
           class="side-nav-bar__link${activeSidebar eq 'dashboard' ? ' is-active' : ''}"
           data-node-id="admin:10"
           <c:if test="${activeSidebar eq 'dashboard'}">aria-current="page"</c:if>>
            <span class="side-nav-bar__icon side-nav-bar__icon--md" aria-hidden="true">
                <svg width="24" height="24" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                    <rect x="3" y="3" width="7" height="7" rx="1.5" stroke="currentColor" stroke-width="1.5"/>
                    <rect x="14" y="3" width="7" height="7" rx="1.5" stroke="currentColor" stroke-width="1.5"/>
                    <rect x="3" y="14" width="7" height="7" rx="1.5" stroke="currentColor" stroke-width="1.5"/>
                    <rect x="14" y="14" width="7" height="7" rx="1.5" stroke="currentColor" stroke-width="1.5"/>
                </svg>
            </span>
            <span class="side-nav-bar__label" data-node-id="admin:11">Dashboard</span>
        </a>

        <%-- SC-071: Quản lý Khu vực thi --%>
        <a href="${ctx}/admin/exam-area"
           class="side-nav-bar__link${activeSidebar eq 'khu-vuc' ? ' is-active' : ''}"
           data-node-id="admin:20"
           <c:if test="${activeSidebar eq 'khu-vuc'}">aria-current="page"</c:if>>
            <span class="side-nav-bar__icon side-nav-bar__icon--md" aria-hidden="true">
                <svg width="24" height="24" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                    <path d="M12 2C8.13 2 5 5.13 5 9C5 14.25 12 22 12 22C12 22 19 14.25 19 9C19 5.13 15.87 2 12 2Z" stroke="currentColor" stroke-width="1.5" stroke-linejoin="round"/>
                    <circle cx="12" cy="9" r="2.5" stroke="currentColor" stroke-width="1.5"/>
                </svg>
            </span>
            <span class="side-nav-bar__label" data-node-id="admin:21">Khu vực thi</span>
        </a>

        <%-- SC-072: Quản lý Phòng thi --%>
        <a href="${ctx}/admin/exam-room"
           class="side-nav-bar__link${activeSidebar eq 'phong-thi' ? ' is-active' : ''}"
           data-node-id="admin:30"
           <c:if test="${activeSidebar eq 'phong-thi'}">aria-current="page"</c:if>>
            <span class="side-nav-bar__icon side-nav-bar__icon--md" aria-hidden="true">
                <svg width="24" height="24" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                    <path d="M3 21V7L12 3L21 7V21" stroke="currentColor" stroke-width="1.5" stroke-linejoin="round"/>
                    <path d="M9 21V15H15V21" stroke="currentColor" stroke-width="1.5" stroke-linejoin="round"/>
                    <path d="M9 10H10M14 10H15M9 13H10M14 13H15" stroke="currentColor" stroke-width="1.5" stroke-linecap="round"/>
                </svg>
            </span>
            <span class="side-nav-bar__label" data-node-id="admin:31">Phòng thi</span>
        </a>

        <%-- SC-073: Quản lý Máy thi --%>
        <a href="${ctx}/admin/exam-computer"
           class="side-nav-bar__link${activeSidebar eq 'may-thi' ? ' is-active' : ''}"
           data-node-id="admin:40"
           <c:if test="${activeSidebar eq 'may-thi'}">aria-current="page"</c:if>>
            <span class="side-nav-bar__icon side-nav-bar__icon--md" aria-hidden="true">
                <svg width="24" height="24" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                    <rect x="2" y="4" width="20" height="13" rx="2" stroke="currentColor" stroke-width="1.5"/>
                    <path d="M8 20H16" stroke="currentColor" stroke-width="1.5" stroke-linecap="round"/>
                    <path d="M12 17V20" stroke="currentColor" stroke-width="1.5" stroke-linecap="round"/>
                </svg>
            </span>
            <span class="side-nav-bar__label" data-node-id="admin:41">Máy thi</span>
        </a>

        <%-- SC-074: Quản lý Hạng GPLX --%>
        <a href="${ctx}/admin/licence-class"
           class="side-nav-bar__link${activeSidebar eq 'hang-gplx' ? ' is-active' : ''}"
           data-node-id="admin:50"
           <c:if test="${activeSidebar eq 'hang-gplx'}">aria-current="page"</c:if>>
            <span class="side-nav-bar__icon side-nav-bar__icon--sm" aria-hidden="true">
                <svg width="20" height="20" viewBox="0 0 20 20" fill="none" xmlns="http://www.w3.org/2000/svg">
                    <rect x="1" y="4" width="18" height="12" rx="2" stroke="currentColor" stroke-width="1.5"/>
                    <circle cx="6" cy="10" r="2" stroke="currentColor" stroke-width="1.5"/>
                    <path d="M10 7.5H16M10 10H14M10 12.5H15" stroke="currentColor" stroke-width="1.5" stroke-linecap="round"/>
                </svg>
            </span>
            <span class="side-nav-bar__label" data-node-id="admin:51">Hạng GPLX</span>
        </a>

        <%-- SC-075: Quản lý Lệ phí thi --%>
        <a href="${ctx}/admin/exam-fee"
           class="side-nav-bar__link${activeSidebar eq 'le-phi' ? ' is-active' : ''}"
           data-node-id="admin:60"
           <c:if test="${activeSidebar eq 'le-phi'}">aria-current="page"</c:if>>
            <span class="side-nav-bar__icon side-nav-bar__icon--md" aria-hidden="true">
                <svg width="24" height="24" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                    <rect x="2" y="5" width="20" height="14" rx="2" stroke="currentColor" stroke-width="1.5"/>
                    <path d="M2 10H22" stroke="currentColor" stroke-width="1.5"/>
                    <path d="M6 15H8M10 15H12" stroke="currentColor" stroke-width="1.5" stroke-linecap="round"/>
                </svg>
            </span>
            <span class="side-nav-bar__label" data-node-id="admin:61">Lệ phí thi</span>
        </a>

        <%-- SC-076: Quản lý Tài khoản hệ thống --%>
        <a href="${ctx}/admin/accounts"
           class="side-nav-bar__link${activeSidebar eq 'tai-khoan' ? ' is-active' : ''}"
           data-node-id="admin:70"
           <c:if test="${activeSidebar eq 'tai-khoan'}">aria-current="page"</c:if>>
            <span class="side-nav-bar__icon side-nav-bar__icon--user" aria-hidden="true">
                <svg width="20" height="17" viewBox="0 0 20 17" fill="none" xmlns="http://www.w3.org/2000/svg">
                    <circle cx="8" cy="5" r="3.5" stroke="currentColor" stroke-width="1.5"/>
                    <path d="M1 16C1 12.69 4.13 10 8 10C11.87 10 15 12.69 15 16" stroke="currentColor" stroke-width="1.5" stroke-linecap="round"/>
                    <path d="M15.5 6.5H19.5M17.5 4.5V8.5" stroke="currentColor" stroke-width="1.5" stroke-linecap="round"/>
                </svg>
            </span>
            <span class="side-nav-bar__label" data-node-id="admin:71">Tài khoản</span>
        </a>

        <%-- SC-077: Xem Audit toàn hệ thống --%>
        <a href="${ctx}/admin/audit"
           class="side-nav-bar__link${activeSidebar eq 'audit' ? ' is-active' : ''}"
           data-node-id="admin:80"
           <c:if test="${activeSidebar eq 'audit'}">aria-current="page"</c:if>>
            <span class="side-nav-bar__icon side-nav-bar__icon--sm" aria-hidden="true">
                <svg width="16" height="20" viewBox="0 0 16 20" fill="none" xmlns="http://www.w3.org/2000/svg">
                    <path d="M8 1L2 3.5V9.5C2 13.64 4.69 17.44 8 18.5C11.31 17.44 14 13.64 14 9.5V3.5L8 1Z" stroke="currentColor" stroke-width="1.5" stroke-linejoin="round"/>
                    <path d="M6 10L7.5 11.5L10.5 8.5" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"/>
                </svg>
            </span>
            <span class="side-nav-bar__label" data-node-id="admin:81">Nhật ký hệ thống</span>
        </a>

    </nav>

    <div class="side-nav-bar__footer" data-node-id="admin:90">
        <a href="${ctx}/change-password" class="side-nav-bar__logout" data-node-id="admin:93" style="margin-bottom:6px;">
            <span class="side-nav-bar__icon side-nav-bar__icon--xs" aria-hidden="true">
                <svg width="18" height="18" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                    <rect x="3" y="11" width="18" height="11" rx="2" stroke="currentColor" stroke-width="1.5"/>
                    <path d="M7 11V7a5 5 0 0 1 10 0v4" stroke="currentColor" stroke-width="1.5" stroke-linecap="round"/>
                </svg>
            </span>
            <span class="side-nav-bar__logout-label">Đổi mật khẩu</span>
        </a>
        <a href="${ctx}/logout" class="side-nav-bar__logout" data-node-id="admin:91">
            <span class="side-nav-bar__icon side-nav-bar__icon--xs" aria-hidden="true">
                <svg width="18" height="18" viewBox="0 0 18 18" fill="none" xmlns="http://www.w3.org/2000/svg">
                    <path d="M6.5 16H3.5C2.67 16 2 15.33 2 14.5V3.5C2 2.67 2.67 2 3.5 2H6.5" stroke="currentColor" stroke-width="1.5" stroke-linecap="round"/>
                    <path d="M12 12.5L16 9L12 5.5" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"/>
                    <path d="M16 9H7" stroke="currentColor" stroke-width="1.5" stroke-linecap="round"/>
                </svg>
            </span>
            <span class="side-nav-bar__logout-label" data-node-id="admin:92">Đăng xuất</span>
        </a>
    </div>
</aside>

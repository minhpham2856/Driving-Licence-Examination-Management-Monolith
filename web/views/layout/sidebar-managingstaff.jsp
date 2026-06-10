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
        <c:when test="${fn:contains(pageContext.request.requestURI, 'users') or fn:contains(pageContext.request.requestURI, 'user-detail')}">
            <c:set var="activeSidebar" value="hoc-vien" />
        </c:when>
        <c:when test="${fn:contains(pageContext.request.requestURI, 'approve')}">
            <c:set var="activeSidebar" value="duyet-ho-so" />
        </c:when>
        <c:when test="${fn:contains(pageContext.request.requestURI, 'create-user')}">
            <c:set var="activeSidebar" value="tao-tai-khoan" />
        </c:when>
        <c:when test="${fn:contains(pageContext.request.requestURI, 'report')}">
            <c:set var="activeSidebar" value="bao-cao" />
        </c:when>
        <c:when test="${fn:contains(pageContext.request.requestURI, 'audit')}">
            <c:set var="activeSidebar" value="nhat-ky" />
        </c:when>
        <c:otherwise>
            <c:set var="activeSidebar" value="dashboard" />
        </c:otherwise>
    </c:choose>
</c:if>

<aside class="side-nav-bar" role="navigation" aria-label="Ban quản lý đào tạo" data-node-id="manager:1">
    <div class="side-nav-bar__brand" data-node-id="manager:2">
        <div class="side-nav-bar__brand-inner" data-node-id="manager:3">
            <a href="#" class="side-nav-bar__logo-link" aria-label="Quản lý đào tạo">
                <img src="${logoUrl}" alt="Lái Vui" width="63" height="63" class="side-nav-bar__logo-img" data-node-id="manager:4">
            </a>
            <div class="side-nav-bar__brand-title-wrap" data-node-id="manager:5">
                <h1 class="side-nav-bar__brand-title" data-node-id="manager:6">Ban Quản Lý</h1>
            </div>
        </div>
    </div>

    <nav class="side-nav-bar__menu" data-node-id="manager:7">
        
        <%-- SC-030: Dashboard quản lý --%>
        <a href="${ctx}/views/staff/managingstaff/dashboard.jsp"
           class="side-nav-bar__link${activeSidebar eq 'dashboard' ? ' is-active' : ''}"
           data-node-id="manager:10"
           <c:if test="${activeSidebar eq 'dashboard'}">aria-current="page"</c:if>>
            <span class="side-nav-bar__icon side-nav-bar__icon--md" aria-hidden="true">
                <svg width="24" height="24" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                    <rect x="3" y="3" width="7" height="7" rx="1.5" stroke="currentColor" stroke-width="1.5"/>
                    <rect x="14" y="3" width="7" height="7" rx="1.5" stroke="currentColor" stroke-width="1.5"/>
                    <rect x="3" y="14" width="7" height="7" rx="1.5" stroke="currentColor" stroke-width="1.5"/>
                    <rect x="14" y="14" width="7" height="7" rx="1.5" stroke="currentColor" stroke-width="1.5"/>
                </svg>
            </span>
            <span class="side-nav-bar__label" data-node-id="manager:11">Dashboard</span>
        </a>

        <%-- SC-031: Danh sách registered users --%>
        <a href="${ctx}/views/staff/managingstaff/users.jsp"
           class="side-nav-bar__link${activeSidebar eq 'hoc-vien' ? ' is-active' : ''}"
           data-node-id="manager:20"
           <c:if test="${activeSidebar eq 'hoc-vien'}">aria-current="page"</c:if>>
            <span class="side-nav-bar__icon side-nav-bar__icon--user" aria-hidden="true">
                <svg width="24" height="24" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                    <path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2" stroke="currentColor" stroke-width="1.5" stroke-linecap="round"/>
                    <circle cx="9" cy="7" r="4" stroke="currentColor" stroke-width="1.5"/>
                    <path d="M23 21v-2a4 4 0 0 0-3-3.87" stroke="currentColor" stroke-width="1.5" stroke-linecap="round"/>
                    <path d="M16 3.13a4 4 0 0 1 0 7.75" stroke="currentColor" stroke-width="1.5" stroke-linecap="round"/>
                </svg>
            </span>
            <span class="side-nav-bar__label" data-node-id="manager:21">Danh sách học viên</span>
        </a>

        <%-- SC-033: Duyệt / Từ chối hồ sơ, giấy tờ --%>
        <a href="${ctx}/views/staff/managingstaff/approve.jsp"
           class="side-nav-bar__link${activeSidebar eq 'duyet-ho-so' ? ' is-active' : ''}"
           data-node-id="manager:30"
           <c:if test="${activeSidebar eq 'duyet-ho-so'}">aria-current="page"</c:if>>
            <span class="side-nav-bar__icon side-nav-bar__icon--md" aria-hidden="true">
                <svg width="24" height="24" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                    <path d="M9 11L12 14L22 4" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"/>
                    <path d="M21 12v7a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h11" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"/>
                </svg>
            </span>
            <span class="side-nav-bar__label" data-node-id="manager:31">Duyệt hồ sơ</span>
        </a>

        <%-- SC-034: Tạo tài khoản mới cho học viên --%>
        <a href="${ctx}/views/staff/managingstaff/create-user.jsp"
           class="side-nav-bar__link${activeSidebar eq 'tao-tai-khoan' ? ' is-active' : ''}"
           data-node-id="manager:40"
           <c:if test="${activeSidebar eq 'tao-tai-khoan'}">aria-current="page"</c:if>>
            <span class="side-nav-bar__icon side-nav-bar__icon--md" aria-hidden="true">
                <svg width="24" height="24" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                    <path d="M16 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2" stroke="currentColor" stroke-width="1.5" stroke-linecap="round"/>
                    <circle cx="8" cy="7" r="4" stroke="currentColor" stroke-width="1.5"/>
                    <path d="M20 8v6M17 11h6" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"/>
                </svg>
            </span>
            <span class="side-nav-bar__label" data-node-id="manager:41">Tạo tài khoản</span>
        </a>

        <%-- SC-035: Báo cáo quản lý --%>
        <a href="${ctx}/views/staff/managingstaff/report.jsp"
           class="side-nav-bar__link${activeSidebar eq 'bao-cao' ? ' is-active' : ''}"
           data-node-id="manager:50"
           <c:if test="${activeSidebar eq 'bao-cao'}">aria-current="page"</c:if>>
            <span class="side-nav-bar__icon side-nav-bar__icon--xs" aria-hidden="true">
                <svg width="18" height="18" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                    <rect x="1" y="1" width="16" height="16" rx="2" stroke="currentColor" stroke-width="1.5"/>
                    <path d="M5 12V9M9 12V6M13 12V8" stroke="currentColor" stroke-width="1.5" stroke-linecap="round"/>
                </svg>
            </span>
            <span class="side-nav-bar__label" data-node-id="manager:51">Báo cáo thống kê</span>
        </a>

        <%-- SC-036: Nhật ký thao tác --%>
        <a href="${ctx}/views/staff/managingstaff/audit.jsp"
           class="side-nav-bar__link${activeSidebar eq 'nhat-ky' ? ' is-active' : ''}"
           data-node-id="manager:60"
           <c:if test="${activeSidebar eq 'nhat-ky'}">aria-current="page"</c:if>>
            <span class="side-nav-bar__icon side-nav-bar__icon--sm" aria-hidden="true">
                <svg width="16" height="20" viewBox="0 0 16 20" fill="none" xmlns="http://www.w3.org/2000/svg">
                    <path d="M8 1L2 3.5V9.5C2 13.64 4.69 17.44 8 18.5C11.31 17.44 14 13.64 14 9.5V3.5L8 1Z" stroke="currentColor" stroke-width="1.5" stroke-linejoin="round"/>
                    <path d="M6 10L7.5 11.5L10.5 8.5" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"/>
                </svg>
            </span>
            <span class="side-nav-bar__label" data-node-id="manager:61">Nhật ký thao tác</span>
        </a>

    </nav>

    <div class="side-nav-bar__footer" data-node-id="manager:90">
        <a href="#" class="side-nav-bar__logout" data-node-id="manager:91">
            <span class="side-nav-bar__icon side-nav-bar__icon--xs" aria-hidden="true">
                <svg width="18" height="18" viewBox="0 0 18 18" fill="none" xmlns="http://www.w3.org/2000/svg">
                    <path d="M6.5 16H3.5C2.67 16 2 15.33 2 14.5V3.5C2 2.67 2.67 2 3.5 2H6.5" stroke="currentColor" stroke-width="1.5" stroke-linecap="round"/>
                    <path d="M12 12.5L16 9L12 5.5" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"/>
                    <path d="M16 9H7" stroke="currentColor" stroke-width="1.5" stroke-linecap="round"/>
                </svg>
            </span>
            <span class="side-nav-bar__logout-label" data-node-id="manager:92">Đăng xuất</span>
        </a>
    </div>
</aside>

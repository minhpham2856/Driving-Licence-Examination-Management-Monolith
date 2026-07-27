<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<c:set var="ctx" value="${pageContext.request.contextPath}" />
<c:set var="activeSidebar" value="${param.activeSidebar}" />
<aside class="side-nav-bar" role="navigation" aria-label="Cổng cán bộ CSGT">
    <div class="side-nav-bar__brand">
        <div class="side-nav-bar__brand-inner">
            <a href="${ctx}/police/dashboard" class="side-nav-bar__logo-link" aria-label="Về Dashboard CSGT">
                <img src="${ctx}/assets/imgs/LOGO.png" alt="Lái Vui" width="63" height="63" class="side-nav-bar__logo-img">
            </a>
            <div class="side-nav-bar__brand-title-wrap">
                <h1 class="side-nav-bar__brand-title">Cán bộ CSGT</h1>
            </div>
        </div>
    </div>
    <nav class="side-nav-bar__menu">
        <a href="${ctx}/police/dashboard"
           class="side-nav-bar__link${activeSidebar eq 'dashboard' ? ' is-active' : ''}"
           <c:if test="${activeSidebar eq 'dashboard'}">aria-current="page"</c:if>>
            <span class="side-nav-bar__icon" aria-hidden="true">
                <svg width="24" height="24" viewBox="0 0 24 24" fill="none"><rect x="3" y="3" width="7" height="7" rx="1.5" stroke="currentColor" stroke-width="1.5"/><rect x="14" y="3" width="7" height="7" rx="1.5" stroke="currentColor" stroke-width="1.5"/><rect x="3" y="14" width="7" height="7" rx="1.5" stroke="currentColor" stroke-width="1.5"/><rect x="14" y="14" width="7" height="7" rx="1.5" stroke="currentColor" stroke-width="1.5"/></svg>
            </span>
            <span class="side-nav-bar__label">Tiếp nhận & thẩm định</span>
        </a>
        <a href="${ctx}/police/official-rosters"
           class="side-nav-bar__link${activeSidebar eq 'rosters' ? ' is-active' : ''}"
           <c:if test="${activeSidebar eq 'rosters'}">aria-current="page"</c:if>>
            <span class="side-nav-bar__icon" aria-hidden="true">
                <svg width="24" height="24" viewBox="0 0 24 24" fill="none"><path d="M7 3h10v4H7zM5 7h14v14H5z" stroke="currentColor" stroke-width="1.5"/><path d="M8 12h8M8 16h8" stroke="currentColor" stroke-width="1.5" stroke-linecap="round"/></svg>
            </span>
            <span class="side-nav-bar__label">Danh sách thi chính thức</span>
        </a>
        <a href="${ctx}/police/profile" class="side-nav-bar__link${activeSidebar eq 'profile' ? ' is-active' : ''}">
            <span class="side-nav-bar__icon" aria-hidden="true">
                <svg width="24" height="24" viewBox="0 0 24 24" fill="none"><circle cx="12" cy="8" r="4" stroke="currentColor" stroke-width="1.5"/><path d="M4 21a8 8 0 0 1 16 0" stroke="currentColor" stroke-width="1.5" stroke-linecap="round"/></svg>
            </span>
            <span class="side-nav-bar__label">Hồ sơ cá nhân</span>
        </a>
        <a href="${ctx}/police/change-password" class="side-nav-bar__link${activeSidebar eq 'change-password' ? ' is-active' : ''}">
            <span class="side-nav-bar__icon" aria-hidden="true">
                <svg width="24" height="24" viewBox="0 0 24 24" fill="none">
                    <rect x="5" y="10" width="14" height="10" rx="2" stroke="currentColor" stroke-width="1.5"/>
                    <path d="M8 10V7a4 4 0 0 1 8 0v3M12 14v2" stroke="currentColor" stroke-width="1.5" stroke-linecap="round"/>
                </svg>
            </span>
            <span class="side-nav-bar__label">Đổi mật khẩu</span>
        </a>
    </nav>
    <div class="side-nav-bar__footer">
        <a href="${ctx}/logout" class="side-nav-bar__logout">
            <span class="side-nav-bar__logout-label">Đăng xuất</span>
        </a>
    </div>
</aside>

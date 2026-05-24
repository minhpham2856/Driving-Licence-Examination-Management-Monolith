<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib prefix = "fn" uri = "http://java.sun.com/jsp/jstl/functions" %>

<c:set var="ctx" value="${pageContext.request.contextPath}" />
<c:set var="logoUrl" value="${ctx}/assets/imgs/LOGO.png" />

<c:set var="activeSidebar" value="${param.activeSidebar}" />
<c:if test="${empty activeSidebar}">
    <c:choose>
        <c:when test="${fn:contains(pageContext.request.requestURI, 'upload')}">
            <c:set var="activeSidebar" value="tai-ds" />
        </c:when>
        <c:when test="${fn:contains(pageContext.request.requestURI, 'grading') or fn:contains(pageContext.request.requestURI, 'cham-diem')}">
            <c:set var="activeSidebar" value="cham-diem" />
        </c:when>
        <c:when test="${fn:contains(pageContext.request.requestURI, 'edit-score') or fn:contains(pageContext.request.requestURI, 'sua-diem')}">
            <c:set var="activeSidebar" value="sua-diem" />
        </c:when>
        <c:when test="${fn:contains(pageContext.request.requestURI, 'candidates') or fn:contains(pageContext.request.requestURI, 'ds-thi-sinh')}">
            <c:set var="activeSidebar" value="ds-thi-sinh" />
        </c:when>
        <c:when test="${fn:contains(pageContext.request.requestURI, 'report') or fn:contains(pageContext.request.requestURI, 'bao-cao')}">
            <c:set var="activeSidebar" value="bao-cao" />
        </c:when>
        <c:when test="${fn:contains(pageContext.request.requestURI, 'audit') or fn:contains(pageContext.request.requestURI, 'nhat-ky')}">
            <c:set var="activeSidebar" value="nhat-ky" />
        </c:when>
        <c:otherwise>
            <c:set var="activeSidebar" value="ds-thi-sinh" />
        </c:otherwise>
    </c:choose>
</c:if>

<aside class="side-nav-bar" role="navigation" aria-label="Quản lý thi" data-node-id="4:4">
    <div class="side-nav-bar__brand" data-node-id="4:750">
        <div class="side-nav-bar__brand-inner" data-node-id="4:751">
            <a href="#" class="side-nav-bar__logo-link" aria-label="Quản lý thi">
                <img src="${logoUrl}" alt="Lái Vui" width="63" height="63" class="side-nav-bar__logo-img" data-node-id="4:752">
            </a>
            <div class="side-nav-bar__brand-title-wrap" data-node-id="4:753">
                <h1 class="side-nav-bar__brand-title" data-node-id="4:755">Quản lý thi</h1>
            </div>
        </div>
    </div>

    <nav class="side-nav-bar__menu" data-node-id="4:756">
        <a href="#"
           class="side-nav-bar__link${activeSidebar eq 'tai-ds' ? ' is-active' : ''}"
           data-node-id="4:757"
           <c:if test="${activeSidebar eq 'tai-ds'}">aria-current="page"</c:if>>
            <span class="side-nav-bar__icon side-nav-bar__icon--sm" aria-hidden="true">
                <svg width="16" height="20" viewBox="0 0 16 20" fill="none" xmlns="http://www.w3.org/2000/svg">
                    <path d="M9 1H3.5C2.67 1 2 1.67 2 2.5V17.5C2 18.33 2.67 19 3.5 19H12.5C13.33 19 14 18.33 14 17.5V6L9 1Z" stroke="currentColor" stroke-width="1.5" stroke-linejoin="round"/>
                    <path d="M9 1V6H14M8 11V15M6 13H10" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"/>
                </svg>
            </span>
            <span class="side-nav-bar__label" data-node-id="4:761">Tải DS Thí sinh</span>
        </a>

        <a href="${pageContext.request.contextPath}/views/examiner/grading.jsp"
           class="side-nav-bar__link${activeSidebar eq 'cham-diem' ? ' is-active' : ''}"
           data-node-id="4:762"
           <c:if test="${activeSidebar eq 'cham-diem'}">aria-current="page"</c:if>>
            <span class="side-nav-bar__icon side-nav-bar__icon--md" aria-hidden="true">
                <svg width="24" height="24" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                    <path d="M6 18L10 14L13 17L18 11" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"/>
                    <path d="M15 11H18V14" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"/>
                    <path d="M7 4H12L14 6H19C19.55 6 20 6.45 20 7V18C20 18.55 19.55 19 19 19H5C4.45 19 4 18.55 4 18V5C4 4.45 4.45 4 5 4H7Z" stroke="currentColor" stroke-width="1.5" stroke-linejoin="round"/>
                </svg>
            </span>
            <span class="side-nav-bar__label" data-node-id="4:766">Chấm điểm</span>
        </a>

        <a href="#"
           class="side-nav-bar__link${activeSidebar eq 'sua-diem' ? ' is-active' : ''}"
           data-node-id="4:767"
           <c:if test="${activeSidebar eq 'sua-diem'}">aria-current="page"</c:if>>
            <span class="side-nav-bar__icon side-nav-bar__icon--md" aria-hidden="true">
                <svg width="24" height="24" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                    <path d="M12 20H20" stroke="currentColor" stroke-width="1.5" stroke-linecap="round"/>
                    <path d="M16.5 3.5C17.05 3 17.95 3 18.5 3.5C19.05 4.05 19.05 4.95 18.5 5.5L7 17L3 18L4 14L15.5 3.5Z" stroke="currentColor" stroke-width="1.5" stroke-linejoin="round"/>
                </svg>
            </span>
            <span class="side-nav-bar__label" data-node-id="4:771">Sửa điểm</span>
        </a>

        <a href="#"
           class="side-nav-bar__link${activeSidebar eq 'ds-thi-sinh' ? ' is-active' : ''}"
           data-node-id="4:772"
           <c:if test="${activeSidebar eq 'ds-thi-sinh'}">aria-current="page"</c:if>>
            <span class="side-nav-bar__icon side-nav-bar__icon--user" aria-hidden="true">
                <svg width="20" height="17" viewBox="0 0 20 17" fill="none" xmlns="http://www.w3.org/2000/svg">
                    <circle cx="8" cy="5" r="3.5" stroke="currentColor" stroke-width="1.5"/>
                    <path d="M1 16C1 12.69 4.13 10 8 10C11.87 10 15 12.69 15 16" stroke="currentColor" stroke-width="1.5" stroke-linecap="round"/>
                    <path d="M14.5 3.5L16.5 5.5L20 2" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"/>
                </svg>
            </span>
            <span class="side-nav-bar__label" data-node-id="4:776">DS thí sinh</span>
        </a>

        <a href="#"
           class="side-nav-bar__link${activeSidebar eq 'bao-cao' ? ' is-active' : ''}"
           data-node-id="4:777"
           <c:if test="${activeSidebar eq 'bao-cao'}">aria-current="page"</c:if>>
            <span class="side-nav-bar__icon side-nav-bar__icon--xs" aria-hidden="true">
                <svg width="18" height="18" viewBox="0 0 18 18" fill="none" xmlns="http://www.w3.org/2000/svg">
                    <rect x="1" y="1" width="16" height="16" rx="2" stroke="currentColor" stroke-width="1.5"/>
                    <path d="M5 12V9M9 12V6M13 12V8" stroke="currentColor" stroke-width="1.5" stroke-linecap="round"/>
                </svg>
            </span>
            <span class="side-nav-bar__label" data-node-id="4:781">Báo cáo</span>
        </a>

        <a href="#"
           class="side-nav-bar__link${activeSidebar eq 'nhat-ky' ? ' is-active' : ''}"
           data-node-id="4:782"
           <c:if test="${activeSidebar eq 'nhat-ky'}">aria-current="page"</c:if>>
            <span class="side-nav-bar__icon side-nav-bar__icon--sm" aria-hidden="true">
                <svg width="16" height="20" viewBox="0 0 16 20" fill="none" xmlns="http://www.w3.org/2000/svg">
                    <path d="M8 1L2 3.5V9.5C2 13.64 4.69 17.44 8 18.5C11.31 17.44 14 13.64 14 9.5V3.5L8 1Z" stroke="currentColor" stroke-width="1.5" stroke-linejoin="round"/>
                    <path d="M6 10L7.5 11.5L10.5 8.5" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"/>
                </svg>
            </span>
            <span class="side-nav-bar__label" data-node-id="4:786">Nhật ký thao tác</span>
        </a>
    </nav>

    <div class="side-nav-bar__footer" data-node-id="4:787">
        <a href="#" class="side-nav-bar__logout" data-node-id="4:788">
            <span class="side-nav-bar__icon side-nav-bar__icon--xs" aria-hidden="true">
                <svg width="18" height="18" viewBox="0 0 18 18" fill="none" xmlns="http://www.w3.org/2000/svg">
                    <path d="M6.5 16H3.5C2.67 16 2 15.33 2 14.5V3.5C2 2.67 2.67 2 3.5 2H6.5" stroke="currentColor" stroke-width="1.5" stroke-linecap="round"/>
                    <path d="M12 12.5L16 9L12 5.5" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"/>
                    <path d="M16 9H7" stroke="currentColor" stroke-width="1.5" stroke-linecap="round"/>
                </svg>
            </span>
            <span class="side-nav-bar__logout-label" data-node-id="4:792">Đăng xuất</span>
        </a>
    </div>
</aside>

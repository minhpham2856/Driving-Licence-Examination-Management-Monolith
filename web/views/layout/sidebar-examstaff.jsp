<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" language="java" %>
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
        <c:when test="${fn:contains(pageContext.request.requestURI, 'grading')}">
            <c:set var="activeSidebar" value="cham-diem" />
        </c:when>
        <c:when test="${fn:contains(pageContext.request.requestURI, 'edit-score')}">
            <c:set var="activeSidebar" value="sua-diem" />
        </c:when>
        <c:when test="${fn:contains(pageContext.request.requestURI, 'candidatelist') or fn:contains(pageContext.request.requestURI, 'candidates')}">
            <c:set var="activeSidebar" value="ds-thi-sinh" />
        </c:when>
        <c:when test="${fn:contains(pageContext.request.requestURI, 'report')}">
            <c:set var="activeSidebar" value="bao-cao" />
        </c:when>
        <c:when test="${fn:contains(pageContext.request.requestURI, 'audit')}">
            <c:set var="activeSidebar" value="audit" />
        </c:when>
        <c:otherwise>
            <c:set var="activeSidebar" value="dashboard" />
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
                <h1 class="side-nav-bar__brand-title" data-node-id="4:755">Ban Sát Hạch</h1>
            </div>
        </div>
    </div>

    <nav class="side-nav-bar__menu" data-node-id="4:756">
        <a href="${ctx}/views/staff/examstaff/dashboard.jsp"
           class="side-nav-bar__link${activeSidebar eq 'dashboard' ? ' is-active' : ''}"
           data-node-id="examiner:dash"
           <c:if test="${activeSidebar eq 'dashboard'}">aria-current="page"</c:if>>
            <span class="side-nav-bar__icon side-nav-bar__icon--md" aria-hidden="true">
                <svg width="24" height="24" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                    <rect x="3" y="3" width="7" height="7" rx="1.5" stroke="currentColor" stroke-width="1.5"/>
                    <rect x="14" y="3" width="7" height="7" rx="1.5" stroke="currentColor" stroke-width="1.5"/>
                    <rect x="3" y="14" width="7" height="7" rx="1.5" stroke="currentColor" stroke-width="1.5"/>
                    <rect x="14" y="14" width="7" height="7" rx="1.5" stroke="currentColor" stroke-width="1.5"/>
                </svg>
            </span>
            <span class="side-nav-bar__label" data-node-id="examiner:dash-lbl">Tổng quan ca thi</span>
        </a>

        <a href="${pageContext.request.contextPath}/views/staff/examstaff/allocation"
           class="side-nav-bar__link${activeSidebar eq 'phan-bo' ? ' is-active' : ''}"
           data-node-id="4:762"
           <c:if test="${activeSidebar eq 'phan-bo'}">aria-current="page"</c:if>>
            <span class="side-nav-bar__icon side-nav-bar__icon--md" aria-hidden="true">
                <svg width="24" height="24" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                    <rect x="3" y="3" width="7" height="9" rx="1.5" stroke="currentColor" stroke-width="1.5"/>
                    <rect x="14" y="3" width="7" height="5" rx="1.5" stroke="currentColor" stroke-width="1.5"/>
                    <rect x="14" y="12" width="7" height="9" rx="1.5" stroke="currentColor" stroke-width="1.5"/>
                    <rect x="3" y="16" width="7" height="5" rx="1.5" stroke="currentColor" stroke-width="1.5"/>
                </svg>
            </span>
            <span class="side-nav-bar__label" data-node-id="4:766">Phân bổ thí sinh</span>
        </a>

        <a href="${pageContext.request.contextPath}/views/staff/examstaff/examiner-allocation"
           class="side-nav-bar__link${activeSidebar eq 'phan-bo-giam-khao' ? ' is-active' : ''}"
           <c:if test="${activeSidebar eq 'phan-bo-giam-khao'}">aria-current="page"</c:if>>
            <span class="side-nav-bar__icon side-nav-bar__icon--md" aria-hidden="true">
                <svg width="24" height="24" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                    <circle cx="9" cy="8" r="3.5" stroke="currentColor" stroke-width="1.5"/>
                    <path d="M2 20c0-3.5 3-6 7-6s7 2.5 7 6" stroke="currentColor" stroke-width="1.5" stroke-linecap="round"/>
                    <rect x="14" y="4" width="8" height="6" rx="1.5" stroke="currentColor" stroke-width="1.5"/>
                    <path d="M16 14h6M16 17h4" stroke="currentColor" stroke-width="1.5" stroke-linecap="round"/>
                </svg>
            </span>
            <span class="side-nav-bar__label">Phân bổ sát hạch viên</span>
        </a>

        <a href="${pageContext.request.contextPath}/views/staff/examstaff/candidatecall"
           class="side-nav-bar__link${activeSidebar eq 'goi-thi' ? ' is-active' : ''}"
           data-node-id="4:767"
           <c:if test="${activeSidebar eq 'goi-thi'}">aria-current="page"</c:if>>
            <span class="side-nav-bar__icon side-nav-bar__icon--md" aria-hidden="true">
                <svg width="24" height="24" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                    <path d="M11 5L6 9H2v6h4l5 4V5z" stroke="currentColor" stroke-width="1.5" stroke-linejoin="round"/>
                    <path d="M15.54 8.46a5 5 0 0 1 0 7.07M19.07 4.93a10 10 0 0 1 0 14.14" stroke="currentColor" stroke-width="1.5" stroke-linecap="round"/>
                </svg>
            </span>
            <span class="side-nav-bar__label" data-node-id="4:771">Gọi làm thủ tục</span>
        </a>

        <a href="${ctx}/views/staff/examstaff/report.jsp"
           class="side-nav-bar__link${activeSidebar eq 'bao-cao' ? ' is-active' : ''}"
           data-node-id="4:777"
           <c:if test="${activeSidebar eq 'bao-cao'}">aria-current="page"</c:if>>
            <span class="side-nav-bar__icon side-nav-bar__icon--xs" aria-hidden="true">
                <svg width="18" height="18" viewBox="0 0 18 18" fill="none" xmlns="http://www.w3.org/2000/svg">
                    <rect x="1" y="1" width="16" height="16" rx="2" stroke="currentColor" stroke-width="1.5"/>
                    <path d="M5 12V9M9 12V6M13 12V8" stroke="currentColor" stroke-width="1.5" stroke-linecap="round"/>
                </svg>
            </span>
            <span class="side-nav-bar__label" data-node-id="4:781">Báo cáo cuối ngày</span>
        </a>

        <a href="${ctx}/views/staff/examstaff/audit.jsp"
           class="side-nav-bar__link${activeSidebar eq 'audit' ? ' is-active' : ''}"
           data-node-id="4:782"
           <c:if test="${activeSidebar eq 'audit'}">aria-current="page"</c:if>>
            <span class="side-nav-bar__icon side-nav-bar__icon--sm" aria-hidden="true">
                <svg width="16" height="20" viewBox="0 0 16 20" fill="none" xmlns="http://www.w3.org/2000/svg">
                    <path d="M8 1L2 3.5V9.5C2 13.64 4.69 17.44 8 18.5C11.31 17.44 14 13.64 14 9.5V3.5L8 1Z" stroke="currentColor" stroke-width="1.5" stroke-linejoin="round"/>
                    <path d="M6 10L7.5 11.5L10.5 8.5" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"/>
                </svg>
            </span>
            <span class="side-nav-bar__label" data-node-id="4:786">Nhật ký cá nhân</span>
        </a>

        <!-- Public Live Displays SC-080 / SC-081 -->
        <div style="margin: 1.25rem 1.25rem 0.5rem; font-size: 0.72rem; font-weight: 800; color: #64748b; text-transform: uppercase; letter-spacing: 0.05em;">Màn hình công cộng</div>

        <a href="${ctx}/views/public/public-call"
           class="side-nav-bar__link${activeSidebar eq 'public-call' ? ' is-active' : ''}"
           <c:if test="${activeSidebar eq 'public-call'}">aria-current="page"</c:if>>
            <span class="side-nav-bar__icon side-nav-bar__icon--sm" aria-hidden="true">
                <svg width="24" height="24" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                    <path d="M11 5L6 9H2v6h4l5 4V5z" stroke="currentColor" stroke-width="1.5" stroke-linejoin="round"/>
                    <path d="M15.54 8.46a5 5 0 0 1 0 7.07M19.07 4.93a10 10 0 0 1 0 14.14" stroke="currentColor" stroke-width="1.5" stroke-linecap="round"/>
                </svg>
            </span>
            <span class="side-nav-bar__label">Màn hình gọi TV</span>
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

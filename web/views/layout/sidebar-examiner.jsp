<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>

<c:set var="ctx" value="${pageContext.request.contextPath}" />
<c:set var="activeSidebar" value="${param.activeSidebar}" />
<c:set var="requestUri" value="${pageContext.request.requestURI}" />

<c:if test="${empty activeSidebar}">
    <c:choose>
        <c:when test="${fn:contains(requestUri, 'dashboard')}"><c:set var="activeSidebar" value="dashboard" /></c:when>
        <c:when test="${fn:contains(requestUri, 'action') or fn:contains(requestUri, 'score-entry')}"><c:set var="activeSidebar" value="action" /></c:when>
        <c:when test="${fn:contains(requestUri, 'candidates') or fn:contains(requestUri, 'candidate-details') or fn:contains(requestUri, 'candidate-paper') or fn:contains(requestUri, 'violations') or fn:contains(requestUri, 'result-details')}"><c:set var="activeSidebar" value="details" /></c:when>
        <c:when test="${fn:contains(requestUri, 'devices')}"><c:set var="activeSidebar" value="devices" /></c:when>
        <c:when test="${fn:contains(requestUri, 'print-documents') or fn:contains(requestUri, 'export')}"><c:set var="activeSidebar" value="print-documents" /></c:when>
        <c:when test="${fn:contains(requestUri, 'audit')}"><c:set var="activeSidebar" value="audit" /></c:when>
        <c:otherwise><c:set var="activeSidebar" value="dashboard" /></c:otherwise>
    </c:choose>
</c:if>

<aside class="side-nav-bar side-nav-bar--examiner">
    <div class="side-nav-bar__brand">
        <div class="side-nav-bar__brand-inner">
            <img src="${ctx}/assets/imgs/csgt-footer.png" alt="CSGT" width="48" class="side-nav-bar__logo-img">
            <div class="side-nav-bar__brand-title-wrap">
                <h1 class="side-nav-bar__brand-title">Sát hạch viên</h1>
                <p class="side-nav-bar__brand-subtitle">${sessionScope.userProfile.fullName}</p>
            </div>
        </div>
    </div>

    <nav class="side-nav-bar__menu${empty examinerHasActiveExam or not examinerHasActiveExam ? ' side-nav-bar__menu--locked' : ''}">
        <a href="${ctx}/examiner/dashboard"
           class="side-nav-bar__link${activeSidebar eq 'dashboard' ? ' is-active' : ''}">
            <span class="side-nav-bar__icon material-symbols-outlined">grid_view</span>
            <span class="side-nav-bar__label">Bảng điều khiển</span>
        </a>

        <a href="${ctx}/examiner/action"
           class="side-nav-bar__link${activeSidebar eq 'action' ? ' is-active' : ''}">
            <span class="side-nav-bar__icon material-symbols-outlined">campaign</span>
            <span class="side-nav-bar__label">Thao tác</span>
        </a>

        <a href="${ctx}/examiner/candidates"
           class="side-nav-bar__link${activeSidebar eq 'details' or activeSidebar eq 'candidates' or activeSidebar eq 'candidate-details' or activeSidebar eq 'violations' or activeSidebar eq 'result-details' ? ' is-active' : ''}">
            <span class="side-nav-bar__icon material-symbols-outlined">edit_document</span>
            <span class="side-nav-bar__label">Thông tin chi tiết</span>
        </a>

        <a href="${ctx}/examiner/devices"
           class="side-nav-bar__link${activeSidebar eq 'devices' ? ' is-active' : ''}">
            <span class="side-nav-bar__icon material-symbols-outlined">devices</span>
            <span class="side-nav-bar__label">Thiết bị</span>
        </a>

        <a href="${ctx}/examiner/print-documents"
           class="side-nav-bar__link${activeSidebar eq 'print-documents' or activeSidebar eq 'export' ? ' is-active' : ''}">
            <span class="side-nav-bar__icon material-symbols-outlined">print</span>
            <span class="side-nav-bar__label">Biên bản</span>
        </a>

        <a href="${ctx}/examiner/audit"
           class="side-nav-bar__link${activeSidebar eq 'audit' ? ' is-active' : ''}">
            <span class="side-nav-bar__icon material-symbols-outlined">history</span>
            <span class="side-nav-bar__label">Nhật ký</span>
        </a>
    </nav>

    <div class="side-nav-bar__footer">
        <a href="${ctx}/examiner/profile" class="side-nav-bar__logout">
            <span class="side-nav-bar__icon material-symbols-outlined">account_circle</span>
            <span class="side-nav-bar__logout-label">Hồ sơ cá nhân</span>
        </a>
        <a href="${ctx}/examiner/change-password" class="side-nav-bar__logout">
            <span class="side-nav-bar__icon material-symbols-outlined">lock_reset</span>
            <span class="side-nav-bar__logout-label">Đổi mật khẩu</span>
        </a>
        <a href="${ctx}/staff/logout" class="side-nav-bar__logout">
            <span class="side-nav-bar__icon material-symbols-outlined">logout</span>
            <span class="side-nav-bar__logout-label">Đăng xuất</span>
        </a>
    </div>
</aside>

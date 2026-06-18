<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>

<c:set var="ctx" value="${pageContext.request.contextPath}" />
<c:set var="logoUrl" value="${ctx}/assets/imgs/LOGO.png" />
<c:set var="activeSidebar" value="${param.activeSidebar}" />
<c:set var="requestUri" value="${pageContext.request.requestURI}" />

<c:if test="${empty activeSidebar}">
    <c:choose>
        <c:when test="${fn:contains(requestUri, 'dashboard')}"><c:set var="activeSidebar" value="dashboard" /></c:when>
        <c:when test="${fn:contains(requestUri, 'score-entry')}"><c:set var="activeSidebar" value="nhap-diem" /></c:when>
        <c:when test="${fn:contains(requestUri, 'candidate-call') or fn:contains(requestUri, 'confirmation')}"><c:set var="activeSidebar" value="goi-thi-sinh" /></c:when>
        <c:when test="${fn:contains(requestUri, 'violations') or fn:contains(requestUri, 'violation-')}"><c:set var="activeSidebar" value="vi-pham" /></c:when>
        <c:when test="${fn:contains(requestUri, 'candidate-detail')}"><c:set var="activeSidebar" value="sua-thong-tin" /></c:when>
        <c:when test="${fn:contains(requestUri, 'result-detail')}"><c:set var="activeSidebar" value="sua-ket-qua" /></c:when>
        <c:when test="${fn:contains(requestUri, 'devices')}"><c:set var="activeSidebar" value="thiet-bi" /></c:when>
        <c:when test="${fn:contains(requestUri, 'export')}"><c:set var="activeSidebar" value="xuat-file" /></c:when>
        <c:when test="${fn:contains(requestUri, 'print-documents')}"><c:set var="activeSidebar" value="in-van-ban" /></c:when>
        <c:when test="${fn:contains(requestUri, 'audit')}"><c:set var="activeSidebar" value="nhat-ky" /></c:when>
        <c:otherwise><c:set var="activeSidebar" value="dashboard" /></c:otherwise>
    </c:choose>
</c:if>

<aside class="side-nav-bar side-nav-bar--examiner">
    <div class="side-nav-bar__brand">
        <div class="side-nav-bar__brand-inner">
            <a href="${ctx}/views/examiner/dashboard" class="side-nav-bar__logo-link">
                <img src="${logoUrl}" width="40" height="40" class="side-nav-bar__logo-img" alt="">
            </a>
            <div class="side-nav-bar__brand-title-wrap">
                <h1 class="side-nav-bar__brand-title">Sát hạch viên</h1>
                <p class="side-nav-bar__brand-subtitle">${sessionScope.user.profile.fullName}</p>
            </div>
        </div>
    </div>

    <nav class="side-nav-bar__menu${empty examinerHasActiveSession or not examinerHasActiveSession ? ' side-nav-bar__menu--locked' : ''}">
        <a href="${ctx}/views/examiner/dashboard"
           class="side-nav-bar__link${activeSidebar eq 'dashboard' ? ' is-active' : ''}">
            <span class="side-nav-bar__icon material-symbols-outlined">grid_view</span>
            <span class="side-nav-bar__label">Bảng điều khiển</span>
        </a>

        <a href="${ctx}/views/examiner/candidate-call"
           class="side-nav-bar__link${activeSidebar eq 'goi-thi-sinh' ? ' is-active' : ''}">
            <span class="side-nav-bar__icon material-symbols-outlined">campaign</span>
            <span class="side-nav-bar__label">Gọi thí sinh</span>
        </a>

        <a href="${ctx}/views/examiner/violations"
           class="side-nav-bar__link${activeSidebar eq 'vi-pham' ? ' is-active' : ''}">
            <span class="side-nav-bar__icon material-symbols-outlined">report</span>
            <span class="side-nav-bar__label">Vi phạm</span>
        </a>

        <a href="${ctx}/views/examiner/candidate-details"
           class="side-nav-bar__link${activeSidebar eq 'sua-thong-tin' ? ' is-active' : ''}">
            <span class="side-nav-bar__icon material-symbols-outlined">edit_document</span>
            <span class="side-nav-bar__label">Thông tin thí sinh</span>
        </a>

        <c:choose>
            <c:when test="${examinerSectionTheory}">
                <span class="side-nav-bar__link side-nav-bar__link--disabled${activeSidebar eq 'nhap-diem' ? ' is-active' : ''}" aria-disabled="true">
                    <span class="side-nav-bar__icon material-symbols-outlined">assignment_turned_in</span>
                    <span class="side-nav-bar__label">Nhập điểm</span>
                </span>
            </c:when>
            <c:otherwise>
                <a href="${ctx}/views/examiner/score-entry"
                   class="side-nav-bar__link${activeSidebar eq 'nhap-diem' ? ' is-active' : ''}">
                    <span class="side-nav-bar__icon material-symbols-outlined">assignment_turned_in</span>
                    <span class="side-nav-bar__label">Nhập điểm</span>
                </a>
            </c:otherwise>
        </c:choose>

        <c:choose>
            <c:when test="${examinerSectionTheory}">
                <span class="side-nav-bar__link side-nav-bar__link--disabled${activeSidebar eq 'sua-ket-qua' ? ' is-active' : ''}" aria-disabled="true">
                    <span class="side-nav-bar__icon material-symbols-outlined">fact_check</span>
                    <span class="side-nav-bar__label">Sửa kết quả</span>
                </span>
            </c:when>
            <c:otherwise>
                <a href="${ctx}/views/examiner/result-details"
                   class="side-nav-bar__link${activeSidebar eq 'sua-ket-qua' ? ' is-active' : ''}">
                    <span class="side-nav-bar__icon material-symbols-outlined">fact_check</span>
                    <span class="side-nav-bar__label">Sửa kết quả</span>
                </a>
            </c:otherwise>
        </c:choose>

        <a href="${ctx}/views/examiner/devices"
           class="side-nav-bar__link${activeSidebar eq 'thiet-bi' ? ' is-active' : ''}">
            <span class="side-nav-bar__icon material-symbols-outlined">devices</span>
            <span class="side-nav-bar__label">Thiết bị</span>
        </a>

        <a href="${ctx}/views/examiner/export"
           class="side-nav-bar__link${activeSidebar eq 'xuat-file' ? ' is-active' : ''}">
            <span class="side-nav-bar__icon material-symbols-outlined">download</span>
            <span class="side-nav-bar__label">Xuất file</span>
        </a>

        <a href="${ctx}/views/examiner/print-documents"
           class="side-nav-bar__link${activeSidebar eq 'in-van-ban' ? ' is-active' : ''}">
            <span class="side-nav-bar__icon material-symbols-outlined">print</span>
            <span class="side-nav-bar__label">In văn bản</span>
        </a>

        <a href="${ctx}/views/examiner/audit"
           class="side-nav-bar__link${activeSidebar eq 'nhat-ky' ? ' is-active' : ''}">
            <span class="side-nav-bar__icon material-symbols-outlined">history</span>
            <span class="side-nav-bar__label">Nhật Ký</span>
        </a>
    </nav>

    <div class="side-nav-bar__footer">
        <a href="${ctx}/logout" class="side-nav-bar__logout">
            <span class="side-nav-bar__icon material-symbols-outlined">logout</span>
            <span class="side-nav-bar__logout-label">Đăng xuất</span>
        </a>
    </div>
</aside>

<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>

<!--variables-->
<c:set var="ctx" value="${pageContext.request.contextPath}" />
<c:set var="logoUrl" value="${ctx}/assets/imgs/csgt-footer.png" />
<c:set var="activeSidebar" value="${param.activeSidebar}" />
<c:set var="requestUri" value="${pageContext.request.requestURI}" />

<!--get current page to set active sidebar-->
<c:if test="${empty activeSidebar}">
    <c:choose>
        <c:when test="${fn:contains(requestUri, 'dashboard')}">
            <c:set var="activeSidebar" value="dashboard" />
        </c:when>
        <c:when test="${fn:contains(requestUri, 'score-entry')}">
            <c:set var="activeSidebar" value="score-entry" />
        </c:when>
        <c:when test="${fn:contains(requestUri, 'action')}">
            <c:set var="activeSidebar" value="action" />
        </c:when>
        <c:when test="${fn:contains(requestUri, 'violations') or fn:contains(requestUri, 'violation-')}">
            <c:set var="activeSidebar" value="violations" />
        </c:when>
        <c:when test="${fn:contains(requestUri, '/examiner/candidates')}">
            <c:set var="activeSidebar" value="candidates" />
        </c:when>
        <c:when test="${fn:contains(requestUri, 'candidate-details') or fn:contains(requestUri, 'candidate-paper')}">
            <c:set var="activeSidebar" value="candidate-details" />
        </c:when>
        <c:when test="${fn:contains(requestUri, 'result-details')}">
            <c:set var="activeSidebar" value="result-details" />
        </c:when>
        <c:when test="${fn:contains(requestUri, 'devices')}">
            <c:set var="activeSidebar" value="devices" />
        </c:when>
        <c:when test="${fn:contains(requestUri, 'export')}">
            <c:set var="activeSidebar" value="export" />
        </c:when>
        <c:when test="${fn:contains(requestUri, 'print-documents')}">
            <c:set var="activeSidebar" value="print-documents" />
        </c:when>
        <c:when test="${fn:contains(requestUri, 'audit')}">
            <c:set var="activeSidebar" value="audit" />
        </c:when>
        <c:when test="${fn:contains(requestUri, 'change-password')}">
            <c:set var="activeSidebar" value="doi-mat-khau" />
        </c:when>
        <c:when test="${fn:contains(requestUri, 'profile')}">
            <c:set var="activeSidebar" value="ho-so" />
        </c:when>
        <c:otherwise><c:set var="activeSidebar" value="dashboard" /></c:otherwise>
    </c:choose>
</c:if>

<!--sidebar-->
<aside class="side-nav-bar side-nav-bar--examiner">

    <!--top-->
    <div class="side-nav-bar__brand">
        <div class="side-nav-bar__brand-inner">
            <img src="${logoUrl}" alt="CSGT" width="48" class="side-nav-bar__logo-img">
            <div class="side-nav-bar__brand-title-wrap">
                <h1 class="side-nav-bar__brand-title">Sát hạch viên</h1>
                <p class="side-nav-bar__brand-subtitle">${sessionScope.userProfile.fullName}</p>
            </div>
        </div>
    </div>

    <!--menu-->
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

        <a href="${ctx}/examiner/violations"
           class="side-nav-bar__link${activeSidebar eq 'violations' ? ' is-active' : ''}">
            <span class="side-nav-bar__icon material-symbols-outlined">report</span>
            <span class="side-nav-bar__label">Vi phạm</span>
        </a>

        <a href="${ctx}/examiner/candidates"
           class="side-nav-bar__link${activeSidebar eq 'candidates' or activeSidebar eq 'candidate-details' ? ' is-active' : ''}">
            <span class="side-nav-bar__icon material-symbols-outlined">edit_document</span>
            <span class="side-nav-bar__label">Thông tin thí sinh</span>
        </a>

        <c:choose>
            <c:when test="${examinerSectionTheory}">
                <span class="side-nav-bar__link side-nav-bar__link--disabled${activeSidebar eq 'score-entry' ? ' is-active' : ''}" aria-disabled="true">
                    <span class="side-nav-bar__icon material-symbols-outlined">assignment_turned_in</span>
                    <span class="side-nav-bar__label">Nhập điểm</span>
                </span>
            </c:when>
            <c:otherwise>
                <a href="${ctx}/examiner/score-entry"
                   class="side-nav-bar__link${activeSidebar eq 'score-entry' ? ' is-active' : ''}">
                    <span class="side-nav-bar__icon material-symbols-outlined">assignment_turned_in</span>
                    <span class="side-nav-bar__label">Nhập điểm</span>
                </a>
            </c:otherwise>
        </c:choose>

        <c:choose>
            <c:when test="${examinerSectionTheory}">
                <span class="side-nav-bar__link side-nav-bar__link--disabled${activeSidebar eq 'result-details' ? ' is-active' : ''}" aria-disabled="true">
                    <span class="side-nav-bar__icon material-symbols-outlined">fact_check</span>
                    <span class="side-nav-bar__label">Sửa kết quả</span>
                </span>
            </c:when>
            <c:otherwise>
                <a href="${ctx}/examiner/result-details"
                   class="side-nav-bar__link${activeSidebar eq 'result-details' ? ' is-active' : ''}">
                    <span class="side-nav-bar__icon material-symbols-outlined">fact_check</span>
                    <span class="side-nav-bar__label">Sửa kết quả</span>
                </a>
            </c:otherwise>
        </c:choose>

        <a href="${ctx}/examiner/devices"
           class="side-nav-bar__link${activeSidebar eq 'devices' ? ' is-active' : ''}">
            <span class="side-nav-bar__icon material-symbols-outlined">devices</span>
            <span class="side-nav-bar__label">Thiết bị</span>
        </a>

        <a href="${ctx}/examiner/export"
           class="side-nav-bar__link${activeSidebar eq 'export' ? ' is-active' : ''}">
            <span class="side-nav-bar__icon material-symbols-outlined">download</span>
            <span class="side-nav-bar__label">Xuất file</span>
        </a>

        <a href="${ctx}/examiner/print-documents"
           class="side-nav-bar__link${activeSidebar eq 'print-documents' ? ' is-active' : ''}">
            <span class="side-nav-bar__icon material-symbols-outlined">print</span>
            <span class="side-nav-bar__label">In văn bản</span>
        </a>

        <a href="${ctx}/examiner/audit"
           class="side-nav-bar__link${activeSidebar eq 'audit' ? ' is-active' : ''}">
            <span class="side-nav-bar__icon material-symbols-outlined">history</span>
            <span class="side-nav-bar__label">Nhật Ký</span>
        </a>
    </nav>

    <!--bottom-->
    <div class="side-nav-bar__footer">
        <a href="${ctx}/examiner/profile"
           class="side-nav-bar__logout${activeSidebar eq 'ho-so' ? ' is-active' : ''}"
           style="margin-bottom:6px;"
           <c:if test="${activeSidebar eq 'ho-so'}">aria-current="page"</c:if>>
            <span class="side-nav-bar__icon material-symbols-outlined">person</span>
            <span class="side-nav-bar__logout-label">Hồ sơ cá nhân</span>
        </a>
        <a href="${ctx}/examiner/change-password"
           class="side-nav-bar__logout${activeSidebar eq 'doi-mat-khau' ? ' is-active' : ''}"
           style="margin-bottom:6px;"
           <c:if test="${activeSidebar eq 'doi-mat-khau'}">aria-current="page"</c:if>>
            <span class="side-nav-bar__icon material-symbols-outlined">lock</span>
            <span class="side-nav-bar__logout-label">Đổi mật khẩu</span>
        </a>
        <a href="${ctx}/staff/logout" class="side-nav-bar__logout">
            <span class="side-nav-bar__icon material-symbols-outlined">logout</span>
            <span class="side-nav-bar__logout-label">Đăng xuất</span>
        </a>
    </div>
</aside>

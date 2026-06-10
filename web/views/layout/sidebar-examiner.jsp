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
            <span class="side-nav-bar__icon material-symbols-outlined">grid_view</span>
            <span class="side-nav-bar__label">Bảng điều khiển</span>
        </a>

        <a href="${ctx}/views/examiner/candidate-call.jsp"
           class="side-nav-bar__link${activeSidebar eq 'goi-thi-sinh' ? ' is-active' : ''}">
            <span class="side-nav-bar__icon material-symbols-outlined">campaign</span>
            <span class="side-nav-bar__label">Gọi thí sinh</span>
        </a>

        <a href="${ctx}/views/examiner/candidate-details.jsp"
           class="side-nav-bar__link${activeSidebar eq 'sua-thong-tin' ? ' is-active' : ''}">
            <span class="side-nav-bar__icon material-symbols-outlined">edit_document</span>
            <span class="side-nav-bar__label">Sửa thông tin</span>
        </a>

        <a href="${ctx}/views/examiner/result-details.jsp"
           class="side-nav-bar__link${activeSidebar eq 'sua-ket-qua' ? ' is-active' : ''}">
            <span class="side-nav-bar__icon material-symbols-outlined">fact_check</span>
            <span class="side-nav-bar__label">Sửa kết quả</span>
        </a>

        <a href="${ctx}/views/examiner/export.jsp"
           class="side-nav-bar__link${activeSidebar eq 'xuat-file' ? ' is-active' : ''}">
            <span class="side-nav-bar__icon material-symbols-outlined">download</span>
            <span class="side-nav-bar__label">Xuất file</span>
        </a>

        <a href="${ctx}/views/examiner/audit.jsp"
           class="side-nav-bar__link${activeSidebar eq 'nhat-ky' ? ' is-active' : ''}">
            <span class="side-nav-bar__icon material-symbols-outlined">history</span>
            <span class="side-nav-bar__label">Nhật Ký</span>
        </a>
    </nav>

    <div class="side-nav-bar__footer">
        <a href="${ctx}/logout" class="side-nav-bar__logout">
            <span class="side-nav-bar__icon material-symbols-outlined">logout</span>
            <span class="side-nav-bar__logout-label">Logout</span>
        </a>
    </div>
</aside>

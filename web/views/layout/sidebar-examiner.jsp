<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<c:set var="ctx" value="${pageContext.request.contextPath}" />
<c:set var="activeSidebar" value="${param.activeSidebar}" />
<aside class="side-nav-bar side-nav-bar--examiner">
    <div class="side-nav-bar__brand">
        <div class="side-nav-bar__brand-inner">
            <img src="${ctx}/assets/imgs/csgt-footer.png" alt="CSGT" width="48" class="side-nav-bar__logo-img">
            <div><h1 class="side-nav-bar__brand-title">Sát hạch viên</h1>
                <p class="side-nav-bar__brand-subtitle">${sessionScope.userProfile.fullName}</p></div>
        </div>
    </div>
    <nav class="side-nav-bar__menu">
        <a href="${ctx}/examiner/dashboard" class="side-nav-bar__link${activeSidebar eq 'dashboard' ? ' is-active' : ''}">
            <span class="material-symbols-outlined">grid_view</span><span>Bảng điều khiển</span>
        </a>
        <a href="${ctx}/examiner/action" class="side-nav-bar__link${activeSidebar eq 'action' ? ' is-active' : ''}">
            <span class="material-symbols-outlined">campaign</span><span>Thao tác</span>
        </a>
        <div class="side-nav-bar__group">
            <span class="side-nav-bar__group-title">Thông tin chi tiết</span>
            <a href="${ctx}/examiner/candidates" class="side-nav-bar__link${activeSidebar eq 'candidates' ? ' is-active' : ''}">Thông tin thí sinh</a>
            <a href="${ctx}/examiner/violations" class="side-nav-bar__link${activeSidebar eq 'violations' ? ' is-active' : ''}">Vi phạm</a>
            <c:choose>
                <c:when test="${examinerSectionTheory}">
                    <span class="side-nav-bar__link side-nav-bar__link--disabled">Sửa kết quả</span>
                    <a href="${ctx}/examiner/candidates" class="side-nav-bar__link">Đề thi</a>
                </c:when>
                <c:otherwise>
                    <a href="${ctx}/examiner/result-details" class="side-nav-bar__link">Sửa kết quả</a>
                    <span class="side-nav-bar__link side-nav-bar__link--disabled">Đề thi</span>
                </c:otherwise>
            </c:choose>
        </div>
        <a href="${ctx}/examiner/devices" class="side-nav-bar__link${activeSidebar eq 'devices' ? ' is-active' : ''}">
            <span class="material-symbols-outlined">devices</span><span>Thiết bị</span>
        </a>
        <a href="${ctx}/examiner/print-documents" class="side-nav-bar__link${activeSidebar eq 'print-documents' ? ' is-active' : ''}">
            <span class="material-symbols-outlined">print</span><span>Biên bản</span>
        </a>
        <a href="${ctx}/examiner/audit" class="side-nav-bar__link${activeSidebar eq 'audit' ? ' is-active' : ''}">
            <span class="material-symbols-outlined">history</span><span>Nhật ký</span>
        </a>
    </nav>
    <div class="side-nav-bar__footer">
        <a href="${ctx}/staff/logout" class="side-nav-bar__logout">
            <span class="material-symbols-outlined">logout</span><span>Đăng xuất</span>
        </a>
    </div>
</aside>

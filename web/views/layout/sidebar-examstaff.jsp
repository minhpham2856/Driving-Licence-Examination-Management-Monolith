<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" language="java" %>

<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>

<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>

<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>

<c:set var="ctx" value="${pageContext.request.contextPath}" />

<c:set var="logoUrl" value="${ctx}/assets/imgs/LOGO.png" />

<c:set var="activeSidebar" value="${param.activeSidebar}" />

<c:if test="${empty activeSidebar}">

    <c:choose>

        <c:when test="${fn:contains(pageContext.request.requestURI, 'dashboard') or fn:contains(pageContext.request.requestURI, 'tong-quan')}">

            <c:set var="activeSidebar" value="dashboard" />

        </c:when>

        <c:when test="${fn:contains(pageContext.request.requestURI, 'examiner-allocation') or fn:contains(pageContext.request.requestURI, 'giam-khao')}">

            <c:set var="activeSidebar" value="phan-bo-giam-khao" />

        </c:when>

        <c:when test="${(fn:contains(pageContext.request.requestURI, '/allocation') or fn:contains(pageContext.request.requestURI, 'phan-bo')) and not fn:contains(pageContext.request.requestURI, 'examiner-allocation')}">

            <c:set var="activeSidebar" value="phan-bo" />

        </c:when>

        <c:when test="${fn:contains(pageContext.request.requestURI, 'candidatecall') or fn:contains(pageContext.request.requestURI, 'goi-thi')}">

            <c:set var="activeSidebar" value="goi-thi" />

        </c:when>

        <c:when test="${fn:contains(pageContext.request.requestURI, 'procedure')}">

            <c:set var="activeSidebar" value="goi-thi" />

        </c:when>

        <c:when test="${fn:contains(pageContext.request.requestURI, 'report') or fn:contains(pageContext.request.requestURI, 'bao-cao')}">

            <c:set var="activeSidebar" value="bao-cao" />

        </c:when>

        <c:when test="${fn:contains(pageContext.request.requestURI, 'audit') or fn:contains(pageContext.request.requestURI, 'nhat-ky')}">

            <c:set var="activeSidebar" value="nhat-ky" />

        </c:when>

        <c:when test="${fn:contains(pageContext.request.requestURI, 'profile') or fn:contains(pageContext.request.requestURI, 'ho-so')}">

            <c:set var="activeSidebar" value="ho-so" />

        </c:when>

        <c:when test="${fn:contains(pageContext.request.requestURI, 'change-password') or fn:contains(pageContext.request.requestURI, 'doi-mat-khau')}">

            <c:set var="activeSidebar" value="doi-mat-khau" />

        </c:when>

        <c:otherwise>

            <c:set var="activeSidebar" value="dashboard" />

        </c:otherwise>

    </c:choose>

</c:if>

<c:set var="staffName" value="${sessionScope.user.username}" />

<c:if test="${not empty sessionScope.userProfile and not empty sessionScope.userProfile.fullName}">

    <c:set var="staffName" value="${sessionScope.userProfile.fullName}" />

</c:if>

<aside class="side-nav-bar side-nav-bar--examiner side-nav-bar--examstaff" role="navigation" aria-label="Ban Sát Hạch">

    <div class="side-nav-bar__brand">

        <div class="side-nav-bar__brand-inner">

            <img src="${logoUrl}" alt="Lái Vui" width="40" height="40" class="side-nav-bar__logo-img">

            <div class="side-nav-bar__brand-title-wrap">

                <h1 class="side-nav-bar__brand-title">Ban Sát Hạch</h1>

                <p class="side-nav-bar__brand-subtitle"><c:out value="${staffName}" /></p>

            </div>

        </div>

    </div>

    <c:set var="sidebarRedirect" value="${pageContext.request.servletPath}" />

    <c:set var="sidebarOptions" value="${requestScope.examOptions}" />

    <c:if test="${empty sidebarOptions}">

        <c:set var="sidebarOptions" value="${sessionScope.examStaffExamOptions}" />

    </c:if>

    <c:set var="pickerExamIdValue" value="${param.examId}" />

    <c:if test="${empty pickerExamIdValue}">

        <c:set var="pickerExamIdValue" value="${requestScope.selectedExamId}" />

    </c:if>

    <c:if test="${empty pickerExamIdValue}">

        <c:set var="pickerExamIdValue" value="${sessionScope.selectedExamId}" />

    </c:if>

    <c:set var="pickerExamId" value="${requestScope.selectedExamId}" />

    <c:set var="navExamId" value="${requestScope.selectedExamId}" />

    <c:if test="${empty navExamId}">

        <c:set var="navExamId" value="${pickerExamIdValue}" />

    </c:if>

    <c:if test="${empty navExamId}">

        <c:set var="navExamId" value="${sessionScope.selectedExamId}" />

    </c:if>

    <c:set var="examQuery" value="" />

    <c:if test="${not empty navExamId}">

        <c:set var="examQuery" value="?examId=${navExamId}" />

    </c:if>

    <div class="side-nav-bar__exam-picker">

        <form method="GET" action="${ctx}/examstaff/select-exam" class="side-nav-bar__exam-form">

            <input type="hidden" name="redirect" value="<c:out value='${sidebarRedirect}' />" />

            <label class="side-nav-bar__exam-label" for="examId">Kỳ thi</label>

            <select id="examId" name="examId" class="side-nav-bar__exam-select"

                    aria-label="Chọn kỳ thi" data-exam-picker="true"

                    data-selected-exam-id="${pickerExamId}"

                    data-committed-exam-id="${not empty requestScope.pickerCommittedExamId ? requestScope.pickerCommittedExamId : navExamId}"

                    onchange="if(window.syncExamStaffSessionApply){window.syncExamStaffSessionApply(this);}">

                <c:if test="${empty sidebarOptions}">

                    <option value="">- Chưa có kỳ thi -</option>

                </c:if>

                <c:forEach var="exam" items="${sidebarOptions}" varStatus="optSt">

                    <option value="${exam.id}" data-exam-id="${exam.examId}"
                            <c:choose>
                                <c:when test="${not empty requestScope.pickerCommittedExamId}">
                                    <c:if test="${exam.examId == requestScope.pickerCommittedExamId}">selected="selected"</c:if>
                                </c:when>
                                <c:when test="${not empty pickerExamIdValue or not empty pickerExamId}">
                                    <c:if test="${pickerExamIdValue == exam.id or pickerExamId == exam.examId}">selected="selected"</c:if>
                                </c:when>
                                <c:otherwise>
                                    <c:if test="${optSt.first}">selected="selected"</c:if>
                                </c:otherwise>
                            </c:choose>>

                        Hạng <c:out value="${exam.licenseCode}" default="-" /> -

                        <c:if test="${not empty exam.examDate}"><fmt:formatDate value="${exam.examDate}" pattern="dd/MM/yyyy" /></c:if>

                        <c:if test="${empty exam.examDate}">-</c:if>

                    </option>

                </c:forEach>

            </select>

            <button type="submit" class="side-nav-bar__exam-apply" data-exam-apply="true"

                    data-loading-label="Đang tải..."

                    data-default-label="Xác nhận"

                    aria-label="Xác nhận đổi kỳ thi">Xác nhận</button>

        </form>

    </div>

    <nav class="side-nav-bar__menu">

        <a href="${ctx}/examstaff/dashboard${examQuery}"

           class="side-nav-bar__link${activeSidebar eq 'dashboard' ? ' is-active' : ''}"

           <c:if test="${activeSidebar eq 'dashboard'}">aria-current="page"</c:if>>

            <span class="side-nav-bar__icon material-symbols-outlined" aria-hidden="true">grid_view</span>

            <span class="side-nav-bar__label">Tổng quan kỳ thi</span>

        </a>

        <c:set var="allocUri" value="${pageContext.request.requestURI}" />

        <c:set var="allocOpen" value="${(fn:contains(allocUri, '/allocation') or fn:contains(allocUri, 'phan-bo')) and not fn:contains(allocUri, 'examiner-allocation')}" />

        <div class="side-nav-bar__menu-group is-open" data-allocation-menu>

            <button type="button" class="side-nav-bar__link side-nav-bar__link--toggle${activeSidebar eq 'phan-bo' ? ' is-active' : ''}"

                    aria-expanded="true" aria-controls="allocation-submenu"
                    data-allocation-overview-url="${ctx}/examstaff/allocation${examQuery}">

                <span class="side-nav-bar__icon material-symbols-outlined" aria-hidden="true">view_module</span>

                <span class="side-nav-bar__label">Phân bổ thí sinh</span>

                <span class="side-nav-bar__chevron" aria-hidden="true">

                    <span class="material-symbols-outlined">expand_more</span>

                </span>

            </button>

            <div id="allocation-submenu" class="side-nav-bar__submenu">

                <a href="${ctx}/examstaff/allocation${examQuery}"

                   class="side-nav-bar__submenu-link${(fn:contains(allocUri, '/allocation') and not fn:contains(allocUri, 'allocation-')) ? ' is-active' : ''}"><span class="submenu-dot"></span> Tổng quan</a>

                <a href="${ctx}/examstaff/allocation-waiting${examQuery}"

                   class="side-nav-bar__submenu-link${fn:contains(allocUri, 'allocation-waiting') ? ' is-active' : ''}"><span class="submenu-dot"></span> Phòng chờ</a>

                <a href="${ctx}/examstaff/allocation-theory${examQuery}"

                   class="side-nav-bar__submenu-link${fn:contains(allocUri, 'allocation-theory') ? ' is-active' : ''}"><span class="submenu-dot"></span> Lý thuyết</a>

                <a href="${ctx}/examstaff/allocation-practical${examQuery}"

                   class="side-nav-bar__submenu-link${fn:contains(allocUri, 'allocation-practical') ? ' is-active' : ''}"><span class="submenu-dot"></span> TH / Sa hình</a>

                <a href="${ctx}/examstaff/allocation-results-pass${examQuery}"

                   class="side-nav-bar__submenu-link${fn:contains(allocUri, 'allocation-results-pass') ? ' is-active' : ''}"><span class="submenu-dot"></span> Kết quả - Đỗ</a>

                <a href="${ctx}/examstaff/allocation-results-fail${examQuery}"

                   class="side-nav-bar__submenu-link${fn:contains(allocUri, 'allocation-results-fail') ? ' is-active' : ''}"><span class="submenu-dot"></span> Kết quả - Trượt</a>

            </div>

        </div>

        <a href="${ctx}/examstaff/examiner-allocation${examQuery}"

           class="side-nav-bar__link${activeSidebar eq 'phan-bo-giam-khao' ? ' is-active' : ''}"

           <c:if test="${activeSidebar eq 'phan-bo-giam-khao'}">aria-current="page"</c:if>>

            <span class="side-nav-bar__icon material-symbols-outlined" aria-hidden="true">supervisor_account</span>

            <span class="side-nav-bar__label">Phân bổ sát hạch viên</span>

        </a>

        <a href="${ctx}/examstaff/candidatecall${examQuery}"

           class="side-nav-bar__link${activeSidebar eq 'goi-thi' ? ' is-active' : ''}"

           <c:if test="${activeSidebar eq 'goi-thi'}">aria-current="page"</c:if>>

            <span class="side-nav-bar__icon material-symbols-outlined" aria-hidden="true">campaign</span>

            <span class="side-nav-bar__label">Gọi làm thủ tục</span>

        </a>

        <a href="${ctx}/examstaff/report${examQuery}"

           class="side-nav-bar__link${activeSidebar eq 'bao-cao' ? ' is-active' : ''}"

           <c:if test="${activeSidebar eq 'bao-cao'}">aria-current="page"</c:if>>

            <span class="side-nav-bar__icon material-symbols-outlined" aria-hidden="true">bar_chart</span>

            <span class="side-nav-bar__label">Báo cáo</span>

        </a>

        <a href="${ctx}/examstaff/audit${examQuery}"

           class="side-nav-bar__link${activeSidebar eq 'nhat-ky' ? ' is-active' : ''}"

           <c:if test="${activeSidebar eq 'nhat-ky'}">aria-current="page"</c:if>>

            <span class="side-nav-bar__icon material-symbols-outlined" aria-hidden="true">history</span>

            <span class="side-nav-bar__label">Nhật ký cá nhân</span>

        </a>

        <div class="side-nav-bar__section-label">Màn hình công cộng</div>

        <a href="${ctx}/examstaff/public-call<c:if test="${not empty sessionScope.selectedExamId}">?examId=${sessionScope.selectedExamId}</c:if>"

           class="side-nav-bar__link${activeSidebar eq 'public-call' ? ' is-active' : ''}"

           <c:if test="${activeSidebar eq 'public-call'}">aria-current="page"</c:if>>

            <span class="side-nav-bar__icon material-symbols-outlined" aria-hidden="true">live_tv</span>

            <span class="side-nav-bar__label">Màn hình gọi TV</span>

        </a>

    </nav>

    <div class="side-nav-bar__footer">

        <a href="${ctx}/examstaff/profile" class="side-nav-bar__logout" style="margin-bottom:6px;">

            <span class="side-nav-bar__icon material-symbols-outlined" aria-hidden="true">person</span>

            <span class="side-nav-bar__logout-label">Hồ sơ cá nhân</span>

        </a>

        <a href="${ctx}/examstaff/change-password" class="side-nav-bar__logout" style="margin-bottom:6px;">

            <span class="side-nav-bar__icon material-symbols-outlined" aria-hidden="true">lock</span>

            <span class="side-nav-bar__logout-label">Đổi mật khẩu</span>

        </a>

        <a href="${ctx}/staff/logout" class="side-nav-bar__logout">

            <span class="side-nav-bar__icon material-symbols-outlined" aria-hidden="true">logout</span>

            <span class="side-nav-bar__logout-label">Đăng xuất</span>

        </a>

    </div>

</aside>

<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>

<%--account data / shell flags--%>
<c:set var="u" value="${requestScope.accountUser}" />
<c:set var="p" value="${requestScope.accountProfile}" />
<c:set var="ctx" value="${pageContext.request.contextPath}" />
<c:set var="useExamstaffShell" value="${requestScope.accountShell eq 'examstaff'}" />
<c:set var="useExaminerShell" value="${requestScope.accountShell eq 'examiner'}" />

<%--shell open--%>
<c:choose>
    <%--case 1: examstaff shell--%>
    <c:when test="${useExamstaffShell}">
        <jsp:include page="/views/staff/examstaff/includes/examstaff-layout-head.jsp">
            <jsp:param name="activeSidebar" value="ho-so" />
            <jsp:param name="pageTitle" value="Hồ sơ cá nhân" />
            <jsp:param name="noCache" value="true" />
            <jsp:param name="mainClass" value="examstaff-main--scroll" />
            <jsp:param name="resolveQueue" value="false" />
            <jsp:param name="pageCss" value="account.css" />
        </jsp:include>
        <link rel="stylesheet"
              href="${ctx}/assets/css/examstaff/account.css?v=20260714c">
        <style>
            body.examstaff-portal .examstaff-main > .account-page--portal {
                width: 100% !important;
                max-width: none !important;
                margin: 0 !important;
                align-self: stretch !important;
            }
        </style>
        <div class="account-page account-page--portal">
    </c:when>
    <%--case 2: examiner shell--%>
    <c:when test="${useExaminerShell}">
        <!DOCTYPE html>
        <html lang="vi">
            <head>
                <jsp:include page="/views/examiner/components/head.jsp">
                    <jsp:param name="title" value="Hồ sơ cá nhân - SÁT HẠCH" />
                </jsp:include>
                <meta http-equiv="Cache-Control"
                      content="no-cache, no-store, must-revalidate">
                <link rel="stylesheet"
                      href="${ctx}/assets/css/examstaff/account.css?v=20260714c">
                <style>
                    body.portal .main > .account-page--portal {
                        width: 100% !important;
                        max-width: none !important;
                        margin: 0 !important;
                        align-self: stretch !important;
                    }
                </style>
            </head>
            <body class="has-side-nav-bar
                  portal${empty examinerHasActiveExam or not examinerHasActiveExam ? ' inactive' : ''}">
                <jsp:include page="/views/layout/sidebar-examiner.jsp">
                    <jsp:param name="activeSidebar" value="ho-so" />
                </jsp:include>
                <div class="shell">
                    <jsp:include page="/views/layout/header-examiner.jsp">
                        <jsp:param name="title" value="Hồ sơ cá nhân" />
                    </jsp:include>
                    <main class="main scroll">
                        <div class="account-page account-page--portal">
    </c:when>
    <%--case 3: public shell--%>
    <c:otherwise>
        <jsp:include page="/views/layout/header.jsp">
            <jsp:param name="title" value="Lái Vui - Hồ sơ cá nhân" />
        </jsp:include>
        <link rel="stylesheet"
              href="${ctx}/assets/css/examstaff/account.css?v=20260714c">
        <main class="account-page account-page--public">
    </c:otherwise>
</c:choose>

<%--shared profile body--%>
<div class="account-shell">
    <%--flash message--%>
    <c:if test="${not empty message}">
        <div class="account-alert account-alert--${messageType eq 'success' ? 'success' : 'error'}"
             role="status">
            <span class="material-symbols-outlined" aria-hidden="true">
                ${messageType eq 'success' ? 'check_circle' : 'error'}
            </span>
            <span><c:out value="${message}" /></span>
        </div>
    </c:if>

    <c:choose>
        <%--case 1: missing profile--%>
        <c:when test="${empty p}">
            <section class="account-panel">
                <p class="account-empty">
                    Chưa có hồ sơ cá nhân gắn với tài khoản. Liên hệ quản trị viên để được hỗ trợ.
                </p>
            </section>
        </c:when>
        <%--case 2: edit profile form--%>
        <c:otherwise>
            <form method="post"
                  action="${ctx}${accountProfilePath}"
                  class="account-stack"
                  novalidate>
                <section class="account-panel" aria-label="Thông tin tài khoản">
                    <h2 class="account-panel__title">Tài khoản</h2>
                    <div class="account-grid">
                        <div class="account-field">
                            <label class="account-field__label" for="username">
                                Tên đăng nhập
                            </label>
                            <input class="account-input"
                                   type="text"
                                   id="username"
                                   name="username"
                                   required
                                   minlength="3"
                                   maxlength="50"
                                   pattern="[A-Za-z0-9._-]+"
                                   value="${fn:escapeXml(u.username)}">
                        </div>
                        <div class="account-field">
                            <label class="account-field__label" for="email">Email</label>
                            <input class="account-input"
                                   type="email"
                                   id="email"
                                   name="email"
                                   required
                                   value="${fn:escapeXml(u.email)}">
                        </div>
                    </div>
                </section>

                <section class="account-panel" aria-label="Thông tin cá nhân">
                    <h2 class="account-panel__title">Thông tin cá nhân</h2>
                    <div class="account-grid">
                        <div class="account-field">
                            <label class="account-field__label" for="fullName">
                                Họ và tên
                            </label>
                            <input class="account-input"
                                   type="text"
                                   id="fullName"
                                   name="fullName"
                                   required
                                   value="${fn:escapeXml(p.fullName)}">
                        </div>
                        <div class="account-field">
                            <label class="account-field__label" for="dateOfBirth">
                                Ngày sinh
                            </label>
                            <input class="account-input"
                                   type="date"
                                   id="dateOfBirth"
                                   name="dateOfBirth"
                                   value="<fmt:formatDate value='${p.dateOfBirth}' pattern='yyyy-MM-dd' />">
                        </div>
                        <div class="account-field">
                            <label class="account-field__label" for="sex">Giới tính</label>
                            <select class="account-select" id="sex" name="sex">
                                <%--case 1: male--%>
                                <option value="1"
                                        <c:if test="${p.sex}">selected="selected"</c:if>>Nam</option>
                                <%--case 2: female--%>
                                <option value="0"
                                        <c:if test="${not p.sex}">selected="selected"</c:if>>Nữ</option>
                            </select>
                        </div>
                        <div class="account-field">
                            <label class="account-field__label" for="phoneNumber">
                                Số điện thoại
                            </label>
                            <input class="account-input"
                                   type="tel"
                                   id="phoneNumber"
                                   name="phoneNumber"
                                   required
                                   pattern="0[0-9]{9,10}"
                                   value="${fn:escapeXml(p.phoneNumber)}">
                        </div>
                        <div class="account-field">
                            <label class="account-field__label" for="governmentIdNumber">
                                Số căn cước
                            </label>
                            <input class="account-input"
                                   type="text"
                                   id="governmentIdNumber"
                                   name="governmentIdNumber"
                                   required
                                   pattern="\\d{12}"
                                   maxlength="12"
                                   inputmode="numeric"
                                   value="${fn:escapeXml(p.governmentIdNumber)}">
                        </div>
                        <div class="account-field account-grid__full">
                            <label class="account-field__label" for="address">Địa chỉ</label>
                            <input class="account-input"
                                   type="text"
                                   id="address"
                                   name="address"
                                   value="${fn:escapeXml(p.address)}">
                        </div>
                    </div>
                    <div class="account-actions">
                        <button type="submit" class="account-btn account-btn--primary">
                            <span class="material-symbols-outlined" aria-hidden="true">save</span>
                            Lưu thay đổi
                        </button>
                    </div>
                </section>
            </form>
        </c:otherwise>
    </c:choose>
</div>

<%--shell close--%>
<c:choose>
    <%--case 1: examstaff shell--%>
    <c:when test="${useExamstaffShell}">
        </div>
        <jsp:include page="/views/staff/examstaff/includes/examstaff-layout-foot.jsp" />
    </c:when>
    <%--case 2: examiner shell--%>
    <c:when test="${useExaminerShell}">
                        </div>
                    </main>
                </div>
            </body>
        </html>
    </c:when>
    <%--case 3: public shell--%>
    <c:otherwise>
        </main>
        <jsp:include page="/views/layout/footer.jsp" />
    </c:otherwise>
</c:choose>

<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>

<c:set var="u" value="${requestScope.accountUser}" />
<c:set var="p" value="${requestScope.accountProfile}" />
<c:set var="ctx" value="${pageContext.request.contextPath}" />
<c:set var="roleName" value="" />
<c:if test="${not empty u and not empty u.role and not empty u.role.roleName}">
    <c:set var="roleName" value="${u.role.roleName}" />
</c:if>
<c:set var="useExamstaffShell" value="${requestScope.accountShell eq 'examstaff'}" />

<c:choose>
    <c:when test="${useExamstaffShell}">
        <jsp:include page="/views/staff/examstaff/includes/examstaff-layout-head.jsp">
            <jsp:param name="activeSidebar" value="ho-so" />
            <jsp:param name="pageTitle" value="Hồ sơ cá nhân" />
            <jsp:param name="noCache" value="true" />
            <jsp:param name="mainClass" value="examstaff-main--scroll" />
            <jsp:param name="resolveQueue" value="false" />
            <jsp:param name="pageCss" value="account.css" />
        </jsp:include>
        <link href="https://fonts.googleapis.com/css2?family=Be+Vietnam+Pro:wght@400;500;600;700&display=swap" rel="stylesheet">
        <div class="account-page account-page--portal">
    </c:when>
    <c:otherwise>
        <jsp:include page="/views/layout/header.jsp">
            <jsp:param name="title" value="Lái Vui - Hồ sơ cá nhân" />
        </jsp:include>
        <link href="https://fonts.googleapis.com/css2?family=Be+Vietnam+Pro:wght@400;500;600;700&display=swap" rel="stylesheet">
        <link rel="stylesheet" href="${ctx}/assets/css/examstaff/account.css">
        <main class="account-page account-page--public">
    </c:otherwise>
</c:choose>

            <div class="account-shell">
                <header class="account-hero">
                    <h1 class="account-hero__title">Hồ sơ cá nhân</h1>
                    <p class="account-hero__sub">Xem và cập nhật thông tin của bạn. Tên đăng nhập, email và số CCCD không thể sửa tại đây.</p>
                </header>

                <c:if test="${not empty message}">
                    <div class="account-alert account-alert--${messageType eq 'success' ? 'success' : 'error'}" role="status">
                        <span class="material-symbols-outlined" aria-hidden="true">
                            ${messageType eq 'success' ? 'check_circle' : 'error'}
                        </span>
                        <span><c:out value="${message}" /></span>
                    </div>
                </c:if>

                <section class="account-panel" aria-label="Thông tin tài khoản">
                    <h2 class="account-panel__title">Tài khoản</h2>
                    <div class="account-grid">
                        <div class="account-field">
                            <label class="account-field__label" for="usernameReadonly">Tên đăng nhập</label>
                            <input id="usernameReadonly" class="account-input" type="text"
                                   value="${fn:escapeXml(u.username)}" readonly disabled>
                        </div>
                        <div class="account-field">
                            <label class="account-field__label" for="emailReadonly">Email</label>
                            <input id="emailReadonly" class="account-input" type="text"
                                   value="${fn:escapeXml(u.email)}" readonly disabled>
                        </div>
                        <div class="account-field account-grid__full">
                            <label class="account-field__label" for="roleReadonly">Vai trò</label>
                            <input id="roleReadonly" class="account-input" type="text"
                                   value="${fn:escapeXml(roleName)}" readonly disabled>
                        </div>
                    </div>
                </section>

                <section class="account-panel" aria-label="Thông tin cá nhân">
                    <h2 class="account-panel__title">Thông tin cá nhân</h2>
                    <c:choose>
                        <c:when test="${empty p}">
                            <p class="account-empty">Chưa có hồ sơ cá nhân gắn với tài khoản. Liên hệ quản trị viên để được hỗ trợ.</p>
                        </c:when>
                        <c:otherwise>
                            <form method="post" action="${ctx}/profile" class="account-stack" novalidate>
                                <div class="account-grid">
                                    <div class="account-field">
                                        <label class="account-field__label" for="fullName">Họ và tên</label>
                                        <input class="account-input" type="text" id="fullName" name="fullName" required
                                               value="${fn:escapeXml(p.fullName)}">
                                    </div>
                                    <div class="account-field">
                                        <label class="account-field__label" for="dateOfBirth">Ngày sinh</label>
                                        <input class="account-input" type="date" id="dateOfBirth" name="dateOfBirth"
                                               value="<fmt:formatDate value='${p.dateOfBirth}' pattern='yyyy-MM-dd' />">
                                    </div>
                                    <div class="account-field">
                                        <label class="account-field__label" for="sex">Giới tính</label>
                                        <select class="account-select" id="sex" name="sex">
                                            <option value="1" <c:if test="${p.sex}">selected="selected"</c:if>>Nam</option>
                                            <option value="0" <c:if test="${not p.sex}">selected="selected"</c:if>>Nữ</option>
                                        </select>
                                    </div>
                                    <div class="account-field">
                                        <label class="account-field__label" for="phoneNumber">Số điện thoại</label>
                                        <input class="account-input" type="tel" id="phoneNumber" name="phoneNumber"
                                               required pattern="0[0-9]{9,10}"
                                               value="${fn:escapeXml(p.phoneNumber)}">
                                        <span class="account-field__hint">10–11 số, bắt đầu bằng 0</span>
                                    </div>
                                    <div class="account-field">
                                        <label class="account-field__label" for="govIdReadonly">Số CCCD</label>
                                        <input id="govIdReadonly" class="account-input" type="text"
                                               value="${fn:escapeXml(p.governmentIdNumber)}" readonly disabled>
                                    </div>
                                    <div class="account-field account-grid__full">
                                        <label class="account-field__label" for="address">Địa chỉ</label>
                                        <input class="account-input" type="text" id="address" name="address"
                                               value="${fn:escapeXml(p.address)}">
                                    </div>
                                </div>
                                <div class="account-actions">
                                    <button type="submit" class="account-btn account-btn--primary">
                                        <span class="material-symbols-outlined" aria-hidden="true">save</span>
                                        Lưu thay đổi
                                    </button>
                                    <a class="account-btn account-btn--muted" href="${ctx}/change-password">
                                        <span class="material-symbols-outlined" aria-hidden="true">lock</span>
                                        Đổi mật khẩu
                                    </a>
                                    <c:if test="${not empty backUrl}">
                                        <a class="account-btn account-btn--ghost" href="${ctx}${backUrl}">
                                            <span class="material-symbols-outlined" aria-hidden="true">arrow_back</span>
                                            Quay lại
                                        </a>
                                    </c:if>
                                </div>
                            </form>
                        </c:otherwise>
                    </c:choose>
                </section>
            </div>

<c:choose>
    <c:when test="${useExamstaffShell}">
        </div>
        <jsp:include page="/views/staff/examstaff/includes/examstaff-layout-foot.jsp" />
    </c:when>
    <c:otherwise>
        </main>
        <jsp:include page="/views/layout/footer.jsp" />
    </c:otherwise>
</c:choose>

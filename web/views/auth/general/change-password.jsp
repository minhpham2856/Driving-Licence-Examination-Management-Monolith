<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<c:set var="ctx" value="${pageContext.request.contextPath}" />
<c:set var="useExamstaffShell" value="${requestScope.accountShell eq 'examstaff'}" />

<c:choose>
    <c:when test="${useExamstaffShell}">
        <jsp:include page="/views/staff/examstaff/includes/examstaff-layout-head.jsp">
            <jsp:param name="activeSidebar" value="doi-mat-khau" />
            <jsp:param name="pageTitle" value="Đổi mật khẩu" />
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
            <jsp:param name="title" value="Lái Vui - Đổi mật khẩu" />
        </jsp:include>
        <link href="https://fonts.googleapis.com/css2?family=Be+Vietnam+Pro:wght@400;500;600;700&display=swap" rel="stylesheet">
        <link rel="stylesheet" href="${ctx}/assets/css/examstaff/account.css">
        <main class="account-page account-page--public">
    </c:otherwise>
</c:choose>

            <div class="account-shell">
                <header class="account-hero">
                    <h1 class="account-hero__title">Đổi mật khẩu</h1>
                    <p class="account-hero__sub">Nhập mật khẩu hiện tại và mật khẩu mới. Mật khẩu mới tối thiểu 6 ký tự và phải khác mật khẩu cũ.</p>
                </header>

                <c:if test="${not empty message}">
                    <div class="account-alert account-alert--${messageType eq 'success' ? 'success' : 'error'}" role="status">
                        <span class="material-symbols-outlined" aria-hidden="true">
                            ${messageType eq 'success' ? 'check_circle' : 'error'}
                        </span>
                        <span><c:out value="${message}" /></span>
                    </div>
                </c:if>

                <section class="account-panel" aria-label="Form đổi mật khẩu">
                    <h2 class="account-panel__title">Bảo mật tài khoản</h2>
                    <form method="post" action="${ctx}/change-password" class="account-stack" autocomplete="off">
                        <div class="account-field">
                            <label class="account-field__label" for="currentPassword">Mật khẩu hiện tại</label>
                            <input class="account-input" type="password" id="currentPassword" name="currentPassword"
                                   required autocomplete="current-password">
                        </div>
                        <div class="account-field">
                            <label class="account-field__label" for="newPassword">Mật khẩu mới</label>
                            <input class="account-input" type="password" id="newPassword" name="newPassword"
                                   required minlength="6" autocomplete="new-password">
                            <span class="account-field__hint">Tối thiểu 6 ký tự</span>
                        </div>
                        <div class="account-field">
                            <label class="account-field__label" for="confirmPassword">Xác nhận mật khẩu mới</label>
                            <input class="account-input" type="password" id="confirmPassword" name="confirmPassword"
                                   required minlength="6" autocomplete="new-password">
                        </div>
                        <div class="account-actions">
                            <button type="submit" class="account-btn account-btn--primary">
                                <span class="material-symbols-outlined" aria-hidden="true">key</span>
                                Cập nhật mật khẩu
                            </button>
                            <a class="account-btn account-btn--ghost" href="${ctx}/profile">
                                <span class="material-symbols-outlined" aria-hidden="true">arrow_back</span>
                                Quay lại hồ sơ
                            </a>
                        </div>
                    </form>
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

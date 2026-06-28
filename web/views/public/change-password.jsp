<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<jsp:include page="/views/layout/header.jsp">
    <jsp:param name="title" value="Lái Vui - Đổi mật khẩu" />
</jsp:include>

<link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/forgot-password.css">

<main class="recovery-page-main">
    <div class="recovery-ambient-glow"></div>
    <div class="recovery-ambient-glow-left"></div>

    <div class="recovery-card">
        <div class="recovery-card__content">
            <div class="recovery-card__badge" aria-hidden="true">
                <svg width="28" height="28" viewBox="0 0 24 24" fill="none"
                     stroke="currentColor" stroke-width="2" stroke-linecap="round"
                     stroke-linejoin="round">
                    <rect x="3" y="11" width="18" height="10" rx="2"/>
                    <path d="M7 11V7a5 5 0 0 1 10 0v4"/>
                </svg>
            </div>

            <div class="recovery-card__header-wrap">
                <h1 class="recovery-card__title">Đổi mật khẩu</h1>
                <p class="recovery-card__subtitle">
                    Sử dụng mật khẩu mạnh và không chia sẻ mật khẩu với người khác.
                </p>
            </div>

            <c:if test="${not empty message}">
                <div role="alert"
                     style="width:100%;box-sizing:border-box;padding:12px 16px;border-radius:8px;text-align:left;
                            background:${messageType eq 'success' ? '#F0FDF4' : '#FEF2F2'};
                            border:1px solid ${messageType eq 'success' ? '#86EFAC' : '#FCA5A5'};
                            color:${messageType eq 'success' ? '#166534' : '#991B1B'};">
                    <c:out value="${message}" />
                </div>
            </c:if>

            <form class="recovery-form"
                  action="${pageContext.request.contextPath}/change-password"
                  method="post">
                <div class="form-group">
                    <label class="form-label" for="currentPassword">Mật khẩu hiện tại</label>
                    <input class="form-input" type="password" id="currentPassword"
                           name="currentPassword" autocomplete="current-password" required
                           style="padding-left:16px;">
                </div>

                <div class="form-group">
                    <label class="form-label" for="newPassword">Mật khẩu mới</label>
                    <input class="form-input" type="password" id="newPassword"
                           name="newPassword" minlength="6" autocomplete="new-password" required
                           style="padding-left:16px;">
                </div>

                <div class="form-group">
                    <label class="form-label" for="confirmPassword">Xác nhận mật khẩu mới</label>
                    <input class="form-input" type="password" id="confirmPassword"
                           name="confirmPassword" minlength="6" autocomplete="new-password" required
                           style="padding-left:16px;">
                </div>

                <button type="submit" class="btn-submit-recovery">Cập nhật mật khẩu</button>
            </form>
        </div>
    </div>
</main>

<jsp:include page="/views/layout/footer.jsp" />

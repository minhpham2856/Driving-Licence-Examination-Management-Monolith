<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<jsp:include page="/views/layout/header.jsp">
    <jsp:param name="title" value="Lái Vui - Đổi mật khẩu" />
</jsp:include>

<link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/landing/forgot-password.css">

<main class="recovery-page-main">
    <div class="recovery-card">
        <div class="recovery-card__content">
            <div class="recovery-card__header-wrap">
                <h2 class="recovery-card__title">Đổi mật khẩu</h2>
                <p class="recovery-card__subtitle">Nhập mật khẩu hiện tại và mật khẩu mới.</p>
            </div>

            <c:if test="${not empty message}">
                <div style="width:100%;padding:12px 16px;border-radius:8px;margin-bottom:12px;
                    background-color:${messageType eq 'success' ? '#F0FDF4' : '#FEF2F2'};
                    border:1px solid ${messageType eq 'success' ? '#86EFAC' : '#FCA5A5'};
                    color:${messageType eq 'success' ? '#166534' : '#991B1B'};">
                    <c:out value="${message}" />
                </div>
            </c:if>

            <form method="post" action="${pageContext.request.contextPath}/change-password" style="width:100%;display:flex;flex-direction:column;gap:12px;">
                <input type="password" name="currentPassword" placeholder="Mật khẩu hiện tại" required class="recovery-card__input">
                <input type="password" name="newPassword" placeholder="Mật khẩu mới" required class="recovery-card__input">
                <input type="password" name="confirmPassword" placeholder="Xác nhận mật khẩu mới" required class="recovery-card__input">
                <button type="submit" class="recovery-card__submit-btn">Cập nhật mật khẩu</button>
            </form>
        </div>
    </div>
</main>

<jsp:include page="/views/layout/footer.jsp" />

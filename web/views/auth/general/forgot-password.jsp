<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>

<jsp:include page="/views/layout/header.jsp">
    <jsp:param name="title" value="Lái Vui - Quên mật khẩu" />
</jsp:include>

<!-- Link custom stylesheet for the forgot password page -->
<link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/landing/forgot-password.css">

<main class="recovery-page-main">
    <!-- Ambient Blur Background Overlays (matching Figma specs!) -->
    <div class="recovery-ambient-glow"></div>
    <div class="recovery-ambient-glow-left"></div>
    
    <!-- Central Card -->
    <div class="recovery-card">
        <div class="recovery-card__content">
            
            <!-- Top visual illustration key badge -->
            <div class="recovery-card__badge">
                <span class="material-symbols-outlined" style="font-size:28px">vpn_key</span>
            </div>
            
            <!-- Header elements -->
            <div class="recovery-card__header-wrap">
                <h2 class="recovery-card__title">Quên mật khẩu?</h2>
                <p class="recovery-card__subtitle">Nhập địa chỉ email của bạn để nhận liên kết khôi phục mật khẩu.</p>
            </div>
            
            <!-- JSTL server-side warning blocks -->
            <c:if test="${not empty error}">
                <div style="width: 100%; background-color: #FEF2F2; border: 1px solid #FCA5A5; color: #991B1B; padding: 12px 16px; border-radius: 8px; font-family: 'Inter', sans-serif; font-size: 14px; display: flex; align-items: center; gap: 8px; box-sizing: border-box; text-align: left;">
                    <span class="material-symbols-outlined" style="font-size:18px;flex-shrink:0;">error</span>
                    <c:out value="${error}" />
                </div>
            </c:if>
            <c:if test="${not empty success}">
                <div style="width: 100%; background-color: #F0FDF4; border: 1px solid #86EFAC; color: #166534; padding: 12px 16px; border-radius: 8px; font-family: 'Inter', sans-serif; font-size: 14px; display: flex; align-items: center; gap: 8px; box-sizing: border-box; text-align: left;">
                    <span class="material-symbols-outlined" style="font-size:18px;flex-shrink:0;">check_circle</span>
                    <c:out value="${success}" />
                </div>
            </c:if>
            
            <!-- Recovery Action Form without Javascript -->
            <form class="recovery-form" action="${pageContext.request.contextPath}/forgot-password" method="POST">
                
                <!-- Email Input Field -->
                <div class="form-group">
                    <label class="form-label" for="email">Địa chỉ Email</label>
                    <div class="input-icon-wrapper">
                        <span class="input-icon">
                            <span class="material-symbols-outlined" style="font-size:18px">mail</span>
                        </span>
                        <input class="form-input" type="email" id="email" name="email" placeholder="example@gmail.com" required>
                    </div>
                </div>
                
                <button type="submit" class="btn-submit-recovery">Gửi link khôi phục</button>
            </form>
            
            <!-- Alternate Switch Options -->
            <div class="alternate-actions-wrap">
                <a href="login.jsp" class="alternate-action-link">
                    <span class="material-symbols-outlined" style="font-size:16px">arrow_back</span>
                    Quay lại đăng nhập
                </a>
                
                <a href="register.jsp" class="alternate-action-link alternate-action-link--register">
                    Bạn chưa có tài khoản? <span>Đăng ký ngay</span>
                </a>
            </div>
            
        </div>
    </div>
</main>

<jsp:include page="/views/layout/footer.jsp" />

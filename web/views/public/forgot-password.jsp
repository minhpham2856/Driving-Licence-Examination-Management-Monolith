<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>

<jsp:include page="/views/layout/header.jsp">
    <jsp:param name="title" value="Lái Vui - Quên mật khẩu" />
</jsp:include>

<!-- Link custom stylesheet for the forgot password page -->
<link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/forgot-password.css">

<main class="recovery-page-main">
    <!-- Ambient Blur Background Overlays (matching Figma specs!) -->
    <div class="recovery-ambient-glow"></div>
    <div class="recovery-ambient-glow-left"></div>
    
    <!-- Central Card -->
    <div class="recovery-card">
        <div class="recovery-card__content">
            
            <!-- Top visual illustration key badge -->
            <div class="recovery-card__badge">
                <svg width="28" height="28" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                    <path d="M21 2l-2 2m-7.61 7.61a5.5 5.5 0 1 1-7.778 7.778 5.5 5.5 0 0 1 7.777-7.777zm0 0L15.5 7.5m0 0l3 3L22 7l-3-3m-3.5 3.5L19 4"/>
                </svg>
            </div>
            
            <!-- Header elements -->
            <div class="recovery-card__header-wrap">
                <h2 class="recovery-card__title">Quên mật khẩu?</h2>
                <p class="recovery-card__subtitle">Nhập địa chỉ email của bạn để nhận liên kết khôi phục mật khẩu.</p>
            </div>
            
            <!-- JSTL server-side warning blocks -->
            <c:if test="${not empty error}">
                <div style="width: 100%; background-color: #FEF2F2; border: 1px solid #FCA5A5; color: #991B1B; padding: 12px 16px; border-radius: 8px; font-family: 'Inter', sans-serif; font-size: 14px; display: flex; align-items: center; gap: 8px; box-sizing: border-box; text-align: left;">
                    <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" style="flex-shrink:0;">
                        <circle cx="12" cy="12" r="10"/>
                        <line x1="12" y1="8" x2="12" y2="12"/>
                        <line x1="12" y1="16" x2="12.01" y2="16"/>
                    </svg>
                    <c:out value="${error}" />
                </div>
            </c:if>
            <c:if test="${not empty success}">
                <div style="width: 100%; background-color: #F0FDF4; border: 1px solid #86EFAC; color: #166534; padding: 12px 16px; border-radius: 8px; font-family: 'Inter', sans-serif; font-size: 14px; display: flex; align-items: center; gap: 8px; box-sizing: border-box; text-align: left;">
                    <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" style="flex-shrink:0;">
                        <path d="M22 11.08V12a10 10 0 1 1-5.93-9.14"/>
                        <polyline points="22 4 12 14.01 9 11.01"/>
                    </svg>
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
                            <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                                <path d="M4 4h16c1.1 0 2 .9 2 2v12c0 1.1-.9 2-2 2H4c-1.1 0-2-.9-2-2V6c0-1.1.9-2 2-2z"/>
                                <polyline points="22,6 12,13 2,6"/>
                            </svg>
                        </span>
                        <input class="form-input" type="email" id="email" name="email" placeholder="example@gmail.com" required>
                    </div>
                </div>
                
                <button type="submit" class="btn-submit-recovery">Gửi link khôi phục</button>
            </form>
            
            <!-- Alternate Switch Options -->
            <div class="alternate-actions-wrap">
                <a href="${pageContext.request.contextPath}/login" class="alternate-action-link">
                    <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round">
                        <line x1="19" y1="12" x2="5" y2="12"/>
                        <polyline points="12 19 5 12 12 5"/>
                    </svg>
                    Quay lại đăng nhập
                </a>
                
                <a href="${pageContext.request.contextPath}/register" class="alternate-action-link alternate-action-link--register">
                    Bạn chưa có tài khoản? <span>Đăng ký ngay</span>
                </a>
            </div>
            
        </div>
    </div>
</main>

<jsp:include page="/views/layout/footer.jsp" />

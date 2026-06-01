<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>

<!--HEADER-->
<jsp:include page="/views/layout/header.jsp">
    <jsp:param name="title" value="Lái Vui - Đăng nhập" />
</jsp:include>
<!--HEADER-->

<link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/login.css">

<main class="login-page-main">
    <div class="login-card-container">

        <!-- Left Side: Visual / Branding -->
        <div class="login-visual-panel">
            <div class="login-visual-panel__overlay"></div>
            <div class="login-visual-panel__gradient"></div>

            <div class="login-visual-panel__content">
                <div class="login-visual-panel__brand">
                    <img src="${pageContext.request.contextPath}/assets/imgs/LOGO.png" alt="Lái Vui" class="login-visual-panel__logo" width="36" height="36">
                    <span class="login-visual-panel__brand-name">Lái Vui</span>
                </div>
                <div class="login-visual-panel__text-wrap">
                    <h1 class="login-visual-panel__heading">
                        Hành trình vạn dặm<br>
                          <span>bắt đầu từ tay lái an toàn.</span>
                    </h1>
                    <p class="login-visual-panel__subtitle">
                        Trung tâm đào tạo và sát hạch GPLX hiện đại nhất Việt Nam.
                    </p>
                </div>
            </div>
        </div>

        <!-- Right Side: Login Form -->
        <div class="login-form-panel">
            <div>
                <div class="login-form-panel__header">
                    <h2 class="login-form-panel__title">Chào mừng trở lại!</h2>
                    <p class="login-form-panel__subtitle">Vui lòng nhập thông tin để truy cập.</p>
                </div>

                <!-- Server-side alert feedback -->
                <c:if test="${not empty error}">
                    <div style="background-color: #FEF2F2; border: 1px solid #FCA5A5; color: #991B1B; padding: 12px 16px; border-radius: 8px; margin-bottom: 24px; font-family: 'Inter', sans-serif; font-size: 14px; display: flex; align-items: center; gap: 8px;">
                        <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" style="flex-shrink:0;">
                        <circle cx="12" cy="12" r="10"/>
                        <line x1="12" y1="8" x2="12" y2="12"/>
                        <line x1="12" y1="16" x2="12.01" y2="16"/>
                        </svg>
                        <c:out value="${error}" />
                    </div>
                </c:if>
                <c:if test="${not empty success}">
                    <div style="background-color: #F0FDF4; border: 1px solid #86EFAC; color: #166534; padding: 12px 16px; border-radius: 8px; margin-bottom: 24px; font-family: 'Inter', sans-serif; font-size: 14px; display: flex; align-items: center; gap: 8px;">
                        <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" style="flex-shrink:0;">
                        <path d="M22 11.08V12a10 10 0 1 1-5.93-9.14"/>
                        <polyline points="22 4 12 14.01 9 11.01"/>
                        </svg>
                        <c:out value="${success}" />
                    </div>
                </c:if>

                <!-- Login Form -->
                <form class="login-form" action="${pageContext.request.contextPath}/login" method="POST">

                    <!-- Email / Username Field -->
                    <div class="form-group">
                        <label class="form-label" for="identifier">Email hoặc SĐT</label>
                        <div class="input-icon-wrapper">
                            <span class="input-icon">
                                <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                                <path d="M4 4h16c1.1 0 2 .9 2 2v12c0 1.1-.9 2-2 2H4c-1.1 0-2-.9-2-2V6c0-1.1.9-2 2-2z"/>
                                <polyline points="22,6 12,13 2,6"/>
                                </svg>
                            </span>
                            <input class="form-input" type="text" id="identifier" name="identifier" placeholder="example@gmail.com hoặc 0912..." required>
                        </div>
                    </div>

                    <!-- Password Field -->
                    <div class="form-group">
                        <div class="form-label-row">
                            <label class="form-label" for="password">Mật khẩu</label>
                            <a href="${pageContext.request.contextPath}/forgot-password" class="forgot-password-link">Quên mật khẩu?</a>
                        </div>
                        <div class="input-icon-wrapper">
                            <span class="input-icon">
                                <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                                <rect x="3" y="11" width="18" height="11" rx="2" ry="2"/>
                                <path d="M7 11V7a5 5 0 0 1 10 0v4"/>
                                </svg>
                            </span>
                            <input class="form-input" type="password" id="password" name="password" placeholder="••••••••" required>
                            <span class="password-toggle-visual">
                                <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                                <path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z"/>
                                <circle cx="12" cy="12" r="3"/>
                                </svg>
                            </span>
                        </div>
                    </div>

                    <!-- Remember Me Checkbox -->
                    <div class="remember-me-group">
                        <input type="checkbox" id="rememberMe" name="rememberMe">
                        <label for="rememberMe" class="remember-me-text">Ghi nhớ đăng nhập</label>
                    </div>

                    <!-- Submit Area -->
                    <div class="form-submit-wrap">
                        <button type="submit" class="btn-submit-login">
                            Đăng nhập
                            <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                            <line x1="5" y1="12" x2="19" y2="12"/>
                            <polyline points="12 5 19 12 12 19"/>
                            </svg>
                        </button>

                        <div class="alternate-prompt-wrap">
                            <p class="alternate-prompt-text">Chưa có tài khoản?</p>
                            <a href="${pageContext.request.contextPath}/register" class="btn-capsule-register">
                                Đăng ký ngay
                                <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round">
                                <line x1="5" y1="12" x2="19" y2="12"/>
                                <polyline points="12 5 19 12 12 19"/>
                                </svg>
                            </a>
                        </div>
                    </div>
                </form>
            </div>
        </div>

    </div>
</main>

<!--FOOTER-->
<jsp:include page="/views/layout/footer.jsp" />
<!--FOOTER-->
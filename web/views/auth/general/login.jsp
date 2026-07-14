<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>

<!--HEADER-->
<jsp:include page="/views/layout/header.jsp">
    <jsp:param name="title" value="Lái Vui - Đăng nhập" />
</jsp:include>
<!--HEADER-->

<link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/landing/login.css">

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
                        <span class="material-symbols-outlined" style="font-size:18px">error</span>
                        <c:out value="${error}" />
                    </div>
                </c:if>
                <c:if test="${not empty success}">
                    <div style="background-color: #F0FDF4; border: 1px solid #86EFAC; color: #166534; padding: 12px 16px; border-radius: 8px; margin-bottom: 24px; font-family: 'Inter', sans-serif; font-size: 14px; display: flex; align-items: center; gap: 8px;">
                        <span class="material-symbols-outlined" style="font-size:18px">check_circle</span>
                        <c:out value="${success}" />
                    </div>
                </c:if>
                <c:if test="${not empty registrationUsername}">
                    <div style="background-color: #EFF6FF; border: 1px solid #93C5FD; color: #1E3A8A; padding: 12px 16px; border-radius: 8px; margin-bottom: 24px; font-family: 'Inter', sans-serif; font-size: 14px;">
                        <p style="margin: 0 0 8px; font-weight: 600;">Thông tin đăng nhập của bạn</p>
                        <p style="margin: 0 0 4px;">Tên đăng nhập: <strong><c:out value="${registrationUsername}" /></strong></p>
                        <p style="margin: 0;">Mật khẩu: <strong><c:out value="${registrationPassword}" /></strong></p>
                    </div>
                </c:if>

                <!-- Login Form -->
                <form class="login-form" action="${pageContext.request.contextPath}/login" method="POST">

                    <!-- Email / Username Field -->
                    <div class="form-group">
                        <label class="form-label" for="identifier">Tên đăng nhập, Email hoặc SĐT</label>
                        <div class="input-icon-wrapper">
                            <span class="input-icon">
                                <span class="material-symbols-outlined" style="font-size:18px">mail</span>
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
                                <span class="material-symbols-outlined" style="font-size:18px">lock</span>
                            </span>
                            <input class="form-input" type="password" id="password" name="password" placeholder="••••••••" required>
                            <span class="password-toggle-visual">
                                <span class="material-symbols-outlined" style="font-size:18px">visibility</span>
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
                            <span class="material-symbols-outlined" style="font-size:18px">arrow_forward</span>
                        </button>

                        <div class="alternate-prompt-wrap">
                            <p class="alternate-prompt-text">Chưa có tài khoản?</p>
                            <a href="${pageContext.request.contextPath}/register" class="btn-capsule-register">
                                Đăng ký ngay
                                <span class="material-symbols-outlined" style="font-size:16px">arrow_forward</span>
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
<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<c:set var="ctx" value="${pageContext.request.contextPath}" />
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Lái Vui - Đăng nhập</title>
    <link rel="preconnect" href="https://fonts.googleapis.com">
    <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
    <link href="https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700;800&display=swap" rel="stylesheet">
    <link href="https://fonts.googleapis.com/css2?family=Material+Symbols+Outlined:opsz,wght,FILL,GRAD@24,400,0,0&display=swap" rel="stylesheet">
    <link rel="stylesheet" href="${ctx}/assets/css/landing/login.css">
</head>
<body class="auth-split-body">
<main class="auth-split">
    <aside class="auth-split__visual auth-split__visual--login"
           style="background-image: url('${ctx}/assets/imgs/smiling_professional_driver.png');">
        <div class="auth-split__overlay"></div>
        <div class="auth-split__gradient"></div>
        <div class="auth-split__visual-inner">
            <a href="${ctx}/home" class="auth-split__brand">
                <img src="${ctx}/assets/imgs/LOGO.png" alt="Lái Vui" width="36" height="36">
                <span>Lái Vui</span>
            </a>
            <div class="auth-split__copy">
                <h1 class="auth-split__heading">Hành trình vạn dặm<br><span>bắt đầu từ tay lái an toàn.</span></h1>
                <p class="auth-split__sub">Trung tâm đào tạo và sát hạch GPLX hiện đại.</p>
                <a href="${ctx}/home" class="auth-split__home-link">
                    <span class="material-symbols-outlined" aria-hidden="true">arrow_back</span>
                    Về trang chủ
                </a>
            </div>
        </div>
    </aside>

    <section class="auth-split__form-pane">
        <div class="auth-split__form-inner">
            <header class="auth-split__form-header">
                <h2>Chào mừng trở lại</h2>
                <p>Nhập thông tin để truy cập tài khoản.</p>
            </header>

            <c:if test="${not empty error}">
                <div class="auth-alert auth-alert--error" role="alert">
                    <span class="material-symbols-outlined" aria-hidden="true">error</span>
                    <span><c:out value="${error}" /></span>
                </div>
            </c:if>
            <c:if test="${not empty success}">
                <div class="auth-alert auth-alert--success" role="status">
                    <span class="material-symbols-outlined" aria-hidden="true">check_circle</span>
                    <span><c:out value="${success}" /></span>
                </div>
            </c:if>
            <c:if test="${not empty registrationUsername}">
                <div class="auth-alert auth-alert--info" role="status">
                    <p class="auth-alert__title">Thông tin đăng nhập của bạn</p>
                    <p>Tên đăng nhập: <strong><c:out value="${registrationUsername}" /></strong></p>
                    <p>Mật khẩu: <strong><c:out value="${registrationPassword}" /></strong></p>
                </div>
            </c:if>

            <form class="login-form" action="${ctx}/login" method="POST" autocomplete="on">
                <div class="form-group">
                    <label class="form-label" for="identifier">Tên đăng nhập, Email hoặc số căn cước</label>
                    <div class="input-icon-wrapper">
                        <span class="input-icon material-symbols-outlined" aria-hidden="true">person</span>
                        <input class="form-input" type="text" id="identifier" name="identifier"
                               placeholder="example@gmail.com hoặc số căn cước" required
                               autocomplete="username">
                    </div>
                </div>
                <div class="form-group">
                    <div class="form-label-row">
                        <label class="form-label" for="password">Mật khẩu</label>
                        <a href="${ctx}/forgot-password" class="forgot-password-link">Quên mật khẩu?</a>
                    </div>
                    <div class="input-icon-wrapper">
                        <span class="input-icon material-symbols-outlined" aria-hidden="true">lock</span>
                        <input class="form-input" type="password" id="password" name="password"
                               placeholder="••••••••" required autocomplete="current-password">
                    </div>
                </div>
<!--                <div class="remember-me-group">
                    <input type="checkbox" id="rememberMe" name="rememberMe">
                    <label for="rememberMe" class="remember-me-text">Ghi nhớ đăng nhập</label>
                </div>-->
                <div class="form-submit-wrap">
                    <button type="submit" class="btn-submit-login">
                        Đăng nhập
                        <span class="material-symbols-outlined" aria-hidden="true">arrow_forward</span>
                    </button>
                    <p class="alternate-prompt-text">
                        Chưa có tài khoản?
                        <a href="${ctx}/register">Đăng ký ngay</a>
                    </p>
                </div>
            </form>
        </div>
    </section>
</main>
</body>
</html>

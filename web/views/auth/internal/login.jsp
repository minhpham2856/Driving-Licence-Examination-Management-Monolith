<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>

<!DOCTYPE html>
<html lang="vi">

    <head>
        <meta charset="UTF-8">
        <meta name="viewport" content="width=device-width, initial-scale=1.0">
        <title>${pageTitle}</title>
        <link rel="preconnect" href="https://fonts.googleapis.com">
        <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
        <link href="https://fonts.googleapis.com/css2?family=Inter:wght@400;600;700;800&display=swap" rel="stylesheet">
        <link href="https://fonts.googleapis.com/css2?family=Material+Symbols+Outlined:opsz,wght,FILL,GRAD@24,400,0,0&display=swap" rel="stylesheet">
        <link rel="stylesheet" href="${ctx}/assets/css/style.css">
        <link rel="stylesheet" href="${ctx}/assets/css/layout.css">
    </head>

    <body class="${bodyClass}">

        <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/landing/login.css">

        <main class="login-page-main login-page-main--standalone">
            <div class="login-card-container login-card-container--standalone">



                <!-- Right Side: Login Form -->
                <div class="login-form-panel">
                    <div>
                        <div class="login-form-panel__header">
                            <h2 class="login-form-panel__title">Cổng xác thực nhân sự</h2>
                        </div>

                        <!-- Server-side alert feedback -->
                        <c:if test="${not empty error}">
                            <div
                                style="background-color: #FEF2F2; border: 1px solid #FCA5A5; color: #991B1B; padding: 12px 16px; border-radius: 8px; margin-bottom: 24px; font-family: 'Inter', sans-serif; font-size: 14px; display: flex; align-items: center; gap: 8px;">
                                <span class="material-symbols-outlined"
                                      style="font-size:18px;flex-shrink:0;">error</span>
                                <c:out value="${error}" />
                            </div>
                        </c:if>
                        <c:if test="${not empty success}">
                            <div
                                style="background-color: #F0FDF4; border: 1px solid #86EFAC; color: #166534; padding: 12px 16px; border-radius: 8px; margin-bottom: 24px; font-family: 'Inter', sans-serif; font-size: 14px; display: flex; align-items: center; gap: 8px;">
                                <span class="material-symbols-outlined"
                                      style="font-size:18px;flex-shrink:0;">check_circle</span>
                                <c:out value="${success}" />
                            </div>
                        </c:if>
                        <c:if test="${not empty registrationUsername}">
                            <div
                                style="background-color: #EFF6FF; border: 1px solid #93C5FD; color: #1E3A8A; padding: 12px 16px; border-radius: 8px; margin-bottom: 24px; font-family: 'Inter', sans-serif; font-size: 14px;">
                                <p style="margin: 0 0 8px; font-weight: 600;">Thông tin đăng nhập của bạn</p>
                                <p style="margin: 0 0 4px;">Tên đăng nhập: <strong>
                                        <c:out value="${registrationUsername}" />
                                    </strong></p>
                                <p style="margin: 0;">Mật khẩu: <strong>
                                        <c:out value="${registrationPassword}" />
                                    </strong></p>
                            </div>
                        </c:if>

                        <!-- Login Form -->
                        <form class="login-form" action="${pageContext.request.contextPath}/staff/login"
                              method="POST">

                            <!-- Email / Username Field -->
                            <div class="form-group">
                                <label class="form-label" for="identifier">Tên đăng nhập, Email hoặc SĐT</label>
                                <div class="input-icon-wrapper">
                                    <span class="input-icon">
                                        <span class="material-symbols-outlined" style="font-size:18px">mail</span>
                                    </span>
                                    <input class="form-input" type="text" id="identifier" name="identifier"
                                           placeholder="example@gmail.com hoặc 0912..." required>
                                </div>
                            </div>

                            <!-- Password Field -->
                            <div class="form-group">
                                <div class="form-label-row">
                                    <label class="form-label" for="password">Mật khẩu</label>
                                    <a href="${pageContext.request.contextPath}/forgot-password"
                                       class="forgot-password-link">Quên mật khẩu?</a>
                                </div>
                                <div class="input-icon-wrapper">
                                    <span class="input-icon">
                                        <span class="material-symbols-outlined" style="font-size:18px">lock</span>
                                    </span>
                                    <input class="form-input" type="password" id="password" name="password"
                                           placeholder="••••••••" required>
                                    <span class="password-toggle-visual">
                                        <span class="material-symbols-outlined"
                                              style="font-size:18px">visibility</span>
                                    </span>
                                </div>
                            </div>


                            <!-- Submit Area -->
                            <div class="form-submit-wrap">
                                <button type="submit" class="btn-submit-login">
                                    Đăng nhập
                                    <span class="material-symbols-outlined"
                                          style="font-size:18px">arrow_forward</span>
                                </button>

                            </div>
                        </form>
                    </div>
                </div>

            </div>
        </main>
    </body>

</html>

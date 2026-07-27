<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>

<%--context--%>
<c:set var="ctx" value="${pageContext.request.contextPath}" />

<!DOCTYPE html>
<html lang="vi">
    <head>
        <meta charset="UTF-8">
        <meta name="viewport" content="width=device-width, initial-scale=1.0">
        <title>Lái Vui - Quên mật khẩu</title>
        <link rel="preconnect" href="https://fonts.googleapis.com">
        <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
        <link href="https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700;800&display=swap"
              rel="stylesheet">
        <link href="https://fonts.googleapis.com/css2?family=Material+Symbols+Outlined:opsz,wght,FILL,GRAD@24,400,0,0&display=swap"
              rel="stylesheet">
        <link rel="stylesheet" href="${ctx}/assets/css/landing/forgot-password.css">
    </head>
    <body class="auth-split-body">
        <main class="auth-split">
            <%--visual pane--%>
            <aside class="auth-split__visual auth-split__visual--login"
                   style="background-image: url('${ctx}/assets/imgs/smiling_professional_driver.png');">
                <div class="auth-split__overlay"></div>
                <div class="auth-split__gradient"></div>
                <div class="auth-split__visual-inner">
                    <a href="${ctx}/home" class="auth-split__brand">
                        <img src="${ctx}/assets/imgs/LOGO.png"
                             alt="Lái Vui"
                             width="36"
                             height="36">
                        <span>Lái Vui</span>
                    </a>
                    <div class="auth-split__copy">
                        <h1 class="auth-split__heading">
                            Khôi phục<br><span>truy cập an toàn.</span>
                        </h1>
                        <p class="auth-split__sub">
                            Nhập email đã đăng ký để nhận mật khẩu tạm thời.
                        </p>
                        <a href="${ctx}/home" class="auth-split__home-link">
                            <span class="material-symbols-outlined" aria-hidden="true">arrow_back</span>
                            Về trang chủ
                        </a>
                    </div>
                </div>
            </aside>

            <%--form pane--%>
            <section class="auth-split__form-pane">
                <div class="auth-split__form-inner">
                    <header class="auth-split__form-header">
                        <h2>Quên mật khẩu?</h2>
                        <p>Nhập địa chỉ email để nhận thông tin khôi phục.</p>
                    </header>

                    <%--case 1: error--%>
                    <c:if test="${not empty error}">
                        <div class="auth-alert auth-alert--error" role="alert">
                            <span class="material-symbols-outlined" aria-hidden="true">error</span>
                            <span><c:out value="${error}" /></span>
                        </div>
                    </c:if>
                    <%--case 2: success--%>
                    <c:if test="${not empty success}">
                        <div class="auth-alert auth-alert--success" role="status">
                            <span class="material-symbols-outlined" aria-hidden="true">check_circle</span>
                            <span><c:out value="${success}" /></span>
                        </div>
                    </c:if>

                    <form class="recovery-form"
                          action="${ctx}/forgot-password"
                          method="POST"
                          autocomplete="on">
                        <div class="form-group">
                            <label class="form-label" for="email">Địa chỉ email</label>
                            <div class="input-icon-wrapper">
                                <span class="input-icon material-symbols-outlined"
                                      aria-hidden="true">mail</span>
                                <input class="form-input"
                                       type="email"
                                       id="email"
                                       name="email"
                                       placeholder="example@gmail.com"
                                       required
                                       autocomplete="email">
                            </div>
                        </div>
                        <button type="submit" class="btn-submit-recovery">
                            Gửi thông tin khôi phục
                        </button>
                    </form>

                    <div class="alternate-actions-wrap">
                        <a href="${ctx}/login" class="alternate-action-link">
                            <span class="material-symbols-outlined" aria-hidden="true">arrow_back</span>
                            Quay lại đăng nhập
                        </a>
                        <a href="${ctx}/register" class="alternate-action-link">
                            Chưa có tài khoản? <strong>Đăng ký ngay</strong>
                        </a>
                    </div>
                </div>
            </section>
        </main>
    </body>
</html>

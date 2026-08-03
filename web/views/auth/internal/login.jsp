<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<%--context--%>
<c:set var="ctx" value="${pageContext.request.contextPath}" />

<!DOCTYPE html>
<html lang="vi">
    <head>
        <meta charset="UTF-8">
        <meta name="viewport" content="width=device-width, initial-scale=1.0">
        <title>Đăng nhập nhân sự | Lái Vui</title>
        <link rel="preconnect" href="https://fonts.googleapis.com">
        <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
        <link href="https://fonts.googleapis.com/css2?family=Inter:wght@400;600;700;800&display=swap"
              rel="stylesheet">
        <link href="https://fonts.googleapis.com/css2?family=Material+Symbols+Outlined:opsz,wght,FILL,GRAD@24,400,0,0&display=swap"
              rel="stylesheet">
        <link rel="stylesheet" href="${ctx}/assets/css/landing/internal-login.css">
    </head>
    <body class="staff-login-body">
        <main class="staff-login">
            <div class="staff-login__card">
                <header class="staff-login__header">
                    <img src="${ctx}/assets/imgs/LOGO.png"
                         alt="Lái Vui"
                         width="48"
                         height="48"
                         class="staff-login__logo">
                    <h1 class="staff-login__title">Cổng xác thực nhân sự</h1>
                    <p class="staff-login__subtitle">
                        Đăng nhập để truy cập hệ thống nội bộ
                    </p>
                </header>

                <%--account type switcher: mirrors the registrant login page--%>
                <nav class="staff-login__tabs" aria-label="Chọn loại tài khoản">
                    <a class="staff-login__tab" href="${ctx}/login">
                        <span class="material-symbols-outlined" aria-hidden="true">person</span>
                        Người đăng ký thi
                    </a>
                    <span class="staff-login__tab staff-login__tab--active" aria-current="page">
                        <span class="material-symbols-outlined" aria-hidden="true">badge</span>
                        Cán bộ, nhân viên
                    </span>
                </nav>

                <%--role hint: the account itself decides the landing page--%>
                <p class="staff-login__roles">
                    Dùng chung cho Quản trị viên, Sát hạch viên, Cán bộ quản lý,
                    Cán bộ kỳ thi và Cán bộ CSGT. Hệ thống tự chuyển đúng khu vực theo tài khoản.
                </p>

                <%--case 1: error--%>
                <c:if test="${not empty error}">
                    <div class="staff-login__alert staff-login__alert--error" role="alert">
                        <span class="material-symbols-outlined" aria-hidden="true">error</span>
                        <span><c:out value="${error}" /></span>
                    </div>
                </c:if>
                <%--case 2: success--%>
                <c:if test="${not empty success}">
                    <div class="staff-login__alert staff-login__alert--success" role="status">
                        <span class="material-symbols-outlined" aria-hidden="true">check_circle</span>
                        <span><c:out value="${success}" /></span>
                    </div>
                </c:if>

                <form class="staff-login__form"
                      action="${ctx}/staff/login"
                      method="POST"
                      autocomplete="on">
                    <div class="staff-login__field">
                        <label class="staff-login__label" for="identifier">
                            Tên đăng nhập, Email hoặc căn cước
                        </label>
                        <div class="staff-login__input-wrap">
                            <span class="material-symbols-outlined staff-login__icon"
                                  aria-hidden="true">person</span>
                            <input class="staff-login__input"
                                   type="text"
                                   id="identifier"
                                   name="identifier"
                                   placeholder="example@gmail.com hoặc số căn cước"
                                   required
                                   autocomplete="username">
                        </div>
                    </div>

                    <div class="staff-login__field">
                        <div class="staff-login__label-row">
                            <label class="staff-login__label" for="password">Mật khẩu</label>
                            <a href="${ctx}/forgot-password" class="staff-login__link">
                                Quên mật khẩu?
                            </a>
                        </div>
                        <div class="staff-login__input-wrap">
                            <span class="material-symbols-outlined staff-login__icon"
                                  aria-hidden="true">lock</span>
                            <input class="staff-login__input"
                                   type="password"
                                   id="password"
                                   name="password"
                                   placeholder="••••••••"
                                   required
                                   autocomplete="current-password">
                        </div>
                    </div>

                    <button type="submit" class="staff-login__submit">
                        Đăng nhập
                        <span class="material-symbols-outlined" aria-hidden="true">arrow_forward</span>
                    </button>
                </form>
            </div>
        </main>
    </body>
</html>

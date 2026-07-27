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
        <title>Lái Vui - Đăng ký tài khoản</title>
        <link rel="preconnect" href="https://fonts.googleapis.com">
        <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
        <link href="https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700;800&display=swap"
              rel="stylesheet">
        <link href="https://fonts.googleapis.com/css2?family=Material+Symbols+Outlined:opsz,wght,FILL,GRAD@24,400,0,0&display=swap"
              rel="stylesheet">
        <link rel="stylesheet" href="${ctx}/assets/css/landing/register.css">
    </head>
    <body class="auth-split-body">
        <main class="auth-split">
            <%--visual pane--%>
            <aside class="auth-split__visual auth-split__visual--register"
                   style="background-image: url('${ctx}/assets/imgs/register_bg.png');">
                <div class="auth-split__overlay"></div>
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
                            Đăng ký thi GPLX<br><span>dễ dàng, tiện lợi</span>
                        </h1>
                        <p class="auth-split__sub">
                            Đơn giản hoá hồ sơ, thủ tục và nhận kết quả nhanh chóng, chính xác.
                        </p>
                        <ul class="auth-split__benefits">
                            <li>
                                <span class="material-symbols-outlined" aria-hidden="true">verified</span>
                                <span>Uy tín hàng đầu</span>
                            </li>
                            <li>
                                <span class="material-symbols-outlined" aria-hidden="true">calendar_month</span>
                                <span>Thủ tục nhanh chóng</span>
                            </li>
                        </ul>
                        <a href="${ctx}/home" class="auth-split__home-link">
                            <span class="material-symbols-outlined" aria-hidden="true">arrow_back</span>
                            Về trang chủ
                        </a>
                    </div>
                </div>
            </aside>

            <%--form pane--%>
            <section class="auth-split__form-pane auth-split__form-pane--scroll">
                <div class="auth-split__form-inner">
                    <header class="auth-split__form-header">
                        <h2>Tạo tài khoản mới</h2>
                        <p>
                            Điền thông tin cá nhân - hệ thống tạo tên đăng nhập và gửi qua email.
                        </p>
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

                    <form class="register-form"
                          action="${ctx}/register"
                          method="POST"
                          autocomplete="on">
                        <div class="form-row-two-col">
                            <div class="form-group form-group--half">
                                <label class="form-label" for="govIdNo">Số căn cước</label>
                                <input class="form-input form-input--plain"
                                       type="text"
                                       id="govIdNo"
                                       name="govIdNo"
                                       placeholder="001203012345"
                                       required
                                       maxlength="12"
                                       pattern="[0-9]{12}"
                                       inputmode="numeric"
                                       autocomplete="off">
                            </div>
                            <div class="form-group form-group--half">
                                <label class="form-label" for="fullName">Họ và tên</label>
                                <input class="form-input form-input--plain"
                                       type="text"
                                       id="fullName"
                                       name="fullName"
                                       placeholder="Nguyễn Văn Bình"
                                       required
                                       maxlength="200"
                                       autocomplete="name">
                            </div>
                        </div>
                        <div class="form-row-two-col">
                            <div class="form-group form-group--half">
                                <label class="form-label" for="phoneNo">Số điện thoại</label>
                                <input class="form-input form-input--plain"
                                       type="tel"
                                       id="phoneNo"
                                       name="phoneNo"
                                       placeholder="0912345678"
                                       required
                                       maxlength="20"
                                       autocomplete="tel">
                            </div>
                            <div class="form-group form-group--half">
                                <label class="form-label" for="dateOfBirth">Ngày sinh</label>
                                <input class="form-input form-input--plain"
                                       type="date"
                                       id="dateOfBirth"
                                       name="dateOfBirth"
                                       required
                                       autocomplete="bday"
                                       pattern='dd-MM-yyyy'>
                            </div>
                        </div>
                        <div class="form-group">
                            <label class="form-label" for="address">Địa chỉ</label>
                            <input class="form-input form-input--plain"
                                   type="text"
                                   id="address"
                                   name="address"
                                   placeholder="123 Lê Duẩn, Hà Nội"
                                   required
                                   maxlength="500"
                                   autocomplete="street-address">
                        </div>
                        <div class="form-row-two-col">
                            <div class="form-group form-group--half">
                                <label class="form-label" for="email">Email</label>
                                <input class="form-input form-input--plain"
                                       type="email"
                                       id="email"
                                       name="email"
                                       placeholder="example@gmail.com"
                                       required
                                       autocomplete="email">
                            </div>
                            <div class="form-group form-group--half">
                                <label class="form-label" for="sex">Giới tính</label>
                                <select class="form-input form-input--plain"
                                        id="sex"
                                        name="sex"
                                        required>
                                    <option value="0">Nam</option>
                                    <option value="1">Nữ</option>
                                </select>
                            </div>
                        </div>
                        <div class="form-terms-group">
                            <input type="checkbox" id="terms" name="terms" required>
                            <label for="terms" class="form-terms-text">
                                Tôi đồng ý với Điều khoản và Chính sách bảo mật của Lái Vui.
                            </label>
                        </div>
                        <div class="form-submit-wrap">
                            <button type="submit" class="btn-submit-register">Đăng ký ngay</button>
                            <p class="alternate-auth-prompt">
                                Đã có tài khoản? <a href="${ctx}/login">Đăng nhập</a>
                            </p>
                        </div>
                    </form>
                </div>
            </section>
        </main>
    </body>
</html>

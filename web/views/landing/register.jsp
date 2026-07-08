<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>

<jsp:include page="/views/layout/header.jsp">
    <jsp:param name="title" value="Lái Vui - Đăng ký tài khoản" />
</jsp:include>

<link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/register.css">

<main class="register-page-main">
    <div class="register-card-container">

        <!-- Left side-->
        <div class="register-brand-panel" style="background-image: url('${pageContext.request.contextPath}/assets/imgs/register_bg.png'); background-size: cover; background-position: center;">
            <div class="register-brand-panel__content">
                <div class="register-brand-panel__top">
                    <h2 class="register-brand-panel__tagline">
                        <img src="${pageContext.request.contextPath}/assets/imgs/LOGO.png" alt="Lái Vui" width="32" height="32" style="border-radius: 50%;">
                        Lái Vui
                    </h2>
                    <h1 class="register-brand-panel__heading">
                        Đăng ký thi GPLX<br><span>dễ dàng, tiện lợi</span>
                    </h1>
                    <p class="register-brand-panel__desc">
                        Bạn đăng ký thi GPLX qua cổng dịch vụ đăng ký GPLX Lái Vui, chúng tôi sẽ giúp bạn đơn giản hoá quy trình làm hồ sơ, thủ tục giấy tờ, nhận kết quả nhanh chóng và chính xác!
                    </p>
                </div>

                <div class="register-brand-panel__benefits">
                    <!-- Benefit item 1 -->
                    <div class="benefit-item">
                        <div class="benefit-item__icon-wrap">
                            <span class="material-symbols-outlined" style="font-size:20px">verified</span>
                        </div>
                        <div class="benefit-item__content">
                            <h3 class="benefit-item__title">Uy tín hàng đầu</h3>
                            <p class="benefit-item__subtext">Trung tâm uy tín hàng đầu</p>
                        </div>
                    </div>

                    <!-- Benefit item 2 -->
                    <div class="benefit-item">
                        <div class="benefit-item__icon-wrap">
                            <span class="material-symbols-outlined" style="font-size:20px">calendar_month</span>
                        </div>
                        <div class="benefit-item__content">
                            <h3 class="benefit-item__title">Nhanh chóng, tiện lợi</h3>
                            <p class="benefit-item__subtext">Chúng tôi sẽ hoàn thành các thủ tục hành chính cho bạn</p>
                        </div>
                    </div>
                </div>
            </div>
        </div>

        <!-- Right side -->
        <div class="register-form-panel">
            <div>
                <div class="register-form-panel__header">
                    <h2 class="register-form-panel__title">Tạo tài khoản mới</h2>
                    <p class="register-form-panel__subtitle">Điền thông tin cá nhân - hệ thống sẽ tạo tên đăng nhập, mật khẩu và gửi qua email</p>
                </div>

                <c:if test="${not empty error}">
                    <div style="background-color: #FEF2F2; border: 1px solid #FCA5A5; color: #991B1B; padding: 12px 16px; border-radius: 8px; margin-bottom: 20px; font-family: 'Inter', sans-serif; font-size: 14px; display: flex; align-items: center; gap: 8px;">
                        <span class="material-symbols-outlined" style="font-size:18px">error</span>
                        <c:out value="${error}" />
                    </div>
                </c:if>
                <c:if test="${not empty success}">
                    <div style="background-color: #F0FDF4; border: 1px solid #86EFAC; color: #166534; padding: 12px 16px; border-radius: 8px; margin-bottom: 20px; font-family: 'Inter', sans-serif; font-size: 14px; display: flex; align-items: center; gap: 8px;">
                        <span class="material-symbols-outlined" style="font-size:18px">check_circle</span>
                        <c:out value="${success}" />
                    </div>
                </c:if>

                <!-- Registration Form -->
                <form class="register-form" action="${pageContext.request.contextPath}/register" method="POST">

                    <div class="form-row-two-col">
                        <div class="form-group form-group--half">
                            <label class="form-label" for="govIdNo">Số căn cước</label>
                            <div class="input-wrapper">
                                <input class="form-input" type="text" id="govIdNo" name="govIdNo" placeholder="001203012345" required maxlength="12" pattern="[0-9]{9,12}">
                            </div>
                        </div>
                        <div class="form-group form-group--half">
                            <label class="form-label" for="fullName">Họ & tên</label>
                            <div class="input-wrapper">
                                <input class="form-input" type="text" id="fullName" name="fullName" placeholder="Nguyễn Văn Bình" required maxlength="200">
                            </div>
                        </div>
                    </div>

                    <div class="form-row-two-col">
                        <div class="form-group form-group--half">
                            <label class="form-label" for="phoneNo">Số điện thoại</label>
                            <div class="input-wrapper">
                                <input class="form-input" type="tel" id="phoneNo" name="phoneNo" placeholder="0912345678" required maxlength="20">
                            </div>
                        </div>
                        <div class="form-group form-group--half">
                            <label class="form-label" for="dateOfBirth">Ngày tháng năm sinh</label>
                            <div class="input-wrapper">
                                <input class="form-input" type="date" id="dateOfBirth" name="dateOfBirth" required>
                            </div>
                        </div>
                    </div>

                    <div class="form-group">
                        <label class="form-label" for="address">Địa chỉ</label>
                        <div class="input-wrapper">
                            <input class="form-input" type="text" id="address" name="address" placeholder="123 Lê Duẩn, Hà Nội" required maxlength="500">
                        </div>
                    </div>

                    <div class="form-row-two-col">
                        <div class="form-group form-group--half">
                            <label class="form-label" for="email">Email</label>
                            <div class="input-wrapper">
                                <input class="form-input" type="email" id="email" name="email" placeholder="example@gmail.com" required>
                            </div>
                        </div>
                        <div class="form-group form-group--half">
                            <label class="form-label" for="gender">Giới tính</label>
                            <div class="input-wrapper">
                                <select class="form-input" id="gender" name="gender" required>
                                    <option value="0">Nam</option>
                                    <option value="1">Nữ</option>
                                </select>
                            </div>
                        </div>
                    </div>

                    <!-- Terms and Conditions Checkbox -->
                    <div class="form-terms-group">
                        <input type="checkbox" id="terms" name="terms" required>
                        <label for="terms" class="form-terms-text">
                            Tôi đồng ý với <a href="#">Điều khoản</a> và <a href="#">Chính sách bảo mật</a> của Lái Vui.
                        </label>
                    </div>

                    <!-- Form Submission Area -->
                    <div class="form-submit-wrap">
                        <button type="submit" class="btn-submit-register">Đăng ký ngay</button>
                        <p class="alternate-auth-prompt">
                            Bạn đã có tài khoản? <a href="${pageContext.request.contextPath}/login">Đăng nhập</a>
                        </p>
                    </div>
                </form>
            </div>
        </div>

    </div>
</main>

<jsp:include page="/views/layout/footer.jsp" />

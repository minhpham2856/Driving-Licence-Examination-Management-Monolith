<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>

<jsp:include page="/views/layout/header.jsp">
    <jsp:param name="title" value="Lái Vui - Đăng ký tài khoản" />
</jsp:include>

<!-- Link custom stylesheet for the register page -->
<link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/register.css">

<main class="register-page-main">
    <div class="register-card-container">
        
        <!-- Left Side: Branding / Imagery -->
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
                            <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round">
                                <path d="M12 22s8-4 8-10V5l-8-3-8 3v7c0 6 8 10 8 10z"/>
                                <path d="M9 11l2 2 4-4"/>
                            </svg>
                        </div>
                        <div class="benefit-item__content">
                            <h3 class="benefit-item__title">Uy tín hàng đầu</h3>
                            <p class="benefit-item__subtext">Trung tâm uy tín hàng đầu</p>
                        </div>
                    </div>
                    
                    <!-- Benefit item 2 -->
                    <div class="benefit-item">
                        <div class="benefit-item__icon-wrap">
                            <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round">
                                <rect x="3" y="4" width="18" height="18" rx="2" ry="2"/>
                                <line x1="16" y1="2" x2="16" y2="6"/>
                                <line x1="8" y1="2" x2="8" y2="6"/>
                                <line x1="3" y1="10" x2="21" y2="10"/>
                            </svg>
                        </div>
                        <div class="benefit-item__content">
                            <h3 class="benefit-item__title">Nhanh chóng, tiện lợi</h3>
                            <p class="benefit-item__subtext">Chúng tôi sẽ hoàn thành các thủ tục hành chính cho bạn</p>
                        </div>
                    </div>
                </div>
            </div>
        </div>
        
        <!-- Right Side: Registration Form -->
        <div class="register-form-panel">
            <div>
                <div class="register-form-panel__header">
                    <h2 class="register-form-panel__title">Tạo tài khoản mới</h2>
                    <p class="register-form-panel__subtitle">Vui lòng điền đầy đủ thông tin dưới đây</p>
                </div>
                
                <!-- Server-side response handling via JSTL -->
                <c:if test="${not empty error}">
                    <div style="background-color: #FEF2F2; border: 1px solid #FCA5A5; color: #991B1B; padding: 12px 16px; border-radius: 8px; margin-bottom: 20px; font-family: 'Inter', sans-serif; font-size: 14px; display: flex; align-items: center; gap: 8px;">
                        <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                            <circle cx="12" cy="12" r="10"/>
                            <line x1="12" y1="8" x2="12" y2="12"/>
                            <line x1="12" y1="16" x2="12.01" y2="16"/>
                        </svg>
                        <c:out value="${error}" />
                    </div>
                </c:if>
                <c:if test="${not empty success}">
                    <div style="background-color: #F0FDF4; border: 1px solid #86EFAC; color: #166534; padding: 12px 16px; border-radius: 8px; margin-bottom: 20px; font-family: 'Inter', sans-serif; font-size: 14px; display: flex; align-items: center; gap: 8px;">
                        <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                            <path d="M22 11.08V12a10 10 0 1 1-5.93-9.14"/>
                            <polyline points="22 4 12 14.01 9 11.01"/>
                        </svg>
                        <c:out value="${success}" />
                    </div>
                </c:if>
                
                <!-- Registration Form without Javascript -->
                <form class="register-form" action="${pageContext.request.contextPath}/register" method="POST">
                    
                    <!-- Row for Username & Email -->
                    <div class="form-row-two-col">
                        <div class="form-group form-group--half">
                            <label class="form-label" for="username">Tên đăng nhập</label>
                            <div class="input-wrapper">
                                <input class="form-input" type="text" id="username" name="username" placeholder="exampleUser123" required minlength="3" maxlength="32">
                            </div>
                        </div>
                        
                        <div class="form-group form-group--half">
                            <label class="form-label" for="email">Email</label>
                            <div class="input-wrapper">
                                <input class="form-input" type="email" id="email" name="email" placeholder="example@gmail.com" required>
                            </div>
                        </div>
                    </div>
                    
                    <!-- Password Field -->
                    <div class="form-group">
                        <label class="form-label" for="password">Mật khẩu</label>
                        <div class="input-wrapper">
                            <input class="form-input" type="password" id="password" name="password" placeholder="••••••••" required minlength="6">
                        </div>
                    </div>
                    
                    <!-- Confirm Password Field -->
                    <div class="form-group">
                        <label class="form-label" for="confirmPassword">Nhập lại mật khẩu</label>
                        <div class="input-wrapper">
                            <input class="form-input" type="password" id="confirmPassword" name="confirmPassword" placeholder="••••••••" required minlength="6">
                        </div>
                    </div>
                    
                    <!-- Terms and Conditions Checkbox -->
                    <div class="form-terms-group">
                        <div class="checkbox-custom-wrap">
                            <input type="checkbox" id="terms" name="terms" class="checkbox-native" required>
                            <span class="checkbox-visual"></span>
                        </div>
                        <label for="terms" class="form-terms-text">
                            Tôi đồng ý với <a href="#">Điều khoản</a> và <a href="#">Chính sách bảo mật</a> của Lái Vui.
                        </label>
                    </div>
                    
                    <!-- Form Submission Area -->
                    <div class="form-submit-wrap">
                        <button type="submit" class="btn-submit-register">Đăng ký ngay</button>
                        <p class="alternate-auth-prompt">
                            Bạn đã có tài khoản? <a href="login.jsp">Đăng nhập</a>
                        </p>
                    </div>
                </form>
            </div>
        </div>
        
    </div>
</main>

<jsp:include page="/views/layout/footer.jsp" />

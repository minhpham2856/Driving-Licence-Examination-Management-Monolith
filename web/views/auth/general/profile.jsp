<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>

<jsp:include page="/views/layout/header.jsp">
    <jsp:param name="title" value="Lái Vui - Hồ sơ cá nhân" />
</jsp:include>

<link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/landing/forgot-password.css">

<c:set var="u" value="${requestScope.accountUser}" />
<c:set var="p" value="${requestScope.accountProfile}" />
<c:set var="ctx" value="${pageContext.request.contextPath}" />

<main class="recovery-page-main">
    <div class="recovery-card" style="max-width:720px;">
        <div class="recovery-card__content">
            <div class="recovery-card__header-wrap">
                <h2 class="recovery-card__title">Hồ sơ cá nhân</h2>
                <p class="recovery-card__subtitle">Xem và cập nhật thông tin cá nhân. Tên đăng nhập, email và CCCD không thể sửa tại đây.</p>
            </div>

            <c:if test="${not empty message}">
                <div style="width:100%;padding:12px 16px;border-radius:8px;margin-bottom:12px;
                    background-color:${messageType eq 'success' ? '#F0FDF4' : '#FEF2F2'};
                    border:1px solid ${messageType eq 'success' ? '#86EFAC' : '#FCA5A5'};
                    color:${messageType eq 'success' ? '#166534' : '#991B1B'};">
                    <c:out value="${message}" />
                </div>
            </c:if>

            <div style="width:100%;display:grid;grid-template-columns:repeat(auto-fit,minmax(200px,1fr));gap:12px;margin-bottom:16px;">
                <div>
                    <label style="display:block;margin-bottom:6px;font-weight:600;">Tên đăng nhập</label>
                    <input type="text" class="recovery-card__input" value="<c:out value='${u.username}' />" readonly disabled>
                </div>
                <div>
                    <label style="display:block;margin-bottom:6px;font-weight:600;">Email</label>
                    <input type="text" class="recovery-card__input" value="<c:out value='${u.email}' />" readonly disabled>
                </div>
                <div>
                    <label style="display:block;margin-bottom:6px;font-weight:600;">Vai trò</label>
                    <input type="text" class="recovery-card__input"
                           value="<c:out value='${not empty u.role ? u.role.roleName : \"\"}' />" readonly disabled>
                </div>
            </div>

            <c:choose>
                <c:when test="${empty p}">
                    <p style="width:100%;color:#6B7280;">Chưa có hồ sơ cá nhân gắn với tài khoản. Liên hệ quản trị viên.</p>
                </c:when>
                <c:otherwise>
                    <form method="post" action="${ctx}/profile" style="width:100%;display:grid;grid-template-columns:repeat(auto-fit,minmax(220px,1fr));gap:12px;">
                        <div>
                            <label for="fullName" style="display:block;margin-bottom:6px;font-weight:600;">Họ và tên</label>
                            <input type="text" id="fullName" name="fullName" required class="recovery-card__input"
                                   value="<c:out value='${p.fullName}' />">
                        </div>
                        <div>
                            <label for="dateOfBirth" style="display:block;margin-bottom:6px;font-weight:600;">Ngày sinh</label>
                            <input type="date" id="dateOfBirth" name="dateOfBirth" class="recovery-card__input"
                                   value="<fmt:formatDate value='${p.dateOfBirth}' pattern='yyyy-MM-dd' />">
                        </div>
                        <div>
                            <label for="sex" style="display:block;margin-bottom:6px;font-weight:600;">Giới tính</label>
                            <select id="sex" name="sex" class="recovery-card__input">
                                <option value="1" ${p.sex ? 'selected' : ''}>Nam</option>
                                <option value="0" ${not p.sex ? 'selected' : ''}>Nữ</option>
                            </select>
                        </div>
                        <div>
                            <label for="phoneNumber" style="display:block;margin-bottom:6px;font-weight:600;">Số điện thoại</label>
                            <input type="tel" id="phoneNumber" name="phoneNumber" required pattern="0[0-9]{9,10}"
                                   class="recovery-card__input" value="<c:out value='${p.phoneNumber}' />">
                        </div>
                        <div>
                            <label style="display:block;margin-bottom:6px;font-weight:600;">Số CCCD</label>
                            <input type="text" class="recovery-card__input"
                                   value="<c:out value='${p.governmentIdNumber}' />" readonly disabled>
                        </div>
                        <div style="grid-column:1 / -1;">
                            <label for="address" style="display:block;margin-bottom:6px;font-weight:600;">Địa chỉ</label>
                            <input type="text" id="address" name="address" class="recovery-card__input"
                                   value="<c:out value='${p.address}' />">
                        </div>
                        <div style="grid-column:1 / -1;display:flex;gap:10px;flex-wrap:wrap;">
                            <button type="submit" class="recovery-card__submit-btn">Lưu thay đổi</button>
                            <a href="${ctx}/change-password" class="recovery-card__submit-btn"
                               style="text-align:center;text-decoration:none;background:#374151;">Đổi mật khẩu</a>
                            <c:if test="${not empty backUrl}">
                                <a href="${ctx}${backUrl}" class="recovery-card__submit-btn"
                                   style="text-align:center;text-decoration:none;background:#6B7280;">Quay lại</a>
                            </c:if>
                        </div>
                    </form>
                </c:otherwise>
            </c:choose>
        </div>
    </div>
</main>

<jsp:include page="/views/layout/footer.jsp" />

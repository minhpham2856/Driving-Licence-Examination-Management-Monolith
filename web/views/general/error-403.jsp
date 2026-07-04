<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" isErrorPage="true" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<c:set var="ctx" value="${pageContext.request.contextPath}" />
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>403 - Không có quyền | Lái Vui</title>
    <link rel="preconnect" href="https://fonts.googleapis.com">
    <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
    <link href="https://fonts.googleapis.com/css2?family=Inter:wght@400;600;700;800&display=swap" rel="stylesheet">
    <link rel="stylesheet" href="${ctx}/assets/css/landing/error-pages.css">
</head>
<body class="error-page error-page--403">
    <main class="error-page__stage">
        <p class="error-page__code" aria-hidden="true">403</p>
        <div class="error-page__panel">
            <a class="error-page__brand" href="${ctx}/home">
                <img src="${ctx}/assets/imgs/LOGO.png" alt="Lái Vui" width="36" height="36">
                <span>Lái Vui</span>
            </a>
            <p class="error-page__eyebrow">Truy cập bị từ chối</p>
            <h1 class="error-page__title">Bạn chưa đủ quyền để vào khu vực này.</h1>
            <p class="error-page__desc">Khu vực dành cho nhân sự nội bộ. Đăng nhập đúng tài khoản hoặc quay lại trang công khai.</p>
            <div class="error-page__actions">
                <a class="error-page__btn error-page__btn--primary" href="${ctx}/login">Đăng nhập</a>
                <a class="error-page__btn error-page__btn--ghost" href="${ctx}/home">Về trang chủ</a>
            </div>
        </div>
    </main>
</body>
</html>

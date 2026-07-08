<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>

<c:set var="ctx" value="${pageContext.request.contextPath}" />

<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Đổi mật khẩu - Lái Vui</title>

    <link rel="preconnect" href="https://fonts.googleapis.com">
    <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
    <link href="https://fonts.googleapis.com/css2?family=Be+Vietnam+Pro:wght@400;500;600;700;800&family=Inter:wght@400;500;600;700;800&display=swap" rel="stylesheet">

    <link rel="stylesheet" href="${ctx}/assets/css/style.css">
    <link rel="stylesheet" href="${ctx}/assets/css/layout.css">

    <style>
        .auth-wrap {
            min-height: 100vh; display: flex; align-items: center; justify-content: center;
            background: linear-gradient(135deg, #eef3fb 0%, #dde7f7 100%);
            padding: 2rem 1rem; font-family: 'Be Vietnam Pro', 'Inter', sans-serif;
        }
        .auth-card {
            width: 100%; max-width: 460px; background: #fff; border-radius: 16px;
            box-shadow: 0 18px 50px rgba(15, 23, 42, 0.12); padding: 2.5rem 2.25rem;
            border: 1px solid rgba(0, 82, 204, 0.08);
        }
        .auth-brand { text-align: center; margin-bottom: 1.75rem; }
        .auth-brand h1 { font-size: 1.35rem; font-weight: 800; color: #0f172a; margin: 0; }
        .auth-brand p { font-size: 0.88rem; color: #64748b; margin: 0.35rem 0 0; }
        .auth-field { margin-bottom: 1.1rem; }
        .auth-field label {
            display: block; font-size: 0.85rem; font-weight: 600; color: #334155; margin-bottom: 0.4rem;
        }
        .auth-field input {
            width: 100%; box-sizing: border-box; height: 46px; padding: 0 0.9rem; font-size: 0.95rem;
            border: 1px solid #cbd5e1; border-radius: 9px; font-family: inherit;
            transition: border-color .15s, box-shadow .15s;
        }
        .auth-field input:focus {
            outline: none; border-color: #0052cc; box-shadow: 0 0 0 3px rgba(0, 82, 204, 0.12);
        }
        .auth-btn {
            width: 100%; height: 48px; border: none; border-radius: 9px; background: #0052cc;
            color: #fff; font-size: 0.98rem; font-weight: 700; cursor: pointer; font-family: inherit;
            margin-top: 0.4rem; transition: background .15s;
        }
        .auth-btn:hover { background: #003d9b; }
        .auth-alert {
            font-size: 0.85rem; font-weight: 500; padding: 0.75rem 0.9rem; border-radius: 9px;
            margin-bottom: 1.25rem; display: flex; align-items: center; gap: 8px;
        }
        .auth-alert--danger {
            background: rgba(239, 68, 68, 0.08); border: 1px solid rgba(239, 68, 68, 0.25); color: #b91c1c;
        }
        .auth-alert--success {
            background: rgba(16, 185, 129, 0.08); border: 1px solid rgba(16, 185, 129, 0.25); color: #047857;
        }
        .auth-back {
            margin-top: 1.5rem; text-align: center; font-size: 0.84rem;
        }
        .auth-back a { color: #0052cc; text-decoration: none; font-weight: 600; }
    </style>
</head>
<body>
<div class="auth-wrap">
    <div class="auth-card">
        <div class="auth-brand">
            <h1>Đổi mật khẩu</h1>
            <p>Cập nhật mật khẩu đăng nhập tài khoản của bạn</p>
        </div>

        <c:if test="${not empty message}">
            <div class="auth-alert ${messageType eq 'success' ? 'auth-alert--success' : 'auth-alert--danger'}" role="alert">
                <svg width="16" height="16" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                    <circle cx="12" cy="12" r="10" stroke="currentColor" stroke-width="2"/>
                    <path d="M12 8v4M12 16h.01" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
                </svg>
                ${message}
            </div>
        </c:if>

        <form action="${ctx}/change-password" method="POST">
            <div class="auth-field">
                <label for="currentPassword">Mật khẩu hiện tại</label>
                <input type="password" id="currentPassword" name="currentPassword"
                       placeholder="Nhập mật khẩu hiện tại..." required>
            </div>
            <div class="auth-field">
                <label for="newPassword">Mật khẩu mới</label>
                <input type="password" id="newPassword" name="newPassword"
                       placeholder="Tối thiểu 6 ký tự..." required>
            </div>
            <div class="auth-field">
                <label for="confirmPassword">Xác nhận mật khẩu mới</label>
                <input type="password" id="confirmPassword" name="confirmPassword"
                       placeholder="Nhập lại mật khẩu mới..." required>
            </div>
            <button type="submit" class="auth-btn">Cập nhật mật khẩu</button>
        </form>

        <div class="auth-back">
            <a href="${ctx}/admin/dashboard">&larr; Quay lại Dashboard</a>
        </div>
    </div>
</div>
</body>
</html>

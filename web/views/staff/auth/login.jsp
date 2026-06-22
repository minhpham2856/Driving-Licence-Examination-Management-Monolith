<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<!DOCTYPE html>
<html lang="vi">
    <head>
        <meta charset="UTF-8">
        <meta name="viewport" content="width=device-width, initial-scale=1.0">
        <title>XÁC THỰC</title>
        <link href="https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600&display=swap" rel="stylesheet">
        <link href="https://fonts.googleapis.com/css2?family=Material+Symbols+Outlined" rel="stylesheet" />
        <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/staff-login.css">
    </head>
    <body>
        <div class="staff-login-container">
            <c:if test="${not empty error}">
                <div class="alert alert-danger">
                    ${error}
                </div>
            </c:if>
            <c:if test="${not empty success}">
                <div class="alert alert-success">${success}</div>
            </c:if>

            <form action="${pageContext.request.contextPath}/staff/login" method="POST">
                <div class="form-group">
                    <label for="identifier">Tên đăng nhập / Số căn cước / Email</label>
                    <div class="input-container">
                        <span class="material-symbols-outlined">person</span>
                        <input type="text" id="identifier" name="identifier" required placeholder="Tài khoản">
                    </div>
                </div>

                <div class="form-group">
                    <label for="password">Mật khẩu</label>
                    <div class="input-container">
                        <span class="material-symbols-outlined">lock</span>
                        <input type="password" id="password" name="password" required placeholder="Mật khẩu">
                    </div>
                </div>

                <button type="submit" class="btn-primary">Đăng nhập</button>
            </form>
        </div>
    </body>
</html>

<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="vi">
<head><meta charset="UTF-8"><title>Thông tin thí sinh</title></head>
<body>
<main>
    <h1>Thông tin thí sinh</h1>
    <p>SBD: <strong><c:out value="${candidateExam.candidateNumber}"/></strong></p>
    <p>Họ tên: <strong><c:out value="${candidateExam.fullName}"/></strong></p>
    <form method="get" action="${pageContext.request.contextPath}/exam/questions">
        <button type="submit">Bắt đầu thi</button>
    </form>
</main>
</body>
</html>

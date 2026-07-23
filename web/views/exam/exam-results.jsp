<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="vi">
<head><meta charset="UTF-8"><title>Kết quả thi</title>
<link href="${pageContext.request.contextPath}/assets/css/exam/exam-results.css" rel="stylesheet"></head>
<body>
<main>
    <h1>${result.passed ? 'ĐẠT' : 'KHÔNG ĐẠT'}</h1>
    <p>Đúng: ${result.correct}</p>
    <p>Sai: ${result.wrong}</p>
    <p>Không trả lời: ${result.unanswered}</p>
    <c:if test="${result.criticalFailed}"><p>Không đạt do trả lời sai câu điểm liệt.</p></c:if>
    <p>Vui lòng chờ sát hạch viên in biên bản và hoàn tất phần thi.</p>
</main>
</body>
</html>

<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <title>${docTitle}</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/examiner/print-document.css">
</head>
<body class="print-doc" data-auto-print="${autoPrint ? 'true' : 'false'}">
<div class="print-doc__toolbar no-print">
    <button type="button" class="print-doc__btn print-doc__btn--primary" id="btnPrint">In</button>
    <button type="button" class="print-doc__btn" onclick="window.close()">Đóng</button>
</div>

<div class="print-doc__page print-doc__page--table">
    <h1 class="print-doc__title">${payload.excelSheetName}</h1>
    <p class="print-doc__meta">Thời gian in: <c:out value="${printedAt}"/></p>

    <c:if test="${not empty payload.metadata}">
        <c:forEach var="entry" items="${payload.metadata}">
            <p class="print-doc__meta"><c:out value="${entry.key}"/>: <c:out value="${entry.value}"/></p>
        </c:forEach>
    </c:if>

    <c:forEach var="table" items="${payload.tables}">
        <table class="print-doc__data">
            <thead>
            <tr>
                <c:forEach var="h" items="${table.headers}">
                    <th><c:out value="${h}"/></th>
                </c:forEach>
            </tr>
            </thead>
            <tbody>
            <c:forEach var="row" items="${table.rows}">
                <tr>
                    <c:forEach var="cell" items="${row}">
                        <td><c:out value="${cell}"/></td>
                    </c:forEach>
                </tr>
            </c:forEach>
            </tbody>
        </table>
    </c:forEach>
</div>

<script src="${pageContext.request.contextPath}/assets/js/examiner-print-viewer.js"></script>
</body>
</html>

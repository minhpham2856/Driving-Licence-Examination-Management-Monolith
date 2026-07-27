<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <title>${docTitle}</title>
    <link rel="stylesheet"
          href="${pageContext.request.contextPath}/assets/css/examiner/print-document.css">
</head>
<body class="doc"
      data-auto-print="${autoPrint ? 'true' : 'false'}">
<div class="toolbar no-print">
    <button type="button"
            class="btn blue"
            id="btnPrint">In</button>
    <button type="button"
            class="btn"
            onclick="window.close()">Đóng</button>
</div>

<div class="page page-bb1 page-layout">
    <div class="header">
        <div class="header-col">
            <div class="org">CÔNG AN <c:out value="${bb.DEPT}"/></div>
            <div class="org">HỘI ĐỒNG SÁT HẠCH</div>
        </div>
        <div class="header-col right">
            <div class="org">CỘNG HÒA XÃ HỘI CHỦ NGHĨA VIỆT NAM</div>
            <div class="motto">Độc lập – Tự do – Hạnh phúc</div>
        </div>
    </div>

    <h1 class="title">BIÊN BẢN SÁT HẠCH THỰC HÀNH LÁI XE TRONG HÌNH</h1>

    <div class="info">
        <div class="info-main">
            <div class="row">
                <span>Họ và tên: <strong><c:out value="${bb.FNAME}"/></strong></span>
                <span>Khoá sát hạch: <c:out value="${bb.EXAM}"/></span>
            </div>
            <div class="row">
                <span>Hạng: <c:out value="${bb.CLASS}"/></span>
                <span>Ngày sinh: <c:out value="${bb.DOB}"/></span>
            </div>
            <div class="row">
                <span>Ngày sát hạch: <c:out value="${bb.DATE}"/></span>
                <span>Số báo danh: <c:out value="${bb.CNO}"/></span>
            </div>
            <div class="row">
                <span>Số định danh: <c:out value="${bb.IDNO}"/></span>
                <span>Thời điểm bắt đầu: <c:out value="${bb.START}"/></span>
            </div>
            <div class="row">
                <span>Số xe sát hạch: <c:out value="${bb.VNO}"/></span>
                <span>Lần thi: <c:out value="${bb.TAKENO}"/></span>
            </div>
            <div class="row">
                <span>Thời điểm kết thúc: <c:out value="${bb.END}"/></span>
                <span>Tổng thời gian sát hạch: <c:out value="${bb.TIME}"/></span>
            </div>
        </div>
        <div class="photo">
            <c:choose>
                <%--case 1: has photo--%>
                <c:when test="${not empty bb.PHOTO_URL}">
                    <img src="${bb.PHOTO_URL}" alt="Ảnh thí sinh">
                </c:when>
                <%--case 2: empty--%>
                <c:otherwise>Ảnh 3x4</c:otherwise>
            </c:choose>
        </div>
    </div>

    <table class="practice">
        <thead>
        <tr>
            <th>Số TT</th>
            <th>Các lỗi bị trừ điểm</th>
            <th>Số lần mắc lỗi</th>
            <th>Tổng số điểm trừ</th>
        </tr>
        </thead>
        <tbody>
        <c:choose>
            <%--case 1: no deductions--%>
            <c:when test="${empty bb.deductionRows}">
                <tr>
                    <td colspan="4" class="empty">Không có lỗi bị trừ điểm</td>
                </tr>
            </c:when>
            <%--case 2: has deductions--%>
            <c:otherwise>
                <c:forEach var="row" items="${bb.deductionRows}">
                    <tr>
                        <td><c:out value="${row.stt}"/></td>
                        <td><c:out value="${row.reason}"/></td>
                        <td><c:out value="${row.occurrenceCount}"/></td>
                        <td><c:out value="${row.totalDeducted}"/></td>
                    </tr>
                </c:forEach>
            </c:otherwise>
        </c:choose>
        <tr class="practice-total">
            <td colspan="2">Cộng</td>
            <td><c:out value="${bb.TIMES}"/></td>
            <td><c:out value="${bb.TOTAL}"/></td>
        </tr>
        </tbody>
    </table>

    <p class="ketluan">
        Kết luận: Số điểm đạt được: <strong><c:out value="${bb.SCORE}"/></strong>
        &nbsp;&nbsp; Đạt <strong>[<c:out value="${empty bb.P ? ' ' : bb.P}"/>]</strong>
        &nbsp;&nbsp; Không đạt <strong>[<c:out value="${empty bb.F ? ' ' : bb.F}"/>]</strong>
    </p>

    <div class="signs">
        <div>Sát hạch viên ký xác nhận</div>
        <div>Học viên ký xác nhận kết quả</div>
    </div>
</div>

<script src="${pageContext.request.contextPath}/assets/js/examiner-print-viewer.js"></script>
</body>
</html>

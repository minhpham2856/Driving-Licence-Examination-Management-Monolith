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

<div class="page page-bb1">
    <div class="header">
        <div class="header-col">
            <div class="org">CÔNG AN <c:out value="${printModel.DEPT}"/></div>
            <div class="org org-underline">HỘI ĐỒNG SÁT HẠCH</div>
        </div>
        <div class="header-col">
            <div class="org">CỘNG HÒA XÃ HỘI CHỦ NGHĨA VIỆT NAM</div>
            <div class="motto">Độc lập – Tự do – Hạnh phúc</div>
        </div>
    </div>

    <h1 class="title">BIÊN BẢN SÁT HẠCH LÝ THUYẾT</h1>

    <div class="info">
        <div class="info-main">
            <div class="row">
                <span>Họ và tên: <span class="value"><c:out value="${printModel.FNAME}"/></span></span>
                <span>Khoá sát hạch: <span class="value"><c:out value="${printModel.EXAM}"/></span></span>
            </div>
            <div class="row">
                <span>Ngày sinh: <c:out value="${printModel.DOB}"/></span>
                <span>Ngày sát hạch: <c:out value="${printModel.DATE}"/></span>
            </div>
            <div class="row">
                <span>Số định danh: <c:out value="${printModel.IDNO}"/></span>
                <span>Thời điểm bắt đầu: <c:out value="${printModel.START}"/></span>
            </div>
            <div class="row">
                <span>Hạng: <c:out value="${printModel.CLASS}"/></span>
                <span>Thời điểm kết thúc: <c:out value="${printModel.END}"/></span>
            </div>
            <div class="row">
                <span>Số báo danh: <c:out value="${printModel.CNO}"/></span>
                <span>Lần thi: <c:out value="${printModel.TAKENO}"/></span>
            </div>
        </div>
        <div class="photo">
            <c:choose>
                <%--case 1: has photo--%>
                <c:when test="${not empty printModel.photoImageUrl}">
                    <img src="${pageContext.request.contextPath}/examiner/candidate-photo?sbd=${printModel.CNO}" alt="Ảnh thí sinh">
                </c:when>
                <%--case 2: empty--%>
                <c:otherwise>(ảnh 3x4)</c:otherwise>
            </c:choose>
        </div>
    </div>

    <%--answers q1-12--%>
    <table class="answers">
        <tr>
            <th colspan="2">Câu hỏi</th>
            <c:forEach begin="1" end="12" var="q">
                <th class="qno">${q}</th>
            </c:forEach>
        </tr>
        <c:forEach begin="0" end="3" var="choiceIdx">
            <tr>
                <c:if test="${choiceIdx == 0}">
                    <th rowspan="4" class="traloi">Trả lời</th>
                </c:if>
                <th class="choice">
                    <c:choose>
                        <%--case 1: A--%>
                        <c:when test="${choiceIdx == 0}">A</c:when>
                        <%--case 2: B--%>
                        <c:when test="${choiceIdx == 1}">B</c:when>
                        <%--case 3: C--%>
                        <c:when test="${choiceIdx == 2}">C</c:when>
                        <%--case 4: D--%>
                        <c:otherwise>D</c:otherwise>
                    </c:choose>
                </th>
                <c:forEach var="mark" items="${marksA[choiceIdx]}">
                    <td><c:out value="${mark}"/></td>
                </c:forEach>
            </tr>
        </c:forEach>
    </table>

    <%--answers q13-25--%>
    <table class="answers">
        <tr>
            <th colspan="2">Câu hỏi</th>
            <c:forEach begin="13" end="25" var="q">
                <th class="qno">${q}</th>
            </c:forEach>
        </tr>
        <c:forEach begin="0" end="3" var="choiceIdx">
            <tr>
                <c:if test="${choiceIdx == 0}">
                    <th rowspan="4" class="traloi">Trả lời</th>
                </c:if>
                <th class="choice">
                    <c:choose>
                        <%--case 1: A--%>
                        <c:when test="${choiceIdx == 0}">A</c:when>
                        <%--case 2: B--%>
                        <c:when test="${choiceIdx == 1}">B</c:when>
                        <%--case 3: C--%>
                        <c:when test="${choiceIdx == 2}">C</c:when>
                        <%--case 4: D--%>
                        <c:otherwise>D</c:otherwise>
                    </c:choose>
                </th>
                <c:forEach var="mark" items="${marksB[choiceIdx]}">
                    <td><c:out value="${mark}"/></td>
                </c:forEach>
            </tr>
        </c:forEach>
    </table>

    <p class="ketluan">
        <span class="ketluan-label">Kết luận:</span>
        Số điểm đạt được: <c:out value="${printModel.SCORE}"/>
        &nbsp;&nbsp;&nbsp; Đạt
        <span class="box"><c:out value="${empty printModel.P ? ' ' : printModel.P}"/></span>
        &nbsp;&nbsp;&nbsp; Không đạt
        <span class="box"><c:out value="${empty printModel.F ? ' ' : printModel.F}"/></span>
    </p>

    <div class="signs">
        <div><span class="sign-label">Sát hạch viên ký xác nhận</span></div>
        <div><span class="sign-label">Học viên ký xác nhận kết quả</span></div>
    </div>
</div>

<script src="${pageContext.request.contextPath}/assets/js/examiner-print-viewer.js"></script>
</body>
</html>

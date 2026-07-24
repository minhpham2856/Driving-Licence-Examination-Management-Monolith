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

<div class="print-doc__page print-doc__page--bb1">
    <div class="print-doc__header">
        <div class="print-doc__header-col">
            <div class="print-doc__org">CÔNG AN <c:out value="${bb.DEPT}"/></div>
            <div class="print-doc__org print-doc__org--underline">HỘI ĐỒNG SÁT HẠCH</div>
        </div>
        <div class="print-doc__header-col">
            <div class="print-doc__org">CỘNG HÒA XÃ HỘI CHỦ NGHĨA VIỆT NAM</div>
            <div class="print-doc__motto">Độc lập – Tự do – Hạnh phúc</div>
        </div>
    </div>

    <h1 class="print-doc__title">BIÊN BẢN SÁT HẠCH LÝ THUYẾT</h1>

    <div class="print-doc__info">
        <div class="print-doc__info-main">
            <div class="print-doc__row">
                <span>Họ và tên: <span class="print-doc__value"><c:out value="${bb.FNAME}"/></span></span>
                <span>Khoá sát hạch: <span class="print-doc__value"><c:out value="${bb.EXAM}"/></span></span>
            </div>
            <div class="print-doc__row">
                <span>Ngày sinh: <c:out value="${bb.DOB}"/></span>
                <span>Ngày sát hạch: <c:out value="${bb.DATE}"/></span>
            </div>
            <div class="print-doc__row">
                <span>Số định danh: <c:out value="${bb.IDNO}"/></span>
                <span>Thời điểm bắt đầu: <c:out value="${bb.START}"/></span>
            </div>
            <div class="print-doc__row">
                <span>Hạng: <c:out value="${bb.CLASS}"/></span>
                <span>Thời điểm kết thúc: <c:out value="${bb.END}"/></span>
            </div>
            <div class="print-doc__row">
                <span>Số báo danh: <c:out value="${bb.CNO}"/></span>
                <span>Lần thi: <c:out value="${bb.TAKENO}"/></span>
            </div>
        </div>
        <div class="print-doc__photo">
            <c:choose>
                <c:when test="${not empty bb.PHOTO_URL}">
                    <img src="${bb.PHOTO_URL}" alt="Ảnh thí sinh">
                </c:when>
                <c:otherwise>(ảnh 3x4)</c:otherwise>
            </c:choose>
        </div>
    </div>

    <%-- Bảng 1: câu 1-12 — 5 hàng (1 tiêu đề + 4 lựa chọn) --%>
    <table class="print-doc__answers">
        <tr>
            <th colspan="2">Câu hỏi</th>
            <c:forEach begin="1" end="12" var="q">
                <th class="print-doc__qno">${q}</th>
            </c:forEach>
        </tr>
        <c:forEach begin="0" end="3" var="choiceIdx">
            <tr>
                <c:if test="${choiceIdx == 0}">
                    <th rowspan="4" class="print-doc__traloi">Trả lời</th>
                </c:if>
                <th class="print-doc__choice">
                    <c:choose>
                        <c:when test="${choiceIdx == 0}">A</c:when>
                        <c:when test="${choiceIdx == 1}">B</c:when>
                        <c:when test="${choiceIdx == 2}">C</c:when>
                        <c:otherwise>D</c:otherwise>
                    </c:choose>
                </th>
                <c:forEach var="mark" items="${marksA[choiceIdx]}">
                    <td><c:out value="${mark}"/></td>
                </c:forEach>
            </tr>
        </c:forEach>
    </table>

    <%-- Bảng 2: câu 13-25 — 5 hàng --%>
    <table class="print-doc__answers">
        <tr>
            <th colspan="2">Câu hỏi</th>
            <c:forEach begin="13" end="25" var="q">
                <th class="print-doc__qno">${q}</th>
            </c:forEach>
        </tr>
        <c:forEach begin="0" end="3" var="choiceIdx">
            <tr>
                <c:if test="${choiceIdx == 0}">
                    <th rowspan="4" class="print-doc__traloi">Trả lời</th>
                </c:if>
                <th class="print-doc__choice">
                    <c:choose>
                        <c:when test="${choiceIdx == 0}">A</c:when>
                        <c:when test="${choiceIdx == 1}">B</c:when>
                        <c:when test="${choiceIdx == 2}">C</c:when>
                        <c:otherwise>D</c:otherwise>
                    </c:choose>
                </th>
                <c:forEach var="mark" items="${marksB[choiceIdx]}">
                    <td><c:out value="${mark}"/></td>
                </c:forEach>
            </tr>
        </c:forEach>
    </table>

    <p class="print-doc__ketluan">
        <span class="print-doc__ketluan-label">Kết luận:</span>
        Số điểm đạt được: <c:out value="${bb.SCORE}"/>
        &nbsp;&nbsp;&nbsp; Đạt
        <span class="print-doc__box"><c:out value="${empty bb.P ? ' ' : bb.P}"/></span>
        &nbsp;&nbsp;&nbsp; Không đạt
        <span class="print-doc__box"><c:out value="${empty bb.F ? ' ' : bb.F}"/></span>
    </p>

    <div class="print-doc__signs">
        <div><span class="print-doc__sign-label">Sát hạch viên ký xác nhận</span></div>
        <div><span class="print-doc__sign-label">Học viên ký xác nhận kết quả</span></div>
    </div>
</div>

<script src="${pageContext.request.contextPath}/assets/js/examiner-print-viewer.js"></script>
</body>
</html>

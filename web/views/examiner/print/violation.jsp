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

<div class="print-doc__page print-doc__page--bb1 print-doc__page--violation">
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

    <h1 class="print-doc__title">BIÊN BẢN VI PHẠM QUY CHẾ THI</h1>

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

    <div class="print-doc__violation-body">
        <p><strong>Lý do đình chỉ:</strong> <c:out value="${bb.REASON}"/></p>
        <p><strong>Thời điểm vi phạm:</strong> <c:out value="${bb.TIME}"/></p>
        <p><strong>Chi tiết vi phạm:</strong> <c:out value="${bb.DETAILS}"/></p>
        <div class="print-doc__evidence">
            <strong>Ảnh vi phạm:</strong>
            <c:choose>
                <c:when test="${not empty bb.VIOPIC_URL}">
                    <img src="${bb.VIOPIC_URL}" alt="Ảnh vi phạm">
                </c:when>
                <c:otherwise><span>-</span></c:otherwise>
            </c:choose>
        </div>
    </div>

    <div class="print-doc__signs">
        <div><span class="print-doc__sign-label">Sát hạch viên ký xác nhận</span></div>
        <div><span class="print-doc__sign-label">Học viên ký xác nhận kết quả</span></div>
    </div>
</div>

<script src="${pageContext.request.contextPath}/assets/js/examiner-print-viewer.js"></script>
</body>
</html>

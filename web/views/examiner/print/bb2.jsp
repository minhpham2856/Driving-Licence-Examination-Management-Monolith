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

<div class="print-doc__page print-doc__page--result">
    <div class="print-doc__header">
        <div class="print-doc__header-col">
            <div class="print-doc__org">CÔNG AN <c:out value="${bb.DEPT}"/></div>
            <div class="print-doc__org">HỘI ĐỒNG SÁT HẠCH</div>
        </div>
        <div class="print-doc__header-col print-doc__header-col--right">
            <div class="print-doc__org">CỘNG HÒA XÃ HỘI CHỦ NGHĨA VIỆT NAM</div>
            <div class="print-doc__motto">Độc lập – Tự do – Hạnh phúc</div>
        </div>
    </div>

    <h1 class="print-doc__title">BIÊN BẢN SÁT HẠCH THỰC HÀNH LÁI XE TRONG HÌNH</h1>

    <div class="print-doc__info">
        <div class="print-doc__info-main">
            <div class="print-doc__row">
                <span>Họ và tên: <strong><c:out value="${bb.FNAME}"/></strong></span>
                <span>Khoá sát hạch: <c:out value="${bb.EXAM}"/></span>
            </div>
            <div class="print-doc__row">
                <span>Hạng: <c:out value="${bb.CLASS}"/></span>
                <span>Ngày sinh: <c:out value="${bb.DOB}"/></span>
            </div>
            <div class="print-doc__row">
                <span>Ngày sát hạch: <c:out value="${bb.DATE}"/></span>
                <span>Số báo danh: <c:out value="${bb.CNO}"/></span>
            </div>
            <div class="print-doc__row">
                <span>Số định danh: <c:out value="${bb.IDNO}"/></span>
                <span>Thời điểm bắt đầu: <c:out value="${bb.START}"/></span>
            </div>
            <div class="print-doc__row">
                <span>Số xe sát hạch: <c:out value="${bb.VNO}"/></span>
                <span>Lần thi: <c:out value="${bb.TAKENO}"/></span>
            </div>
            <div class="print-doc__row">
                <span>Thời điểm kết thúc: <c:out value="${bb.END}"/></span>
                <span>Tổng thời gian sát hạch: <c:out value="${bb.TIME}"/></span>
            </div>
        </div>
        <div class="print-doc__photo">
            <c:choose>
                <c:when test="${not empty bb.PHOTO_URL}">
                    <img src="${bb.PHOTO_URL}" alt="Ảnh thí sinh">
                </c:when>
                <c:otherwise>Ảnh 3x4</c:otherwise>
            </c:choose>
        </div>
    </div>

    <table class="print-doc__practice">
        <thead>
        <tr>
            <th>STT</th>
            <th>Bài thi</th>
            <th>Thời điểm</th>
            <th>Chi tiết lỗi</th>
            <th>Điểm trừ</th>
            <th>Vào / Ra bài</th>
        </tr>
        </thead>
        <tbody>
        <tr>
            <td>1</td>
            <td>Đi qua hình số 8</td>
            <td></td>
            <td></td>
            <td><c:out value="${bb.A}"/></td>
            <td></td>
        </tr>
        <tr>
            <td>2</td>
            <td>Đi qua vạch đường thẳng</td>
            <td></td>
            <td></td>
            <td></td>
            <td></td>
        </tr>
        <tr>
            <td>3</td>
            <td>Đi qua đường có vạch cản</td>
            <td></td>
            <td></td>
            <td></td>
            <td></td>
        </tr>
        <tr>
            <td>4</td>
            <td>Đi qua đường gồ ghề</td>
            <td></td>
            <td></td>
            <td></td>
            <td></td>
        </tr>
        <tr>
            <td>5</td>
            <td>Các lỗi khác</td>
            <td></td>
            <td></td>
            <td></td>
            <td></td>
        </tr>
        </tbody>
    </table>

    <p class="print-doc__ketluan">
        Kết luận: Số điểm đạt được: <strong><c:out value="${bb.SCORE}"/></strong>
        &nbsp;&nbsp; Đạt <strong>[<c:out value="${empty bb.P ? ' ' : bb.P}"/>]</strong>
        &nbsp;&nbsp; Không đạt <strong>[<c:out value="${empty bb.F ? ' ' : bb.F}"/>]</strong>
    </p>

    <div class="print-doc__signs">
        <div>Sát hạch viên ký xác nhận</div>
        <div>Học viên ký xác nhận kết quả</div>
    </div>
</div>

<script src="${pageContext.request.contextPath}/assets/js/examiner-print-viewer.js"></script>
</body>
</html>

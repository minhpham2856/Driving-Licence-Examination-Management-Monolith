<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" language="java" %>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>In hồ sơ &mdash; ${profile.sbd}</title>
    <link href="https://fonts.googleapis.com/css2?family=Be+Vietnam+Pro:wght@400;600;700&display=swap" rel="stylesheet">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/dossier-print.css">
</head>
<body class="dossier-page"<c:if test="${autoPrint}"> data-auto-print="true"</c:if>>

<div class="dossier-toolbar no-print">
    <a href="${pageContext.request.contextPath}/views/staff/examstaff/candidatecall" class="dossier-toolbar__btn dossier-toolbar__btn--ghost">&larr; Quay lại gọi thủ tục</a>
    <button type="button" class="dossier-toolbar__btn dossier-toolbar__btn--primary">In hồ sơ</button>
</div>

<article class="dossier-sheet">
    <header class="dossier-header">
        <div class="dossier-header__left">
            <img src="${pageContext.request.contextPath}/assets/imgs/LOGO.png" alt="Logo" class="dossier-logo">
            <div class="dossier-org">TRUNG TÂM SÁT HẠCH LÁI XE</div>
        </div>
        <div class="dossier-header__right">
            <div class="dossier-state">CỘNG HÒA XÃ HỘI CHỦ NGHĨA VIỆT NAM</div>
            <div class="dossier-motto">Độc lập - Tự do - Hạnh phúc</div>
            <div class="dossier-council">BAN TỔ CHỨC SÁT HẠCH</div>
        </div>
    </header>

    <h1 class="dossier-title">${dossierTitle}</h1>
    <p class="dossier-subtitle">${dossierSubtitle}</p>

    <section class="dossier-info">
        <div class="dossier-info__photo">
            <c:choose>
                <c:when test="${hasPhotoFile or not empty profile.photoUrl}">
                    <img id="dossierCandidatePhoto"
                         src="${pageContext.request.contextPath}/views/staff/examstaff/candidate-photo?sbd=${profile.sbd}&amp;t=${profile.id}"
                         alt="Ảnh thí sinh">
                </c:when>
                <c:otherwise>
                    <div class="dossier-photo-placeholder">Ảnh 3&times;4</div>
                </c:otherwise>
            </c:choose>
        </div>
        <div class="dossier-info__fields">
            <div class="dossier-field"><span>Họ và tên thí sinh:</span><strong>${profile.name}</strong></div>
            <div class="dossier-field"><span>Ngày tháng năm sinh:</span><strong><fmt:formatDate value="${profile.dob}" pattern="dd/MM/yyyy"/></strong></div>
            <div class="dossier-field"><span>Số định danh / CCCD:</span><strong>${profile.cccd}</strong></div>
            <div class="dossier-field"><span>Số báo danh (SBD):</span><strong>${profile.sbd}</strong></div>
            <div class="dossier-field"><span>Thi lấy giấy phép lái xe hạng:</span><strong>${profile.clazz}</strong></div>
            <c:if test="${not empty examSession}">
                <div class="dossier-field"><span>Ngày sát hạch:</span><strong><fmt:formatDate value="${examSession.examDate}" pattern="dd/MM/yyyy"/></strong></div>
                <div class="dossier-field"><span>Ca thi:</span><strong>${examSession.sessionName}</strong></div>
            </c:if>
        </div>
    </section>

    <section class="dossier-section">
        <h2 class="dossier-section__title">PHẦN THU PHÍ, LỆ PHÍ ĐÃ NỘP</h2>
        <table class="dossier-table dossier-table--fees">
            <thead>
                <tr>
                    <th>Khoản thu</th>
                    <th style="width: 140px; text-align: right;">Số tiền (đồng)</th>
                </tr>
            </thead>
            <tbody>
                <c:forEach var="feeLine" items="${feeLines}">
                    <tr>
                        <td>${feeLine.feeName}</td>
                        <td style="text-align: right;"><fmt:formatNumber value="${feeLine.amount}" pattern="#,##0"/></td>
                    </tr>
                </c:forEach>
                <tr class="dossier-total-row">
                    <td><strong>TỔNG CỘNG</strong></td>
                    <td style="text-align: right;"><strong><fmt:formatNumber value="${feeTotal}" pattern="#,##0"/></strong></td>
                </tr>
            </tbody>
        </table>
        <div class="dossier-meta">
            <c:if test="${feesFromPayment}">
                <span style="font-style: italic;">Khoản thu theo biên lai thanh toán đã ghi nhận.</span>
            </c:if>
            <span>Hình thức thanh toán:
                <c:choose>
                    <c:when test="${not empty payment}">${payment.paymentMethod}</c:when>
                    <c:otherwise>Tiền mặt</c:otherwise>
                </c:choose>
            </span>
            <c:if test="${not empty payment and not empty payment.transactionReference}">
                <span>Mã giao dịch: ${payment.transactionReference}</span>
            </c:if>
            <c:if test="${not empty payment and not empty payment.paymentDate}">
                <span>Thời gian thu: <fmt:formatDate value="${payment.paymentDate}" pattern="dd/MM/yyyy HH:mm"/></span>
            </c:if>
        </div>
    </section>

    <section class="dossier-commitment">
        <p>Tôi xác nhận các thông tin trên phiếu (họ tên, ngày sinh, số CCCD, hạng thi, ảnh chân dung) là chính xác và trùng khớp với giấy tờ tùy thân. Tôi xác nhận đã nộp đủ các khoản phí, lệ phí nêu trên theo quy định của Trung tâm.</p>
        <p>Tôi đồng ý để Trung tâm sử dụng thông tin và ảnh chân dung này cho mục đích tổ chức thi, lưu hồ sơ và in các giấy tờ liên quan sau kỳ thi.</p>
    </section>

    <section class="dossier-signature">
        <p class="dossier-date-line">..........., ngày ..... tháng ..... năm 20.....</p>
        <div class="dossier-signature__box">
            <p class="dossier-signature__title"><strong>THÍ SINH</strong></p>
            <p class="dossier-signature__hint">(Ký, ghi rõ họ tên)</p>
            <div class="dossier-signature__space"></div>
        </div>
    </section>
</article>

<script src="${pageContext.request.contextPath}/assets/js/dossier-print.js" charset="UTF-8"></script>
</body>
</html>

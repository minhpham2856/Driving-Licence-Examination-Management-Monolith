<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" language="java" %>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Báo cáo ca thi &mdash; ${currentSession.sessionName}</title>
    <link href="https://fonts.googleapis.com/css2?family=Be+Vietnam+Pro:wght@400;600;700&display=swap" rel="stylesheet">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/report-print.css">
</head>
<body class="report-print-page" data-auto-print="${autoPrint ? 'true' : 'false'}">

<div class="report-print-toolbar no-print">
    <a href="${pageContext.request.contextPath}/views/staff/examstaff/report" class="report-print-btn report-print-btn--ghost">&larr; Quay lại báo cáo</a>
    <button type="button" class="report-print-btn report-print-btn--primary">In / Lưu PDF</button>
</div>

<article class="report-print-sheet">
    <header class="report-print-header">
        <div class="report-print-header__left">
            <img src="${pageContext.request.contextPath}/assets/imgs/LOGO.png" alt="Logo" class="report-print-logo">
            <div class="report-print-org">TRUNG TÂM SÁT HẠCH LÁI XE</div>
        </div>
        <div class="report-print-header__right">
            <div class="report-print-state">CỘNG HÒA XÃ HỘI CHỦ NGHĨA VIỆT NAM</div>
            <div class="report-print-motto">Độc lập - Tự do - Hạnh phúc</div>
            <div class="report-print-council">BAN TỔ CHỨC SÁT HẠCH</div>
        </div>
    </header>

    <h1 class="report-print-title">BIÊN BẢN TỔNG HỢP KẾT QUẢ CA THI</h1>
    <p class="report-print-subtitle">
        Ca: <strong>${currentSession.sessionName}</strong>
        &mdash; Ngày <fmt:formatDate value="${currentSession.examDate}" pattern="dd/MM/yyyy"/>
    </p>

    <section class="report-print-section">
        <h2 class="report-print-section__title">I. Tổng quan</h2>
        <table class="report-print-kpi">
            <tbody>
                <tr>
                    <td>Tổng đăng ký</td><td><strong>${totalCandidates}</strong></td>
                    <td>Đã thi xong</td><td><strong>${examCompletedCount}</strong></td>
                </tr>
                <tr>
                    <td>Đạt</td><td class="text-pass"><strong>${passedCount}</strong></td>
                    <td>Trượt</td><td class="text-fail"><strong>${failedCount}</strong></td>
                </tr>
                <tr>
                    <td>Vắng</td><td><strong>${absentCount}</strong></td>
                    <td>Đình chỉ</td><td><strong>${suspendedCount}</strong></td>
                </tr>
                <tr>
                    <td>Tỷ lệ đạt</td><td><strong><fmt:formatNumber value="${passRate}" maxFractionDigits="1"/>%</strong></td>
                    <td></td><td></td>
                </tr>
            </tbody>
        </table>
    </section>

    <section class="report-print-section">
        <h2 class="report-print-section__title">II. Theo phần thi</h2>
        <table class="report-print-table">
            <thead>
                <tr>
                    <th>Phần thi</th>
                    <th>Tổng</th>
                    <th>Đạt</th>
                    <th>Bị loại</th>
                    <th>Tỷ lệ loại</th>
                </tr>
            </thead>
            <tbody>
                <tr>
                    <td>Lý thuyết</td>
                    <td>${theoryCount}</td>
                    <td>${theoryPassed}</td>
                    <td>${theoryFailed}</td>
                    <td><c:if test="${theoryCount > 0}"><fmt:formatNumber value="${theoryFailed * 100.0 / theoryCount}" maxFractionDigits="1"/>%</c:if><c:if test="${theoryCount == 0}">0%</c:if></td>
                </tr>
                <tr>
                    <td>Sa hình / Thực hành</td>
                    <td>${practicalCount}</td>
                    <td>${practicalPassed}</td>
                    <td>${practicalFailed}</td>
                    <td><c:if test="${practicalCount > 0}"><fmt:formatNumber value="${practicalFailed * 100.0 / practicalCount}" maxFractionDigits="1"/>%</c:if><c:if test="${practicalCount == 0}">0%</c:if></td>
                </tr>
            </tbody>
        </table>
    </section>

    <section class="report-print-section report-print-section--landscape-hint">
        <h2 class="report-print-section__title">III. Danh sách kết quả thí sinh</h2>
        <table class="report-print-table report-print-table--candidates">
            <thead>
                <tr>
                    <th>STT</th>
                    <th>SBD</th>
                    <th>Họ và tên</th>
                    <th>Hạng</th>
                    <th>LT</th>
                    <th>SH</th>
                    <th>KQ</th>
                </tr>
            </thead>
            <tbody>
                <c:forEach var="c" items="${candidateList}" varStatus="st">
                    <tr>
                        <td>${st.index + 1}</td>
                        <td>${c.sbd}</td>
                        <td>${c.name}</td>
                        <td>${c.clazz}</td>
                        <td>
                            <c:choose>
                                <c:when test="${c.skipsTheory}">Bảo lưu</c:when>
                                <c:when test="${c.theoryPassed eq 'passed'}">Đạt</c:when>
                                <c:when test="${c.theoryPassed eq 'failed'}">Trượt</c:when>
                                <c:otherwise>&mdash;</c:otherwise>
                            </c:choose>
                        </td>
                        <td>
                            <c:choose>
                                <c:when test="${c.skipsPractical}">Bảo lưu</c:when>
                                <c:when test="${c.practicalPassed eq 'passed'}">Đạt</c:when>
                                <c:when test="${c.practicalPassed eq 'failed'}">Trượt</c:when>
                                <c:otherwise>&mdash;</c:otherwise>
                            </c:choose>
                        </td>
                        <td>
                            <c:choose>
                                <c:when test="${c.suspended}">Đình chỉ</c:when>
                                <c:when test="${c.absent}">Vắng</c:when>
                                <c:when test="${c.examFinished and c.finalPass}">Đạt</c:when>
                                <c:when test="${c.examFinished}">Trượt</c:when>
                                <c:otherwise>Chưa xong</c:otherwise>
                            </c:choose>
                        </td>
                    </tr>
                </c:forEach>
            </tbody>
        </table>
    </section>

    <section class="report-print-signature">
        <p class="report-print-date">..........., ngày ..... tháng ..... năm 20.....</p>
        <div class="report-print-signature__grid">
            <div>
                <p><strong>TRƯỞNG BAN TỔ CHỨC</strong></p>
                <p class="hint">(Ký, ghi rõ họ tên, đóng dấu)</p>
            </div>
            <div>
                <p><strong>THƯ KÝ CA THI</strong></p>
                <p class="hint">(Ký, ghi rõ họ tên)</p>
            </div>
            <div>
                <p><strong>NGƯỜI LẬP BIÊN BẢN</strong></p>
                <p class="hint">(Ký, ghi rõ họ tên)</p>
            </div>
        </div>
    </section>
</article>

<script src="${pageContext.request.contextPath}/assets/js/report-print.js" charset="UTF-8"></script>
</body>
</html>

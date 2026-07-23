<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<c:set var="ctx" value="${pageContext.request.contextPath}" />
<c:set var="headerTitle" value="Thao tác" />
<c:set var="pageUrl" value="${ctx}/examiner/action" scope="request" />
<c:set var="detailViewUrl" value="${ctx}/examiner/candidate-details" scope="request" />
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Thao tác sát hạch</title>
    <link href="https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700&display=swap" rel="stylesheet">
    <link href="https://fonts.googleapis.com/css2?family=Material+Symbols+Outlined" rel="stylesheet">
    <jsp:include page="/views/examiner/components/examiner-styles.jsp">
        <jsp:param name="pageCss" value="score-entry.css" />
    </jsp:include>
</head>
<body class="has-side-nav-bar examiner-portal"
      data-context-path="${ctx}/examiner/">
<jsp:include page="/views/layout/sidebar-examiner.jsp">
    <jsp:param name="activeSidebar" value="action" />
</jsp:include>
<div class="examiner-shell">
    <jsp:include page="/views/layout/header-examiner.jsp" />
    <main class="examiner-main examiner-main--dashboard">
        <jsp:include page="/views/examiner/components/examiner-messages.jsp" />
        <jsp:include page="/views/examiner/components/toolbar.jsp">
            <jsp:param name="btnSearch" value="right" />
            <jsp:param name="searchWide" value="true" />
            <jsp:param name="searchPlaceholder" value="Tìm SBD, tên, số căn cước..." />
            <jsp:param name="btnRefresh" value="right" />
        </jsp:include>

        <c:if test="${examinerSectionTheory}">
            <section class="examiner-card examiner-otp-card" aria-live="polite">
                <h2>OTP vào thi</h2>
                <strong id="examOtpCode">------</strong>
                <span id="examOtpCountdown">--s</span>
            </section>
        </c:if>

        <section class="examiner-card">
            <div class="examiner-card__head"><h2>Hàng đợi khu vực thi</h2></div>
            <div class="examiner-table-wrap">
                <table class="examiner-table">
                    <thead>
                    <tr>
                        <th>SBD</th><th>Họ tên</th><th>Trạng thái</th><th>Điểm</th>
                        <th>Điểm danh</th><th>Gọi / bỏ qua</th><th>Đình chỉ</th>
                        <th>Nhập điểm</th><th>In</th><th>Hoàn tất</th>
                    </tr>
                    </thead>
                    <tbody>
                    <c:choose>
                        <c:when test="${empty candidateQueue}">
                            <tr><td colspan="10" class="examiner-table__empty">Hàng đợi hiện đang trống.</td></tr>
                        </c:when>
                        <c:otherwise>
                            <c:forEach var="c" items="${candidateQueue}">
                                <tr>
                                    <td>${c.candidateNumber}</td>
                                    <td><a href="${ctx}/examiner/candidate-details?sbd=${c.candidateNumber}&amp;from=action">${c.fullName}</a></td>
                                    <td>${c.statusLabel}</td>
                                    <td>${empty c.examScore ? '-' : c.examScore}</td>
                                    <td>
                                        <c:choose>
                                            <c:when test="${c.markPresentEligible}">
                                                <form method="post" action="${pageUrl}">
                                                    <input type="hidden" name="action" value="markPresent">
                                                    <input type="hidden" name="sbd" value="${c.candidateNumber}">
                                                    <button class="examiner-btn examiner-btn--orange examiner-btn--compact">Điểm danh</button>
                                                </form>
                                            </c:when>
                                            <c:otherwise><span class="examiner-btn examiner-btn--disabled examiner-btn--compact">Đã điểm danh</span></c:otherwise>
                                        </c:choose>
                                    </td>
                                    <td>
                                        <form method="post" action="${pageUrl}" class="js-call-candidate"
                                              data-sbd="${c.candidateNumber}" data-name="${c.fullName}">
                                            <input type="hidden" name="action" value="call">
                                            <input type="hidden" name="sbd" value="${c.candidateNumber}">
                                            <button class="examiner-btn examiner-btn--primary examiner-btn--compact">Gọi</button>
                                        </form>
                                        <form method="post" action="${pageUrl}">
                                            <input type="hidden" name="action" value="defer">
                                            <input type="hidden" name="sbd" value="${c.candidateNumber}">
                                            <button class="examiner-btn examiner-btn--white examiner-btn--compact">Bỏ qua</button>
                                        </form>
                                    </td>
                                    <td>
                                        <a href="${ctx}/examiner/violations?sbd=${c.candidateNumber}&amp;mode=create"
                                           class="examiner-btn examiner-btn--danger examiner-btn--compact">Đình chỉ</a>
                                    </td>
                                    <td>
                                        <c:choose>
                                            <c:when test="${not examinerSectionTheory and c.practicalEntryAllowed and not c.suspended}">
                                                <a href="${pageUrl}?sbd=${c.candidateNumber}"
                                                   class="examiner-btn examiner-btn--orange examiner-btn--compact">Nhập điểm</a>
                                            </c:when>
                                            <c:otherwise><span class="examiner-btn examiner-btn--disabled examiner-btn--compact">Nhập điểm</span></c:otherwise>
                                        </c:choose>
                                    </td>
                                    <td>
                                        <c:choose>
                                            <c:when test="${c.awaitingSignature}">
                                                <form method="post" action="${pageUrl}" target="_blank">
                                                    <input type="hidden" name="action" value="printResult">
                                                    <input type="hidden" name="sbd" value="${c.candidateNumber}">
                                                    <button class="examiner-btn examiner-btn--white examiner-btn--compact">In</button>
                                                </form>
                                            </c:when>
                                            <c:otherwise><span class="examiner-btn examiner-btn--disabled examiner-btn--compact">In</span></c:otherwise>
                                        </c:choose>
                                    </td>
                                    <td>
                                        <c:choose>
                                            <c:when test="${c.completeEligible}">
                                                <form method="post" action="${pageUrl}">
                                                    <input type="hidden" name="action" value="completeSection">
                                                    <input type="hidden" name="sbd" value="${c.candidateNumber}">
                                                    <button class="examiner-btn examiner-btn--success examiner-btn--compact">Hoàn tất</button>
                                                </form>
                                            </c:when>
                                            <c:otherwise><span class="examiner-btn examiner-btn--disabled examiner-btn--compact">Hoàn tất</span></c:otherwise>
                                        </c:choose>
                                    </td>
                                </tr>
                            </c:forEach>
                        </c:otherwise>
                    </c:choose>
                    </tbody>
                </table>
            </div>
        </section>

        <c:if test="${scoreModalOpen and not examinerSectionTheory}">
            <div class="score-modal is-open" id="scoreModal" role="dialog" aria-modal="true"
                 data-draft-key="exam-score:${activeExamId}:${examSectionName}:${examAreaId}:${candidate.candidateNumber}">
                <div class="score-modal__backdrop"></div>
                <section class="score-modal__panel">
                    <header class="score-modal__head">
                        <h2>Nhập điểm SBD ${candidate.candidateNumber} - ${candidate.fullName}</h2>
                        <a href="${pageUrl}" class="examiner-btn examiner-btn--white">Đóng</a>
                    </header>
                    <form method="post" action="${pageUrl}" id="practicalScoreForm">
                        <input type="hidden" name="action" value="savePracticalScore">
                        <input type="hidden" name="sbd" value="${candidate.candidateNumber}">
                        <input type="hidden" name="submissionToken" value="${scoreSubmissionToken}">
                        <input type="hidden" name="elapsedSeconds" id="elapsedSeconds" value="0">
                        <label for="deviceId">Chọn xe thi</label>
                        <select id="deviceId" name="deviceId" required>
                            <option value="">-- Chọn xe --</option>
                            <c:forEach var="vehicle" items="${examVehicles}">
                                <option value="${vehicle.id}" ${candidateVehicleId == vehicle.id ? 'selected' : ''}>${vehicle.name}</option>
                            </c:forEach>
                        </select>
                        <div class="score-entry-timer">
                            <strong id="examTimer">00:00:00</strong>
                            <button type="button" id="timerStartBtn">Bắt đầu / Tạm dừng</button>
                            <button type="button" id="timerResetBtn">Đặt lại</button>
                        </div>
                        <p>Điểm: <strong id="currentScore">${scoreCurrent}</strong>/100</p>
                        <jsp:include page="/views/examiner/components/faults.jsp">
                            <jsp:param name="deferredAdjust" value="true" />
                        </jsp:include>
                        <button type="submit" class="examiner-btn examiner-btn--primary">Lưu điểm</button>
                    </form>
                </section>
            </div>
        </c:if>
    </main>
</div>
<script src="${ctx}/assets/js/examiner-action.js"></script>
</body>
</html>

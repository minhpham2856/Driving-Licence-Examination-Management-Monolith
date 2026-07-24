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
    <link href="https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700&family=JetBrains+Mono:wght@600;700;800&display=swap" rel="stylesheet">
    <link href="https://fonts.googleapis.com/css2?family=Material+Symbols+Outlined" rel="stylesheet">
    <jsp:include page="/views/examiner/components/examiner-styles.jsp">
        <jsp:param name="pageCss" value="action.css" />
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

        <section class="examiner-card examiner-action-card">
            <div class="examiner-card__head"><h2>Danh sách thí sinh</h2></div>
            <div class="examiner-table-wrap">
                <table class="examiner-table">
                    <thead>
                    <tr>
                        <th>SBD</th><th>Họ tên</th><th>Trạng thái</th><th>Điểm</th>
                        <th>Điểm danh</th><th>Gọi</th><th>Đình chỉ</th>
                        <th>Nhập điểm</th><th>In</th><th>Hoàn tất</th>
                    </tr>
                    </thead>
                    <tbody>
                    <c:choose>
                        <c:when test="${empty candidates}">
                            <tr><td colspan="10" class="examiner-table__empty">Chưa có thí sinh trong kỳ thi/phần thi này.</td></tr>
                        </c:when>
                        <c:otherwise>
                            <c:forEach var="c" items="${candidates}">
                                <tr>
                                    <td>${c.candidateNumber}</td>
                                    <td><a class="examiner-table-link" href="${ctx}/examiner/candidate-details?sbd=${c.candidateNumber}&amp;from=action">${c.fullName}</a></td>
                                    <td>
                                        <c:choose>
                                            <c:when test="${c.status == 'suspended'}"><span class="examiner-tag examiner-tag--suspended">${c.statusLabel}</span></c:when>
                                            <c:when test="${c.status == 'done'}"><span class="examiner-tag examiner-tag--done">${c.statusLabel}</span></c:when>
                                            <c:when test="${c.status == 'awaiting'}"><span class="examiner-tag examiner-tag--awaiting">${c.statusLabel}</span></c:when>
                                            <c:when test="${c.status == 'testing'}"><span class="examiner-tag examiner-tag--testing">${c.statusLabel}</span></c:when>
                                            <c:otherwise><span class="examiner-tag examiner-tag--pending">${c.statusLabel}</span></c:otherwise>
                                        </c:choose>
                                    </td>
                                    <td>${empty c.examScore ? '-' : c.examScore}</td>
                                    <c:choose>
                                        <c:when test="${not c.sectionRequired or not c.practicalEntryAllowed}">
                                            <td colspan="6">
                                                <span class="examiner-text-muted">-</span>
                                            </td>
                                        </c:when>
                                        <c:otherwise>
                                            <td>
                                                <c:choose>
                                                    <c:when test="${c.markPresentEligible}">
                                                        <form method="post" action="${pageUrl}">
                                                            <input type="hidden" name="action" value="markPresent">
                                                            <input type="hidden" name="sbd" value="${c.candidateNumber}">
                                                            <button class="examiner-btn examiner-btn--orange examiner-btn--compact">Điểm danh</button>
                                                        </form>
                                                    </c:when>
                                                    <c:when test="${c.present}">
                                                        <span class="examiner-btn examiner-btn--disabled examiner-btn--compact">Đã điểm danh</span>
                                                    </c:when>
                                                    <c:otherwise>
                                                        <span class="examiner-btn examiner-btn--disabled examiner-btn--compact">Điểm danh</span>
                                                    </c:otherwise>
                                                </c:choose>
                                            </td>
                                            <td>
                                                <c:choose>
                                                    <c:when test="${c.actionEligible}">
                                                        <form method="post" action="${pageUrl}" class="js-call-candidate"
                                                              data-sbd="${c.candidateNumber}" data-name="${c.fullName}">
                                                            <input type="hidden" name="action" value="call">
                                                            <input type="hidden" name="sbd" value="${c.candidateNumber}">
                                                            <button class="examiner-btn examiner-btn--primary examiner-btn--compact">Gọi</button>
                                                        </form>
                                                    </c:when>
                                                    <c:otherwise><span class="examiner-btn examiner-btn--disabled examiner-btn--compact">Gọi</span></c:otherwise>
                                                </c:choose>
                                            </td>
                                            <td>
                                                <c:choose>
                                                    <c:when test="${c.violationEligible}">
                                                        <a href="${ctx}/examiner/violations?sbd=${c.candidateNumber}&amp;mode=create&amp;from=action"
                                                           class="examiner-btn examiner-btn--danger examiner-btn--compact">Đình chỉ</a>
                                                    </c:when>
                                                    <c:otherwise><span class="examiner-btn examiner-btn--disabled examiner-btn--compact">Đình chỉ</span></c:otherwise>
                                                </c:choose>
                                            </td>
                                            <td>
                                                <c:choose>
                                                    <c:when test="${c.scoreEntryEligible and not examinerSectionTheory}">
                                                        <a href="${ctx}/examiner/score-entry?sbd=${c.candidateNumber}&amp;from=action"
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
                                        </c:otherwise>
                                    </c:choose>
                                </tr>
                            </c:forEach>
                        </c:otherwise>
                    </c:choose>
                    </tbody>
                </table>
            </div>
        </section>

    </main>
</div>
<script src="${ctx}/assets/js/examiner-action.js"></script>
</body>
</html>

<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>

<!--variables-->
<c:set var="ctx" value="${pageContext.request.contextPath}" />
<c:set var="headerTitle" value="Nhập điểm" />
<c:set var="pageUrl" value="${ctx}/views/examiner/score-entry" scope="request" />
<c:set var="callUrl" value="${ctx}/views/examiner/candidate-call" />
<c:set var="confirmUrl" value="${ctx}/views/examiner/confirmation" />
<c:set var="exportResultsUrl" value="${ctx}/examiner/export/results" />
<c:set var="exportDocxUrl" value="${ctx}/examiner/export/docx?type=results" scope="request" />
<c:set var="baseScore" value="100" />

<!--page-->
<!DOCTYPE html>
<html lang="vi">
    <head>
        <meta charset="UTF-8">
        <meta name="viewport" content="width=device-width, initial-scale=1.0">
        <title>SÁT HẠCH</title>
        <link rel="preconnect" href="https://fonts.googleapis.com">
        <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
        <link href="https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700&family=JetBrains+Mono:wght@500;600;700&display=swap" rel="stylesheet">
        <link href="https://fonts.googleapis.com/css2?family=Material+Symbols+Outlined:opsz,wght,FILL,GRAD@24,400,0,0&display=swap" rel="stylesheet">
        <jsp:include page="/views/examiner/components/examiner-styles.jsp">
            <jsp:param name="pageCss" value="score-entry.css,devices.css" />
        </jsp:include>
    </head>
    <body class="has-side-nav-bar examiner-portal${empty examinerHasActiveSession or not examinerHasActiveSession ? ' examiner-portal--inactive' : ''}">

        <!--sidebar-->
        <jsp:include page="/views/examiner/components/sidebar.jsp">
            <jsp:param name="activeSidebar" value="score-entry" />
        </jsp:include>

        <!--shell-->
        <div class="examiner-shell">

            <!--header-->
            <jsp:include page="/views/examiner/components/header.jsp" />

            <!--main content-->
            <main class="examiner-main examiner-main--score-entry">

                <!--action message-->
                <jsp:include page="/views/examiner/components/examiner-messages.jsp" />

                <!--toolbar-->
                <jsp:include page="/views/examiner/components/toolbar.jsp">
                    <jsp:param name="wrapperClass" value="score-entry-toolbar" />
                    <jsp:param name="leftClass" value="score-entry-toolbar__left" />
                    <jsp:param name="rightClass" value="score-entry-toolbar__right" />
                    <jsp:param name="btnVehicle" value="left" />
                    <jsp:param name="btnViolation" value="left" />
                    <jsp:param name="btnPrintSignature" value="left" />
                    <jsp:param name="btnComplete" value="left" />
                    <jsp:param name="btnPrintDocs" value="right" />
                    <jsp:param name="btnExportDocx" value="right" />
                    <jsp:param name="btnRefresh" value="right" />
                </jsp:include>

                <!--score entry-->
                <div class="score-entry-grid">

                    <!--se.left-->
                    <div class="score-entry-col score-entry-col--main">

                        <!--queue-->
                        <jsp:include page="/views/examiner/components/candidate-list.jsp">
                            <jsp:param name="title" value="Danh sách thí sinh" />
                            <jsp:param name="cardClass" value="score-entry-card score-entry-card--queue" />
                            <jsp:param name="headerClass" value="score-entry-card__head" />
                            <jsp:param name="titleClass" value="score-entry-card__title" />
                            <jsp:param name="badgeClass" value="score-entry-badge score-entry-badge--pending" />
                            <jsp:param name="itemsAttr" value="scoreQueue" />
                            <jsp:param name="isQueueRow" value="true" />
                            <jsp:param name="showVehicle" value="true" />
                            <jsp:param name="showDob" value="false" />
                            <jsp:param name="showAddress" value="false" />
                            <jsp:param name="actionCallScore" value="true" />
                        </jsp:include>

                        <!--timer-->
                        <section class="score-entry-timer-card">
                            <div class="score-entry-timer">
                                <p class="score-entry-timer__label">THỜI GIAN THI</p>
                                <p class="score-entry-timer__value" id="examTimer">20:00:00</p>
                            </div>
                            <button type="button" class="examiner-btn examiner-btn--success score-entry-timer__btn" id="timerStartBtn">
                                <span class="material-symbols-outlined">play_arrow</span>Bắt đầu
                            </button>
                        </section>

                        <!--final score-->
                        <section class="score-entry-score-card">
                            <h3 class="score-entry-score-card__title">Điểm</h3>
                            <div class="score-entry-score-display">
                                <c:choose>
                                    <c:when test="${scoreDisqualified}">
                                        <span class="score-entry-score-value score-entry-score-value--fail">TRƯỢT</span>
                                    </c:when>

                                    <c:otherwise>
                                        <span class="score-entry-score-value" id="currentScore">
                                            <fmt:formatNumber value="${currentScore}" pattern="#"/>
                                        </span>
                                        <span class="score-entry-score-max" id="scoreMaxLabel">/ 100</span>
                                    </c:otherwise>
                                </c:choose>
                            </div>
                            <c:if test="${not empty candidate}">
                                <form method="post" action="${pageUrl}" class="score-entry-finalize-form">
                                    <input type="hidden" name="action" value="finalize">
                                    <input type="hidden" name="sbd" value="${candidate.sbd}">
                                    <button type="submit" class="examiner-btn examiner-btn--primary score-entry-finalize-btn">
                                        Hoàn tất nhập điểm
                                    </button>
                                </form>
                            </c:if>
                        </section>
                    </div>

                    <!--se.right-->
                    <aside class="score-entry-col score-entry-col--penalties">

                        <!--faults-->
                        <jsp:include page="/views/examiner/components/faults.jsp" />

                        <!--vehicles-->
                        <c:if test="${not empty sessionVehicles}">
                            <jsp:include page="/views/examiner/components/device-grid.jsp">
                                <jsp:param name="title" value="Xe thi" />
                                <jsp:param name="cardClass" value="score-entry-card score-entry-card--vehicles" />
                                <jsp:param name="headerClass" value="score-entry-card__head" />
                                <jsp:param name="titleClass" value="score-entry-card__title" />
                                <jsp:param name="badgeClass" value="score-entry-badge score-entry-badge--pending" />
                                <jsp:param name="hasBody" value="false" />
                                <jsp:param name="badgeText" value="${fn:length(sessionVehicles)} xe" />
                                <jsp:param name="pageUrl" value="${pageUrl}" />
                                <jsp:param name="itemsAttr" value="sessionVehicles" />
                                <jsp:param name="compact" value="true" />
                                <jsp:param name="showArea" value="false" />
                                <jsp:param name="returnSbd" value="${candidate.sbd}" />
                            </jsp:include>
                        </c:if>
                    </aside>
                </div>
            </main>
        </div>
        <script>
            (function () {
                var timerEl = document.getElementById('examTimer');
                var startBtn = document.getElementById('timerStartBtn');
                var remaining = 20 * 60;
                var intervalId = null;
                function formatTime(seconds) {
                    var h = Math.floor(seconds / 3600);
                    var m = Math.floor((seconds % 3600) / 60);
                    var s = seconds % 60;
                    return String(h).padStart(2, '0') + ':' +
                            String(m).padStart(2, '0') + ':' +
                            String(s).padStart(2, '0');
                }
                if (timerEl) {
                    timerEl.textContent = formatTime(remaining);
                }
                if (startBtn) {
                    startBtn.addEventListener('click', function () {
                        if (intervalId) {
                            clearInterval(intervalId);
                            intervalId = null;
                            startBtn.innerHTML = '<span class="material-symbols-outlined">play_arrow</span>Bắt đầu';
                            return;
                        }
                        intervalId = setInterval(function () {
                            if (remaining <= 0) {
                                clearInterval(intervalId);
                                intervalId = null;
                                return;
                            }
                            remaining -= 1;
                            timerEl.textContent = formatTime(remaining);
                        }, 1000);
                        startBtn.innerHTML = '<span class="material-symbols-outlined">pause</span>Tạm dừng';
                    });
                }
            })();
        </script>

    </body>
</html>

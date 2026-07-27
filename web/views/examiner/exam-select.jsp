<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>

<%--context variable--%>
<c:set var="ctx" value="${pageContext.request.contextPath}" />

<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Chọn kỳ thi</title>
    <link rel="preconnect" href="https://fonts.googleapis.com">
    <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
    <link href="https://fonts.googleapis.com/css2?family=Hanken+Grotesk:wght@400;600;700&display=swap" rel="stylesheet">
    <link href="https://fonts.googleapis.com/css2?family=Material+Symbols+Outlined:opsz,wght,FILL,GRAD@24,400,0,0&display=swap" rel="stylesheet">
    <link rel="stylesheet" href="${ctx}/assets/css/examiner/exam-select.css">
</head>
<body class="session">

<header class="session-header">
    <div class="session-header-inner">
        <div class="session-brand">
            <span class="material-symbols-outlined session-brand-icon">assignment</span>
            <h1 class="session-title">Chọn kỳ thi</h1>
        </div>
        <div class="session-header-actions">
            <a class="session-header-btn" href="${ctx}/examiner/exam">
                <span class="material-symbols-outlined">refresh</span>
                Làm mới
            </a>
            <a class="session-header-btn" href="${ctx}/staff/logout">
                <span class="material-symbols-outlined">logout</span>
                Đăng xuất
            </a>
        </div>
    </div>
</header>

<main class="session-main">
    <%--case 1: exam not active--%>
    <c:if test="${param.error == 'notActive'}">
        <p class="session-alert">
            Kỳ thi chưa mở, đang tạm dừng hoặc đã kết thúc. Vui lòng chọn kỳ thi khác.
        </p>
    </c:if>

    <%--case 2: exam paused--%>
    <c:if test="${param.error == 'paused'}">
        <p class="session-alert">
            Kỳ thi đang tạm dừng. Vui lòng chờ cán bộ kỳ thi tiếp tục.
        </p>
    </c:if>

    <%--case 3: access denied / invalid--%>
    <c:if test="${param.error == 'denied' || param.error == 'invalid'}">
        <p class="session-alert">Không thể vào kỳ thi.</p>
    </c:if>

    <c:choose>
        <%--case 1: no schedules--%>
        <c:when test="${empty schedules}">
            <div class="session-empty">
                <span class="material-symbols-outlined">event_busy</span>
                <p>Chưa có kỳ thi.</p>
            </div>
        </c:when>

        <%--case 2: schedule cards--%>
        <c:otherwise>
            <div class="session-list">
                <c:forEach var="schedule" items="${schedules}">
                    <%--exam card fields--%>
                    <c:set var="exam" value="${schedule.exam}" />
                    <%--true when exam is in progress--%>
                    <c:set var="enterable" value="${exam.status eq 'Đang diễn ra'}" />
                    <%--licence class for this exam--%>
                    <c:set var="licence" value="" />
                    <c:if test="${not empty exam}">
                        <c:set var="licence" value="${licencesByExamId[exam.examId]}" />
                    </c:if>
                    <c:set var="sectionType" value="${schedule.examSection.sectionType}" />
                    <%--area name, fallback to centre--%>
                    <c:set var="locationName" value="${schedule.examArea.areaName}" />
                    <c:if test="${empty locationName}">
                        <c:set var="locationName" value="${exam.centreName}" />
                    </c:if>

                    <article class="exam-card${enterable ? ' exam-card-active' : ''}">
                        <div class="exam-card-content">
                            <div class="exam-card-top">
                                <span class="exam-card-dot${enterable ? ' live' : ''}"
                                      aria-hidden="true"></span>
                                <span class="exam-card-code">
                                    <c:out value="${exam.examCode}" default="" />
                                </span>
                                <c:choose>
                                    <%--case 1: theory badge--%>
                                    <c:when test="${sectionType eq 'Lý thuyết'}">
                                        <span class="exam-card-badge exam-card-badge-theory">
                                            <c:out value="${sectionType}" default="" />
                                        </span>
                                    </c:when>

                                    <%--case 2: layout badge--%>
                                    <c:otherwise>
                                        <span class="exam-card-badge exam-card-badge-layout">
                                            <c:out value="${sectionType}" default="" />
                                        </span>
                                    </c:otherwise>
                                </c:choose>
                                <span class="exam-card-badge ${enterable ? 'live' : 'idle'}">
                                    <c:out value="${exam.status}" default="" />
                                </span>
                            </div>
                            <div class="exam-card-fields">
                                <div class="exam-card-field">
                                    <span class="exam-card-label">Ngày thi</span>
                                    <span class="exam-card-value">
                                        <span class="material-symbols-outlined">calendar_today</span>
                                        <c:choose>
                                            <%--case 1: has exam date--%>
                                            <c:when test="${not empty exam.examDate}">
                                                <fmt:formatDate value="${exam.examDate}" pattern="dd/MM/yyyy" />
                                            </c:when>

                                            <%--case 2: empty date--%>
                                            <c:otherwise></c:otherwise>
                                        </c:choose>
                                    </span>
                                </div>
                                <div class="exam-card-field">
                                    <span class="exam-card-label">Giờ thi</span>
                                    <span class="exam-card-value">
                                        <span class="material-symbols-outlined">schedule</span>
                                        <c:choose>
                                            <%--case 1: has start time--%>
                                            <c:when test="${not empty exam.startTime}">
                                                <fmt:formatDate value="${exam.startTime}" pattern="HH:mm" />
                                            </c:when>

                                            <%--case 2: empty time--%>
                                            <c:otherwise></c:otherwise>
                                        </c:choose>
                                    </span>
                                </div>
                                <div class="exam-card-field">
                                    <span class="exam-card-label">Nội dung sát hạch</span>
                                    <span class="exam-card-value">
                                        <span class="material-symbols-outlined">description</span>
                                        <c:out value="${sectionType}" default="" />
                                        <c:if test="${not empty licence.licenceClass}">
                                            (<c:out value="${licence.licenceClass}" />)
                                        </c:if>
                                    </span>
                                </div>
                                <div class="exam-card-field">
                                    <span class="exam-card-label">Địa điểm</span>
                                    <span class="exam-card-value">
                                        <span class="material-symbols-outlined">location_on</span>
                                        <c:out value="${locationName}" default="" />
                                    </span>
                                </div>
                            </div>
                        </div>
                        <div class="exam-card-action">
                            <c:choose>
                                <%--case 1: can enter exam--%>
                                <c:when test="${enterable}">
                                    <form method="post" action="${ctx}/examiner/exam">
                                        <input type="hidden" name="examId" value="${exam.examId}" />
                                        <button type="submit" class="exam-card-enter">
                                            Vào kỳ thi
                                        </button>
                                    </form>
                                </c:when>

                                <%--case 2: enter disabled--%>
                                <c:otherwise>
                                    <button type="button"
                                            class="exam-card-enter grey-out"
                                            disabled>
                                        Vào kỳ thi
                                    </button>
                                </c:otherwise>
                            </c:choose>
                        </div>
                    </article>
                </c:forEach>
            </div>
        </c:otherwise>
    </c:choose>
</main>

</body>
</html>

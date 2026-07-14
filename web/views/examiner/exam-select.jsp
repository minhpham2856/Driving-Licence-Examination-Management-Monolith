<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>

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
<body class="examiner-session-select">

<header class="examiner-session-select__header">
    <div class="examiner-session-select__header-inner">
        <div class="examiner-session-select__brand">
            <span class="material-symbols-outlined examiner-session-select__brand-icon">assignment</span>
            <h1 class="examiner-session-select__title">Chọn kỳ thi</h1>
        </div>
        <div class="examiner-session-select__header-actions">
            <a class="examiner-session-select__header-btn" href="${ctx}/views/examiner/exam">
                <span class="material-symbols-outlined">refresh</span>
                Làm mới
            </a>
            <a class="examiner-session-select__header-btn" href="${ctx}/staff/logout">
                <span class="material-symbols-outlined">logout</span>
                Đăng xuất
            </a>
        </div>
    </div>
</header>

<main class="examiner-session-select__main">
    <c:if test="${param.error == 'notActive'}">
        <p class="examiner-session-select__alert">Kỳ thi chưa mở, đang tạm dừng hoặc đã kết thúc. Vui lòng chọn kỳ thi khác.</p>
    </c:if>
    <c:if test="${param.error == 'paused'}">
        <p class="examiner-session-select__alert">Kỳ thi đang tạm dừng. Vui lòng chờ cán bộ kỳ thi tiếp tục.</p>
    </c:if>
    <c:if test="${param.error == 'denied' || param.error == 'invalid'}">
        <p class="examiner-session-select__alert">Không thể vào kỳ thi.</p>
    </c:if>

    <c:choose>
        <c:when test="${empty schedules}">
            <div class="examiner-session-select__empty">
                <span class="material-symbols-outlined">event_busy</span>
                <p>Chưa có kỳ thi.</p>
            </div>
        </c:when>
        <c:otherwise>
            <div class="examiner-session-select__list">
                <c:forEach var="schedule" items="${schedules}">
                    <c:set var="exam" value="${schedule.exam}" />
                    <c:set var="enterable" value="${exam.status eq 'Đang diễn ra'}" />
                    <c:set var="licence" value="" />
                    <c:if test="${not empty exam}">
                        <c:set var="licence" value="${licencesByExamId[exam.examId]}" />
                    </c:if>
                    <c:set var="sectionType" value="${schedule.examSection.sectionType}" />
                    <c:set var="sectionName" value="${schedule.examSection.sectionName}" />
                    <c:set var="locationName" value="${schedule.examArea.areaName}" />
                    <c:if test="${empty locationName}">
                        <c:set var="locationName" value="${exam.centreName}" />
                    </c:if>

                    <article class="examiner-exam-card${enterable ? ' examiner-exam-card--active' : ''}">
                        <div class="examiner-exam-card__content">
                            <div class="examiner-exam-card__top">
                                <span class="examiner-exam-card__dot${enterable ? ' examiner-exam-card__dot--live' : ''}" aria-hidden="true"></span>
                                <span class="examiner-exam-card__code">
                                    <c:out value="${exam.examCode}" default="-" />
                                </span>
                                <c:choose>
                                    <c:when test="${sectionType eq 'Lý thuyết'}">
                                        <span class="examiner-exam-card__badge examiner-exam-card__badge--theory">
                                            <c:out value="${sectionType}" default="-" />
                                        </span>
                                    </c:when>
                                    <c:otherwise>
                                        <span class="examiner-exam-card__badge examiner-exam-card__badge--layout">
                                            <c:out value="${sectionType}" default="-" />
                                        </span>
                                    </c:otherwise>
                                </c:choose>
                                <span class="examiner-exam-card__badge examiner-exam-card__badge--${enterable ? 'live' : 'idle'}">
                                    <c:out value="${exam.status}" default="-" />
                                </span>
                            </div>
                            <div class="examiner-exam-card__fields">
                                <div class="examiner-exam-card__field">
                                    <span class="examiner-exam-card__label">Ngày thi</span>
                                    <span class="examiner-exam-card__value">
                                        <span class="material-symbols-outlined">calendar_today</span>
                                        <c:choose>
                                            <c:when test="${not empty exam.examDate}">
                                                <fmt:formatDate value="${exam.examDate}" pattern="dd/MM/yyyy" />
                                            </c:when>
                                            <c:otherwise>-</c:otherwise>
                                        </c:choose>
                                    </span>
                                </div>
                                <div class="examiner-exam-card__field">
                                    <span class="examiner-exam-card__label">Giờ thi</span>
                                    <span class="examiner-exam-card__value">
                                        <span class="material-symbols-outlined">schedule</span>
                                        <c:choose>
                                            <c:when test="${not empty exam.startTime}">
                                                <fmt:formatDate value="${exam.startTime}" pattern="HH:mm" />
                                            </c:when>
                                            <c:otherwise>-</c:otherwise>
                                        </c:choose>
                                    </span>
                                </div>
                                <div class="examiner-exam-card__field">
                                    <span class="examiner-exam-card__label">Nội dung sát hạch</span>
                                    <span class="examiner-exam-card__value">
                                        <span class="material-symbols-outlined">description</span>
                                        <c:out value="${sectionName}" default="-" />
                                        <c:if test="${not empty licence.licenceClass}">
                                            (<c:out value="${licence.licenceClass}" />)
                                        </c:if>
                                    </span>
                                </div>
                                <div class="examiner-exam-card__field">
                                    <span class="examiner-exam-card__label">Địa điểm</span>
                                    <span class="examiner-exam-card__value">
                                        <span class="material-symbols-outlined">location_on</span>
                                        <c:out value="${locationName}" default="-" />
                                    </span>
                                </div>
                            </div>
                        </div>
                        <div class="examiner-exam-card__action">
                            <c:choose>
                                <c:when test="${enterable}">
                                    <form method="post" action="${ctx}/views/examiner/exam">
                                        <input type="hidden" name="examId" value="${exam.examId}" />
                                        <button type="submit" class="examiner-exam-card__enter">Vào kỳ thi</button>
                                    </form>
                                </c:when>
                                <c:otherwise>
                                    <button type="button" class="examiner-exam-card__enter examiner-exam-card__enter--disabled" disabled>
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

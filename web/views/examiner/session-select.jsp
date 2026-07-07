<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>

<c:set var="ctx" value="${pageContext.request.contextPath}" />

<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Chọn ca thi</title>
    <link rel="preconnect" href="https://fonts.googleapis.com">
    <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
    <link href="https://fonts.googleapis.com/css2?family=Hanken+Grotesk:wght@400;600;700&display=swap" rel="stylesheet">
    <link href="https://fonts.googleapis.com/css2?family=Material+Symbols+Outlined:opsz,wght,FILL,GRAD@24,400,0,0&display=swap" rel="stylesheet">
    <link rel="stylesheet" href="${ctx}/assets/css/examiner/session-select.css">
</head>
<body class="examiner-session-select">

<header class="examiner-session-select__header">
    <div class="examiner-session-select__header-inner">
        <div class="examiner-session-select__brand">
            <span class="material-symbols-outlined examiner-session-select__brand-icon">assignment</span>
            <h1 class="examiner-session-select__title">Chọn ca thi</h1>
        </div>
        <a class="examiner-session-select__logout" href="${ctx}/staff/logout">
            <span class="material-symbols-outlined">logout</span>
            Đăng xuất
        </a>
    </div>
</header>

<main class="examiner-session-select__main">
    <c:if test="${param.error == 'notActive'}">
        <p class="examiner-session-select__alert">Ca thi đã kết thúc. Vui lòng chọn ca thi khác.</p>
    </c:if>
    <c:if test="${param.error == 'denied' || param.error == 'invalid'}">
        <p class="examiner-session-select__alert">Không thể vào ca thi.</p>
    </c:if>

    <c:choose>
        <c:when test="${empty schedules}">
            <div class="examiner-session-select__empty">
                <span class="material-symbols-outlined">event_busy</span>
                <p>Chưa có ca thi.</p>
            </div>
        </c:when>
        <c:otherwise>
            <div class="examiner-session-select__list">
                <c:forEach var="schedule" items="${schedules}">
                    <c:set var="session" value="${schedule.session}" />
                    <c:set var="enterable" value="${session.status eq 'Đang diễn ra'}" />
                    <c:set var="exam" value="${session.exam}" />
                    <c:set var="licence" value="" />
                    <c:if test="${not empty exam}">
                        <c:set var="licence" value="${licencesByExamId[exam.examId]}" />
                    </c:if>
                    <c:set var="sectionName" value="${schedule.examSection.sectionName}" />
                    <c:set var="locationName" value="${schedule.examArea.areaName}" />
                    <c:if test="${empty locationName}">
                        <c:set var="locationName" value="${exam.centreName}" />
                    </c:if>

                    <article class="examiner-session-card${enterable ? ' examiner-session-card--active' : ''}">
                        <div class="examiner-session-card__content">
                            <div class="examiner-session-card__top">
                                <span class="examiner-session-card__dot${enterable ? ' examiner-session-card__dot--live' : ''}" aria-hidden="true"></span>
                                <span class="examiner-session-card__code">
                                    <c:out value="${exam.examCode}" default="-" />
                                </span>
                                <span class="examiner-session-card__badge examiner-session-card__badge--${enterable ? 'live' : 'idle'}">
                                    <c:out value="${session.status}" default="-" />
                                </span>
                            </div>
                            <div class="examiner-session-card__fields">
                                <div class="examiner-session-card__field">
                                    <span class="examiner-session-card__label">Ngày thi</span>
                                    <span class="examiner-session-card__value">
                                        <span class="material-symbols-outlined">calendar_today</span>
                                        <c:choose>
                                            <c:when test="${not empty session.startTime}">
                                                <fmt:formatDate value="${session.startTime}" pattern="dd/MM/yyyy" />
                                            </c:when>
                                            <c:otherwise>-</c:otherwise>
                                        </c:choose>
                                    </span>
                                </div>
                                <div class="examiner-session-card__field">
                                    <span class="examiner-session-card__label">Ca thi</span>
                                    <span class="examiner-session-card__value">
                                        <span class="material-symbols-outlined">schedule</span>
                                        <c:choose>
                                            <c:when test="${session.morningSession}">Ca sáng</c:when>
                                            <c:otherwise>Ca chiều</c:otherwise>
                                        </c:choose>
                                    </span>
                                </div>
                                <div class="examiner-session-card__field">
                                    <span class="examiner-session-card__label">Nội dung sát hạch</span>
                                    <span class="examiner-session-card__value">
                                        <span class="material-symbols-outlined">description</span>
                                        <c:out value="${sectionName}" default="-" />
                                        <c:if test="${not empty licence.licenceClass}">
                                            (<c:out value="${licence.licenceClass}" />)
                                        </c:if>
                                    </span>
                                </div>
                                <div class="examiner-session-card__field">
                                    <span class="examiner-session-card__label">Địa điểm</span>
                                    <span class="examiner-session-card__value">
                                        <span class="material-symbols-outlined">location_on</span>
                                        <c:out value="${locationName}" default="-" />
                                    </span>
                                </div>
                            </div>
                        </div>
                        <div class="examiner-session-card__action">
                            <c:choose>
                                <c:when test="${enterable}">
                                    <form method="post" action="${ctx}/views/examiner/session">
                                        <input type="hidden" name="examinerScheduleId" value="${schedule.examinerScheduleId}" />
                                        <button type="submit" class="examiner-session-card__enter">Vào ca thi</button>
                                    </form>
                                </c:when>
                                <c:otherwise>
                                    <button type="button" class="examiner-session-card__enter examiner-session-card__enter--disabled" disabled>
                                        Vào ca thi
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

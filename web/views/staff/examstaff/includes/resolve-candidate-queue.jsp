<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>

<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<c:set var="queueUrlExamId" value="${param.examId}" />
<c:if test="${empty queueUrlExamId}">
    <c:set var="queueUrlExamId" value="${requestScope.selectedExamId}" />
</c:if>
<c:set var="queueExamStale" value="false" />
<c:if test="${not empty queueUrlExamId and not empty sessionScope.examStaffLoadedExamId}">
    <c:set var="queueExamStale" value="${queueUrlExamId != sessionScope.examStaffLoadedExamId}" />
</c:if>

<c:choose>

    <c:when test="${requestScope.examStaffLoadedExamId != null}">

        <c:set var="candidateQueue" value="${requestScope.candidateQueue}" scope="request" />

        <c:set var="activeCallQueue" value="${requestScope.activeCallQueue != null ? requestScope.activeCallQueue : requestScope.candidateQueue}" scope="request" />

        <c:if test="${requestScope.procedureDoneCandidates != null}">

            <c:set var="procedureDoneCandidates" value="${requestScope.procedureDoneCandidates}" scope="request" />

        </c:if>

    </c:when>

    <c:when test="${requestScope.activeCallQueue != null}">

        <c:set var="candidateQueue" value="${requestScope.candidateQueue != null ? requestScope.candidateQueue : sessionScope.candidateQueue}" scope="request" />

        <c:set var="activeCallQueue" value="${requestScope.activeCallQueue}" scope="request" />

        <c:if test="${requestScope.procedureDoneCandidates != null}">

            <c:set var="procedureDoneCandidates" value="${requestScope.procedureDoneCandidates}" scope="request" />

        </c:if>

    </c:when>

    <c:when test="${sessionScope.examStaffLoadedExamId != null and not queueExamStale}">

        <c:set var="candidateQueue" value="${sessionScope.candidateQueue}" scope="request" />

        <c:set var="activeCallQueue" value="${sessionScope.activeCallQueue != null ? sessionScope.activeCallQueue : sessionScope.candidateQueue}" scope="request" />

        <c:if test="${sessionScope.procedureDoneCandidates != null}">

            <c:set var="procedureDoneCandidates" value="${sessionScope.procedureDoneCandidates}" scope="request" />

        </c:if>

    </c:when>

    <c:otherwise>

        <c:set var="candidateQueue" value="${sessionScope.candidateQueue}" scope="request" />

        <c:set var="activeCallQueue" value="${sessionScope.activeCallQueue != null ? sessionScope.activeCallQueue : sessionScope.candidateQueue}" scope="request" />

        <c:if test="${sessionScope.procedureDoneCandidates != null}">

            <c:set var="procedureDoneCandidates" value="${sessionScope.procedureDoneCandidates}" scope="request" />

        </c:if>

    </c:otherwise>

</c:choose>

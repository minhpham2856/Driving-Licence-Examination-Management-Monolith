<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<c:set var="c" value="${candidateRow}" />

<tr<c:if test="${rowAlt}"> class="examiner-table__row--alt"</c:if>>
    <td class="examiner-table__name">${c.fullName}</td>
    <td class="examiner-table__mono">${c.sbd}</td>
    <td>${c.dob}</td>
    <td>${c.address}</td>
    <td class="examiner-table__mono">${c.governmentId}</td>
    <c:if test="${examinerSectionTheory}">
        <td class="examiner-text-green examiner-table__mono-md">${c.correct}</td>
        <td class="examiner-text-red examiner-table__mono-md">${c.wrong}</td>
        <td class="examiner-table__mono-md">${c.unanswered}</td>
    </c:if>
    <c:if test="${not examinerSectionTheory}">
        <td class="examiner-table__mono-md">${c.examScore}</td>
    </c:if>
    <td>
        <c:choose>
            <c:when test="${c.passed}"><span class="examiner-tag examiner-tag--pass">${c.resultLabel}</span></c:when>
            <c:when test="${c.resultLabel != '—'}"><span class="examiner-tag examiner-tag--fail">${c.resultLabel}</span></c:when>
            <c:otherwise>—</c:otherwise>
        </c:choose>
    </td>
    <td>
        <c:choose>
            <c:when test="${c.status == 'done'}"><span class="examiner-tag examiner-tag--done">${c.statusLabel}</span></c:when>
            <c:when test="${c.status == 'awaiting'}"><span class="examiner-tag examiner-tag--awaiting">${c.statusLabel}</span></c:when>
            <c:when test="${c.status == 'testing'}"><span class="examiner-tag examiner-tag--testing">${c.statusLabel}</span></c:when>
            <c:when test="${c.status == 'absent'}"><span class="examiner-tag examiner-tag--fail">${c.statusLabel}</span></c:when>
            <c:when test="${c.status == 'suspended'}"><span class="examiner-tag examiner-tag--suspended">${c.statusLabel}</span></c:when>
            <c:otherwise><span class="examiner-tag examiner-tag--pending">${c.statusLabel}</span></c:otherwise>
        </c:choose>
    </td>
</tr>

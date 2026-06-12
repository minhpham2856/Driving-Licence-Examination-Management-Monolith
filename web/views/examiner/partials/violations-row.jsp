<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<c:set var="c" value="${candidateRow}" />

<tr<c:if test="${rowAlt}"> class="examiner-table__row--alt"</c:if>>
    <td class="examiner-table__name">${c.fullName}</td>
    <td class="examiner-table__mono-md">${c.sbd}</td>
    <td class="examiner-table__mono-md">${c.dob}</td>
    <td>${c.address}</td>
    <td class="examiner-table__mono-md">${c.governmentId}</td>
    <td>
        <c:choose>
            <c:when test="${c.status == 'done'}"><span class="examiner-tag examiner-tag--done">${c.statusLabel}</span></c:when>
            <c:when test="${c.status == 'testing'}"><span class="examiner-tag examiner-tag--testing">${c.statusLabel}</span></c:when>
            <c:when test="${c.status == 'absent'}"><span class="examiner-tag examiner-tag--fail">${c.statusLabel}</span></c:when>
            <c:when test="${c.status == 'suspended'}"><span class="examiner-tag examiner-tag--suspended">${c.statusLabel}</span></c:when>
            <c:otherwise><span class="examiner-tag examiner-tag--pending">${c.statusLabel}</span></c:otherwise>
        </c:choose>
    </td>
    <td>
        <c:choose>
            <c:when test="${c.suspended}">
                <a href="${undoUrl}?sbd=${c.sbd}" class="examiner-btn examiner-btn--white examiner-btn--compact">Hoàn tác</a>
            </c:when>
            <c:otherwise>
                <a href="${confirmUrl}?sbd=${c.sbd}" class="examiner-btn examiner-btn--danger examiner-btn--compact">Đình chỉ</a>
            </c:otherwise>
        </c:choose>
    </td>
</tr>

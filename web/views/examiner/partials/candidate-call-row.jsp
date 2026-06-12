<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<c:set var="c" value="${candidateRow}" />

<tr<c:if test="${rowAlt}"> class="examiner-table__row--alt"</c:if>>
    <td>
        <input type="checkbox" class="examiner-check" name="sbd" value="${c.sbd}" form="callSelectedForm" ${c.callEligible ? '' : 'disabled'}>
    </td>
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
            <c:when test="${c.absent}">
                <a href="${pageUrl}?action=undoAbsent&amp;sbd=${c.sbd}" class="examiner-btn examiner-btn--white examiner-btn--compact">Hoàn tác</a>
            </c:when>
            <c:otherwise>
                <c:choose>
                    <c:when test="${c.callEligible}">
                        <a href="${pageUrl}?action=markAbsent&amp;sbd=${c.sbd}" class="examiner-btn examiner-btn--danger examiner-btn--compact">Vắng</a>
                    </c:when>
                    <c:otherwise>
                        <span class="examiner-btn examiner-btn--white examiner-btn--compact examiner-btn--disabled">Vắng</span>
                    </c:otherwise>
                </c:choose>
            </c:otherwise>
        </c:choose>
    </td>
    <td>
        <c:choose>
            <c:when test="${c.suspended}">
                <a href="${violationUndoUrl}?sbd=${c.sbd}" class="examiner-btn examiner-btn--white examiner-btn--compact">Hoàn tác</a>
            </c:when>
            <c:otherwise>
                <a href="${violationConfirmUrl}?sbd=${c.sbd}" class="examiner-btn examiner-btn--danger examiner-btn--compact">Đình chỉ</a>
            </c:otherwise>
        </c:choose>
    </td>
    <td>
        <div class="examiner-actions">
            <c:choose>
                <c:when test="${c.callEligible}">
                    <a href="${pageUrl}?action=call&amp;sbd=${c.sbd}" class="examiner-btn examiner-btn--primary examiner-btn--compact">Gọi</a>
                </c:when>
                <c:otherwise>
                    <span class="examiner-btn examiner-btn--white examiner-btn--compact examiner-btn--disabled">Gọi</span>
                </c:otherwise>
            </c:choose>
            <a href="${detailViewUrl}?sbd=${c.sbd}" class="examiner-btn examiner-btn--white examiner-btn--compact">Chi tiết</a>
            <a href="${detailEditUrl}?sbd=${c.sbd}" class="examiner-btn examiner-btn--white examiner-btn--compact">Sửa TT</a>
            <c:choose>
                <c:when test="${examinerSectionTheory}">
                    <span class="examiner-btn examiner-btn--white examiner-btn--compact examiner-btn--disabled">Sửa KQ</span>
                </c:when>
                <c:otherwise>
                    <a href="${resultUrl}?sbd=${c.sbd}" class="examiner-btn examiner-btn--white examiner-btn--compact">Sửa KQ</a>
                </c:otherwise>
            </c:choose>
        </div>
    </td>
</tr>

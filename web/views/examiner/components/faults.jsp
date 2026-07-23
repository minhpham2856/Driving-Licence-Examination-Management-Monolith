<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>

<c:set var="faultSbd" value="${not empty requestScope.candidate ? requestScope.candidate.candidateNumber : param.sbd}" />
<c:set var="faultPageUrl" value="${not empty requestScope.pageUrl ? requestScope.pageUrl : pageContext.request.contextPath.concat('/examiner/score-entry')}" />
<c:set var="deferredAdjust" value="${param.deferredAdjust == 'true'}" />

<section class="score-entry-card score-entry-card--penalties">
    <!--header-->
    <div class="score-entry-card__head">
        <div class="score-entry-card__title">
            <span class="material-symbols-outlined">warning</span>
            <h2>Danh sách lỗi</h2>
        </div>
    </div>

    <!--fault list-->
    <div class="score-entry-penalty-wrap">
        <table class="score-entry-penalty-table">
            <thead>
                <tr>
                    <th>Lỗi</th>
                    <th>Trừ</th>
                    <th>Lần</th>
                    <th>Thời gian</th>
                    <th>Thao tác</th>
                </tr>
            </thead>
            <tbody>
                <c:choose>
                    <c:when test="${empty requestScope.scoreDeductions}">
                        <tr>
                            <td colspan="5" class="score-entry-table__empty">Chưa có dữ liệu.</td>
                        </tr>
                    </c:when>
                    <c:otherwise>
                        <c:forEach var="deduction" items="${requestScope.scoreDeductions}">
                            <tr class="${deduction.critical ? 'score-entry-penalty-row--critical' : ''}"
                                data-deduction-id="${deduction.id}"
                                data-critical="${deduction.critical}"
                                data-points="${deduction.points}"
                                data-base-count="${deduction.occurrenceCount}">
                                <td>
                                    <span class="score-entry-penalty-reason">${deduction.reason}</span>
                                </td>
                                <td class="score-entry-penalty-points">
                                    <c:choose>
                                        <c:when test="${deduction.critical}">TRƯỢT</c:when>
                                        <c:otherwise>
                                            <fmt:formatNumber value="${deduction.points}" pattern="#"/>
                                        </c:otherwise>
                                    </c:choose>
                                </td>
                                <td class="score-entry-penalty-count">
                                    <span class="js-deduction-count" data-deduction-id="${deduction.id}">
                                        ${deduction.occurrenceCount > 0 ? deduction.occurrenceCount : ''}
                                    </span>
                                </td>
                                <td class="score-entry-penalty-time">
                                    <c:choose>
                                        <c:when test="${not empty deduction.recordedAt}">
                                            <fmt:formatDate value="${deduction.recordedAt}" pattern="HH:mm:ss"/>
                                        </c:when>
                                        <c:otherwise/>
                                    </c:choose>
                                </td>
                                <td class="score-entry-penalty-actions">
                                    <c:choose>
                                        <c:when test="${not empty faultSbd and deferredAdjust}">
                                            <button type="button"
                                                    class="score-entry-penalty-btn score-entry-penalty-btn--minus js-deduction-adjust"
                                                    data-deduction-id="${deduction.id}" data-delta="-1" title="Giảm">−</button>
                                            <button type="button"
                                                    class="score-entry-penalty-btn score-entry-penalty-btn--plus js-deduction-adjust"
                                                    data-deduction-id="${deduction.id}" data-delta="1" title="Tăng">+</button>
                                        </c:when>
                                        <c:when test="${not empty faultSbd}">
                                            <form method="post" action="${faultPageUrl}" class="score-entry-penalty-form">
                                                <input type="hidden" name="action" value="adjustDeduction">
                                                <input type="hidden" name="sbd" value="${faultSbd}">
                                                <input type="hidden" name="deductionId" value="${deduction.id}">
                                                <input type="hidden" name="delta" value="-1">
                                                <button type="submit" class="score-entry-penalty-btn score-entry-penalty-btn--minus" title="Giảm">−</button>
                                            </form>
                                            <form method="post" action="${faultPageUrl}" class="score-entry-penalty-form">
                                                <input type="hidden" name="action" value="adjustDeduction">
                                                <input type="hidden" name="sbd" value="${faultSbd}">
                                                <input type="hidden" name="deductionId" value="${deduction.id}">
                                                <input type="hidden" name="delta" value="1">
                                                <button type="submit" class="score-entry-penalty-btn score-entry-penalty-btn--plus" title="Tăng">+</button>
                                            </form>
                                        </c:when>
                                        <c:otherwise>
                                            <span class="score-entry-penalty-btn score-entry-penalty-btn--minus examiner-btn--disabled" title="Chọn thí sinh trước">−</span>
                                            <span class="score-entry-penalty-btn score-entry-penalty-btn--plus examiner-btn--disabled" title="Chọn thí sinh trước">+</span>
                                        </c:otherwise>
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

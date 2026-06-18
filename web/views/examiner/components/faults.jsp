<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>

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
                            <tr class="${deduction.critical ? 'score-entry-penalty-row--critical' : ''}">
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
                                    ${deduction.occurrenceCount > 0 ? deduction.occurrenceCount : ''}
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
                                    <c:if test="${not empty requestScope.candidate}">
                                        <a href="${requestScope.pageUrl}?sbd=${requestScope.candidate.sbd}&amp;action=adjustDeduction&amp;deductionId=${deduction.id}&amp;delta=-1"
                                           class="score-entry-penalty-btn score-entry-penalty-btn--minus">−</a>
                                        <a href="${requestScope.pageUrl}?sbd=${requestScope.candidate.sbd}&amp;action=adjustDeduction&amp;deductionId=${deduction.id}&amp;delta=1"
                                           class="score-entry-penalty-btn score-entry-penalty-btn--plus">+</a>
                                    </c:if>
                                </td>
                            </tr>
                        </c:forEach>
                    </c:otherwise>
                </c:choose>
            </tbody>
        </table>
    </div>
</section>
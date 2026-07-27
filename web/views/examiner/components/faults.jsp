<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>

<%--fault helpers--%>
<%--sbd from candidate or param--%>
<c:set var="faultSbd"
       value="${not empty requestScope.candidate ? requestScope.candidate.candidateNumber : param.sbd}" />
<%--post url for adjust--%>
<c:set var="faultPageUrl"
       value="${not empty requestScope.pageUrl ? requestScope.pageUrl : pageContext.request.contextPath.concat('/examiner/score-entry')}" />
<%--true = client-side +/- before save--%>
<c:set var="deferredAdjust" value="${param.deferredAdjust == 'true'}" />

<section class="card card-penalties">
    <div class="card-head">
        <div class="card-title">
            <span class="material-symbols-outlined">warning</span>
            <h2>Danh sách lỗi</h2>
        </div>
    </div>

    <div class="penalty-wrap">
        <table class="penalty-table">
            <%--headers--%>
            <thead>
                <tr>
                    <th>Lỗi</th>
                    <th>Trừ</th>
                    <th>Lần</th>
                    <th>Thao tác</th>
                </tr>
            </thead>

            <%--body--%>
            <tbody>
                <c:choose>

                    <%--case 1: no deductions--%>
                    <c:when test="${empty requestScope.scoreDeductions}">
                        <tr>
                            <td colspan="4" class="table-empty">Chưa có dữ liệu lỗi.</td>
                        </tr>
                    </c:when>

                    <%--case 2: has deductions--%>
                    <c:otherwise>
                        <c:forEach var="deduction" items="${requestScope.scoreDeductions}">
                            <tr class="${deduction.critical ? 'penalty-row critical' : ''}"
                                data-deduction-id="${deduction.id}"
                                data-critical="${deduction.critical}"
                                data-points="${deduction.points}"
                                data-base-count="${deduction.occurrenceCount}">

                                <td>
                                    <span class="penalty-reason">${deduction.reason}</span>
                                </td>

                                <%--points / fail--%>
                                <td class="penalty-points">
                                    <c:choose>
                                        <%--case 1: critical fail--%>
                                        <c:when test="${deduction.critical}">TRƯỢT</c:when>
                                        <%--case 2: point value--%>
                                        <c:otherwise>
                                            <fmt:formatNumber value="${deduction.points}" pattern="#"/>
                                        </c:otherwise>
                                    </c:choose>
                                </td>

                                <td class="penalty-count">
                                    <span class="js-deduction-count" data-deduction-id="${deduction.id}">
                                        ${deduction.occurrenceCount > 0 ? deduction.occurrenceCount : ''}
                                    </span>
                                </td>

                                <%--+/- actions--%>
                                <td class="penalty-actions">
                                    <c:choose>
                                        <%--case 1: js adjust (edit result)--%>
                                        <c:when test="${not empty faultSbd and deferredAdjust}">
                                            <button type="button"
                                                    class="penalty-btn penalty-btn-minus js-deduction-adjust"
                                                    data-deduction-id="${deduction.id}"
                                                    data-delta="-1"
                                                    title="Giảm">−</button>
                                            <button type="button"
                                                    class="penalty-btn penalty-btn-plus js-deduction-adjust"
                                                    data-deduction-id="${deduction.id}"
                                                    data-delta="1"
                                                    title="Tăng">+</button>
                                        </c:when>

                                        <%--case 2: post adjust (score entry)--%>
                                        <c:when test="${not empty faultSbd}">
                                            <form method="post"
                                                  action="${faultPageUrl}"
                                                  class="penalty-form">
                                                <input type="hidden" name="action" value="adjustDeduction">
                                                <input type="hidden" name="sbd" value="${faultSbd}">
                                                <input type="hidden" name="deductionId" value="${deduction.id}">
                                                <input type="hidden" name="delta" value="-1">
                                                <button type="submit"
                                                        class="penalty-btn penalty-btn-minus"
                                                        title="Giảm">−</button>
                                            </form>
                                            <form method="post"
                                                  action="${faultPageUrl}"
                                                  class="penalty-form">
                                                <input type="hidden" name="action" value="adjustDeduction">
                                                <input type="hidden" name="sbd" value="${faultSbd}">
                                                <input type="hidden" name="deductionId" value="${deduction.id}">
                                                <input type="hidden" name="delta" value="1">
                                                <button type="submit"
                                                        class="penalty-btn penalty-btn-plus"
                                                        title="Tăng">+</button>
                                            </form>
                                        </c:when>

                                        <%--case 3: no candidate yet--%>
                                        <c:otherwise>
                                            <span class="penalty-btn penalty-btn-minus btn grey-out"
                                                  title="Chọn thí sinh trước">−</span>
                                            <span class="penalty-btn penalty-btn-plus btn grey-out"
                                                  title="Chọn thí sinh trước">+</span>
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

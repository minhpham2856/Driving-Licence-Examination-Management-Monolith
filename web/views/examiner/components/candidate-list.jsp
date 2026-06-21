<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>

<!--variables-->
<c:set var="ctx" value="${pageContext.request.contextPath}" />

<c:set var="showCheckbox" value="${param.showCheckbox == 'true'}" />
<c:set var="showCheckboxCall" value="${param.showCheckboxCall == 'true'}" />
<c:set var="showName" value="${param.showName != 'false'}" />
<c:set var="showSbd" value="${param.showSbd != 'false'}" />
<c:set var="showDob" value="${param.showDob != 'false'}" />
<c:set var="showGovId" value="${param.showGovId != 'false'}" />
<c:set var="showAddress" value="${param.showAddress != 'false'}" />
<c:set var="showExamDate" value="${param.showExamDate == 'true'}" />
<c:set var="showTheoryScores" value="${param.showTheoryScores == 'true'}" />
<c:set var="showExamScore" value="${param.showExamScore == 'true'}" />
<c:set var="showResult" value="${param.showResult == 'true'}" />
<c:set var="showStatus" value="${param.showStatus == 'true'}" />

<c:set var="actionAbsent" value="${param.actionAbsent == 'true'}" />
<c:set var="actionSuspend" value="${param.actionSuspend == 'true'}" />
<c:set var="actionCall" value="${param.actionCall == 'true'}" />
<c:set var="actionDetail" value="${param.actionDetail == 'true'}" />
<c:set var="actionEditResult" value="${param.actionEditResult == 'true'}" />

<c:set var="itemsAttr" value="${not empty param.itemsAttr ? param.itemsAttr : 'candidates'}" />
<c:set var="listItems" value="${requestScope[itemsAttr]}" />

<c:set var="isQueueRow" value="${param.isQueueRow == 'true'}" />
<c:set var="showVehicle" value="${param.showVehicle == 'true'}" />
<c:set var="actionCallScore" value="${param.actionCallScore == 'true'}" />

<c:set var="title" value="${param.title}" />
<c:set var="badgeText" value="${param.badgeText}" />
<c:set var="cardClass" value="${empty param.cardClass ? 'examiner-card examiner-card--dashboard-table' : param.cardClass}" />
<c:set var="headerClass" value="${empty param.headerClass ? 'examiner-card__head' : param.headerClass}" />
<c:set var="titleClass" value="${empty param.titleClass ? 'examiner-card__title' : param.titleClass}" />
<c:set var="badgeClass" value="${empty param.badgeClass ? 'examiner-card__badge' : param.badgeClass}" />

<section class="${cardClass}">
    <c:if test="${not empty title or not empty badgeText}">
        <div class="${headerClass}">
            <c:if test="${not empty title}">
                <c:choose>
                    <c:when test="${fn:contains(cardClass, 'score-entry-card')}">
                        <div class="${titleClass}"><h2>${title}</h2></div>
                            </c:when>
                            <c:otherwise>
                        <h3 class="${titleClass}">${title}</h3>
                    </c:otherwise>
                </c:choose>
            </c:if>
            <c:if test="${not empty badgeText}">
                <span class="${badgeClass}">${badgeText}</span>
            </c:if>
        </div>
    </c:if>

    <div class="examiner-table-wrap">
        <table class="examiner-table${isQueueRow ? ' score-entry-table score-entry-table--queue' : ''}">
            <thead>
                <tr>
                    <c:if test="${showCheckbox or showCheckboxCall}"><th><input type="checkbox" class="examiner-check" id="checkAll"></th></c:if>
                        <c:if test="${isQueueRow}">
                        <th>SBD</th>
                        <th>Họ và tên</th>
                        </c:if>
                        <c:if test="${not isQueueRow}">
                            <c:if test="${showName}"><jsp:include page="/views/examiner/components/sort-th.jsp"><jsp:param name="sortColumn" value="fullName"/><jsp:param name="label" value="Tên"/></jsp:include></c:if>
                            <c:if test="${showSbd}"><jsp:include page="/views/examiner/components/sort-th.jsp"><jsp:param name="sortColumn" value="sbd"/><jsp:param name="label" value="SBD"/></jsp:include></c:if>
                        </c:if>

                    <c:if test="${showDob}"><jsp:include page="/views/examiner/components/sort-th.jsp"><jsp:param name="sortColumn" value="dob"/><jsp:param name="label" value="Ngày sinh"/></jsp:include></c:if>
                    <c:if test="${showAddress}"><jsp:include page="/views/examiner/components/sort-th.jsp"><jsp:param name="sortColumn" value="address"/><jsp:param name="label" value="Địa chỉ"/></jsp:include></c:if>

                    <c:if test="${isQueueRow}">
                        <c:if test="${showGovId}"><th>Số căn cước</th></c:if>
                        <c:if test="${showVehicle}"><th>Xe</th></c:if>
                            <th>Tình trạng</th>
                        </c:if>
                        <c:if test="${not isQueueRow}">
                            <c:if test="${showGovId}"><jsp:include page="/views/examiner/components/sort-th.jsp"><jsp:param name="sortColumn" value="governmentId"/><jsp:param name="label" value="Số CC"/></jsp:include></c:if>
                        </c:if>

                    <c:if test="${showExamDate}"><jsp:include page="/views/examiner/components/sort-th.jsp"><jsp:param name="sortColumn" value="examDate"/><jsp:param name="label" value="Ngày thi"/></jsp:include></c:if>

                    <c:if test="${showTheoryScores}">
                        <jsp:include page="/views/examiner/components/sort-th.jsp"><jsp:param name="sortColumn" value="correct"/><jsp:param name="label" value="Đúng"/></jsp:include>
                        <jsp:include page="/views/examiner/components/sort-th.jsp"><jsp:param name="sortColumn" value="wrong"/><jsp:param name="label" value="Sai"/></jsp:include>
                        <jsp:include page="/views/examiner/components/sort-th.jsp"><jsp:param name="sortColumn" value="unanswered"/><jsp:param name="label" value="Không TL"/></jsp:include>
                    </c:if>

                    <c:if test="${showExamScore}"><jsp:include page="/views/examiner/components/sort-th.jsp"><jsp:param name="sortColumn" value="examScore"/><jsp:param name="label" value="Điểm thi"/></jsp:include></c:if>
                    <c:if test="${showResult}"><jsp:include page="/views/examiner/components/sort-th.jsp"><jsp:param name="sortColumn" value="result"/><jsp:param name="label" value="Kết quả"/></jsp:include></c:if>
                    <c:if test="${showStatus}"><jsp:include page="/views/examiner/components/sort-th.jsp"><jsp:param name="sortColumn" value="status"/><jsp:param name="label" value="Tình trạng"/></jsp:include></c:if>

                    <c:if test="${actionAbsent}"><th>Vắng</th></c:if>
                    <c:if test="${actionSuspend}"><th>Đình chỉ</th></c:if>
                    <c:if test="${actionCall or actionDetail or actionEditResult or actionCallScore}"><th>Thao tác</th></c:if>
                    </tr>
                </thead>
                <tbody>
                <c:choose>
                    <c:when test="${empty listItems}">
                        <tr><td colspan="15" class="examiner-table__empty">
                                <c:choose>
                                    <c:when test="${requestScope.searchActive}">Không tìm thấy thí sinh phù hợp.</c:when>
                                    <c:otherwise>Chưa có dữ liệu.</c:otherwise>
                                </c:choose>
                            </td></tr>
                        </c:when>
                        <c:otherwise>
                            <c:forEach items="${listItems}" var="c" varStatus="st">
                            <tr class="<c:if test="${not isQueueRow and st.index % 2 == 1}">examiner-table__row--alt</c:if><c:if test="${isQueueRow}">score-entry-queue-row<c:if test="${c.active}"> score-entry-queue-row--active</c:if><c:if test="${c.called}"> score-entry-queue-row--called</c:if></c:if>">

                                <c:if test="${showCheckbox}">
                                    <td><input type="checkbox" class="examiner-check" name="sbd" value="${c.sbd}"></td>
                                    </c:if>
                                    <c:if test="${showCheckboxCall}">
                                    <td><input type="checkbox" class="examiner-check" name="sbd" value="${c.sbd}" form="callSelectedForm" ${c.callEligible ? '' : 'disabled'}></td>
                                    </c:if>

                                <c:choose>
                                    <c:when test="${isQueueRow}">
                                        <td class="score-entry-table__sbd"><a href="${requestScope.pageUrl}?sbd=${c.sbd}" class="score-entry-queue-link">${c.sbd}</a></td>
                                        <td><a href="${requestScope.pageUrl}?sbd=${c.sbd}" class="score-entry-queue-link">${c.fullName}</a></td>
                                        </c:when>
                                        <c:otherwise>
                                            <c:if test="${showName}"><td class="examiner-table__name">${c.fullName}</td></c:if>
                                        <c:if test="${showSbd}"><td class="examiner-table__mono-md">${c.sbd}</td></c:if>
                                    </c:otherwise>
                                </c:choose>

                                <c:if test="${showDob}"><td class="examiner-table__mono-md">${c.dob}</td></c:if>
                                <c:if test="${showAddress}"><td>${c.address}</td></c:if>

                                <c:choose>
                                    <c:when test="${isQueueRow}">
                                        <c:if test="${showGovId}"><td class="score-entry-table__mono">${c.governmentId}</td></c:if>
                                        <c:if test="${showVehicle}"><td class="score-entry-table__vehicle">${c.vehicleName}</td></c:if>
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
                                    </c:when>
                                    <c:otherwise>
                                        <c:if test="${showGovId}"><td class="examiner-table__mono-md">${c.governmentId}</td></c:if>
                                    </c:otherwise>
                                </c:choose>

                                <c:if test="${showExamDate}"><td class="examiner-table__mono-md">${c.examDate}</td></c:if>

                                <c:if test="${showTheoryScores}">
                                    <td class="examiner-text-green examiner-table__mono-md">${c.correct}</td>
                                    <td class="examiner-text-red examiner-table__mono-md">${c.wrong}</td>
                                    <td class="examiner-table__mono-md">${c.unanswered}</td>
                                </c:if>

                                <c:if test="${showExamScore}"><td class="examiner-table__mono-md">${c.examScore}</td></c:if>

                                <c:if test="${showResult}">
                                    <td>
                                        <c:choose>
                                            <c:when test="${c.passed}"><span class="examiner-tag examiner-tag--pass">${c.resultLabel}</span></c:when>
                                            <c:when test="${c.resultLabel != '-'}"><span class="examiner-tag examiner-tag--fail">${c.resultLabel}</span></c:when>
                                            <c:otherwise>-</c:otherwise>
                                        </c:choose>
                                    </td>
                                </c:if>

                                <c:if test="${showStatus}">
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
                                </c:if>

                                <c:if test="${actionAbsent}">
                                    <td>
                                        <c:choose>
                                            <c:when test="${c.absent}">
                                                <a href="${requestScope.pageUrl}?action=undoAbsent&amp;sbd=${c.sbd}" class="examiner-btn examiner-btn--white examiner-btn--compact">Hoàn tác</a>
                                            </c:when>
                                            <c:otherwise>
                                                <c:choose>
                                                    <c:when test="${c.markAbsentEligible}">
                                                        <a href="${requestScope.pageUrl}?action=markAbsent&amp;sbd=${c.sbd}" class="examiner-btn examiner-btn--danger examiner-btn--compact">Vắng</a>
                                                    </c:when>
                                                    <c:otherwise>
                                                        <span class="examiner-btn examiner-btn--white examiner-btn--compact examiner-btn--disabled">Vắng</span>
                                                    </c:otherwise>
                                                </c:choose>
                                            </c:otherwise>
                                        </c:choose>
                                    </td>
                                </c:if>

                                <c:if test="${actionSuspend}">
                                    <td>
                                        <c:choose>
                                            <c:when test="${c.suspended}">
                                                <a href="${requestScope.violationUndoUrl}?sbd=${c.sbd}" class="examiner-btn examiner-btn--white examiner-btn--compact">Hoàn tác</a>
                                            </c:when>
                                            <c:otherwise>
                                                <a href="${requestScope.violationConfirmUrl}?sbd=${c.sbd}" class="examiner-btn examiner-btn--danger examiner-btn--compact">Đình chỉ</a>
                                            </c:otherwise>
                                        </c:choose>
                                    </td>
                                </c:if>

                                <c:if test="${actionCall or actionDetail or actionEditResult or actionCallScore}">
                                    <td>
                                        <c:if test="${actionCallScore}">
                                            <a href="${requestScope.pageUrl}?action=call&amp;sbd=${c.sbd}" class="examiner-btn examiner-btn--primary examiner-btn--sm">Gọi</a>
                                        </c:if>
                                        <c:if test="${actionCall or actionDetail or actionEditResult}">
                                            <div class="examiner-actions">
                                                <c:if test="${actionCall}">
                                                    <c:choose>
                                                        <c:when test="${c.callEligible}">
                                                            <a href="${requestScope.pageUrl}?action=call&amp;sbd=${c.sbd}" class="examiner-btn examiner-btn--primary examiner-btn--compact">Gọi</a>
                                                        </c:when>
                                                        <c:otherwise>
                                                            <span class="examiner-btn examiner-btn--white examiner-btn--compact examiner-btn--disabled">Gọi</span>
                                                        </c:otherwise>
                                                    </c:choose>
                                                    <a href="${requestScope.detailViewUrl}?sbd=${c.sbd}" class="examiner-btn examiner-btn--white examiner-btn--compact">Chi tiết</a>
                                                    <a href="${requestScope.detailEditUrl}?sbd=${c.sbd}" class="examiner-btn examiner-btn--white examiner-btn--compact">Sửa TT</a>
                                                    <c:if test="${not requestScope.examinerSectionTheory}">
                                                        <a href="${requestScope.resultUrl}?sbd=${c.sbd}" class="examiner-btn examiner-btn--white examiner-btn--compact">Sửa KQ</a>
                                                    </c:if>
                                                    <c:choose>
                                                        <c:when test="${c.awaitingSignature}">
                                                            <a href="${requestScope.pageUrl}?action=printSignature&amp;sbd=${c.sbd}" class="examiner-btn examiner-btn--orange examiner-btn--compact">In biên bản</a>
                                                        </c:when>
                                                        <c:otherwise>
                                                            <span class="examiner-btn examiner-btn--orange examiner-btn--compact examiner-btn--disabled">In biên bản</span>
                                                        </c:otherwise>
                                                    </c:choose>
                                                    <c:choose>
                                                        <c:when test="${c.completeEligible}">
                                                            <a href="${requestScope.pageUrl}?action=completeSection&amp;sbd=${c.sbd}" class="examiner-btn examiner-btn--success examiner-btn--compact">Hoàn tất</a>
                                                        </c:when>
                                                        <c:otherwise>
                                                            <span class="examiner-btn examiner-btn--success examiner-btn--compact examiner-btn--disabled">Hoàn tất</span>
                                                        </c:otherwise>
                                                    </c:choose>
                                                </c:if>

                                                <c:if test="${actionDetail}">
                                                    <a href="${requestScope.detailEditUrl}?sbd=${c.sbd}" class="examiner-btn examiner-btn--white examiner-btn--compact">Chi tiết</a>
                                                </c:if>

                                                <c:if test="${actionEditResult}">
                                                    <a href="${requestScope.editUrl}?sbd=${c.sbd}" class="examiner-btn examiner-btn--white examiner-btn--compact">Sửa</a>
                                                </c:if>
                                            </div>
                                        </c:if>
                                    </td>
                                </c:if>
                            </tr>
                        </c:forEach>
                    </c:otherwise>
                </c:choose>
            </tbody>
        </table>
    </div>
</section>

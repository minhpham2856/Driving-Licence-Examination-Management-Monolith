<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>

<!--variables-->
<c:set var="ctx" value="${pageContext.request.contextPath}" />

<c:set var="layoutActionQueue" value="${param.layoutActionQueue == 'true'}" />
<c:set var="layoutActionBoard" value="${param.layoutActionBoard == 'true'}" />
<c:set var="actionViewViolation" value="${param.actionViewViolation == 'true'}" />
<c:set var="detailFromAction" value="${not empty requestScope.detailViewUrl}" />
<c:set var="detailUrl" value="${not empty requestScope.detailUrl ? requestScope.detailUrl : (not empty requestScope.detailViewUrl ? requestScope.detailViewUrl : ctx.concat('/examiner/candidate-details'))}" />
<c:set var="candidateDetailUrl" value="${ctx}/examiner/candidate-details" />
<c:set var="detailFromQuery" value="${detailFromAction ? '&amp;from=action' : ''}" />

<c:set var="showCheckbox" value="${param.showCheckbox == 'true'}" />
<c:set var="showName" value="${param.showName != 'false'}" />
<c:set var="showSbd" value="${param.showSbd != 'false'}" />
<c:set var="showDob" value="${param.showDob != 'false'}" />
<c:set var="showGovId" value="${param.showGovId != 'false'}" />
<c:set var="showAddress" value="${param.showAddress != 'false'}" />
<c:if test="${layoutActionQueue or layoutActionBoard}">
    <c:set var="showAddress" value="false" />
    <c:set var="showDob" value="false" />
    <c:set var="showGovId" value="false" />
</c:if>
<c:set var="showExamDate" value="${param.showExamDate == 'true'}" />
<c:set var="showTheoryScores" value="${param.showTheoryScores == 'true'}" />
<c:set var="showExamScore" value="${param.showExamScore == 'true' or param.showPracticalScore == 'true'}" />
<c:set var="showResult" value="${param.showResult == 'true'}" />
<c:set var="showStatus" value="${param.showStatus == 'true'}" />

<c:set var="actionSuspend" value="${param.actionSuspend == 'true'}" />
<c:set var="actionAttendance" value="${param.actionAttendance == 'true'}" />
<c:set var="actionCall" value="${param.actionCall == 'true'}" />
<c:set var="actionScoreEntry" value="${param.actionScoreEntry == 'true'}" />
<c:set var="actionPrint" value="${param.actionPrint == 'true'}" />
<c:set var="actionComplete" value="${param.actionComplete == 'true'}" />
<c:set var="showActionBar" value="${param.showActionBar == 'true'}" />
<c:set var="actionDetail" value="${param.actionDetail == 'true'}" />
<c:set var="actionEditResult" value="${param.actionEditResult == 'true'}" />

<c:set var="itemsAttr" value="${not empty param.itemsAttr ? param.itemsAttr : 'candidates'}" />
<c:set var="listItems" value="${requestScope[itemsAttr]}" />

<c:set var="isQueueRow" value="${param.isQueueRow == 'true'}" />
<c:set var="showVehicle" value="${param.showVehicle == 'true'}" />

<c:set var="title" value="${param.title}" />
<c:set var="badgeText" value="${param.badgeText}" />
<c:set var="cardClass" value="${empty param.cardClass ? (layoutActionBoard ? 'examiner-card examiner-action-card' : 'examiner-card examiner-card--dashboard-table') : param.cardClass}" />
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
                    <c:when test="${layoutActionBoard}">
                        <h2>${title}</h2>
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
                    <c:if test="${showCheckbox}"><th><input type="checkbox" class="examiner-check" id="checkAll"></th></c:if>
                    <c:if test="${isQueueRow}">
                        <th>SBD</th>
                        <th>Họ và tên</th>
                    </c:if>
                    <c:if test="${layoutActionBoard}">
                        <th>SBD</th>
                        <th>Họ tên</th>
                        <th>Trạng thái</th>
                        <th>Điểm</th>
                        <c:if test="${actionAttendance}"><th>Điểm danh</th></c:if>
                        <c:if test="${actionCall}"><th>Gọi</th></c:if>
                        <c:if test="${actionSuspend}"><th>Đình chỉ</th></c:if>
                        <c:if test="${actionScoreEntry}"><th>Nhập điểm</th></c:if>
                        <c:if test="${actionPrint}"><th>In</th></c:if>
                        <c:if test="${actionComplete}"><th>Hoàn thành</th></c:if>
                        <c:if test="${actionEditResult}"><th>Sửa kết quả</th></c:if>
                    </c:if>
                    <c:if test="${layoutActionQueue}">
                        <jsp:include page="/views/examiner/components/sort-th.jsp"><jsp:param name="sortColumn" value="fullName"/><jsp:param name="label" value="Tên"/></jsp:include>
                        <jsp:include page="/views/examiner/components/sort-th.jsp"><jsp:param name="sortColumn" value="sbd"/><jsp:param name="label" value="SBD"/></jsp:include>
                        <jsp:include page="/views/examiner/components/sort-th.jsp"><jsp:param name="sortColumn" value="dob"/><jsp:param name="label" value="Ngày sinh"/></jsp:include>
                        <jsp:include page="/views/examiner/components/sort-th.jsp"><jsp:param name="sortColumn" value="governmentId"/><jsp:param name="label" value="Số căn cước"/></jsp:include>
                        <jsp:include page="/views/examiner/components/sort-th.jsp"><jsp:param name="sortColumn" value="status"/><jsp:param name="label" value="Tình trạng"/></jsp:include>
                        <th>Điểm danh</th>
                        <th>Sai TT</th>
                        <th>Đình chỉ</th>
                        <th>In KQ thi</th>
                        <th>Hoàn thành</th>
                    </c:if>
                    <c:if test="${not layoutActionQueue and not layoutActionBoard}">
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
                        <c:if test="${actionSuspend}"><th>Đình chỉ</th></c:if>
                        <c:if test="${showActionBar or actionDetail or actionEditResult}"><th>Thao tác</th></c:if>
                    </c:if>
                    <c:if test="${actionViewViolation}"><th>Vi phạm</th></c:if>
                </tr>
            </thead>
            <tbody>
                <c:choose>
                    <c:when test="${empty listItems}">
                        <tr><td colspan="15" class="examiner-table__empty">
                            <c:choose>
                                <c:when test="${requestScope.searchActive}">Không tìm thấy thí sinh phù hợp.</c:when>
                                <c:when test="${layoutActionBoard}">Chưa có thí sinh trong kỳ thi/phần thi này.</c:when>
                                <c:otherwise>Chưa có dữ liệu.</c:otherwise>
                            </c:choose>
                        </td></tr>
                    </c:when>
                    <c:otherwise>
                        <c:forEach items="${listItems}" var="c" varStatus="st">
                            <tr class="<c:if test="${not isQueueRow and st.index % 2 == 1}">examiner-table__row--alt</c:if><c:if test="${isQueueRow}">score-entry-queue-row<c:if test="${c.active}"> score-entry-queue-row--active</c:if><c:if test="${c.invoked}"> score-entry-queue-row--invoked</c:if></c:if>">

                                <c:if test="${showCheckbox}">
                                    <td><input type="checkbox" class="examiner-check" name="sbd" value="${c.candidateNumber}"></td>
                                </c:if>

                                <c:choose>
                                    <c:when test="${layoutActionBoard}">
                                        <c:set var="actionRowAllowed" value="${c.sectionRequired and c.practicalEntryAllowed}" />
                                        <c:set var="cellDetailHref" value="${ctx}/examiner/candidate-details?sbd=${c.candidateNumber}&amp;from=action" />
                                        <%@ include file="cells/sbd.jsp" %>
                                        <%@ include file="cells/name.jsp" %>
                                        <%@ include file="cells/status.jsp" %>
                                        <%@ include file="cells/score.jsp" %>
                                        <c:choose>
                                            <c:when test="${actionRowAllowed}">
                                                <c:if test="${actionAttendance}"><%@ include file="cells/actions-attendance.jsp" %></c:if>
                                                <c:if test="${actionCall}"><%@ include file="cells/actions-call.jsp" %></c:if>
                                                <c:if test="${actionSuspend}"><%@ include file="cells/actions-suspend.jsp" %></c:if>
                                                <c:if test="${actionScoreEntry}"><%@ include file="cells/actions-score-entry.jsp" %></c:if>
                                                <c:if test="${actionPrint}"><%@ include file="cells/actions-print.jsp" %></c:if>
                                                <c:if test="${actionComplete}"><%@ include file="cells/actions-complete.jsp" %></c:if>
                                                <c:if test="${actionEditResult}"><%@ include file="cells/actions-edit-result.jsp" %></c:if>
                                            </c:when>
                                            <c:otherwise>
                                                <c:if test="${actionAttendance}"><td></td></c:if>
                                                <c:if test="${actionCall}"><td></td></c:if>
                                                <c:if test="${actionSuspend}"><td></td></c:if>
                                                <c:if test="${actionScoreEntry}"><td></td></c:if>
                                                <c:if test="${actionPrint}"><td></td></c:if>
                                                <c:if test="${actionComplete}"><td></td></c:if>
                                                <c:if test="${actionEditResult}"><td></td></c:if>
                                            </c:otherwise>
                                        </c:choose>
                                    </c:when>
                                    <c:when test="${layoutActionQueue}">
                                        <td class="examiner-table__name">
                                            <a href="${detailUrl}?sbd=${c.candidateNumber}${detailFromAction ? '&amp;from=action' : ''}" class="examiner-table-link">${c.fullName}</a>
                                        </td>
                                        <td class="examiner-table__mono-md">
                                            <a href="${detailUrl}?sbd=${c.candidateNumber}" class="examiner-table-link">${c.candidateNumber}</a>
                                        </td>
                                        <td class="examiner-table__mono-md">${c.dob}</td>
                                        <td class="examiner-table__mono-md">
                                            <a href="${detailUrl}?sbd=${c.candidateNumber}" class="examiner-table-link">${c.governmentId}</a>
                                        </td>
                                        <%@ include file="cells/status.jsp" %>
                                        <c:choose>
                                            <c:when test="${c.sectionRequired}">
                                                <%@ include file="cells/actions-attendance.jsp" %>
                                                <td>
                                                    <c:if test="${c.wrongInfoEligible}">
                                                        <form method="post" action="${requestScope.pageUrl}" style="display:inline">
                                                            <input type="hidden" name="action" value="wrongInfo">
                                                            <input type="hidden" name="sbd" value="${c.candidateNumber}">
                                                            <button type="submit" class="examiner-btn examiner-btn--white examiner-btn--compact">Sai TT</button>
                                                        </form>
                                                    </c:if>
                                                </td>
                                                <%@ include file="cells/actions-suspend.jsp" %>
                                                <%@ include file="cells/actions-print.jsp" %>
                                                <%@ include file="cells/actions-complete.jsp" %>
                                            </c:when>
                                            <c:otherwise>
                                                <td></td><td></td><td></td><td></td><td></td>
                                            </c:otherwise>
                                        </c:choose>
                                    </c:when>
                                    <c:when test="${isQueueRow}">
                                        <td class="score-entry-table__sbd"><a href="${requestScope.pageUrl}?sbd=${c.candidateNumber}" class="examiner-table-link">${c.candidateNumber}</a></td>
                                        <td><a href="${requestScope.pageUrl}?sbd=${c.candidateNumber}" class="examiner-table-link">${c.fullName}</a></td>
                                    </c:when>
                                    <c:otherwise>
                                        <c:if test="${showName}"><td class="examiner-table__name">${c.fullName}</td></c:if>
                                        <c:if test="${showSbd}"><td class="examiner-table__mono-md">${c.candidateNumber}</td></c:if>
                                    </c:otherwise>
                                </c:choose>

                                <c:if test="${not layoutActionQueue and not layoutActionBoard}">
                                    <c:if test="${showDob}"><td class="examiner-table__mono-md">${empty c.dob ? '' : c.dob}</td></c:if>
                                    <c:if test="${showAddress}"><td>${empty c.address ? '' : c.address}</td></c:if>

                                    <c:choose>
                                        <c:when test="${isQueueRow}">
                                            <c:if test="${showGovId}"><td class="score-entry-table__mono">${empty c.governmentId ? '' : c.governmentId}</td></c:if>
                                            <c:if test="${showVehicle}"><td class="score-entry-table__vehicle">${empty c.vehicleName ? '' : c.vehicleName}</td></c:if>
                                            <%@ include file="cells/status.jsp" %>
                                        </c:when>
                                        <c:otherwise>
                                            <c:if test="${showGovId}"><td class="examiner-table__mono-md">${empty c.governmentId ? '' : c.governmentId}</td></c:if>
                                        </c:otherwise>
                                    </c:choose>

                                    <c:if test="${showExamDate}"><td class="examiner-table__mono-md">${empty c.examDate ? '' : c.examDate}</td></c:if>

                                    <c:if test="${showTheoryScores}">
                                        <td class="examiner-text-green examiner-table__mono-md">${c.correct}</td>
                                        <td class="examiner-text-red examiner-table__mono-md">${c.wrong}</td>
                                        <td class="examiner-table__mono-md">${c.unanswered}</td>
                                    </c:if>

                                    <c:if test="${showExamScore}"><td class="examiner-table__mono-md">${empty c.examScore ? '' : c.examScore}</td></c:if>

                                    <c:if test="${showResult}">
                                        <td>
                                            <c:choose>
                                                <c:when test="${c.passed}"><span class="examiner-tag examiner-tag--pass">${c.resultLabel}</span></c:when>
                                                <c:when test="${not empty c.resultLabel}"><span class="examiner-tag examiner-tag--fail">${c.resultLabel}</span></c:when>
                                            </c:choose>
                                        </td>
                                    </c:if>

                                    <c:if test="${showStatus}">
                                        <%@ include file="cells/status.jsp" %>
                                    </c:if>
                                </c:if>

                                <c:if test="${actionSuspend and not layoutActionQueue and not layoutActionBoard}">
                                    <c:choose>
                                        <c:when test="${c.sectionRequired}">
                                            <%@ include file="cells/actions-suspend.jsp" %>
                                        </c:when>
                                        <c:otherwise><td></td></c:otherwise>
                                    </c:choose>
                                </c:if>

                                <c:if test="${actionViewViolation}">
                                    <td>
                                        <c:if test="${c.suspended}">
                                            <form method="post" action="${requestScope.pageUrl}" style="display:inline">
                                                <input type="hidden" name="action" value="undoSuspend">
                                                <input type="hidden" name="sbd" value="${c.candidateNumber}">
                                                <button type="submit" class="examiner-btn examiner-btn--white examiner-btn--compact">Gỡ đình chỉ</button>
                                            </form>
                                        </c:if>
                                    </td>
                                </c:if>

                                <c:if test="${not layoutActionQueue and not layoutActionBoard and (showActionBar or actionDetail or actionEditResult)}">
                                    <td>
                                        <c:if test="${showActionBar}">
                                            <div class="examiner-actions">
                                                <a href="${candidateDetailUrl}?sbd=${c.candidateNumber}${detailFromQuery}" class="examiner-btn examiner-btn--white examiner-btn--compact">Chi tiết</a>
                                                <c:if test="${requestScope.examinerSectionTheory and c.sectionRequired and (c.status == 'awaiting' or c.status == 'done')}">
                                                    <a href="${ctx}/examiner/candidate-paper?sbd=${c.candidateNumber}" class="examiner-btn examiner-btn--white examiner-btn--compact">Đề thi</a>
                                                </c:if>
                                                <c:if test="${not requestScope.examinerSectionTheory and c.sectionRequired and c.status eq 'done'}">
                                                    <a href="${requestScope.resultUrl}?sbd=${c.candidateNumber}" class="examiner-btn examiner-btn--white examiner-btn--compact">Sửa KQ</a>
                                                </c:if>
                                                <c:if test="${c.wrongInfoEligible}">
                                                    <form method="post" action="${requestScope.pageUrl}" style="display:inline"
                                                          onsubmit="return confirm('Chuyển thí sinh SBD ${c.candidateNumber} về phòng thủ tục?');">
                                                        <input type="hidden" name="action" value="wrongInfo">
                                                        <input type="hidden" name="sbd" value="${c.candidateNumber}">
                                                        <button type="submit" class="examiner-btn examiner-btn--white examiner-btn--compact">Sai thông tin</button>
                                                    </form>
                                                </c:if>
                                                <c:if test="${c.awaitingSignature}">
                                                    <form method="post" action="${requestScope.pageUrl}" target="examinerPrintTab"
                                                          style="display:inline"
                                                          onsubmit="window.open('', 'examinerPrintTab'); setTimeout(function () { window.location.reload(); }, 800);">
                                                        <input type="hidden" name="action" value="printResult">
                                                        <input type="hidden" name="sbd" value="${c.candidateNumber}">
                                                        <button type="submit" class="examiner-btn examiner-btn--orange examiner-btn--compact">In biên bản</button>
                                                    </form>
                                                </c:if>
                                                <c:if test="${c.completeEligible}">
                                                    <form method="post" action="${requestScope.pageUrl}" style="display:inline">
                                                        <input type="hidden" name="action" value="completeSection">
                                                        <input type="hidden" name="sbd" value="${c.candidateNumber}">
                                                        <button type="submit" class="examiner-btn examiner-btn--success examiner-btn--compact">Hoàn thành</button>
                                                    </form>
                                                </c:if>
                                            </div>
                                        </c:if>

                                        <c:if test="${actionDetail}">
                                            <div class="examiner-actions">
                                                <a href="${candidateDetailUrl}?sbd=${c.candidateNumber}${detailFromQuery}" class="examiner-btn examiner-btn--white examiner-btn--compact">Thông tin</a>
                                                <c:if test="${c.sectionRequired and c.practicalEntryAllowed}">
                                                    <c:if test="${not requestScope.examinerSectionTheory and c.status eq 'done'}">
                                                        <a href="${requestScope.resultUrl}?sbd=${c.candidateNumber}" class="examiner-btn examiner-btn--white examiner-btn--compact">Sửa KQ</a>
                                                    </c:if>
                                                    <c:if test="${requestScope.examinerSectionTheory}">
                                                        <a href="${ctx}/examiner/candidate-paper?sbd=${c.candidateNumber}" class="examiner-btn examiner-btn--white examiner-btn--compact">Đề thi</a>
                                                    </c:if>
                                                </c:if>
                                            </div>
                                        </c:if>

                                        <c:if test="${actionEditResult and c.sectionRequired and c.status eq 'done' and not requestScope.examinerSectionTheory}">
                                            <a href="${ctx}/examiner/result-details-edit?sbd=${c.candidateNumber}" class="examiner-btn examiner-btn--white examiner-btn--compact">Sửa</a>
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

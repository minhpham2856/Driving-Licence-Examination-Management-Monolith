<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" language="java" %>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>

<jsp:include page="/views/staff/examstaff/includes/examstaff-layout-head.jsp">
    <jsp:param name="activeSidebar" value="phan-bo-giam-khao" />
    <jsp:param name="pageTitle" value="Phân bổ giám khảo" />
    <jsp:param name="noCache" value="true" />
    <jsp:param name="mainClass" value="examstaff-main--scroll" />
</jsp:include>

        <c:if test="${not empty requestScope.sessionSelectMsg}">
            <div class="examiner-alert examiner-alert--success">${requestScope.sessionSelectMsg}</div>
        </c:if>
        <c:if test="${not empty alertMsg}">
            <div class="examiner-alert examiner-alert--success">${alertMsg}</div>
        </c:if>
        <c:if test="${not empty errorMsg}">
            <div class="examiner-alert examiner-alert--error">${errorMsg}</div>
        </c:if>

        <c:if test="${not empty currentSession}">
        <section class="report-pane dashboard-sessions-panel" aria-label="Phân bổ giám khảo">
            <div class="report-pane__header dashboard-sessions-panel__header">
                <h2 class="report-pane__title dashboard-sessions-panel__title">Phân bổ giám khảo</h2>
            </div>
            <jsp:include page="/views/staff/examstaff/includes/exam-session-summary-line.jsp" />
            <p class="dashboard-sessions-panel__desc es-text-muted-sm" style="margin-top: -0.25rem;">
                Phân công giám khảo theo phòng / phần thi trong kỳ.
                <c:if test="${not empty currentSession.examTypeName}">Nội dung: ${currentSession.examTypeName}.</c:if>
            </p>
            <jsp:include page="/views/staff/examstaff/includes/exam-session-shift-chip.jsp">
                <jsp:param name="redirect" value="examiner-allocation" />
            </jsp:include>
        </section>

            <div class="examiner-grid">
                <div class="examiner-panel-card">
                    <h3>Giám khảo khả dụng (${availableExaminers.size()})</h3>
                    <p class="es-text-muted-sm">Chưa được phân công trong kỳ thi này.</p>
                    <c:choose>
                        <c:when test="${empty availableExaminers}">
                            <p class="es-text-muted-sm">Không còn giám khảo trống trong kỳ thi.</p>
                        </c:when>
                        <c:otherwise>
                            <c:forEach var="ex" items="${availableExaminers}">
                                <span class="examiner-chip chip-available">${not empty ex.profile ? ex.profile.fullName : ex.username} (@${ex.username})</span>
                            </c:forEach>
                        </c:otherwise>
                    </c:choose>
                </div>
                <div class="examiner-panel-card">
                    <h3>Giám khảo đã phân công (${busyExaminers.size()})</h3>
                    <c:choose>
                        <c:when test="${empty busyExaminers}">
                            <p class="es-text-muted-sm">Chưa phân công giám khảo nào.</p>
                        </c:when>
                        <c:otherwise>
                            <c:forEach var="ex" items="${busyExaminers}">
                                <span class="examiner-chip chip-busy">${not empty ex.profile ? ex.profile.fullName : ex.username} (@${ex.username})</span>
                            </c:forEach>
                        </c:otherwise>
                    </c:choose>
                </div>
            </div>

            <div class="examiner-panel-card examiner-panel-card--section">
                <h3>Phân công mới</h3>
                <form class="examiner-assign-form" method="get" action="${pageContext.request.contextPath}/views/staff/examstaff/examiner-allocation">
                    <input type="hidden" name="sessionId" value="${currentSession.id}">
                    <input type="hidden" name="action" value="assign">
                    <input type="hidden" name="targetSessionId" value="${currentSession.id}">
                    <div>
                        <label for="areaId">Phòng / khu thi</label>
                        <select name="areaId" id="areaId" required>
                            <c:choose>
                                <c:when test="${empty areaAssignOptions}">
                                    <option value="">— Chưa có phòng thi —</option>
                                </c:when>
                                <c:otherwise>
                                    <c:forEach var="opt" items="${areaAssignOptions}">
                                        <option value="${opt.areaId}" data-session="${opt.sessionId}" data-type="${opt.areaType}">
                                            ${opt.areaName}
                                        </option>
                                    </c:forEach>
                                </c:otherwise>
                            </c:choose>
                        </select>
                    </div>
                    <div>
                        <label for="examinerUserId">Giám khảo</label>
                        <select name="examinerUserId" id="examinerUserId" required>
                            <c:choose>
                                <c:when test="${empty allExaminers}">
                                    <option value="">— Chưa có giám khảo (Role=Examiner) —</option>
                                </c:when>
                                <c:otherwise>
                                    <c:forEach var="ex" items="${allExaminers}">
                                        <option value="${ex.id}">${not empty ex.profile ? ex.profile.fullName : ex.username}</option>
                                    </c:forEach>
                                </c:otherwise>
                            </c:choose>
                        </select>
                    </div>
                    <div>
                        <label>&nbsp;</label>
                        <button type="submit">Phân công</button>
                    </div>
                </form>
            </div>

            <div class="examiner-panel-card examiner-panel-card--section">
                <h3>Phân công toàn kỳ thi</h3>
                <table class="examiner-data-table">
                    <thead>
                        <tr>
                            <th>Phần thi</th>
                            <th>Phòng</th>
                            <th>Loại khu vực</th>
                            <th>Giám khảo</th>
                            <th></th>
                        </tr>
                    </thead>
                    <tbody>
                        <c:choose>
                            <c:when test="${empty examAssignments}">
                                <tr><td colspan="5" class="es-text-muted-sm">Chưa có phân công cho kỳ thi này.</td></tr>
                            </c:when>
                            <c:otherwise>
                                <c:forEach var="a" items="${examAssignments}">
                                    <tr>
                                        <td>${a.sessionName}</td>
                                        <td>
                                            <c:choose>
                                                <c:when test="${not empty a.areaName}">${a.areaName}</c:when>
                                                <c:otherwise>—</c:otherwise>
                                            </c:choose>
                                        </td>
                                        <td>
                                            <c:choose>
                                                <c:when test="${not empty a.areaType}">${a.areaType}</c:when>
                                                <c:when test="${not empty a.examTypeName}">${a.examTypeName}</c:when>
                                                <c:otherwise>—</c:otherwise>
                                            </c:choose>
                                        </td>
                                        <td>${a.examinerName} <span class="es-text-muted-sm">(@${a.examinerUsername})</span></td>
                                        <td>
                                            <c:if test="${a.areaId > 0}">
                                                <a class="btn-examiner-remove"
                                                   href="${pageContext.request.contextPath}/views/staff/examstaff/examiner-allocation?sessionId=${currentSession.id}&action=remove&slotKey=${a.slotKey}"
                                                   data-confirm-remove="true"
                                                   data-confirm-msg="Gỡ phân công giám khảo này?">Gỡ</a>
                                            </c:if>
                                        </td>
                                    </tr>
                                </c:forEach>
                            </c:otherwise>
                        </c:choose>
                    </tbody>
                </table>
            </div>
        </c:if>

<jsp:include page="/views/staff/examstaff/includes/examstaff-layout-foot.jsp">
    <jsp:param name="extraScript" value="/assets/js/examiner-allocation.js" />
</jsp:include>

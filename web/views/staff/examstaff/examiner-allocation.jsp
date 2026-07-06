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
            <div class="examiner-panel-card examiner-panel-card--spaced">
                <h3>Kỳ thi hạng ${currentSession.licenseCode} — <fmt:formatDate value="${currentSession.examDate}" pattern="dd/MM/yyyy"/></h3>
                <p class="es-text-muted-sm" style="margin: 0 0 10px 0;">Các ca trong kỳ thi (phân công giám khảo theo từng ca/môn):</p>
                <div class="session-shift-list">
                <c:forEach var="ds" items="${examSessions}">
                    <div class="session-shift-chip">
                        <span class="session-shift-chip__meta">
                            <strong>${ds.sessionName}</strong>
                        </span>
                        <jsp:include page="/views/staff/examstaff/includes/session-shift-controls.jsp">
                            <jsp:param name="sessionId" value="${ds.id}" />
                            <jsp:param name="sessionName" value="${ds.sessionName}" />
                            <jsp:param name="status" value="${ds.status}" />
                            <jsp:param name="redirect" value="examiner-allocation" />
                        </jsp:include>
                    </div>
                </c:forEach>
                </div>
            </div>

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
                    <div>
                        <label for="targetSessionId">Ca / môn thi</label>
                        <select name="targetSessionId" id="targetSessionId" required>
                            <c:forEach var="ds" items="${examSessions}">
                                <option value="${ds.id}">${ds.sessionName} (<c:choose>
                                    <c:when test="${ds.examTypeName eq 'Theory' or fn:contains(ds.examTypeName, 'Lý thuyết')}">Lý thuyết</c:when>
                                    <c:when test="${ds.examTypeName eq 'Practical' or fn:contains(ds.examTypeName, 'Sa hình') or fn:contains(ds.examTypeName, 'Thực hành')}">Sa hình</c:when>
                                    <c:when test="${ds.examTypeName eq 'OnRoad' or fn:contains(ds.examTypeName, 'Đường')}">Đường trường</c:when>
                                    <c:otherwise>${ds.examTypeName}</c:otherwise>
                                </c:choose>)</option>
                            </c:forEach>
                        </select>
                    </div>
                    <div>
                        <label for="areaId">Phòng thi</label>
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
                            <th>Ca / môn thi</th>
                            <th>Phòng</th>
                            <th>Loại thi</th>
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
                                                <c:when test="${not empty a.areaName}">${a.areaName}<div class="area-type-tag">${a.areaType}</div></c:when>
                                                <c:otherwise>—</c:otherwise>
                                            </c:choose>
                                        </td>
                                        <td>${a.examTypeName}</td>
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

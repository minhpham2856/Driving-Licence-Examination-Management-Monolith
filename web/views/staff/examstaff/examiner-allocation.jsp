<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" language="java" %>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>

<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Phân bổ Giám khảo - Ban Sát Hạch</title>
    <link rel="preconnect" href="https://fonts.googleapis.com">
    <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
    <link href="https://fonts.googleapis.com/css2?family=Be+Vietnam+Pro:wght@400;500;600;700;800&family=Inter:wght@400;500;600;700;800&display=swap" rel="stylesheet">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/style.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/layout.css">
</head>
<body class="has-side-nav-bar">

<jsp:include page="/views/layout/sidebar-examstaff.jsp">
    <jsp:param name="activeSidebar" value="phan-bo-giam-khao" />
</jsp:include>

<div class="dashboard-shell">
    <main class="main-content">
        <nav class="breadcrumbs" aria-label="Breadcrumb">
            <a href="${pageContext.request.contextPath}/views/public/home.jsp">Trang chủ</a>
            <span class="breadcrumbs__separator" aria-hidden="true">/</span>
            <span class="breadcrumbs__current">Ban Sát Hạch</span>
            <span class="breadcrumbs__separator" aria-hidden="true">/</span>
            <span class="breadcrumbs__current" aria-current="page">Phân bổ giám khảo</span>
        </nav>

        <header class="page-header">
            <div class="page-title-wrap">
                <h1 class="page-title">Phân bổ giám khảo theo kỳ thi</h1>
                <p class="page-subtitle">Đồng bộ với <strong>Quy trình phân bổ</strong>: một kỳ thi gồm lý thuyết → sa hình → đường trường (hạng B). Phòng từ <strong>Session_ExamArea</strong>, giám khảo <strong>Session_Examiner</strong>.</p>
            </div>
            <div class="page-actions">
                <form method="get" action="${pageContext.request.contextPath}/views/staff/examstaff/examiner-allocation" class="examiner-session-form">
                    <label for="sessionId" class="examiner-session-form__label">Kỳ thi (hạng / ngày):</label>
                    <select name="sessionId" id="sessionId" class="examiner-session-form__select">
                        <c:forEach var="exam" items="${examOptions}">
                            <option value="${exam.id}" ${selectedExamId eq exam.examId ? 'selected' : ''}>
                                Kỳ thi hạng ${exam.licenseCode} — <fmt:formatDate value="${exam.examDate}" pattern="dd/MM/yyyy"/> (${exam.status})
                            </option>
                        </c:forEach>
                    </select>
                </form>
            </div>
        </header>

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
                <c:forEach var="ds" items="${examSessions}">
                    <div class="session-pill" style="display: inline-flex; align-items: center; gap: 8px; flex-wrap: wrap; margin: 4px 8px 4px 0;">
                        <span>
                            <strong>${ds.sessionName}</strong>
                            (<fmt:formatDate value="${ds.shiftStartTime}" pattern="HH:mm"/>–<fmt:formatDate value="${ds.shiftEndTime}" pattern="HH:mm"/>)
                            — <c:choose>
                                <c:when test="${ds.examTypeName eq 'Theory' or fn:contains(ds.examTypeName, 'Lý thuyết')}">Lý thuyết</c:when>
                                <c:when test="${ds.examTypeName eq 'Practical' or fn:contains(ds.examTypeName, 'Sa hình') or fn:contains(ds.examTypeName, 'Thực hành')}">Sa hình / Thực hành</c:when>
                                <c:when test="${ds.examTypeName eq 'OnRoad' or fn:contains(ds.examTypeName, 'Đường')}">Đường trường</c:when>
                                <c:otherwise>${ds.examTypeName}</c:otherwise>
                            </c:choose>
                            — <em>${ds.status}</em>
                        </span>
                        <c:if test="${ds.status ne 'InProgress' and ds.status ne 'Completed'}">
                            <form action="session-control" method="POST" style="margin: 0; display: inline;" onsubmit="return confirm('Bắt đầu ca ${ds.sessionName}?');">
                                <input type="hidden" name="action" value="startSession">
                                <input type="hidden" name="sessionId" value="${ds.id}">
                                <input type="hidden" name="redirect" value="examiner-allocation">
                                <button type="submit" class="btn-filter" style="height: 28px; padding: 0 0.6rem; border-radius: 6px; font-size: 0.72rem; font-weight: 700;">Bắt đầu ca</button>
                            </form>
                        </c:if>
                        <c:if test="${ds.status eq 'InProgress'}">
                            <form action="session-control" method="POST" style="margin: 0; display: inline;" onsubmit="return confirm('Kết thúc ca ${ds.sessionName}?');">
                                <input type="hidden" name="action" value="endSession">
                                <input type="hidden" name="sessionId" value="${ds.id}">
                                <input type="hidden" name="redirect" value="examiner-allocation">
                                <button type="submit" class="btn-export" style="height: 28px; padding: 0 0.6rem; border-radius: 6px; font-size: 0.72rem; font-weight: 700; color: #b91c1c; border-color: #fecaca;">Kết thúc ca</button>
                            </form>
                        </c:if>
                    </div>
                </c:forEach>
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
                                <span class="examiner-chip chip-available">${ex.person.fullName} (@${ex.username})</span>
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
                                <span class="examiner-chip chip-busy">${ex.person.fullName} (@${ex.username})</span>
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
                        <label for="areaId">Phòng thi (Session_ExamArea)</label>
                        <select name="areaId" id="areaId" required>
                            <c:forEach var="ds" items="${examSessions}">
                                <c:forEach var="ar" items="${areasBySession[ds.id]}">
                                    <option value="${ar.id}" data-session="${ds.id}" data-type="${ar.areaType}">
                                        ${ar.areaName} (${ar.areaType}) — ${ds.sessionName}
                                    </option>
                                </c:forEach>
                            </c:forEach>
                        </select>
                    </div>
                    <div>
                        <label for="examinerUserId">Giám khảo</label>
                        <select name="examinerUserId" id="examinerUserId" required>
                            <c:forEach var="ex" items="${allExaminers}">
                                <option value="${ex.id}">${ex.person.fullName}</option>
                            </c:forEach>
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
    </main>
</div>

<script src="${pageContext.request.contextPath}/assets/js/examiner-allocation.js" charset="UTF-8"></script>
</body>
</html>

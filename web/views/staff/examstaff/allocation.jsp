<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" language="java" %>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib prefix = "fn" uri = "http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix = "fmt" uri = "http://java.sun.com/jsp/jstl/fmt" %>

<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Quy Trình Phân Bổ Thí Sinh - Ban Sát Hạch</title>
    
    <link rel="preconnect" href="https://fonts.googleapis.com">
    <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
    <link href="https://fonts.googleapis.com/css2?family=Be+Vietnam+Pro:wght@400;500;600;700;800&family=Inter:wght@400;500;600;700;800&display=swap" rel="stylesheet">
    
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/style.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/layout.css">
</head>
<body class="has-side-nav-bar">

<jsp:include page="/views/layout/sidebar-examstaff.jsp">
    <jsp:param name="activeSidebar" value="phan-bo" />
</jsp:include>

<div class="dashboard-shell">
    <main class="main-content">
        
        <nav class="breadcrumbs" aria-label="Breadcrumb">
            <a href="${pageContext.request.contextPath}/views/public/home.jsp">Trang chủ</a>
            <span class="breadcrumbs__separator" aria-hidden="true">/</span>
            <span class="breadcrumbs__current">Ban Sát Hạch</span>
            <span class="breadcrumbs__separator" aria-hidden="true">/</span>
            <span class="breadcrumbs__current" aria-current="page">Quy trình phân bổ</span>
        </nav>
        
        <header class="page-header">
            <div class="page-title-wrap">
                <h1 class="page-title">Trình tự điều phối thí sinh</h1>
                <p class="page-subtitle">Quản lý và di chuyển thí sinh theo trình tự nghiệp vụ — hiển thị dạng bảng theo từng giai đoạn ca thi.</p>
            </div>
            
            <div class="page-actions" style="display: flex; gap: 10px; align-items: center;">
                <div class="es-search-box">
                    <input type="text" id="candidateSearch" class="es-search-box__input" placeholder="Tìm thí sinh (SBD, Họ tên, CCCD...)">
                    <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round" class="es-search-box__icon">
                        <circle cx="11" cy="11" r="8"></circle>
                        <line x1="21" y1="21" x2="16.65" y2="16.65"></line>
                    </svg>
                </div>

                <button type="button" id="btnExtendAll" class="btn-export" style="height: 38px; padding: 0 1rem; font-size: 0.82rem; border-radius: 8px; cursor: pointer; display: inline-flex; align-items: center; gap: 6px; border-color: #cbd5e1; background-color: #ffffff; color: #475569; user-select: none;">
                    <svg class="extend-all-icon" width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" style="transition: transform 0.3s ease;">
                        <path d="M6 9l6 6 6-6"/>
                    </svg>
                    <span class="extend-text">Mở rộng tất cả</span>
                    <span class="collapse-text" style="display: none;">Thu gọn tất cả</span>
                </button>
                
            </div>
        </header>

        <c:if test="${not empty requestScope.errorMsg}">
            <div style="background-color: #fef2f2; border: 1.5px solid #ef4444; border-radius: 12px; padding: 0.88rem 1.25rem; margin-bottom: 1.25rem; display: flex; gap: 8px; align-items: center;" class="animated shake">
                <svg width="20" height="20" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg" style="color: #ef4444; flex-shrink: 0;">
                    <circle cx="12" cy="12" r="10" stroke="currentColor" stroke-width="2"/>
                    <path d="M12 16v-4M12 8h.01" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                </svg>
                <span style="font-size: 0.85rem; font-weight: 700; color: #b91c1c;">
                    ${requestScope.errorMsg}
                </span>
            </div>
        </c:if>
        
        <c:if test="${not empty requestScope.warningMsg}">
            <div style="background-color: #fffbeb; border: 1.5px solid #f59e0b; border-radius: 12px; padding: 0.88rem 1.25rem; margin-bottom: 1.25rem; display: flex; gap: 8px; align-items: center;" class="animated pulse">
                <svg width="20" height="20" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg" style="color: #f59e0b; flex-shrink: 0;">
                    <path d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-3L13.732 4c-.77-1.333-2.694-1.333-3.464 0L3.34 16c-.77 1.333.192 3 1.732 3z" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                </svg>
                <span style="font-size: 0.85rem; font-weight: 700; color: #b45309;">
                    ${requestScope.warningMsg}
                </span>
            </div>
        </c:if>

        <c:if test="${not empty requestScope.alertMsg}">
            <div style="background-color: #eff6ff; border: 1.5px solid #3b82f6; border-radius: 12px; padding: 0.88rem 1.25rem; margin-bottom: 1.25rem; display: flex; gap: 8px; align-items: center;">
                <svg width="20" height="20" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg" style="color: #3b82f6; flex-shrink: 0;">
                    <circle cx="12" cy="12" r="10" stroke="currentColor" stroke-width="2"/>
                    <path d="M12 16v-4M12 8h.01" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                </svg>
                <span style="font-size: 0.85rem; font-weight: 700; color: #1e3a8a;">
                    ${requestScope.alertMsg}
                </span>
            </div>
        </c:if>

        <div class="report-pane" style="margin-top: 1.25rem; border-radius: 16px; padding: 1.5rem; border: 1px solid #cbd5e1; background: #ffffff; box-shadow: 0 4px 15px rgba(0,0,0,0.02);">
            <div style="display: flex; justify-content: space-between; align-items: center; flex-wrap: wrap; gap: 10px;">
                <h2 style="font-size: 1.05rem; font-weight: 800; color: #0f172a; margin: 0; display: flex; align-items: center; gap: 8px;">
                    <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" style="color: #0052cc;">
                        <rect x="4" y="4" width="16" height="16" rx="2"/>
                        <path d="M9 9h6M9 13h6"/>
                    </svg>
                    Bảng điều phối ca sát hạch
                </h2>
                
                <span class="role-badge role-badge--admin" style="font-size: 0.78rem; font-weight: 800; padding: 4px 10px; border-radius: 6px;">
                    Tổng số: ${fn:length(sessionScope.candidateQueue)} thí sinh
                </span>
            </div>
            
            <div style="display: flex; flex-wrap: wrap; gap: 1.5rem; justify-content: space-between; align-items: flex-end; margin-top: 1rem; border-top: 1.5px solid #f1f5f9; padding-top: 1rem;">
                <form action="allocation" method="GET" style="display: flex; flex-direction: column; gap: 6px; flex: 1; min-width: 280px;">
                    <label for="sessionId" style="font-size: 0.72rem; font-weight: 800; color: #475569; text-transform: uppercase; letter-spacing: 0.03em;">Chọn kỳ thi (hạng / ngày):</label>
                    <div style="display: flex; gap: 8px;">
                        <select id="sessionId" name="sessionId" data-auto-submit class="es-session-selector__select es-session-selector__select--wide">
                            <c:forEach var="exam" items="${requestScope.examOptions}">
                                <option value="${exam.id}" ${requestScope.selectedExamId eq exam.examId ? 'selected' : ''}>
                                    Kỳ thi hạng ${exam.licenseCode} — ${exam.examDate} (${exam.status})
                                </option>
                            </c:forEach>
                        </select>
                        <button type="submit" class="btn-batch btn-batch--alt" style="height: 38px; width: 38px; padding: 0; display: inline-flex; align-items: center; justify-content: center; border-radius: 8px;" title="Tải lại">
                            <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5">
                                <path d="M21.5 2v6h-6M21.34 15.57a10 10 0 1 1-.57-8.38l5.67-5.67"/>
                            </svg>
                        </button>
                    </div>
                </form>
            </div>
        </div>

        <div class="pipeline-container" style="display: flex; flex-direction: column; gap: 1.5rem; margin-top: 1.5rem;">

            <div class="allocation-pipeline-row">
            <div class="allocation-pipeline-cell">

                <c:set var="waitingCount" value="0" />
                <c:forEach var="c" items="${sessionScope.candidateQueue}">
                    <c:if test="${not c.procedureComplete and not c.absent}">
                        <c:set var="waitingCount" value="${waitingCount + 1}" />
                    </c:if>
                </c:forEach>

                <div class="allocation-stage-panel allocation-stage-panel--waiting">
                    <div class="allocation-stage-panel__head">
                        <div class="allocation-stage-panel__title-wrap">
                            <h4 class="allocation-stage-panel__title">Phòng chờ chính</h4>
                            <p class="allocation-stage-panel__meta">Phòng Chờ Số 01 · Sức chứa 100 người</p>
                        </div>
                        <span class="allocation-stage-panel__count">${waitingCount} thí sinh</span>
                    </div>
                    <div class="table-responsive">
                        <table class="report-table allocation-stage-table allocation-table--fill">
                            <thead>
                                <tr>
                                    <th scope="col">STT</th>
                                    <th scope="col">SBD</th>
                                    <th scope="col">Họ tên</th>
                                    <th scope="col">Hạng</th>
                                    <th scope="col">Trạng thái</th>
                                </tr>
                            </thead>
                                <tbody>
                                    <c:set var="waitingIdx" value="0" />
                                    <c:forEach var="c" items="${sessionScope.candidateQueue}">
                                        <c:if test="${not c.procedureComplete and not c.absent}">
                                            <c:set var="waitingIdx" value="${waitingIdx + 1}" />
                                            <tr class="allocation-stage-row">
                                                <td>${waitingIdx}</td>
                                                <td><strong>${c.sbd}</strong></td>
                                                <td>${c.name}</td>
                                                <td>${c.clazz}</td>
                                                <td><span class="allocation-stage-status allocation-stage-status--waiting">Chờ làm thủ tục / thu lệ phí</span></td>
                                            </tr>
                                        </c:if>
                                    </c:forEach>
                                    <c:if test="${waitingCount eq 0}">
                                        <tr>
                                            <td colspan="5" class="allocation-stage-table__empty">Không có thí sinh nào trong phòng chờ chính.</td>
                                        </tr>
                                    </c:if>
                                </tbody>
                            </table>
                    </div>
                </div>
            </div>
            
            <div class="allocation-pipeline-cell">

                <c:set var="theoryCount" value="0" />
                <c:forEach var="c" items="${sessionScope.candidateQueue}">
                    <c:if test="${c.procedureComplete and c.theoryPassed eq 'none' and not c.absent}">
                        <c:set var="theoryCount" value="${theoryCount + 1}" />
                    </c:if>
                </c:forEach>

                <div class="allocation-stage-panel allocation-stage-panel--theory">
                    <div class="allocation-stage-panel__head">
                        <div class="allocation-stage-panel__title-wrap">
                            <h4 class="allocation-stage-panel__title">Phòng thi lý thuyết</h4>
                            <p class="allocation-stage-panel__meta">Phòng thi lý thuyết · Máy tính do Giám thị quản lý</p>
                        </div>
                        <span class="allocation-stage-panel__count">${theoryCount} thí sinh</span>
                    </div>
                    <div class="table-responsive">
                        <table class="report-table allocation-stage-table allocation-table--fill">
                            <thead>
                                <tr>
                                    <th scope="col">STT</th>
                                    <th scope="col">SBD</th>
                                    <th scope="col">Họ tên</th>
                                    <th scope="col">Hạng</th>
                                    <th scope="col">Hồ sơ</th>
                                    <th scope="col">Phòng thi</th>
                                    <th scope="col">Thao tác</th>
                                </tr>
                            </thead>
                                <tbody>
                                    <c:set var="theoryIdx" value="0" />
                                    <c:forEach var="c" items="${sessionScope.candidateQueue}">
                                        <c:if test="${c.procedureComplete and c.theoryPassed eq 'none' and not c.absent}">
                                            <c:set var="theoryIdx" value="${theoryIdx + 1}" />
                                            <tr class="allocation-stage-row">
                                                <td>${theoryIdx}</td>
                                                <td><strong>${c.sbd}</strong></td>
                                                <td>${c.name}</td>
                                                <td>${c.clazz}</td>
                                                <td>
                                                    <span class="badge-pill-status badge-pill-status--success">Ảnh</span>
                                                    <span class="badge-pill-status badge-pill-status--success">Lệ phí</span>
                                                </td>
                                                <td>
                                                    <form action="allocation" method="GET" class="allocation-inline-form">
                                                        <input type="hidden" name="action" value="allocateRoom">
                                                        <input type="hidden" name="id" value="${c.id}">
                                                        <select name="areaId" data-auto-submit class="allocation-area-select allocation-area-select--table">
                                                            <c:forEach var="room" items="${requestScope.activeTheoryRooms}">
                                                                <option value="${room.id}" ${c.allocatedAreaId eq room.id ? 'selected' : ''}>${room.areaName}</option>
                                                            </c:forEach>
                                                        </select>
                                                    </form>
                                                </td>
                                                <td>
                                                    <a href="allocation?action=submitTheoryScore&amp;id=${c.id}&amp;score=90" class="allocation-table-action allocation-table-action--theory">Chấm LT (Auto)</a>
                                                </td>
                                            </tr>
                                        </c:if>
                                    </c:forEach>
                                    <c:if test="${theoryCount eq 0}">
                                        <tr>
                                            <td colspan="7" class="allocation-stage-table__empty">Chưa có thí sinh nào hoàn tất hồ sơ và thu lệ phí, chờ thi Lý thuyết.</td>
                                        </tr>
                                    </c:if>
                                </tbody>
                            </table>
                    </div>
                </div>
            </div>
            </div>

            <div class="allocation-pipeline-row">
            <div class="allocation-pipeline-cell">

                <c:set var="practicalCount" value="0" />
                <c:forEach var="c" items="${sessionScope.candidateQueue}">
                    <c:if test="${c.theoryPassed eq 'passed' and c.practicalPassed eq 'none' and not c.absent}">
                        <c:set var="practicalCount" value="${practicalCount + 1}" />
                    </c:if>
                </c:forEach>

                <div class="allocation-stage-panel allocation-stage-panel--practical">
                    <div class="allocation-stage-panel__head">
                        <div class="allocation-stage-panel__title-wrap">
                            <h4 class="allocation-stage-panel__title">Sa hình / Thực hành</h4>
                            <p class="allocation-stage-panel__meta">Sân Thực hành Số 1 · Thiết bị do Giám thị quản lý</p>
                        </div>
                        <span class="allocation-stage-panel__count">${practicalCount} thí sinh</span>
                    </div>
                    <div class="table-responsive">
                        <table class="report-table allocation-stage-table allocation-table--fill">
                            <thead>
                                <tr>
                                    <th scope="col">STT</th>
                                    <th scope="col">SBD</th>
                                    <th scope="col">Họ tên</th>
                                    <th scope="col">Hạng</th>
                                    <th scope="col">Lý thuyết</th>
                                    <th scope="col">Phòng / khu vực</th>
                                    <th scope="col">Thao tác</th>
                                </tr>
                            </thead>
                                <tbody>
                                    <c:set var="practicalIdx" value="0" />
                                    <c:forEach var="c" items="${sessionScope.candidateQueue}">
                                        <c:if test="${c.theoryPassed eq 'passed' and c.practicalPassed eq 'none' and not c.absent}">
                                            <c:set var="practicalIdx" value="${practicalIdx + 1}" />
                                            <tr class="allocation-stage-row">
                                                <td>${practicalIdx}</td>
                                                <td><strong>${c.sbd}</strong></td>
                                                <td>${c.name}</td>
                                                <td>${c.clazz}</td>
                                                <td><span class="allocation-score allocation-score--pass">${c.theoryScore}</span></td>
                                                <td>${empty c.allocatedAreaName ? '—' : c.allocatedAreaName}</td>
                                                <td>
                                                    <a href="allocation?action=submitPracticalScore&amp;id=${c.id}&amp;score=90" class="allocation-table-action allocation-table-action--practical">Chấm TH (Auto)</a>
                                                </td>
                                            </tr>
                                        </c:if>
                                    </c:forEach>
                                    <c:if test="${practicalCount eq 0}">
                                        <tr>
                                            <td colspan="7" class="allocation-stage-table__empty">Chưa có thí sinh nào thi đạt lý thuyết chờ thi Thực hành.</td>
                                        </tr>
                                    </c:if>
                                </tbody>
                            </table>
                    </div>
                </div>
            </div>
            
            <div class="allocation-pipeline-cell">

                <c:set var="roadCount" value="0" />
                <c:forEach var="c" items="${sessionScope.candidateQueue}">
                    <c:if test="${c.requiresRoadTest and c.practicalPassed eq 'passed' and c.roadTestPassed eq 'none' and not c.absent}">
                        <c:set var="roadCount" value="${roadCount + 1}" />
                    </c:if>
                </c:forEach>

                <div class="allocation-stage-panel allocation-stage-panel--road">
                    <div class="allocation-stage-panel__head">
                        <div class="allocation-stage-panel__title-wrap">
                            <h4 class="allocation-stage-panel__title">Thi đường trường</h4>
                            <p class="allocation-stage-panel__meta">Đường trường ngoài sân · Hạng B/B1/B2/C/D/E/F</p>
                        </div>
                        <span class="allocation-stage-panel__count">${roadCount} thí sinh</span>
                    </div>
                    <div class="table-responsive">
                        <table class="report-table allocation-stage-table allocation-table--fill">
                            <thead>
                                <tr>
                                    <th scope="col">STT</th>
                                    <th scope="col">SBD</th>
                                    <th scope="col">Họ tên</th>
                                    <th scope="col">Hạng</th>
                                    <th scope="col">Lý thuyết</th>
                                    <th scope="col">Thực hành</th>
                                    <th scope="col">Thao tác</th>
                                </tr>
                            </thead>
                                <tbody>
                                    <c:set var="roadIdx" value="0" />
                                    <c:forEach var="c" items="${sessionScope.candidateQueue}">
                                        <c:if test="${c.requiresRoadTest and c.practicalPassed eq 'passed' and c.roadTestPassed eq 'none' and not c.absent}">
                                            <c:set var="roadIdx" value="${roadIdx + 1}" />
                                            <tr class="allocation-stage-row">
                                                <td>${roadIdx}</td>
                                                <td><strong>${c.sbd}</strong></td>
                                                <td>${c.name}</td>
                                                <td>${c.clazz}</td>
                                                <td><span class="allocation-score allocation-score--pass">${c.theoryScore}</span></td>
                                                <td><span class="allocation-score allocation-score--pass">${c.practicalScore}</span></td>
                                                <td>
                                                    <a href="allocation?action=submitRoadScore&amp;id=${c.id}&amp;score=90" class="allocation-table-action allocation-table-action--road">Chấm ĐT (Auto)</a>
                                                </td>
                                            </tr>
                                        </c:if>
                                    </c:forEach>
                                    <c:if test="${roadCount eq 0}">
                                        <tr>
                                            <td colspan="7" class="allocation-stage-table__empty">Chưa có thí sinh nào đạt Thực hành chờ thi Đường trường.</td>
                                        </tr>
                                    </c:if>
                                </tbody>
                            </table>
                    </div>
                </div>
            </div>
            </div>
            
            <div class="allocation-pipeline-row allocation-pipeline-row--full">
                
                <c:set var="passCount" value="0" />
                <c:set var="failCount" value="0" />
                <c:forEach var="c" items="${sessionScope.candidateQueue}">
                    <c:if test="${c.examFinished}">
                        <c:choose>
                            <c:when test="${c.finalPass}">
                                <c:set var="passCount" value="${passCount + 1}" />
                            </c:when>
                            <c:otherwise>
                                <c:set var="failCount" value="${failCount + 1}" />
                            </c:otherwise>
                        </c:choose>
                    </c:if>
                </c:forEach>

                <div class="allocation-results-wrap">
                    <c:if test="${passCount eq 0 and failCount eq 0}">
                        <div class="allocation-results-empty">
                            Chưa có thí sinh nào hoàn thành ca sát hạch.
                        </div>
                    </c:if>

                    <c:if test="${passCount gt 0 or failCount gt 0}">
                    <div class="allocation-results-panel allocation-results-panel--pass">
                        <div class="allocation-results-panel__head">
                            <div class="allocation-stage-panel__title-wrap">
                                <h4 class="allocation-results-panel__title">Tổng hợp kết quả — Đỗ</h4>
                                <p class="allocation-stage-panel__meta">Hoàn thành ca sát hạch · Cấp GPLX</p>
                            </div>
                            <span class="allocation-results-panel__count">${passCount} thí sinh</span>
                        </div>
                        <div class="table-responsive">
                            <table class="report-table allocation-results-table allocation-table--fill">
                                <thead>
                                    <tr>
                                        <th scope="col">STT</th>
                                        <th scope="col">SBD</th>
                                        <th scope="col">Họ tên</th>
                                        <th scope="col">Hạng</th>
                                        <th scope="col">Lý thuyết</th>
                                        <th scope="col">Thực hành</th>
                                        <th scope="col">Đường trường</th>
                                    </tr>
                                </thead>
                                <tbody>
                                    <c:set var="passIdx" value="0" />
                                    <c:forEach var="c" items="${sessionScope.candidateQueue}">
                                        <c:if test="${c.examFinished and c.finalPass}">
                                            <c:set var="passIdx" value="${passIdx + 1}" />
                                            <tr class="allocation-result-row">
                                                <td>${passIdx}</td>
                                                <td><strong>${c.sbd}</strong></td>
                                                <td>${c.name}</td>
                                                <td>${c.clazz}</td>
                                                <td><span class="allocation-score allocation-score--pass">${c.theoryScore}</span></td>
                                                <td><span class="allocation-score allocation-score--pass">${c.practicalScore}</span></td>
                                                <td>
                                                    <c:choose>
                                                        <c:when test="${c.requiresRoadTest}">
                                                            <span class="allocation-score allocation-score--pass">${c.roadTestScore}</span>
                                                        </c:when>
                                                        <c:otherwise>—</c:otherwise>
                                                    </c:choose>
                                                </td>
                                            </tr>
                                        </c:if>
                                    </c:forEach>
                                    <c:if test="${passCount eq 0}">
                                        <tr>
                                            <td colspan="7" class="allocation-results-table__empty">Chưa có thí sinh đỗ.</td>
                                        </tr>
                                    </c:if>
                                </tbody>
                            </table>
                        </div>
                    </div>

                    <div class="allocation-results-panel allocation-results-panel--fail">
                        <div class="allocation-results-panel__head">
                            <div class="allocation-stage-panel__title-wrap">
                                <h4 class="allocation-results-panel__title">Tổng hợp kết quả — Trượt</h4>
                                <p class="allocation-stage-panel__meta">Hoàn thành ca sát hạch · Trượt hoặc vắng thi</p>
                            </div>
                            <span class="allocation-results-panel__count">${failCount} thí sinh</span>
                        </div>
                        <div class="table-responsive">
                            <table class="report-table allocation-results-table allocation-table--fill">
                                <thead>
                                    <tr>
                                        <th scope="col">STT</th>
                                        <th scope="col">SBD</th>
                                        <th scope="col">Họ tên</th>
                                        <th scope="col">Hạng</th>
                                        <th scope="col">Lý thuyết</th>
                                        <th scope="col">Thực hành</th>
                                        <th scope="col">Đường trường</th>
                                        <th scope="col">Lý do</th>
                                    </tr>
                                </thead>
                                <tbody>
                                    <c:set var="failIdx" value="0" />
                                    <c:forEach var="c" items="${sessionScope.candidateQueue}">
                                        <c:if test="${c.examFinished and not c.finalPass}">
                                            <c:set var="failIdx" value="${failIdx + 1}" />
                                            <tr class="allocation-result-row">
                                                <td>${failIdx}</td>
                                                <td><strong>${c.sbd}</strong></td>
                                                <td>${c.name}</td>
                                                <td>${c.clazz}</td>
                                                <td>
                                                    <c:choose>
                                                        <c:when test="${c.absent}">—</c:when>
                                                        <c:otherwise>
                                                            <span class="allocation-score allocation-score--${c.theoryPassed eq 'passed' ? 'pass' : 'fail'}">${c.theoryScore}</span>
                                                        </c:otherwise>
                                                    </c:choose>
                                                </td>
                                                <td>
                                                    <c:choose>
                                                        <c:when test="${c.absent or c.theoryPassed ne 'passed'}">—</c:when>
                                                        <c:otherwise>
                                                            <span class="allocation-score allocation-score--${c.practicalPassed eq 'passed' ? 'pass' : 'fail'}">${c.practicalScore}</span>
                                                        </c:otherwise>
                                                    </c:choose>
                                                </td>
                                                <td>
                                                    <c:choose>
                                                        <c:when test="${c.absent or not c.requiresRoadTest or c.practicalPassed ne 'passed'}">—</c:when>
                                                        <c:otherwise>
                                                            <span class="allocation-score allocation-score--${c.roadTestPassed eq 'passed' ? 'pass' : 'fail'}">${c.roadTestScore}</span>
                                                        </c:otherwise>
                                                    </c:choose>
                                                </td>
                                                <td>
                                                    <c:choose>
                                                        <c:when test="${c.absent}">
                                                            <span class="allocation-result-reason allocation-result-reason--absent">Vắng thi</span>
                                                        </c:when>
                                                        <c:otherwise>
                                                            <span class="allocation-result-reason allocation-result-reason--fail">Trượt sát hạch</span>
                                                        </c:otherwise>
                                                    </c:choose>
                                                </td>
                                            </tr>
                                        </c:if>
                                    </c:forEach>
                                    <c:if test="${failCount eq 0}">
                                        <tr>
                                            <td colspan="8" class="allocation-results-table__empty">Chưa có thí sinh trượt hoặc vắng.</td>
                                        </tr>
                                    </c:if>
                                </tbody>
                            </table>
                        </div>
                    </div>
                    </c:if>
                </div>
            </div>
            
        </div>
    </main>
    
    <jsp:include page="/views/layout/footer.jsp">
        <jsp:param name="standalone" value="false" />
    </jsp:include>
</div>

<script src="${pageContext.request.contextPath}/assets/js/allocation.js"></script>
</body>
</html>

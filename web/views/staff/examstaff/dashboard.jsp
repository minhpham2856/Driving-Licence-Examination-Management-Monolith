<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" language="java" %>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib prefix = "fn" uri = "http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix = "fmt" uri = "http://java.sun.com/jsp/jstl/fmt" %>

<jsp:include page="/views/staff/examstaff/includes/examstaff-layout-head.jsp">
    <jsp:param name="activeSidebar" value="dashboard" />
    <jsp:param name="pageTitle" value="Tổng quan kỳ thi" />
    <jsp:param name="noCache" value="true" />
    <jsp:param name="mainClass" value="examstaff-main--scroll" />
</jsp:include>

        <c:if test="${not empty requestScope.examSelectMsg}">
            <div class="examstaff-flash examstaff-flash--success">${requestScope.examSelectMsg}</div>
        </c:if>
        <c:if test="${not empty requestScope.examSelectError}">
            <div class="examstaff-flash examstaff-flash--error">${requestScope.examSelectError}</div>
        </c:if>

        <c:if test="${not empty requestScope.examControlMsg}">
            <div class="examstaff-flash examstaff-flash--success">${requestScope.examControlMsg}</div>
        </c:if>
        <c:if test="${not empty requestScope.examControlError}">
            <div class="examstaff-flash examstaff-flash--error">${requestScope.examControlError}</div>
        </c:if>

        <c:if test="${not empty currentExam}">
        <section class="report-pane dashboard-exam-panel" aria-label="Điều hành kỳ thi">
            <div class="report-pane__header dashboard-exam-panel__header">
                <h2 class="report-pane__title dashboard-exam-panel__title">Điều hành kỳ thi</h2>
            </div>
            <jsp:include page="/views/staff/examstaff/includes/exam-summary-line.jsp">
                <jsp:param name="showAllocatorLink" value="true" />
            </jsp:include>
            <jsp:include page="/views/staff/examstaff/includes/exam-shift-chip.jsp">
                <jsp:param name="redirect" value="dashboard" />
            </jsp:include>
        </section>
        </c:if>

        <c:set var="totalCandidatesCount" value="${fn:length(candidateQueue)}" />
        <c:set var="procedureDoneCount" value="0" />
        <c:set var="processingCount" value="0" />
        <c:set var="waitingCount" value="0" />
        <c:set var="examFinishedCount" value="0" />
        <c:set var="examPassedCount" value="0" />
        <c:set var="suspendedExamCount" value="0" />
        <c:set var="examOutcomeCount" value="0" />

        <c:forEach var="c" items="${candidateQueue}">
            <c:if test="${c.suspended}">
                <c:set var="suspendedExamCount" value="${suspendedExamCount + 1}" />
                <c:set var="examOutcomeCount" value="${examOutcomeCount + 1}" />
            </c:if>
            <c:set var="isExamFinished" value="${c.examFinished}" />
            <c:if test="${isExamFinished}">
                <c:set var="examFinishedCount" value="${examFinishedCount + 1}" />
                <c:set var="examOutcomeCount" value="${examOutcomeCount + 1}" />
                <c:if test="${c.finalPass}">
                    <c:set var="examPassedCount" value="${examPassedCount + 1}" />
                </c:if>
            </c:if>

            <c:set var="procedureComplete" value="${c.procedureComplete}" />
            <c:if test="${procedureComplete}">
                <c:set var="procedureDoneCount" value="${procedureDoneCount + 1}" />
            </c:if>
            <c:if test="${not procedureComplete and sessionScope.callingSbd eq c.sbd}">
                <c:set var="processingCount" value="${processingCount + 1}" />
            </c:if>
            <c:if test="${not procedureComplete and not c.suspended and sessionScope.callingSbd ne c.sbd and not isExamFinished and not c.absent}">
                <c:set var="waitingCount" value="${waitingCount + 1}" />
            </c:if>
        </c:forEach>

        <c:set var="completedCount" value="${procedureDoneCount}" />
        <c:set var="pendingCount" value="${waitingCount}" />

        <c:set var="completedPercent" value="${totalCandidatesCount gt 0 ? (completedCount * 100.0) / totalCandidatesCount : 0.0}" />
        <c:set var="processingPercent" value="${totalCandidatesCount gt 0 ? (processingCount * 100.0) / totalCandidatesCount : 0.0}" />
        <c:set var="pendingPercent" value="${totalCandidatesCount gt 0 ? (pendingCount * 100.0) / totalCandidatesCount : 0.0}" />

        <c:if test="${not empty currentExam and totalCandidatesCount == 0}">
            <div class="examstaff-flash examstaff-flash--warning">
                Kỳ thi đang chọn chưa có thí sinh đăng ký. Chọn kỳ thi khác ở sidebar.
            </div>
        </c:if>

        <section class="metrics-row dashboard-metrics-row" aria-label="Chỉ số kỳ thi">
            <div class="stat-card">
                <div class="stat-icon stat-icon--blue">
                    <span class="material-symbols-outlined" aria-hidden="true">event</span>
                </div>
                <div class="stat-info">
                    <span class="stat-number" style="font-size: 1.05rem; font-weight: 800; color: #0f172a; margin-bottom: 0.15rem;">
                        Kỳ thi hạng ${not empty currentExam ? currentExam.licenseCode : '—'}
                    </span>
                    <span class="stat-label">
                        <c:if test="${not empty currentExam and not empty currentExam.examDate}">
                            <fmt:formatDate value="${currentExam.examDate}" pattern="dd/MM/yyyy"/>
                        </c:if>
                        <c:if test="${empty currentExam or empty currentExam.examDate}">—</c:if>
                        <c:if test="${not empty currentExam}">
                            — ${currentExam.status}
                        </c:if>
                    </span>
                </div>
            </div>

            <div class="stat-card">
                <div class="stat-icon stat-icon--blue">
                    <span class="material-symbols-outlined" aria-hidden="true">groups</span>
                </div>
                <div class="stat-info">
                    <span class="stat-number">${totalCandidatesCount}</span>
                    <span class="stat-label">Tổng thí sinh kỳ thi</span>
                </div>
            </div>

            <div class="stat-card">
                <div class="stat-icon stat-icon--green">
                    <span class="material-symbols-outlined" aria-hidden="true">task_alt</span>
                </div>
                <div class="stat-info">
                    <span class="stat-number" style="color: #10b981;">${completedCount}</span>
                    <span class="stat-label">Đã xong thủ tục</span>
                </div>
            </div>

            <div class="stat-card">
                <div class="stat-icon stat-icon--amber">
                    <span class="material-symbols-outlined" aria-hidden="true">hourglass_top</span>
                </div>
                <div class="stat-info">
                    <span class="stat-number" style="color: #d97706;">${waitingCount}</span>
                    <span class="stat-label">Đang ở phòng chờ</span>
                </div>
            </div>

            <div class="stat-card">
                <div class="stat-icon stat-icon--green">
                    <span class="material-symbols-outlined" aria-hidden="true">emoji_events</span>
                </div>
                <div class="stat-info">
                    <span class="stat-number" style="color: #10b981;">${examPassedCount}<span style="font-size: 0.85rem; color: #64748b;"> / ${examOutcomeCount}</span></span>
                    <span class="stat-label">Kết quả thi đạt</span>
                </div>
            </div>
        </section>

        <div class="report-pane" style="margin-top: 1.5rem;">
            <div class="grading-pane__header" style="border-bottom: none; padding-bottom: 0; margin-bottom: 0.5rem;">
                <h2 class="grading-pane__title" style="font-size: 1.05rem; display: inline-flex; align-items: center; gap: 8px;">
                    <svg width="20" height="20" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg" style="color: #0052cc;">
                        <path d="M12 20h9M3 20v-8a2 2 0 0 1 2-2h4a2 2 0 0 1 2 2v8M13 20V4a2 2 0 0 1 2-2h4a2 2 0 0 1 2 2v16" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                    </svg>
                    Tiến độ thủ tục &amp; kết quả thi
                </h2>
            </div>

            <div class="progress-indicator-bar">
                <div class="progress-indicator-segment progress-indicator-segment--success" style="width: ${completedPercent}%" title="Xong thủ tục: ${procedureDoneCount}"></div>
                <div class="progress-indicator-segment progress-indicator-segment--info" style="width: ${processingPercent}%" title="Đang thủ tục: ${processingCount}"></div>
                <div class="progress-indicator-segment progress-indicator-segment--pending" style="width: ${pendingPercent}%" title="Phòng chờ: ${waitingCount}"></div>
            </div>

            <div class="progress-legend">
                <div class="progress-legend-item">
                    <span class="progress-legend-dot" style="background-color: #10b981;"></span>
                    <span>Xong thủ tục (ảnh + lệ phí): <strong>${procedureDoneCount}</strong></span>
                </div>
                <div class="progress-legend-item">
                    <span class="progress-legend-dot" style="background-color: #3b82f6;"></span>
                    <span>Đang tại quầy thủ tục: <strong>${processingCount}</strong></span>
                </div>
                <div class="progress-legend-item">
                    <span class="progress-legend-dot" style="background-color: #f59e0b;"></span>
                    <span>Đang ở phòng chờ: <strong>${waitingCount}</strong></span>
                </div>
                <div class="progress-legend-item">
                    <span class="progress-legend-dot" style="background-color: #7c3aed;"></span>
                    <span>Đã có kết quả: <strong>${examOutcomeCount}</strong> (Đạt: ${examPassedCount})</span>
                </div>
            </div>
        </div>

        <div class="room-monitor-grid">

            <div class="room-monitor-card">
                <div class="room-header">
                    <h3 class="room-title">
                        <svg width="20" height="20" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg" style="color: #ea580c;">
                            <path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2M9 11a4 4 0 1 0 0-8 4 4 0 0 0 0 8z" stroke="currentColor" stroke-width="2"/>
                            <path d="M23 21v-2a4 4 0 0 0-3-3.87M16 3.13a4 4 0 0 1 0 7.75" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
                        </svg>
                        Phòng chờ chính
                    </h3>
                    <span class="room-badge room-badge--orange">Chờ gọi (${waitingCount})</span>
                </div>

                <div class="room-candidate-list">
                    <c:set var="waitRenderCount" value="0" />
                    <c:forEach var="c" items="${candidateQueue}">
                        <c:if test="${not c.procedureComplete and not c.suspended and sessionScope.callingSbd ne c.sbd and not c.examFinished and not c.absent and waitRenderCount lt 6}">
                            <c:set var="waitRenderCount" value="${waitRenderCount + 1}" />
                            <div class="room-candidate-item">
                                <div class="candidate-meta">
                                    <span class="candidate-sbd">SBD: ${c.sbd}</span>
                                    <span class="candidate-step candidate-step--waiting">Hạng ${c.clazz}</span>
                                </div>
                                <div class="candidate-name">${c.name}</div>
                                <div style="font-size: 0.72rem; color: #64748b; display: flex; justify-content: space-between; align-items: center; margin-top: 2px;">
                                    <span>Trạng thái: Đang chờ</span>
                                    <span style="font-weight: 600; color: #475569;">SĐT: ${c.phoneNo}</span>
                                </div>
                            </div>
                        </c:if>
                    </c:forEach>

                    <c:if test="${waitRenderCount eq 0}">
                        <div class="empty-room-state">
                            Không có thí sinh nào đang chờ ở phòng chờ.
                        </div>
                    </c:if>
                </div>

                <a href="candidatecall" class="room-monitor-card__action hover-elevate">
                    Xem phòng điều hành gọi thi &rarr;
                </a>
            </div>

            <div class="room-monitor-card">
                <div class="room-header">
                    <h3 class="room-title">
                        <svg width="20" height="20" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg" style="color: #2563eb;">
                            <rect x="3" y="4" width="18" height="12" rx="2" stroke="currentColor" stroke-width="2"/>
                            <path d="M12 20h.01M16 20h.01M8 20h.01M12 16v4" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
                        </svg>
                        Thủ tục
                    </h3>
                    <span class="room-badge room-badge--blue">Đang xử lý</span>
                </div>

                <div class="room-candidate-list">
                    <c:set var="activeCalledCount" value="0" />
                    <c:forEach var="c" items="${candidateQueue}">
                        <c:if test="${sessionScope.callingSbd eq c.sbd and not c.procedureComplete and activeCalledCount lt 3}">
                            <c:set var="activeCalledCount" value="${activeCalledCount + 1}" />
                            <div class="room-candidate-item" style="border-left: 3px solid #2563eb;">
                                <div class="candidate-meta">
                                    <span style="font-weight: 700; color: #1e293b;">SBD: ${c.sbd}</span>
                                    <span class="candidate-step candidate-step--payment">Đang ở quầy thủ tục</span>
                                </div>
                                <div style="display: flex; justify-content: space-between; align-items: center; margin-top: 4px;">
                                    <span class="candidate-name">${c.name}</span>
                                    <span style="font-size: 0.72rem; color: #64748b;">Hạng ${c.clazz}</span>
                                </div>
                            </div>
                        </c:if>
                    </c:forEach>

                    <c:if test="${activeCalledCount eq 0}">
                        <div class="empty-room-state">
                            Bàn làm thủ tục đang trống.
                        </div>
                    </c:if>
                </div>

                <a href="procedure" class="room-monitor-card__action hover-elevate">
                    Vào quầy làm thủ tục &rarr;
                </a>
            </div>

            <div class="room-monitor-card">
                <div class="room-header">
                    <h3 class="room-title">
                        <svg width="20" height="20" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg" style="color: #10b981;">
                            <path d="M22 11.08V12a10 10 0 1 1-5.93-9.14" stroke="currentColor" stroke-width="2"/>
                            <path d="M22 4L12 14.01l-3-3" stroke="currentColor" stroke-width="2"/>
                        </svg>
                        Kết quả thi cuối cùng
                    </h3>
                    <span class="room-badge room-badge--green">${examOutcomeCount} thí sinh</span>
                </div>

                <div class="examiner-table-wrap">
                    <table class="examiner-table allocation-results-table allocation-table--fill" style="font-size: 0.78rem;">
                        <thead>
                            <tr>
                                <th style="text-align: left;">SBD</th>
                                <th style="text-align: left;">Họ tên</th>
                                <th style="text-align: center;">Kết quả</th>
                            </tr>
                        </thead>
                        <tbody>
                            <c:set var="resultRenderCount" value="0" />
                            <c:forEach var="c" items="${candidateQueue}">
                                <c:if test="${c.examFinished or c.suspended}">
                                    <c:set var="resultRenderCount" value="${resultRenderCount + 1}" />
                                    <c:set var="finalPass" value="${c.finalPass}" />
                                    <tr>
                                        <td style="font-weight: 800; color: #0052cc; font-family: monospace;">${c.sbd}</td>
                                        <td style="font-weight: 600; color: #0f172a;">${c.name}</td>
                                        <td style="text-align: center;">
                                            <c:choose>
                                                <c:when test="${c.suspended}">
                                                    <span style="font-weight: 800; color: #b91c1c; background: #fef2f2; padding: 2px 8px; border-radius: 4px;">ĐÌNH CHỈ</span>
                                                </c:when>
                                                <c:when test="${c.absent}">
                                                    <span style="font-weight: 800; color: #ef4444; background: #fef2f2; padding: 2px 8px; border-radius: 4px;">VẮNG</span>
                                                </c:when>
                                                <c:when test="${finalPass}">
                                                    <span style="font-weight: 800; color: #047857; background: #ecfdf5; padding: 2px 8px; border-radius: 4px;">ĐẠT</span>
                                                </c:when>
                                                <c:otherwise>
                                                    <span style="font-weight: 800; color: #991b1b; background: #fee2e2; padding: 2px 8px; border-radius: 4px;">TRƯỢT</span>
                                                </c:otherwise>
                                            </c:choose>
                                        </td>
                                    </tr>
                                </c:if>
                            </c:forEach>
                            <c:if test="${resultRenderCount eq 0}">
                                <tr>
                                    <td colspan="3" class="allocation-results-table__empty">
                                        Chưa có thí sinh nào hoàn thành toàn bộ kỳ thi.
                                    </td>
                                </tr>
                            </c:if>
                        </tbody>
                    </table>
                </div>

                <a href="${pageContext.request.contextPath}/views/staff/examstaff/report" class="room-monitor-card__action hover-elevate">
                    Xem báo cáo chi tiết &rarr;
                </a>
            </div>

        </div>

<jsp:include page="/views/staff/examstaff/includes/examstaff-layout-foot.jsp" />

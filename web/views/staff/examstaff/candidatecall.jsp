<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" language="java" %>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib prefix = "fn" uri = "http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix = "fmt" uri = "http://java.sun.com/jsp/jstl/fmt" %>

<jsp:include page="/views/staff/examstaff/includes/examstaff-layout-head.jsp">
    <jsp:param name="activeSidebar" value="goi-thi" />
    <jsp:param name="pageTitle" value="${requestScope.deskMode ? 'Bàn làm thủ tục' : 'Gọi làm thủ tục'}" />
    <jsp:param name="sectionTitle" value="Gọi làm thủ tục" />
    <jsp:param name="sectionUrl" value="${pageContext.request.contextPath}/examstaff/candidatecall" />
    <jsp:param name="mainClass" value="examstaff-main--scroll" />
</jsp:include>

        <header class="page-header page-header--toolbar">
            <div class="call-page-actions">
                <c:if test="${sessionScope.shiftEnded ne 'true' and sessionScope.shiftPaused ne 'true'}">
                    <a href="candidatecall?action=pauseShift" class="call-toolbar-btn call-toolbar-btn--warn"
                       onclick="return confirm('Tạm dừng gọi số? Hàng đợi thí sinh chưa làm thủ tục sẽ được giữ nguyên và không bị đánh vắng. Kỳ thi vẫn diễn ra bình thường, sát hạch viên vẫn đăng nhập được.');">
                        <svg width="16" height="16" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                            <rect x="6" y="4" width="4" height="16" rx="1" fill="currentColor"/>
                            <rect x="14" y="4" width="4" height="16" rx="1" fill="currentColor"/>
                        </svg>
                        Tạm dừng gọi số
                    </a>
                    <a href="candidatecall?action=closeExam" class="call-toolbar-btn call-toolbar-btn--danger"
                       onclick="return confirm('Dừng gọi số? Tất cả thí sinh chưa làm thủ tục trong hàng đợi sẽ bị đánh vắng.');">
                        <svg width="16" height="16" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                            <rect x="3" y="3" width="18" height="18" rx="2" stroke="currentColor" stroke-width="2"/>
                            <path d="M9 9h6v6H9z" fill="currentColor"/>
                        </svg>
                        Dừng gọi số
                    </a>
                </c:if>
            </div>
        </header>

        <c:if test="${not empty sessionScope.examControlMsg}">
            <div class="examstaff-flash examstaff-flash--success">${sessionScope.examControlMsg}</div>
            <c:remove var="examControlMsg" scope="session"/>
        </c:if>
        <c:if test="${not empty sessionScope.examControlError}">
            <div class="examstaff-flash examstaff-flash--error">${sessionScope.examControlError}</div>
            <c:remove var="examControlError" scope="session"/>
        </c:if>
        <c:if test="${not empty requestScope.examLockedMsg}">
            <div class="examstaff-flash examstaff-flash--error">${requestScope.examLockedMsg}</div>
        </c:if>
        <c:if test="${requestScope.examMutationsLocked}">
            <div class="examstaff-flash examstaff-flash--error">
                Kỳ thi đã kết thúc. Không thể xóa/sửa hồ sơ thủ tục, đình chỉ hoặc hoàn tác đình chỉ.
            </div>
        </c:if>

        <nav class="call-subnav" aria-label="Điều hướng gọi thủ tục">
            <a href="candidatecall" class="call-subnav__link is-active">Gọi thủ tục</a>
            <a href="candidatecall?view=suspended" class="call-subnav__link">
                Đình chỉ thi
                <c:if test="${requestScope.suspendedCount > 0}">
                    <span class="call-toolbar-badge">${requestScope.suspendedCount}</span>
                </c:if>
            </a>
        </nav>

        <c:if test="${not empty requestScope.absentAlert}">
            <div style="background-color: #fef2f2; border: 1px solid #fecaca; border-radius: 8px; padding: 10px 12px; margin-bottom: 1.25rem; display: flex; gap: 8px; align-items: center;">
                <svg width="18" height="18" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg" style="color: #ef4444; flex-shrink: 0;">
                    <circle cx="12" cy="12" r="10" stroke="currentColor" stroke-width="2"/>
                    <path d="M12 8v4M12 16h.01" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                </svg>
                <span style="font-size: 0.82rem; font-weight: 600; color: #b91c1c;">
                    Đã đánh dấu thí sinh <strong style="color: #7f1d1d;">${requestScope.absentAlert}</strong> vắng mặt! Hệ thống đã xếp người này xuống cuối danh sách chờ và tự động gọi thí sinh kế tiếp.
                </span>
            </div>
        </c:if>

        <c:if test="${not empty requestScope.permanentAbsentAlert}">
            <div style="background-color: #fef2f2; border: 1px solid #ef4444; border-radius: 8px; padding: 10px 12px; margin-bottom: 1.25rem; display: flex; gap: 8px; align-items: center;" class="animated shake">
                <svg width="18" height="18" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg" style="color: #ef4444; flex-shrink: 0;">
                    <circle cx="12" cy="12" r="10" stroke="currentColor" stroke-width="2"/>
                    <path d="M12 8v4M12 16h.01" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                </svg>
                <span style="font-size: 0.82rem; font-weight: 600; color: #b91c1c;">
                    Đã xác nhận thí sinh <strong style="color: #7f1d1d;">${requestScope.permanentAbsentAlert}</strong> vắng thi! Kết quả thi của thí sinh được ghi nhận là TRƯỢT và khóa hồ sơ.
                </span>
            </div>
        </c:if>

        <c:if test="${not empty requestScope.undoAlert}">
            <div style="background-color: #ecfdf5; border: 1px solid #10b981; border-radius: 8px; padding: 10px 12px; margin-bottom: 1.25rem; display: flex; gap: 8px; align-items: center;" class="animated bounceIn">
                <svg width="18" height="18" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg" style="color: #10b981; flex-shrink: 0;">
                    <circle cx="12" cy="12" r="10" stroke="currentColor" stroke-width="2"/>
                    <path d="M8 12l3 3 5-5" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                </svg>
                <span style="font-size: 0.82rem; font-weight: 600; color: #065f46;">
                    Đã hoàn tác trạng thái vắng mặt của thí sinh <strong style="color: #047857;">${requestScope.undoAlert}</strong> thành công! Đưa thí sinh trở về đầu hàng đợi để tiếp tục gọi.
                </span>
            </div>
        </c:if>

        <c:if test="${not empty param.procedureReset}">
            <div style="background-color: #eff6ff; border: 1px solid #93c5fd; border-radius: 8px; padding: 10px 12px; margin-bottom: 1.25rem; font-size: 0.82rem; font-weight: 600; color: #1e40af;">
                Đã xóa hồ sơ thủ tục của <strong>${param.procedureReset}</strong>. Thí sinh có thể làm lại thủ tục từ bước 1.
            </div>
        </c:if>

        <div class="report-grid" style="grid-template-columns: 1.32fr 1.68fr; gap: 1.5rem; display: grid;">

            <div style="display: flex; flex-direction: column; gap: 1.25rem;">

                <c:choose>
                    <c:when test="${sessionScope.shiftEnded eq 'true'}">
                        <div class="waiting-list-pane" style="text-align: center; padding: 3rem 1.5rem; display: flex; flex-direction: column; align-items: center; justify-content: center; gap: 12px;">
                            <div style="background-color: #fee2e2; border-radius: 50%; width: 64px; height: 64px; display: flex; align-items: center; justify-content: center; color: #ef4444; border: 1px solid rgba(239, 68, 68, 0.2);">
                                <svg width="32" height="32" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                                    <path d="M12 2C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2zm1 15h-2v-2h2v2zm0-4h-2V7h2v6z" fill="currentColor"/>
                                </svg>
                            </div>
                            <h3 style="margin: 0; font-size: 1.2rem; font-weight: 800; color: #991b1b;">Đã dừng gọi số</h3>
                            <p style="margin: 0; font-size: 0.85rem; color: #64748b; max-width: 320px; line-height: 1.5;">Hàng đợi gọi thủ tục đã dừng. Các thí sinh chưa làm thủ tục đã được đánh vắng theo quy định.</p>

                            <a href="candidatecall?action=startShift" class="btn-batch" style="background: linear-gradient(135deg, #0052cc, #003d9b); border: none; font-size: 0.88rem; height: 42px; margin-top: 1rem; width: auto; padding: 0 1.5rem;">
                                Mở lại hàng đợi gọi số
                            </a>
                        </div>
                    </c:when>

                    <c:when test="${sessionScope.shiftPaused eq 'true'}">
                        <div class="waiting-list-pane" style="text-align: center; padding: 3rem 1.5rem; display: flex; flex-direction: column; align-items: center; justify-content: center; gap: 12px;">
                            <div style="background-color: #fef3c7; border-radius: 50%; width: 64px; height: 64px; display: flex; align-items: center; justify-content: center; color: #d97706; border: 1px solid rgba(217, 119, 6, 0.25);">
                                <svg width="32" height="32" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                                    <rect x="7" y="5" width="4" height="14" rx="1" fill="currentColor"/>
                                    <rect x="13" y="5" width="4" height="14" rx="1" fill="currentColor"/>
                                </svg>
                            </div>
                            <h3 style="margin: 0; font-size: 1.2rem; font-weight: 800; color: #92400e;">Đã tạm dừng gọi số</h3>
                            <p style="margin: 0; font-size: 0.85rem; color: #64748b; max-width: 320px; line-height: 1.5;">Đã dừng gọi loa và bàn thủ tục. Hàng đợi được giữ nguyên. Kỳ thi vẫn diễn ra — sát hạch viên vẫn đăng nhập và chấm thi bình thường. Nhấn nút bên dưới để tiếp tục gọi thí sinh đầu hàng.</p>

                            <a href="candidatecall?action=startCall" class="btn-batch" style="background: linear-gradient(135deg, #10b981, #059669); border: none; font-size: 0.88rem; height: 42px; margin-top: 1rem; width: auto; padding: 0 1.5rem;">
                                Bắt đầu gọi thi (Tự động)
                            </a>
                        </div>
                    </c:when>

                    <c:otherwise>
                        <div class="waiting-list-pane">
                            <h3 class="called-status-title">
                                <svg width="20" height="20" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg" style="color: #0052cc;">
                                    <path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2M9 11a4 4 0 1 0 0-8 4 4 0 0 0 0 8z" stroke="currentColor" stroke-width="2"/>
                                    <path d="M19 8v6M16 11h6" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
                                </svg>
                                Bảng điều hành loa gọi thi
                            </h3>

                            <c:choose>
                                <c:when test="${empty sessionScope.callingSbd}">
                                    <div style="text-align: center; padding: 2rem 1rem; background-color: #f8fafc; border: 1px dashed #cbd5e1; border-radius: 12px; margin-bottom: 1.25rem;">
                                        <svg width="36" height="36" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg" style="color: #94a3b8; margin: 0 auto 0.5rem; display: block;">
                                            <path d="M12 2C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2zm-1 14H9V8h2v8zm4 0h-2V8h2v8z" fill="currentColor"/>
                                        </svg>
                                        <span style="font-weight: 700; font-size: 0.85rem; color: #475569; display: block; text-transform: uppercase; margin-bottom: 4px;">Hàng đợi đang dừng gọi</span>
                                        <span style="font-size: 0.78rem; color: #64748b;">Nhấn Bắt đầu gọi bên dưới để tự động gọi người đứng đầu hàng đợi.</span>
                                    </div>

                                    <c:choose>
                                        <c:when test="${empty candidateQueue}">
                                            <button class="btn-batch" style="background-color: #e2e8f0; border-color: #cbd5e1; color: #94a3b8; cursor: not-allowed; font-size: 0.9rem;" disabled>
                                                Hàng đợi trống - Không thể gọi
                                            </button>
                                        </c:when>
                                        <c:otherwise>
                                            <a href="candidatecall?action=startCall" class="btn-batch" style="background: linear-gradient(135deg, #10b981, #059669); border: none; font-size: 0.92rem; height: 46px;">
                                                <svg width="18" height="18" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                                                    <path d="M12 2C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2zm-2 14.5v-9l6 4.5-6 4.5z" fill="currentColor"/>
                                                </svg>
                                                Bắt đầu gọi thi (Tự động)
                                            </a>
                                        </c:otherwise>
                                    </c:choose>
                                </c:when>

                                <c:otherwise>
                                    <div class="active-calling-card">
                                        <span style="font-size: 0.72rem; font-weight: 800; color: #0052cc; text-transform: uppercase; letter-spacing: 0.05em; display: block; margin-bottom: 6px;">Học viên đang gọi lên bàn:</span>
                                        <div style="font-family: monospace; font-size: 2.25rem; font-weight: 900; color: #0f172a; letter-spacing: 0.02em; line-height: 1.1;">
                                            ${callingCandidate.sbd}
                                        </div>
                                        <div style="font-size: 1.15rem; font-weight: 800; color: #1e293b; margin-top: 4px;">
                                            ${callingCandidate.name}
                                        </div>
                                        <div style="display: flex; gap: 8px; align-items: center; margin-top: 8px;">
                                            <span class="role-badge role-badge--coi" style="font-size: 0.72rem; padding: 2px 8px;">Hạng ${callingCandidate.clazz}</span>
                                            <span style="font-size: 0.75rem; color: #64748b; font-family: monospace;">Căn cước: ${callingCandidate.cccd}</span>
                                        </div>

                                        <p style="margin: 0.75rem 0 0; font-size: 0.78rem; color: #64748b; line-height: 1.45;">
                                            Loa gọi tên phát trên
                                            <c:choose>
                                                <c:when test="${not empty sessionScope.selectedExamId}">
                                                    <a href="${pageContext.request.contextPath}/examstaff/public-call?examId=${sessionScope.selectedExamId}"
                                                       target="_blank" rel="noopener"
                                                       style="font-weight: 700; color: #0052cc; text-decoration: none;">màn hình TV</a>.
                                                </c:when>
                                                <c:otherwise>
                                                    <span style="font-weight: 700; color: #94a3b8;">màn hình TV (chưa chọn ca)</span>.
                                                </c:otherwise>
                                            </c:choose>
                                        </p>

                                        <c:if test="${not empty requestScope.nextCallingCandidate}">
                                            <div style="margin-top: 1rem; padding: 10px 12px; background: rgba(16, 185, 129, 0.06); border: 1px solid rgba(16, 185, 129, 0.2); border-radius: 10px; text-align: left; width: 100%;">
                                                <span style="font-size: 0.68rem; font-weight: 800; color: #047857; text-transform: uppercase; letter-spacing: 0.05em;">Chuẩn bị tiếp theo (hiển thị TV)</span>
                                                <div style="margin-top: 4px; font-family: monospace; font-weight: 800; color: #059669; font-size: 1rem;">${requestScope.nextCallingCandidate.sbd}</div>
                                                <div style="font-size: 0.85rem; font-weight: 700; color: #1e293b;">${requestScope.nextCallingCandidate.name} &mdash; Hạng ${requestScope.nextCallingCandidate.clazz}</div>
                                            </div>
                                        </c:if>

                                        <p style="margin: 1.25rem 0 0; font-size: 0.72rem; color: #64748b; line-height: 1.45; text-align: left; width: 100%;">
                                            Nếu thí sinh không trình diện, bấm <strong>Vắng</strong> để đẩy xuống cuối hàng đợi và gọi người tiếp theo.
                                        </p>

                                        <div style="display: flex; flex-direction: column; gap: 8px; margin-top: 1rem; border-top: 1px solid #e2e8f0; padding-top: 1rem;">
                                            <a href="procedure?sbd=${callingCandidate.sbd}#procedure-desk" class="btn-batch" style="background-color: #0052cc; border-color: #0052cc; height: 40px; font-size: 0.85rem;">
                                                Tiến hành lập hồ sơ &rarr;
                                            </a>

                                            <div style="display: flex; gap: 8px; width: 100%;">
                                                <c:if test="${not requestScope.examMutationsLocked}">
                                                <a href="candidatecall?action=absent&amp;sbd=${callingCandidate.sbd}" class="btn-batch btn-batch--alt" style="flex: 1; height: 38px; border-color: rgba(245, 158, 11, 0.3); color: #d97706; background: rgba(245, 158, 11, 0.01); font-size: 0.8rem;" title="Đẩy xuống cuối hàng đợi">
                                                    Vắng
                                                </a>
                                                <a href="candidatecall?action=permanentAbsent&amp;sbd=${callingCandidate.sbd}" class="btn-batch btn-batch--alt" style="flex: 1; height: 38px; border-color: rgba(239, 68, 68, 0.25); color: #dc2626; background: rgba(239, 68, 68, 0.04); font-size: 0.8rem;" title="Đình chỉ thi"
                                                   onclick="return confirm('Đình chỉ thí sinh ${callingCandidate.sbd}? Thí sinh sẽ bị loại khỏi kỳ thi và không được gọi lại.');">
                                                    Đình chỉ
                                                </a>
                                                </c:if>
                                            </div>
                                        </div>
                                    </div>
                                </c:otherwise>
                            </c:choose>
                        </div>
                    </c:otherwise>
                </c:choose>

            </div>

            <div class="waiting-list-pane">
                <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 1.25rem; border-bottom: 1px solid #f1f5f9; padding-bottom: 0.75rem;">
                    <div style="display: flex; align-items: center; gap: 8px;">
                        <h3 class="called-status-title" style="margin: 0; font-size: 0.95rem;">
                            <svg width="20" height="20" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg" style="color: #10b981;">
                                <rect x="3" y="4" width="18" height="16" rx="2" stroke="currentColor" stroke-width="2"/>
                                <path d="M7 8h10M7 12h10M7 16h6" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
                            </svg>
                            Hàng đợi thí sinh đang chờ sát hạch
                        </h3>
                        <c:if test="${sessionScope.shiftEnded ne 'true'}">
                            <c:set var="pendingCount" value="${fn:length(activeCallQueue)}" />
                            <span style="background: rgba(16, 185, 129, 0.1); color: #047857; border: 1px solid rgba(16, 185, 129, 0.2); font-size: 0.65rem; font-weight: 800; padding: 2px 6px; border-radius: 4px;">
                                ${pendingCount} Người
                            </span>
                        </c:if>
                    </div>
                </div>

                <c:choose>
                    <c:when test="${sessionScope.shiftEnded eq 'true' or pendingCount eq 0}">
                        <div style="text-align: center; padding: 5rem 1rem; color: #94a3b8; display: flex; flex-direction: column; align-items: center; gap: 8px;">
                            <svg width="40" height="40" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg" style="opacity: 0.4;">
                                <circle cx="12" cy="12" r="10" stroke="currentColor" stroke-width="2"/>
                                <path d="M8 12h8" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
                            </svg>
                            <span style="font-weight: 700; font-size: 0.9rem; color: #475569;">Hàng đợi trống</span>
                            <span style="font-size: 0.78rem; max-width: 250px;">Không có thí sinh nào trong hàng đợi của kỳ thi này.</span>
                        </div>
                    </c:when>

                    <c:otherwise>
                        <div class="table-responsive" style="max-height: 480px; overflow-y: auto;">
                            <table class="audit-table" style="font-size: 0.85rem; width: 100%;">
                                <thead>
                                    <tr>
                                        <th scope="col" style="width: 80px;">SBD</th>
                                        <th scope="col">Họ tên</th>
                                        <th scope="col" style="width: 60px; text-align: center;">Hạng</th>
                                        <th scope="col" style="width: 110px; text-align: center;">Căn cước</th>
                                        <th scope="col" style="width: 200px; text-align: right;">Hành động</th>
                                    </tr>
                                </thead>
                                <tbody>
                                    <c:forEach var="candidate" items="${activeCallQueue}" varStatus="status">
                                            <c:set var="isCurrentCalling" value="${candidate.sbd eq sessionScope.callingSbd}" />
                                            <tr style="${isCurrentCalling ? 'background-color: rgba(0, 82, 204, 0.04); border-left: 3px solid #0052cc;' : ''}">
                                                <td style="font-weight: 800; color: #0052cc; font-family: monospace; padding-left: ${isCurrentCalling ? '8px' : '0px'};">
                                                    ${candidate.sbd}
                                                    <c:if test="${isCurrentCalling}">
                                                        <span style="background: #10b981; width: 6px; height: 6px; border-radius: 50%; display: inline-block; margin-left: 4px;" title="Đang gọi"></span>
                                                    </c:if>
                                                </td>
                                                <td style="font-weight: 700; color: #0f172a;">${candidate.name}</td>
                                                <td style="text-align: center;"><span class="role-badge role-badge--coi" style="font-size: 0.65rem; padding: 1px 4px;">${candidate.clazz}</span></td>
                                                <td style="text-align: center; font-family: monospace; color: #475569;">${candidate.cccd}</td>
                                                <td style="text-align: right;">
                                                    <div style="display: inline-flex; gap: 4px; flex-wrap: wrap; justify-content: flex-end;">
                                                        <a href="procedure?sbd=${candidate.sbd}#procedure-desk" class="btn-filter" style="height: 26px; padding: 0 8px; font-size: 0.7rem; border-radius: 6px; text-decoration: none; display: inline-flex; align-items: center;">Hồ sơ</a>
                                                        <c:if test="${not requestScope.examMutationsLocked}">
                                                        <a href="candidatecall?action=absent&amp;sbd=${candidate.sbd}" class="btn-reset" style="height: 26px; padding: 0 8px; font-size: 0.7rem; border-radius: 6px; text-decoration: none; display: inline-flex; align-items: center; color: #d97706; border-color: rgba(245, 158, 11, 0.3); background: rgba(245, 158, 11, 0.02);" title="Đẩy xuống cuối hàng chờ">Vắng</a>
                                                        <a href="candidatecall?action=permanentAbsent&amp;sbd=${candidate.sbd}" class="btn-reset" style="height: 26px; padding: 0 8px; font-size: 0.7rem; border-radius: 6px; text-decoration: none; display: inline-flex; align-items: center; color: #dc2626; border-color: rgba(239, 68, 68, 0.25); background: rgba(239, 68, 68, 0.04);" title="Đình chỉ thi"
                                                           onclick="return confirm('Đình chỉ thí sinh ${candidate.sbd}? Thí sinh sẽ bị loại khỏi kỳ thi và không được gọi lại.');">Đình chỉ</a>
                                                        </c:if>
                                                    </div>
                                                </td>
                                            </tr>
                                    </c:forEach>
                                </tbody>
                            </table>
                        </div>
                    </c:otherwise>
                </c:choose>

                <c:set var="doneCount" value="${fn:length(requestScope.procedureDoneCandidates)}" />
                <c:if test="${doneCount > 0}">
                    <div class="procedure-done-panel">
                        <div class="procedure-done-panel__header">
                            <h4 class="procedure-done-panel__title">Đã xong thủ tục (${doneCount})</h4>
                            <p class="procedure-done-panel__hint">Sửa hồ sơ, in phiếu xác nhận, đình chỉ hoặc xóa để làm lại thủ tục.</p>
                        </div>
                        <div class="procedure-done-panel__toolbar">
                            <form method="GET" action="candidatecall" class="procedure-done-search-form">
                                <c:if test="${not empty param.view}">
                                    <input type="hidden" name="view" value="${param.view}">
                                </c:if>
                                <input id="doneProcedureSearchInput"
                                       type="search"
                                       name="doneQ"
                                       class="procedure-done-search"
                                       placeholder="Tìm theo SBD hoặc tên thí sinh..."
                                       value="${fn:escapeXml(param.doneQ)}"
                                       autocomplete="off">
                                <button type="submit" class="procedure-done-search-submit">Tìm</button>
                                <c:if test="${not empty param.doneQ}">
                                    <a href="candidatecall" class="procedure-done-search-clear">Xóa</a>
                                </c:if>
                            </form>
                        </div>
                        <div class="table-responsive procedure-done-scroll">
                            <table class="report-table procedure-done-table">
                                <thead>
                                    <tr>
                                        <th scope="col" style="width: 110px;">SBD</th>
                                        <th scope="col">Họ và tên</th>
                                        <th scope="col" style="width: 72px; text-align: center;">Hạng</th>
                                        <th scope="col" style="width: 280px; text-align: right;">Thao tác</th>
                                    </tr>
                                </thead>
                                <tbody>
                                    <c:forEach var="candidate" items="${requestScope.procedureDoneCandidates}">
                                            <tr class="procedure-done-row"
                                                data-sbd="${fn:toLowerCase(candidate.sbd)}"
                                                data-name="${fn:toLowerCase(candidate.name)}">
                                                <td class="procedure-done-sbd">${candidate.sbd}</td>
                                                <td class="procedure-done-name">${candidate.name}</td>
                                                <td style="text-align: center;">
                                                    <span class="role-badge role-badge--coi" style="font-size: 0.68rem; padding: 2px 6px;">${candidate.clazz}</span>
                                                </td>
                                                <td>
                                                    <div class="procedure-done-actions">
                                                        <c:if test="${not requestScope.examMutationsLocked}">
                                                            <a href="procedure?sbd=${candidate.sbd}&amp;step=1#procedure-desk"
                                                               class="procedure-done-btn procedure-done-btn--edit"
                                                               title="Sửa hồ sơ thủ tục">Sửa</a>
                                                        </c:if>
                                                        <a href="candidate-dossier?sbd=${candidate.sbd}&amp;print=true"
                                                           target="_blank"
                                                           class="procedure-done-btn procedure-done-btn--print"
                                                           title="In phiếu hồ sơ">In</a>
                                                        <c:if test="${not requestScope.examMutationsLocked}">
                                                            <a href="candidatecall?action=permanentAbsent&amp;sbd=${candidate.sbd}"
                                                               class="procedure-done-btn procedure-done-btn--suspend"
                                                               title="Đình chỉ thi"
                                                               onclick="return confirm('Đình chỉ thí sinh ${candidate.sbd}? Thí sinh sẽ bị loại khỏi kỳ thi và không được gọi lại.');">Đình chỉ</a>
                                                            <a href="procedure?sbd=${candidate.sbd}&amp;action=resetProcedure"
                                                               class="procedure-done-btn procedure-done-btn--delete"
                                                               title="Xóa hồ sơ thủ tục, làm lại từ đầu"
                                                               onclick="return confirm('Xóa hồ sơ thủ tục của ${candidate.sbd}? Ảnh và thanh toán sẽ bị hủy để làm lại.');">Xóa</a>
                                                        </c:if>
                                                    </div>
                                                </td>
                                            </tr>
                                    </c:forEach>
                                </tbody>
                            </table>
                        </div>
                    </div>
                </c:if>
            </div>

        </div>

        <c:if test="${requestScope.deskMode}">
            <jsp:include page="/views/staff/examstaff/procedure.jsp"/>
        </c:if>

        <c:if test="${requestScope.deskMode}">
            <c:set var="currentStep" value="${not empty requestScope.step ? requestScope.step : param.step}" />
            <c:if test="${empty currentStep}"><c:set var="currentStep" value="1" /></c:if>
            <div id="procedureCameraConfig"
                 data-enabled="${currentStep eq '2' and not requestScope.hasValidPhoto and not requestScope.examMutationsLocked ? 'true' : 'false'}"
                 data-ctx-path="${pageContext.request.contextPath}"
                 data-sbd="${not empty requestScope.profile ? requestScope.profile.sbd : ''}"
                 data-msg-live="LIVE - Camera sẵn sàng"
                 data-msg-starting="Đang khởi động camera..."
                 data-msg-unavailable="Camera không khả dụng"
                 data-msg-no-api="Trình duyệt không hỗ trợ camera. Dùng Chrome/Edge/Firefox trên localhost hoặc HTTPS."
                 data-msg-denied="Quyền camera bị từ chối. Cho phép camera trong trình duyệt rồi tải lại trang."
                 data-msg-not-found="Không tìm thấy camera trên thiết bị."
                 data-msg-open-fail="Không thể mở camera."
                 data-msg-not-ready="Camera chưa sẵn sàng. Đợi vài giây rồi thử lại."
                 data-msg-frame-fail="Không đọc được khung hình từ camera."
                 data-msg-save-fail="Lưu ảnh thất bại: "
                 hidden></div>
            <script src="${pageContext.request.contextPath}/assets/js/procedure.js" charset="UTF-8"></script>
        </c:if>

<jsp:include page="/views/staff/examstaff/includes/examstaff-layout-foot.jsp" />

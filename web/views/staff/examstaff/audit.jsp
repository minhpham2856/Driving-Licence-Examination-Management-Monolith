<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" language="java" %>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib prefix = "fn" uri = "http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix = "fmt" uri = "http://java.sun.com/jsp/jstl/fmt" %>
<c:if test="${requestScope.personalLogs == null}">
    <c:redirect url="/examstaff/audit"/>
</c:if>

<jsp:useBean id="now" class="java.util.Date" />
<fmt:formatDate var="todayFormatted" value="${now}" pattern="dd/MM/yyyy" />

<jsp:include page="/views/staff/examstaff/includes/examstaff-layout-head.jsp">
    <jsp:param name="activeSidebar" value="nhat-ky" />
    <jsp:param name="pageTitle" value="Nhật ký cá nhân" />
    <jsp:param name="mainClass" value="examstaff-main--scroll" />
    <jsp:param name="dataAuditBase" value="/examstaff/audit" />
    <jsp:param name="dataAuditExportBase" value="/examstaff/audit-export" />
</jsp:include>

        <header class="page-header page-header--toolbar">
            <p class="examiner-page-desc">Xem lại lịch sử thao tác nghiệp vụ, đối chiếu hồ sơ thí sinh do chính bạn thực hiện trong ngày trực.</p>
        </header>

        <c:if test="${param.exportError eq '1'}">
            <div style="background-color: #fef2f2; border: 1px solid #fecaca; border-radius: 12px; padding: 1rem; margin-bottom: 1.5rem;">
                <p style="margin: 0; font-size: 0.85rem; color: #991b1b; font-weight: 600;">
                    Không xuất được file Excel. Hãy Clean and Build lại project (để copy thư viện POI vào WEB-INF/lib) rồi thử lại.
                </p>
            </div>
        </c:if>

        <c:set var="staffName" value="${sessionScope.user.username}" />
        <c:if test="${not empty sessionScope.userProfile}">
            <c:set var="staffName" value="${sessionScope.userProfile.fullName}" />
        </c:if>

        <div class="staff-profile-card">
            <div class="profile-info-group">
                <div class="profile-avatar-circle">
                    ${fn:substring(staffName, 0, 2)}
                </div>
                <div class="profile-meta-text">
                    <span style="font-size: 1.15rem; font-weight: 800;">${staffName}</span>
                    <span style="font-size: 0.82rem; opacity: 0.85; font-family: monospace;">Tài khoản: @${sessionScope.user.username} | Mã cán bộ: CBSH-00${sessionScope.user.userId}</span>
                </div>
            </div>

            <div style="text-align: right; font-size: 0.82rem; opacity: 0.9;">
                <span style="display: block; font-weight: 700; text-transform: uppercase;">Phạm vi nhật ký</span>
                <span id="auditScopeText" style="font-size: 1.0rem; font-weight: 800;">
                    <c:choose>
                        <c:when test="${not empty param.filterDate}">
                            Ngày: ${param.filterDate}
                        </c:when>
                        <c:otherwise>
                            Tất cả lịch sử
                        </c:otherwise>
                    </c:choose>
                </span>
            </div>
        </div>

        <div style="background-color: #ffffff; border: 1px solid #cbd5e1; border-radius: 12px; padding: 15px; margin-top: 1.5rem; margin-bottom: 1.5rem; display: flex; justify-content: space-between; align-items: center; gap: 1rem; flex-wrap: wrap;">
            <div style="display: flex; align-items: center; gap: 8px;">
                <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" style="color: #0052cc;">
                    <rect x="3" y="4" width="18" height="18" rx="2" ry="2"></rect>
                    <line x1="16" y1="2" x2="16" y2="6"></line>
                    <line x1="8" y1="2" x2="8" y2="6"></line>
                    <line x1="3" y1="10" x2="21" y2="10"></line>
                </svg>
                <span style="font-size: 0.9rem; font-weight: 700; color: #1e293b;">Bộ lọc thời gian nhật ký:</span>
            </div>

            <form id="auditFilterForm" action="${pageContext.request.contextPath}/examstaff/audit" method="GET" style="display: flex; align-items: center; gap: 10px; margin: 0;">
                <c:if test="${not empty param.examId}">
                    <input type="hidden" name="examId" value="${param.examId}" />
                </c:if>
                <input type="date" id="dateFilter" name="filterDate" value="${param.filterDate}" style="height: 38px; padding: 0 10px; border-radius: 8px; border: 1.5px solid #cbd5e1; font-weight: 600; color: #334155; outline: none; background-color: #ffffff; cursor: pointer;">
                <button type="submit" class="btn-filter" style="height: 38px; padding: 0 1.25rem; font-size: 0.82rem; border-radius: 8px; font-weight: 700; background: linear-gradient(135deg, #0052cc, #003d9b); border: none; color: #ffffff; cursor: pointer; transition: all 0.2s;">
                    Lọc kết quả
                </button>
                <c:if test="${not empty param.filterDate}">
                    <a href="${pageContext.request.contextPath}/examstaff/audit<c:if test='${not empty param.examId}'>?examId=${param.examId}</c:if>" data-audit-clear-filter="true" style="font-size: 0.8rem; font-weight: 600; color: #ef4444; text-decoration: none; padding: 0 5px;">Xóa bộ lọc</a>
                </c:if>
            </form>
        </div>

        <section class="metrics-row" aria-label="Số liệu hoạt động cá nhân">
            <div class="stat-card">
                <div class="stat-icon stat-icon--blue">
                    <svg width="24" height="24" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                        <circle cx="12" cy="12" r="10" stroke="currentColor" stroke-width="2"/>
                        <path d="M12 6v6l4 2" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                    </svg>
                </div>
                <div class="stat-info">
                    <span class="stat-number">${examStaffPageSlice.totalItems}</span>
                    <span class="stat-label">Tổng thao tác cá nhân</span>
                    <span class="stat-trend stat-trend--up">
                        <c:choose>
                            <c:when test="${not empty param.filterDate}">Trong ngày ${param.filterDate}</c:when>
                            <c:otherwise>Lịch sử tất cả thời gian</c:otherwise>
                        </c:choose>
                    </span>
                </div>
            </div>

            <div class="stat-card">
                <div class="stat-icon stat-icon--amber" style="background-color: rgba(126, 34, 206, 0.06); color: #7e22ce;">
                    <svg width="24" height="24" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                        <path d="M23 19a2 2 0 0 1-2 2H3a2 2 0 0 1-2-2V8a2 2 0 0 1 2-2h4l2-3h6l2 3h4a2 2 0 0 1 2 2z" stroke="currentColor" stroke-width="2"/>
                        <circle cx="12" cy="13" r="4" stroke="currentColor" stroke-width="2"/>
                    </svg>
                </div>
                <div class="stat-info">
                    <span class="stat-number" style="color: #7e22ce;">${requestScope.myCompletedProcedures}</span>
                    <span class="stat-label">Thí sinh đã làm thủ tục</span>
                    <span class="stat-trend stat-trend--up">Theo log thu lệ phí thủ tục do bạn ghi nhận</span>
                </div>
            </div>

            <div class="stat-card">
                <div class="stat-icon stat-icon--green">
                    <svg width="24" height="24" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                        <path d="M12 2v20M17 5H9.5a3.5 3.5 0 0 0 0 7h5a3.5 3.5 0 0 1 0 7H6" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                    </svg>
                </div>
                <div class="stat-info">
                    <span class="stat-number" style="color: #10b981;">
                        <fmt:formatNumber value="${requestScope.myTotalFees}" type="number" /> đ
                    </span>
                    <span class="stat-label">Lệ phí đã xác nhận thu</span>
                    <span class="stat-trend stat-trend--up">Tổng từ Payment_Fee (hoặc TotalAmount) của các lần thu bạn ghi log</span>
                </div>
            </div>
        </section>

        <c:set var="pg" value="${examStaffPageSlice}" />
        <c:set var="rowStart" value="${empty pg ? 0 : pg.rowOffset}" />

        <jsp:include page="/views/staff/examstaff/includes/examstaff-pagination.jsp" />

        <div id="auditPanel" class="allocation-stage-panel examstaff-audit-panel">
            <div class="allocation-stage-panel__head">
                <div class="allocation-stage-panel__title-wrap">
                    <h4 id="auditPanelTitle" class="allocation-stage-panel__title log-card-title">
                        <c:choose>
                            <c:when test="${not empty param.filterDate}">
                                Nhật ký hoạt động cá nhân ngày ${param.filterDate}
                            </c:when>
                            <c:otherwise>
                                Bảng kiểm toán tất cả hoạt động cá nhân
                            </c:otherwise>
                        </c:choose>
                    </h4>
                </div>
                <div class="allocation-panel-head-actions log-card-actions">
                    <span class="allocation-stage-panel__count">${pg.totalItems} thao tác</span>
                    <a id="auditExportLink"
                       href="${pageContext.request.contextPath}/examstaff/audit-export<c:if test='${not empty param.filterDate}'>?filterDate=${param.filterDate}</c:if>"
                       class="btn-export allocation-table-action">
                        Xuất Excel
                    </a>
                </div>
            </div>
            <div class="examiner-table-wrap examstaff-list-wrap">
                <table class="examiner-table examstaff-audit-table audit-table allocation-results-table allocation-table--fill">
                    <thead>
                        <tr>
                            <th scope="col" class="examiner-table__center">STT</th>
                            <th scope="col">Thời gian</th>
                            <th scope="col">Nghiệp vụ</th>
                            <th scope="col">Chi tiết thao tác</th>
                            <th scope="col" class="examiner-table__center">Trạng thái</th>
                        </tr>
                    </thead>
                    <tbody>
                        <c:forEach var="log" items="${requestScope.personalLogs}" varStatus="status">
                            <tr>
                                <td class="examiner-table__center">${rowStart + status.count}</td>
                                <td>
                                    <c:choose>
                                        <c:when test="${not empty log.changedAt}">
                                            <fmt:formatDate value="${log.changedAt}" pattern="dd/MM/yyyy HH:mm" />
                                        </c:when>
                                        <c:otherwise>-</c:otherwise>
                                    </c:choose>
                                </td>
                                <td>
                                    <span class="role-badge role-badge--coi examstaff-audit-entity">${log.entityLabelVi}</span>
                                    <span class="examstaff-audit-action">${log.actionLabelVi}</span>
                                </td>
                                <td class="examstaff-audit-details">${log.displayDetails}</td>
                                <td class="examiner-table__center">
                                    <span class="allocation-stage-status allocation-stage-status--logged">Ghi nhận</span>
                                </td>
                            </tr>
                        </c:forEach>
                        <c:if test="${empty requestScope.personalLogs}">
                            <tr>
                                <td colspan="5" class="allocation-stage-table__empty">
                                    <c:choose>
                                        <c:when test="${not empty param.filterDate}">
                                            Không có hoạt động thao tác nào được ghi nhận trong ngày ${param.filterDate}.
                                        </c:when>
                                        <c:otherwise>
                                            Không có hoạt động thao tác nào được ghi nhận trong lịch sử của bạn.
                                        </c:otherwise>
                                    </c:choose>
                                </td>
                            </tr>
                        </c:if>
                    </tbody>
                </table>
            </div>
        </div>

<jsp:include page="/views/staff/examstaff/includes/examstaff-layout-foot.jsp">
    <jsp:param name="extraScript" value="/assets/js/audit.js" />
</jsp:include>

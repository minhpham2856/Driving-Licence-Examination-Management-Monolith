<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" language="java" %>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>

<jsp:include page="/views/staff/examstaff/includes/examstaff-layout-head.jsp">
    <jsp:param name="activeSidebar" value="goi-thi" />
    <jsp:param name="pageTitle" value="Đình chỉ thi" />
    <jsp:param name="sectionTitle" value="Gọi làm thủ tục" />
    <jsp:param name="sectionUrl" value="${pageContext.request.contextPath}/views/staff/examstaff/candidatecall" />
    <jsp:param name="mainClass" value="examstaff-main--scroll" />
</jsp:include>

        <header class="page-header page-header--toolbar">
            <p class="examiner-page-desc">Thí sinh đình chỉ được ghi TRƯỢT và không được gọi lại trong ca thi này.</p>
            <div class="call-page-actions">
                <a href="candidatecall" class="call-toolbar-btn">Quay lại gọi thủ tục</a>
            </div>
        </header>

        <nav class="call-subnav">
            <a href="candidatecall" class="call-subnav__link">Gọi thủ tục</a>
            <a href="candidatecall?view=suspended" class="call-subnav__link is-active--warn">
                Đình chỉ thi
                <c:if test="${suspendedCount > 0}">
                    <span class="call-toolbar-badge">${suspendedCount}</span>
                </c:if>
            </a>
        </nav>

        <c:if test="${not empty requestScope.permanentAbsentAlert}">
            <div style="background-color: #fef2f2; border: 1px solid #ef4444; border-radius: 8px; padding: 10px 12px; margin-bottom: 1.25rem; display: flex; gap: 8px; align-items: center;">
                <svg width="18" height="18" viewBox="0 0 24 24" fill="none" style="color: #ef4444; flex-shrink: 0;">
                    <circle cx="12" cy="12" r="10" stroke="currentColor" stroke-width="2"/>
                    <path d="M12 8v4M12 16h.01" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                </svg>
                <span style="font-size: 0.82rem; font-weight: 600; color: #b91c1c;">
                    Đã đình chỉ thí sinh <strong style="color: #7f1d1d;">${requestScope.permanentAbsentAlert}</strong>. Kết quả ghi TRƯỢT.
                </span>
            </div>
        </c:if>

        <c:if test="${not empty requestScope.undoAlert}">
            <div style="background-color: #ecfdf5; border: 1px solid #10b981; border-radius: 8px; padding: 10px 12px; margin-bottom: 1.25rem; display: flex; gap: 8px; align-items: center;">
                <svg width="18" height="18" viewBox="0 0 24 24" fill="none" style="color: #10b981; flex-shrink: 0;">
                    <circle cx="12" cy="12" r="10" stroke="currentColor" stroke-width="2"/>
                    <path d="M8 12l3 3 5-5" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                </svg>
                <span style="font-size: 0.82rem; font-weight: 600; color: #065f46;">
                    Đã hoàn tác đình chỉ thí sinh <strong style="color: #047857;">${requestScope.undoAlert}</strong> — đưa về đầu hàng đợi gọi thủ tục.
                </span>
            </div>
        </c:if>

        <div class="report-pane" style="border-radius: 16px; padding: 1.5rem; border: 1px solid #cbd5e1; background: #ffffff; box-shadow: 0 4px 15px rgba(0,0,0,0.02);">
            <div style="border-bottom: 1.5px solid #f1f5f9; padding-bottom: 0.75rem; margin-bottom: 1.25rem; display: flex; justify-content: space-between; align-items: center; flex-wrap: wrap; gap: 10px;">
                <h3 style="font-size: 0.95rem; font-weight: 800; color: #dc2626; margin: 0; display: flex; align-items: center; gap: 8px;">
                    <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5">
                        <path d="M18.36 6.64a9 9 0 1 1-12.73 0"/>
                        <line x1="12" y1="2" x2="12" y2="12"/>
                    </svg>
                    Thí sinh bị đình chỉ trong ca
                </h3>
                <span class="role-badge role-badge--admin" style="font-size: 0.72rem; background-color: #fee2e2; color: #b91c1c; font-weight: 800; padding: 2px 8px;">
                    ${suspendedCount} người
                </span>
            </div>

            <c:choose>
                <c:when test="${empty suspendedList}">
                    <div style="text-align: center; padding: 4rem 1rem; color: #94a3b8; display: flex; flex-direction: column; align-items: center; gap: 8px;">
                        <svg width="40" height="40" viewBox="0 0 24 24" fill="none" style="opacity: 0.4;">
                            <circle cx="12" cy="12" r="10" stroke="currentColor" stroke-width="2"/>
                            <path d="M8 12h8" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
                        </svg>
                        <span style="font-weight: 700; font-size: 0.9rem; color: #475569;">Chưa có thí sinh bị đình chỉ</span>
                        <span style="font-size: 0.78rem; max-width: 320px;">Khi đình chỉ từ màn gọi thủ tục, danh sách sẽ hiển thị tại đây.</span>
                    </div>
                </c:when>
                <c:otherwise>
                    <div class="table-responsive">
                        <table class="audit-table" style="font-size: 0.85rem; width: 100%;">
                            <thead>
                                <tr>
                                    <th scope="col" style="width: 100px;">SBD</th>
                                    <th scope="col">Họ tên</th>
                                    <th scope="col" style="width: 80px; text-align: center;">Hạng</th>
                                    <th scope="col" style="width: 150px; text-align: center;">CCCD</th>
                                    <th scope="col" style="width: 150px; text-align: center;">Trạng thái</th>
                                    <th scope="col" style="width: 120px; text-align: right;">Hoàn tác</th>
                                </tr>
                            </thead>
                            <tbody>
                                <c:forEach var="c" items="${suspendedList}">
                                    <tr>
                                        <td style="font-weight: 800; color: #dc2626; font-family: monospace;">${c.sbd}</td>
                                        <td style="font-weight: 700; color: #0f172a;">${c.name}</td>
                                        <td style="text-align: center;">
                                            <span class="role-badge role-badge--admin" style="font-size: 0.65rem; padding: 1px 4px; background-color: #fee2e2; color: #991b1b;">${c.clazz}</span>
                                        </td>
                                        <td style="text-align: center; font-family: monospace; color: #475569;">${c.cccd}</td>
                                        <td style="text-align: center;">
                                            <span class="action-badge action-badge--danger" style="font-weight: 700;">Đình chỉ</span>
                                        </td>
                                        <td style="text-align: right;">
                                            <a href="candidatecall?action=undoAbsent&amp;returnView=suspended&amp;sbd=${c.sbd}"
                                               class="btn-filter"
                                               style="height: 26px; padding: 0 10px; font-size: 0.72rem; border-radius: 6px; text-decoration: none; display: inline-flex; align-items: center; border-color: #10b981; color: #10b981; background: rgba(16, 185, 129, 0.02);"
                                               title="Khôi phục về hàng đợi gọi thủ tục"
                                               onclick="return confirm('Hoàn tác đình chỉ ${c.sbd}? Thí sinh sẽ được đưa về đầu hàng đợi.');">
                                                Hoàn tác
                                            </a>
                                        </td>
                                    </tr>
                                </c:forEach>
                            </tbody>
                        </table>
                    </div>
                </c:otherwise>
            </c:choose>
        </div>

<jsp:include page="/views/staff/examstaff/includes/examstaff-layout-foot.jsp" />

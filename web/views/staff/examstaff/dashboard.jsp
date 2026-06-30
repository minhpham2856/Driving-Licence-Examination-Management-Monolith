<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<c:set var="ctx" value="${pageContext.request.contextPath}" />

<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Tổng quan kỳ thi - Ban Sát Hạch</title>
    <link rel="stylesheet" href="${ctx}/assets/css/style.css">
    <link rel="stylesheet" href="${ctx}/assets/css/layout.css">
</head>
<body class="has-side-nav-bar">

<jsp:include page="/views/layout/sidebar-examstaff.jsp">
    <jsp:param name="activeSidebar" value="dashboard" />
</jsp:include>

<div class="dashboard-shell">
    <main class="main-content">

        <jsp:include page="/views/layout/header-examstaff.jsp">
            <jsp:param name="pageTitle" value="Tổng quan kỳ thi" />
            <jsp:param name="sectionTitle" value="Tổng quan kỳ thi" />
        </jsp:include>

        <nav class="breadcrumbs">
            <a href="${ctx}/views/public/home.jsp">Trang chủ</a>
            <span class="breadcrumbs__separator">/</span>
            <span class="breadcrumbs__current">Tổng quan kỳ thi</span>
        </nav>

        <header class="page-header">
            <div class="page-title-wrap">
                <h1 class="page-title">Tổng quan kỳ thi sát hạch</h1>
                <p class="page-subtitle">Giám sát trực quan tiến độ đón tiếp, làm thủ tục hồ sơ và trạng thái thi của thí sinh trong ngày.</p>
            </div>

            <div class="page-actions" style="display: flex; gap: 10px; align-items: center; flex-wrap: wrap;">
                <form action="${ctx}/views/staff/examstaff/dashboard" method="GET" style="margin: 0; display: inline-flex; align-items: center;">
                    <div style="background: rgba(255, 255, 255, 0.7); border: 1px solid rgba(226, 232, 240, 0.8); border-radius: 8px; padding: 4px 10px; display: flex; align-items: center; gap: 8px; height: 42px;">
                        <span style="font-size: 0.72rem; font-weight: 800; color: #475569; text-transform: uppercase; letter-spacing: 0.03em; white-space: nowrap;">Ca sát hạch:</span>
                        <select id="examId" name="examId" class="es-exam-selector__select" onchange="this.form.submit()">
                            <c:forEach var="sess" items="${allExams}">
                                <option value="${sess.id}" ${selectedExamId eq sess.id ? "selected" : ""}>
                                    Ca #${sess.id} - ${sess.examLabel} (${sess.licenseCode})
                                </option>
                            </c:forEach>
                        </select>
                    </div>
                </form>

                <a href="${ctx}/views/staff/examstaff/allocation?examId=${selectedExamId}" class="btn-export" style="height: 42px; padding: 0 1.25rem; font-size: 0.9rem; border-radius: 8px; background-color: #ffffff; color: #475569; border-color: #e2e8f0; text-decoration: none; display: inline-flex; align-items: center; gap: 6px;">
                    Phân bổ khu vực
                </a>
            </div>
        </header>

        <c:if test="${not empty examControlMsg}">
            <div style="background-color: #ecfdf5; border: 1.5px solid #10b981; border-radius: 12px; padding: 0.88rem 1.25rem; margin-bottom: 1.25rem; display: flex; gap: 8px; align-items: center;">
                <span style="font-size: 0.85rem; font-weight: 700; color: #047857;">${examControlMsg}</span>
            </div>
        </c:if>
        <c:if test="${not empty sessionControlError}">
            <div style="background-color: #fef2f2; border: 1.5px solid #ef4444; border-radius: 12px; padding: 0.88rem 1.25rem; margin-bottom: 1.25rem; display: flex; gap: 8px; align-items: center;">
                <span style="font-size: 0.85rem; font-weight: 700; color: #b91c1c;">${sessionControlError}</span>
            </div>
        </c:if>

        <section class="report-pane" style="margin-top: 1rem; border-radius: 16px; padding: 1.25rem 1.5rem; border: 1px solid #cbd5e1; background: #ffffff;">
            <div style="display: flex; justify-content: space-between; align-items: center; flex-wrap: wrap; gap: 1rem;">
                <div>
                    <h2 style="font-size: 1rem; font-weight: 800; color: #0f172a; margin: 0 0 6px 0;">Điều khiển kỳ thi</h2>
                    <p style="font-size: 0.82rem; color: #64748b; margin: 0;">
                        Giám khảo chỉ đăng nhập được sau khi ca ở trạng thái <strong>Đang diễn ra</strong>
                        và đã được phân vào khu vực thi
                        (<a href="${ctx}/views/staff/examstaff/examiner-allocation?examId=${selectedExamId}" style="color: #0052cc; font-weight: 700;">Phân bổ giám khảo</a>).
                        Hiện có <strong>${assignedExaminerCount}</strong> giám khảo đã phân phòng.
                    </p>
                </div>
                <div style="display: flex; gap: 10px; flex-wrap: wrap; align-items: center;">
                    <c:choose>
                        <c:when test="${examStatusName eq "IN_PROGRESS"}">
                            <span class="role-badge role-badge--admin" style="background: #dcfce7; color: #166534; border: 1px solid #86efac;">${examStatusValue}</span>
                            <form action="${ctx}/staff/examstaff/exam-control" method="POST" style="margin: 0;">
                                <input type="hidden" name="action" value="endSession">
                                <input type="hidden" name="examId" value="${selectedExamId}">
                                <input type="hidden" name="redirect" value="dashboard">
                                <button type="submit" class="btn-export" style="height: 40px; padding: 0 1.25rem; border-radius: 8px; background: #fef2f2; color: #b91c1c; border-color: #fecaca; font-weight: 700;">
                                    Kết thúc kỳ thi
                                </button>
                            </form>
                        </c:when>
                        <c:when test="${examStatusName eq "COMPLETED" or examStatusName eq "CANCELLED"}">
                            <span class="role-badge" style="background: #f1f5f9; color: #64748b;">${examStatusValue}</span>
                        </c:when>
                        <c:otherwise>
                            <span class="role-badge role-badge--coi" style="background: #fffbeb; color: #b45309; border: 1px solid #fde68a;">${examStatusValue}</span>
                            <c:choose>
                                <c:when test="${assignedExaminerCount gt 0}">
                                    <form action="${ctx}/staff/examstaff/exam-control" method="POST" style="margin: 0;">
                                        <input type="hidden" name="action" value="startSession">
                                        <input type="hidden" name="examId" value="${selectedExamId}">
                                        <input type="hidden" name="redirect" value="dashboard">
                                        <button type="submit" class="btn-filter" style="height: 40px; padding: 0 1.25rem; border-radius: 8px; font-weight: 700;">
                                            Bắt đầu kỳ thi
                                        </button>
                                    </form>
                                </c:when>
                                <c:otherwise>
                                    <a href="${ctx}/views/staff/examstaff/examiner-allocation?examId=${selectedExamId}" class="btn-filter" style="height: 40px; padding: 0 1.25rem; border-radius: 8px; font-weight: 700; text-decoration: none; display: inline-flex; align-items: center; opacity: 0.85;">
                                        Phân giám khảo trước
                                    </a>
                                </c:otherwise>
                            </c:choose>
                        </c:otherwise>
                    </c:choose>
                </div>
            </div>
        </section>

        <section class="report-pane" style="margin-top: 1.5rem; border-radius: 16px; padding: 1.5rem; border: 1px solid #e2e8f0; background: #ffffff;">
            <h2 class="grading-pane__title" style="font-size: 1.05rem; display: flex; align-items: center; gap: 8px; margin: 0 0 1rem 0;">
                Thông tin kỳ thi
            </h2>
            <c:choose>
                <c:when test="${not empty currentExam}">
                    <ul style="list-style: none; padding: 0; margin: 0; display: grid; grid-template-columns: repeat(auto-fit, minmax(200px, 1fr)); gap: 1rem;">
                        <li><span style="display:block; font-size: 0.75rem; color: #64748b; text-transform: uppercase; letter-spacing: 0.03em;">Ca sát hạch</span><strong style="color:#0f172a;">Ca #${currentExam.id} - ${currentExam.examLabel}</strong></li>
                        <li><span style="display:block; font-size: 0.75rem; color: #64748b; text-transform: uppercase; letter-spacing: 0.03em;">Hạng</span><strong style="color:#0f172a;">${currentExam.licenseCode}</strong></li>
                        <li><span style="display:block; font-size: 0.75rem; color: #64748b; text-transform: uppercase; letter-spacing: 0.03em;">Ngày thi</span><strong style="color:#0f172a;"><fmt:formatDate value="${currentExam.examDate}" pattern="dd/MM/yyyy" /></strong></li>
                        <li><span style="display:block; font-size: 0.75rem; color: #64748b; text-transform: uppercase; letter-spacing: 0.03em;">Thí sinh đăng ký</span><strong style="color:#0f172a;">${currentExam.registeredCount}</strong></li>
                        <li><span style="display: block; font-size: 0.75rem; color: #64748b; text-transform: uppercase; letter-spacing: 0.03em;">Trạng thái</span><strong style="color:#0f172a;">${examStatusValue}</strong></li>
                    </ul>
                </c:when>
                <c:otherwise>
                    <p style="color: #64748b; margin: 0;">Không tìm thấy kỳ thi cho mã đã chọn.</p>
                </c:otherwise>
            </c:choose>
        </section>

    </main>
</div>

</body>
</html>

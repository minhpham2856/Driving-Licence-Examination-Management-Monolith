<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" language="java" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<jsp:include page="/views/staff/examstaff/includes/allocation-layout-head.jsp">
    <jsp:param name="pageTitle" value="Tổng quan phân bổ" />
    <jsp:param name="breadcrumbLabel" value="Tổng quan" />
    <jsp:param name="showSearch" value="true" />
    <jsp:param name="examId" value="${param.examId}" />
</jsp:include>

<c:set var="ctx" value="${pageContext.request.contextPath}" />
<c:set var="counts" value="${allocationStageCounts}" />
<c:set var="examIdQuery" value="${not empty param.examId ? '?examId='.concat(param.examId) : (not empty requestScope.examStaffLoadedExamId ? '?examId='.concat(requestScope.examStaffLoadedExamId) : '')}" />

<div class="report-pane allocation-summary-bar">
    <h2 class="allocation-summary-bar__title">Điều phối ca sát hạch</h2>
    <span class="role-badge role-badge--admin allocation-summary-bar__total">Tổng: ${counts.total} thí sinh</span>
</div>

<div class="allocation-overview-grid">
    <a href="${ctx}/views/staff/examstaff/allocation-waiting${examIdQuery}" class="allocation-overview-card allocation-overview-card--waiting">
        <span class="allocation-overview-card__label">Phòng chờ chính</span>
        <span class="allocation-overview-card__count">${counts.waiting}</span>
        <span class="allocation-overview-card__hint">Chờ thủ tục / thu lệ phí</span>
    </a>
    <a href="${ctx}/views/staff/examstaff/allocation-theory${examIdQuery}" class="allocation-overview-card allocation-overview-card--theory">
        <span class="allocation-overview-card__label">Phòng thi lý thuyết</span>
        <span class="allocation-overview-card__count">${counts.theory}</span>
        <span class="allocation-overview-card__hint">Hoàn tất hồ sơ, chờ thi LT</span>
    </a>
    <a href="${ctx}/views/staff/examstaff/allocation-practical${examIdQuery}" class="allocation-overview-card allocation-overview-card--practical">
        <span class="allocation-overview-card__label">Thực hành / Sa hình</span>
        <span class="allocation-overview-card__count">${counts.practical}</span>
        <span class="allocation-overview-card__hint">Đạt LT, chờ TH/SH</span>
    </a>
    <a href="${ctx}/views/staff/examstaff/allocation-results-pass${examIdQuery}" class="allocation-overview-card allocation-overview-card--pass">
        <span class="allocation-overview-card__label">Đỗ sát hạch</span>
        <span class="allocation-overview-card__count">${counts.passCount}</span>
        <span class="allocation-overview-card__hint">Hoàn thành ca · cấp GPLX</span>
    </a>
    <a href="${ctx}/views/staff/examstaff/allocation-results-fail${examIdQuery}" class="allocation-overview-card allocation-overview-card--fail">
        <span class="allocation-overview-card__label">Trượt / vắng</span>
        <span class="allocation-overview-card__count">${counts.failCount}</span>
        <span class="allocation-overview-card__hint">Kết thúc ca</span>
    </a>
    <a href="${ctx}/views/staff/examstaff/allocation-results-suspended${examIdQuery}" class="allocation-overview-card allocation-overview-card--fail">
        <span class="allocation-overview-card__label">Đình chỉ</span>
        <span class="allocation-overview-card__count">${counts.suspendedCount}</span>
        <span class="allocation-overview-card__hint">Bị loại khỏi kỳ thi</span>
    </a>
</div>

<c:if test="${not empty fn:trim(allocationSearchQuery)}">
    <div class="allocation-overview-search">
        <div class="allocation-overview-search__head">
            <span class="allocation-overview-search__count">${fn:length(allocationOverviewHits)} thí sinh</span>
        </div>
        <div class="examiner-table-wrap">
            <table class="examiner-table allocation-stage-table allocation-table--fill">
                <thead>
                    <tr>
                        <th class="examiner-table__center" style="width: 56px;">STT</th>
                        <th>SBD</th>
                        <th>Họ tên</th>
                        <th>Hạng</th>
                        <th>Đang ở phần</th>
                        <th style="width: 120px;">Chi tiết</th>
                    </tr>
                </thead>
                <tbody>
                    <c:forEach var="hit" items="${allocationOverviewHits}" varStatus="st">
                        <c:set var="c" value="${hit.candidate}" />
                        <c:url var="stageUrl" value="${hit.stagePath}">
                            <c:if test="${not empty param.examId}"><c:param name="examId" value="${param.examId}" /></c:if>
                            <c:if test="${empty param.examId and not empty requestScope.examStaffLoadedExamId}">
                                <c:param name="examId" value="${requestScope.examStaffLoadedExamId}" />
                            </c:if>
                        </c:url>
                        <tr>
                            <td class="examiner-table__center">${st.count}</td>
                            <td><strong>${c.sbd}</strong></td>
                            <td>${c.name}</td>
                            <td>${c.clazz}</td>
                            <td>
                                <span class="allocation-stage-status allocation-stage-status--${hit.stageKey}">
                                    <c:out value="${hit.stageLabel}" />
                                </span>
                            </td>
                            <td>
                                <a href="${stageUrl}" class="allocation-overview-search__link">Mở phần</a>
                            </td>
                        </tr>
                    </c:forEach>
                    <c:if test="${empty allocationOverviewHits}">
                        <tr>
                            <td colspan="6" class="allocation-stage-table__empty">
                                Không tìm thấy thí sinh khớp từ khóa trong kỳ này.
                            </td>
                        </tr>
                    </c:if>
                </tbody>
            </table>
        </div>
    </div>
</c:if>

<p class="allocation-overview-footnote">
    Chọn thẻ hoặc mục <strong>Phân bổ thí sinh</strong> trên sidebar để mở từng trang chi tiết.
    Dùng ô tìm kiếm phía trên để tra SBD / họ tên / CCCD và xem thí sinh đang ở phần nào.
</p>

<jsp:include page="/views/staff/examstaff/includes/allocation-layout-foot.jsp" />

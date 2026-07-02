<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" language="java" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<jsp:include page="/views/staff/examstaff/includes/allocation-layout-head.jsp">
    <jsp:param name="pageTitle" value="Tổng quan phân bổ" />
    <jsp:param name="breadcrumbLabel" value="Tổng quan" />
    <jsp:param name="showSearch" value="false" />
    <jsp:param name="sessionId" value="${param.sessionId}" />
</jsp:include>

<c:set var="ctx" value="${pageContext.request.contextPath}" />
<c:set var="counts" value="${allocationStageCounts}" />
<c:set var="sq" value="${not empty param.sessionId ? '?sessionId='.concat(param.sessionId) : ''}" />

<div class="report-pane allocation-summary-bar">
    <h2 class="allocation-summary-bar__title">Điều phối ca sát hạch</h2>
    <span class="role-badge role-badge--admin allocation-summary-bar__total">Tổng: ${counts.total} thí sinh</span>
</div>

<div class="allocation-overview-grid">
    <a href="${ctx}/views/staff/examstaff/allocation-waiting${sq}" class="allocation-overview-card allocation-overview-card--waiting">
        <span class="allocation-overview-card__label">Phòng chờ chính</span>
        <span class="allocation-overview-card__count">${counts.waiting}</span>
        <span class="allocation-overview-card__hint">Chờ thủ tục / thu lệ phí</span>
    </a>
    <a href="${ctx}/views/staff/examstaff/allocation-theory${sq}" class="allocation-overview-card allocation-overview-card--theory">
        <span class="allocation-overview-card__label">Phòng thi lý thuyết</span>
        <span class="allocation-overview-card__count">${counts.theory}</span>
        <span class="allocation-overview-card__hint">Hoàn tất hồ sơ, chờ thi LT</span>
    </a>
    <a href="${ctx}/views/staff/examstaff/allocation-practical${sq}" class="allocation-overview-card allocation-overview-card--practical">
        <span class="allocation-overview-card__label">Thực hành / Sa hình</span>
        <span class="allocation-overview-card__count">${counts.practical}</span>
        <span class="allocation-overview-card__hint">Đạt LT, chờ TH/SH</span>
    </a>
    <a href="${ctx}/views/staff/examstaff/allocation-road${sq}" class="allocation-overview-card allocation-overview-card--road">
        <span class="allocation-overview-card__label">Thi đường trường</span>
        <span class="allocation-overview-card__count">${counts.road}</span>
        <span class="allocation-overview-card__hint">Đạt TH/SH, chờ đường trường</span>
    </a>
    <a href="${ctx}/views/staff/examstaff/allocation-results-pass${sq}" class="allocation-overview-card allocation-overview-card--pass">
        <span class="allocation-overview-card__label">Đỗ sát hạch</span>
        <span class="allocation-overview-card__count">${counts.passCount}</span>
        <span class="allocation-overview-card__hint">Hoàn thành ca · cấp GPLX</span>
    </a>
    <a href="${ctx}/views/staff/examstaff/allocation-results-fail${sq}" class="allocation-overview-card allocation-overview-card--fail">
        <span class="allocation-overview-card__label">Trượt / vắng</span>
        <span class="allocation-overview-card__count">${counts.failCount}</span>
        <span class="allocation-overview-card__hint">Kết thúc ca</span>
    </a>
</div>
<p class="allocation-overview-footnote">Chọn thẻ hoặc mục <strong>Phân bổ thí sinh</strong> trên sidebar để mở từng trang chi tiết.</p>

<jsp:include page="/views/staff/examstaff/includes/allocation-layout-foot.jsp" />

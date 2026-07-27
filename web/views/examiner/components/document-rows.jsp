<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<%--export / print urls--%>
<c:set var="ctx" value="${pageContext.request.contextPath}" />
<c:set var="exportCandidatesUrl" value="${ctx}/examiner/export/candidates" />
<c:set var="exportViolationsUrl" value="${ctx}/examiner/export/violations" />
<c:set var="exportAuditUrl" value="${ctx}/examiner/export/audit" />
<c:set var="printUrl" value="${ctx}/examiner/print" />

<%--print result by sbd--%>
<div class="export-row">
    <div class="export-row-left">
        <div class="export-row-icon amber">
            <span class="material-symbols-outlined">assignment</span>
        </div>
        <div class="export-row-info">
            <p class="export-row-title">Kết quả thi - SBD</p>
        </div>
    </div>
    <div class="export-row-actions">
        <form class="export-sbd-form" action="${printUrl}" method="get" target="_blank">
            <input type="hidden" name="type" value="result">
            <input type="text" name="sbd" class="search-input" placeholder="Nhập SBD" required>
            <button type="submit" class="btn">
                <span class="material-symbols-outlined">print</span>
                <span class="btn-text">In</span>
            </button>
        </form>
    </div>
</div>

<%--print violation by sbd--%>
<div class="export-row">
    <div class="export-row-left">
        <div class="export-row-icon export-row-icon-red">
            <span class="material-symbols-outlined">gavel</span>
        </div>
        <div class="export-row-info">
            <p class="export-row-title">Biên bản vi phạm - SBD</p>
        </div>
    </div>
    <div class="export-row-actions">
        <form class="export-sbd-form" action="${printUrl}" method="get" target="_blank">
            <input type="hidden" name="type" value="violation">
            <input type="text" name="sbd" class="search-input" placeholder="Nhập SBD" required>
            <button type="submit" class="btn">
                <span class="material-symbols-outlined">print</span>
                <span class="btn-text">In</span>
            </button>
        </form>
    </div>
</div>

<%--export candidates excel--%>
<div class="export-row">
    <div class="export-row-left">
        <div class="export-row-icon blue">
            <span class="material-symbols-outlined">group</span>
        </div>
        <div class="export-row-info">
            <p class="export-row-title">Danh sách thí sinh</p>
        </div>
    </div>
    <div class="export-row-actions">
        <a href="${exportCandidatesUrl}" class="export-btn">
            <span class="material-symbols-outlined">download</span>
            <span class="export-btn-text">Xuất Excel</span>
        </a>
    </div>
</div>

<%--export violations excel--%>
<div class="export-row">
    <div class="export-row-left">
        <div class="export-row-icon export-row-icon-red">
            <span class="material-symbols-outlined">warning</span>
        </div>
        <div class="export-row-info">
            <p class="export-row-title">Danh sách thí sinh vi phạm</p>
        </div>
    </div>
    <div class="export-row-actions">
        <a href="${exportViolationsUrl}" class="export-btn">
            <span class="material-symbols-outlined">download</span>
            <span class="export-btn-text">Xuất Excel</span>
        </a>
    </div>
</div>

<%--export audit excel--%>
<div class="export-row export-row-last">
    <div class="export-row-left">
        <div class="export-row-icon export-row-icon-gray">
            <span class="material-symbols-outlined">history</span>
        </div>
        <div class="export-row-info">
            <p class="export-row-title">Nhật ký</p>
        </div>
    </div>
    <div class="export-row-actions">
        <a href="${exportAuditUrl}" class="export-btn">
            <span class="material-symbols-outlined">download</span>
            <span class="export-btn-text">Xuất Excel</span>
        </a>
    </div>
</div>

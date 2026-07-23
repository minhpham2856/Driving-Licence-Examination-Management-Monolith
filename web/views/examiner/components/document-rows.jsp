<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<c:set var="ctx" value="${pageContext.request.contextPath}" />
<c:set var="exportCandidatesUrl" value="${ctx}/examiner/export/candidates" />
<c:set var="exportViolationsUrl" value="${ctx}/examiner/export/violations" />
<c:set var="exportAuditUrl" value="${ctx}/examiner/export/audit" />
<c:set var="printUrl" value="${ctx}/examiner/print" />

<div class="export-row">
    <div class="export-row__left">
        <div class="export-row__icon export-row__icon--orange">
            <span class="material-symbols-outlined">assignment</span>
        </div>
        <div class="export-row__info">
            <p class="export-row__title">Kết quả thi - SBD</p>
        </div>
    </div>
    <div class="export-row__actions">
        <form class="export-sbd-form" action="${printUrl}" method="get" target="_blank">
            <input type="hidden" name="type" value="result">
            <input type="text" name="sbd" class="examiner-search__input" placeholder="Nhập SBD" required>
            <button type="submit" class="print-btn">
                <span class="material-symbols-outlined">print</span>
                <span class="print-btn__text">In</span>
            </button>
        </form>
    </div>
</div>

<div class="export-row">
    <div class="export-row__left">
        <div class="export-row__icon export-row__icon--blue">
            <span class="material-symbols-outlined">quiz</span>
        </div>
        <div class="export-row__info">
            <p class="export-row__title">Đề thi - SBD</p>
        </div>
    </div>
    <div class="export-row__actions">
        <form class="export-sbd-form" action="${printUrl}" method="get" target="_blank">
            <input type="hidden" name="type" value="bb1">
            <input type="text" name="sbd" class="examiner-search__input" placeholder="Nhập SBD" required>
            <button type="submit" class="print-btn">
                <span class="material-symbols-outlined">print</span>
                <span class="print-btn__text">In</span>
            </button>
        </form>
    </div>
</div>

<div class="export-row">
    <div class="export-row__left">
        <div class="export-row__icon export-row__icon--red">
            <span class="material-symbols-outlined">gavel</span>
        </div>
        <div class="export-row__info">
            <p class="export-row__title">Biên bản vi phạm - SBD</p>
        </div>
    </div>
    <div class="export-row__actions">
        <form class="export-sbd-form" action="${printUrl}" method="get" target="_blank">
            <input type="hidden" name="type" value="violations">
            <input type="text" name="q" class="examiner-search__input" placeholder="Nhập SBD" required>
            <button type="submit" class="print-btn">
                <span class="material-symbols-outlined">print</span>
                <span class="print-btn__text">In</span>
            </button>
        </form>
    </div>
</div>

<div class="export-row">
    <div class="export-row__left">
        <div class="export-row__icon export-row__icon--blue">
            <span class="material-symbols-outlined">group</span>
        </div>
        <div class="export-row__info">
            <p class="export-row__title">Danh sách thí sinh</p>
        </div>
    </div>
    <div class="export-row__actions">
        <a href="${exportCandidatesUrl}" class="export-btn">
            <span class="material-symbols-outlined">download</span>
            <span class="export-btn__text">Xuất Excel</span>
        </a>
    </div>
</div>

<div class="export-row">
    <div class="export-row__left">
        <div class="export-row__icon export-row__icon--red">
            <span class="material-symbols-outlined">warning</span>
        </div>
        <div class="export-row__info">
            <p class="export-row__title">Danh sách thí sinh vi phạm</p>
        </div>
    </div>
    <div class="export-row__actions">
        <a href="${exportViolationsUrl}" class="export-btn">
            <span class="material-symbols-outlined">download</span>
            <span class="export-btn__text">Xuất Excel</span>
        </a>
    </div>
</div>

<div class="export-row export-row--last">
    <div class="export-row__left">
        <div class="export-row__icon export-row__icon--gray">
            <span class="material-symbols-outlined">history</span>
        </div>
        <div class="export-row__info">
            <p class="export-row__title">Nhật ký</p>
        </div>
    </div>
    <div class="export-row__actions">
        <a href="${exportAuditUrl}" class="export-btn">
            <span class="material-symbols-outlined">download</span>
            <span class="export-btn__text">Xuất Excel</span>
        </a>
    </div>
</div>

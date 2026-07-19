<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<c:set var="ctx" value="${pageContext.request.contextPath}" />
<c:set var="docMode" value="${empty param.mode ? 'export' : param.mode}" />
<c:set var="isPrintMode" value="${docMode eq 'print'}" />
<c:set var="actionBtnClass" value="${empty param.btnClass ? (isPrintMode ? 'print-btn' : 'export-btn') : param.btnClass}" />
<c:set var="actionBtnTextClass" value="${actionBtnClass eq 'print-btn' ? 'print-btn__text' : 'export-btn__text'}" />
<c:set var="actionIcon" value="${isPrintMode ? 'print' : 'download'}" />
<c:set var="exportCandidatesUrl" value="${ctx}/examiner/export/candidates" />
<c:set var="exportResultUrl" value="${ctx}/examiner/export/result" />
<c:set var="exportViolationsUrl" value="${ctx}/examiner/export/violations" />
<c:set var="exportAuditUrl" value="${ctx}/examiner/export/audit" />
<c:set var="exportDocxUrl" value="${ctx}/examiner/export/docx" />
<c:set var="printUrl" value="${ctx}/examiner/print" />

<%-- Biên bản kết quả thi: chỉ DOCX (export + print) --%>
<div class="export-row">
    <div class="export-row__left">
        <div class="export-row__icon export-row__icon--orange">
            <span class="material-symbols-outlined">draw</span>
        </div>
        <div class="export-row__info">
            <p class="export-row__title">Biên bản kết quả thi</p>
        </div>
    </div>
    <c:choose>
        <c:when test="${isPrintMode}">
            <form class="export-row__actions export-sbd-form" action="${printUrl}" method="get" target="_blank">
                <input type="hidden" name="type" value="result">
                <div class="examiner-search">
                    <input type="text" name="sbd" class="examiner-search__input" placeholder="Nhập SBD" value="${param.sbd}" required>
                </div>
                <button type="submit" class="${actionBtnClass}">
                    <span class="material-symbols-outlined">${actionIcon}</span>
                    <span class="${actionBtnTextClass}">In</span>
                </button>
            </form>
        </c:when>
        <c:otherwise>
            <form class="export-row__actions export-sbd-form" action="${exportDocxUrl}" method="get">
                <input type="hidden" name="type" value="result">
                <div class="examiner-search">
                    <input type="text" name="sbd" class="examiner-search__input" placeholder="Nhập SBD" value="${param.sbd}" required>
                </div>
                <button type="submit" class="${actionBtnClass}">
                    <span class="material-symbols-outlined">${actionIcon}</span>
                    <span class="${actionBtnTextClass}">docx</span>
                </button>
            </form>
        </c:otherwise>
    </c:choose>
</div>

<%-- Danh sách thí sinh: chỉ Excel --%>
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
        <c:choose>
            <c:when test="${isPrintMode}">
                <a href="${printUrl}?type=candidates" target="_blank" rel="noopener" class="${actionBtnClass}">
                    <span class="material-symbols-outlined">print</span>
                    <span class="${actionBtnTextClass}">In</span>
                </a>
            </c:when>
            <c:otherwise>
                <a href="${exportCandidatesUrl}" class="export-btn">
                    <span class="material-symbols-outlined">download</span>
                    <span class="export-btn__text">excel</span>
                </a>
            </c:otherwise>
        </c:choose>
    </div>
</div>

<%-- Tổng hợp kết quả thi: chỉ Excel --%>
<div class="export-row">
    <div class="export-row__left">
        <div class="export-row__icon export-row__icon--blue">
            <span class="material-symbols-outlined">assignment</span>
        </div>
        <div class="export-row__info">
            <p class="export-row__title">Tổng hợp kết quả thi</p>
        </div>
    </div>
    <div class="export-row__actions">
        <c:choose>
            <c:when test="${isPrintMode}">
                <a href="${printUrl}?type=result" target="_blank" rel="noopener" class="${actionBtnClass}">
                    <span class="material-symbols-outlined">print</span>
                    <span class="${actionBtnTextClass}">In</span>
                </a>
            </c:when>
            <c:otherwise>
                <a href="${exportResultUrl}" class="export-btn">
                    <span class="material-symbols-outlined">download</span>
                    <span class="export-btn__text">excel</span>
                </a>
            </c:otherwise>
        </c:choose>
    </div>
</div>

<%-- Danh sách thí sinh vi phạm: chỉ Excel --%>
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
        <c:choose>
            <c:when test="${isPrintMode}">
                <a href="${printUrl}?type=violations" target="_blank" rel="noopener" class="${actionBtnClass}">
                    <span class="material-symbols-outlined">print</span>
                    <span class="${actionBtnTextClass}">In</span>
                </a>
            </c:when>
            <c:otherwise>
                <a href="${exportViolationsUrl}" class="export-btn">
                    <span class="material-symbols-outlined">download</span>
                    <span class="export-btn__text">excel</span>
                </a>
            </c:otherwise>
        </c:choose>
    </div>
</div>

<%-- Nhật ký: chỉ Excel --%>
<div class="export-row export-row--last">
    <div class="export-row__left">
        <div class="export-row__icon export-row__icon--blue">
            <span class="material-symbols-outlined">list_alt</span>
        </div>
        <div class="export-row__info">
            <p class="export-row__title">Nhật ký</p>
        </div>
    </div>
    <div class="export-row__actions">
        <c:choose>
            <c:when test="${isPrintMode}">
                <a href="${printUrl}?type=audit" target="_blank" rel="noopener" class="${actionBtnClass}">
                    <span class="material-symbols-outlined">print</span>
                    <span class="${actionBtnTextClass}">In</span>
                </a>
            </c:when>
            <c:otherwise>
                <a href="${exportAuditUrl}" class="export-btn">
                    <span class="material-symbols-outlined">download</span>
                    <span class="export-btn__text">excel</span>
                </a>
            </c:otherwise>
        </c:choose>
    </div>
</div>

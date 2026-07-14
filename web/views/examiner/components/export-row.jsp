<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<!--variables-->
<c:set var="ctx" value="${pageContext.request.contextPath}" />
<c:set var="signatureRowLast" value="${param.lastRow eq 'true'}" />
<c:set var="isPrintMode" value="${param.btnClass eq 'print-btn'}" />
<c:set var="signatureBtnClass" value="${empty param.btnClass ? 'export-btn' : param.btnClass}" />
<c:set var="signatureBtnTextClass" value="${isPrintMode ? 'print-btn__text' : 'export-btn__text'}" />

<div class="export-row${signatureRowLast ? ' export-row--last' : ''}">
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
            <form class="export-row__actions export-sbd-form" action="${ctx}/examiner/print-documents" method="get">
                <div class="examiner-search">
                    <input type="text" name="sbd" class="examiner-search__input" placeholder="Nhập SBD" value="${param.sbd}" required>
                </div>
                <button type="submit" class="${signatureBtnClass}">
                    <span class="material-symbols-outlined">print</span>
                    <span class="${signatureBtnTextClass}">In</span>
                </button>
            </form>
        </c:when>
        <c:otherwise>
            <form class="export-row__actions export-sbd-form" action="${ctx}/examiner/export/docx" method="get">
                <input type="hidden" name="type" value="result" />
                <div class="examiner-search">
                    <input type="text" name="sbd" class="examiner-search__input" placeholder="Nhập SBD" value="${param.sbd}" required>
                </div>
                <button type="submit" class="${signatureBtnClass}">
                    <span class="material-symbols-outlined">download</span>
                    <span class="${signatureBtnTextClass}">docx</span>
                </button>
            </form>
        </c:otherwise>
    </c:choose>
</div>

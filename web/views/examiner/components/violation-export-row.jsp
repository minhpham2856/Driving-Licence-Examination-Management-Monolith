<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<!--variables-->
<c:set var="ctx" value="${pageContext.request.contextPath}" />
<c:set var="isPrintMode" value="${param.mode eq 'print'}" />
<c:set var="rowLast" value="${param.lastRow eq 'true'}" />

<div class="export-row${rowLast ? ' export-row--last' : ''}">
    <div class="export-row__left">
        <div class="export-row__icon export-row__icon--red">
            <span class="material-symbols-outlined">warning</span>
        </div>
        <div class="export-row__info">
            <p class="export-row__title">Biên bản vi phạm</p>
        </div>
    </div>
    <c:choose>
        <c:when test="${isPrintMode}">
            <form class="export-row__actions export-sbd-form"
                  action="${ctx}/examiner/export/docx" method="get">
                <input type="hidden" name="type" value="violations" />
                <div class="examiner-search">
                    <select name="sbd" class="examiner-search__input" required>
                        <option value="" disabled${empty param.sbd ? ' selected' : ''}>Chọn SBD</option>
                        <c:forEach var="c" items="${suspendedCandidates}">
                            <option value="${c.candidateNumber}"
                                    <c:if test="${param.sbd == c.candidateNumber}">selected</c:if>>
                                ${c.candidateNumber}<c:if test="${not empty c.fullName}"> - ${c.fullName}</c:if>
                            </option>
                        </c:forEach>
                    </select>
                </div>
                <button type="submit" class="print-btn"${empty suspendedCandidates ? ' disabled' : ''}>
                    <span class="material-symbols-outlined">print</span>
                    <span class="print-btn__text">In</span>
                </button>
            </form>
        </c:when>
        <c:otherwise>
            <form class="export-row__actions export-sbd-form"
                  action="${ctx}/examiner/export/docx" method="get">
                <input type="hidden" name="type" value="violations" />
                <div class="examiner-search">
                    <select name="sbd" class="examiner-search__input" required>
                        <option value="" disabled${empty param.sbd ? ' selected' : ''}>Chọn SBD</option>
                        <c:forEach var="c" items="${suspendedCandidates}">
                            <option value="${c.candidateNumber}"
                                    <c:if test="${param.sbd == c.candidateNumber}">selected</c:if>>
                                ${c.candidateNumber}<c:if test="${not empty c.fullName}"> - ${c.fullName}</c:if>
                            </option>
                        </c:forEach>
                    </select>
                </div>
                <button type="submit" class="export-btn"${empty suspendedCandidates ? ' disabled' : ''}>
                    <span class="material-symbols-outlined">download</span>
                    <span class="export-btn__text">docx</span>
                </button>
            </form>
        </c:otherwise>
    </c:choose>
</div>

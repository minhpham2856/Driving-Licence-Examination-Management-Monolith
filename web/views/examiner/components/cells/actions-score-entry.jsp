<c:set var="ctx" value="${pageContext.request.contextPath}" />
<td>
    <c:if test="${c.scoreEntryEligible and not examinerSectionTheory}">
        <a href="${ctx}/examiner/score-entry?sbd=${c.candidateNumber}&amp;from=action"
           class="examiner-btn examiner-btn--orange examiner-btn--compact">Nhập điểm</a>
    </c:if>
</td>
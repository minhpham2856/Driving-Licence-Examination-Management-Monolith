<c:set var="ctx" value="${pageContext.request.contextPath}" />
<td>
    <c:if test="${c.sectionRequired and c.status eq 'done' and not examinerSectionTheory}">
        <a href="${ctx}/examiner/result-details-edit?sbd=${c.candidateNumber}&amp;from=action"
           class="examiner-btn examiner-btn--white examiner-btn--compact">Sửa kết quả</a>
    </c:if>
</td>
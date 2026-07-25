<c:set var="ctx" value="${pageContext.request.contextPath}" />
<td>
    <c:choose>
        <c:when test="${c.suspended}">
            <form method="post" action="${ctx}/examiner/violations" style="display:inline">
                <input type="hidden" name="action" value="undoSuspend">
                <input type="hidden" name="sbd" value="${c.candidateNumber}">
                <input type="hidden" name="from" value="action">
                <button type="submit" class="examiner-btn examiner-btn--white examiner-btn--compact">Hủy đình chỉ</button>
            </form>
        </c:when>
        <c:when test="${c.violationEligible}">
            <a href="${ctx}/examiner/violations?sbd=${c.candidateNumber}&amp;mode=create&amp;from=action"
               class="examiner-btn examiner-btn--danger examiner-btn--compact">Đình chỉ</a>
        </c:when>
    </c:choose>
</td>
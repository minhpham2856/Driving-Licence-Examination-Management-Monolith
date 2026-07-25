<td>
    <c:if test="${c.completeEligible}">
        <form method="post" action="${requestScope.pageUrl}">
            <input type="hidden" name="action" value="completeSection">
            <input type="hidden" name="sbd" value="${c.candidateNumber}">
            <button type="submit" class="examiner-btn examiner-btn--success examiner-btn--compact">Hoàn thành</button>
        </form>
    </c:if>
</td>
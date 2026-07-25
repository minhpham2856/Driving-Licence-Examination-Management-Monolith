<td>
    <c:if test="${c.awaitingSignature}">
        <form method="post" action="${requestScope.pageUrl}" target="_blank">
            <input type="hidden" name="action" value="printResult">
            <input type="hidden" name="sbd" value="${c.candidateNumber}">
            <button type="submit" class="examiner-btn examiner-btn--white examiner-btn--compact">In</button>
        </form>
    </c:if>
</td>
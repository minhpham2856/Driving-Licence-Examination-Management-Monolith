<td>
    <c:choose>
        <c:when test="${c.markPresentEligible}">
            <form method="post" action="${requestScope.pageUrl}">
                <input type="hidden" name="action" value="markPresent">
                <input type="hidden" name="sbd" value="${c.candidateNumber}">
                <button type="submit" class="examiner-btn examiner-btn--orange examiner-btn--compact">Điểm danh</button>
            </form>
        </c:when>
        <c:when test="${c.undoPresentEligible}">
            <form method="post" action="${requestScope.pageUrl}">
                <input type="hidden" name="action" value="undoPresent">
                <input type="hidden" name="sbd" value="${c.candidateNumber}">
                <button type="submit" class="examiner-btn examiner-btn--white examiner-btn--compact">Hủy điểm danh</button>
            </form>
        </c:when>
    </c:choose>
</td>
<td>
    <c:if test="${c.actionEligible}">
        <form method="post" action="${requestScope.pageUrl}" class="js-call-candidate"
              data-sbd="${c.candidateNumber}" data-name="${c.fullName}">
            <input type="hidden" name="action" value="call">
            <input type="hidden" name="sbd" value="${c.candidateNumber}">
            <button type="submit" class="examiner-btn examiner-btn--primary examiner-btn--compact">Gọi</button>
        </form>
    </c:if>
</td>
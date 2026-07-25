<td>
    <c:choose>
        <c:when test="${c.status == 'suspended'}"><span class="examiner-tag examiner-tag--suspended">${c.statusLabel}</span></c:when>
        <c:when test="${c.status == 'done'}"><span class="examiner-tag examiner-tag--done">${c.statusLabel}</span></c:when>
        <c:when test="${c.status == 'awaiting'}"><span class="examiner-tag examiner-tag--awaiting">${c.statusLabel}</span></c:when>
        <c:when test="${c.status == 'testing'}"><span class="examiner-tag examiner-tag--testing">${c.statusLabel}</span></c:when>
        <c:when test="${c.status == 'not-required'}"><span class="examiner-tag examiner-tag--pending">${c.statusLabel}</span></c:when>
        <c:otherwise><span class="examiner-tag examiner-tag--pending">${c.statusLabel}</span></c:otherwise>
    </c:choose>
</td>
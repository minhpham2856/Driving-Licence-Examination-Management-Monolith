<td class="examiner-table__mono-md">
    <c:choose>
        <c:when test="${not empty cellDetailHref}">
            <a class="examiner-table-link" href="${cellDetailHref}">${c.candidateNumber}</a>
        </c:when>
        <c:otherwise>${c.candidateNumber}</c:otherwise>
    </c:choose>
</td>
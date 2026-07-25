<td class="examiner-table__name">
    <c:choose>
        <c:when test="${not empty cellDetailHref}">
            <a class="examiner-table-link" href="${cellDetailHref}">${c.fullName}</a>
        </c:when>
        <c:otherwise>${c.fullName}</c:otherwise>
    </c:choose>
</td>
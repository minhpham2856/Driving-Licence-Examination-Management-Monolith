<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%--
  Bộ lọc danh sách đợt thi (dashboard / my-exams).
  Tham số jsp:include:
    filterAction, clearUrl, searchPlaceholder (optional)
  Request attributes: searchQuery, statusFilter, licenceFilter,
    statusFilterOptions, licenceFilterOptions (RegistrantFilterOption)
  Optional hidden field: filterHiddenFields (raw HTML for hidden inputs)
--%>
<c:set var="ctx" value="${pageContext.request.contextPath}" />
<c:set var="filterPlaceholder" value="${empty param.searchPlaceholder ? 'Kỳ thi, SBD, phòng...' : param.searchPlaceholder}" />

<div class="registrant-filter${param.compact eq 'true' ? ' registrant-filter--compact' : ''}">
    <form method="get" action="${param.filterAction}" class="registrant-filter__form" aria-label="Lọc danh sách đợt thi">
        <div class="registrant-filter__fields">
            <div class="registrant-filter__field registrant-filter__field--search">
                <label class="registrant-filter__label" for="registrant-filter-q">Tìm kiếm</label>
                <input type="text" id="registrant-filter-q" name="q" class="registrant-filter__control"
                       value="${searchQuery}" placeholder="${filterPlaceholder}" autocomplete="off">
            </div>
            <div class="registrant-filter__field">
                <label class="registrant-filter__label" for="registrant-filter-status">Trạng thái</label>
                <select id="registrant-filter-status" name="status" class="registrant-filter__control">
                    <c:forEach var="opt" items="${statusFilterOptions}">
                        <option value="${opt.value}"<c:if test="${opt.selected}"> selected="selected"</c:if>>${opt.label}</option>
                    </c:forEach>
                </select>
            </div>
            <div class="registrant-filter__field">
                <label class="registrant-filter__label" for="registrant-filter-licence">Hạng GPLX</label>
                <select id="registrant-filter-licence" name="licence" class="registrant-filter__control">
                    <c:forEach var="opt" items="${licenceFilterOptions}">
                        <option value="${opt.value}"<c:if test="${opt.selected}"> selected="selected"</c:if>>${opt.label}</option>
                    </c:forEach>
                </select>
            </div>
        </div>
        <div class="registrant-filter__actions">
            <button type="submit" class="registrant-filter__btn btn-header-primary">Lọc</button>
            <a href="${param.clearUrl}" class="registrant-filter__clear">Xóa lọc</a>
        </div>
        <c:if test="${not empty selectedExamId}">
            <input type="hidden" name="examId" value="${selectedExamId}">
        </c:if>
    </form>
    <c:if test="${searchActive}">
        <p class="registrant-filter__hint">
            <c:choose>
                <c:when test="${not empty totalRegisteredExamCount}">
                    Hiển thị ${filteredExamCount} / ${totalRegisteredExamCount} đợt thi
                    <c:if test="${not empty filteredActivityCount and filteredActivityCount ne totalActivityCount}">
                        · ${filteredActivityCount} / ${totalActivityCount} hoạt động
                    </c:if>.
                </c:when>
                <c:when test="${not empty totalExamCount}">
                    Hiển thị ${filteredExamCount} / ${totalExamCount} kỳ thi.
                </c:when>
                <c:otherwise>Đang áp dụng bộ lọc.</c:otherwise>
            </c:choose>
        </p>
    </c:if>
</div>

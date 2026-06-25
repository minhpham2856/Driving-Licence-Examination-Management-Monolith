<%@ page contentType="text/html;charset=UTF-8" language="java" %>

<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>



<div class="registrant-filter registrant-filter--in-panel">

    <form method="get" action="${pageContext.request.contextPath}/registrant/track-profile"

          class="registrant-filter__form" aria-label="Lọc nhật ký hồ sơ">

        <div class="registrant-filter__fields registrant-filter__fields--audit">

            <div class="registrant-filter__field registrant-filter__field--search">

                <label class="registrant-filter__label" for="audit-filter-q">Tìm kiếm</label>

                <input type="text" id="audit-filter-q" name="q" class="registrant-filter__control"

                       value="${searchQuery}" placeholder="Nội dung, trạng thái, ghi chú..." autocomplete="off">

            </div>

            <div class="registrant-filter__field">

                <label class="registrant-filter__label" for="audit-filter-category">Loại tác vụ</label>

                <select id="audit-filter-category" name="category" class="registrant-filter__control">

                    <c:forEach var="opt" items="${categoryFilterOptions}">

                        <option value="${opt.value}"<c:if test="${opt.selected}"> selected="selected"</c:if>>${opt.label}</option>

                    </c:forEach>

                </select>

            </div>

            <div class="registrant-filter__field">

                <label class="registrant-filter__label" for="audit-filter-status">Trạng thái</label>

                <select id="audit-filter-status" name="status" class="registrant-filter__control">

                    <c:forEach var="opt" items="${statusFilterOptions}">

                        <option value="${opt.value}"<c:if test="${opt.selected}"> selected="selected"</c:if>>${opt.label}</option>

                    </c:forEach>

                </select>

            </div>

            <div class="registrant-filter__field registrant-filter__field--date">

                <label class="registrant-filter__label" for="audit-filter-from">Từ ngày</label>

                <input type="date" id="audit-filter-from" name="fromDate" class="registrant-filter__control"

                       value="${fromDate}">

            </div>

            <div class="registrant-filter__field registrant-filter__field--date">

                <label class="registrant-filter__label" for="audit-filter-to">Đến ngày</label>

                <input type="date" id="audit-filter-to" name="toDate" class="registrant-filter__control"

                       value="${toDate}">

            </div>

        </div>

        <div class="registrant-filter__actions">

            <button type="submit" class="registrant-filter__btn btn-header-primary">Lọc</button>

            <a href="${pageContext.request.contextPath}/registrant/track-profile" class="registrant-filter__clear">Xóa lọc</a>

        </div>

    </form>

    <c:if test="${searchActive}">

        <p class="registrant-filter__hint">

            Hiển thị ${filteredTrackingCount} / ${trackingTotalCount} bản ghi (trang ${auditPage}/${auditTotalPages}).

        </p>

    </c:if>

</div>


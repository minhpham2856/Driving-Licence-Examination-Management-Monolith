<%@ page contentType="text/html;charset=UTF-8" language="java" %>

<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>

<%--

  Bộ lọc đợt thi khả dụng (register-exam) — form GET độc lập, không lồng form POST.

  Request attributes: searchQuery, locationFilter, locationFilterOptions, fromDate, toDate,

  searchActive, filteredSessionCount, totalSessionCount, selectedLicenceCode, sessionChosen, selectedSessionCode

--%>

<c:set var="ctx" value="${pageContext.request.contextPath}" />

<c:set var="licenceCode" value="${not empty selectedClassCode ? selectedClassCode : selectedLicenceCode}" />



<div class="registrant-filter registrant-filter--in-panel">

    <form method="get" action="${ctx}/registrant/register-exam#register-exam-session"

          class="registrant-filter__form" aria-label="Lọc đợt thi khả dụng">

        <input type="hidden" name="licenceSelect" value="${licenceCode}">

        <c:if test="${sessionChosen and not empty selectedSessionCode}">

            <input type="hidden" name="sessionSelect" value="${selectedSessionCode}">

        </c:if>

        <div class="registrant-filter__fields registrant-filter__fields--panel registrant-filter__fields--session">

            <div class="registrant-filter__field registrant-filter__field--search">

                <label class="registrant-filter__label" for="session-filter-q">Tìm kiếm</label>

                <input type="text" id="session-filter-q" name="q" class="registrant-filter__control"

                       value="${searchQuery}" placeholder="Mã đợt, tên ca, địa điểm..." autocomplete="off">

            </div>

            <div class="registrant-filter__field">

                <label class="registrant-filter__label" for="session-filter-location">Địa điểm</label>

                <select id="session-filter-location" name="location" class="registrant-filter__control">
                    <c:forEach var="opt" items="${locationFilterOptions}">
                        <option value="${opt.value}"<c:if test="${opt.selected}"> selected="selected"</c:if>>${opt.label}</option>
                    </c:forEach>
                </select>

            </div>

            <div class="registrant-filter__field registrant-filter__field--date">

                <label class="registrant-filter__label" for="session-filter-from">Từ ngày</label>

                <input type="date" id="session-filter-from" name="fromDate" class="registrant-filter__control"

                       value="${fromDate}">

            </div>

            <div class="registrant-filter__field registrant-filter__field--date">

                <label class="registrant-filter__label" for="session-filter-to">Đến ngày</label>

                <input type="date" id="session-filter-to" name="toDate" class="registrant-filter__control"

                       value="${toDate}">

            </div>

        </div>

        <div class="registrant-filter__actions registrant-filter__actions--session">

            <button type="submit" class="registrant-filter__btn btn-header-primary">Lọc</button>

            <a href="${ctx}/registrant/register-exam?licenceSelect=${licenceCode}#register-exam-session" class="registrant-filter__clear">Xóa lọc</a>

        </div>

    </form>

    <c:if test="${searchActive}">

        <p class="registrant-filter__hint">

            Hiển thị ${filteredSessionCount} / ${totalSessionCount} đợt thi khả dụng.

        </p>

    </c:if>

</div>


<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>

<c:choose>
    <c:when test="${not empty dashboardActionItems}">
        <ul class="dashboard-action-list" role="list">
            <c:forEach var="action" items="${dashboardActionItems}" varStatus="st">
                <li class="dashboard-action-item dashboard-action-item--${action.tone}" role="listitem">
                    <div class="dashboard-action-item__body">
                        <p class="dashboard-action-item__title">${action.title}</p>
                        <p class="dashboard-action-item__desc">${action.description}</p>
                    </div>
                    <a href="${pageContext.request.contextPath}${action.href}"
                       class="dashboard-action-item__btn"
                       id="dashboard-action-${st.index}">
                        ${action.actionLabel}
                    </a>
                </li>
            </c:forEach>
        </ul>
    </c:when>
    <c:otherwise>
        <div class="dashboard-action-empty" role="status">
            <div class="dashboard-action-empty__icon" aria-hidden="true">
                <svg width="22" height="22" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                    <circle cx="12" cy="12" r="10" stroke="currentColor" stroke-width="2"/>
                    <path d="M9 12l2 2 4-4" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                </svg>
            </div>
            <p class="dashboard-action-empty__title">Bạn đã hoàn tất các bước cần thiết</p>
            <p class="dashboard-action-empty__desc">Theo dõi lịch thi và kết quả trên trang Lịch thi &amp; kết quả.</p>
            <a href="${pageContext.request.contextPath}/registrant/my-exams"
               class="dashboard-action-empty__link"
               id="dashboard-action-fallback">
                Xem lịch thi &amp; kết quả
            </a>
        </div>
    </c:otherwise>
</c:choose>

<%@ page contentType="text/html;charset=UTF-8" language="java" %>
    <%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
        <%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>

            <!--variables-->
            <c:set var="gridUrl" value="${empty param.pageUrl ? pageContext.request.contextPath : param.pageUrl}" />
            <c:if test="${empty param.pageUrl}">
                <c:set var="gridUrl" value="${gridUrl}/examiner/devices" />
            </c:if>
            <c:set var="gridPath" value="${gridUrl}" />
            <c:if
                test="${not empty pageContext.request.contextPath and fn:startsWith(gridPath, pageContext.request.contextPath)}">
                <c:set var="gridPath"
                    value="${fn:substring(gridPath, fn:length(pageContext.request.contextPath), fn:length(gridPath))}" />
            </c:if>
            <c:set var="compact" value="${param.compact eq 'true'}" />
            <c:set var="showArea" value="${param.showArea ne 'false'}" />
            <c:set var="returnSbd" value="${param.returnSbd}" />
            <c:set var="compactGridClass" value="" />
            <c:set var="compactCardClass" value="" />
            <c:set var="compactEmptyClass" value="" />
            <c:if test="${compact}">
                <c:set var="compactGridClass" value=" device-grid--compact" />
                <c:set var="compactCardClass" value=" device-grid-card--compact" />
                <c:set var="compactEmptyClass" value=" examiner-table__empty--compact" />
            </c:if>
            <c:choose>
                <c:when test="${param.itemsAttr eq 'examVehicles'}">
                    <c:set var="deviceItems" value="${examVehicles}" />
                </c:when>
                <c:otherwise>
                    <c:set var="deviceItems" value="${devices}" />
                </c:otherwise>
            </c:choose>

            <c:set var="title" value="${param.title}" />
            <c:set var="badgeText" value="${param.badgeText}" />
            <c:set var="cardClass" value="${empty param.cardClass ? 'examiner-card' : param.cardClass}" />
            <c:set var="headerClass" value="${empty param.headerClass ? 'examiner-card__head' : param.headerClass}" />
            <c:set var="titleClass" value="${empty param.titleClass ? 'examiner-card__title' : param.titleClass}" />
            <c:set var="badgeClass" value="${empty param.badgeClass ? 'examiner-card__badge' : param.badgeClass}" />
            <c:set var="bodyClass" value="${empty param.bodyClass ? 'examiner-card__body' : param.bodyClass}" />
            <c:set var="hasBody" value="${param.hasBody != 'false'}" />

            <section class="${cardClass}">
                <c:if test="${not empty title or not empty badgeText}">
                    <div class="${headerClass}">
                        <c:if test="${not empty title}">
                            <c:choose>
                                <c:when test="${fn:contains(cardClass, 'score-entry-card')}">
                                    <div class="${titleClass}">
                                        <h2>${title}</h2>
                                    </div>
                                </c:when>
                                <c:otherwise>
                                    <h2 class="${titleClass}">${title}</h2>
                                </c:otherwise>
                            </c:choose>
                        </c:if>
                        <c:if test="${not empty badgeText}">
                            <span class="${badgeClass}">${badgeText}</span>
                        </c:if>
                    </div>
                </c:if>
                <c:if test="${hasBody}">
                    <div class="${bodyClass}">
                </c:if>

                <c:choose>
                    <c:when test="${empty deviceItems}">
                        <p class="examiner-table__empty${compactEmptyClass}">
                            Không có thiết bị trong khu vực thi.
                        </p>
                    </c:when>
                    <c:otherwise>
                        <div class="device-grid${compactGridClass}">
                            <c:forEach var="device" items="${deviceItems}">
                                <article class="device-grid-card ${device.statusClass}${compactCardClass}">
                                    <span class="device-grid-card__icon material-symbols-outlined">${device.icon}</span>
                                    <h4 class="device-grid-card__name">${device.name}</h4>
                                    <c:if test="${showArea and not empty device.area}">
                                        <p class="device-grid-card__area">${device.area}</p>
                                    </c:if>
                                    <span class="device-grid-card__status">${device.statusLabel}</span>
                                    <div class="device-grid-card__actions">
                                        <c:choose>
                                            <c:when test="${device.status eq 'Bảo trì'}">
                                                <form method="post" action="${gridUrl}" style="display:inline">
                                                    <input type="hidden" name="action" value="operational">
                                                    <input type="hidden" name="deviceId" value="${device.id}">
                                                    <c:if test="${not empty returnSbd}">
                                                        <input type="hidden" name="returnSbd" value="${returnSbd}">
                                                    </c:if>
                                                    <c:if test="${not empty requestScope.searchQuery}">
                                                        <input type="hidden" name="q" value="${requestScope.searchQuery}">
                                                    </c:if>
                                                    <button type="submit" class="examiner-link-action">Sử dụng</button>
                                                </form>
                                            </c:when>
                                            <c:otherwise>
                                                <form method="post" action="${gridUrl}" style="display:inline">
                                                    <input type="hidden" name="action" value="maintenance">
                                                    <input type="hidden" name="deviceId" value="${device.id}">
                                                    <c:if test="${not empty returnSbd}">
                                                        <input type="hidden" name="returnSbd" value="${returnSbd}">
                                                    </c:if>
                                                    <c:if test="${not empty requestScope.searchQuery}">
                                                        <input type="hidden" name="q" value="${requestScope.searchQuery}">
                                                    </c:if>
                                                    <button type="submit" class="examiner-link-action">Bảo trì</button>
                                                </form>
                                            </c:otherwise>
                                        </c:choose>
                                    </div>
                                </article>
                            </c:forEach>
                        </div>
                    </c:otherwise>
                </c:choose>
                <c:if test="${hasBody}">
                    </div>
                </c:if>
            </section>

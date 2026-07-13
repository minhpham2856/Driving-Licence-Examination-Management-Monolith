<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>

<c:if test="${not empty profileProgressSteps}">
<c:set var="stepCount" value="${fn:length(profileProgressSteps)}"/>
<c:set var="fillIdx" value="0"/>
<c:forEach var="step" items="${profileProgressSteps}" varStatus="st">
    <c:if test="${step.completed}"><c:set var="fillIdx" value="${st.index}"/></c:if>
    <c:if test="${step.active}"><c:set var="fillIdx" value="${st.index}"/></c:if>
</c:forEach>
<c:set var="lineFillPercent" value="${stepCount > 1 ? (fillIdx * 100) div (stepCount - 1) : 0}"/>

<section class="profile-progress profile-progress--horizontal" aria-label="Lịch sử cập nhật tiến trình hồ sơ">
    <header class="profile-progress__header">
        <svg class="profile-progress__header-icon" width="20" height="20" viewBox="0 0 24 24" fill="none" aria-hidden="true">
            <path d="M3 12a9 9 0 1 0 9-9 9.75 9.75 0 0 0-6.74 2.74L3 8" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
            <path d="M3 3v5h5M12 7v5l3 3" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
        </svg>
        <h2 class="profile-progress__title">Lịch sử cập nhật</h2>
    </header>

    <div class="profile-progress__track">
        <div class="profile-progress__line-bg" aria-hidden="true"></div>
        <div class="profile-progress__line-fill" style="width: ${lineFillPercent}%;" aria-hidden="true"></div>

        <ol class="profile-progress__nodes">
            <c:forEach var="step" items="${profileProgressSteps}" varStatus="st">
                <li class="profile-progress__node profile-progress__node--${step.state}">
                    <div class="profile-progress__node-top">
                        <span class="profile-progress__dot profile-progress__dot--${step.state}">
                            <c:choose>
                                <c:when test="${step.icon eq 'document'}">
                                    <svg width="18" height="18" viewBox="0 0 24 24" fill="none"><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z" stroke="currentColor" stroke-width="2"/><path d="M14 2v6h6M16 13H8M16 17H8M10 9H8" stroke="currentColor" stroke-width="2" stroke-linecap="round"/></svg>
                                </c:when>
                                <c:when test="${step.icon eq 'review'}">
                                    <svg width="18" height="18" viewBox="0 0 24 24" fill="none"><rect x="8" y="2" width="8" height="4" rx="1" stroke="currentColor" stroke-width="2"/><path d="M16 4h2a2 2 0 0 1 2 2v14a2 2 0 0 1-2 2H6a2 2 0 0 1-2-2V6a2 2 0 0 1 2-2h2" stroke="currentColor" stroke-width="2"/><path d="M12 11v6M9 14h6" stroke="currentColor" stroke-width="2" stroke-linecap="round"/></svg>
                                </c:when>
                                <c:when test="${step.icon eq 'supplement'}">
                                    <svg width="18" height="18" viewBox="0 0 24 24" fill="none"><circle cx="12" cy="12" r="9" stroke="currentColor" stroke-width="2"/><path d="M12 8v8M8 12h8" stroke="currentColor" stroke-width="2" stroke-linecap="round"/></svg>
                                </c:when>
                                <c:when test="${step.icon eq 'approved'}">
                                    <svg width="18" height="18" viewBox="0 0 24 24" fill="none"><path d="M22 11.08V12a10 10 0 1 1-5.93-9.14" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/><path d="M22 4 12 14.01l-3-3" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/></svg>
                                </c:when>
                                <c:otherwise>
                                    <svg width="18" height="18" viewBox="0 0 24 24" fill="none"><rect x="3" y="5" width="18" height="14" rx="2" stroke="currentColor" stroke-width="2"/><path d="M7 9h4M7 13h6" stroke="currentColor" stroke-width="2" stroke-linecap="round"/></svg>
                                </c:otherwise>
                            </c:choose>
                        </span>
                    </div>

                    <h3 class="profile-progress__step-title profile-progress__step-title--${step.state}">${step.title}</h3>

                    <c:choose>
                        <c:when test="${not empty step.timestamp and (step.completed or step.active)}">
                            <time class="profile-progress__time" datetime="">
                                <fmt:formatDate value="${step.timestamp}" pattern="dd/MM/yyyy HH:mm"/>
                            </time>
                        </c:when>
                        <c:when test="${not empty step.statusHint}">
                            <span class="profile-progress__hint">${step.statusHint}</span>
                        </c:when>
                    </c:choose>

                    <c:if test="${not empty step.description}">
                        <button type="button"
                                class="profile-progress__detail-btn"
                                aria-expanded="false"
                                aria-controls="profile-progress-detail-${st.index}">
                            Chi tiết
                        </button>

                        <div id="profile-progress-detail-${st.index}"
                             class="profile-progress__detail"
                             hidden>
                            <div class="profile-progress__box profile-progress__box--${step.state}${step.placeholder ? ' profile-progress__box--placeholder' : ''}">
                                <p class="profile-progress__desc">${step.description}</p>
                                <c:if test="${not empty step.footerText}">
                                    <div class="profile-progress__footer profile-progress__footer--${step.footerType}">
                                        <c:choose>
                                            <c:when test="${step.footerType eq 'shield'}">
                                                <svg width="14" height="14" viewBox="0 0 24 24" fill="none" aria-hidden="true"><path d="M12 22s8-4 8-10V5l-8-3-8 3v7c0 6 8 10 8 10z" stroke="currentColor" stroke-width="2"/></svg>
                                            </c:when>
                                            <c:when test="${step.footerType eq 'clock'}">
                                                <svg width="14" height="14" viewBox="0 0 24 24" fill="none" aria-hidden="true"><circle cx="12" cy="12" r="9" stroke="currentColor" stroke-width="2"/><path d="M12 7v5l3 3" stroke="currentColor" stroke-width="2" stroke-linecap="round"/></svg>
                                            </c:when>
                                        </c:choose>
                                        <span>${step.footerText}</span>
                                    </div>
                                </c:if>
                            </div>
                        </div>
                    </c:if>
                </li>
            </c:forEach>
        </ol>
    </div>
</section>

<script>
(function () {
    var root = document.querySelector('.profile-progress--horizontal');
    if (!root) return;

    var buttons = root.querySelectorAll('.profile-progress__detail-btn');
    buttons.forEach(function (btn) {
        btn.addEventListener('click', function () {
            var panelId = btn.getAttribute('aria-controls');
            var panel = document.getElementById(panelId);
            if (!panel) return;

            var isOpen = btn.getAttribute('aria-expanded') === 'true';

            buttons.forEach(function (otherBtn) {
                var otherPanel = document.getElementById(otherBtn.getAttribute('aria-controls'));
                otherBtn.setAttribute('aria-expanded', 'false');
                otherBtn.textContent = 'Chi tiết';
                if (otherPanel) otherPanel.hidden = true;
            });

            if (!isOpen) {
                btn.setAttribute('aria-expanded', 'true');
                btn.textContent = 'Thu gọn';
                panel.hidden = false;
            }
        });
    });
})();
</script>
</c:if>

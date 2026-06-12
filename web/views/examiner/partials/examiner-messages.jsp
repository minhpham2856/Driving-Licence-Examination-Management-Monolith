<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<c:if test="${not empty param.successMessage or not empty param.saved or not empty param.suspended
              or not empty param.undoSuspended or not empty param.called or not empty param.calledBatch
              or not empty param.absentDone or not empty param.undoAbsent
              or not empty param.maintenanceDone or not empty param.operationalDone
              or param.error eq 'saveFailed' or param.error eq 'undoFailed' or param.error eq 'undoAbsentFailed'
              or param.error eq 'noSbd' or param.error eq 'alreadySuspended' or param.error eq 'notSuspended'
              or param.error eq 'noSession' or param.error eq 'callSelectedFailed'
              or param.error eq 'absentFailed' or param.error eq 'maintenanceFailed'
              or param.error eq 'operationalFailed' or not empty violationError or not empty undoError}">
    <section class="examiner-card examiner-card--messages">
        <div class="examiner-card__head">
            <h3 class="examiner-card__title">Thông báo</h3>
        </div>
        <div class="examiner-card__body examiner-card__body--messages">
            <c:if test="${not empty param.successMessage}">
                <div class="examiner-alert examiner-alert--success">${param.successMessage}</div>
            </c:if>
            <c:if test="${param.saved eq '1'}">
                <div class="examiner-alert examiner-alert--success">Đã lưu thành công.</div>
            </c:if>
            <c:if test="${not empty param.suspended}">
                <div class="examiner-alert examiner-alert--success">Đã đình chỉ thí sinh SBD ${param.suspended}.</div>
            </c:if>
            <c:if test="${not empty param.undoSuspended}">
                <div class="examiner-alert examiner-alert--success">Đã hoàn tác đình chỉ SBD ${param.undoSuspended}.</div>
            </c:if>
            <c:if test="${not empty param.called}">
                <div class="examiner-alert examiner-alert--success">Đã gọi thí sinh SBD ${param.called}.</div>
            </c:if>
            <c:if test="${not empty param.calledBatch}">
                <div class="examiner-alert examiner-alert--success">Đã gọi ${param.calledBatch} thí sinh đã chọn.</div>
            </c:if>
            <c:if test="${not empty param.absentDone}">
                <div class="examiner-alert examiner-alert--success">Đã đánh dấu vắng thi SBD ${param.absentDone}.</div>
            </c:if>
            <c:if test="${not empty param.undoAbsent}">
                <div class="examiner-alert examiner-alert--success">Đã hoàn tác vắng thi SBD ${param.undoAbsent}.</div>
            </c:if>
            <c:if test="${not empty param.maintenanceDone}">
                <div class="examiner-alert examiner-alert--success">Đã chuyển thiết bị #${param.maintenanceDone} sang bảo trì.</div>
            </c:if>
            <c:if test="${not empty param.operationalDone}">
                <div class="examiner-alert examiner-alert--success">Đã chuyển thiết bị #${param.operationalDone} sang sử dụng.</div>
            </c:if>
            <c:if test="${param.error eq 'saveFailed'}">
                <div class="examiner-alert examiner-alert--error">Không lưu được biên bản vi phạm. Kiểm tra lại dữ liệu.</div>
            </c:if>
            <c:if test="${param.error eq 'undoFailed'}">
                <div class="examiner-alert examiner-alert--error">Không hoàn tác được đình chỉ.</div>
            </c:if>
            <c:if test="${param.error eq 'undoAbsentFailed'}">
                <div class="examiner-alert examiner-alert--error">Không hoàn tác được vắng thi<c:if test="${not empty param.sbd}"> SBD ${param.sbd}</c:if>.</div>
            </c:if>
            <c:if test="${param.error eq 'noSbd'}">
                <div class="examiner-alert examiner-alert--error">Không tìm thấy thí sinh.</div>
            </c:if>
            <c:if test="${param.error eq 'alreadySuspended'}">
                <div class="examiner-alert examiner-alert--error">Thí sinh đã bị đình chỉ.</div>
            </c:if>
            <c:if test="${param.error eq 'notSuspended'}">
                <div class="examiner-alert examiner-alert--error">Thí sinh chưa bị đình chỉ.</div>
            </c:if>
            <c:if test="${param.error eq 'noSession'}">
                <div class="examiner-alert examiner-alert--error">Chưa có ca thi đang diễn ra.</div>
            </c:if>
            <c:if test="${param.error eq 'callSelectedFailed'}">
                <div class="examiner-alert examiner-alert--error">Không gọi được thí sinh nào. Chọn ít nhất một thí sinh ở trạng thái Chưa thi.</div>
            </c:if>
            <c:if test="${param.error eq 'absentFailed'}">
                <div class="examiner-alert examiner-alert--error">Không đánh dấu vắng được<c:if test="${not empty param.sbd}"> SBD ${param.sbd}</c:if>.</div>
            </c:if>
            <c:if test="${param.error eq 'maintenanceFailed'}">
                <div class="examiner-alert examiner-alert--error">Không chuyển được thiết bị sang bảo trì.</div>
            </c:if>
            <c:if test="${param.error eq 'operationalFailed'}">
                <div class="examiner-alert examiner-alert--error">Không chuyển được thiết bị sang sử dụng.</div>
            </c:if>
            <c:if test="${not empty violationError}">
                <div class="examiner-alert examiner-alert--error">${violationError}</div>
            </c:if>
            <c:if test="${not empty undoError}">
                <div class="examiner-alert examiner-alert--error">${undoError}</div>
            </c:if>
        </div>
    </section>
</c:if>

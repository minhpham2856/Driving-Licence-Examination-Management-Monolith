<%@ page contentType="text/html;charset=UTF-8" trimDirectiveWhitespaces="true" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<div class="examiner-flash-container">
<c:if test="${not empty param.successMessage}">
    <p class="examiner-flash-bar examiner-flash-bar--success">${param.successMessage}</p>
</c:if>
<c:if test="${param.saved eq '1'}">
    <p class="examiner-flash-bar examiner-flash-bar--success">Đã lưu thành công.</p>
</c:if>
<c:if test="${not empty param.suspended}">
    <p class="examiner-flash-bar examiner-flash-bar--success">Đã đình chỉ thí sinh SBD ${param.suspended}.</p>
</c:if>
<c:if test="${not empty param.undoSuspended}">
    <p class="examiner-flash-bar examiner-flash-bar--success">Đã hoàn tác đình chỉ SBD ${param.undoSuspended}.</p>
</c:if>
<c:if test="${not empty param.called}">
    <p class="examiner-flash-bar examiner-flash-bar--success">Đã gọi thí sinh SBD ${param.called}.</p>
</c:if>
<c:if test="${not empty param.calledBatch}">
    <p class="examiner-flash-bar examiner-flash-bar--success">Đã gọi ${param.calledBatch} thí sinh đã chọn.</p>
</c:if>
<c:if test="${not empty param.absentDone}">
    <p class="examiner-flash-bar examiner-flash-bar--success">Đã đánh dấu vắng thi SBD ${param.absentDone}.</p>
</c:if>
<c:if test="${not empty param.undoAbsent}">
    <p class="examiner-flash-bar examiner-flash-bar--success">Đã hoàn tác vắng thi SBD ${param.undoAbsent}.</p>
</c:if>
<c:if test="${not empty param.maintenanceDone}">
    <p class="examiner-flash-bar examiner-flash-bar--success">Đã chuyển thiết bị #${param.maintenanceDone} sang bảo trì.</p>
</c:if>
<c:if test="${not empty param.operationalDone}">
    <p class="examiner-flash-bar examiner-flash-bar--success">Đã chuyển thiết bị #${param.operationalDone} sang sử dụng.</p>
</c:if>
<c:if test="${param.scoreCalled eq '1' and not empty param.sbd}">
    <p class="examiner-flash-bar examiner-flash-bar--success">Đã gọi thí sinh SBD ${param.sbd} vào nhập điểm.</p>
</c:if>
<c:if test="${not empty param.deferred}">
    <p class="examiner-flash-bar examiner-flash-bar--success">Đã đẩy SBD ${param.deferred} xuống cuối hàng đợi. Thí sinh vẫn có thể thi sau.</p>
</c:if>
<c:if test="${not empty param.completeDone}">
    <p class="examiner-flash-bar examiner-flash-bar--success">Đã hoàn tất phần thi SBD ${param.completeDone}. Thí sinh đạt sẽ được chuyển sang phần thi tiếp theo.</p>
</c:if>
<c:if test="${param.signatureMarked eq '1' and not empty param.sbd}">
    <p class="examiner-flash-bar examiner-flash-bar--success">Đã ghi nhận in biên bản ký tên SBD ${param.sbd}.</p>
</c:if>
<c:if test="${param.error eq 'saveFailed'}">
    <p class="examiner-flash-bar examiner-flash-bar--error">Không lưu được biên bản vi phạm. Kiểm tra lại dữ liệu.</p>
</c:if>
<c:if test="${param.error eq 'undoFailed'}">
    <p class="examiner-flash-bar examiner-flash-bar--error">Không hoàn tác được đình chỉ.</p>
</c:if>
<c:if test="${param.error eq 'undoAbsentFailed'}">
    <p class="examiner-flash-bar examiner-flash-bar--error">Không hoàn tác được vắng thi<c:if test="${not empty param.sbd}"> SBD ${param.sbd}</c:if>.</p>
</c:if>
<c:if test="${param.error eq 'noSbd'}">
    <p class="examiner-flash-bar examiner-flash-bar--error">Không tìm thấy thí sinh.</p>
</c:if>
<c:if test="${param.error eq 'alreadySuspended'}">
    <p class="examiner-flash-bar examiner-flash-bar--error">Thí sinh đã bị đình chỉ.</p>
</c:if>
<c:if test="${param.error eq 'notSuspended'}">
    <p class="examiner-flash-bar examiner-flash-bar--error">Thí sinh chưa bị đình chỉ.</p>
</c:if>
<c:if test="${param.error eq 'noSession'}">
    <p class="examiner-flash-bar examiner-flash-bar--error">Chưa có ca thi đang diễn ra.</p>
</c:if>
<c:if test="${param.error eq 'callSelectedFailed'}">
    <p class="examiner-flash-bar examiner-flash-bar--error">Không gọi được thí sinh nào. Chọn ít nhất một thí sinh ở trạng thái Chưa thi.</p>
</c:if>
<c:if test="${param.error eq 'absentFailed'}">
    <p class="examiner-flash-bar examiner-flash-bar--error">Không đánh dấu vắng được<c:if test="${not empty param.sbd}"> SBD ${param.sbd}</c:if>.</p>
</c:if>
<c:if test="${param.error eq 'maintenanceFailed'}">
    <p class="examiner-flash-bar examiner-flash-bar--error">Không chuyển được thiết bị sang bảo trì.</p>
</c:if>
<c:if test="${param.error eq 'operationalFailed'}">
    <p class="examiner-flash-bar examiner-flash-bar--error">Không chuyển được thiết bị sang sử dụng.</p>
</c:if>
<c:if test="${param.error eq 'noCandidate'}">
    <p class="examiner-flash-bar examiner-flash-bar--error">Không còn thí sinh trong hàng đợi nhập điểm.</p>
</c:if>
<c:if test="${param.error eq 'callFailed'}">
    <p class="examiner-flash-bar examiner-flash-bar--error">Không gọi được thí sinh<c:if test="${not empty param.sbd}"> SBD ${param.sbd}</c:if>.</p>
</c:if>
<c:if test="${param.error eq 'needSignaturePrint'}">
    <p class="examiner-flash-bar examiner-flash-bar--error">Phải in biên bản ký tên ít nhất một lần trước khi hoàn tất<c:if test="${not empty param.sbd}"> (SBD ${param.sbd})</c:if>.</p>
</c:if>
<c:if test="${param.error eq 'completeFailed'}">
    <p class="examiner-flash-bar examiner-flash-bar--error">Không hoàn tất được phần thi<c:if test="${not empty param.sbd}"> SBD ${param.sbd}</c:if>.</p>
</c:if>
<c:if test="${param.error eq 'signaturePrintFailed'}">
    <p class="examiner-flash-bar examiner-flash-bar--error">Không in được biên bản ký tên<c:if test="${not empty param.sbd}"> SBD ${param.sbd}</c:if>. Thí sinh phải ở trạng thái Chờ ký.</p>
</c:if>
<c:if test="${param.error eq 'theoryNoResultEdit'}">
    <p class="examiner-flash-bar examiner-flash-bar--error">Phần thi lý thuyết không hỗ trợ sửa kết quả.</p>
</c:if>
<c:if test="${not empty violationError}">
    <p class="examiner-flash-bar examiner-flash-bar--error">${violationError}</p>
</c:if>
<c:if test="${not empty undoError}">
    <p class="examiner-flash-bar examiner-flash-bar--error">${undoError}</p>
</c:if>
</div>

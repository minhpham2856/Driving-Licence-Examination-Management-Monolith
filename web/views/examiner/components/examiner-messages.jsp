<%@ page contentType="text/html;charset=UTF-8" trimDirectiveWhitespaces="true" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<div class="examiner-flash-container">
    <c:if test="${not empty param.successMessage}">
        <p class="examiner-flash-bar examiner-flash-bar--success">${param.successMessage}</p>
    </c:if>
    <c:if test="${param.saved eq '1'}">
        <p class="examiner-flash-bar examiner-flash-bar--success">Đã lưu.</p>
    </c:if>
    <c:if test="${not empty param.presentDone}">
        <p class="examiner-flash-bar examiner-flash-bar--success">Đã điểm danh SBD ${param.presentDone}.</p>
    </c:if>
    <c:if test="${not empty param.undoPresent}">
        <p class="examiner-flash-bar examiner-flash-bar--success">Đã hoàn tác điểm danh SBD ${param.undoPresent}.</p>
    </c:if>
    <c:if test="${not empty param.wrongInfoDone}">
        <p class="examiner-flash-bar examiner-flash-bar--success">Đã chuyển SBD ${param.wrongInfoDone} xuống phòng thủ tục.</p>
    </c:if>
    <c:if test="${not empty param.violationDone}">
        <p class="examiner-flash-bar examiner-flash-bar--success">Đã đình chỉ SBD ${param.violationDone}. Xem chi tiết vi phạm để in/tải biên bản.</p>
    </c:if>
    <c:if test="${not empty param.called}">
        <p class="examiner-flash-bar examiner-flash-bar--success">Đã gọi SBD ${param.called}.</p>
    </c:if>
    <c:if test="${not empty param.calledBatch}">
        <p class="examiner-flash-bar examiner-flash-bar--success">Đã gọi ${param.calledBatch} thí sinh.</p>
    </c:if>
    <c:if test="${param.error eq 'presentFailed'}">
        <p class="examiner-flash-bar examiner-flash-bar--error">Không điểm danh được<c:if test="${not empty param.sbd}"> SBD ${param.sbd}</c:if>.</p>
    </c:if>
    <c:if test="${param.error eq 'undoPresentFailed'}">
        <p class="examiner-flash-bar examiner-flash-bar--error">Không hoàn tác điểm danh được<c:if test="${not empty param.sbd}"> SBD ${param.sbd}</c:if>.</p>
    </c:if>
    <c:if test="${param.error eq 'wrongInfoFailed'}">
        <p class="examiner-flash-bar examiner-flash-bar--error">Không chuyển phòng thủ tục được<c:if test="${not empty param.sbd}"> SBD ${param.sbd}</c:if>.</p>
    </c:if>
    <c:if test="${param.error eq 'recordFailed'}">
        <p class="examiner-flash-bar examiner-flash-bar--error">Không ghi nhận vi phạm được.</p>
    </c:if>
    <c:if test="${not empty param.maintenanceDone}">
        <p class="examiner-flash-bar examiner-flash-bar--success">Đã chuyển thiết bị ${param.maintenanceDone} sang bảo trì.</p>
    </c:if>
    <c:if test="${not empty param.operationalDone}">
        <p class="examiner-flash-bar examiner-flash-bar--success">Đã chuyển thiết bị ${param.operationalDone} sang sử dụng.</p>
    </c:if>
    <!--note01-->
    <c:if test="${param.scoreCalled eq '1' and not empty param.sbd}">
        <p class="examiner-flash-bar examiner-flash-bar--success">Đã gọi thí sinh SBD ${param.sbd} vào nhập điểm.</p> 
    </c:if>
    <c:if test="${not empty param.deferred}">
        <p class="examiner-flash-bar examiner-flash-bar--success">Đã đẩy SBD ${param.deferred} xuống cuối hàng đợi.</p>
    </c:if>
    <c:if test="${not empty param.completeDone}">
        <p class="examiner-flash-bar examiner-flash-bar--success">Đã hoàn tất phần thi SBD ${param.completeDone}.</p>
    </c:if>
    <c:if test="${param.finalized eq '1'}">
        <p class="examiner-flash-bar examiner-flash-bar--success">Hoàn tất nhập điểm thành công. Đang hiển thị thí sinh tiếp theo.</p>
    </c:if>
    <c:if test="${param.signatureMarked eq '1' and not empty param.sbd}">
        <p class="examiner-flash-bar examiner-flash-bar--success">Đã in biên bản kết quả thi SBD ${param.sbd}.</p>
    </c:if>
    <c:if test="${param.vehicleChanged eq '1' and not empty param.sbd}">
        <p class="examiner-flash-bar examiner-flash-bar--success">Đã đổi xe thi cho SBD ${param.sbd}.</p>
    </c:if>
    <c:if test="${param.error eq 'saveFailed'}">
        <p class="examiner-flash-bar examiner-flash-bar--error">Không lưu được biên bản vi phạm.</p>
    </c:if>
    <c:if test="${param.error eq 'noSbd'}">
        <p class="examiner-flash-bar examiner-flash-bar--error">Không tìm thấy thí sinh.</p>
    </c:if>
    <c:if test="${param.error eq 'alreadySuspended'}">
        <p class="examiner-flash-bar examiner-flash-bar--error">Thí sinh đã bị đình chỉ.</p>
    </c:if>
    <c:if test="${param.error eq 'noSession'}">
        <p class="examiner-flash-bar examiner-flash-bar--error">Chưa có ca thi đang diễn ra.</p>
    </c:if>
    <c:if test="${param.error eq 'callSelectedFailed'}">
        <p class="examiner-flash-bar examiner-flash-bar--error">Chọn ít nhất một thí sinh để gọi.</p>
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
    <c:if test="${param.error eq 'suspendFailed'}">
        <p class="examiner-flash-bar examiner-flash-bar--error">Không đình chỉ được thí sinh<c:if test="${not empty param.sbd}"> SBD ${param.sbd}</c:if>.</p>
    </c:if>
    <c:if test="${param.error eq 'callFailed'}">
        <p class="examiner-flash-bar examiner-flash-bar--error">Không gọi được thí sinh<c:if test="${not empty param.sbd}"> SBD ${param.sbd}</c:if>.</p>
    </c:if>
    <c:if test="${param.error eq 'finalizeFailed'}">
        <p class="examiner-flash-bar examiner-flash-bar--error">Hoàn tất nhập điểm thất bại<c:if test="${not empty param.sbd}"> (SBD ${param.sbd})</c:if>. Kiểm tra lại trạng thái thí sinh.</p>
    </c:if>
    <c:if test="${param.error eq 'needSignaturePrint'}">
        <p class="examiner-flash-bar examiner-flash-bar--error">Phải in biên bản kết quả thi ít nhất 1 lần trước khi hoàn tất<c:if test="${not empty param.sbd}"> (SBD ${param.sbd})</c:if>.</p>
    </c:if>
    <c:if test="${param.error eq 'completeFailed'}">
        <p class="examiner-flash-bar examiner-flash-bar--error">Không hoàn tất được phần thi<c:if test="${not empty param.sbd}"> SBD ${param.sbd}</c:if>.</p>
    </c:if>
    <c:if test="${param.error eq 'signaturePrintFailed'}">
        <p class="examiner-flash-bar examiner-flash-bar--error">Không in được biên bản kết quả thi<c:if test="${not empty param.sbd}"> SBD ${param.sbd}</c:if>. Thí sinh phải ở trạng thái Chờ ký.</p>
    </c:if>
    <c:if test="${param.error eq 'theoryNoResultEdit'}">
        <p class="examiner-flash-bar examiner-flash-bar--error">Phần thi lý thuyết không được phép sửa kết quả.</p>
    </c:if>
    <c:if test="${not empty profileError}">
        <p class="examiner-flash-bar examiner-flash-bar--error">${profileError}</p>
    </c:if>
    <c:if test="${not empty violationError}">
        <p class="examiner-flash-bar examiner-flash-bar--error">${violationError}</p>
    </c:if>
</div>

<%@ page contentType="text/html;charset=UTF-8" trimDirectiveWhitespaces="true" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<div class="examiner-flash-container">
    <c:if test="${not empty param.successMessage}">
        <p class="examiner-flash-bar examiner-flash-bar--success">${param.successMessage}</p>
    </c:if>
    <c:if test="${param.saved eq '1'}">
        <p class="examiner-flash-bar examiner-flash-bar--success">Đã lưu.</p>
    </c:if>
    <c:if test="${not empty param.suspended}">
        <p class="examiner-flash-bar examiner-flash-bar--success">Đã đình chỉ SBD ${param.suspended}.</p>
    </c:if>
    <c:if test="${param.unsuspended eq '1'}">
        <p class="examiner-flash-bar examiner-flash-bar--success">Đã gỡ đình chỉ SBD ${param.sbd}.</p>
    </c:if>
    <c:if test="${not empty param.unsuspended and param.unsuspended ne '1'}">
        <p class="examiner-flash-bar examiner-flash-bar--success">Đã gỡ đình chỉ SBD ${param.unsuspended}.</p>
    </c:if>
    <c:if test="${not empty param.undoSuspended}">
        <p class="examiner-flash-bar examiner-flash-bar--success">Đã hoàn tác đình chỉ SBD ${param.undoSuspended}.</p>
    </c:if>
    <c:if test="${not empty param.invoked}">
        <p class="examiner-flash-bar examiner-flash-bar--success">Đã thao tác SBD ${param.invoked}.</p>
    </c:if>
    <c:if test="${not empty param.invokedBatch}">
        <p class="examiner-flash-bar examiner-flash-bar--success">Đã thao tác ${param.invokedBatch} thí sinh.</p>
    </c:if>
    <c:if test="${not empty param.presentDone}">
        <p class="examiner-flash-bar examiner-flash-bar--success">Đã điểm danh SBD ${param.presentDone}.</p>
    </c:if>
    <c:if test="${not empty param.maintenanceDone}">
        <p class="examiner-flash-bar examiner-flash-bar--success">Đã chuyển thiết bị ${param.maintenanceDone} sang bảo trì.</p>
    </c:if>
    <c:if test="${not empty param.operationalDone}">
        <p class="examiner-flash-bar examiner-flash-bar--success">Đã chuyển thiết bị ${param.operationalDone} sang sử dụng.</p>
    </c:if>
    <!--note01-->
    <c:if test="${param.scoreInvoked eq '1' and not empty param.sbd}">
        <p class="examiner-flash-bar examiner-flash-bar--success">Đã thao tác thí sinh SBD ${param.sbd} vào nhập điểm.</p> 
    </c:if>
    <c:if test="${not empty param.completeDone}">
        <p class="examiner-flash-bar examiner-flash-bar--success">Đã hoàn thành phần thi SBD ${param.completeDone}.</p>
    </c:if>
    <c:if test="${not empty param.undoPresent}">
        <p class="examiner-flash-bar examiner-flash-bar--success">Đã hủy điểm danh SBD ${param.undoPresent}.</p>
    </c:if>
    <c:if test="${not empty param.wrongInfoDone}">
        <p class="examiner-flash-bar examiner-flash-bar--success">Đã chuyển SBD ${param.wrongInfoDone} về phòng thủ tục (sai thông tin).</p>
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
    <c:if test="${param.error eq 'evidenceInvalid'}">
        <p class="examiner-flash-bar examiner-flash-bar--error">Ảnh minh chứng không hợp lệ. Chỉ nhận JPEG, PNG hoặc WebP, dung lượng tối đa 5 MB.</p>
    </c:if>
    <c:if test="${param.error eq 'evidenceTooLarge'}">
        <p class="examiner-flash-bar examiner-flash-bar--error">Ảnh minh chứng vượt quá dung lượng tối đa 5 MB.</p>
    </c:if>
    <c:if test="${param.error eq 'evidenceCloudinaryMissing'}">
        <p class="examiner-flash-bar examiner-flash-bar--error">Cloudinary chưa được cấu hình. Kiểm tra CLOUDINARY_CLOUD_NAME, CLOUDINARY_API_KEY và CLOUDINARY_API_SECRET.</p>
    </c:if>
    <c:if test="${param.error eq 'evidenceUploadFailed'}">
        <p class="examiner-flash-bar examiner-flash-bar--error">Không upload được ảnh minh chứng<c:if test="${not empty param.uploadMessage}">: <c:out value="${param.uploadMessage}"/></c:if>.</p>
    </c:if>
    <c:if test="${param.error eq 'undoFailed' or param.error eq 'unsuspendFailed'}">
        <p class="examiner-flash-bar examiner-flash-bar--error">Không gỡ được đình chỉ<c:if test="${not empty param.sbd}"> SBD ${param.sbd}</c:if>.</p>
    </c:if>
    <c:if test="${param.error eq 'undoPresentFailed'}">
        <p class="examiner-flash-bar examiner-flash-bar--error">Không thể hủy điểm danh vì thí sinh đang thi, chờ ký hoặc đã hoàn thành phần thi<c:if test="${not empty param.sbd}"> (SBD ${param.sbd})</c:if>.</p>
    </c:if>
    <c:if test="${param.scoreSaved eq '1'}">
        <p class="examiner-flash-bar examiner-flash-bar--success">Đã lưu điểm thực hành thành công.</p>
    </c:if>
    <c:if test="${param.error eq 'scoreFailed'}">
        <p class="examiner-flash-bar examiner-flash-bar--error">Không lưu được điểm thực hành<c:if test="${not empty param.sbd}"> SBD ${param.sbd}</c:if>.</p>
    </c:if>
    <c:if test="${param.error eq 'passwordIncorrect'}">
        <p class="examiner-flash-bar examiner-flash-bar--error">Mật khẩu xác nhận không đúng.</p>
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
        <p class="examiner-flash-bar examiner-flash-bar--error">Chưa có kỳ thi đang diễn ra.</p>
    </c:if>
    <c:if test="${param.error eq 'invokeSelectedFailed'}">
        <p class="examiner-flash-bar examiner-flash-bar--error">Chọn ít nhất một thí sinh để thao tác.</p>
    </c:if>
    <c:if test="${param.error eq 'presentFailed'}">
        <p class="examiner-flash-bar examiner-flash-bar--error">Không điểm danh được<c:if test="${not empty param.sbd}"> SBD ${param.sbd}</c:if>.</p>
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
    <c:if test="${param.error eq 'deductionFailed'}">
        <p class="examiner-flash-bar examiner-flash-bar--error">Không điều chỉnh được điểm trừ lỗi<c:if test="${not empty param.sbd}"> SBD ${param.sbd}</c:if>.</p>
    </c:if>
    <c:if test="${param.error eq 'invalidDeduction'}">
        <p class="examiner-flash-bar examiner-flash-bar--error">Thông tin lỗi trừ điểm không hợp lệ.</p>
    </c:if>
    <c:if test="${param.error eq 'needConfirmSave'}">
        <p class="examiner-flash-bar examiner-flash-bar--error">Chỉ lưu thay đổi điểm sau khi nhập mật khẩu và bấm xác nhận.</p>
    </c:if>
    <c:if test="${param.error eq 'suspendFailed'}">
        <p class="examiner-flash-bar examiner-flash-bar--error">Không đình chỉ được thí sinh<c:if test="${not empty param.sbd}"> SBD ${param.sbd}</c:if>.</p>
    </c:if>
    <c:if test="${param.error eq 'invokeFailed'}">
        <p class="examiner-flash-bar examiner-flash-bar--error">Không thực hiện được thao tác<c:if test="${not empty param.sbd}"> SBD ${param.sbd}</c:if>.</p>
    </c:if>
    <c:if test="${param.error eq 'finalizeFailed'}">
        <p class="examiner-flash-bar examiner-flash-bar--error">Hoàn tất nhập điểm thất bại<c:if test="${not empty param.sbd}"> (SBD ${param.sbd})</c:if>. Kiểm tra lại trạng thái thí sinh.</p>
    </c:if>
    <c:if test="${param.error eq 'needResultPrint'}">
        <p class="examiner-flash-bar examiner-flash-bar--error">Phải in biên bản kết quả thi ít nhất 1 lần trước khi hoàn tất<c:if test="${not empty param.sbd}"> (SBD ${param.sbd})</c:if>.</p>
    </c:if>
    <c:if test="${param.error eq 'completeFailed'}">
        <p class="examiner-flash-bar examiner-flash-bar--error">Không hoàn tất được phần thi<c:if test="${not empty param.sbd}"> SBD ${param.sbd}</c:if>.</p>
    </c:if>
    <c:if test="${param.error eq 'wrongInfoFailed'}">
        <p class="examiner-flash-bar examiner-flash-bar--error">Không chuyển được thí sinh về phòng thủ tục<c:if test="${not empty param.sbd}"> SBD ${param.sbd}</c:if>.</p>
    </c:if>
    <c:if test="${param.error eq 'resultPrintFailed' or param.error eq 'signaturePrintFailed'}">
        <p class="examiner-flash-bar examiner-flash-bar--error">Không in được biên bản kết quả thi<c:if test="${not empty param.sbd}"> SBD ${param.sbd}</c:if>. Thí sinh phải ở trạng thái Chờ ký.</p>
    </c:if>
    <c:if test="${param.error eq 'theoryNoResultEdit'}">
        <p class="examiner-flash-bar examiner-flash-bar--error">Phần thi lý thuyết không được phép sửa kết quả.</p>
    </c:if>
    <c:if test="${param.error eq 'vehicleFailed'}">
        <p class="examiner-flash-bar examiner-flash-bar--error">Không gán được xe cho thí sinh<c:if test="${not empty param.sbd}"> SBD ${param.sbd}</c:if>.</p>
    </c:if>
    <c:if test="${param.error eq 'invalidDevice'}">
        <p class="examiner-flash-bar examiner-flash-bar--error">Thiết bị không hợp lệ.</p>
    </c:if>
    <c:if test="${param.error eq 'practicalNotAllowed'}">
        <p class="examiner-flash-bar examiner-flash-bar--error">Thí sinh chưa đủ điều kiện thi thực hành hoặc đã trượt lý thuyết.</p>
    </c:if>
    <c:if test="${param.error eq 'scoreEditNotAllowed'}">
        <p class="examiner-flash-bar examiner-flash-bar--error">Chỉ có thể sửa kết quả sau khi thí sinh đã hoàn tất phần thi thực hành.</p>
    </c:if>
    <c:if test="${not empty editError}">
        <p class="examiner-flash-bar examiner-flash-bar--error">${editError}</p>
    </c:if>
</div>

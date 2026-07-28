package examiner.util;

// Pure mapping from examiner flash query codes to Vietnamese display text.
public final class ExaminerFlashMessages {

    private ExaminerFlashMessages() {
    }

    // Resolve the first matching success message from flash query params.
    public static String resolveSuccess(
            String successMessage,
            String saved,
            String suspended,
            String unsuspended,
            String sbd,
            String undoSuspended,
            String invoked,
            String invokedBatch,
            String presentDone,
            String maintenanceDone,
            String operationalDone,
            String scoreInvoked,
            String completeDone,
            String undoPresent,
            String wrongInfoDone,
            String finalized,
            String signatureMarked,
            String vehicleChanged,
            String scoreSaved) {
        if (notBlank(successMessage)) {
            return successMessage;
        }
        if ("1".equals(saved)) {
            return "Đã lưu.";
        }
        if (notBlank(suspended)) {
            return "Đã đình chỉ SBD " + suspended + ".";
        }
        if ("1".equals(unsuspended)) {
            return "Đã gỡ đình chỉ SBD " + nullToEmpty(sbd) + ".";
        }
        if (notBlank(unsuspended)) {
            return "Đã gỡ đình chỉ SBD " + unsuspended + ".";
        }
        if (notBlank(undoSuspended)) {
            return "Đã hoàn tác đình chỉ SBD " + undoSuspended + ".";
        }
        if (notBlank(invoked)) {
            return "Đã thao tác SBD " + invoked + ".";
        }
        if (notBlank(invokedBatch)) {
            return "Đã thao tác " + invokedBatch + " thí sinh.";
        }
        if (notBlank(presentDone)) {
            return "Đã điểm danh SBD " + presentDone + ".";
        }
        if (notBlank(maintenanceDone)) {
            return "Đã chuyển thiết bị " + maintenanceDone + " sang bảo trì.";
        }
        if (notBlank(operationalDone)) {
            return "Đã chuyển thiết bị " + operationalDone + " sang sử dụng.";
        }
        if ("1".equals(scoreInvoked) && notBlank(sbd)) {
            return "Đã thao tác thí sinh SBD " + sbd + " vào nhập điểm.";
        }
        if (notBlank(completeDone)) {
            return "Đã hoàn thành phần thi SBD " + completeDone + ".";
        }
        if (notBlank(undoPresent)) {
            return "Đã hủy điểm danh SBD " + undoPresent + ".";
        }
        if (notBlank(wrongInfoDone)) {
            return "Đã chuyển SBD " + wrongInfoDone + " về phòng thủ tục (sai thông tin).";
        }
        if ("1".equals(finalized)) {
            return "Hoàn tất nhập điểm thành công. Đang hiển thị thí sinh tiếp theo.";
        }
        if ("1".equals(signatureMarked) && notBlank(sbd)) {
            return "Đã in biên bản kết quả thi SBD " + sbd + ".";
        }
        if ("1".equals(vehicleChanged) && notBlank(sbd)) {
            return "Đã đổi xe thi cho SBD " + sbd + ".";
        }
        if ("1".equals(scoreSaved)) {
            return "Đã lưu điểm thực hành thành công.";
        }
        return null;
    }

    // Resolve the error message for a flash error code and optional dynamic values.
    public static String resolveError(String error, String sbd, String uploadMessage) {
        if (error == null || error.isBlank()) {
            return null;
        }
        switch (error) {
            case "saveFailed":
                return "Không lưu được biên bản vi phạm.";
            case "evidenceInvalid":
                return "Ảnh minh chứng không hợp lệ. Chỉ nhận JPEG, PNG hoặc WebP, dung lượng tối đa 5 MB.";
            case "evidenceTooLarge":
                return "Ảnh minh chứng vượt quá dung lượng tối đa 5 MB.";
            case "evidenceCloudinaryMissing":
                return "Cloudinary chưa được cấu hình. Kiểm tra CLOUDINARY_CLOUD_NAME, CLOUDINARY_API_KEY và CLOUDINARY_API_SECRET.";
            case "evidenceUploadFailed":
                if (notBlank(uploadMessage)) {
                    return "Không upload được ảnh minh chứng: " + uploadMessage + ".";
                }
                return "Không upload được ảnh minh chứng.";
            case "undoFailed":
            case "unsuspendFailed":
                if (notBlank(sbd)) {
                    return "Không gỡ được đình chỉ SBD " + sbd + ".";
                }
                return "Không gỡ được đình chỉ.";
            case "undoPresentFailed":
                if (notBlank(sbd)) {
                    return "Không thể hủy điểm danh vì thí sinh đang thi, chờ ký hoặc đã hoàn thành phần thi (SBD "
                            + sbd + ").";
                }
                return "Không thể hủy điểm danh vì thí sinh đang thi, chờ ký hoặc đã hoàn thành phần thi.";
            case "scoreFailed":
                if (notBlank(sbd)) {
                    return "Không lưu được điểm thực hành SBD " + sbd + ".";
                }
                return "Không lưu được điểm thực hành.";
            case "candidateNotCheckedIn":
                if (notBlank(sbd)) {
                    return "Thí sinh SBD " + sbd + " chưa được điểm danh.";
                }
                return "Thí sinh chưa được điểm danh.";
            case "candidateNotEligibleForPractical":
                return "Thí sinh chưa đủ điều kiện thi thực hành hoặc đã trượt lý thuyết.";
            case "theoryAttendanceRequired":
                return "Thí sinh phải điểm danh lý thuyết trước khi điểm danh thực hành.";
            case "procedureIncomplete":
                return "Thí sinh chưa hoàn tất thủ tục (thanh toán/ảnh) nên chưa điểm danh được.";
            case "vehicleInvalid":
                return "Xe/thiết bị không hợp lệ hoặc không thuộc khu vực thi hiện tại.";
            case "areaMismatch":
                return "Thí sinh không thuộc khu vực thi hiện tại.";
            case "vehicleOccupied":
                return "Xe đang được sử dụng bởi thí sinh khác.";
            case "scoreAlreadySaved":
                return "Điểm thực hành đã được lưu. Hãy dùng Sửa kết quả nếu cần chỉnh sửa.";
            case "scorePayloadInvalid":
                return "Dữ liệu lưu điểm không hợp lệ.";
            case "passwordIncorrect":
                return "Mật khẩu xác nhận không đúng.";
            case "noSbd":
                return "Không tìm thấy thí sinh.";
            case "alreadySuspended":
                return "Thí sinh đã bị đình chỉ.";
            case "notSuspended":
                return "Thí sinh chưa bị đình chỉ.";
            case "noSession":
                return "Chưa có kỳ thi đang diễn ra.";
            case "invokeSelectedFailed":
                return "Chọn ít nhất một thí sinh để thao tác.";
            case "presentFailed":
                if (notBlank(sbd)) {
                    return "Không điểm danh được SBD " + sbd + ".";
                }
                return "Không điểm danh được.";
            case "maintenanceFailed":
                return "Không chuyển được thiết bị sang bảo trì.";
            case "operationalFailed":
                return "Không chuyển được thiết bị sang sử dụng.";
            case "noCandidate":
                return "Không còn thí sinh trong hàng đợi nhập điểm.";
            case "deductionFailed":
                if (notBlank(sbd)) {
                    return "Không điều chỉnh được điểm trừ lỗi SBD " + sbd + ".";
                }
                return "Không điều chỉnh được điểm trừ lỗi.";
            case "invalidDeduction":
                return "Thông tin lỗi trừ điểm không hợp lệ.";
            case "needConfirmSave":
                return "Chỉ lưu thay đổi điểm sau khi nhập mật khẩu và bấm xác nhận.";
            case "suspendFailed":
                if (notBlank(sbd)) {
                    return "Không đình chỉ được thí sinh SBD " + sbd + ".";
                }
                return "Không đình chỉ được thí sinh.";
            case "invokeFailed":
                if (notBlank(sbd)) {
                    return "Không thực hiện được thao tác SBD " + sbd + ".";
                }
                return "Không thực hiện được thao tác.";
            case "finalizeFailed":
                if (notBlank(sbd)) {
                    return "Hoàn tất nhập điểm thất bại (SBD " + sbd + "). Kiểm tra lại trạng thái thí sinh.";
                }
                return "Hoàn tất nhập điểm thất bại. Kiểm tra lại trạng thái thí sinh.";
            case "needResultPrint":
                if (notBlank(sbd)) {
                    return "Phải in biên bản kết quả thi ít nhất 1 lần trước khi hoàn tất (SBD " + sbd + ").";
                }
                return "Phải in biên bản kết quả thi ít nhất 1 lần trước khi hoàn tất.";
            case "completeFailed":
                if (notBlank(sbd)) {
                    return "Không hoàn tất được phần thi SBD " + sbd + ".";
                }
                return "Không hoàn tất được phần thi.";
            case "wrongInfoFailed":
                if (notBlank(sbd)) {
                    return "Không chuyển được thí sinh về phòng thủ tục SBD " + sbd + ".";
                }
                return "Không chuyển được thí sinh về phòng thủ tục.";
            case "resultPrintFailed":
            case "signaturePrintFailed":
                if (notBlank(sbd)) {
                    return "Không in được biên bản kết quả thi SBD " + sbd + ". Thí sinh phải ở trạng thái Chờ ký.";
                }
                return "Không in được biên bản kết quả thi. Thí sinh phải ở trạng thái Chờ ký.";
            case "theoryNoResultEdit":
                return "Phần thi lý thuyết không được phép sửa kết quả.";
            case "vehicleFailed":
                if (notBlank(sbd)) {
                    return "Không gán được xe cho thí sinh SBD " + sbd + ".";
                }
                return "Không gán được xe cho thí sinh.";
            case "invalidDevice":
                return "Thiết bị không hợp lệ.";
            case "practicalNotAllowed":
                return "Thí sinh chưa đủ điều kiện thi thực hành hoặc đã trượt lý thuyết.";
            case "scoreEditNotAllowed":
                return "Chỉ có thể sửa kết quả khi thí sinh đã lưu điểm (chờ ký) hoặc đã hoàn tất phần thi.";
            default:
                return null;
        }
    }

    private static boolean notBlank(String value) {
        return value != null && !value.isBlank();
    }

    private static String nullToEmpty(String value) {
        return value != null ? value : "";
    }
}

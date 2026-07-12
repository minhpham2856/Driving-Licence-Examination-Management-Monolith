package examstaff.dto;

import shared.enums.ErrorType;

public final class ServiceResult<T> {

    private final boolean success;
    private final ErrorType errorType;
    private final String message;
    private final T data;

    private ServiceResult(boolean success, ErrorType errorType, String message, T data) {
        this.success = success;
        this.errorType = errorType;
        this.message = message;
        this.data = data;
    }

    public static <T> ServiceResult<T> ok(T data) {
        return new ServiceResult<>(true, null, null, data);
    }

    public static <T> ServiceResult<T> ok(T data, String message) {
        return new ServiceResult<>(true, null, message, data);
    }

    public static <T> ServiceResult<T> fail(ErrorType type, String message) {
        return new ServiceResult<>(false, type, message, null);
    }

    public static <T> ServiceResult<T> fail(ErrorType type, String message, T data) {
        return new ServiceResult<>(false, type, message, data);
    }

    public boolean isSuccess() {
        return success;
    }

    public ErrorType getErrorType() {
        return errorType;
    }

    public String getMessage() {
        return message;
    }

    public T getData() {
        return data;
    }
}


package examiner.dto;

import shared.enums.ErrorType;

// Generic success/failure wrapper for examiner service outcomes with ErrorType.
public record ServiceResult<T>(
        boolean success,
        ErrorType errorType,
        String message,
        T data) {

    // Factory method for success with optional data.
    public static <T> ServiceResult<T> ok(T data) {
        return new ServiceResult<>(true, null, null, data);
    }

    // Factory method for success with optional data and message.
    public static <T> ServiceResult<T> ok(T data, String message) {
        return new ServiceResult<>(true, null, message, data);
    }

    // Factory method for failure with ErrorType and message.
    public static <T> ServiceResult<T> fail(ErrorType type, String message) {
        return new ServiceResult<>(false, type, message, null);
    }

    // Factory method for failure with ErrorType, message, and data.
    public static <T> ServiceResult<T> fail(ErrorType type, String message, T data) {
        return new ServiceResult<>(false, type, message, data);
    }

    // Helper method to make checks read smoothly.
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

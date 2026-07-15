package managingstaff.dto;

public record OcrResultDTO(boolean success, String text, String errorMessage) {

    public static OcrResultDTO success(String text) {
        return new OcrResultDTO(true, text == null ? "" : text.trim(), null);
    }

    public static OcrResultDTO failure(String message) {
        return new OcrResultDTO(false, "", message == null ? "OCR không trả về kết quả." : message);
    }
}

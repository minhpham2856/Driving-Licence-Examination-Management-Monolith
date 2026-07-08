package dto.examstaff;

import dto.exam.ExamRegistrationDTO;

import java.util.List;

public class CandidateDstsImportPreviewDTO {

    private List<ExamRegistrationDTO> rows;
    private boolean hasInvalidRows;
    private int validImportCount;

    public List<ExamRegistrationDTO> getRows() {
        return rows;
    }

    public void setRows(List<ExamRegistrationDTO> rows) {
        this.rows = rows;
    }

    public boolean isHasInvalidRows() {
        return hasInvalidRows;
    }

    public void setHasInvalidRows(boolean hasInvalidRows) {
        this.hasInvalidRows = hasInvalidRows;
    }

    public int getValidImportCount() {
        return validImportCount;
    }

    public void setValidImportCount(int validImportCount) {
        this.validImportCount = validImportCount;
    }
}

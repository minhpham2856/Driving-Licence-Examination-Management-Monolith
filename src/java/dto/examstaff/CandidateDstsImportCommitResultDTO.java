package dto.examstaff;

public class CandidateDstsImportCommitResultDTO {

    private int importedCount;
    private int skippedCount;
    private String skipSummary;

    public int getImportedCount() {
        return importedCount;
    }

    public void setImportedCount(int importedCount) {
        this.importedCount = importedCount;
    }

    public int getSkippedCount() {
        return skippedCount;
    }

    public void setSkippedCount(int skippedCount) {
        this.skippedCount = skippedCount;
    }

    public String getSkipSummary() {
        return skipSummary;
    }

    public void setSkipSummary(String skipSummary) {
        this.skipSummary = skipSummary;
    }
}

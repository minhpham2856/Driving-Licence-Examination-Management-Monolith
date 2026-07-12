package examstaff.dto;

// Aggregated deduction reason for the end-of-day report. Built from
// DeductionRecord joined to ScoreDeduction.
public class InfractionDTO {

    private String reason;
    private int count;
    private double percentage;

    public InfractionDTO() {
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public double getPercentage() {
        return percentage;
    }

    public void setPercentage(double percentage) {
        this.percentage = percentage;
    }
}

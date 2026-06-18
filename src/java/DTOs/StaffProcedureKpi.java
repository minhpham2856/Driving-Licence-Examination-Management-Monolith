package DTOs;

public class StaffProcedureKpi {
    private final int completedCount;
    private final double totalFees;

    public StaffProcedureKpi(int completedCount, double totalFees) {
        this.completedCount = completedCount;
        this.totalFees = totalFees;
    }

    public int getCompletedCount() {
        return completedCount;
    }

    public double getTotalFees() {
        return totalFees;
    }
}

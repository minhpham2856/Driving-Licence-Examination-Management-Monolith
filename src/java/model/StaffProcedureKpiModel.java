package model;


public class StaffProcedureKpiModel {
    private final int completedCount;
    private final double totalFees;

    public StaffProcedureKpiModel(int completedCount, double totalFees) { this.completedCount = completedCount; this.totalFees = totalFees; }
    public int getCompletedCount() { return completedCount; }
    public double getTotalFees() { return totalFees; }
}

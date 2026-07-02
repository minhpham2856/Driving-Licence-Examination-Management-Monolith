<<<<<<<< Updated upstream:src/java/dto/staff/StaffProcedureKpiDTO.java
package dto.staff;
========
package model.staff;
>>>>>>>> Stashed changes:src/java/model/staff/StaffProcedureKpi.java

public class StaffProcedureKpiDTO {
    private final int completedCount;
    private final double totalFees;

    public StaffProcedureKpiDTO(int completedCount, double totalFees) {
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

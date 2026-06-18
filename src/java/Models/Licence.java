package Models;

import java.sql.Timestamp;

public class Licence {

    private int licenceId;
    private String licenceClass;
    private String description;
    private int minimumAge;
    private int validForYears;
    private Integer upgradeFromLicenceId;
    private String upgradeFromClass;
    private Timestamp createdAt;
    private Integer createdByUserId;
    private Timestamp updatedAt;
    private Integer updatedByUserId;

    public Licence() {
    }

    public Licence(int licenceId, String licenceClass, String description, int minimumAge, int validForYears, Integer upgradeFromLicenceId) {
        this.licenceId = licenceId;
        this.licenceClass = licenceClass;
        this.description = description;
        this.minimumAge = minimumAge;
        this.validForYears = validForYears;
        this.upgradeFromLicenceId = upgradeFromLicenceId;
    }

    public int getLicenceId() { return licenceId; }
    public void setLicenceId(int licenceId) { this.licenceId = licenceId; }

    public String getLicenceClass() { return licenceClass; }
    public void setLicenceClass(String licenceClass) { this.licenceClass = licenceClass; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public int getMinimumAge() { return minimumAge; }
    public void setMinimumAge(int minimumAge) { this.minimumAge = minimumAge; }

    public int getValidForYears() { return validForYears; }
    public void setValidForYears(int validForYears) { this.validForYears = validForYears; }

    public Integer getUpgradeFromLicenceId() { return upgradeFromLicenceId; }
    public void setUpgradeFromLicenceId(Integer upgradeFromLicenceId) { this.upgradeFromLicenceId = upgradeFromLicenceId; }

    public String getUpgradeFromClass() { return upgradeFromClass; }
    public void setUpgradeFromClass(String upgradeFromClass) { this.upgradeFromClass = upgradeFromClass; }

    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }

    public Integer getCreatedByUserId() { return createdByUserId; }
    public void setCreatedByUserId(Integer createdByUserId) { this.createdByUserId = createdByUserId; }

    public Timestamp getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Timestamp updatedAt) { this.updatedAt = updatedAt; }

    public Integer getUpdatedByUserId() { return updatedByUserId; }
    public void setUpdatedByUserId(Integer updatedByUserId) { this.updatedByUserId = updatedByUserId; }
}

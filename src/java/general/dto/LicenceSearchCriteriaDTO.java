package general.dto;

import java.util.ArrayList;
import java.util.List;

public class LicenceSearchCriteriaDTO {

    private String keyword;
    private List<String> vehicleTypes = new ArrayList<>();
    private List<String> durations = new ArrayList<>();
    private String sortBy = "licenceId";
    private String sortDir = "asc";

    public String getKeyword() {
        return keyword;
    }

    public void setKeyword(String keyword) {
        this.keyword = keyword;
    }

    public List<String> getVehicleTypes() {
        return vehicleTypes;
    }

    public void setVehicleTypes(List<String> vehicleTypes) {
        this.vehicleTypes = vehicleTypes != null ? vehicleTypes : new ArrayList<>();
    }

    public List<String> getDurations() {
        return durations;
    }

    public void setDurations(List<String> durations) {
        this.durations = durations != null ? durations : new ArrayList<>();
    }

    public String getSortBy() {
        return sortBy;
    }

    public void setSortBy(String sortBy) {
        this.sortBy = sortBy;
    }

    public String getSortDir() {
        return sortDir;
    }

    public void setSortDir(String sortDir) {
        this.sortDir = sortDir;
    }
}

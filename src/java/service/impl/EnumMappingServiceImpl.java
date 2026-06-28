package service.impl;

import enums.*;
import service.EnumMappingService;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class EnumMappingServiceImpl implements EnumMappingService {

    @Override
    public String auditLabel(String entityName) {
        if (entityName == null || entityName.isBlank()) return "-";
        String trimmed = entityName.trim();
        String key = trimmed.toUpperCase(Locale.ROOT)
                .replace(" ", "_")
                .replace("A?", "I")
                .replace("'", "O")
                .replace("", "O")
                .replace("?", "D")
                .replace(",", "A")
                .replace("", "Y")
                .replace("_", "E")
                .replace("", "A");
        
        for (AuditEntity e : AuditEntity.values()) {
            if (e.name().equals(key)) {
                return e.getLabelVi(); 
            }
        }
        
        return switch (trimmed.toUpperCase(Locale.ROOT)) {
            case "H' S ?,NG KA?", "THA? SINH" -> "ThA- sinh";
            case "K_T QU THI" -> "Kt qu thi";
            case "PHA'NG THI" -> "PhAng thi";
            default -> trimmed;
        };
    }

    @Override
    public String candidateStatusLabel(String status) {
        if (status == null) return CandidateStatus.PENDING.getLabelVi(); 
        for (CandidateStatus cs : CandidateStatus.values()) {
            if (cs.getStatus().equals(status)) {
                return cs.getLabelVi(); 
            }
        }
        return CandidateStatus.PENDING.getLabelVi();
    }

    @Override
    public boolean isCandidateAwaitingSignature(String status) {
        return CandidateStatus.AWAITING_SIGNATURE.getStatus().equals(status);
    }

    @Override
    public boolean isCandidateDone(String status) {
        return CandidateStatus.DONE.getStatus().equals(status);
    }

    @Override
    public boolean isPresentStatus(String registrationStatus) {
        if (registrationStatus == null) return false;
        return registrationStatus.equals("CheckedIn") || registrationStatus.equals("Present") || registrationStatus.equals("Completed");
    }

    @Override
    public String statusLabelVi(String status) {
        if (status == null) return "-";
        for (DeviceStatus ds : DeviceStatus.values()) {
            if (ds.getStatus().equalsIgnoreCase(status.trim())) { 
                return ds.getLabelVi(); 
            }
        }
        return status;
    }

    @Override
    public String statusCssClass(String status) {
        if (status == null) return "device-grid-card--unknown";
        for (DeviceStatus ds : DeviceStatus.values()) {
            if (ds.getStatus().equalsIgnoreCase(status.trim())) {
                return ds.getCssClass(); 
            }
        }
        return "device-grid-card--unknown";
    }

    @Override
    public boolean isComputer(String deviceType) {
        return deviceType != null && DeviceType.COMPUTER.getTypeName().equalsIgnoreCase(deviceType.trim());
    }

    @Override
    public boolean isVehicle(String deviceType) {
        if (deviceType == null) return false;
        String normalized = deviceType.trim();
        return DeviceType.MOTORCYCLE.getTypeName().equalsIgnoreCase(normalized) ||
               DeviceType.CAR.getTypeName().equalsIgnoreCase(normalized) ||
               DeviceType.TRUCK.getTypeName().equalsIgnoreCase(normalized);
    }

    @Override
    public List<String> vehicleTypesForLicence(String licenceClass) {
        String lc = licenceClass != null ? licenceClass.trim().toUpperCase(Locale.ROOT) : "";
        List<String> types = new ArrayList<>();
        if ("A1".equals(lc) || "A".equals(lc)) {
            types.add(DeviceType.MOTORCYCLE.getTypeName());
            return types;
        }
        if ("C".equals(lc) || lc.startsWith("D") || "FC".equals(lc)) {
            types.add(DeviceType.CAR.getTypeName());
            types.add(DeviceType.TRUCK.getTypeName());
            return types;
        }
        types.add(DeviceType.CAR.getTypeName());
        return types;
    }

    @Override
    public boolean matchesLicence(String licenceClass, String deviceType) {
        if (deviceType == null) return false;
        String normalized = deviceType.trim();
        for (String allowed : vehicleTypesForLicence(licenceClass)) {
            if (allowed.equalsIgnoreCase(normalized)) return true;
        }
        return false;
    }

    @Override
    public String iconFor(String deviceType) {
        if (deviceType == null) return "devices";
        for (DeviceType dt : DeviceType.values()) {
            if (dt.getTypeName().equalsIgnoreCase(deviceType.trim())) {
                return dt.getIcon(); 
            }
        }
        return "devices";
    }

    @Override
    public String typeLabelVi(String deviceType) {
        if (deviceType == null) return "Thit b<";
        for (DeviceType dt : DeviceType.values()) {
            if (dt.getTypeName().equalsIgnoreCase(deviceType.trim())) {
                return dt.getLabelVi(); 
            }
        }
        return deviceType;
    }

    @Override
    public boolean canStartSession(String status) {
        return ExamSessionStatus.SCHEDULED.getStatus().equalsIgnoreCase(status) || ExamSessionStatus.OPEN.getStatus().equalsIgnoreCase(status);
    }

    @Override
    public boolean isSessionInProgress(String status) {
        return ExamSessionStatus.IN_PROGRESS.getStatus().equalsIgnoreCase(status);
    }

    @Override
    public boolean isSessionEnded(String status) {
        return ExamSessionStatus.COMPLETED.getStatus().equalsIgnoreCase(status) || ExamSessionStatus.CANCELLED.getStatus().equalsIgnoreCase(status);
    }

    @Override
    public String sexFromSex(boolean isMale) {
        return isMale ? Sex.MALE.getLabelVi() : Sex.FEMALE.getLabelVi();
    }

    @Override
    public boolean sexFromSex(String sex) {
        if (sex == null) return false;
        String s = sex.trim();
        return s.equalsIgnoreCase("Nam") || s.equalsIgnoreCase("Male") || s.equals("M") || s.equals("1");
    }

    @Override
    public SectionType resolveSectionType(String sectionName) {
        if (sectionName == null || sectionName.isBlank()) {
            return SectionType.THEORY;
        }
        String normalized = sectionName.trim().toLowerCase();
        if (normalized.contains("lA thuyt") || normalized.contains("ly thuyet") || normalized.contains("theory")) {
            return SectionType.THEORY;
        }
        return SectionType.SCORE_BASED;
    }

    @Override
    public boolean isSidebarMenuDisabled(SectionType type, String menuKey) {
        if (type != SectionType.THEORY || menuKey == null) {
            return false;
        }
        return "score-entry".equals(menuKey) || "result-details".equals(menuKey);
    }

    @Override
    public String violationLabel(String code) {
        if (code == null || code.isBlank()) return "";
        for (ViolationReason reason : ViolationReason.values()) {
            if (reason.getCode().equalsIgnoreCase(code.trim())) {
                return reason.getLabel();
            }
        }
        return code.trim();
    }

    @Override
    public Map<String, String> violationMap() {
        Map<String, String> map = new LinkedHashMap<>();
        for (ViolationReason reason : ViolationReason.values()) {
            map.put(reason.getCode(), reason.getLabel());
        }
        return map;
    }

    @Override
    public List<Map<String, String>> violationOptionList() {
        List<Map<String, String>> list = new ArrayList<>();
        for (ViolationReason reason : ViolationReason.values()) {
            Map<String, String> row = new LinkedHashMap<>();
            row.put("code", reason.getCode());
            row.put("label", reason.getLabel());
            list.add(row);
        }
        return list;
    }
}

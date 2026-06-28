package service;

import enums.SectionType;
import java.util.List;
import java.util.Map;

public interface EnumMappingService {

    // From AuditEntity
    String auditLabel(String entityName);

    // From CandidateStatus
    String candidateStatusLabel(String status);
    boolean isCandidateAwaitingSignature(String status);
    boolean isCandidateDone(String status);
    boolean isPresentStatus(String registrationStatus);

    // From DeviceStatus
    String statusLabelVi(String status);
    String statusCssClass(String status);

    // From DeviceType
    boolean isComputer(String deviceType);
    boolean isVehicle(String deviceType);
    List<String> vehicleTypesForLicence(String licenceClass);
    boolean matchesLicence(String licenceClass, String deviceType);
    String iconFor(String deviceType);
    String typeLabelVi(String deviceType);

    // From ExamSessionStatus
    boolean canStartSession(String status);
    boolean isSessionInProgress(String status);
    boolean isSessionEnded(String status);

    // From Gender
    String sexFromGender(boolean isMale);
    boolean genderFromSex(String sex);

    // From SectionType
    SectionType resolveSectionType(String sectionName);
    boolean isSidebarMenuDisabled(SectionType type, String menuKey);

    // From ViolationReason
    String violationLabel(String code);
    Map<String, String> violationMap();
    List<Map<String, String>> violationOptionList();

}

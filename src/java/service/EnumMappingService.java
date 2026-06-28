package service;

import enums.SectionType;
import java.util.List;
import java.util.Map;

public interface EnumMappingService {

    
    String auditLabel(String entityName);

    
    String candidateStatusLabel(String status);
    boolean isCandidateAwaitingSignature(String status);
    boolean isCandidateDone(String status);
    boolean isPresentStatus(String registrationStatus);

    
    String statusLabelVi(String status);
    String statusCssClass(String status);

    
    boolean isComputer(String deviceType);
    boolean isVehicle(String deviceType);
    List<String> vehicleTypesForLicence(String licenceClass);
    boolean matchesLicence(String licenceClass, String deviceType);
    String iconFor(String deviceType);
    String typeLabelVi(String deviceType);

    
    boolean canStartSession(String status);
    boolean isSessionInProgress(String status);
    boolean isSessionEnded(String status);

    
    String sexFromSex(boolean isMale);
    boolean sexFromSex(String sex);

    
    SectionType resolveSectionType(String sectionName);
    boolean isSidebarMenuDisabled(SectionType type, String menuKey);

    
    String violationLabel(String code);
    Map<String, String> violationMap();
    List<Map<String, String>> violationOptionList();

}

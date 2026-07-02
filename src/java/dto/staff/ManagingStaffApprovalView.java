package dto.staff;

import dto.registrant.RegistrantDocumentView;
import java.util.List;
import java.util.Map;

/**
 * Dữ liệu hiển thị màn duyệt hồ sơ cho ban quản lý (ManagingStaff).
 */
public class ManagingStaffApprovalView {

    private int id;
    private int userId;
    private String code;
    private String fullName;
    private String cccd;
    private String dob;
    private String gender;
    private String phone;
    private String licenseClass;
    private String type;
    private String typeName;
    private String registerDate;
    private Map<String, RegistrantDocumentView> documentsByType;
    private List<RegistrantDocumentView> otherDocuments;

    private int workflowExamRegistrationId;
    private boolean supplementApproval;

    public int getWorkflowExamRegistrationId() {
        return workflowExamRegistrationId;
    }

    public void setWorkflowExamRegistrationId(int workflowExamRegistrationId) {
        this.workflowExamRegistrationId = workflowExamRegistrationId;
    }

    public boolean isSupplementApproval() {
        return supplementApproval;
    }

    public void setSupplementApproval(boolean supplementApproval) {
        this.supplementApproval = supplementApproval;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getCccd() {
        return cccd;
    }

    public void setCccd(String cccd) {
        this.cccd = cccd;
    }

    public String getDob() {
        return dob;
    }

    public void setDob(String dob) {
        this.dob = dob;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getLicenseClass() {
        return licenseClass;
    }

    public void setLicenseClass(String licenseClass) {
        this.licenseClass = licenseClass;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getTypeName() {
        return typeName;
    }

    public void setTypeName(String typeName) {
        this.typeName = typeName;
    }

    public String getRegisterDate() {
        return registerDate;
    }

    public void setRegisterDate(String registerDate) {
        this.registerDate = registerDate;
    }

    public Map<String, RegistrantDocumentView> getDocumentsByType() {
        return documentsByType;
    }

    public void setDocumentsByType(Map<String, RegistrantDocumentView> documentsByType) {
        this.documentsByType = documentsByType;
    }

    public List<RegistrantDocumentView> getOtherDocuments() {
        return otherDocuments;
    }

    public void setOtherDocuments(List<RegistrantDocumentView> otherDocuments) {
        this.otherDocuments = otherDocuments;
    }
}

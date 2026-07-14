package examstaff.dto.exam;


import java.sql.Timestamp;
import java.sql.Date;

public class ExamRegistrationDTO {
    private int id;
    private int examId;
    private int examEnrollmentId;
    private int candidateNo;
    private String registrationType;
    private boolean isPaymentCompleted;
    private boolean isPresent;
    private boolean absent;
    private boolean suspended;
    private Timestamp presentMarkedAt;
    private String notes;

    // Helper fields joined from Person
    private String fullName;
    private String govIdNo;
    private Date dateOfBirth;
    private String phoneNo;
    private String email;
    private String photoUrl;

    // Helper fields from allocation/pipeline
    private String computerCode;
    private String theoryPassed = "none";
    private String practicalPassed = "none";
    private Integer theoryScore;
    private Integer practicalScore;
    private String licenseCode;

    private Integer allocatedAreaId;
    private String allocatedAreaName;
    private Integer practicalAllocatedAreaId;
    private String practicalAllocatedAreaName;
    private boolean validCapturedPhoto;
    /** NULL = thi phần đó; false = bảo lưu, không thi lại. */
    private Boolean takeTheory;
    private Boolean takePractical;
    private Date examDate;

    public ExamRegistrationDTO() {
    }

    public ExamRegistrationDTO(int id, int examId, int candidateNo, String registrationType, boolean isPaymentCompleted, boolean isPresent, Timestamp presentMarkedAt, String notes) {
        this.id = id;
        this.examId = examId;
        this.candidateNo = candidateNo;
        this.registrationType = registrationType;
        this.isPaymentCompleted = isPaymentCompleted;
        this.isPresent = isPresent;
        this.presentMarkedAt = presentMarkedAt;
        this.notes = notes;
    }

    public String getSbd() {
        if (candidateNo <= 0) {
            return "000";
        }
        return candidateNo < 1000
                ? String.format("%03d", candidateNo)
                : String.valueOf(candidateNo);
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getExamId() {
        return examId;
    }

    public void setExamId(int examId) {
        this.examId = examId;
    }

    public int getExamEnrollmentId() {
        return examEnrollmentId;
    }

    public void setExamEnrollmentId(int examEnrollmentId) {
        this.examEnrollmentId = examEnrollmentId;
    }

    public int getCandidateNo() {
        return candidateNo;
    }

    public void setCandidateNo(int candidateNo) {
        this.candidateNo = candidateNo;
    }

    public String getRegistrationType() {
        return registrationType;
    }

    public void setRegistrationType(String registrationType) {
        this.registrationType = registrationType;
    }

    public boolean isIsPaymentCompleted() {
        return isPaymentCompleted;
    }

    public boolean isPaymentCompleted() {
        return isPaymentCompleted;
    }

    public void setIsPaymentCompleted(boolean isPaymentCompleted) {
        this.isPaymentCompleted = isPaymentCompleted;
    }

    public boolean isPresent() {
        return isPresent;
    }

    public void setIsPresent(boolean isPresent) {
        this.isPresent = isPresent;
    }

    public boolean isAbsent() {
        return absent;
    }

    public void setAbsent(boolean absent) {
        this.absent = absent;
    }

    public boolean isSuspended() {
        return suspended;
    }

    public void setSuspended(boolean suspended) {
        this.suspended = suspended;
    }

    public Timestamp getPresentMarkedAt() {
        return presentMarkedAt;
    }

    public void setPresentMarkedAt(Timestamp presentMarkedAt) {
        this.presentMarkedAt = presentMarkedAt;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getGovIdNo() {
        return govIdNo;
    }

    public void setGovIdNo(String govIdNo) {
        this.govIdNo = govIdNo;
    }

    public Date getDateOfBirth() {
        return dateOfBirth;
    }

    public void setDateOfBirth(Date dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }

    public String getPhoneNo() {
        return phoneNo;
    }

    public void setPhoneNo(String phoneNo) {
        this.phoneNo = phoneNo;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhotoUrl() {
        return photoUrl;
    }

    public void setPhotoUrl(String photoUrl) {
        this.photoUrl = photoUrl;
    }

    public String getComputerCode() {
        return computerCode;
    }

    public void setComputerCode(String computerCode) {
        this.computerCode = computerCode;
    }

    public String getTheoryPassed() {
        return theoryPassed;
    }

    public void setTheoryPassed(String theoryPassed) {
        this.theoryPassed = theoryPassed;
    }

    public String getPracticalPassed() {
        return practicalPassed;
    }

    public void setPracticalPassed(String practicalPassed) {
        this.practicalPassed = practicalPassed;
    }

    public Integer getTheoryScore() {
        return theoryScore;
    }

    public void setTheoryScore(Integer theoryScore) {
        this.theoryScore = theoryScore;
    }

    public Integer getPracticalScore() {
        return practicalScore;
    }

    public void setPracticalScore(Integer practicalScore) {
        this.practicalScore = practicalScore;
    }

    public String getLicenseCode() {
        return licenseCode;
    }

    public void setLicenseCode(String licenseCode) {
        this.licenseCode = licenseCode;
    }

    public Integer getAllocatedAreaId() {
        return allocatedAreaId;
    }

    public void setAllocatedAreaId(Integer allocatedAreaId) {
        this.allocatedAreaId = allocatedAreaId;
    }

    public String getAllocatedAreaName() {
        return allocatedAreaName;
    }

    public void setAllocatedAreaName(String allocatedAreaName) {
        this.allocatedAreaName = allocatedAreaName;
    }

    public Integer getPracticalAllocatedAreaId() {
        return practicalAllocatedAreaId;
    }

    public void setPracticalAllocatedAreaId(Integer practicalAllocatedAreaId) {
        this.practicalAllocatedAreaId = practicalAllocatedAreaId;
    }

    public String getPracticalAllocatedAreaName() {
        return practicalAllocatedAreaName;
    }

    public void setPracticalAllocatedAreaName(String practicalAllocatedAreaName) {
        this.practicalAllocatedAreaName = practicalAllocatedAreaName;
    }

    public boolean isValidCapturedPhoto() {
        return validCapturedPhoto;
    }

    public void setValidCapturedPhoto(boolean validCapturedPhoto) {
        this.validCapturedPhoto = validCapturedPhoto;
    }

    public Boolean getTakeTheory() {
        return takeTheory;
    }

    public void setTakeTheory(Boolean takeTheory) {
        this.takeTheory = takeTheory;
    }

    public Boolean getTakePractical() {
        return takePractical;
    }

    public void setTakePractical(Boolean takePractical) {
        this.takePractical = takePractical;
    }

    /** Bảo lưu lý thuyết, không thi lại phần này. */
    public boolean skipsTheory() {
        return Boolean.FALSE.equals(takeTheory);
    }

    /** JSP EL: {@code ${profile.skipsTheory}} */
    public boolean isSkipsTheory() {
        return skipsTheory();
    }

    /** Chỉ thi lại lý thuyết - bảo lưu thực hành/sa hình. */
    public boolean skipsPractical() {
        return Boolean.FALSE.equals(takePractical);
    }

    /** JSP EL: {@code ${profile.skipsPractical}} */
    public boolean isSkipsPractical() {
        return skipsPractical();
    }

    public Date getExamDate() {
        return examDate;
    }

    public void setExamDate(Date examDate) {
        this.examDate = examDate;
    }

    /** Alias for JSP / legacy ExamStaff views. */
    public String getName() {
        return fullName;
    }

    /** Alias for JSP / legacy ExamStaff views. */
    public String getClazz() {
        return licenseCode;
    }

    public java.sql.Date getDob() {
        return dateOfBirth;
    }

    public String getCccd() {
        return govIdNo;
    }

    /** Thủ tục hoàn tất: đối chiếu hồ sơ + chụp ảnh + thu lệ phí. Không tính vắng/đình chỉ. */
    public boolean isProcedureComplete() {
        if (isAbsent() || isSuspended()) {
            return false;
        }
        if (!isPaymentCompleted) {
            return false;
        }
        boolean hasPhoto = validCapturedPhoto;
        return hasPhoto;
    }

    /** Đã xong toàn bộ kỳ thi (đủ phần thi theo hạng bằng). */
    public boolean isExamFinished() {
        if (isSuspended()) {
            return false;
        }
        if (isAbsent()) {
            return true;
        }
        if (!isPaymentCompleted) {
            return false;
        }
        if ("failed".equalsIgnoreCase(theoryPassed)) {
            return true;
        }
        if (skipsTheory()) {
            if ("failed".equalsIgnoreCase(practicalPassed)) {
                return true;
            }
            return "passed".equalsIgnoreCase(practicalPassed);
        }
        if (skipsPractical() && "passed".equalsIgnoreCase(theoryPassed)) {
            return true;
        }
        if ("failed".equalsIgnoreCase(practicalPassed) && !skipsPractical()) {
            return true;
        }
        String practical = effectivePracticalPassed();
        return "passed".equalsIgnoreCase(theoryPassed)
                && "passed".equalsIgnoreCase(practical);
    }

    public boolean isFinalPass() {
        if (isSuspended()) {
            return false;
        }
        if (isAbsent()) {
            return false;
        }
        if (skipsTheory()) {
            return "passed".equalsIgnoreCase(practicalPassed);
        }
        if (!"passed".equalsIgnoreCase(theoryPassed)) {
            return false;
        }
        if (skipsPractical()) {
            return true;
        }
        return "passed".equalsIgnoreCase(effectivePracticalPassed());
    }

    private String effectivePracticalPassed() {
        if (skipsPractical() && "passed".equalsIgnoreCase(theoryPassed)) {
            return "passed";
        }
        return practicalPassed == null || practicalPassed.isBlank() ? "none" : practicalPassed.trim();
    }
}

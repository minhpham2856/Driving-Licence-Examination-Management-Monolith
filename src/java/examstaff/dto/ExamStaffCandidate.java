package examstaff.dto;

import java.sql.Date;
import java.sql.Timestamp;

/**
 * Read-model Persistence cho thí sinh ExamStaff: một hàng SELECT JOIN
 * Candidate + Enrollment (+ kết quả / phân bổ / Person) trước khi map sang UI DTO.
 *
 * Vai trò trong luồng examstaff:
 * Nằm ở biên Persistence → BLL. DAO view đổ dữ liệu thô vào class này;
 * ExamStaffCandidateMapper chuyển sang ExamRegistrationDTO để Presentation/JSP dùng.
 * Không bind trực tiếp lên JSP; không chứa logic nghiệp vụ (vắng, đậu, thủ tục…).
 *
 * Ai tạo:
 * ExamStaffCandidateViewDAOImpl#mapRow (và các query list theo kỳ thi).
 *
 * Ai tiêu thụ:
 * ExamStaffCandidateMapper → sản phẩm là ExamRegistrationDTO;
 * CandidateQueueQueryServiceImpl gọi DAO rồi map sang danh sách đăng ký cho queue.
 *
 * Trang / servlet:
 * Không gắn attribute request trực tiếp. Dữ liệu sau map xuất hiện trên
 * dashboard.jsp, candidatecall.jsp, allocation, dossier, … qua ExamRegistrationDTO.
 */
public class ExamStaffCandidate {

    private int candidateId;
    private int examId;
    private int examEnrollmentId;
    private int candidateNo;
    private String registrationType;
    private boolean paymentCompleted;
    private boolean present;
    private boolean absent;
    private boolean suspended;
    private Timestamp presentMarkedAt;
    private String notes;
    private String fullName;
    private String govIdNo;
    private Date dateOfBirth;
    private boolean male;
    private String phoneNo;
    private String email;
    private String photoUrl;
    private String licenseCode;
    private String computerCode;
    private String address;
    private String reasonForTaking;
    private Boolean takeTheory;
    private Boolean takePractical;
    private Date examDate;
    private String sectionStatus;
    private boolean signaturePrinted;
    private Integer allocatedAreaId;
    private String allocatedAreaName;
    private Integer practicalAllocatedAreaId;
    private String practicalAllocatedAreaName;
    private Integer theoryScore;
    private boolean wrongCriticalTheory;
    private Integer practicalScore;

    /** Mã thí sinh (Candidate.id) từ view JOIN. */
    public int getCandidateId() {
        return candidateId;
    }

    /** Gán mã thí sinh từ hàng DAO. */
    public void setCandidateId(int candidateId) {
        this.candidateId = candidateId;
    }

    /** Mã kỳ thi của enrollment. */
    public int getExamId() {
        return examId;
    }

    /** Gán mã kỳ thi. */
    public void setExamId(int examId) {
        this.examId = examId;
    }

    /** Mã ExamEnrollment (khóa enrollment trong kỳ). */
    public int getExamEnrollmentId() {
        return examEnrollmentId;
    }

    /** Gán mã ExamEnrollment. */
    public void setExamEnrollmentId(int examEnrollmentId) {
        this.examEnrollmentId = examEnrollmentId;
    }

    /** Số báo danh dạng số (map sang SBD trên ExamRegistrationDTO). */
    public int getCandidateNo() {
        return candidateNo;
    }

    /** Gán số báo danh thô. */
    public void setCandidateNo(int candidateNo) {
        this.candidateNo = candidateNo;
    }

    /** Loại đăng ký trên enrollment. */
    public String getRegistrationType() {
        return registrationType;
    }

    /** Gán loại đăng ký. */
    public void setRegistrationType(String registrationType) {
        this.registrationType = registrationType;
    }

    /** Đã thu lệ phí thủ tục theo cột DB. */
    public boolean isPaymentCompleted() {
        return paymentCompleted;
    }

    /** Gán cờ thanh toán lệ phí từ DB. */
    public void setPaymentCompleted(boolean paymentCompleted) {
        this.paymentCompleted = paymentCompleted;
    }

    /** Đã điểm danh có mặt. */
    public boolean isPresent() {
        return present;
    }

    /** Gán cờ có mặt. */
    public void setPresent(boolean present) {
        this.present = present;
    }

    /** Cờ vắng mặt trên enrollment. */
    public boolean isAbsent() {
        return absent;
    }

    /** Gán cờ vắng. */
    public void setAbsent(boolean absent) {
        this.absent = absent;
    }

    /** Cờ đình chỉ trên enrollment. */
    public boolean isSuspended() {
        return suspended;
    }

    /** Gán cờ đình chỉ. */
    public void setSuspended(boolean suspended) {
        this.suspended = suspended;
    }

    /** Thời điểm đánh dấu có mặt (DB timestamp). */
    public Timestamp getPresentMarkedAt() {
        return presentMarkedAt;
    }

    /** Gán thời điểm điểm danh. */
    public void setPresentMarkedAt(Timestamp presentMarkedAt) {
        this.presentMarkedAt = presentMarkedAt;
    }

    /** Ghi chú enrollment từ DB. */
    public String getNotes() {
        return notes;
    }

    /** Gán ghi chú enrollment. */
    public void setNotes(String notes) {
        this.notes = notes;
    }

    /** Họ tên từ Person / Profile JOIN. */
    public String getFullName() {
        return fullName;
    }

    /** Gán họ tên. */
    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    /** Số CCCD / giấy tờ định danh. */
    public String getGovIdNo() {
        return govIdNo;
    }

    /** Gán số CCCD. */
    public void setGovIdNo(String govIdNo) {
        this.govIdNo = govIdNo;
    }

    /** Ngày sinh trên hồ sơ. */
    public Date getDateOfBirth() {
        return dateOfBirth;
    }

    /** Gán ngày sinh. */
    public void setDateOfBirth(Date dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }

    /** Giới tính nam (true) / nữ (false) theo cột Person. */
    public boolean isMale() {
        return male;
    }

    /** Gán giới tính từ hàng Person. */
    public void setMale(boolean male) {
        this.male = male;
    }

    /** Số điện thoại liên hệ. */
    public String getPhoneNo() {
        return phoneNo;
    }

    /** Gán số điện thoại. */
    public void setPhoneNo(String phoneNo) {
        this.phoneNo = phoneNo;
    }

    /** Email liên hệ. */
    public String getEmail() {
        return email;
    }

    /** Gán email. */
    public void setEmail(String email) {
        this.email = email;
    }

    /** URL / path ảnh hồ sơ từ DB. */
    public String getPhotoUrl() {
        return photoUrl;
    }

    /** Gán path ảnh hồ sơ. */
    public void setPhotoUrl(String photoUrl) {
        this.photoUrl = photoUrl;
    }

    /** Mã hạng bằng lái. */
    public String getLicenseCode() {
        return licenseCode;
    }

    /** Gán mã hạng bằng. */
    public void setLicenseCode(String licenseCode) {
        this.licenseCode = licenseCode;
    }

    /** Mã máy thi lý thuyết (nếu đã gán). */
    public String getComputerCode() {
        return computerCode;
    }

    /** Gán mã máy thi. */
    public void setComputerCode(String computerCode) {
        this.computerCode = computerCode;
    }

    /** Địa chỉ thường trú / liên hệ trên hồ sơ (chỉ có ở read-model, không đẩy hết sang UI DTO). */
    public String getAddress() {
        return address;
    }

    /** Gán địa chỉ. */
    public void setAddress(String address) {
        this.address = address;
    }

    /** Lý do dự thi / thi lại theo hồ sơ đăng ký. */
    public String getReasonForTaking() {
        return reasonForTaking;
    }

    /** Gán lý do dự thi. */
    public void setReasonForTaking(String reasonForTaking) {
        this.reasonForTaking = reasonForTaking;
    }

    /** Cờ thi phần lý thuyết (false = bảo lưu). */
    public Boolean getTakeTheory() {
        return takeTheory;
    }

    /** Gán cờ thi / bảo lưu lý thuyết. */
    public void setTakeTheory(Boolean takeTheory) {
        this.takeTheory = takeTheory;
    }

    /** Cờ thi phần thực hành (false = bảo lưu). */
    public Boolean getTakePractical() {
        return takePractical;
    }

    /** Gán cờ thi / bảo lưu thực hành. */
    public void setTakePractical(Boolean takePractical) {
        this.takePractical = takePractical;
    }

    /** Ngày thi gắn enrollment. */
    public Date getExamDate() {
        return examDate;
    }

    /** Gán ngày thi. */
    public void setExamDate(Date examDate) {
        this.examDate = examDate;
    }

    /** Trạng thái section / phần thi tổng hợp từ view (chuỗi DB). */
    public String getSectionStatus() {
        return sectionStatus;
    }

    /** Gán trạng thái section từ view. */
    public void setSectionStatus(String sectionStatus) {
        this.sectionStatus = sectionStatus;
    }

    /** Đã in phiếu / chữ ký liên quan enrollment chưa. */
    public boolean isSignaturePrinted() {
        return signaturePrinted;
    }

    /** Gán cờ đã in chữ ký / phiếu. */
    public void setSignaturePrinted(boolean signaturePrinted) {
        this.signaturePrinted = signaturePrinted;
    }

    /** Id khu vực đã phân bổ (chung / lý thuyết). */
    public Integer getAllocatedAreaId() {
        return allocatedAreaId;
    }

    /** Gán id khu vực phân bổ. */
    public void setAllocatedAreaId(Integer allocatedAreaId) {
        this.allocatedAreaId = allocatedAreaId;
    }

    /** Tên khu vực đã phân bổ. */
    public String getAllocatedAreaName() {
        return allocatedAreaName;
    }

    /** Gán tên khu vực phân bổ. */
    public void setAllocatedAreaName(String allocatedAreaName) {
        this.allocatedAreaName = allocatedAreaName;
    }

    /** Id khu vực thực hành đã phân. */
    public Integer getPracticalAllocatedAreaId() {
        return practicalAllocatedAreaId;
    }

    /** Gán id khu vực thực hành. */
    public void setPracticalAllocatedAreaId(Integer practicalAllocatedAreaId) {
        this.practicalAllocatedAreaId = practicalAllocatedAreaId;
    }

    /** Tên khu vực thực hành đã phân. */
    public String getPracticalAllocatedAreaName() {
        return practicalAllocatedAreaName;
    }

    /** Gán tên khu vực thực hành. */
    public void setPracticalAllocatedAreaName(String practicalAllocatedAreaName) {
        this.practicalAllocatedAreaName = practicalAllocatedAreaName;
    }

    /** Điểm lý thuyết từ kết quả thi (null nếu chưa có). */
    public Integer getTheoryScore() {
        return theoryScore;
    }

    /** Gán điểm lý thuyết. */
    public void setTheoryScore(Integer theoryScore) {
        this.theoryScore = theoryScore;
    }

    /** Điểm thực hành từ kết quả thi (null nếu chưa có). */
    /** True when at least one critical theory question is wrong or unanswered. */
    public boolean hasWrongCriticalTheory() {
        return wrongCriticalTheory;
    }

    public void setWrongCriticalTheory(boolean wrongCriticalTheory) {
        this.wrongCriticalTheory = wrongCriticalTheory;
    }

    public Integer getPracticalScore() {
        return practicalScore;
    }

    /** Gán điểm thực hành. */
    public void setPracticalScore(Integer practicalScore) {
        this.practicalScore = practicalScore;
    }

}

package examstaff.dto;


import java.sql.Timestamp;
import java.sql.Date;

/**
 * DTO đăng ký / hồ sơ thí sinh trong một kỳ thi — envelope nghiệp vụ trung tâm của luồng ExamStaff.
 *
 * Vai trò trong luồng examstaff:
 *
 * Đây là đối tượng dữ liệu mà hầu hết màn hình staff và bảng gọi công khai đều bind lên request/session:
 * hàng chờ gọi, hồ sơ bàn thủ tục, phân bổ khu vực, dossier, báo cáo, snapshot TV.
 * Mỗi instance thường tương ứng một enrollment (đăng ký thi) kèm thông tin Person đã JOIN
 * và cờ trạng thái vận hành (có mặt / vắng / đình chỉ / lệ phí / ảnh).
 *
 * Ai tạo:
 * - ExamRegistrationDAOImpl — map trực tiếp từ JDBC khi đọc bảng đăng ký.
 * - ExamStaffCandidateMapper#toDto — chuyển từ read-model ExamStaffCandidate
 *       (DAO view JOIN Candidate+Enrollment) sang DTO này.
 * - Một số service (ví dụ thủ tục / thanh toán) có thể tạo stub rỗng rồi đổ field.
 *
 * Ai tiêu thụ:
 * - BLL: RegistrationServiceImpl, CandidateQueue*, Procedure*,
 *       StaffCallServiceImpl, Allocation*, DocumentServiceImpl,
 *       CallQueueRules, ExamEnrollmentMerge.
 * - Presentation binders: ExamStaffPageBinder, PublicCallSnapshotSupport.
 * - Servlet: DashboardServlet, CandidateCallServlet, ProcedureServlet,
 *       AllocationServlet, PublicCallServlet, CandidateDossierServlet,
 *       ReportServlet, …
 *
 * Trang / JSP liên quan:
 * Bound dưới các attribute như candidateQueue, callingCandidate, profile,
 * waitingQueue, … trên dashboard.jsp, candidatecall.jsp (+ include
 * procedure.jsp), candidate-suspended.jsp, candidate-dossier.jsp,
 * các JSP phân bổ, public-call.jsp, báo cáo.
 * <p>Không chứa Servlet API; chỉ mang dữ liệu qua Presentation ↔ BLL.</p>
 */
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

    // Thông tin Person (JOIN từ bảng người / hồ sơ cá nhân)
    private String fullName;
    private String govIdNo;
    private Date dateOfBirth;
    private String phoneNo;
    private String email;
    private String photoUrl;

    // Kết quả phần thi / phân bổ khu vực / hạng bằng
    private String computerCode;
    private String theoryPassed = "none";
    private String practicalPassed = "none";
    private Integer theoryScore;
    private boolean wrongCriticalTheory;
    private Integer practicalScore;
    private String licenseCode;

    private Integer allocatedAreaId;
    private String allocatedAreaName;
    private Integer practicalAllocatedAreaId;
    private String practicalAllocatedAreaName;
    private boolean validCapturedPhoto;
    /** null = thi phần đó; false = bảo lưu, không thi lại phần này. */
    private Boolean takeTheory;
    private Boolean takePractical;
    private Date examDate;

    /** Khởi tạo rỗng — dùng khi tạo stub rồi set từng field từ mapper/service. */
    public ExamRegistrationDTO() {
    }

    /**
     * Khởi tạo nhanh các field lõi enrollment (không gồm Person JOIN / kết quả thi).
     * @param id                 mã đăng ký / candidate id tùy ngữ cảnh map
     * @param examId             kỳ thi
     * @param candidateNo        số báo danh số (SBD thô)
     * @param registrationType   loại đăng ký
     * @param isPaymentCompleted đã thu lệ phí thủ tục chưa
     * @param isPresent          đã điểm danh có mặt chưa
     * @param presentMarkedAt    thời điểm đánh dấu có mặt
     * @param notes              ghi chú vận hành
     */
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

    /**
     * Số báo danh dạng chuỗi 3 chữ số (pad từ candidateNo) để hiển thị / so khớp bảng gọi.
     * Nếu candidateNo <= 0 trả "000"; nếu >= 1000 giữ nguyên dạng số đầy đủ.
     * @return SBD chuẩn hóa cho UI và CallBoard
     */
    public String getSbd() {
        if (candidateNo <= 0) {
            return "000";
        }
        return candidateNo < 1000
                ? String.format("%03d", candidateNo)
                : String.valueOf(candidateNo);
    }

    /** Mã định danh đăng ký / thí sinh (key nghiệp vụ tùy nguồn map DAO). */
    public int getId() {
        return id;
    }

    /** Gán mã định danh đăng ký / thí sinh. */
    public void setId(int id) {
        this.id = id;
    }

    /** Mã kỳ thi mà enrollment này thuộc về. */
    public int getExamId() {
        return examId;
    }

    /** Gán mã kỳ thi chứa đăng ký. */
    public void setExamId(int examId) {
        this.examId = examId;
    }

    /** Mã enrollment kỳ thi (FK ExamEnrollment) — phân biệt với getId(). */
    public int getExamEnrollmentId() {
        return examEnrollmentId;
    }

    /** Gán mã enrollment kỳ thi. */
    public void setExamEnrollmentId(int examEnrollmentId) {
        this.examEnrollmentId = examEnrollmentId;
    }

    /** Số báo danh dạng số nguyên (nguồn để format getSbd()). */
    public int getCandidateNo() {
        return candidateNo;
    }

    /** Gán số báo danh thô. */
    public void setCandidateNo(int candidateNo) {
        this.candidateNo = candidateNo;
    }

    /** Loại đăng ký thí sinh (new / retake / … theo quy ước DB). */
    public String getRegistrationType() {
        return registrationType;
    }

    /** Gán loại đăng ký thí sinh. */
    public void setRegistrationType(String registrationType) {
        this.registrationType = registrationType;
    }

    /**
     * Bean-style: đã hoàn tất thu lệ phí thủ tục hay chưa.
     * Tên isIsPaymentCompleted giữ tương thích JSP/legacy getter.
     */
    public boolean isIsPaymentCompleted() {
        return isPaymentCompleted;
    }

    /** Đã thu lệ phí thủ tục (ảnh + đối chiếu hồ sơ xong bước thanh toán) hay chưa. */
    public boolean isPaymentCompleted() {
        return isPaymentCompleted;
    }

    /** Gán cờ đã thu lệ phí thủ tục. */
    public void setIsPaymentCompleted(boolean isPaymentCompleted) {
        this.isPaymentCompleted = isPaymentCompleted;
    }

    /** Thí sinh đã được điểm danh có mặt tại ca. */
    public boolean isPresent() {
        return isPresent;
    }

    /** Gán trạng thái điểm danh có mặt. */
    public void setIsPresent(boolean isPresent) {
        this.isPresent = isPresent;
    }

    /** Đã đánh vắng (tạm thời hoặc theo kết quả gọi). */
    public boolean isAbsent() {
        return absent;
    }

    /** Gán cờ vắng mặt. */
    public void setAbsent(boolean absent) {
        this.absent = absent;
    }

    /** Đã đình chỉ — không tiếp tục quy trình gọi / thủ tục bình thường. */
    public boolean isSuspended() {
        return suspended;
    }

    /** Gán cờ đình chỉ thí sinh. */
    public void setSuspended(boolean suspended) {
        this.suspended = suspended;
    }

    /** Thời điểm staff đánh dấu có mặt (null nếu chưa điểm danh). */
    public Timestamp getPresentMarkedAt() {
        return presentMarkedAt;
    }

    /** Gán thời điểm điểm danh có mặt. */
    public void setPresentMarkedAt(Timestamp presentMarkedAt) {
        this.presentMarkedAt = presentMarkedAt;
    }

    /** Ghi chú vận hành gắn enrollment (ghi chú staff / lý do…). */
    public String getNotes() {
        return notes;
    }

    /** Gán ghi chú vận hành. */
    public void setNotes(String notes) {
        this.notes = notes;
    }

    /** Họ tên đầy đủ (từ Person / Profile JOIN). */
    public String getFullName() {
        return fullName;
    }

    /** Gán họ tên thí sinh. */
    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    /** Số CCCD / giấy tờ định danh nhà nước. */
    public String getGovIdNo() {
        return govIdNo;
    }

    /** Gán số CCCD / giấy tờ định danh. */
    public void setGovIdNo(String govIdNo) {
        this.govIdNo = govIdNo;
    }

    /** Ngày sinh trên hồ sơ thí sinh. */
    public Date getDateOfBirth() {
        return dateOfBirth;
    }

    /** Gán ngày sinh. */
    public void setDateOfBirth(Date dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }

    /** Số điện thoại liên hệ. */
    public String getPhoneNo() {
        return phoneNo;
    }

    /** Gán số điện thoại liên hệ. */
    public void setPhoneNo(String phoneNo) {
        this.phoneNo = phoneNo;
    }

    /** Email liên hệ (nếu có). */
    public String getEmail() {
        return email;
    }

    /** Gán email liên hệ. */
    public void setEmail(String email) {
        this.email = email;
    }

    /** Đường dẫn / URL ảnh hồ sơ (ảnh đăng ký hoặc ảnh chụp thủ tục tùy nguồn). */
    public String getPhotoUrl() {
        return photoUrl;
    }

    /** Gán đường dẫn ảnh hồ sơ. */
    public void setPhotoUrl(String photoUrl) {
        this.photoUrl = photoUrl;
    }

    /** Mã máy / máy tính phân công thi lý thuyết (nếu có). */
    public String getComputerCode() {
        return computerCode;
    }

    /** Gán mã máy thi lý thuyết. */
    public void setComputerCode(String computerCode) {
        this.computerCode = computerCode;
    }

    /**
     * Trạng thái phần lý thuyết: thường none/passed/failed.
     */
    public String getTheoryPassed() {
        return theoryPassed;
    }

    /** Gán trạng thái đậu/trượt phần lý thuyết. */
    public void setTheoryPassed(String theoryPassed) {
        this.theoryPassed = theoryPassed;
    }

    /**
     * Trạng thái phần thực hành / sa hình: thường none/passed/failed.
     */
    public String getPracticalPassed() {
        return practicalPassed;
    }

    /** Gán trạng thái đậu/trượt phần thực hành. */
    public void setPracticalPassed(String practicalPassed) {
        this.practicalPassed = practicalPassed;
    }

    /** Điểm phần lý thuyết (null nếu chưa có kết quả). */
    public Integer getTheoryScore() {
        return theoryScore;
    }

    /** Gán điểm phần lý thuyết. */
    public void setTheoryScore(Integer theoryScore) {
        this.theoryScore = theoryScore;
    }

    /** Điểm phần thực hành (null nếu chưa có kết quả). */
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

    /** Gán điểm phần thực hành. */
    public void setPracticalScore(Integer practicalScore) {
        this.practicalScore = practicalScore;
    }

    /** Mã hạng bằng lái (A1, B1, …) gắn enrollment. */
    public String getLicenseCode() {
        return licenseCode;
    }

    /** Gán mã hạng bằng lái. */
    public void setLicenseCode(String licenseCode) {
        this.licenseCode = licenseCode;
    }

    /** Id khu vực đã phân bổ (thường lý thuyết / khu chung — null nếu chưa phân). */
    public Integer getAllocatedAreaId() {
        return allocatedAreaId;
    }

    /** Gán id khu vực phân bổ. */
    public void setAllocatedAreaId(Integer allocatedAreaId) {
        this.allocatedAreaId = allocatedAreaId;
    }

    /** Tên khu vực đã phân bổ (hiển thị UI). */
    public String getAllocatedAreaName() {
        return allocatedAreaName;
    }

    /** Gán tên khu vực phân bổ. */
    public void setAllocatedAreaName(String allocatedAreaName) {
        this.allocatedAreaName = allocatedAreaName;
    }

    /** Id khu vực phân bổ phần thực hành / sa hình (null nếu chưa). */
    public Integer getPracticalAllocatedAreaId() {
        return practicalAllocatedAreaId;
    }

    /** Gán id khu vực thực hành đã phân. */
    public void setPracticalAllocatedAreaId(Integer practicalAllocatedAreaId) {
        this.practicalAllocatedAreaId = practicalAllocatedAreaId;
    }

    /** Tên khu vực thực hành đã phân (hiển thị UI). */
    public String getPracticalAllocatedAreaName() {
        return practicalAllocatedAreaName;
    }

    /** Gán tên khu vực thực hành đã phân. */
    public void setPracticalAllocatedAreaName(String practicalAllocatedAreaName) {
        this.practicalAllocatedAreaName = practicalAllocatedAreaName;
    }

    /** Đã có ảnh chụp thủ tục hợp lệ tại bàn (điều kiện hoàn tất thủ tục). */
    public boolean isValidCapturedPhoto() {
        return validCapturedPhoto;
    }

    /** Gán cờ ảnh chụp thủ tục hợp lệ. */
    public void setValidCapturedPhoto(boolean validCapturedPhoto) {
        this.validCapturedPhoto = validCapturedPhoto;
    }

    /**
     * Cờ thi lại lý thuyết: null/true = phải thi; false = bảo lưu, bỏ qua phần này.
     */
    public Boolean getTakeTheory() {
        return takeTheory;
    }

    /** Gán cờ có thi phần lý thuyết hay bảo lưu. */
    public void setTakeTheory(Boolean takeTheory) {
        this.takeTheory = takeTheory;
    }

    /**
     * Cờ thi lại thực hành: null/true = phải thi; false = bảo lưu, bỏ qua phần này.
     */
    public Boolean getTakePractical() {
        return takePractical;
    }

    /** Gán cờ có thi phần thực hành hay bảo lưu. */
    public void setTakePractical(Boolean takePractical) {
        this.takePractical = takePractical;
    }

    /**
     * Bảo lưu lý thuyết — không thi lại phần lý thuyết trong kỳ này.
     * @return true khi takeTheory == Boolean.FALSE
     */
    public boolean skipsTheory() {
        return Boolean.FALSE.equals(takeTheory);
    }

    /**
     * Alias bean cho JSP EL: ${profile.skipsTheory}.
     * @return cùng nghĩa skipsTheory()
     */
    public boolean isSkipsTheory() {
        return skipsTheory();
    }

    /**
     * Bảo lưu thực hành / sa hình — không thi lại phần này.
     * @return true khi takePractical == Boolean.FALSE
     */
    public boolean skipsPractical() {
        return Boolean.FALSE.equals(takePractical);
    }

    /**
     * Alias bean cho JSP EL: ${profile.skipsPractical}.
     * @return cùng nghĩa skipsPractical()
     */
    public boolean isSkipsPractical() {
        return skipsPractical();
    }

    /** Ngày thi gắn enrollment / kỳ (hiển thị dossier, báo cáo). */
    public Date getExamDate() {
        return examDate;
    }

    /** Gán ngày thi. */
    public void setExamDate(Date examDate) {
        this.examDate = examDate;
    }

    /** Alias họ tên cho JSP / view legacy ExamStaff (name). */
    public String getName() {
        return fullName;
    }

    /** Alias hạng bằng (clazz) cho JSP / view legacy. */
    public String getClazz() {
        return licenseCode;
    }

    /** Alias ngày sinh (dob) cho JSP / view legacy. */
    public java.sql.Date getDob() {
        return dateOfBirth;
    }

    /** Alias CCCD (cccd) cho JSP / view legacy. */
    public String getCccd() {
        return govIdNo;
    }

    /**
     * Thủ tục bàn đã hoàn tất: đã thu lệ phí và có ảnh chụp hợp lệ.
     * Vắng / đình chỉ luôn trả false (không tính là xong thủ tục).
     * - Loại trừ absent / suspended.
     * - Yêu cầu isPaymentCompleted.
     * - Yêu cầu validCapturedPhoto.
     * @return true nếu đủ điều kiện đóng bước thủ tục tại bàn
     */
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

    /**
     * Kỳ thi của thí sinh đã kết thúc (đủ kết quả theo hạng + bảo lưu), hoặc vắng mặt.
     * Đình chỉ → false. Chưa thanh toán → false (trừ nhánh vắng đã xét trên).
     * <p>Logic lần lượt:</p>
     * - Suspended → chưa kết thúc theo nghĩa “xong kỳ”.
     * - Absent → coi như kết thúc (không thi tiếp).
     * - Chưa trả phí → chưa xong.
     * - Trượt lý thuyết → kết thúc.
     * - Bỏ lý thuyết (bảo lưu): chỉ phụ thuộc thực hành.
     * - Bỏ thực hành + đậu lý thuyết → kết thúc.
     * - Trượt thực hành (khi vẫn thi) → kết thúc.
     * - Còn lại: cả lý thuyết và thực hành hiệu lực đều passed.
     * @return true nếu thí sinh không còn phần thi mở
     */
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

    /**
     * Đậu cuối cùng toàn kỳ (không tính vắng / đình chỉ).
     * Tôn trọng bảo lưu: bỏ lý thuyết → chỉ cần thực hành passed; bỏ thực hành → lý thuyết passed là đủ.
     * @return true nếu đạt kết quả đậu cuối
     */
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

    /**
     * Trạng thái thực hành “hiệu lực” khi xét kết thúc / đậu cuối:
     * nếu bảo lưu thực hành và đã đậu lý thuyết thì coi như passed;
     * ngược lại chuẩn hóa blank → none.
     */
    private String effectivePracticalPassed() {
        if (skipsPractical() && "passed".equalsIgnoreCase(theoryPassed)) {
            return "passed";
        }
        return practicalPassed == null || practicalPassed.isBlank() ? "none" : practicalPassed.trim();
    }
}

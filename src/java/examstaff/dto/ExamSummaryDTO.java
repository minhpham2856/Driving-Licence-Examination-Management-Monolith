package examstaff.dto;

import java.sql.Date;
import java.sql.Time;
import java.sql.Timestamp;

/**
 * DTO tóm tắt kỳ thi mang qua layer ExamStaff (picker, dashboard, public call, report…).
 *
 * Vai trò:
 * Ánh xạ thông tin Exam + loại thi / hạng bằng để hiển thị danh sách chọn kỳ và header trang,
 * không chứa nghiệp vụ gọi / thủ tục.
 *
 * Ai tạo / tiêu thụ:
 * ExamViewDAOImpl, ExamDAOImpl → binders, ExamStaffPickerViewDTO,
 * PublicCallSnapshotDTO, hầu hết servlet staff.
 */
public class ExamSummaryDTO {

    private int id;
    private int examId;
    private String examName;
    private int examTypeId;
    private Date examDate;
    private Time shiftStartTime;
    private Time shiftEndTime;
    private Timestamp scheduledStartAt;
    private Timestamp scheduledEndAt;
    private String status;
    private Timestamp createdAt;
    private String licenseCode;
    private String examCode;
    private String examTypeName;

    /** Bản ghi rỗng — map từng cột từ DAO. */
    public ExamSummaryDTO() {
    }

    /** Khóa surrogate / id hàng (tùy nguồn DAO; có thể trùng nghĩa examId). */
    public int getId() {
        return id;
    }

    /** Gán id surrogate. */
    public void setId(int id) {
        this.id = id;
    }

    /** Mã kỳ thi nghiệp vụ dùng xuyên suốt ExamStaff. */
    public int getExamId() {
        return examId;
    }

    /** Gán mã kỳ thi. */
    public void setExamId(int examId) {
        this.examId = examId;
    }

    /** Tên kỳ thi hiển thị (picker, TV, report). */
    public String getExamName() {
        return examName;
    }

    /** Gán tên kỳ thi. */
    public void setExamName(String examName) {
        this.examName = examName;
    }

    /** Id loại kỳ / phần thi gắn Exam. */
    public int getExamTypeId() {
        return examTypeId;
    }

    /** Gán id loại kỳ thi. */
    public void setExamTypeId(int examTypeId) {
        this.examTypeId = examTypeId;
    }

    /** Ngày tổ chức kỳ thi. */
    public Date getExamDate() {
        return examDate;
    }

    /** Gán ngày thi. */
    public void setExamDate(Date examDate) {
        this.examDate = examDate;
    }

    /** Giờ bắt đầu ca làm việc / ca thi. */
    public Time getShiftStartTime() {
        return shiftStartTime;
    }

    /** Gán giờ bắt đầu ca. */
    public void setShiftStartTime(Time shiftStartTime) {
        this.shiftStartTime = shiftStartTime;
    }

    /** Giờ kết thúc ca. */
    public Time getShiftEndTime() {
        return shiftEndTime;
    }

    /** Gán giờ kết thúc ca. */
    public void setShiftEndTime(Time shiftEndTime) {
        this.shiftEndTime = shiftEndTime;
    }

    /** Thời điểm lên lịch bắt đầu (timestamp đầy đủ). */
    public Timestamp getScheduledStartAt() {
        return scheduledStartAt;
    }

    /** Gán scheduled start. */
    public void setScheduledStartAt(Timestamp scheduledStartAt) {
        this.scheduledStartAt = scheduledStartAt;
    }

    /** Thời điểm lên lịch kết thúc. */
    public Timestamp getScheduledEndAt() {
        return scheduledEndAt;
    }

    /** Gán scheduled end. */
    public void setScheduledEndAt(Timestamp scheduledEndAt) {
        this.scheduledEndAt = scheduledEndAt;
    }

    /** Trạng thái kỳ (Scheduled / InProgress / Completed / Cancelled…). */
    public String getStatus() {
        return status;
    }

    /** Gán trạng thái kỳ thi. */
    public void setStatus(String status) {
        this.status = status;
    }

    /** Thời điểm tạo bản ghi kỳ thi. */
    public Timestamp getCreatedAt() {
        return createdAt;
    }

    /** Gán thời điểm tạo. */
    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }

    /** Mã hạng bằng gắn kỳ (A1, B2…). */
    public String getLicenseCode() {
        return licenseCode;
    }

    /** Gán mã hạng bằng. */
    public void setLicenseCode(String licenseCode) {
        this.licenseCode = licenseCode;
    }

    /** Mã kỳ thi dạng chuỗi (hiển thị / tra cứu). */
    public String getExamCode() {
        return examCode;
    }

    /** Gán mã kỳ dạng chuỗi. */
    public void setExamCode(String examCode) {
        this.examCode = examCode;
    }

    /** Tên loại thi hiển thị. */
    public String getExamTypeName() {
        return examTypeName;
    }

    /** Gán tên loại thi. */
    public void setExamTypeName(String examTypeName) {
        this.examTypeName = examTypeName;
    }
}

# Business Flows — DLEM (Driving Licence Examination Management)

Tài liệu liệt kê **quy trình nghiệp vụ** của hệ thống Lái Vui / DLEM, bắt nguồn từ `tracking.md` và phạm vi màn hình trong codebase (`public`, `registrant`, `exam`, `examiner`, `examstaff`, `managingstaff`, `admin`).

**Cách dùng:** Mỗi flow có **Diagram Prompt** — copy nguyên khối đó cho AI agent (hoặc công cụ vẽ swimlane) để sinh sơ đồ làn bơi (swimlane). Không chia theo role làm trục chính; role chỉ xuất hiện trong swimlane khi flow cần.

**Phân tầng độ phức tạp**

| Tier | Mô tả | Số actor điển hình |
| ---- | ----- | ------------------ |
| **T1** | Quy trình xuyên suốt, nhiều bước, nhiều actor, có rẽ nhánh nghiệp vụ | 3–7 |
| **T2** | Quy trình domain rõ, 1–3 actor, có DB transaction | 1–3 |
| **T3** | Đơn giản: tra cứu, hiển thị, thao tác một màn | 0–1 |

**Actor / swimlane chuẩn hệ thống**

`Guest` · `Registrant` · `ManagingStaff` · `ExamStaff` · `Examiner` · `Admin` · `Candidate (thi)` · `Hệ thống` · `Email/SMTP`

**Quy tắc nghiệp vụ quan trọng (tham chiếu schema)**

- SBD (`CandidateNumber`): số **001–600** (3 chữ số).
- Hạng bằng ↔ phần thi: bảng `Licence_ExamSection` (thời gian thi theo hạng).
- Thí sinh: `TakeTheory`, `TakePractical`, `TakeRoadLayout`, `TakeOnRoad`, `ReasonForTaking`.
- Trượt lý thuyết → thi lại **toàn bộ** phần; trượt sa hình/thực hành → chỉ thi lại phần đó, **lý thuyết bảo lưu**; trừ hết điểm GPLX → **chỉ thi lại lý thuyết**.
- Email: Examiner `@pc08a.com`; Registrant `@gmail.com`.

---

## T1 — Quy trình phức tạp (end-to-end, đa actor)

### BF-01: Hành trình sát hạch GPLX từ đăng ký đến có kết quả

| Thuộc tính | Giá trị |
| ---------- | ------- |
| Tier | T1 |
| Màn hình liên quan | `home`, `register`, `login`, `upload-documents`, `register-exam`, `approve`, `my-exams`, `exam-entrance` → `exam-results`, `dashboard` (examiner/staff), `result-details` |
| Tracking | Iter1 public + toàn bộ pipeline chưa Done ở registrant/staff |

**Diagram Prompt**

> Vẽ swimlane diagram tiếng Việt cho hệ thống sát hạch GPLX "Lái Vui". Swimlanes: Guest, Registrant, ManagingStaff, ExamStaff, Admin, Candidate (thi), Hệ thống, Email.  
> Luồng chính (happy path): (1) Guest xem Home / Quy trình thi / Hạng GPLX → (2) Guest đăng ký tài khoản → Hệ thống tạo User+Profile, gửi username/password qua Email → (3) Registrant đăng nhập → (4) Registrant upload CCCD, ảnh, giấy khám SK → (5) Registrant chọn hạng GPLX và đăng ký kỳ thi (`ExamRegistration` Pending) → (6) ManagingStaff duyệt hồ sơ (Approved/Rejected) → (7) Registrant thanh toán lệ phí (`Payment` Completed) → (8) ExamStaff/Admin lập kỳ thi, ca, phòng, gán SBD 001–600, gán ca (`Exam_Candidate`) → (9) Ngày thi: Candidate nhập SBD tại phòng LT → xác minh khuôn mặt → làm bài LT → Hệ thống chấm tự động → (10) Examiner/ExamStaff gọi thí sinh thi sa hình/đường trường → chấm điểm → (11) Hệ thống tổng hợp `ExamResult`/`ExamScore` → Registrant/Examiner xem kết quả.  
> Nhánh: Rejected hồ sơ (dừng, Registrant sửa và nộp lại); Payment Pending (không vào ca thi); trượt một phần (nhánh BF-06).  
> Ghi chú lane: Admin cấu hình hạng bằng, phí, phòng, máy thi song song với bước 8.

---

### BF-02: Đăng ký tài khoản & xác thực lần đầu

| Thuộc tính | Giá trị |
| ---------- | ------- |
| Tier | T1 |
| Màn hình | `register.jsp`, `login.jsp`, `RegisterServlet`, `LoginServlet` |
| Tracking | Iter1 Register (Done), Login (Done) |

**Diagram Prompt**

> Swimlane: Guest/Registrant, Hệ thống, Email, Database.  
> Flow: Guest mở `/register` → nhập CCCD, họ tên, SĐT, ngày sinh, địa chỉ, email (gmail), giới tính, đồng ý điều khoản → POST `/register` → Hệ thống validate (đủ field, checkbox) → kiểm tra trùng CCCD/email/SĐT → tạo `User` (Role=Registrant, email gmail) + `Profile` → sinh username tự động + password ngẫu nhiên → gửi email thông tin đăng nhập → redirect `/login` kèm flash success.  
> Nhánh lỗi: trùng CCCD / email / SĐT (ở lại form, thông báo); thiếu field; không tick điều khoản; lỗi DB; email gửi thất bại (tài khoản đã tạo, báo liên hệ hỗ trợ).  
> Tiếp nối: Registrant đăng nhập identifier (username/email/SĐT) + password → Hệ thống kiểm tra active + hash → lưu session → redirect dashboard theo role (Registrant → registrant dashboard).

---

### BF-03: Nộp hồ sơ, upload giấy tờ & đăng ký kỳ thi

| Thuộc tính | Giá trị |
| ---------- | ------- |
| Tier | T1 |
| Màn hình | `upload-documents.jsp`, `register-exam.jsp`, `profile.jsp`, `track-profile.jsp` |
| Tracking | Chưa trong tracking (registrant module) |

**Diagram Prompt**

> Swimlane: Registrant, Hệ thống, ManagingStaff (chờ duyệt), Database.  
> Flow: Registrant đăng nhập → cập nhật Profile → upload Document (CCCD mặt trước/sau, giấy khám sức khỏe, ảnh chân dung; nếu giấy tờ ngoài danh mục thì `DocumentType = Khác` và mô tả ở `Document.Notes`) → Hệ thống lưu `Document` gắn ProfileId → Registrant mở đăng ký thi → chọn `LicenceClass` (A1, B, B1, C1…) → chọn kỳ/ca thi mở (`Exam` Status=Open) → xem breakdown phí (lý thuyết, sa hình, đường trường, hồ sơ) → xác nhận → Hệ thống tạo `ExamRegistration` (Pending) + ghi Notes nếu có → hiển thị trạng thái trên `track-profile` / `my-exams`.  
> Nhánh: thiếu giấy tờ bắt buộc (chặn submit); hạng không đủ tuổi (`MinimumAge`); kỳ thi đã đóng đăng ký.  
> Output: RegistrationStatus = Pending chờ BF-04.

---

### BF-04: Thẩm định & phê duyệt hồ sơ đăng ký thi

| Thuộc tính | Giá trị |
| ---------- | ------- |
| Tier | T1 |
| Màn hình | `approve.jsp`, `managingstaff/dashboard.jsp` |
| Tracking | Staff module (chưa tracking) |

**Diagram Prompt**

> Swimlane: ManagingStaff, Hệ thống, Registrant (nhận kết quả), Database, Audit.  
> Flow: ManagingStaff mở danh sách hồ sơ Pending → chọn học viên → xem Profile + Documents đối chiếu CCCD → quyết định Approve hoặc Reject (kèm Notes/lý do) → Hệ thống cập nhật `ExamRegistration.RegistrationStatus` → ghi `Audit` (Entity=ExamRegistration, Action=APPROVE/REJECT) → thông báo Registrant (dashboard/track-profile).  
> Nhánh Approve: Registrant được phép thanh toán (BF-05).  
> Nhánh Reject: ví dụ "Không đủ yêu cầu sức khoẻ" — Registrant sửa hồ sơ và tạo đăng ký mới hoặc khiếu nại.  
> Precondition: Profile + Document tối thiểu đã upload.

---

### BF-05: Thanh toán lệ phí thi & kích hoạt thí sinh

| Thuộc tính | Giá trị |
| ---------- | ------- |
| Tier | T1 |
| Màn hình | `register-exam.jsp` (phí), `my-exams.jsp` |
| Entities | `Payment`, `Payment_Fee`, `Fee`, `Candidate` |

**Diagram Prompt**

> Swimlane: Registrant, Hệ thống, Ngân hàng/Quầy (external), ExamStaff, Database.  
> Flow: Sau khi ExamRegistration Approved → Registrant chọn phương thức (BankTransfer/Cash) → Hệ thống tạo `Payment` (Pending hoặc Completed nếu tiền mặt tại quầy) → liên kết `Payment_Fee` theo hạng bằng (lý thuyết, sa hình, đường trường, phí hồ sơ, cấp GPLX) → khi PaidAt có giá trị và Status=Completed → ExamStaff tạo bản ghi `Candidate` (SBD 001–600, Take* theo loại thi, ReasonForTaking) gắn ExamRegistrationId + UserId → gán `Exam_Candidate` vào Exam + Session.  
> Nhánh: Payment Pending → không gọi thi, không in phiếu; hoàn tiền/hủy (ngoài scope, ghi note).  
> Rule: SBD unique; TakeTheory/TakeRoadLayout/TakeOnRoad phản ánh phần thi lần này.

---

### BF-06: Thi lại / bảo lưu điểm — chọn phần thi theo lý do

| Thuộc tính | Giá trị |
| ---------- | ------- |
| Tier | T1 |
| Màn hình | `register-exam.jsp`, `candidate-details-edit.jsp`, seed DML |
| Rule | `TakeTheory`, `TakePractical`, `TakeRoadLayout`, `TakeOnRoad`, `ReasonForTaking` |

**Diagram Prompt**

> Swimlane: Registrant, ExamStaff, Hệ thống, Database.  
> Ba nhánh song song từ gateway "Lý do thi":  
> **(A) Thi lần đầu** — ReasonForTaking="Thi lần đầu": set Take* = tất cả phần thi của hạng (B: LT+Sa hình+Đường trường; A1: LT+Thực hành; B1: LT+Thực hành).  
> **(B) Trượt lý thuyết** — Reason="Thi lại vì trượt lý thuyết": TakeTheory=1, TakeRoadLayout=1, TakeOnRoad=1 (thi lại hết, không bảo lưu).  
> **(C) Trượt sa hình/thực hành** — Reason="Thi lại vì trượt sa hình": TakeTheory=0 (bảo lưu LT), TakeRoadLayout=1, TakeOnRoad=1 (nếu hạng B); chỉ gán Session tương ứng phần Take*=1.  
> **(D) Trừ hết điểm GPLX** — Reason="Thi lại vì trừ hết điểm": TakeTheory=1, các Take* khác=0/NULL.  
> ExamStaff chỉ tạo `Exam_Candidate` cho session khớp Take*; phí chỉ tính phần thi lại. Hiển thị lý do trên màn examiner `candidate-details`.

---

### BF-07: Thi lý thuyết trên máy — từ SBD đến kết quả tự động

| Thuộc tính | Giá trị |
| ---------- | ------- |
| Tier | T1 |
| Màn hình | `exam-entrance.jsp`, `exam-face.jsp`, `exam-candidate-info.jsp`, `exam-questions.jsp`, `exam-results.jsp`, `candidate-paper.jsp` (examiner xem lại) |
| Tracking | `candidate-paper.jsp` (Done, examiner) |

**Diagram Prompt**

> Swimlane: Candidate (thi), Hệ thống, ExamDevice, Examiner (giám sát), Database.  
> Flow: Candidate tại phòng LT nhập SBD (001–600) → Hệ thống validate: tồn tại, đúng ca (`Session`), TakeTheory=1, Payment Completed → hiển thị thông tin thí sinh → xác minh khuôn mặt + chụp ảnh (`PhotoImageUrl`) → gán máy `ExamDevice` (Available→InUse) → tạo `TheoryPaper` (StartedAt) → hiển thị câu hỏi theo hạng (`Licence_Question`, 35 câu cho B) → đếm ngược theo `Licence_ExamSection.DurationMinutes` → Candidate nộp bài (SubmittedAt) → Hệ thống lưu `CandidateAnswer`, tự chấm so `CorrectAnswer` + `IsCritical` → tạo `ExamResult`/`ExamScore` (Lý thuyết) → hiển thị `exam-results` (đậu/rớt).  
> Nhánh: SBD sai/ca sai/TakeTheory=0 → từ chối; hết giờ → auto-submit; vi phạm (Examiner WARNING qua BF-09).  
> Examiner có thể mở `candidate-paper.jsp` xem lại đề và đáp án đã chọn.

---

### BF-08: Thi sa hình & đường trường — gọi thí sinh, chấm điểm, trừ điểm

| Thuộc tính | Giá trị |
| ---------- | ------- |
| Tier | T1 |
| Màn hình | `candidate-call.jsp`, `grading.jsp`, `editscore.jsp` (staff), `result-details-edit.jsp` (examiner) |
| Tracking | Call Candidate (Done), Edit Score (Doing) |

**Diagram Prompt**

> Swimlane: Examiner, ExamStaff, Candidate, Hệ thống, Database.  
> Flow: Examiner mở Call Candidate theo ca (Sa hình / Đường trường) → danh sách hàng đợi theo SBD → bấm Gọi / In phiếu → Candidate vào sân/đường → ExamStaff/Examiner mở Chấm điểm (`grading.jsp`): điểm khởi đầu 100, thực hiện từng bước thi → mỗi lỗi chọn `ScoreDeduction` → trừ điểm qua `Score_Deduction` → lỗi critical có thể trượt ngay → kết thúc: lưu `ExamScore` (Sa hình hoặc Đường trường) + `ExamResult.IsPassed` → đồng bộ lên Results List.  
> Nhánh: Candidate TakeRoadLayout=0 → không có trong ca sa hình; trượt sa hình → BF-06 nhánh C.  
> Liên kết: Session_ExamSection xác định phần thi của ca; Session_Examiner gán giám khảo.

---

### BF-09: Điều chỉnh điểm & kiểm toán (audit trail)

| Thuộc tính | Giá trị |
| ---------- | ------- |
| Tier | T1 |
| Màn hình | `result-details-edit.jsp`, `editscore.jsp`, `audit.jsp` (examiner/admin/staff) |
| Tracking | Edit Score (Doing), View Audit Log (Done) |

**Diagram Prompt**

> Swimlane: Examiner, ExamStaff, Admin, Hệ thống, Database (Audit).  
> Flow: Examiner/Staff mở kết quả thí sinh → chọn Sửa điểm → nhập điểm mới + lý do bắt buộc + xác thực (password, captcha) → Hệ thống đọc `ExamScore` cũ → cập nhật điểm mới → ghi `Audit` (Action=UPDATE, EntityName=Kết quả thi/Thí sinh, OldValue/NewValue, Reason, UserId, CreatedAt) → có thể kéo theo đổi `ExamResult.IsPassed`.  
> Nhánh: sai password/captcha → hủy; phúc khảo (Audit Reason="Phúc khảo").  
> Tra cứu: Examiner/Admin mở Audit Log → lọc theo thời gian, entity, SBD → phân trang.  
> Ví dụ seed: SBD 123 chấm sai LT 25→27 điểm.

---

### BF-10: Thiết lập kỳ thi, ca, khu vực & phân công giám khảo (Admin + ExamStaff)

| Thuộc tính | Giá trị |
| ---------- | ------- |
| Tier | T1 |
| Màn hình | `licence-class.jsp`, `exam-fee.jsp`, `exam-room.jsp`, `exam-area.jsp`, `exam-computer.jsp`, `examstaff/dashboard.jsp`, `examstaff/upload.jsp` |
| Tracking | Admin module (chưa tracking) |

**Diagram Prompt**

> Swimlane: Admin, ExamStaff, Hệ thống, Database.  
> Flow cấu hình: Admin quản lý `Licence` (hạng, tuổi, thời hạn) + `Licence_ExamSection` (phần thi + DurationMinutes theo hạng) + `Fee` + `ExamArea` (phòng LT, sân, đường trường) + `ExamDevice` (máy LT, trạng thái Available/InUse/Maintenance).  
> Flow vận hành: ExamStaff tạo `Exam` (ExamCode, ExamDate, CentreName, LicenceId, Status Open/Scheduled) → tạo `Session` (ca LT, Sa hình, Đường trường) → `Session_ExamSection` + `Session_ExamArea` + `Session_Examiner` (examiner @pc08a.com) → upload/import danh sách thí sinh (`examstaff/upload.jsp`) → gán `Exam_Candidate`.  
> Nhánh: máy Maintenance → không gán TheoryPaper; ca Scheduled → chưa mở gọi thi.

---

## T2 — Quy trình trung bình (domain, ít actor)

### BF-11: Quên mật khẩu & đặt lại qua email

| Thuộc tính | Giá trị |
| ---------- | ------- |
| Tier | T2 |
| Màn hình | `forgot-password.jsp`, `ForgotPasswordServlet` |
| Tracking | Iter1 Forgot Password (Done) |

**Diagram Prompt**

> Swimlane: Registrant, Hệ thống, Email, Database.  
> Flow: Registrant nhập email đăng ký (gmail) → Hệ thống tìm User theo email/Profile → sinh mật khẩu tạm 6 số → cập nhật PasswordHash → gửi email → thông báo thành công → Registrant đăng nhập bằng mật khẩu tạm → đổi mật khẩu (settings, nếu có).  
> Nhánh: email không tồn tại → thông báo chung (không lộ tồn tại account); lỗi SMTP.

---

### BF-12: Đăng nhập đa vai trò & điều hướng workspace

| Thuộc tính | Giá trị |
| ---------- | ------- |
| Tier | T2 |
| Màn hình | `login.jsp` (shared), dashboards theo role |
| Tracking | Login (Done), Examiner Login shared (Done) |

**Diagram Prompt**

> Swimlane: User (bất kỳ role), Hệ thống.  
> Flow: POST `/login` → validate credential → session `user` + role → redirect: Admin→admin dashboard; ManagingStaff→managingstaff dashboard; ExamStaff→examstaff dashboard; Examiner→examiner dashboard; Registrant→registrant dashboard.  
> Nhánh: chưa đăng nhập truy cập URL staff → redirect login + error; account Status=0 → từ chối.

---

### BF-13: ManagingStaff — quản lý tài khoản học viên/nhân sự

| Thuộc tính | Giá trị |
| ---------- | ------- |
| Tier | T2 |
| Màn hình | `users.jsp`, `user-detail.jsp`, `create-user.jsp` |

**Diagram Prompt**

> Swimlane: ManagingStaff, Hệ thống, Email, Database.  
> Flow: ManagingStaff xem danh sách User → tạo tài khoản học viên (nhập CCCD, họ tên, email…) → Hệ thống tạo User+Profile (có thể khác flow tự đăng ký: staff chủ động tạo) → gửi credential / kích hoạt → xem chi tiết, khóa/mở Status.  
> Nhánh: trùng CCCD; tạo user role Examiner với email @pc08a.com.

---

### BF-14: Examiner — vận hành ca thi (dashboard, tìm kiếm, tổng quan)

| Thuộc tính | Giá trị |
| ---------- | ------- |
| Tier | T2 |
| Màn hình | `dashboard.jsp`, `sidebar-examiner.jsp` |
| Tracking | View Dashboard (Done) |

**Diagram Prompt**

> Swimlane: Examiner, Hệ thống, Database.  
> Flow: Examiner đăng nhập → dashboard hiển thị tổng số thí sinh ca hiện tại, đã gọi/chưa gọi, đậu/rớt → ô tìm kiếm SBD/tên → lọc bảng Candidate theo Session đang mở → điều hướng sang Call / Chi tiết / Kết quả / Audit / Export.  
> Ghi chú: dữ liệu gắn Session_Examiner của examiner đăng nhập.

---

### BF-15: Examiner — tra cứu & chỉnh sửa hồ sơ thí sinh ngày thi

| Thuộc tính | Giá trị |
| ---------- | ------- |
| Tier | T2 |
| Màn hình | `candidate-details.jsp`, `candidate-details-edit.jsp` |
| Tracking | Candidate List (Done), Candidate Detail View (Done) |

**Diagram Prompt**

> Swimlane: Examiner, Hệ thống, Database.  
> Flow: Examiner mở danh sách thí sinh → tìm SBD → xem chi tiết (read-only): SBD, họ tên, CCCD, Take*, ReasonForTaking, ca thi, trạng thái thanh toán → nếu sai sót nhập liệu: mở form sửa → cập nhật Candidate fields → Audit ghi thay đổi.  
> Nhánh: sửa SBD đã in phiếu (cần quyền cao hơn / từ chối).

---

### BF-16: Examiner / ExamStaff — xem & xuất báo cáo

| Thuộc tính | Giá trị |
| ---------- | ------- |
| Tier | T2 |
| Màn hình | `export.jsp`, `ExportCandidatesExcelServlet`, `report.jsp` (staff) |
| Tracking | Export Reports (Doing) |

**Diagram Prompt**

> Swimlane: Examiner, Hệ thống, File storage, Database.  
> Flow: Examiner chọn loại báo cáo (danh sách thí sinh / kết quả / audit) → chọn ca thi, định dạng (Excel) → GET `/examiner/export/candidates` → Hệ thống query Candidate+Exam_Candidate+Payment → Apache POI tạo XSSF workbook → trả file download.  
> Nhánh: chưa chọn ca → validate; không có dữ liệu → báo trống.  
> Mở rộng: PDF kết quả, nhật ký audit (chưa wired).

---

### BF-17: ExamStaff — giám sát phòng thi lý thuyết & gọi thí sinh

| Thuộc tính | Giá trị |
| ---------- | ------- |
| Tier | T2 |
| Màn hình | `examstaff/candidatecall.jsp`, `examstaff/candidatelist.jsp`, `examstaff/view-candidate.jsp` |

**Diagram Prompt**

> Swimlane: ExamStaff, Candidate, Hệ thống, Database.  
> Flow: ExamStaff dashboard ca LT → xem danh sách đã check-in SBD → trạng thái máy (ExamDevice InUse/Available) → gọi thí sinh tiếp theo → in danh sách → xử lý vi phạm (ghi Audit WARNING).  
> Khác BF-08: lane ExamStaff tập trung phòng LT; BF-08 tập trung sân/đường.

---

### BF-18: Registrant — theo dõi hồ sơ & kỳ thi của tôi

| Thuộc tính | Giá trị |
| ---------- | ------- |
| Tier | T2 |
| Màn hình | `dashboard.jsp`, `my-exams.jsp`, `track-profile.jsp`, `settings.jsp` |

**Diagram Prompt**

> Swimlane: Registrant, Hệ thống, Database.  
> Flow: Registrant dashboard → xem trạng thái ExamRegistration (Pending/Approved/Rejected) → tiến độ Payment → lịch thi (Exam, Session) nếu đã gán Candidate → xem kết quả từng phần (LT đậu bảo lưu…) → cài đặt đổi mật khẩu/email.  
> Nhánh Rejected: hiển thị Notes từ ManagingStaff.

---

### BF-19: Admin — vận hành hệ thống & tra cứu audit toàn cục

| Thuộc tính | Giá trị |
| ---------- | ------- |
| Tier | T2 |
| Màn hình | `admin/dashboard.jsp`, `admin/accounts.jsp`, `admin/audit.jsp` |

**Diagram Prompt**

> Swimlane: Admin, Hệ thống, Database.  
> Flow: Admin quản lý tài khoản nội bộ (Admin, ExamStaff, Examiner @pc08a.com) → khóa/mở → xem audit toàn hệ thống (mọi role) → lọc theo Action (UPDATE, DELETE, WARNING, SYSTEM, APPROVE).  
> Tách với BF-09: BF-09 nhấn mạnh *tạo* audit khi sửa điểm; BF-19 nhấn mạnh *tra cứu* admin.

---

## T3 — Quy trình đơn giản (tra cứu / hiển thị / một thao tác)

### BF-20: Khách truy cập — tìm hiểu dịch vụ & hạng GPLX

| Thuộc tính | Giá trị |
| ---------- | ------- |
| Tier | T3 |
| Màn hình | `home.jsp`, `license-categories.jsp`, `process.jsp` |
| Tracking | Home, License Categories, Exam Process (Done) |

**Diagram Prompt**

> Swimlane: Guest, Hệ thống (tĩnh).  
> Flow một chiều: Guest vào Home → CTA Đăng ký/Đăng nhập → hoặc xem Hạng GPLX (yêu cầu tuổi, mô tả hạng A1/A/B/B1/C1…) → hoặc xem Quy trình 5 bước (nộp hồ sơ → học → thi nội bộ → thi sát hạch 3 phần → nhận bằng). Không ghi DB. Dùng cho marketing/onboarding trước BF-01.

---

### BF-21: Examiner — xem danh sách kết quả (read-only)

| Thuộc tính | Giá trị |
| ---------- | ------- |
| Tier | T3 |
| Màn hình | `result-details.jsp` |
| Tracking | Results List (Done) |

**Diagram Prompt**

> Swimlane: Examiner, Hệ thống, Database.  
> Flow: Mở Results List → nhập SBD/tên → bảng kết quả theo phần thi (Lý thuyết, Sa hình, Đường trường) + IsPassed → click xem chi tiết (không sửa). Nhánh: chưa có ExamResult → hiển thị "Chưa thi".

---

### BF-22: Examiner — xem đề thi lý thuyết đã làm (35 câu + hình ảnh)

| Thuộc tính | Giá trị |
| ---------- | ------- |
| Tier | T3 |
| Màn hình | `candidate-paper.jsp` |
| Tracking | Exam Paper View (Done) |

**Diagram Prompt**

> Swimlane: Examiner, Hệ thống, Database (Question, CandidateAnswer, Cloudinary URL).  
> Flow: Examiner nhập/chọn SBD → tải TheoryPaper + 35 CandidateAnswer → hiển thị câu hỏi, ảnh minh họa, đáp án chọn vs đáp án đúng, đánh dấu câu điểm liệt sai → lọc đúng/sai/chưa trả lời. Không thay đổi điểm (sửa điểm → BF-09).

---

### BF-23: Examiner — tra cứu nhật ký audit (mock / read-only)

| Thuộc tính | Giá trị |
| ---------- | ------- |
| Tier | T3 |
| Màn hình | `audit.jsp` |
| Tracking | View Audit Log (Done) |

**Diagram Prompt**

> Swimlane: Examiner, Hệ thống, Database (Audit).  
> Flow: Mở Audit Log → search theo SBD (001–600), khoảng ngày, loại Action → bảng: thời gian, user, action, reason, entity, old/new value → phân trang. Ví dụ: UPDATE Thí sinh SBD 123 phúc khảo 28/30→30/30; WARNING SBD 456 mang điện thoại.

---

### BF-24: Registrant — quản lý hồ sơ cá nhân

| Thuộc tính | Giá trị |
| ---------- | ------- |
| Tier | T3 |
| Màn hình | `profile.jsp`, `settings.jsp` |

**Diagram Prompt**

> Swimlane: Registrant, Hệ thống, Database (Profile, User).  
> Flow: Xem/sửa Profile (họ tên, SĐT, địa chỉ — CCCD read-only sau duyệt) → Settings đổi mật khẩu → validate → cập nhật User.PasswordHash. Không đổi email gmail nếu ràng buộc đăng nhập.

---

### BF-25: Hệ thống — ghi nhật ký sự kiện tự động (SYSTEM audit)

| Thuộc tính | Giá trị |
| ---------- | ------- |
| Tier | T3 |
| Màn hình | (backend / scheduler) |
| Tracking | Audit seed trong DML |

**Diagram Prompt**

> Swimlane: Hệ thống (scheduler), Database (Audit).  
> Flow: Theo lịch ca thi → đến StartTime Session → Action=SYSTEM, Reason="Theo lịch trình", EntityName=Phòng thi, đổi trạng thái Khóa→Mở; kết thúc ca → đóng phòng. UserId=NULL. Dùng làm lane phụ trong BF-10 và BF-07.

---

## Ma trận flow ↔ màn hình (`tracking.md` + mở rộng)

| Flow | Tracking screen | Status |
| ---- | --------------- | ------ |
| BF-02 | Register Account, Login | Done |
| BF-11 | Forgot Password | Done |
| BF-20 | Home, License Categories, Exam Process | Done |
| BF-14 | View Dashboard, Sidebar | Done |
| BF-08 | Call Candidate | Done |
| BF-15 | Candidate List, Candidate Detail View | Done |
| BF-22 | Exam Paper View | Done |
| BF-21 | Results List | Done |
| BF-09 | Edit / Change Score | Doing |
| BF-16 | Export Reports | Doing |
| BF-23 | View Audit Log | Done (FE mock) |
| BF-01–07, 10–19 | Registrant / Staff / Admin / Exam runtime | Planned / partial FE |

---

## Gợi ý thứ tự vẽ diagram cho agent

1. **Onboarding:** BF-20 → BF-02 → BF-11 → BF-12  
2. **Trước ngày thi:** BF-03 → BF-04 → BF-05 → BF-10 → BF-06  
3. **Ngày thi:** BF-07 → BF-17 → BF-08 → BF-14  
4. **Sau thi / kiểm soát:** BF-21 → BF-09 → BF-23 → BF-16  
5. **Toàn cảnh demo:** BF-01 (tổng hợp, có thể rút gọn bước)

---

*Generated from `private/tracking.md` and codebase view inventory. Cập nhật khi thêm màn hình mới vào tracking.*

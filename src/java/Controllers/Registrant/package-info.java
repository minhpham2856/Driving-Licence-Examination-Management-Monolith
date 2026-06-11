/**
 * <h2>Actor Thí sinh (Registrant) — Hướng dẫn đọc code từ đầu</h2>
 *
 * <p>Package này chứa các <b>Servlet</b> (controller) xử lý HTTP cho khu vực {@code /registrant/...}.
 * Pattern chung: Servlet nhận request → gọi Service (logic) → forward JSP hoặc trả HTML/redirect.</p>
 *
 * <h3>Kiến trúc 3 lớp (đọc theo thứ tự khi debug)</h3>
 * <ol>
 *   <li><b>Servlet</b> (package này) — URL mapping, login, chọn view</li>
 *   <li><b>Service</b> ({@code Services.Impl.Registrant*}) — nghiệp vụ, validate, gọi DAO</li>
 *   <li><b>DAO</b> ({@code DAO.Impl}) — SQL insert/select/update</li>
 * </ol>
 *
 * <h3>Luồng nghiệp vụ thí sinh</h3>
 * <pre>
 * 1. Login → session key "user" (Models.User)
 * 2. Dashboard — thống kê tổng quan
 * 3. Profile — tạo/sửa Person (hồ sơ cá nhân)
 * 4. Upload documents — ảnh, CCCD, giấy khám
 * 5. [Staff duyệt] Person.approvalStatus = Approved
 * 6. Track profile — timeline tiến trình
 * 7. Register exam — chọn hạng + đợt thi → chỉ tạo ExamRegistration (không thanh toán)
 * 8. My exams — lịch thi, trạng thái (chờ thanh toán tại quầy / chờ thi / kết quả)
 * 9. Settings — đổi mật khẩu
 * </pre>
 *
 * <p><b>Thanh toán SEPay</b> không nằm trong luồng đăng ký thí sinh. Module tái sử dụng:
 * {@code Integration.Sepay} — wire vào actor Payment/Cashier.</p>
 *
 * <h3>Danh sách Servlet — thí sinh</h3>
 * <ul>
 *   <li>{@link Controllers.Registrant.DashboardServlet} — GET /registrant/dashboard</li>
 *   <li>{@link Controllers.Registrant.ProfileServlet} — GET/POST /registrant/profile</li>
 *   <li>{@link Controllers.Registrant.UploadDocumentsServlet} — GET/POST /registrant/upload-documents</li>
 *   <li>{@link Controllers.Registrant.TrackProfileServlet} — GET /registrant/track-profile</li>
 *   <li>{@link Controllers.Registrant.RegisterExamServlet} — GET/POST /registrant/register-exam</li>
 *   <li>{@link Controllers.Registrant.MyExamsServlet} — GET /registrant/my-exams (danh sách, filter)</li>
 *   <li>{@link Controllers.Registrant.MyExamDetailServlet} — GET /registrant/my-exams/detail</li>
 *   <li>{@link Controllers.Registrant.SettingsServlet} — GET/POST /registrant/settings</li>
 * </ul>
 *
 * <h3>Servlet SEPay (giữ trong repo, dùng bởi actor thanh toán)</h3>
 * <ul>
 *   <li>{@link Controllers.Registrant.SepayCheckoutServlet} — GET /registrant/payment/sepay-checkout</li>
 *   <li>{@link Controllers.Registrant.ResumePaymentServlet} — GET /registrant/payment/resume</li>
 *   <li>{@link Controllers.Registrant.SepayReturnServlet} — success/error/cancel (public)</li>
 *   <li>{@link Controllers.Registrant.SepayIpnServlet} — POST /registrant/payment/sepay-ipn (webhook)</li>
 *   <li>{@link Controllers.Registrant.SepayStatusServlet} — debug (sepay.debug=true)</li>
 * </ul>
 *
 * <h3>Xác thực</h3>
 * <p>Hầu hết servlet dùng {@link Controllers.Registrant.RegistrantAuth#requireUser}.
 * Callback SEPay (return + IPN) <b>không</b> bắt login — caller là SEPay hoặc browser sau redirect.</p>
 */
package Controllers.Registrant;

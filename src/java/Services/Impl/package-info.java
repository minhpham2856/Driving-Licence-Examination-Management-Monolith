/**
 * <h2>Service layer — Logic nghiệp vụ thí sinh</h2>
 *
 * <p>Servlet không viết SQL trực tiếp; gọi các class {@code Registrant*ServiceImpl} ở đây.
 * Service gọi DAO, validate input, set attribute cho JSP.</p>
 *
 * <h3>Ánh xạ Servlet → Service (thí sinh)</h3>
 * <ul>
 *   <li>DashboardServlet → RegistrantDashboardServiceImpl</li>
 *   <li>ProfileServlet → RegistrantProfileServiceImpl</li>
 *   <li>UploadDocumentsServlet → RegistrantUploadServiceImpl</li>
 *   <li>TrackProfileServlet → RegistrantTrackProfileServiceImpl</li>
 *   <li>RegisterExamServlet → RegistrantRegisterExamServiceImpl (không gọi SEPay)</li>
 *   <li>MyExamsServlet → RegistrantMyExamsServiceImpl</li>
 *   <li>SettingsServlet → RegistrantSettingsServiceImpl</li>
 * </ul>
 *
 * <h3>Module thanh toán SEPay (tách khỏi đăng ký)</h3>
 * <ul>
 *   <li>SepayPaymentServiceImpl — build form checkout + xử lý IPN ORDER_PAID</li>
 *   <li>PendingPaymentServiceImpl — pending/resume/expire (cho actor Payment)</li>
 *   <li>SepayIpnServlet → SepayPaymentServiceImpl</li>
 * </ul>
 * <p>Chi tiết tích hợp: {@code Integration.Sepay} package-info.</p>
 */
package Services.Impl;

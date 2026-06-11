/**
 * <h2>Module SEPay — tích hợp thanh toán (tách khỏi đăng ký thi)</h2>
 *
 * <p>Module độc lập, giữ nguyên trong repo để member khác wire vào actor Payment / Cashier.
 * Luồng đăng ký thi ({@code RegisterExamServlet}) <b>không</b> gọi module này.</p>
 *
 * <h3>Cấu hình</h3>
 * <ul>
 *   <li>{@code .env} — sepay.merchantId, secretKey, appBaseUrl, returnBaseUrl, …</li>
 *   <li>{@code Config.EnvLoader}, {@code Config.SepayConfig}, {@code Config.EnvConfigListener}</li>
 *   <li>{@code Config.PaymentExpiryConfig} — cửa sổ chờ thanh toán (nếu dùng PendingPayment)</li>
 * </ul>
 *
 * <h3>Service layer</h3>
 * <ul>
 *   <li>{@code Services.SepayPaymentService} / {@code Services.Impl.SepayPaymentServiceImpl}</li>
 *   <li>{@code Services.PendingPaymentService} / {@code Services.Impl.PendingPaymentServiceImpl}</li>
 * </ul>
 *
 * <h3>HTTP endpoints (sẵn sàng tích hợp)</h3>
 * <ul>
 *   <li>{@code Controllers.Registrant.SepayIpnServlet} — POST /registrant/payment/sepay-ipn</li>
 *   <li>{@code Controllers.Registrant.SepayReturnServlet} — success / error / cancel</li>
 *   <li>{@code Controllers.Registrant.SepayCheckoutServlet} — replay checkout từ session</li>
 *   <li>{@code Controllers.Registrant.ResumePaymentServlet} — resume theo registrationId</li>
 *   <li>{@code Controllers.Registrant.SepayStatusServlet} — debug (sepay.debug=true)</li>
 * </ul>
 *
 * <h3>Utils</h3>
 * <ul>
 *   <li>{@code Utils.SepaySignatureUtil}, {@code Utils.SepayCheckoutHtml}</li>
 *   <li>{@code Utils.SepayReturnHtml}, {@code Utils.SepayIpnParser}</li>
 * </ul>
 *
 * <h3>SQL</h3>
 * <ul>
 *   <li>{@code WEB-INF/others/sql/MIGRATE_SEPAY_PAYMENT.sql}</li>
 *   <li>{@code WEB-INF/others/sql/MIGRATE_PAYMENT_EXPIRY.sql}</li>
 * </ul>
 *
 * <h3>Cách tích hợp cho actor Payment (gợi ý)</h3>
 * <ol>
 *   <li>SBD: bảng {@code Candidate} (import Công an), không lưu trên ExamRegistration</li>
 *   <li>Chọn ExamRegistration chưa thanh toán (isPaymentCompleted=0)</li>
 *   <li>{@code PaymentDAO.insertPending} + {@code PaymentDAO.findResumablePending}</li>
 *   <li>{@code SepayPaymentServiceImpl.buildCheckoutFields}</li>
 *   <li>{@code SepayCheckoutHtml.writeAutoSubmitForm} hoặc redirect tới checkout</li>
 *   <li>IPN {@code handleOrderPaid} → markPaymentCompleted + incrementRegisteredCount</li>
 * </ol>
 */
package Integration.Sepay;

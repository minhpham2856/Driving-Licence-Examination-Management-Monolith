package Controllers.Registrant;



import Config.EnvLoader;

import Config.SepayConfig;

import Models.User;

import Services.Impl.SepayPaymentServiceImpl;

import Utils.SepaySignatureUtil;

import jakarta.servlet.ServletException;

import java.math.BigDecimal;

import java.util.Map;

import jakarta.servlet.annotation.WebServlet;

import jakarta.servlet.http.HttpServlet;

import jakarta.servlet.http.HttpServletRequest;

import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

import java.io.PrintWriter;

import java.nio.charset.StandardCharsets;



/**

 * Trang chẩn đoán SEPay — <b>chỉ dành cho developer</b>.

 * <p>Bật bằng {@code sepay.debug=true} trong .env. Mặc định trả 404 để bảo mật.</p>

 * <p>Không liên kết từ UI thí sinh; không hiển thị secret key.</p>

 */

@WebServlet("/registrant/payment/sepay-status")

public class SepayStatusServlet extends HttpServlet {



    /**
     * Trang debug cấu hình thanh toán — chỉ khi sepay.debug=true trong .env.
     * Mặc định 404; không link từ UI thí sinh; không hiển thị secret key.
     */
    @Override

    protected void doGet(HttpServletRequest request, HttpServletResponse response)

            throws ServletException, IOException {



        if (!SepayConfig.isDebugEnabled()) {

            response.sendError(HttpServletResponse.SC_NOT_FOUND);

            return;

        }



        User user = RegistrantAuth.requireUser(request, response, "Bạn cần đăng nhập.");

        if (user == null) {

            return;

        }



        EnvLoader.reload();



        boolean configured = SepayConfig.isConfigured();

        boolean hasBase = !SepayConfig.getAppBaseUrl().isBlank();

        SepayPaymentServiceImpl service = new SepayPaymentServiceImpl();

        boolean ready = service.isReady();

        String configError = service.configurationError();



        Map<String, String> sampleFields = null;

        String signPreview = null;

        if (ready) {

            String sampleInvoice = SepayPaymentServiceImpl.invoiceNumberForRegistration(0);

            sampleFields = service.buildCheckoutFields(

                    request, user, sampleInvoice, "C", "TEST-SESSION", new BigDecimal("100000"));

            signPreview = SepaySignatureUtil.buildSignPayloadPreview(sampleFields);

        }



        response.setCharacterEncoding(StandardCharsets.UTF_8.name());

        response.setContentType("text/html;charset=UTF-8");



        try (PrintWriter out = response.getWriter()) {

            out.write("<!DOCTYPE html><html lang=\"vi\"><head><meta charset=\"UTF-8\">");

            out.write("<title>[DEV] Kiểm tra SEPay</title></head>");

            out.write("<body style=\"font-family:system-ui;padding:1.5rem\">");

            out.write("<p style=\"color:#b00020\"><b>Chế độ debug — không dùng trên production công khai.</b></p>");

            out.write("<h1>Kiểm tra cấu hình SEPay</h1><ul>");

            out.write("<li>Đọc file env: <b>" + escape(EnvLoader.getLoadSummary()) + "</b></li>");

            out.write("<li>Merchant: <b>" + (configured ? "OK" : "THIẾU / SAI") + "</b> ");

            out.write(escape(maskMerchant(SepayConfig.getMerchantId())) + "</li>");

            out.write("<li>Secret: <b>" + (configured ? "đã cấu hình" : "THIẾU") + "</b> (không hiển thị)</li>");

            out.write("<li>sepay.env: <b>" + escape(SepayConfig.getEnv()) + "</b></li>");

            String envWarn = SepayConfig.environmentMismatchWarning();

            if (envWarn != null) {

                out.write("<li style=\"color:#b00020\"><b>Lệch môi trường:</b> " + escape(envWarn) + "</li>");

            }

            out.write("<li>appBaseUrl (IPN): <b>" + escape(SepayConfig.getAppBaseUrl()) + "</b> "

                    + (hasBase ? "(OK)" : "(THIẾU)") + "</li>");

            out.write("<li>returnBaseUrl: <b>" + escape(SepayConfig.getReturnBaseUrl()) + "</b></li>");

            out.write("<li>checkoutUrl: <b>" + escape(SepayConfig.getCheckoutUrl()) + "</b></li>");

            out.write("<li>IPN URL: <b>" + escape(SepayConfig.ipnUrl()) + "</b></li>");

            out.write("<li>Sẵn sàng: <b>" + (ready ? "CÓ" : "KHÔNG") + "</b></li>");

            if (configError != null) {

                out.write("<li>Lỗi kỹ thuật: <span style=\"color:#b00020\">" + escape(configError) + "</span></li>");

            }

            out.write("</ul>");

            if (signPreview != null) {

                out.write("<h2>Chuỗi ký mẫu</h2><pre style=\"background:#f4f4f4;padding:1rem;overflow:auto\">"

                        + escape(signPreview) + "</pre>");

            }

            out.write("<p><a href=\"" + request.getContextPath() + "/\">Về trang chủ</a></p>");

            out.write("</body></html>");

        }

    }



    /** Che bớt merchant ID khi hiển thị debug (chỉ 8 ký tự đầu). */
    private static String maskMerchant(String merchantId) {

        if (merchantId == null || merchantId.length() < 8) {

            return merchantId != null ? merchantId : "";

        }

        return merchantId.substring(0, 8) + "…";

    }



    private static String escape(String s) {

        if (s == null) {

            return "";

        }

        return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");

    }

}



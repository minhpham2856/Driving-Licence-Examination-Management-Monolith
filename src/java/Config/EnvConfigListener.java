package Config;

import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;
import java.nio.file.Paths;

/**
 * <b>Listener chạy một lần khi Tomcat deploy WAR</b> — nạp file .env trước khi servlet xử lý request.
 *
 * <p>Servlet không tự đọc .env; phải có listener này (hoặc gọi {@link EnvLoader#reload()} thủ công)
 * thì {@link SepayConfig} mới có merchant/secret/URL.</p>
 *
 * <p>Thứ tự đăng ký đường dẫn (file sau ghi đè file trước trong EnvLoader):</p>
 * <ol>
 *   <li>{@code WEB-INF/others/config/sepay.env} — bản copy khi deploy</li>
 *   <li>{@code web.xml} param {@code dlem.env.path} — thường trỏ root {@code .env} của project</li>
 * </ol>
 *
 * <p>Sau {@link EnvLoader#reload()}, in ra console để dev kiểm tra SEPay đã configured chưa.</p>
 */
@WebListener
public class EnvConfigListener implements ServletContextListener {

    /**
     * Tomcat gọi khi ứng dụng khởi động — đăng ký path .env và reload cache.
     */
    @Override
    public void contextInitialized(ServletContextEvent sce) {
        ServletContext context = sce.getServletContext();

        // File đóng gói trong WAR — dùng khi deploy server không có file .env ở root project
        String sepayEnv = context.getRealPath("/WEB-INF/others/config/sepay.env");
        if (sepayEnv != null) {
            EnvLoader.addPath(Paths.get(sepayEnv));
        }

        // Ưu tiên cao hơn sepay.env trong WAR (xem EnvLoader.partitionCandidatePaths)
        String initPath = context.getInitParameter("dlem.env.path");
        if (initPath != null && !initPath.isBlank()) {
            EnvLoader.addPath(Paths.get(initPath));
        }

        EnvLoader.reload();

        String summary = EnvLoader.getLoadSummary();
        context.log("[DLEM] " + summary);
        System.out.println("[DLEM] " + summary);
        System.out.println("[DLEM] SEPay configured=" + SepayConfig.isConfigured()
                + ", sepay.env=" + SepayConfig.getEnv()
                + ", checkoutUrl=" + SepayConfig.getCheckoutUrl()
                + ", appBaseUrl=" + SepayConfig.getAppBaseUrl()
                + ", returnBaseUrl=" + SepayConfig.getReturnBaseUrl());
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        // Không cần giải phóng — map .env là static, Tomcat dừng là hết process
    }
}

package listener;

import shared.util.ConfigManager;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;
import java.nio.file.Paths;

/** Nạp web/WEB-INF/.env khi Tomcat khởi động (bổ sung cho .env ở thư mục project). */
@WebListener
public class AppConfigListener implements ServletContextListener {

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        String webInfEnv = sce.getServletContext().getRealPath("/WEB-INF/.env");
        if (webInfEnv != null) {
            ConfigManager.registerEnvFile(Paths.get(webInfEnv));
        }
        ConfigManager.reload();
    }
}

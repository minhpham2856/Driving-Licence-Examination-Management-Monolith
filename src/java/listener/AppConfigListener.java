package listener;

import util.ConfigManager;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;

import java.nio.file.Paths;

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
